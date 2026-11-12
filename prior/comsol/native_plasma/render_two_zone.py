"""Synthetic images from saved two-zone populations with layered LOS transfer.

The spherical zones are assumed geometry, not a spatially resolved bright core.
No fitting of radii, positions, reaction rates or individual frame brightness.
"""
from pathlib import Path
import csv,json,sys
import numpy as np
from scipy.special import erf
from scipy.ndimage import gaussian_filter
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
sys.path.insert(0,str(OLD/'work'))
from camera_s4 import gate
K=1.380649e-23;HC=6.62607015e-34*299792458.;EV=1.602176634e-19;MCU=.063546/6.02214076e23
def load(case):
    with (R/'results'/f'{case}_two_zone.csv').open() as f:
        rd=csv.DictReader(f);names=rd.fieldnames;rows=list(rd)
    return {n:np.array([float(x[n]) for x in rows]) for n in names}
def spectrum(d,p,k,lam):
    te=d[p+'TeK'][k]*K/EV;e=HC/(lam*EV)
    j=(d[p+'Qff'][k]*np.exp(-e/te)+d[p+'Qfb_CuH3'][k]*np.exp(-(e-1.5352049)/te)+d[p+'Qfb_CuH5'][k]*np.exp(-(e-1.5343548)/te)+d[p+'tailQfb'][k]*np.exp(-(e-.8587344)/te))/(4*np.pi*te)
    alpha=sum(d[p+n][k]*6.3e-22*np.sqrt(13.605693/threshold)*(threshold/e)**3 for n,threshold in [('nCuH3',1.5352049),('nCuH5',1.5343548),('nCuT5',.8587344)])
    nu=299792458./lam
    alpha+=d[p+'Qff'][k]/(4*np.pi)*299792458.**2/(2*K*d[p+'TeK'][k]*nu**3)*(-np.expm1(-e/te))
    return max(j,0),max(alpha,0)
def render(d,indices,x,z,order=100):
    px=x[1]-x[0];images=[];nodes,weights=np.polynomial.legendre.leggauss(order)
    for k in indices:
        im=np.zeros((len(z),len(x)));ro=d['outer.Rp'][k];ri=d['c.Rp'][k]
        b0=np.sqrt(2*K*d['c.Tg'][k]/MCU);b1=np.sqrt(2*K*d['outer.Tg'][k]/MCU);grad=d['outer.vRadius'][k]/ro
        for dx in [-.25,.25]:
          for dz in [-.25,.25]:
            X,Z=np.meshgrid((x+dx*px)*1e-6,(z+dz*px)*1e-6)
            use=(X*X+Z*Z<ro*ro)&(Z>=0);rr=(X*X+Z*Z)[use]
            if not use.any():continue
            yo=np.sqrt(np.maximum(ro*ro-rr,0));yi=np.sqrt(np.maximum(ri*ri-rr,0));length0=2*yi;length1=yo-yi
            limit=abs(grad)*yo+7*max(b0,b1);vel=limit[:,None]*nodes
            def profile(b,lo,hi,g):
                if abs(g)*max(float(np.max(abs(lo))),float(np.max(abs(hi))))<1e-5*b:
                    return (hi-lo)[:,None]*np.exp(-(vel/b)**2)
                return b*np.sqrt(np.pi)/(2*g)*(erf((vel-g*lo[:,None])/b)-erf((vel-g*hi[:,None])/b))
            shapes=[profile(b1,-yo,-yi,grad),profile(b0,-yi,yi,0),profile(b1,yi,yo,grad)]
            band=np.zeros(len(yo))
            for li in range(3,7):
                intensity=np.zeros_like(vel)
                for p,b,shape in [('outer.',b1,shapes[0]),('c.',b0,shapes[1]),('outer.',b1,shapes[2])]:
                    j=max(float(d[p+f'jline{li}'][k]),0);alpha=max(float(d[p+f'tauLine{li}/'+p+'Req'][k]),0)
                    tau=alpha*shape
                    emission=j/(np.sqrt(np.pi)*b)*shape
                    factor=np.ones_like(tau);np.divide(-np.expm1(-tau),tau,out=factor,where=tau>1e-8)
                    intensity=intensity*np.exp(-tau)+emission*factor
                band+=limit*np.sum(intensity*weights,axis=1)
            for lam in (475+(np.arange(32)+.5)*50/32)*1e-9:
                intensity=np.zeros(len(yo));de=HC/(EV*lam*lam)*50e-9/32
                for p,length in [('outer.',length1),('c.',length0),('outer.',length1)]:
                    j,alpha=spectrum(d,p,k,lam);tau=alpha*length;factor=np.ones_like(tau)
                    np.divide(-np.expm1(-tau),tau,out=factor,where=tau>1e-8)
                    intensity=intensity*np.exp(-tau)+j*length*factor
                band+=intensity*de
            im[use]+=band/4
        images.append(im)
    return np.array(images)
