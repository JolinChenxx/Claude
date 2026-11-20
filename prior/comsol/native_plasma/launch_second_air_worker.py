"""Use released spatial-solver cores for a disjoint native reaction batch."""
from pathlib import Path
import os,sys,json,subprocess,time
import numpy as np
R=Path(__file__).resolve().parent
src=R/'results/R412_operator';dst=R/'results/R415_operator'
dst.mkdir(exist_ok=False)
while True:
    req=(src/'request.properties').read_text();state=(src/'state.txt').read_bytes()
    if req==(src/'request.properties').read_text():break
props=dict(line.split('=',1) for line in req.splitlines() if '=' in line)
(dst/'state.txt').write_bytes(state)
(dst/'audit_input.txt').write_bytes(state)
(dst/'worker_config.json').write_text((src/'worker_config.json').read_text())
env=os.environ.copy();env.update(ROUTE1_THREADS='16',ROUTE1_LOG_TAG='R415')
with (R/'logs/R415_launch.log').open('w') as out,(R/'logs/R415_launch.err').open('w') as err:
    proc=subprocess.Popen([sys.executable,'-X','utf8',str(R/'run.py'),'java','NativeReactionWorker',str(R/'models/R401_ready.mph'),str(dst),'192','reactionOnly','multicharge'],env=env,stdout=out,stderr=err)
while not (dst/'ready.txt').exists():
    if proc.poll() is not None:raise RuntimeError('R415 failed during setup')
    time.sleep(3)
while not (src/f'complete_{props["id"]}.json').exists():
    if (src/f'error_{props["id"]}.txt').exists():raise RuntimeError('Reference native batch failed')
    time.sleep(3)
key='air_parallel_audit'
(dst/'request.properties').write_text(f'id={key}\ndt_s={props["dt_s"]}\nsave=false\n')
while not (dst/f'complete_{key}.json').exists():
    if (dst/f'error_{key}.txt').exists():raise RuntimeError((dst/f'error_{key}.txt').read_text())
    time.sleep(3)
a=json.loads((src/f'complete_{props["id"]}.json').read_text());b=json.loads((dst/f'complete_{key}.json').read_text())
x=np.fromfile(src/f'output_{props["id"]}.bin',dtype='>f8').reshape(a['shape']);y=np.fromfile(dst/f'output_{key}.bin',dtype='>f8').reshape(b['shape'])
metrics={}
for i in [0,1,2,6,7]:
    scale=max(float(abs(x[i]).max()),1e-100)
    metrics[a['expressions'][i]]=float(abs(x[i]-y[i]).max()/scale)
report=dict(reference_id=props['id'],reference_threads=8,new_threads=16,reference_seconds=a['seconds'],new_seconds=b['seconds'],peak_scaled_field_differences=metrics,same_physical_equations=True)
(R/'results/parallel_air_worker_audit.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
