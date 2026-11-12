"""Audit a small native worker on the actual trace-electron air cells."""
from pathlib import Path
import os,sys,json,time,subprocess,numpy as np
R=Path(__file__).resolve().parent;src=R/'results/R427_operator';dst=R/'results/R434_operator';dst.mkdir(exist_ok=False)
while True:
 req=(src/'request.properties').read_text();raw=(src/'state.txt').read_bytes()
 if req==(src/'request.properties').read_text() and (src/'state.txt').stat().st_mtime_ns<=(src/'request.properties').stat().st_mtime_ns:break
props=dict(line.split('=',1) for line in req.splitlines() if '=' in line);(dst/'reference_input.txt').write_bytes(raw);a=np.loadtxt(dst/'reference_input.txt');s=a[:,7:]
charged=s[:,4:10].sum(1)+s[:,17]+(s[:,18:]*np.array([2,3,4])).sum(1);idx=np.flatnonzero((a[:,4]<1e-6*np.maximum(charged,1))&(charged>1e12));idx=idx[:128]
if not len(idx):raise RuntimeError('No trace-air cells in snapshot')
x=np.zeros((128,a.shape[1]));x[:,1]=1.;x[:,2]=293.15;x[:,3]=717.5;x[:,4]=1e9;x[:,5]=1.5e9*1.380649e-23*293.15/1.602176634e-19;x[:,7]=6.02214076e23/.0280134-1e9;x[:,11]=1e9;x[:len(idx)]=a[idx];x[:,0]=np.arange(128)
np.savetxt(dst/'state.txt',x,fmt='%.16e');cfg=json.loads((src/'worker_config.json').read_text());cfg['slots']=128;(dst/'worker_config.json').write_text(json.dumps(cfg,indent=2))
env=os.environ.copy();env.update(ROUTE1_THREADS='4',ROUTE1_LOG_TAG='R434')
with (R/'logs/R434_launch.log').open('w') as out,(R/'logs/R434_launch.err').open('w') as err:proc=subprocess.Popen([sys.executable,'-X','utf8',str(R/'run.py'),'java','NativeReactionWorker',str(R/'models/R401_ready.mph'),str(dst),'4','reactionOnly','multicharge','actualElectronRemoval'],env=env,stdout=out,stderr=err)
while not (dst/'ready.txt').exists():
 if proc.poll() is not None:raise RuntimeError('Trace worker setup failed')
 time.sleep(2)
ident='trace_air_audit';(dst/'request.properties').write_text(f'id={ident}\ndt_s={props["dt_s"]}\nsave=false\n')
while not (dst/f'complete_{ident}.json').exists() or not (src/f'complete_{props["id"]}.json').exists():
 if (dst/f'error_{ident}.txt').exists():raise RuntimeError((dst/f'error_{ident}.txt').read_text(encoding='utf8'))
 time.sleep(2)
ma=json.loads((src/f'complete_{props["id"]}.json').read_text());mb=json.loads((dst/f'complete_{ident}.json').read_text());ra=np.fromfile(src/f'output_{props["id"]}.bin',dtype='>f8').reshape(ma['shape'])[:,idx];rb=np.fromfile(dst/f'output_{ident}.bin',dtype='>f8').reshape(mb['shape'])[:,:len(idx)]
diff={ma['expressions'][i]:float(abs(ra[i]-rb[i]).max()/max(abs(ra[i]).max(),1e-100)) for i in [0,1,2,6,7]}
report=dict(reference=props['id'],reference_seconds=ma['seconds'],trace_seconds=mb['seconds'],trace_cells=len(idx),reference_indices=idx.tolist(),peak_scaled_field_differences=diff,scope='Same exact selected inputs and native equations; separate only stiff trace-electron cells from large cold-air group.')
(dst/'comparison.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