def main(cases):
    case='-'.join(cases);d=load(cases[0])
    for seg in cases[1:]:
        q=load(seg);keep=d['time_s']<q['time_s'][0]-1e-15;d={n:np.r_[v[keep],q[n]] for n,v in d.items()}
    ns=d['time_s']*1e9;exp=np.load(OLD/'experiment/S4_reconstructed.npz');times=exp['times_ns'];px=float(exp['pixel_um']);h,w=exp['gray'].shape[1:]
    prescribed='c.thermalSourceMode' in d
    valid=(times+113>=ns[0]-1e-5)&(times+123<=ns[-1]+1e-5);times=times[valid]
    if not len(times):print('NO COMPLETE EXPOSURE');return
    needed=np.zeros(len(ns),bool)
    for tt in times+118:
        lo=max(0,np.searchsorted(ns,tt-5,side='right')-1);hi=min(len(ns),np.searchsorted(ns,tt+5)+1);needed[lo:hi]=True
    indices=np.flatnonzero(needed);x=(np.arange(w)+.5-w/2)*px;z=(h-np.arange(h)-.5)*px
    raw=render(d,indices,x,z);rad=gaussian_filter(gate(ns[indices],raw,times+118),[0,1/2.354820045,1/2.354820045],mode='constant')
    photons=rad*(2*np.pi*(1-np.sqrt(1-.4**2)))*(px*1e-6)**2*10e-9*525e-9/HC
    # Compatible camera calibration format, with explicit model-type metadata.
    np.savez_compressed(R/'results'/f'{case}_global_images.npz',experiment_times_ns=times,radiance=rad,photons_upper_525nm=photons,x_um=x,z_um=z,instant_model_times_ns=ns[indices],instant_radiance=raw,model_kind='two-zone native Plasma; assumed concentric hemispheres'+('; one-way thermal predictor' if prescribed else ''))
    out=R/'figures'/case;out.mkdir(exist_ok=True)
    pick=np.arange(len(times)) if len(times)<=8 else np.unique([np.argmin(abs(times-v)) for v in [0,20,60,150,260,400,580,740]])
    fig,axes=plt.subplots(2,len(pick),figsize=(max(10,2.2*len(pick)),6),layout='constrained',squeeze=False)
    vmax=max(float(rad[times<=80].max()),1e-30);extent=[-w*px/2,w*px/2,0,h*px]
    for col,k in enumerate(pick):
        axes[0,col].imshow(exp['gray'][np.flatnonzero(valid)[k]],vmin=0,vmax=255,cmap='turbo',extent=extent)
        artist=axes[1,col].imshow(rad[k],vmin=0,vmax=vmax,cmap='turbo',extent=extent)
        axes[0,col].set_title(f'{times[k]:g} ns');axes[1,col].axhline(0,c='white',lw=1)
        axes[1,col].text(.02,.95,f'{photons[k].sum():.2g} ideal photons',color='white',fontsize=7,va='top',transform=axes[1,col].transAxes)
    for a in axes.flat:a.set_xticks([]);a.set_yticks([])
    axes[0,0].set_ylabel('S4 experiment');axes[1,0].set_ylabel('Two-zone model')
    fig.colorbar(artist,ax=axes[1].tolist(),label='Radiance [W m^-2 sr^-1]; fixed linear scale',fraction=.04)
    fig.suptitle(f'{case}: native two-zone chemistry'+(', prescribed thermal source' if prescribed else '')+'\nSame length scale, assumed hemispheres, 118 ns shift, 10 ns exposure; elevated core not resolved',fontsize=10)
    fig.savefig(out/'S4_two_zone_physical.png',dpi=160);plt.close(fig)
    report=dict(case=case,complete_times_ns=times.tolist(),physical_radiance=True,geometry_assumed=True,photon_total=photons.sum((1,2)).tolist(),photon_peak_pixel=photons.max((1,2)).tolist(),peak_total_time_ns=float(times[photons.sum((1,2)).argmax()]),peak_pixel_time_ns=float(times[photons.max((1,2)).argmax()]),negative_radiation_population_policy='Nonpositive trace emission and opacity set to zero in optical rendering only; population minima separately audited',limitations=['Uniform concentric hemispheres, no spatially resolved lifted core','Postprocessed layered radiation is not fed back between zones','High-temperature copper closure not independently validated'])
    (R/'results'/f'{case}_two_zone_optics.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
if __name__=='__main__':main(sys.argv[1:])
