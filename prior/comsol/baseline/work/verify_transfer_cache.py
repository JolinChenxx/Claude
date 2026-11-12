"""Compare cached transport to original on real, optically thick H15 rays."""
from pathlib import Path
import json,time
import numpy as np
from numba import set_num_threads
from radiation_s4 import Atomic,Rays,physical_grid,transfer_line_reference as reference
from line_transfer_fast import transfer_line as cached
ROOT=Path(__file__).resolve().parents[1]

def main():
 set_num_threads(2)
 m=json.loads((ROOT/'results/H15_fields.json').read_text());f=np.memmap(ROOT/'results/H15_fields.bin',dtype=m['dtype'],mode='r',shape=tuple(m['shape']))
 r=np.array(m['coordinates_um']['r']);z=np.array(m['coordinates_um']['z']);rays=Rays(r,z);atom=Atomic();rows=[]
 for it in [60,70,80,100,150]:
  q=physical_grid(np.array(f[it],dtype=float),r,z);jl,_=atom.emissivities(q)
  tr=np.maximum(rays.sample(q[0]),100);vr=(rays.sample(q[5]).reshape(rays.shape)*rays.vproj[None,:,:]).reshape(tr.shape)
  for wl in [510.5541,515.3235,521.8202]:
   li=min(range(len(atom.lines)),key=lambda i:abs(atom.lines[i]['wavelength_air_nm']-wl));line=atom.lines[li];j=rays.sample(jl[li])
   ids=np.unique(np.r_[np.argsort((j*rays.ds).sum(1))[-16:],np.arange(0,len(j),107)])
   args=(j[ids],tr[ids],vr[ids],rays.ds,line['nu'],line['A'],line['mass'])
   start=time.perf_counter();a=reference(*args);tref=time.perf_counter()-start
   start=time.perf_counter();b=cached(*args);tnew=time.perf_counter()-start
   denom=np.maximum(abs(a[0]),1e-100);rel=float(np.max(abs(a[0]-b[0])/denom))
   assert rel<1e-10,(it,wl,rel)
   assert np.array_equal(a[1],b[1]) and np.array_equal(a[2],b[2])
   rows.append(dict(saved_index=it,line_nm=wl,rays=len(ids),max_relative_radiance_difference=rel,reference_s=tref,cached_s=tnew,max_optical_depth=float(a[1].max())))
   print(rows[-1],flush=True)
 (ROOT/'results/transport_cache_audit.json').write_text(json.dumps({'identical_sampling_and_opacity_thresholds':True,'passed':True,'rows':rows},indent=2))
if __name__=='__main__':main()
