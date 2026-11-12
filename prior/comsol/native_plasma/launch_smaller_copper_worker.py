"""Avoid thousands of padding cells in the cold-copper native reaction group."""
from pathlib import Path
import os,sys,json,subprocess,time,numpy as np
R=Path(__file__).resolve().parent;src=R/'results/R413_operator';dst=R/'results/R417_operator';dst.mkdir(exist_ok=False)
a=np.loadtxt(src/'state.txt');np.savetxt(dst/'state.txt',a[:1024],fmt='%.16e')
cfg=json.loads((src/'worker_config.json').read_text());cfg['slots']=1024;(dst/'worker_config.json').write_text(json.dumps(cfg,indent=2))
props=dict(line.split('=',1) for line in (src/'request.properties').read_text().splitlines() if '=' in line)
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_LOG_TAG='R417')
with (R/'logs/R417_launch.log').open('w') as out,(R/'logs/R417_launch.err').open('w') as err:
    p=subprocess.Popen([sys.executable,'-X','utf8',str(R/'run.py'),'java','NativeReactionWorker',str(R/'models/R401_ready.mph'),str(dst),'32','dependentCu','reactionOnly','multicharge'],env=env,stdout=out,stderr=err)
while not (dst/'ready.txt').exists():
    if p.poll() is not None:raise RuntimeError('R417 setup failed')
    time.sleep(3)
key='smaller_copper_audit';(dst/'request.properties').write_text(f'id={key}\ndt_s={props["dt_s"]}\nsave=false\n')
while not (dst/f'complete_{key}.json').exists():
    if (dst/f'error_{key}.txt').exists():raise RuntimeError((dst/f'error_{key}.txt').read_text())
    time.sleep(3)
a=json.loads((src/f'complete_{props["id"]}.json').read_text());b=json.loads((dst/f'complete_{key}.json').read_text())
x=np.fromfile(src/f'output_{props["id"]}.bin',dtype='>f8').reshape(a['shape'])[:,:1024];y=np.fromfile(dst/f'output_{key}.bin',dtype='>f8').reshape(b['shape'])
metrics={a['expressions'][i]:float(abs(x[i]-y[i]).max()/max(float(abs(x[i]).max()),1e-100)) for i in [0,1,2,6,7]}
report=dict(reference_id=props['id'],reference_seconds=a['seconds'],new_seconds=b['seconds'],peak_scaled_field_differences=metrics,same_physical_equations=True)
(R/'results/smaller_copper_worker_audit.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
