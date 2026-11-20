"""T004: diagnostic quadratures only; never run hydro, camera, or fitting."""
from pathlib import Path
import os
for key in ('OMP_NUM_THREADS', 'OPENBLAS_NUM_THREADS', 'MKL_NUM_THREADS'):
    os.environ[key] = '1'
import ast, csv, hashlib, json, shutil, sys, time
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

REPO = Path(__file__).resolve().parents[2]
SOURCE = Path('D:/junlin/PlasmaPlumeHighInfluence/runs/case_00326_coarse')
OUT = REPO / 'results/T004'
ARCHIVE = Path(__file__).parent / 'archived'
KB = 1.380649e-23
EV = 1.602176634e-19
MCU = 1.055206e-25
H = 6.62607015e-34
C = 299792458.
HC = H*C
CHI = 7.72638*EV/MCU

def safe_json(obj):
    if isinstance(obj, dict): return {k: safe_json(v) for k,v in obj.items()}
    if isinstance(obj, (list, tuple)): return [safe_json(v) for v in obj]
    if isinstance(obj, (float, np.floating)): return float(obj) if np.isfinite(obj) else None
    if isinstance(obj, np.integer): return int(obj)
    if isinstance(obj, np.bool_): return bool(obj)
    return obj

def coefficients(T, rho, y, ion, upper=3.817):
    neutral=rho*(y-ion)/MCU; ne=rho*ion/MCU
    fraction=2*np.exp(-upper*EV/(KB*T))
    emiss=neutral*fraction/(1+fraction)*2e6*(HC/(510.55e-9))/(4*np.pi)
    band=np.exp(-HC/(520e-9*KB*T))-np.exp(-HC/(480e-9*KB*T))
    ff=1.426e-40*np.sqrt(T)*ne*ne*band/(4*np.pi)
    return emiss,ff

def conduction(T,rho,y,ion,p,mask,rb,zb):
    # Reconstruct archived TC flux on the cell-face surface of Y_Cu>0.01.
    # Reflective/axis and zero-gradient outer boundaries have zero normal flux.
    rc=rb.mean(1); zc=zb.mean(1); dr=np.diff(rb,axis=1).ravel(); dz=np.diff(zb,axis=1).ravel()
    pad=np.pad(T,1,mode='edge')
    trans_z=(pad[2:,1:-1]-pad[:-2,1:-1])/(2*dz[:,None])
    trans_r=(pad[1:-1,2:]-pad[1:-1,:-2])/(2*dr[None,:])
    totals=[0.,0.]; pieces=[]
    for axis,centers,trans,area in [(1,rc,trans_z,2*np.pi*rb[:-1,1][None,:]*dz[:,None]),
                                   (0,zc,trans_r,np.pi*(rb[:,1]**2-rb[:,0]**2)[None,:])]:
        left=[slice(None),slice(None)]; right=left.copy()
        left[axis]=slice(None,-1);right[axis]=slice(1,None);left=tuple(left);right=tuple(right)
        avg=lambda a: .5*(a[left]+a[right])
        rf=avg(rho); yf=np.clip(avg(y),0,1); qf=np.clip(avg(ion),0,yf); pf=avg(p)
        Tf=np.maximum(pf/(rf*((1-yf)*287.05+(yf+qf)*KB/MCU)),50.)
        kap=(1-yf)*.026*(Tf/300)**.7+yf*.02*np.sqrt(Tf/300)+qf*1.84e-10*Tf**2.5/5
        dist=np.diff(centers)[None,:] if axis==1 else np.diff(centers)[:,None]
        grad=(T[right]-T[left])/dist
        magnitude=np.sqrt(grad*grad+avg(trans)**2)
        # Archived van Leer pressure slopes and hot-side saturation pressure.
        pp=np.pad(p,((1,1),(0,0)) if axis==0 else ((0,0),(1,1)),mode='edge')
        dm=np.diff(pp,axis=axis)
        a=np.take(dm,range(p.shape[axis]),axis=axis)
        b=np.take(dm,range(1,p.shape[axis]+1),axis=axis)
        slope=np.where(a*b>0,2*a*b/np.where(abs(a+b)>0,a+b,1),0.)
        up=np.where(grad>0,(p-.5*slope)[right],(p+.5*slope)[left])
        sat=1.5*np.maximum(up,0)**1.5/np.sqrt(rf)
        factor=sat/np.maximum(sat+kap*magnitude,1e-300)
        normal=(mask[left].astype(float)-mask[right].astype(float))
        classical=-kap*grad*area*normal
        limited=classical*factor
        totals[0]+=float(classical.sum());totals[1]+=float(limited.sum())
        pieces.append(float(np.abs(limited).sum()))
    return totals[0],totals[1],sum(pieces)

