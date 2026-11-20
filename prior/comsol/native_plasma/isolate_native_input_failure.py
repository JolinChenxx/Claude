"""Locate failing original cells by disjoint padded native batches."""
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import numpy as np,json,time
R=Path(__file__).resolve().parent;root=R/'results';a=np.loadtxt(root/'failed_cold_air95p75_input.txt')
pad=np.zeros(a.shape[1]);pad[1]=1;pad[2]=293.15;pad[3]=717.5;pad[4]=1e9;pad[5]=1e9*1.5*1.380649e-23*293.15/1.602176634e-19;pad[7]=1/(.0280134/6.02214076e23)-1e9;pad[11]=1e9

def solve(worker,ids,key):
    p=root/f'{worker}_operator';slots=json.loads((p/'worker_config.json').read_text())['slots'];b=np.tile(pad,(slots,1));b[:len(ids)]=a[ids];b[:,0]=np.arange(slots)
    np.savetxt(p/'state.txt',b,fmt='%.16e');(p/'request.properties').write_text(f'id={key}\ndt_s=2.5e-10\nsave=false\n')
    start=time.time()
    while True:
        if (p/f'error_{key}.txt').exists():return False
        if (p/f'complete_{key}.json').exists():return True
        if time.time()-start>240:raise TimeoutError(worker)
        time.sleep(2)

ids=np.arange(len(a));history=[]
for level in range(6):
    parts=[x for x in np.array_split(ids,3) if len(x)]
    with ThreadPoolExecutor(max_workers=3) as pool:
        futures=[pool.submit(solve,w,part,f'isolate95_{level}_{j}') for j,(w,part) in enumerate(zip(['R415','R416','R421'],parts))]
        success=[p.result() for p in futures]
    history.append(dict(level=level,parts=[x.tolist() for x in parts],success=success));print('ISOLATION',level,[(len(x),ok) for x,ok in zip(parts,success)],flush=True)
    bad=[part for part,ok in zip(parts,success) if not ok]
    if not bad:break
    ids=np.concatenate(bad)
    if len(ids)<=12:break
(root/'native_cold_failure_isolation.json').write_text(json.dumps(dict(suspect_ids=ids.tolist(),history=history),indent=2))
print('SUSPECT_IDS',ids.tolist(),flush=True)
