"""Actual conservative axial cells: no fitted trajectory, image radius or late gain."""
from pathlib import Path
import csv,json,sys,subprocess
import numpy as np
from scipy.special import erf
from scipy.ndimage import gaussian_filter
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
sys.path.insert(0,str(OLD/'work'))
sys.path.insert(0,str(R))
from camera_s4 import gate
from render_two_zone import spectrum,HC,K,MCU
cases=sys.argv[1:];case='-'.join(cases)
def read_saved(seg):
    with (R/'results'/f'{seg}_axial.csv').open() as f:
        rd=csv.DictReader(f);names=rd.fieldnames;rows=list(rd)
    return {n:np.array([float(r[n]) for r in rows]) for n in names}
d=read_saved(cases[0])
for seg in cases[1:]:
    q=read_saved(seg);keep=d['time_s']<q['time_s'][0]-1e-15
    d={n:np.r_[v[keep],q[n]] for n,v in d.items()}
t=d['time_s'];ns=t*1e9;nc=int(d['axialCellCount'][0]);prefixes=['cell'+chr(65+j)+'.' for j in range(nc)]
bad=[n for n,v in d.items() if not np.isfinite(v).all()]
bad += [p+n for p in prefixes for n in ['rhoP','Tg','TeK'] if np.any(d[p+n]<=0)]
if bad:
    (R/'results'/f'{case}_axial_invalid_fields.json').write_text(json.dumps({'invalid_fields':bad},indent=2))
    sys.exit('Nonfinite or nonpositive thermodynamic fields; no synthetic comparison generated')
ene=d['axialTotalEnergy'];ein=d['cellA.axialInputEnergy*energyScale'];radloss=d['cellA.axialRadiatedEnergy*energyScale'];eout=d['cellA.axialOutEnergy*energyScale']
eres=(ene-ene[0])+radloss+eout-ein;cm=d['axialCopper'];cin=d['cellA.axialInputCu*mScale'];cout=d['cellA.axialOutputCu*mScale'];cres=cm-cm[0]+cout-cin
report=dict(case=case,cells=nc,saved_end_ns=float(ns[-1]),energy_error_percent=float(100*eres[-1]/max(ein[-1],1e-30)),copper_error_percent=float(100*cres[-1]/max(cin[-1],1e-30)),native_plasma_in_each_cell=True,axial_FV_transport=True,transverse_dynamics_resolved=False,target_motion_resolved=False,source='H04 actual saved thermal history; no gas-to-target feedback',area_closure='pi*(11um^2+z^2); prescribed stream-tube geometry, no image-derived trajectory',negative_mass_fractions={n:float(v.min()) for n,v in d.items() if '.w' in n and v.min()<0},limitations=['First-order Rusanov axial flux; numerical mixing may affect cooling','Expanding cross section is assumed, not solved radial momentum','Target history prescribed; no return flux or gas heat feedback','Cu high-charge excited levels and dielectronic recombination omitted','Per-cell escape approximation; photon exchange between cells not in chemistry'])
if 'prescribedUpstreamOutflow' in d:
    report['source']='Actual G21-G22-G23-G26-G44 source-reservoir outflow; no downstream feedback; no downstream laser heating'
    report['limitations'].append('Upstream source has singly-ionized Cu truncation; downstream Cu0-Cu4+ does not remove that upstream limitation')
out=R/'figures'/case;out.mkdir(exist_ok=True)
z=np.array([d[p+'zCenter'][0] for p in prefixes])*1e6;fields={label:np.array([d[p+v] for p in prefixes]) for label,v in [('Cu','rhoCopper'),('Tg','Tg'),('Te','TeK'),('u','u')]};fields['ne']=np.array([d[p+'plasma'+chr(65+j)+'.ne'] for j,p in enumerate(prefixes)])
fig,ax=plt.subplots(2,3,figsize=(13,7),layout='constrained')
for a,(n,field) in zip(ax.flat,fields.items()):
    if n in ('Cu','ne'):field=np.log10(np.maximum(field,1e-30));title=n+' log10(SI)'
    else:title=n+(' [m/s]' if n=='u' else ' [K]')
    im=a.pcolormesh(ns-118,z,field,shading='auto');a.set_title(title);a.set_xlabel('Experimental time [ns]');a.set_ylabel('Height from copper [um]');fig.colorbar(im,ax=a)
