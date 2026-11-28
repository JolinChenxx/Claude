"""Compare root implementation on saved cold and strongly ionized states."""
from pathlib import Path
import os,sys,json,time,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
variant=sys.argv[1]
os.environ.update(ROUTE1_MULTICHARGE='1',ROUTE1_PHASE_CHARGE_PARTNERS='1',ROUTE1_PLASMA_WAVE_BOUND='1',ROUTE1_FLASH_CHECK_HINT='1' if variant=='hint' else '0')
from coupled_hem_plasma import ENERGY,CV
from hem_reactive_flash import reactive_flash
from numba import set_num_threads
set_num_threads(12)
e=np.load(R/'data/Cu_HEM_wide_20261110/eos.npz');lt=np.log10(e['temperatures_K']);lr=np.log10(e['densities_kg_m3']);tab=np.ascontiguousarray(e['table'][:,2:].reshape(len(lt),len(lr),7))
reports=[];saved={}
for case,frame in [('HC438','frame_0000.npz'),('HC431','frame_0018.npz')]:
 d=np.load(R/'results'/case/'frames'/frame);U,S,W=d['U'],d['S'],d['W']
 args=(U,S,W,lt,lr,tab,ENERGY,CV,True)
 nw,ns,err=reactive_flash(*args)
 begin=time.perf_counter()
 for _ in range(20):nw,ns,err=reactive_flash(*args)
 elapsed=(time.perf_counter()-begin)/20
 scale=np.maximum(abs(U[:,:,4]),U[:,:,:2].sum(2)*1e4)
 reports.append(dict(case=case,seconds_per_call=elapsed,failed_cells=int(np.isnan(err).sum()),max_scaled_energy_residual=float(np.nanmax(abs(err)/scale))))
 saved[case+'_W']=nw;saved[case+'_S']=ns
np.savez_compressed(R/'results'/f'flash_hint_{variant}.npz',**saved)
(R/'results'/f'flash_hint_{variant}.json').write_text(json.dumps(reports,indent=2));print(json.dumps(reports),flush=True)
