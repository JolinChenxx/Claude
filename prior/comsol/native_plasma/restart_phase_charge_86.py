"""Restart before local charge loss, preserving the accepted cold trajectory."""
from pathlib import Path
import sys,os,json,subprocess,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from split_history import read_history
source='HC425prefix86';out=R/'results'/source;out.mkdir(exist_ok=False)
h=[{k:v for k,v in q.items() if k!='source'} for q in read_history('HC425') if q['saved_time_ns']<=86.000001]
assert abs(h[-1]['saved_time_ns']-86)<1e-6
d=np.load(h[-1]['frame']);S=d['S'];net=S[:,:,4:8].sum(2)-S[:,:,8:10].sum(2)+S[:,:,17]+(S[:,:,20:]*np.array([2,3,4])).sum(2)-S[:,:,18]
inventory=S[:,:,4:10].sum(2)+S[:,:,17]+(S[:,:,20:]*np.array([2,3,4])).sum(2)+S[:,:,18]
error=float(np.max(abs(net)/np.maximum(inventory,1.)))
if error>1e-10:raise ValueError('Selected restart already has local charge error')
(out/'history.json').write_text(json.dumps(h,indent=2));(out/'status.json').write_text(json.dumps(h[-1],indent=2))
for name in ['grid.npz','configuration.json']:(out/name).write_bytes((R/'results/HC425'/name).read_bytes())
(out/'restart_provenance.json').write_text(json.dumps(dict(source_chain='HC423 ->HC424 ->HC425, actual saved frames from initial state',selected_ns=86.,max_local_charge_error_fraction=error,reason='Restart precedes attached-electron condensation charge loss; no S4 timing or input change'),indent=2))
env=os.environ.copy();env['ROUTE1_PHASE_CHARGE_PARTNERS']='1'
subprocess.run([sys.executable,'-X','utf8',str(R/'start_split_continuation.py'),'HC428',source,'180','Cu_HEM_monotone_20261110','--parallel-hot'],env=env,check=True)
session=json.loads((R/'day_session_20261110.json').read_text());session['split_material_cases']=['HC416','HC425','HC428'];(R/'day_session_20261110.json').write_text(json.dumps(session,indent=2))
