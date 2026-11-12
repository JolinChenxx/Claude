"""Measure existing material kernels on a saved field without changing a run."""
from pathlib import Path
import sys,json,time,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from numba import set_num_threads
from coupled_hem_plasma import thermo_coupled,advect
from axisymmetric_hem_fv import step,stable_dt
case=sys.argv[1];s=json.loads((R/'results'/case/'status.json').read_text());f=np.load(s['frame']);g=np.load(R/'results'/case/'grid.npz');e=np.load(R/'data/Cu_HEM_20261110/eos.npz')
U,W,S=f['U'],f['W'],f['S'];re,rc,zc,ze,volume=[g[k] for k in ['radial_edges_m','r_m','z_m','axial_edges_m','cell_volume_m3']];dr=np.diff(re);dz=np.diff(ze)
lt=np.log10(e['temperatures_K']);lr=np.log10(e['densities_kg_m3']);tab=np.ascontiguousarray(e['table'][:,2:].reshape(len(lt),len(lr),7));dt=stable_dt(W,dr,dz)
alpha=np.zeros(U.shape[:2]);idx=f['reaction_indices'];alpha[idx[:,0],idx[:,1]]=np.maximum(f['reaction_outputs'][16],0)*W[idx[:,0],idx[:,1],6]
def once():
    t0=time.perf_counter();a,da,de,dm,bad,Fr,Fz,Qe=step(U.copy(),W,rc,zc,dr,dz,re,volume,float(f['time_s']),dt,.1,alpha);t1=time.perf_counter()
    b=advect(S,U,a,Fr,Fz,volume,re,dz,dt);t2=time.perf_counter();c=thermo_coupled(a,b,lt,lr,tab,W.copy());t3=time.perf_counter()
    return np.array([t1-t0,t2-t1,t3-t2])
set_num_threads(4);once()
rows=[]
for n in [1,2,4,8,12,16,24,32]:
    set_num_threads(n);once();v=np.median([once() for _ in range(4)],axis=0)
    rows.append(dict(threads=n,material_step_s=v[0],advection_s=v[1],thermo_s=v[2],total_s=float(v.sum())))
report=dict(case=case,time_ns=s['saved_time_ns'],cells=list(U.shape[:2]),stable_dt_ns=dt*1e9,other_jobs_running=True,measurements=rows)
(R/'results'/f'{case}_kernel_benchmark.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
