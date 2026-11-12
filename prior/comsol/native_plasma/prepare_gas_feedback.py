from pathlib import Path
import numpy as np,json,sys
R=Path(__file__).resolve().parent;case=sys.argv[1]
d=np.genfromtxt(R/'results'/f'{case}_feedback.csv',delimiter=',',names=True)
ts=np.unique(d['time_s']);fields=['transmission','p_Pa','Qexchange','return_mass','return_energy','electron_heat','gas_ur','gas_uz']
if 'emitted_state_energy' in d.dtype.names:fields.append('emitted_state_energy')
if not all(np.isfinite(d[n]).all() for n in fields):raise ValueError('Invalid feedback values: trim actual source solution first')
rr=np.unique(np.r_[np.arange(0,60.001,.25),np.arange(62,302,2),np.arange(310,1001,10)])
rows=[]
for t in ts:
 a=d[d['time_s']==t];r,ix=np.unique(a['r_um'],return_index=True);a=a[ix]
 rows.append(np.column_stack([rr,np.full(len(rr),t*1e9),*[np.interp(rr,r,a[f]) for f in fields]]))
p=R/'data'/f'{case}_gas_feedback.txt';np.savetxt(p,np.vstack(rows),fmt='%.14e')
(R/'results'/f'{case}_feedback_transfer.json').write_text(json.dumps(dict(source_case=case,end_ns=float(ts[-1]*1e9),fields=fields,source='actual saved spatial native Plasma wall fields',two_way_converged=False,table=str(p)),indent=2))
print('GAS FEEDBACK TABLE',p,flush=True)
