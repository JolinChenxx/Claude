"""Join contiguous saved optics, then compute complete camera gates only."""
from pathlib import Path
import os,sys,time,json,subprocess
import numpy as np
R=Path(__file__).resolve().parent;first,last,out=sys.argv[1:4]
while not (R/'results'/f'{last}_stage.json').exists():time.sleep(30)
parts=[];meta=[]
for c in [first,last]:
 d=json.loads((R/'results'/f'{c}mapped_fields.json').read_text());meta.append(d)
 parts.append(np.memmap(R/'results'/f'{c}mapped_fields.bin',dtype='>f4',mode='r',shape=tuple(d['shape'])))
for key in ['r_um','z_um','expressions']:
 if meta[0][key]!=meta[1][key]:raise ValueError('Different sampling grid: '+key)
t0=np.array(meta[0]['times_s']);t1=np.array(meta[1]['times_s'])
if abs(t0[-1]-t1[0])>1e-15:raise ValueError('Noncontiguous saved times')
use=t1>t0[-1]+1e-16;d=meta[0].copy();d['times_s']=np.r_[t0,t1[use]].tolist();d['surface_height_um']=meta[0]['surface_height_um']+np.array(meta[1]['surface_height_um'])[use].tolist();d['shape'][0]=len(d['times_s']);d['constituent_models']=[first,last]
dest=R/'results'/f'{out}mapped'
with Path(str(dest)+'_fields.bin').open('wb') as f:
 parts[0].tofile(f);np.asarray(parts[1][use],dtype='>f4').tofile(f)
Path(str(dest)+'_fields.json').write_text(json.dumps(d))
for suffix in ['mesh_audit','feedback']:
 p0=R/'results'/f'{first}_{suffix}.csv';p1=R/'results'/f'{last}_{suffix}.csv'
 h0=p0.read_text().splitlines()[0].split(',');h1=p1.read_text().splitlines()[0].split(',')
 headers=list(dict.fromkeys(h0+h1))
 def aligned(path,head):
  values=np.loadtxt(path,delimiter=',',skiprows=1,ndmin=2)
  missing=set(headers)-set(head)
  if missing-{'emitted_state_energy'}:raise ValueError('Incompatible saved export columns: '+str(missing))
  return np.column_stack([values[:,head.index(n)] if n in head else np.zeros(len(values)) for n in headers])
 a=aligned(p0,h0);b=aligned(p1,h1);a=np.vstack([a,b[b[:,0]>a[:,0].max()+1e-16]])
 np.savetxt(R/'results'/f'{out}_{suffix}.csv',a,delimiter=',',header=','.join(headers),comments='',fmt='%.16e')
env=os.environ.copy();env.update(ROUTE1_CAMERA_ZERO_NS='90',ROUTE1_RADIATION_EXPOSURES_ONLY='1',ROUTE1_RADIATION_MIN_NS='80',ROUTE1_POST_THREADS='16')
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
py('plot_mapped_fields.py',out+'mapped')
# Continue optics from the already evaluated source knots. Recomputing every
# earlier exposure at each checkpoint needlessly delays the next comparison.
first_rad=R/'results'/f'{first}mapped_radiation';last_rad=R/'results'/f'{last}mapped_radiation';out_rad=R/'results'/f'{out}mapped_radiation'
if (first_rad/'radiance.npz').exists():
 py('radiation_native.py',last+'mapped')
 x=np.load(first_rad/'radiance.npz');y=np.load(last_rad/'radiance.npz');pick=y['times_s']>x['times_s'][-1]+1e-16
 for k in ['pixel_um','x_um','z_um','pixel_quadrature_orders']:
  if not np.array_equal(x[k],y[k]):raise ValueError('Different optical sampling: '+k)
 values={k:x[k] for k in x.files}
 for k in ['times_s','radiance','optically_thin','line_tau_bound']:values[k]=np.concatenate([x[k],y[k][pick]],axis=0)
 out_rad.mkdir(exist_ok=True);np.savez_compressed(out_rad/'radiance.npz',**values)
 method=json.loads((first_rad/'method.json').read_text());method['saved_source_times_s']=d['times_s'];method['radiation_source_indices']=[int(np.argmin(abs(np.array(d['times_s'])-q))) for q in values['times_s']];method['continued_optics_from']=[first,last];(out_rad/'method.json').write_text(json.dumps(method,indent=2))
 if (first_rad/'moving_optical_audit.json').exists():
  audit=json.loads((first_rad/'moving_optical_audit.json').read_text());nextaudit=json.loads((last_rad/'moving_optical_audit.json').read_text());end=audit['samples'][-1]['time_ns'];audit['samples'] += [q for q in nextaudit['samples'] if q['time_ns']>end+1e-7];(out_rad/'moving_optical_audit.json').write_text(json.dumps(audit,indent=2))
 print('REUSED SAVED OPTICS',first,'NEW KNOTS',int(pick.sum()),flush=True)
else:py('radiation_native.py',out+'mapped')
py('preview_native.py',out+'mapped');py('preview_gate_sequence.py',out+'mapped');py('prepare_gas_feedback.py',out)
(R/'results'/f'{out}_post_complete.json').write_text(json.dumps({'saved_end_ns':d['times_s'][-1]*1e9,'constituent_models':[first,last],'two_way_converged':False},indent=2))
print('JOINED PLASMA OPTICS COMPLETE',out,flush=True)
