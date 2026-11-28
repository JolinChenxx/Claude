"""Coarse solved axisymmetric finite volumes with layered optical transfer."""
from pathlib import Path
import os,sys,csv,json,subprocess
os.environ['OPENBLAS_NUM_THREADS']='1'
import numpy as np
from scipy.special import erf
from scipy.ndimage import gaussian_filter
from concurrent.futures import ProcessPoolExecutor
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
sys.path.insert(0,str(R));sys.path.insert(0,str(OLD/'work'))
from camera_s4 import gate
from render_two_zone import spectrum,HC,K,MCU

def suffix(j):
    s=""
    while j>=0:
        s=chr(65+j%26)+s;j=j//26-1
    return s

def initialize(d,nr,nz,x,z,px):
    global D,NR,NZ,X,Z,PX,PREF,NODES,WEIGHTS
    D=d;NR=nr;NZ=nz;X=x;Z=z;PX=px;PREF=['cell'+suffix(j)+'.' for j in range(nr*nz)]
    NODES,WEIGHTS=np.polynomial.legendre.leggauss(80)

def render_time(k):
    d=D;im=np.zeros((len(Z),len(X)));lower=np.array([d[PREF[q*NR]+'zLower'][0] for q in range(NZ)]);upper=d[PREF[-1]+'zUpper'][0]
    for dz in [-.25,.25]:
      for iz,zz in enumerate((Z+dz*PX)*1e-6):
        q=np.searchsorted(lower,zz,side='right')-1
        if q<0 or zz>upper:continue
        ps=PREF[q*NR:(q+1)*NR]
        for dx in [-.25,.25]:
            xx=(X+dx*PX)*1e-6;use=abs(xx)<d[ps[-1]+'rUpper'][0]
            if not use.any():continue
            xx=xx[use];rings=[];limit=np.zeros(len(xx))
            for p in ps:
                ro=d[p+'rUpper'][0];ri=d[p+'rLower'][0];hi=np.sqrt(np.maximum(ro*ro-xx*xx,0));lo=np.sqrt(np.maximum(ri*ri-xx*xx,0))
                b=np.sqrt(2*K*d[p+'Tg'][k]/MCU);g=d[p+'uR'][k]/d[p+'rCenter'][0]
                rings.append((p,b,g,lo,hi));limit=np.maximum(limit,abs(g)*hi+7*b)
            vel=limit[:,None]*NODES
            def shape(b,lo,hi,g):
                if abs(g)*max(float(np.max(abs(lo))),float(np.max(abs(hi))))<1e-5*b:return (hi-lo)[:,None]*np.exp(-(vel/b)**2)
                return b*np.sqrt(np.pi)/(2*g)*(erf((vel-g*lo[:,None])/b)-erf((vel-g*hi[:,None])/b))
            layers=[]
            for p,b,g,lo,hi in rings[::-1]:layers.append((p,b,shape(b,-hi,-lo,g),hi-lo))
            for p,b,g,lo,hi in rings:layers.append((p,b,shape(b,lo,hi,g),hi-lo))
            band=np.zeros(len(xx))
            for li in range(3,7):
                intensity=np.zeros_like(vel)
                for p,b,sh,length in layers:
                    jj=max(d[p+f'jline{li}'][k],0);alpha=max(d[p+f'tauLine{li}/'+p+'Req'][k],0);tau=alpha*sh;fac=np.ones_like(tau);np.divide(-np.expm1(-tau),tau,out=fac,where=tau>1e-8)
                    intensity=intensity*np.exp(-tau)+jj/(np.sqrt(np.pi)*b)*sh*fac
                band+=limit*np.sum(intensity*WEIGHTS,axis=1)
            for lam in (475+(np.arange(24)+.5)*50/24)*1e-9:
                intensity=np.zeros(len(xx))
                for p,b,sh,length in layers:
                    jj,alpha=spectrum(d,p,k,lam);tau=alpha*length;fac=np.ones_like(tau);np.divide(-np.expm1(-tau),tau,out=fac,where=tau>1e-8)
                    intensity=intensity*np.exp(-tau)+jj*length*fac
                band+=intensity*(HC/(1.602176634e-19*lam*lam)*50e-9/24)
            im[iz,use]+=band/4
    return im

