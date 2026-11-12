"""Join saved radiation stages of the SAME physical case without gap filling."""
from pathlib import Path
import sys,json
import numpy as np
R=Path(__file__).resolve().parent
outcase,*cases=sys.argv[1:]
if not cases:raise ValueError('Usage: merge_radiation.py outputcase stage1 stage2 ...')
loaded=[np.load(R/'results'/f'{case}_radiation/radiance.npz') for case in cases]
methods=[json.loads((R/'results'/f'{case}_radiation/method.json').read_text()) for case in cases]
for d in loaded[1:]:
 for k in ['pixel_um','x_um','z_um']:
  if not np.array_equal(loaded[0][k],d[k]):raise ValueError('Incompatible physical image grid')
t=np.concatenate([d['times_s'] for d in loaded]);order=np.argsort(t,kind='stable')
order=order[np.r_[True,np.diff(t[order])>1e-16]]
values={k:np.concatenate([d[k] for d in loaded],axis=0)[order] for k in ['times_s','radiance','optically_thin','line_tau_bound']}
values.update({k:loaded[0][k] for k in ['pixel_um','x_um','z_um','pixel_quadrature_orders']})
out=R/'results'/f'{outcase}_radiation';out.mkdir(exist_ok=True)
np.savez_compressed(out/'radiance.npz',**values)
method=methods[0].copy();method.update(stages=cases,merge='Sorted saved samples; duplicate endpoints retain first stage; no interpolation or extrapolation')
method['saved_source_times_s']=np.unique(np.concatenate([np.array(m['saved_source_times_s']) for m in methods])).tolist()
method.pop('radiation_source_indices',None)
(out/'method.json').write_text(json.dumps(method,indent=2),encoding='utf8')
print(out,len(order),'samples',values['times_s'][[0,-1]]*1e9)