def main():
    started=time.perf_counter(); OUT.mkdir(parents=True,exist_ok=True); ARCHIVE.mkdir(exist_ok=True)
    copied={}
    for name in ('postprocess.py','common.py'):
        src=REPO/'prior/pluto/scripts'/name; dst=ARCHIVE/name
        shutil.copy2(src,dst); copied[name]=hashlib.sha256(dst.read_bytes()).hexdigest()
    # Execute ONLY the three original read functions, no original fit/project routines.
    tree=ast.parse((ARCHIVE/'postprocess.py').read_text(encoding='utf-8'))
    selected=[n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name in {'load_grid','outputs','field'}]
    scope={'np':np}; exec(compile(ast.Module(body=selected,type_ignores=[]),str(ARCHIVE/'postprocess.py'),'exec'),scope)
    sys.path.insert(0,str(ARCHIVE)); from common import read_json
    axes=scope['load_grid'](SOURCE); rb,zb,_=axes; entries=scope['outputs'](SOURCE)
    sources=[SOURCE/'grid.out',SOURCE/'flt.out',SOURCE/'budget.csv',SOURCE/'params.json']+[SOURCE/f'data.{e[0]:04d}.flt' for e in entries]
    snapshot=lambda: {str(p):[p.stat().st_size,p.stat().st_mtime_ns] for p in sources}
    before=snapshot()
    params=read_json(SOURCE/'params.json')['parameters']; width=params['line_width_nm']*1e-9
    exp=np.load('D:/junlin/PlasmaPlumeHighInfluence/data/experimental/s4_frames.npz')
    w=exp['gray'].shape[2]; pix=float(exp['pixel_um'])
    x=(np.arange(w)-(w-1)/2)*pix; zgrid=np.arange(0,1100+pix,pix)
    ir=np.flatnonzero(rb.mean(1)<900); iz=np.flatnonzero(zb.mean(1)<1150); crop=np.ix_(iz,ir)
    r=rb[ir]; z=zb.mean(1)[iz]
    chord=(np.sqrt(np.maximum(r[:,1,None]**2-x[None,:]**2,0))-np.sqrt(np.maximum(r[:,0,None]**2-x[None,:]**2,0)))*1e-6
    vol=np.pi*(rb[:,1]**2-rb[:,0]**2)[None,:]*np.diff(zb,axis=1)*1e-18
    rows=[]
    for entry in entries:
        f={k:np.asarray(v,dtype=float) for k,v in scope['field'](SOURCE,entry,(len(zb),len(rb))).items()}
        rho=np.maximum(f['rho'],0); T=np.maximum(f['temperature'],50); y=np.clip(f['tr1'],0,1); ion=np.clip(f['tr2'],0,y)
        mask=y>.01; mass=rho*y*vol; p=f['prs']*1e6
        j,ff=coefficients(T,rho,y,ion); j6,_=coefficients(T,rho,y,ion,6.19)
        cv=(1-y)*717.625+1.5*(y+ion)*KB/MCU
        thermal=rho*cv*T; eion=rho*ion*CHI; kinetic=.5*rho*sum(f[v]**2 for v in ('vx1','vx2','vx3'))*1e6
        B=2*H*C**2/(510.55e-9)**5/np.expm1(np.minimum(HC/(510.55e-9*KB*T),700))*width
        opacity=j/np.maximum(B,1e-100)
        line=np.where((T>=500)&(y>ion),4*np.pi*j/(1+4*np.pi*j/np.maximum(4*np.pi*B,4*np.pi*1e-30)*50e-6),0)
        ne=rho*ion/MCU; ff_all=1.426e-40*np.sqrt(T)*ne**2
        rr=2.6e-19*(np.maximum(T,300)/1e4)**(-.7)
        recomb=rr*ne*ne*(7.72638*EV+1.5*KB*T)
        rad=(line+ff_all+recomb)*params['cooling_scale']
        Pc,Pl,Pabs=conduction(T,rho,y,ion,p,mask,rb*1e-6,zb*1e-6)
        line_image=np.zeros((len(iz),w))
        for indices in (range(len(ir)-1,-1,-1),range(len(ir))):
            for i in indices:
                tau=opacity[crop][:,i,None]*chord[i][None,:]
                absorbed=-np.expm1(-np.minimum(tau,700))
                line_image=line_image*(1-absorbed)+B[crop][:,i,None]*absorbed
        ff_image=ff[crop]@(2*chord)
        images=[line_image+ff_image,(j[crop]+ff[crop])@(2*chord),(j6[crop]+ff[crop])@(2*chord)]
        signals=[]
        for image in images:
            image_z=np.stack([np.interp(zgrid,z,image[:,col],left=image[0,col],right=0) for col in range(w)],1)
            signals.append(float(image_z.sum()*(pix*1e-6)**2))
        V=float(vol[mask].sum()); Eth=float((thermal*vol)[mask].sum()); Ei=float((eion*vol)[mask].sum()); Pr=float((rad*vol)[mask].sum())
        row=dict(time_ns=entry[1],cu_mass_ng=mass.sum()*1e12,cu_mass_mean_temperature_K=(T*mass).sum()/mass.sum() if mass.sum()>0 else np.nan,
                 emissivity_mean_temperature_K=(T*j*vol).sum()/(j*vol).sum() if (j*vol).sum()>0 else np.nan,
                 max_temperature_K=T.max(),region_volume_m3=V,equivalent_sphere_radius_um=(3*V/(4*np.pi))**(1/3)*1e6,
                 mean_pressure_Pa=(p*vol)[mask].sum()/V if V else np.nan,thermal_energy_uJ=Eth*1e6,ionization_energy_uJ=Ei*1e6,
                 kinetic_energy_uJ=(kinetic*vol)[mask].sum()*1e6,radiation_power_W=Pr,line_power_W=(line*vol)[mask].sum(),
                 freefree_power_W=(ff_all*vol)[mask].sum(),recombination_power_W=(recomb*vol)[mask].sum(),
                 conduction_classical_power_W=Pc,conduction_limited_power_W=Pl,conduction_absolute_power_W=Pabs,
                 radiation_cooling_time_ns=(Eth+Ei)/Pr*1e9 if Pr>0 else np.nan,conduction_cooling_time_ns=(Eth+Ei)/Pl*1e9 if Pl>0 else np.nan,
                 signal_thick_W_sr=signals[0],signal_thin_W_sr=signals[1],signal_thin_619_W_sr=signals[2],max_line_tau=(opacity[crop]@(2*chord)).max(),
                 region_touches_outer_boundary=bool(mask[-1].any() or mask[:,-1].any()))
        for lo,hi,label in [(0,3000,'lt3000'),(3000,6000,'3000_6000'),(6000,8000,'6000_8000'),(8000,10000,'8000_10000'),(10000,np.inf,'ge10000')]:
            row['cu_mass_'+label+'_ng']=mass[(T>=lo)&(T<hi)].sum()*1e12
        rows.append(row)
    ts=np.array([r['time_ns'] for r in rows]); efolds={}
    for key in ('thick','thin','thin_619'):
        sig=np.array([r['signal_'+key+'_W_sr'] for r in rows]); ref=np.interp(300,ts,sig); end=np.interp(630,ts,sig)
        efolds[key]={'signal_300_W_sr':ref,'signal_630_W_sr':end,'ratio_630_300':end/ref,'endpoint_efold_ns':330/np.log(ref/end)}
        for row,val in zip(rows,sig): row['signal_'+key+'_normalized_300']=val/ref
    with (OUT/'forensic_timeseries.csv').open('w',newline='',encoding='utf-8') as fp:
        wr=csv.DictWriter(fp,fieldnames=list(rows[0]));wr.writeheader();wr.writerows(rows)
    budget=np.genfromtxt(SOURCE/'budget.csv',delimiter=',',names=True); checks=[]
    for idx in [10,40,60,120,200]:
        row=rows[idx]; expected=np.interp(row['time_ns'],budget['time_ns'],budget['cu_mass_ng']); relative=abs(row['cu_mass_ng']/expected-1)
        checks.append(dict(time_ns=row['time_ns'],field_mass_ng=row['cu_mass_ng'],budget_mass_ng=expected,relative_error=relative,pass_=relative<.01))
    def figure(name,keys,ylabel,log=False):
        fig,ax=plt.subplots(figsize=(8,4.5))
        for key,label in keys: ax.plot(ts,[r[key] for r in rows],label=label)
        ax.set(xlabel='Time (ns)',ylabel=ylabel);ax.grid(alpha=.25);ax.legend(fontsize=8)
        if log: ax.set_yscale('log')
        fig.tight_layout();fig.savefig(OUT/name,dpi=160);plt.close(fig)
    figure('temperature_bins.png',[(k,k.removeprefix('cu_mass_').removesuffix('_ng')+' K') for k in rows[0] if k.startswith('cu_mass_') and k.endswith('_ng') and k!='cu_mass_ng'],'Copper mass (ng)')
    figure('emission_temperature.png',[('cu_mass_mean_temperature_K','Cu mass weighted'),('emissivity_mean_temperature_K','Line emissivity weighted'),('max_temperature_K','Maximum')],'Temperature (K)')
    figure('cooling_times.png',[('radiation_cooling_time_ns','Radiation'),('conduction_cooling_time_ns','Conduction, flux limited')],'(Thermal + ionization energy) / power (ns)',True)
    figure('normalized_signals.png',[(f'signal_{k}_normalized_300',lab) for k,lab in [('thick','Archived thick 3.817 eV'),('thin','Thin 3.817 eV'),('thin_619','Thin 6.19 eV')]],'Signal / signal at 300 ns')
    summary=dict(task='T004',case=str(SOURCE),model_parameters_unchanged=params,n_outputs=len(rows),time_range_ns=[ts[0],ts[-1]],
                 passed=all(c['pass_'] for c in checks) and len(rows)>=50 and ts[0]==0 and ts[-1]>=1000 and before==snapshot(),
                 mass_checks=checks,source_size_mtime_unchanged=before==snapshot(),copied_scripts_sha256=copied,signal_decay=efolds,
                 selected_times=[rows[int(np.argmin(abs(ts-t)))] for t in [20,100,200,300,400,630,700,1000]],
                 definitions={'region':'Y_Cu > 0.01; volume weighted pressure; equivalent FULL SPHERE radius',
                  'temperature':'Emissivity weighted by intrinsic archived 510.55 nm line emissivity times cylindrical volume, not escaped intensity',
                  'energy':'Absolute thermal and ionization energy of all gas in mask; no ambient subtraction; kinetic excluded from cooling time',
                  'conduction':'Archived kappa, face primitive average, EOS temperature, van Leer pressure, full-gradient saturation; cell-face mask surface; positive outward; snapshot estimate, not a stored time-stage flux',
                  'radiation':'Instantaneous archived free-free + radiative recombination + 50 um escape-length line; before source-substep energy cap',
                  'signals':'Exact archived crop, pixel size, chord LOS and z interpolation; sum image times pixel area in W/sr; NO camera, gate, gain, clipping or fit',
                  '619_scenario':'Only upper excitation energy changed to 6.19 eV. Wavelength, A, degeneracy and continuum unchanged; sensitivity diagnostic, not atomic model for real 515/522 nm lines',
                  'efold':'330 ns / ln(S(300)/S(630)); endpoints linearly interpolated, no regression or model fitting',
                  'bins':'Half-open intervals; highest bin includes exactly 10000 K'},elapsed_s=time.perf_counter()-started)
    (OUT/'forensic_summary.json').write_text(json.dumps(safe_json(summary),indent=2,ensure_ascii=False),encoding='utf-8')
    print(json.dumps(safe_json({k:summary[k] for k in ['passed','n_outputs','time_range_ns','signal_decay','elapsed_s']}),indent=2))

if __name__=='__main__': main()
