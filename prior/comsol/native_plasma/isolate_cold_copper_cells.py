"""Locate failing physical inputs; retain every original number and rate."""
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import json,time,numpy as np
R=Path(__file__).resolve().parent;a=np.loadtxt(R/'results/failed_cold_Cu95_input.txt');workers=['R414','R420','R422','R423']
def run(job):
 j,lo,hi=job;p=R/'results'/f'{workers[j]}_operator'
 x=np.tile(a[-1],(256,1));x[:hi-lo]=a[lo:hi];x[:,0]=np.arange(256);np.savetxt(p/'state.txt',x,fmt='%.16e');ident=f'coldCu95_{lo}_{hi}'
 (p/'request.properties').write_text(f'id={ident}\ndt_s=2.5e-10\nsave=false\n')
 while True:
  error=p/f'error_{ident}.txt';done=p/f'complete_{ident}.json'
  if error.exists():return dict(worker=workers[j],begin=lo,end=hi,success=False)
  if done.exists():return dict(worker=workers[j],begin=lo,end=hi,success=True)
  time.sleep(1)
pending=[(256,512)];failed=[];records=[]
while pending:
 lo,hi=pending.pop(0);edges=np.unique(np.linspace(lo,hi,min(4,hi-lo)+1,dtype=int));jobs=[(j,int(x),int(y)) for j,(x,y) in enumerate(zip(edges[:-1],edges[1:]))]
 with ThreadPoolExecutor(max_workers=4) as pool:rows=list(pool.map(run,jobs))
 records+=rows
 for q in rows:
  if q['success']:continue
  if q['end']-q['begin']==1:failed.append(q['begin'])
  else:pending.append((q['begin'],q['end']))
 print(json.dumps(rows),flush=True)
(R/'results/native_cold_copper95_cells.json').write_text(json.dumps(dict(failed_indices=failed,records=records),indent=2))
