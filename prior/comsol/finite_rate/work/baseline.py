"""LTE baseline with spectral photon accounting, no fitted gain or time shift."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from legacy_radiation import Atomic,physical_grid,planck
from transport import ff_images,ff_bands,line_transfer
from numba import set_num_threads
import time,argparse,csv

def run(case='B64A',quick=False,outname=None):
    set_num_threads(8)
    f,r,z,t=fields(case);atom=Atomic();rays=Rays(r,z)
    exp=np.load(SOURCE/'experiment'/'S4_reconstructed.npz')
    centres=np.unique(np.concatenate([np.asarray(exp['times_ns'])+s for s in SHIFTS]))
    ids=set(np.searchsorted(t,np.arange(0,901,10)*1e-9).tolist())
    for tc in centres:
        ids.update(np.where((t*1e9>=tc-6.01)&(t*1e9<=tc+6.01))[0].tolist())
    ids=np.array(sorted(i for i in ids if i<len(t) and t[i]<=901e-9))
    if quick:ids=np.array([int(np.argmin(abs(t-x*1e-9))) for x in [90,120,150,170,240,350,490,670,830]])
    out=RES/(outname or f'R2_{case}_LTE');out.mkdir(exist_ok=True)
    meta=out/'progress.json';finished=0
    shape=(len(ids),3,len(rays.z),2*len(rays.x))
    reuse=meta.exists() and json.loads(meta.read_text())['ids']==ids.tolist()
    arrays={key:np.lib.format.open_memmap(out/f'{key}.npy',mode='r+' if reuse else 'w+',dtype='float64',shape=shape)
            for key in ('radiance','photon_radiance','thin_radiance','thin_photon_radiance')}
    rows=[]
    if reuse:
        state=json.loads(meta.read_text());finished=state['finished'];rows=state['rows']
    started=time.time()
    for k in range(finished,len(ids)):
        it=ids[k];native=np.array(f[it],dtype=float);phys=physical_grid(native,r,z);T=phys[0]
        jl,ff=atom.emissivities(phys);jn,ffn=atom.emissivities(native[:9]);jac=native[10]
        L=4*np.pi*volume_integral(jn,r,z,jac)
        en,pn=ff_bands(ffn,native[0]);Le=4*np.pi*volume_integral(en,r,z,jac);Lp=4*np.pi*volume_integral(pn,r,z,jac)
        e,p,te,tp,tauff=ff_images(ff,T,rays)
        selected=set();omitted=[]
        for bi,(lo,hi) in enumerate(BANDS):
            valid=[i for i,line in enumerate(atom.lines) if lo<=line['wavelength_air_nm']<=hi]
            Le[bi]+=sum(L[i] for i in valid);Lp[bi]+=sum(L[i]/(H*atom.lines[i]['nu']) for i in valid)
            ranked=sorted(valid,key=lambda i:L[i],reverse=True);total=sum(L[i] for i in valid);kept=0.
            for li in ranked:
                if kept>=.9999*total:break
                selected.add(li);kept+=L[li]
            omitted.append((total-kept)/total if total else 0.)
        tr=np.maximum(rays.sample(T),100.)
        vr=rays.sample(phys[5]).reshape(rays.shape)*rays.vproj[None,:,:];vr=vr.reshape(tr.shape)
        taumax=0.
        for li in selected:
            line=atom.lines[li];nu=line['nu']
            alpha=jl[li]/np.maximum(planck(nu,T),1e-280)
            rad,thin,tau=line_transfer(rays.sample(jl[li]),rays.sample(alpha),tr,vr,rays.ds,nu,line.get('radiative_width_s',line['A']),line['mass'])
            rad,thin=rays.image(rad),rays.image(thin);taumax=max(taumax,float(tau.max()))
            for bi,(lo,hi) in enumerate(BANDS):
                if lo<=line['wavelength_air_nm']<=hi:
                    e[bi]+=rad;p[bi]+=rad/(H*nu);te[bi]+=thin;tp[bi]+=thin/(H*nu)
        for key,a in zip(arrays,(e,p,te,tp)):arrays[key][k]=a;arrays[key].flush()
        rows.append(dict(t_ns=float(t[it]*1e9),emitted_W=Le.tolist(),emitted_photons_s=Lp.tolist(),
            max_line_tau=taumax,max_ff_tau=tauff,omitted_line_energy_fraction=max(omitted)))
        write_json(meta,dict(ids=ids.tolist(),finished=k+1,rows=rows))
        if k%5==0 or k==len(ids)-1:
            print(f'{case} {k+1}/{len(ids)} t={t[it]*1e9:.0f} ns max L={e[0].max():.4g} tau={taumax:.3g} elapsed={time.time()-started:.1f}s',flush=True)
    np.savez(out/'axes.npz',times_s=t[ids],x_um=np.r_[-rays.x[::-1],rays.x],z_um=rays.z[::-1],bands_nm=BANDS,pixel_um=rays.pixel)
    write_json(out/'summary.json',dict(case=case,model='LTE',gain_fit=False,shift_fit=False,ideal_collection=True,
        gate_ns=10,NA=.4,throughput=1.,QE=1.,times=len(ids),line_photon_weight='1/(h*nu) per line',
        continuum_photon_weight='20-point Gauss-Legendre frequency quadrature per band',
        opacity='independent emissivity/opacity interface; LTE alpha=j/B only in LTE adapter',natural_width='Resolved Cu I: total upper+lower radiative widths; incomplete other levels: transition A approximation',
        source=str(SOURCE/'results'/f'{case}_fields.bin'),pixel_um=rays.pixel,shape=shape,
        omitted_line_fraction=max(a['omitted_line_energy_fraction'] for a in rows),status='completed'))
    with (out/'luminosity.csv').open('w',newline='',encoding='utf-8') as file:
        w=csv.writer(file);w.writerow(['t_ns']+[f'energy_{int(lo)}_{int(hi)}_W' for lo,hi in BANDS]+[f'photons_{int(lo)}_{int(hi)}_s' for lo,hi in BANDS])
        w.writerows([[a['t_ns'],*a['emitted_W'],*a['emitted_photons_s']] for a in rows])
    return out

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--case',default='B64A');parser.add_argument('--quick',action='store_true');parser.add_argument('--out')
    args=parser.parse_args();run(args.case,args.quick,args.out)
