"""Surface-aware bulk radiation and explicit omitted-layer emission audit."""
from pathlib import Path
import os,sys,subprocess,time,json
R=Path(__file__).resolve().parent;case=sys.argv[1];mapped=case+'mapped'
while True:
    p=R/'logs'/f'{case}_solver.log';s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    if '总时间:' in s or 'Total time:' in s:break
    time.sleep(30)
import numpy as np
audit=R/'results'/f'{case}_mesh_audit.csv'
while not audit.exists():time.sleep(10)
mesh=np.genfromtxt(audit,delimiter=',',names=True)
bad=mesh['cminAllspatialrelVol']<=0
if np.any(bad):
    first_bad=float(mesh['time_s'][np.flatnonzero(bad)[0]])
    last_valid=float(mesh['time_s'][mesh['time_s']<first_bad].max())
else:last_valid=float(mesh['time_s'][-1])
if last_valid<113e-9:sys.exit('No S4 exposure in this saved moving segment')
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_LOG_TAG=mapped,ROUTE1_POST_THREADS='8',ROUTE1_RADIATION_MIN_NS='113',ROUTE1_RADIATION_EXPOSURES_ONLY='1',ROUTE1_RADIATION_MAX_NS=str(last_valid*1e9))
subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java','ExportMovingOptics',str(R/'models'/f'{case}.mph'),str(R/'results'/mapped),'112'],env=env,check=True)
t=json.loads((R/'results'/f'{mapped}_fields.json').read_text())['times_s']
if t[-1]<113e-9:sys.exit('No exposure yet')
subprocess.run([sys.executable,'-X','utf8',str(R/'radiation_native.py'),mapped],env=env,check=True)
if t[0]<=113e-9+1e-16 and last_valid>=123e-9-1e-16:
    subprocess.run([sys.executable,'-X','utf8',str(R/'preview_native.py'),mapped],env=env,check=True)
print('MOVING_BULK_OPTICS_SAVED',mapped,flush=True)
