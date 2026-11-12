"""Compare accelerated transfer with the original calculation on saved rays."""
from pathlib import Path
import json,time,sys,numpy as np
from scipy.interpolate import RegularGridInterpolator
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from radiation_native import line_transfer,line_transfer_reference,set_num_threads,bilinear_sample
case='F36';d=json.loads((R/'results'/f'{case}_fields.json').read_text())
a=np.memmap(R/'results'/f'{case}_fields.bin',dtype='>f4',mode='r',shape=tuple(d['shape']))
set_num_threads(8);edges=np.unique(np.r_[-1000,np.arange(-1000,-100,10),np.arange(-100,-30,3),np.arange(-30,30.01,.5),np.arange(33,100,3),np.arange(100,1000,10),1000]);y=(edges[:-1]+edges[1:])/2;ds=np.diff(edges)*1e-6
x,z=np.meshgrid([.1,4.17,12.5,20.83,29.17,37.5],[.001,.01,.2,1,3,8,16,40,80]);x=x.ravel();z=z.ravel();radius=np.sqrt(x[:,None]**2+y[None,:]**2);coords=np.stack([np.broadcast_to(z[:,None],radius.shape),radius],axis=-1)
reports=[]
rr=np.r_[0,d['r_um']];zz=np.r_[0,d['z_um']];sr=radius.ravel();sz=coords[...,0].ravel()
ri=np.clip(np.searchsorted(rr,sr,side='right')-1,0,len(rr)-2).astype(np.int32);zi=np.clip(np.searchsorted(zz,sz,side='right')-1,0,len(zz)-2).astype(np.int32)
rw=(sr-rr[ri])/(rr[ri+1]-rr[ri]);zw=(sz-zz[zi])/(zz[zi+1]-zz[zi]);valid=(sr<=rr[-1])&(sz<=zz[-1])
for ti in [0,len(a)-1]:
 def sample(name):
  field=np.nan_to_num(np.array(a[ti,d['expressions'].index(name)],dtype=float));field=np.pad(field,((1,0),(1,0)),mode='edge')
  original=RegularGridInterpolator((zz,rr),field,bounds_error=False,fill_value=0)(coords)
  cached=bilinear_sample(field,zi,ri,zw,rw,valid).reshape(original.shape)
  max_error=float(np.max(np.abs(original-cached))/max(float(np.max(abs(original))),1e-30))
  assert max_error<2e-14,(name,max_error)
  return cached
 T=np.maximum(sample('Tg'),150);v=sample('urP')*y[None,:]/radius
 for li in range(3,7):
  args=(np.maximum(sample('jline'+str(li)),0),np.maximum(sample('tauLine'+str(li)+'/w0'),0),T,v,ds,500e-9)
  fast,_,_=line_transfer(*args);old,_,_=line_transfer_reference(*args)
  difference=np.abs(fast-old);mask=old>old.max()*1e-8
  report={'time_ns':d['times_s'][ti]*1e9,'line':li,'max_error_relative_to_peak':float(difference.max()/max(old.max(),1e-30)),'max_relative_error_in_visible_rays':float(np.max(difference[mask]/old[mask])) if mask.any() else 0.}
  assert report['max_error_relative_to_peak']<1e-8 and report['max_relative_error_in_visible_rays']<1e-5
  reports.append(report)
(R/'results/line_transfer_acceleration_check.json').write_text(json.dumps(reports,indent=2));print(json.dumps(reports,indent=2))
