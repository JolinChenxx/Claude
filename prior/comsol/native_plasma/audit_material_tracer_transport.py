"""Physical consistency check: a uniform material tracer follows material mass."""
from pathlib import Path
import sys,json,numpy as np,time
from numba import set_num_threads
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from axisymmetric_hem_fv import step,stable_dt
from coupled_hem_plasma import advect,MASS
set_num_threads(8)
source=json.loads((R/'results/HC404/status.json').read_text());d=np.load(source['frame']);old=d['U'].copy();W=d['W'];g=np.load(R/'results/HC404/grid.npz');rc=g['r_m'];zc=g['z_m'];re=g['radial_edges_m'];dr=np.diff(re);dz=np.diff(g['axial_edges_m']);v=g['cell_volume_m3']
S=np.zeros((*old.shape[:2],20));S[:,:,10]=old[:,:,0]/MASS[10];S[:,:,0]=old[:,:,1]/MASS[0];S[:,:,18]=(old[:,:,0]+old[:,:,1])*1e9
dt=stable_dt(W,dr,dz);new,_,_,_,_,Fr,Fz,_=step(old.copy(),W,rc,zc,dr,dz,re,v,96e-9,dt,.1,np.zeros(old.shape[:2]));out=advect(S,old,new,Fr,Fz,v,re,dz,dt)
errors=[np.max(abs(out[:,:,10]*MASS[10]-new[:,:,0]))/np.max(new[:,:,0]),np.max(abs(out[:,:,0]*MASS[0]-new[:,:,1]))/np.max(new[:,:,1]),np.max(abs(out[:,:,18]/1e9-new[:,:,0]-new[:,:,1]))/np.max(new[:,:,0]+new[:,:,1])]
assert max(errors)<1e-12,errors
report={'source':'HC40496ns','dt_s':float(dt),'relative_Linf_Cu_air_total_tracer':list(map(float,errors)),'old_state_unchanged':bool(np.array_equal(old,d['U'])),'passed':True}
(R/'results/material_tracer_transport_audit.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
