import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from legacy_radiation import Atomic,physical_grid,planck
from transport import line_transfer,ff_images
import csv,time

def snapshot(ns,subpixels):
    f,r,z,t=fields();i=int(np.argmin(abs(t*1e9-ns)));a=physical_grid(np.array(f[i],float),r,z)
    atom=Atomic();jl,ff=atom.emissivities(a);rays=Rays(r,z,subpixels=subpixels)
    e,p,te,tp,_=ff_images(ff,a[0],rays);T=np.maximum(rays.sample(a[0]),100.)
    vr=(rays.sample(a[5]).reshape(rays.shape)*rays.vproj[None]).reshape(T.shape)
    native,_=atom.emissivities(np.array(f[i,:9],float))
    lum=4*np.pi*volume_integral(native,r,z,f[i,10]);selected=[k for k,l in enumerate(atom.lines) if 475<=l['wavelength_air_nm']<=525]
    total=sum(lum[k] for k in selected);kept=0
    for k in sorted(selected,key=lambda k:lum[k],reverse=True):
        if kept>=.9999*total:break
        kept+=lum[k];l=atom.lines[k];nu=l['nu']
        val,thin,_=line_transfer(rays.sample(jl[k]),rays.sample(jl[k]/np.maximum(planck(nu,a[0]),1e-280)),T,vr,rays.ds,nu,l['A'],l['mass'])
        p[0]+=rays.image(val)/(H*nu);tp[0]+=rays.image(thin)/(H*nu)
    factor=OMEGA*(PIXEL*1e-6)**2*10e-9
    return dict(numerical_ns=ns,subpixels=subpixels,total_photons_10ns=float(p[0].sum()*factor),peak_photons_10ns=float(p[0].max()*factor),thin_total_photons=float(tp[0].sum()*factor))

if __name__=='__main__':
    rows=[]
    for ns in (120,150):
        for s in (1,3,5,7):
            row=snapshot(ns,s);rows.append(row);print(row,flush=True)
            write_json(RES/'R2_spatial_sampling_check.json',rows)
