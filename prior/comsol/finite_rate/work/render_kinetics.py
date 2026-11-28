"""Resolved Cu lines + Milne free-bound + free-free, with shared transport."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from transport import line_transfer,line_coefficients,ff_images,ff_bands,continuum_transfer
from legacy_radiation import Atomic,planck
from rate_tables import ENERGIES,G,CHI,GION,RY
from numba import set_num_threads
import time,csv,argparse

def physical_all(native,r,z):
    result=np.empty_like(native)
    for ir in range(len(r)):
        zp=native[9,:,ir]*1e6
        for e in range(native.shape[0]):result[e,:,ir]=np.interp(z,zp,native[e,:,ir])
    return result

def free_bound_spectral(pop,ne,Te,nu):
    j=np.zeros_like(Te);alpha=np.zeros_like(Te)
    factor=pop[7]*ne*(H*H/(2*np.pi*ME*K*Te))**1.5
    for i in range(7):
        bind=CHI-ENERGIES[i]
        if H*nu<bind*EV:continue
        sigma=6.3e-22*np.sqrt(RY/bind)*(bind*EV/(H*nu))**3
        ji=factor*G[i]/(2*GION)*sigma*2*H*nu**3/C**2*np.exp(-(H*nu-bind*EV)/(K*Te))
        j+=ji;alpha+=pop[i]*sigma-ji*C*C/(2*H*nu**3)
    return j,alpha

def fb_bands(pop,ne,Te):
    x,w=np.polynomial.legendre.leggauss(20);energy=[];photons=[];opacity=[]
    for lo,hi in BANDS:
        a,b=C/(hi*1e-9),C/(lo*1e-9);je=np.zeros_like(Te);jp=np.zeros_like(Te);ab=np.zeros_like(Te)
        # Split quadrature at photoionization edges so discontinuities are resolved.
        edges=sorted(set([a,b]+[(CHI-E)*EV/H for E in ENERGIES if a<(CHI-E)*EV/H<b]))
        for left,right in zip(edges[:-1],edges[1:]):
            for xx,ww in zip(x,w):
                nu=(left+right)/2+xx*(right-left)/2;j,alpha=free_bound_spectral(pop,ne,Te,nu)
                je+=j*ww*(right-left)/2;jp+=j/(H*nu)*ww*(right-left)/2;ab=np.maximum(ab,abs(alpha))
        energy.append(je);photons.append(jp);opacity.append(ab)
    return np.array(energy),np.array(photons),np.array(opacity)

def fb_images(pop,ne,Te,rays):
    e,p,ab=fb_bands(pop,ne,Te);te=np.array([rays.project(a) for a in e]);tp=np.array([rays.project(a) for a in p])
    bound=max(float(rays.project(a).max()) for a in ab)
    if bound<.005:return te.copy(),tp.copy(),te,tp,bound
    outE=np.zeros_like(te);outP=np.zeros_like(tp);x,w=np.polynomial.legendre.leggauss(20)
    for bi,(lo,hi) in enumerate(BANDS):
        a,b=C/(hi*1e-9),C/(lo*1e-9);edges=sorted(set([a,b]+[(CHI-E)*EV/H for E in ENERGIES if a<(CHI-E)*EV/H<b]))
        for left,right in zip(edges[:-1],edges[1:]):
            for xx,ww in zip(x,w):
                nu=(left+right)/2+xx*(right-left)/2;j,alpha=free_bound_spectral(pop,ne,Te,nu)
                val=rays.image(continuum_transfer(rays.sample(j),rays.sample(alpha),rays.ds))*ww*(right-left)/2
                outE[bi]+=val;outP[bi]+=val/(H*nu)
    return outE,outP,te,tp,bound

def run(case,stride=1):
    set_num_threads(8);net=json.loads((ROOT/'data/network.json').read_text());atom=Atomic()
    widths=np.zeros(8)
    for transition in net['lines']:widths[transition['up']]+=transition['A']
    meta=json.loads((RES/f'{case}_fields.json').read_text());r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z']);t=np.array(meta['times_s'])
    f=np.memmap(RES/f'{case}_fields.bin',mode='r',dtype=meta['dtype'],shape=tuple(meta['shape']))
    ids=np.unique(np.r_[np.arange(0,len(t),stride),len(t)-1]);rays=Rays(r,z);out=RES/f'{case}_radiation';out.mkdir(exist_ok=True)
    shape=(len(ids),3,len(rays.z),2*len(rays.x));progress=out/'progress.json';rows=[];finished=0
    reuse=progress.exists() and json.loads(progress.read_text())['ids']==ids.tolist() and json.loads(progress.read_text()).get('profile_version')=='total_radiative_lifetime_v1'
    arrays={name:np.lib.format.open_memmap(out/f'{name}.npy',mode='r+' if reuse else 'w+',dtype='float64',shape=shape)
        for name in ('radiance','photon_radiance','thin_radiance','thin_photon_radiance','line_photon_radiance','fb_photon_radiance','ff_photon_radiance')}
    if reuse:state=json.loads(progress.read_text());finished=state['finished'];rows=state['rows']
    start=time.time()
    for k in range(finished,len(ids)):
        it=ids[k];native=np.array(f[it],float);phys=physical_all(native,r,z);T=phys[0];ne=phys[1];Te=np.maximum(phys[19],1.);pop=np.maximum(phys[11:19],0.)
        qff=1.426e-40*np.sqrt(Te)*ne*phys[8]/(4*np.pi)
        e,p,te,tp,tauff=ff_images(qff,Te,rays);ffp=p.copy()
        fb_e,fb_p,fb_te,fb_tp,taufb=fb_images(pop,ne,Te,rays)
        e+=fb_e;p+=fb_p;te+=fb_te;tp+=fb_tp;linep=np.zeros_like(p)
        tr=np.maximum(rays.sample(T),100.);vr=rays.sample(phys[5]).reshape(rays.shape)*rays.vproj[None,:,:];vr=vr.reshape(tr.shape)
        jac=native[10];pn=np.maximum(native[11:19],0.);nt=np.maximum(native[19],1.);nn=native[1]
        qe=1.426e-40*np.sqrt(nt)*nn*native[8]/(4*np.pi)
        en,ph=ff_bands(qe,nt);fben,fbph,_=fb_bands(pn,nn,nt)
        Le=4*np.pi*volume_integral(en+fben,r,z,jac);Lp=4*np.pi*volume_integral(ph+fbph,r,z,jac)
        tax=0.;inversion=0;omitted_air_photon_bound=0.
        for line in net['lines']:
            if not 450<=line['wavelength_nm']<=550:continue
            lo,up=line['lo'],line['up'];nu=line['nu'];A=line['A']
            j,alpha=line_coefficients(pop[lo],pop[up],G[lo],G[up],A,nu)
            rad,thin,tau=line_transfer(rays.sample(j),rays.sample(alpha),tr,vr,rays.ds,nu,widths[lo]+widths[up],MCU)
            tax=max(tax,float(tau.max()));inversion+=int((alpha<0).sum());rad,thin=rays.image(rad),rays.image(thin)
            nativeL=float(volume_integral(H*nu*A*pn[up],r,z,jac))
            for bi,(a,b) in enumerate(BANDS):
                if a<=line['wavelength_nm']<=b:
                    e[bi]+=rad;p[bi]+=rad/(H*nu);te[bi]+=thin;tp[bi]+=thin/(H*nu);linep[bi]+=rad/(H*nu)
                    Le[bi]+=nativeL;Lp[bi]+=nativeL/(H*nu)
        # N/O are a disclosed LTE background at gas temperature, not Cu kinetics.
        af=phys[:9].copy();af[1]=phys[34];jl,_=atom.emissivities(af)
        an=native[:9].copy();an[1]=native[34];jln,_=atom.emissivities(an)
        for j,jn,line in zip(jl,jln,atom.lines):
            if line['element']==0:continue
            if float(j.max())==0:continue
            nu=line['nu'];nativeL=4*np.pi*float(volume_integral(jn,r,z,jac))
            # Below 1e-6 collected photons per line/gate, use an explicit thin
            # upper bound. Air is LTE so no population inversion can amplify it.
            # A maximum-emissivity times bounding-box volume upper bound avoids
            # hundreds of LOS projections of extinguished air lines. The bound
            # applies to the whole image and is accumulated explicitly below.
            air_bound=float(j.max())*(1300e-6)**2*1000e-6*OMEGA*1e-8/(H*nu)
            if air_bound<1e-8:
                omitted_air_photon_bound+=air_bound
                rad=thin=np.zeros_like(e[0])
            else:
                thin=rays.project(j)
                if thin.sum()*OMEGA*(PIXEL*1e-6)**2*1e-8/(H*nu)<1e-6:rad=thin
                else:
                    alpha=j/np.maximum(planck(nu,T),1e-280)
                    rad,thin,tau=line_transfer(rays.sample(j),rays.sample(alpha),tr,vr,rays.ds,nu,line['A'],line['mass'])
                    rad,thin=rays.image(rad),rays.image(thin)
            for bi,(a,b) in enumerate(BANDS):
                if a<=line['wavelength_air_nm']<=b:
                    e[bi]+=rad;p[bi]+=rad/(H*nu);te[bi]+=thin;tp[bi]+=thin/(H*nu);linep[bi]+=rad/(H*nu)
                    Le[bi]+=nativeL;Lp[bi]+=nativeL/(H*nu)
        for key,value in zip(arrays,(e,p,te,tp,linep,fb_p,ffp)):arrays[key][k]=value;arrays[key].flush()
        # The feedback criterion is evaluated in the emitting region, not ambient air.
        active=np.sum(pop[1:7],axis=0)>np.sum(pop[1:7],axis=0).max()*1e-4
        weight=np.sum(pop[1:7],axis=0)
        delta_num=volume_integral(abs(phys[20])*active,r,z);denom=volume_integral(abs(phys[32])*active,r,z)
        p_ratio=volume_integral(abs(phys[21])*weight,r,z)/max(volume_integral(abs(phys[7]-phys[21])*weight,r,z),1e-300)
        rows.append(dict(t_ns=float(t[it]*1e9),emitted_W=Le.tolist(),emitted_photons_s=Lp.tolist(),max_line_tau=tax,max_fb_tau=taufb,max_ff_tau=tauff,
            inversion_cells=inversion,omitted_air_photons_per_gate_upper_bound=omitted_air_photon_bound,emitting_region_energy_change=float(delta_num/max(denom,1e-300)),emission_weighted_pressure_change=float(p_ratio),
            peak_Te_K=float(Te[active].max()) if active.any() else None))
        write_json(progress,dict(ids=ids.tolist(),finished=k+1,rows=rows,profile_version='total_radiative_lifetime_v1'))
        if k%10==0 or k==len(ids)-1:print(f'RENDER {case} {k+1}/{len(ids)} t={t[it]*1e9:.0f} ns peakL={e[0].max():.4g} elapsed={time.time()-start:.1f}s',flush=True)
    np.savez(out/'axes.npz',times_s=t[ids],x_um=np.r_[-rays.x[::-1],rays.x],z_um=rays.z[::-1],bands_nm=BANDS,pixel_um=PIXEL)
    with (out/'luminosity.csv').open('w',newline='',encoding='utf-8') as file:
        w=csv.writer(file);w.writerow(['t_ns']+[f'energy_{int(lo)}_{int(hi)}_W' for lo,hi in BANDS]+[f'photons_{int(lo)}_{int(hi)}_s' for lo,hi in BANDS]);w.writerows([[a['t_ns'],*a['emitted_W'],*a['emitted_photons_s']] for a in rows])
    write_json(out/'summary.json',dict(case=case,status='completed',gain_fit=False,rows=rows,model_parameters=meta.get('model_parameters',{}),
        spectrum='resolved Cu lines; Milne seven-state free-bound; free-free; LTE N/O line background',natural_width='Cu: total upper+lower radiative widths including UV branches',
        limitations=['Cu low-temperature rates and RR cross sections contain declared approximations.','Unresolved Cu levels outside the four main-band lines are omitted.','Initial LTE reservoir inherited at numerical 120 ns.'],
        feedback_required=any(a['emitting_region_energy_change']>.05 or a['emission_weighted_pressure_change']>.1 for a in rows)))
    return out

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('case');p.add_argument('--stride',type=int,default=1);a=p.parse_args();run(a.case,a.stride)
