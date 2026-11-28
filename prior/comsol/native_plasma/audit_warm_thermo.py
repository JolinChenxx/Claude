"""Compare accelerated and original EOS inversions on actual material states."""
from pathlib import Path
import sys,json,time,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from numba import set_num_threads
from axisymmetric_hem_fv import fields
from hem_warm_thermo import fields_warm
from coupled_hem_plasma import ENERGY,CV,KB,MASS,EV
set_num_threads(12)
e=np.load(R/'data/Cu_HEM_20261110/eos.npz');lt=np.log10(e['temperatures_K']);lr=np.log10(e['densities_kg_m3']);tab=np.ascontiguousarray(e['table'][:,2:].reshape(len(lt),len(lr),7))
rows=[]
for case in sys.argv[1:]:
    s=json.loads((R/'results'/case/'status.json').read_text());f=np.load(s['frame']);U,W=f['U'],f['W'];nuclear=U.copy()
    if 'S' in f:
        S=f['S'];chem=(S[:,:,:18]*ENERGY).sum(2)+S[:,:,19]*EV
        base=U[:,:,1]*717.5+1.5*KB*U[:,:,0]*W[:,:,5]/MASS[10]
        nuclear[:,:,4]-=chem+((S[:,:,:18]*CV).sum(2)-base)*W[:,:,0]
    fields(nuclear,lt,lr,tab);fields_warm(nuclear,lt,lr,tab,W)
    ta=time.perf_counter();a=fields(nuclear,lt,lr,tab);ta=time.perf_counter()-ta
    tb=time.perf_counter();b=fields_warm(nuclear,lt,lr,tab,W);tb=time.perf_counter()-tb
    delta=np.abs(a-b);relative=delta/np.maximum(abs(a),1.)
    row=dict(case=case,time_ns=s['saved_time_ns'],max_scaled_difference=float(relative.max()),max_temperature_difference_K=float(delta[:,:,0].max()),old_seconds=ta,warm_seconds=tb,speedup=ta/tb)
    rows.append(row)
    if relative.max()>1e-6:raise RuntimeError(str(row))
report=dict(same_EOS=True,same_energy_tolerance=True,rows=rows)
(R/'results/warm_thermo_audit.json').write_text(json.dumps(report,indent=2));print(json.dumps(report))
