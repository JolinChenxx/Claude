"""Validate a smaller independent collision worker against a live input snapshot."""
from pathlib import Path
import os,sys,json,subprocess,time,numpy as np
R=Path(__file__).resolve().parent;name,source,count,threads=sys.argv[1:];count=int(count)
src=R/'results'/f'{source}_operator';dst=R/'results'/f'{name}_operator';dst.mkdir(exist_ok=False)
while True:
    req=(src/'request.properties').read_text();raw=(src/'state.txt').read_bytes()
    if req==(src/'request.properties').read_text() and (src/'state.txt').stat().st_mtime_ns<=(src/'request.properties').stat().st_mtime_ns:break
props=dict(line.split('=',1) for line in req.splitlines() if '=' in line)
(dst/'reference_input.txt').write_bytes(raw)
a=np.loadtxt(dst/'reference_input.txt');np.savetxt(dst/'state.txt',a[:count],fmt='%.16e')
cfg=json.loads((src/'worker_config.json').read_text());cfg['slots']=count;(dst/'worker_config.json').write_text(json.dumps(cfg,indent=2))
env=os.environ.copy();env.update(ROUTE1_THREADS=threads,ROUTE1_LOG_TAG=name)
args=[sys.executable,'-X','utf8',str(R/'run.py'),'java','NativeReactionWorker',str(R/'models/R401_ready.mph'),str(dst),str(count//32)]
if cfg['dependent_species']=='Cu':args.append('dependentCu')
args+=['reactionOnly','multicharge']
with (R/'logs'/f'{name}_launch.log').open('w') as out,(R/'logs'/f'{name}_launch.err').open('w') as err:p=subprocess.Popen(args,env=env,stdout=out,stderr=err)
while not (dst/'ready.txt').exists():
    if p.poll() is not None:raise RuntimeError(f'{name} setup failed')
    time.sleep(3)
while not (src/f'complete_{props["id"]}.json').exists():
    if (src/f'error_{props["id"]}.txt').exists():raise RuntimeError('Reference batch failed')
    time.sleep(3)
key='group_audit';(dst/'request.properties').write_text(f'id={key}\ndt_s={props["dt_s"]}\nsave=false\n')
while not (dst/f'complete_{key}.json').exists():
    if (dst/f'error_{key}.txt').exists():raise RuntimeError((dst/f'error_{key}.txt').read_text())
    time.sleep(3)
a=json.loads((src/f'complete_{props["id"]}.json').read_text());b=json.loads((dst/f'complete_{key}.json').read_text())
x=np.fromfile(src/f'output_{props["id"]}.bin',dtype='>f8').reshape(a['shape'])[:,:count];y=np.fromfile(dst/f'output_{key}.bin',dtype='>f8').reshape(b['shape'])
metrics={a['expressions'][i]:float(abs(x[i]-y[i]).max()/max(float(abs(x[i]).max()),1e-100)) for i in [0,1,2,6,7]}
report=dict(reference=source,reference_id=props['id'],reference_seconds=a['seconds'],new_seconds=b['seconds'],peak_scaled_field_differences=metrics,same_physical_equations=True)
(dst/'comparison.json').write_text(json.dumps(report,indent=2));print(json.dumps(dict(worker=name,**report)),flush=True)
