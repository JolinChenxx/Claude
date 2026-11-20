"""Join actual same-equation continuation exports; preserve constituent MPH files."""
from pathlib import Path
import os,sys,time,json,subprocess
import numpy as np
R=Path(__file__).resolve().parent;first,last,out=sys.argv[1:4]
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_LOG_TAG=out)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
while not (R/'results'/f'{last}_copper_summary.json').exists():time.sleep(20)
for suffix in ['surface','melt_history']:
 p1=R/'results'/f'{first}_{suffix}.csv';p2=R/'results'/f'{last}_{suffix}.csv'
 header=p1.read_text().splitlines()[0]
 if header!=p2.read_text().splitlines()[0]:raise ValueError('Different export columns')
 a=np.loadtxt(p1,delimiter=',',skiprows=1);b=np.loadtxt(p2,delimiter=',',skiprows=1)
 if abs(a[:,0].max()-b[:,0].min())>1e-14:raise ValueError('Noncontiguous saved histories')
 joined=np.vstack([a,b[b[:,0]>a[:,0].max()+1e-16]])
 np.savetxt(R/'results'/f'{out}_{suffix}.csv',joined,delimiter=',',header=header,comments='',fmt='%.16e')
(R/'results'/f'{out}_composition.json').write_text(json.dumps({'saved_models':[first,last],'note':'Joined same-equation saved exports; no standalone MPH named '+out,'interpolated_or_extrapolated_states':False},indent=2))
py('plot_partitioned_copper.py',out);py('plot_copper_ledger.py',out);py('prepare_copper_history.py',out)
py('run.py','java','MeshStateAudit',str(R/'models'/f'{last}.mph'),str(R/'results'/f'{last}_mesh_audit.csv'))
(R/'results'/f'{out}_stage.json').write_text(json.dumps({'case':out,'constituent_models':[first,last],'two_way_converged':False,'source':'joined actual saved copper fields'},indent=2))
print('COPPER CHAIN COMPLETE',out,flush=True)
