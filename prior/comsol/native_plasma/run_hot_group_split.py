"""Check native hot-cell batching on identical inputs, then resume thermal supply."""
from pathlib import Path
import os,sys,time,json,subprocess
import numpy as np
R=Path(__file__).resolve().parent;old=R/'results/R408_operator';new=R/'results/R409_operator';out=R/'results/hot_group_audit';out.mkdir(exist_ok=True)
while True:
    raw=(old/'request.properties').read_text();props=dict(x.split('=',1) for x in raw.splitlines() if '=' in x)
    a=np.loadtxt(old/'state.txt')
    if raw==(old/'request.properties').read_text() and props['id'].startswith('stepHC414'):break
    time.sleep(2)
hot=np.flatnonzero((a[:,2]>5000)|(2/3*a[:,5]/np.maximum(a[:,4],1e-100)*11604.51812>5000))
if not len(hot):raise RuntimeError('No hot reference states')
if len(hot)>256:hot=hot[np.unique(np.linspace(0,len(hot)-1,256).astype(int))]
np.savez(out/'reference_input.npz',state=a,indices=hot)
(out/'reference_request.json').write_text(json.dumps(props,indent=2))
while not (old/f'complete_{props["id"]}.json').exists():
    if (old/f'error_{props["id"]}.txt').exists():raise RuntimeError('Reference collision solve failed')
    time.sleep(5)
while not (new/'ready.txt').exists():time.sleep(5)
b=np.loadtxt(new/'state.txt');b[:len(hot)]=a[hot];b[:,0]=np.arange(256)
np.savetxt(new/'state.next',b,fmt='%.16e');os.replace(new/'state.next',new/'state.txt')
key='hot_group_audit_001';(new/'request.next').write_text(f'id={key}\ndt_s={props["dt_s"]}\nsave=true\n');os.replace(new/'request.next',new/'request.properties')
while not (new/f'complete_{key}.json').exists():
    if (new/f'error_{key}.txt').exists():raise RuntimeError((new/f'error_{key}.txt').read_text())
    time.sleep(5)
om=json.loads((old/f'complete_{props["id"]}.json').read_text());nm=json.loads((new/f'complete_{key}.json').read_text())
x=np.fromfile(old/f'output_{props["id"]}.bin',dtype='>f8').reshape(om['shape'])[:,hot]
y=np.fromfile(new/f'output_{key}.bin',dtype='>f8').reshape(nm['shape'])[:,:len(hot)]
checks={}
for k in [0,1,2,6,7,16,17]:
    checks[om['expressions'][k]]=float(np.max(abs(x[k]-y[k]))/max(float(np.max(abs(x[k]))),1e-100))
audit=dict(reference_request=props['id'],same_input_states=len(hot),relative_differences=checks,small_batch_seconds=nm['seconds'],large_batch_seconds=om['seconds'],passed=max(checks.values())<.01,
    note='Identical local inputs, rates and native tolerances; native RMS error weighting changes with the number of independent cells.')
(out/'comparison.json').write_text(json.dumps(audit,indent=2));print(json.dumps(audit),flush=True)
if not audit['passed']:raise RuntimeError('Hot batch difference exceeds 1%; do not use automatically')
while 'STOPPED_AT_SAVED_FRAME' not in (R/'logs/HC414.log').read_text(encoding='utf8',errors='replace'):
    if 'Traceback' in (R/'logs/HC414.log').read_text(encoding='utf8',errors='replace'):raise RuntimeError('Thermal source stopped with error')
    time.sleep(20)
history=json.loads((R/'results/HC414/history.json').read_text());snapshot=R/'results/HC414_at92';snapshot.mkdir(exist_ok=True)
state=next(q for q in history if abs(q['saved_time_ns']-92)<1e-6)
(snapshot/'status.json').write_text(json.dumps(state,indent=2));(snapshot/'history.json').write_text(json.dumps([q for q in history if q['saved_time_ns']<=92+1e-6],indent=2))
import shutil
for filename in ['grid.npz','configuration.json']:shutil.copy2(R/'results/HC414'/filename,snapshot/filename)
env=os.environ.copy();env.update(ROUTE1_PHASE_FEED='thermal_neutral',ROUTE1_EOS_INVERSION='warm',ROUTE1_MATERIAL_THREADS='12',ROUTE1_CU_REACTION_WORKER='R408_operator',ROUTE1_HOT_CU_REACTION_WORKER='R409_operator',ROUTE1_PARALLEL_REACTION_GROUPS='1',ROUTE1_REACTION_SCHEDULE='110:0.5,180:2')
cfgpath=R/'day_session_20261110.json';cfg=json.loads(cfgpath.read_text(encoding='utf8'));cfg['split_material_cases']=[x if x!='HC414' else 'HC417' for x in cfg['split_material_cases']];cfgpath.write_text(json.dumps(cfg,indent=2),encoding='utf8')
with (R/'logs/HC417.log').open('wb') as log:subprocess.run([sys.executable,'-X','utf8',str(R/'coupled_hem_plasma.py'),'HC417','180','R404_operator','HC414_at92'],env=env,stdout=log,stderr=subprocess.STDOUT,check=True)
