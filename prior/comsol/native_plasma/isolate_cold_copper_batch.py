"""Replay the failing input in four disjoint native workers, without edits."""
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import json,time,numpy as np
R=Path(__file__).resolve().parent
src=R/'results/R417_operator';raw=(src/'state.txt').read_bytes();(R/'results/failed_cold_Cu95_input.txt').write_bytes(raw)
a=np.loadtxt(src/'state.txt');workers=['R414','R420','R422','R423']
def run(j):
 p=R/'results'/f'{workers[j]}_operator';x=a[j*256:(j+1)*256].copy();x[:,0]=np.arange(256)
 np.savetxt(p/'state.txt',x,fmt='%.16e');ident='coldCu95_partition'
 (p/'request.properties').write_text(f'id={ident}\ndt_s=2.5e-10\nsave=false\n')
 while True:
  error=p/f'error_{ident}.txt';done=p/f'complete_{ident}.json'
  if error.exists():return dict(worker=workers[j],begin=j*256,end=(j+1)*256,success=False,error=error.read_text(encoding='utf8'))
  if done.exists():
   meta=json.loads(done.read_text());return dict(worker=workers[j],begin=j*256,end=(j+1)*256,success=True,seconds=meta['seconds'])
  time.sleep(2)
with ThreadPoolExecutor(max_workers=4) as pool:
 rows=list(pool.map(run,range(4)))
(R/'results/native_cold_copper95_isolation.json').write_text(json.dumps(rows,indent=2));print(json.dumps(rows),flush=True)
