"""Measure complete material substeps at a real ionized state; no native requests."""
from pathlib import Path
import os,sys,time,json,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
os.environ.update(ROUTE1_MULTICHARGE='1',ROUTE1_PHASE_CHARGE_PARTNERS='1',ROUTE1_PLASMA_WAVE_BOUND='1',ROUTE1_PHASE_FLASH='coupled',ROUTE1_PHASE_FEED='thermal_neutral')
from coupled_hem_plasma import advect,update_thermo,EV
from axisymmetric_hem_fv import step,stable_dt
from split_absorption_current import current_absorption
from numba import set_num_threads
sys.argv[1:] = ['material_thread_audit'];(R/'results/material_thread_audit').mkdir(exist_ok=True)
g=np.load(R/'results/HC431/grid.npz');re=g['radial_edges_m'];rc=g['r_m'];zc=g['z_m'];dr=np.diff(re);dz=np.diff(g['axial_edges_m']);v=g['cell_volume_m3']
e=np.load(R/'data/Cu_HEM_wide_20261110/eos.npz');lt=np.log10(e['temperatures_K']);lr=np.log10(e['densities_kg_m3']);tab=np.ascontiguousarray(e['table'][:,2:].reshape(len(lt),len(lr),7))
d=np.load(json.loads((R/'results/HC431/status.json').read_text())['frame']);U,W,S=d['U'],d['W'],d['S'];t=float(d['time_s'])
set_num_threads(8);W,S=update_thermo(U,S,lt,lr,tab,W);dt=stable_dt(W,dr,dz)
div=np.gradient(W[:,:,9],rc,axis=0)+W[:,:,9]/rc[:,None]+np.gradient(W[:,:,3],zc,axis=1)
def one():
 alpha=current_absorption(S,W)
 u,da,de,dm,bad,fr,fz,qe=step(U.copy(),W,rc,zc,dr,dz,re,v,t,dt,.1,alpha)
 s=advect(S,U,u,fr,fz,v,re,dz,dt);s[:,:,19]*=np.exp(-2/3*div*dt);s[:,:,19]+=dt*qe/EV
 if bad or np.any(s<0):raise RuntimeError('Benchmark step requires a rejected-state path')
 w,s=update_thermo(u,s,lt,lr,tab,W)
 return u,w,s
ref=one();rows=[]
for n in [4,8,16,32,64,16,8,4]:
 set_num_threads(n);one();begin=time.perf_counter()
 for _ in range(10):q=one()
 elapsed=(time.perf_counter()-begin)/10
 rows.append(dict(threads=n,seconds_per_step=elapsed,max_field_difference=max(float(np.max(abs(a-b))/max(np.max(abs(a)),1.)) for a,b in zip(ref,q))))
 print(json.dumps(rows[-1]),flush=True)
(R/'results/material_thread_audit/timing.json').write_text(json.dumps(rows,indent=2))
