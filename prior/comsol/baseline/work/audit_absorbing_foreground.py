"""Check opacity omitted by emissivity-relative ray masks on real H15 fields."""
import json
import numpy as np
from numba import set_num_threads
from radiation_s4 import ROOT, RES, Atomic, Rays, physical_grid, K, H, C
from line_transfer_fast import transfer_line

def full_ray(j,t,v,ds,line):
    """Independent full-cell LTE transfer, retaining dim absorbing cells."""
    nu=line['nu'];mass=line['mass'];active=j>0
    if not active.any():return 0.
    b=2*H*nu**3/C**2/np.expm1(np.minimum(H*nu/(K*t),700.))
    sig=nu/C*np.sqrt(K*t/mass);tau=(j/np.maximum(b,1e-280)/(sig*np.sqrt(2*np.pi))*ds).sum()
    if tau<.005:return float(j@ds)
    vmax=np.max(abs(v[active]));vlim=vmax+7*np.sqrt(K*t[active].max()/mass)
    dv=min(200.,np.sqrt(K*t[active].min()/mass)/3);nv=max(3,int(np.ceil(2*vlim/dv)));dv=2*vlim/nv
    fg=2*np.sqrt(2*np.log(2.))*sig;fl=2*line['A']/(4*np.pi)
    fw=(fg**5+2.69269*fg**4*fl+2.42843*fg**3*fl**2+4.47163*fg**2*fl**3+.07842*fg*fl**4+fl**5)**.2
    eta=np.clip(1.36603*fl/fw-.47719*(fl/fw)**2+.11116*(fl/fw)**3,0,1)
    vel=-vlim+(np.arange(nv)+.5)*dv;I=np.zeros(nv)
    for iy in np.flatnonzero(active):
        dn=nu/C*(vel-v[iy]);phi=(1-eta[iy])*np.sqrt(4*np.log(2.)/np.pi)/fw[iy]*np.exp(-4*np.log(2.)*(dn/fw[iy])**2)+eta[iy]*(fw[iy]/2)/np.pi/(dn*dn+(fw[iy]/2)**2)
        optical=j[iy]*phi/np.maximum(b[iy],1e-280)*ds[iy]
        I=I*np.exp(-optical)+b[iy]*(-np.expm1(-optical))
    return float(I.sum()*nu/C*dv)

meta=json.loads((RES/'H15_fields.json').read_text())
field=np.memmap(RES/'H15_fields.bin',dtype=meta['dtype'],mode='r',shape=tuple(meta['shape']))
r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z'])
times=np.array(meta['times_s']);atom=Atomic();rays=Rays(r,z);rows=[];set_num_threads(2)
for ns in [120,180,220,300,400,600,860]:
    it=int(np.argmin(abs(times-ns*1e-9)))
    phys=physical_grid(np.array(field[it],dtype=float),r,z)
    jl,_=atom.emissivities(phys);temp=np.maximum(rays.sample(phys[0]),100.)
    for li,line in enumerate(atom.lines):
        if line['element']!=0 or min(abs(line['wavelength_air_nm']-v) for v in [510.5541,515.3235,521.8202])>.001:continue
        j=rays.sample(jl[li]);peak=j.max(axis=1)[:,None]
        b=2*H*line['nu']**3/C**2/np.expm1(np.minimum(H*line['nu']/(K*temp),700.))
        sig=line['nu']/C*np.sqrt(K*temp/line['mass'])
        dtau=j/np.maximum(b,1e-280)/(sig*np.sqrt(2*np.pi))*rays.ds
        old=(dtau*(j>peak*1e-10)).sum(axis=1)
        total=dtau.sum(axis=1);miss=total-old
        vr=rays.sample(phys[5]).reshape(rays.shape)*rays.vproj[None,:,:];vr=vr.reshape(temp.shape)
        ids=np.unique(np.r_[np.argsort(miss)[-8:],np.argsort(j@rays.ds)[-8:]])
        old_i=transfer_line(j[ids],temp[ids],vr[ids],rays.ds,line['nu'],line['A'],line['mass'])[0]
        full_i=np.array([full_ray(j[p],temp[p],vr[p],rays.ds,line) for p in ids])
        active=full_i>max(full_i.max()*1e-6,1e-300)
        rel=np.max(abs(old_i[active]/full_i[active]-1)) if active.any() else 0.
        rows.append(dict(numerical_ns=float(times[it]*1e9),wavelength_nm=line['wavelength_air_nm'],max_total_tau_bound=float(total.max()),max_omitted_tau=float(miss.max()),rays_omitted_tau_above_005=int((miss>.005).sum()),rays_false_thin=int(((old<.005)&(total>=.005)).sum()),full_cell_transfer_tested_rays=len(ids),max_relative_intensity_change_above_1e6_peak=float(rel)))
out={'case':'H15','test':'All local opacity vs emissivity-relative 1e-10 mask, no absolute emission cutoff','rows':rows}
(RES/'absorbing_foreground_audit.json').write_text(json.dumps(out,indent=2))
print(json.dumps(out,indent=2))