def main(case):
    with (R/'results'/f'{case}_axial.csv').open() as f:
        rd=csv.DictReader(f);names=rd.fieldnames;rows=list(rd)
    d={n:np.array([float(q[n]) for q in rows]) for n in names};ns=d['time_s']*1e9;offset=float(d.get('cameraZero/1[ns]',np.array([90.]))[0])
    nr=int(d['gridRadialCount'][0]);nz=int(d['gridAxialCount'][0]);ps=['cell'+suffix(j)+'.' for j in range(nr*nz)]
    bad=[n for n,a in d.items() if not np.isfinite(a).all()]+[p+n for p in ps for n in ['Tg','TeK','rhoP'] if np.any(d[p+n]<=0)]
    if bad:raise ValueError('Invalid saved fields: '+repr(bad))
    ene=d['axialTotalEnergy'];ein=d['cellA.axialInputEnergy*energyScale'];eout=d['cellA.axialOutEnergy*energyScale'];radloss=d['cellA.axialRadiatedEnergy*energyScale'];eres=ene-ene[0]+eout+radloss-ein
    cin=d['cellA.axialInputCu*mScale'];cout=d['cellA.axialOutputCu*mScale'];cu=d['axialCopper'];cres=cu-cu[0]+cout-cin
    report=dict(case=case,saved_end_ns=float(ns[-1]),grid=[nr,nz],native_plasma_per_cell=True,axisymmetric_mass_momentum_energy_transport=True,prescribed_plume_radius=False,target_motion=False,copper_error_percent=float(100*cres[-1]/max(cin[-1],1e-30)),energy_error_percent=float(100*eres[-1]/max(ein[-1],1e-30)),copper_outflow_fraction=float(cout[-1]/max(cin[-1],1e-30)),negative_mass_fractions={n:float(a.min()) for n,a in d.items() if '.w' in n and a.min()<0},limitations=['First-order FV transport; spatial convergence not yet checked','Copper heat PDE and dynamic laser shielding, fixed surface','No moving liquid or explicit sheath; direct conductive wall heat not yet included','Equivalent-volume local escape; no intercell radiative feedback in kinetics','Ionic excited states and dielectronic recombination absent','Local linear radial Doppler reconstruction within each optical ring'])
    out=R/'figures'/case;out.mkdir(exist_ok=True)
    re=np.r_[[d[p+'rLower'][0]*1e6 for p in ps[:nr]],d[ps[nr-1]+'rUpper'][0]*1e6];ze=np.r_[[d[ps[q*nr]+'zLower'][0]*1e6 for q in range(nz)],d[ps[-1]+'zUpper'][0]*1e6]
    fields=[('rhoCopper','Cu density log10 [kg/m3]',True),('ne','Electron density log10 [1/m3]',True),('Tg','Gas temperature [K]',False),('TeK','Electron temperature [K]',False)]
    # Never label a nearest endpoint as a future experimental time.
    requested=np.array([0,60,150,400],dtype=float)+offset
    available=requested[(requested>=ns[0])&(requested<=ns[-1])]
    ids=np.unique([np.argmin(abs(ns-q)) for q in available]) if len(available) else np.array([len(ns)-1])
    choose=ns[ids]-offset
    fig,ax=plt.subplots(4,len(choose),figsize=(max(5,3*len(choose)),11),squeeze=False,layout='constrained')
    for row,(field,label,log) in enumerate(fields):
        values=np.array([d[p+('plasma'+suffix(j)+'.ne' if field=='ne' else field)] for j,p in enumerate(ps)])
        if log:values=np.log10(np.maximum(values,1e-30))
        vmax=values[:,ids].max();vmin=max(values[:,ids].min(),vmax-8) if log else 0
        for col,(tt,i) in enumerate(zip(choose,ids)):
            val=values[:,i].reshape(nz,nr);artist=ax[row,col].pcolormesh(np.r_[-re[:0:-1],re],ze,np.c_[val[:,::-1],val],vmin=vmin,vmax=vmax,shading='flat',cmap='turbo',edgecolors=(.5,.5,.5,.3),linewidth=.3)
            ax[row,col].axhline(0,c='black');ax[row,col].set_aspect('equal');ax[row,col].set_xlabel('Radius [um]');ax[row,col].set_ylabel('Height above fixed copper [um]');ax[row,col].set_title(f'{tt:g} ns (model {ns[i]:g})')
        fig.colorbar(artist,ax=ax[row].tolist(),label=label,fraction=.018,pad=.015)
    fig.suptitle(f'{case}: solved coarse axisymmetric FV transport; cell boundaries shown; not spatially converged');fig.savefig(out/'grid_fields.png',dpi=150);plt.close(fig)
    exp=np.load(OLD/'experiment/S4_reconstructed.npz');times=exp['times_ns'];px=float(exp['pixel_um']);h,w=exp['gray'].shape[1:];valid=(times+offset-5>=ns[0]-1e-5)&(times+offset+5<=ns[-1]+1e-5);times=times[valid]
    if not len(times):
        (R/'results'/f'{case}_grid_summary.json').write_text(json.dumps(report,indent=2));return
    need=np.zeros(len(ns),bool)
    for tt in times+offset:
        lo=max(0,np.searchsorted(ns,tt-5,side='right')-1);hi=min(len(ns),np.searchsorted(ns,tt+5)+1);need[lo:hi]=True
    indices=np.flatnonzero(need);x=(np.arange(w)+.5-w/2)*px;z=(h-np.arange(h)-.5)*px
    print('RENDERING',len(indices),'saved times with8 optical workers',flush=True)
    with ProcessPoolExecutor(max_workers=8,initializer=initialize,initargs=(d,nr,nz,x,z,px)) as pool:raw=np.array(list(pool.map(render_time,indices,chunksize=3)))
    rad=gaussian_filter(gate(ns[indices],raw,times+offset),[0,1/2.354820045,1/2.354820045],mode='constant');photons=rad*(2*np.pi*(1-np.sqrt(1-.4**2)))*(px*1e-6)**2*10e-9*525e-9/HC
    np.savez_compressed(R/'results'/f'{case}_global_images.npz',experiment_times_ns=times,radiance=rad,photons_upper_525nm=photons,x_um=x,z_um=z,instant_model_times_ns=ns[indices],instant_radiance=raw,model_kind=f'axisymmetric FV native Plasma; {nr}r x{nz}z; computed thermal/laser feedback, fixed surface',camera_zero_model_ns=offset,spatial_geometry=f'{nr}r x{nz}z axisymmetric finite volumes')
    report.update(complete_times_ns=times.tolist(),peak_total_time_ns=float(times[photons.sum((1,2)).argmax()]),peak_pixel_time_ns=float(times[photons.max((1,2)).argmax()]),photon_total=photons.sum((1,2)).tolist(),photon_peak_pixel=photons.max((1,2)).tolist())
    (R/'results'/f'{case}_grid_summary.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
    subprocess.run([sys.executable,'-X','utf8',str(R/'calibrate_global_camera.py'),case],check=True)
    if (R/'results'/f'{case}_global_camera/images.npz').exists():subprocess.run([sys.executable,'-X','utf8',str(R/'paper_style_panel.py'),case],check=True)
if __name__=='__main__':main(sys.argv[1])