ax[1,2].plot(ns-118,eres/max(ein[-1],1e-30)*100,label='Energy');ax[1,2].plot(ns-118,cres/max(cin[-1],1e-30)*100,label='Cu');ax[1,2].legend();ax[1,2].set_ylabel('Balance error [%]');ax[1,2].set_xlabel('Experimental time [ns]')
fig.suptitle(f'{case}: native Plasma + conservative axial transport; assumed stream-tube area');fig.savefig(out/'axial_fields.png',dpi=160);plt.close(fig)
exp=np.load(OLD/'experiment/S4_reconstructed.npz');times=exp['times_ns'];px=float(exp['pixel_um']);h,w=exp['gray'].shape[1:];valid=(times+113>=ns[0]-1e-5)&(times+123<=ns[-1]+1e-5);times=times[valid]
if not len(times):
    (R/'results'/f'{case}_axial_summary.json').write_text(json.dumps(report,indent=2));sys.exit('No complete exposure yet')
needed=np.zeros(len(ns),bool)
for tt in times+118:
    lo=max(0,np.searchsorted(ns,tt-5,side='right')-1);hi=min(len(ns),np.searchsorted(ns,tt+5)+1);needed[lo:hi]=True
indices=np.flatnonzero(needed);x=(np.arange(w)+.5-w/2)*px;zi=(h-np.arange(h)-.5)*px;nodes,weights=np.polynomial.legendre.leggauss(80)
raw=[];lower=np.array([d[p+'zLower'][0] for p in prefixes]);upper=d[prefixes[-1]+'zUpper'][0]
for k in indices:
    im=np.zeros((h,w))
    for dzsub in [-.25,.25]:
      for iz,zpix in enumerate((zi+dzsub*px)*1e-6):
        j=np.searchsorted(lower,zpix,side='right')-1
        if j<0 or zpix>upper:continue
        p=prefixes[j];radius=np.sqrt((11e-6)**2+zpix*zpix);b=np.sqrt(2*K*d[p+'Tg'][k]/MCU);grad=d[p+'u'][k]*zpix/(radius*radius)
        for dxsub in [-.25,.25]:
            xx=(x+dxsub*px)*1e-6;use=abs(xx)<radius
            if not use.any():continue
            y=np.sqrt(radius*radius-xx[use]**2);length=2*y;limit=abs(grad)*y+7*b;vel=limit[:,None]*nodes
            if abs(grad)*max(y)<1e-5*b:shape=length[:,None]*np.exp(-(vel/b)**2)
            else:shape=b*np.sqrt(np.pi)/(2*grad)*(erf((vel+grad*y[:,None])/b)-erf((vel-grad*y[:,None])/b))
            band=np.zeros(len(y))
            for li in range(3,7):
                jj=max(d[p+f'jline{li}'][k],0);alpha=max(d[p+f'tauLine{li}/'+p+'Req'][k],0);tau=alpha*shape;factor=np.ones_like(tau);np.divide(-np.expm1(-tau),tau,out=factor,where=tau>1e-8)
                band+=limit*np.sum(jj/(np.sqrt(np.pi)*b)*shape*factor*weights,axis=1)
            for lam in (475+(np.arange(24)+.5)*50/24)*1e-9:
                jj,alpha=spectrum(d,p,k,lam);tau=alpha*length;factor=np.ones_like(tau);np.divide(-np.expm1(-tau),tau,out=factor,where=tau>1e-8);band+=jj*length*factor*(HC/(1.602176634e-19*lam*lam)*50e-9/24)
            im[iz,use]+=band/4
    raw.append(im)
raw=np.array(raw);rad=gaussian_filter(gate(ns[indices],raw,times+118),[0,1/2.354820045,1/2.354820045],mode='constant');photons=rad*(2*np.pi*(1-np.sqrt(1-.4**2)))*(px*1e-6)**2*10e-9*525e-9/HC
np.savez_compressed(R/'results'/f'{case}_global_images.npz',experiment_times_ns=times,radiance=rad,photons_upper_525nm=photons,x_um=x,z_um=zi,instant_model_times_ns=ns[indices],instant_radiance=raw,model_kind='native axial FV Plasma; prescribed stream-tube cross section; one-way thermal predictor')
report.update(complete_times_ns=times.tolist(),peak_total_time_ns=float(times[photons.sum((1,2)).argmax()]),peak_pixel_time_ns=float(times[photons.max((1,2)).argmax()]),photon_total=photons.sum((1,2)).tolist(),photon_peak_pixel=photons.max((1,2)).tolist())
(R/'results'/f'{case}_axial_summary.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
subprocess.run([sys.executable,'-X','utf8',str(R/'calibrate_global_camera.py'),case],check=True)
if (R/'results'/f'{case}_global_camera/images.npz').exists():
    subprocess.run([sys.executable,'-X','utf8',str(R/'paper_style_panel.py'),case],check=True)
