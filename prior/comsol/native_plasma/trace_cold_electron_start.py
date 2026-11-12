from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import json,time,numpy as np
R=Path(__file__).resolve().parent;a=np.loadtxt(R/'results/failed_cold_Cu95_input.txt');workers=['R414','R420','R422','R423'];ends=[1e-14,1e-13,3e-13,1e-12]
def run(j):
 p=R/'results'/f'{workers[j]}_operator';x=np.tile(a[-1],(256,1));x[0]=a[505];x[:,0]=np.arange(256);np.savetxt(p/'state.txt',x,fmt='%.16e');ident=f'coldCu505_early{j}'
 (p/'request.properties').write_text(f'id={ident}\ndt_s={ends[j]:.17g}\nsave=true\n')
 while True:
  if (p/f'error_{ident}.txt').exists():return dict(time_s=ends[j],success=False)
  if (p/f'complete_{ident}.json').exists():
   meta=json.loads((p/f'complete_{ident}.json').read_text());d=np.fromfile(p/f'output_{ident}.bin',dtype='>f8').reshape(meta['shape'])
   return dict(time_s=ends[j],success=True,fields={name:float(d[i,0]) for i,name in enumerate(meta['expressions'])})
  time.sleep(1)
with ThreadPoolExecutor(max_workers=4) as pool:rows=list(pool.map(run,range(4)))
(R/'results/native_cold_electron_start.json').write_text(json.dumps(rows,indent=2));print(json.dumps([{k:v for k,v in q.items() if k!='fields'}|({'ne':q['fields']['plas.ne'],'en':q['fields']['plas.en'],'TeK':q['fields']['TeK']} if 'fields' in q else {}) for q in rows]),flush=True)
