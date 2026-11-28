"""Native global-plasma diagnostics and explicitly homogeneous-shape images.

The COMSOL populations and expansion variables are solved quantities. Internal
spatial structure is a uniform half-ellipsoid closure, never a spatial prediction.
No per-frame normalization, shape fitting, or emission-support extrapolation.
"""
from pathlib import Path
import csv,json,sys
import numpy as np
from scipy.integrate import cumulative_trapezoid
from scipy.special import erf
from scipy.ndimage import gaussian_filter
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
R=Path(__file__).resolve().parent
OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
sys.path.insert(0,str(OLD/'work'))
from camera_s4 import gate
from extract_experiment import contour_mask
K=1.380649e-23;HC=6.62607015e-34*299792458.;EV=1.602176634e-19
MCU=.063546/6.02214076e23

def load(case):
    with (R/'results'/f'{case}_global.csv').open() as f:
        reader=csv.DictReader(f);names=reader.fieldnames;rows=list(reader)
    return {n:np.array([float(x[n]) for x in rows]) for n in names}

def line_radiance(j,alpha,chord,b,gradient,order=80):
    """Exact homogeneous source function, thermal profile and linear LOS flow."""
    ans=j*chord
    use=(alpha*chord>.001)&(chord>0)
    if not np.any(use):return ans
    length=chord[use];halfvel=abs(gradient)*length/2
    nodes,weights=np.polynomial.legendre.leggauss(order)
    limit=halfvel+7*b;velocity=limit[:,None]*nodes
    if abs(gradient)*length.max()<1e-5*b:
        tau=alpha*length[:,None]*np.exp(-(velocity/b)**2)
    else:
        tau=alpha*b*np.sqrt(np.pi)/(2*abs(gradient))*(erf((velocity+halfvel[:,None])/b)-erf((velocity-halfvel[:,None])/b))
    ans[use]=j/(alpha*np.sqrt(np.pi)*b)*limit*np.sum(-np.expm1(-tau)*weights,axis=1)
    return ans

def render(d,indices,x,z,subpixel=2):
    px=x[1]-x[0];answer=[];thin=[]
    offsets=(np.arange(subpixel)+.5)/subpixel-.5
    for k in indices:
        rp=d['c.Rp'][k];hp=d['c.Hp'][k]
        tg=d['c.Tg'][k];te=d['c.TeK'][k];tev=te*K/EV
        im=np.zeros((len(z),len(x)));imthin=im.copy()
        for dx in offsets:
          for dz in offsets:
            X,Z=np.meshgrid((x+dx*px)*1e-6,(z+dz*px)*1e-6)
            q=1-(X/rp)**2-(Z/hp)**2
            chord=2*rp*np.sqrt(np.maximum(q,0))*(Z>=0)
            band=np.zeros_like(chord);unattenuated=band.copy()
            for li in range(3,7):
                j=d[f'c.jline{li}'][k];alpha=d[f'c.tauLine{li}/c.Req'][k]
                band+=line_radiance(j,alpha,chord,np.sqrt(2*K*tg/MCU),d['c.vRadius'][k]/rp)
                unattenuated+=j*chord
            # Same Kramers-Milne continuum convention as radiation_native.py.
            for lam in (475+(np.arange(32)+.5)*50/32)*1e-9:
                e=HC/(lam*EV);de=HC/(EV*lam*lam)*50e-9/32
                ff=d['c.Qff'][k]/(4*np.pi)
                j=(ff*np.exp(-e/tev)+d['c.Qfb_CuH3'][k]/(4*np.pi)*np.exp(-(e-1.5352049)/tev)+d['c.Qfb_CuH5'][k]/(4*np.pi)*np.exp(-(e-1.5343548)/tev))/tev
                ab=d['c.nCuH3'][k]*6.3e-22*np.sqrt(13.605693/1.5352049)*(1.5352049/e)**3+d['c.nCuH5'][k]*6.3e-22*np.sqrt(13.605693/1.5343548)*(1.5343548/e)**3
                if 'c.tailQfb' in d:
                    j+=d['c.tailQfb'][k]/(4*np.pi*tev)*np.exp(-(e-.8587344)/tev)
                    ab+=d['c.nCuT5'][k]*6.3e-22*np.sqrt(13.605693/.8587344)*(.8587344/e)**3
                nu=299792458./lam
                alpha=ab+ff*299792458.**2/(2*K*te*nu**3)*(-np.expm1(-e/tev))
                tau=alpha*chord
                attenuation=np.ones_like(tau)
                np.divide(-np.expm1(-tau),tau,out=attenuation,where=tau>1e-8)
                band+=j*chord*attenuation*de;unattenuated+=j*chord*de
            im+=band/subpixel**2;imthin+=unattenuated/subpixel**2
        answer.append(im);thin.append(imthin)
    return np.array(answer),np.array(thin)

def main():
    cases=sys.argv[1:];case='-'.join(cases);d=load(cases[0])
    for segment in cases[1:]:
        more=load(segment);keep=d['time_s']<more['time_s'][0]-1e-15
        d={n:np.r_[values[keep],more[n]] for n,values in d.items()}
    t=d['time_s'];ns=t*1e9
    out=R/'figures'/case;out.mkdir(exist_ok=True)
    def c(n):return d['c.'+n]
    Cu=c('rhoCopper')*c('Vp');Cu0=Cu[0];removed=c('removedMass*mScale')
    energy=c('gasEnergy*energyScale')+c('intCu(rhoCu*c.hCond)')
    swept_shell='c.shellStoredEnergy' in d
    fed_source='c.feedSourceMode' in d
    if swept_shell:energy+=c('shellStoredEnergy')+c('Kinetic')
    base=cumulative_trapezoid(c('intBase(-c.kCu*(c.Tr*nr+c.Tz*nz))'),t,initial=0)
    ledger=energy-energy[0]+c('radiatedEnergy*energyScale')+c('ambientWork*energyScale')-c('entrainedEnergy*energyScale')+base-c('absorbedEnergy*energyScale')
    net_air=c('Mgas')-Cu
    if swept_shell:net_air+=c('shellMass*mScale')
    mass_air_error=net_air-net_air[0]-cumulative_trapezoid(c('Msweep'),t,initial=0)
    denom=max(float(c('absorbedEnergy*energyScale')[-1]),1e-30)
    report=dict(case=case,native_global_plasma=True,spatial_plasma_solution=False,saved_end_ns=float(ns[-1]),n_saved=len(t),limitations=['Uniform half-ellipsoid geometry; no resolved bright core, target gap or sheath','Inherited high-temperature condensed-copper properties are not validated near the critical region','Quasineutral surface ion/electron injection is an effective closure','No independent electron-density validation'],copper_error_percent=100*float(Cu[-1]-Cu0-(removed[-1]-removed[0]))/max(float(abs(removed[-1]-removed[0])),1e-30),energy_error_percent=100*float(ledger[-1])/denom,ambient_mass_error_kg=float(mass_air_error[-1]),max_surface_K=float(c('maxTarget(c.T)').max()),max_ne_m3=float(c('plas.ne').max()),radius_max_um=float(c('Rp').max()*1e6),height_max_um=float(c('Hp').max()*1e6),initial_ne_m3=float(c('plas.ne')[0]),initial_Te_K=float(c('TeK')[0]))
    incident=cumulative_trapezoid(c('Pincident'),t,initial=0)
    report['incident_energy_in_saved_window_uJ']=float(incident[-1]*1e6)
    report['incident_energy_at_experiment_zero_uJ']=float(np.interp(118,ns,incident)*1e6) if ns[0]<=118<=ns[-1] else None
    report['laser_input_description']='Unmeasured finite60ns envelope from model118ns; timing sensitivity' if cases[0] in ('G10','G18','G24') else 'Inherited unmeasured Gaussian, peak model120ns, FWHM60ns'
    report['ambient_mixing_closure']='Separate swept-air inertial shell; only initial air mixes in hot gas' if swept_shell else 'All swept air instantly mixes into hot gas'
    if fed_source:
        report['model_kind']='One-way native outer Plasma driven by actual source-layer outflow'
        report['energy_denominator']='Injected source enthalpy, not laser absorption; target energy excluded'
        report['limitations']+=['No downstream pressure or laser shielding feedback to recorded source','Outer laser absorption omitted','Source chemistry truncates atCu+; hot input has missing multiple ionization']
    if swept_shell:report['limitations'].append('Thin air shell energy is tracked, but its thermochemistry and radiation are not solved')
    fig,ax=plt.subplots(2,3,figsize=(14,8),layout='constrained')
    ax[0,0].plot(ns,c('Rp')*1e6,label='Radius');ax[0,0].plot(ns,c('Hp')*1e6,label='Height');ax[0,0].set_ylabel('um')
    for n,label in [('Tg','Gas'),('TeK','Electrons'),('maxTarget(c.T)','Target peak')]:ax[0,1].plot(ns,c(n),label=label)
    ax[0,1].set_ylabel('K');ax[0,2].semilogy(ns,np.maximum(c('plas.ne'),1e-30),label='Electron density');ax[0,2].set_ylabel('1/m3')
    ax[1,0].plot(ns,Cu*1e12,label='Gas Cu');ax[1,0].plot(ns,removed*1e12,'--',label='Net removed target Cu');ax[1,0].set_ylabel('ng')
    if fed_source:ax[1,0].get_lines()[1].set_label('Injected source Cu')
    for n,label in [('absorbedEnergy*energyScale','Absorbed'),('radiatedEnergy*energyScale','Radiated'),('gasEnergy*energyScale','Gas total'),('intCu(rhoCu*c.hCond)','Target stored')]:ax[1,1].plot(ns,c(n)*1e6,label=label)
    if swept_shell:
        ax[1,1].plot(ns,c('shellStoredEnergy')*1e6,label='Swept shell internal')
        ax[1,1].plot(ns,c('Kinetic')*1e6,label='Total kinetic')
    if fed_source:ax[1,1].get_lines()[0].set_label('Injected source enthalpy')
    ax[1,1].set_ylabel('uJ');ax[1,2].plot(ns,ledger/denom*100,label='Total-energy residual');ax[1,2].set_ylabel('% of injected enthalpy' if fed_source else '% of final absorption')
    for a in ax.flat:a.grid(alpha=.2);a.legend(fontsize=8);a.set_xlabel('Model time [ns]')
    fig.suptitle(f'{case}: native global Plasma + '+('recorded source outflow' if fed_source else 'target heat')+' + expansion\nHomogeneous bulk reduction; not a spatial plasma/sheath solution')
    fig.savefig(out/'global_history.png',dpi=160);plt.close(fig)
    exp=np.load(OLD/'experiment/S4_reconstructed.npz');times=exp['times_ns'];px=float(exp['pixel_um']);h,w=exp['gray'].shape[1:]
    valid=(times+113>=ns[0]-1e-5)&(times+123<=ns[-1]+1e-5)
    report['complete_experiment_times_ns']=times[valid].tolist()
    if np.any(valid):
        needed=np.zeros(len(t),bool)
        for tt in times[valid]+118:
            lo=max(0,np.searchsorted(ns,tt-5,side='right')-1);hi=min(len(t),np.searchsorted(ns,tt+5)+1);needed[lo:hi]=True
        indices=np.flatnonzero(needed);x=(np.arange(w)+.5-w/2)*px;z=(h-np.arange(h)-.5)*px
        raw,thin=render(d,indices,x,z)
        rad=gaussian_filter(gate(ns[indices],raw,times[valid]+118),[0,1/2.354820045,1/2.354820045],mode='constant')
        photons=rad*(2*np.pi*(1-np.sqrt(1-.4**2)))*(px*1e-6)**2*10e-9*525e-9/HC
        np.savez_compressed(R/'results'/f'{case}_global_images.npz',experiment_times_ns=times[valid],radiance=rad,photons_upper_525nm=photons,x_um=x,z_um=z,instant_model_times_ns=ns[indices],instant_radiance=raw,instant_thin=thin,model_kind='one-way source, native outer Plasma' if fed_source else 'native global Plasma')
        chosen=np.arange(valid.sum()) if valid.sum()<=8 else np.unique([int(np.argmin(abs(times[valid]-a))) for a in [0,30,80,150,260,400,580,740]])
        fig,axes=plt.subplots(2,len(chosen),figsize=(max(11,2*len(chosen)),5.5),squeeze=False,layout='constrained')
        top=max(1.,float(photons.max()));norm=LogNorm(max(1e-8,top*1e-6),top)
        for col,k in enumerate(chosen):
            axes[0,col].imshow(exp['gray'][np.flatnonzero(valid)[k]],vmin=0,vmax=255,cmap='inferno');axes[0,col].set_title(f'{times[valid][k]:g} ns')
            artist=axes[1,col].imshow(np.maximum(photons[k],1e-30),norm=norm,cmap='inferno')
            axes[1,col].text(.03,.97,f'{photons[k].sum():.3g} photons',color='white',va='top',transform=axes[1,col].transAxes,fontsize=7)
        for a in axes.flat:a.set_xticks([]);a.set_yticks([])
        axes[0,0].set_ylabel('S4 experiment');axes[1,0].set_ylabel('Uniform bulk closure')
        fig.colorbar(artist,ax=axes[1].tolist(),label='Ideal photons / pixel / 10 ns; one fixed color scale',fraction=.025)
        fig.suptitle(f'{case}: complete gates, 118 ns shift, 475-525 nm, identical length scale\nUniform half-ellipsoid closure; internal shape and target separation NOT predicted',fontsize=11)
        fig.savefig(out/'S4_global_shape_closure.png',dpi=170);plt.close(fig)
        ph=photons.sum((1,2));report.update(peak_photons_per_gate=float(ph.max()),peak_experiment_time_ns=float(times[valid][ph.argmax()]),last_gate_photons=float(ph[-1]))
        peakpix=photons.max((1,2))
        report.update(peak_pixel_photons_per_gate=float(peakpix.max()),peak_pixel_experiment_time_ns=float(times[valid][peakpix.argmax()]))
        rows=[]
        for tt,im in zip(times[valid],photons):
            total=im.sum();cx=np.cumsum(im.sum(0));cz=np.cumsum(im[::-1].sum(1))
            width=np.diff(np.interp([.05,.95],cx/max(total,1e-300),x))[0]
            height=np.diff(np.interp([.05,.95],cz/max(total,1e-300),z[::-1]))[0]
            rows.append(dict(time_ns=float(tt),photons=float(total),width90_um=float(width),height90_um=float(height),centroid_z_um=float((im*z[:,None]).sum()/max(total,1e-300))))
        with (R/'results'/f'{case}_global_metrics.csv').open('w',newline='') as f:
            wr=csv.DictWriter(f,fieldnames=rows[0]);wr.writeheader();wr.writerows(rows)
    (R/'results'/f'{case}_global_summary.json').write_text(json.dumps(report,indent=2))
    print(json.dumps(report,indent=2))
if __name__=='__main__':main()
