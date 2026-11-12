"""One saved-state material interval: nuclear versus electron-inclusive waves."""
from pathlib import Path
import os,sys,json,time,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
os.environ['ROUTE1_MULTICHARGE']='1';os.environ['ROUTE1_PHASE_CHARGE_PARTNERS']='1'
os.environ['ROUTE1_PHASE_FLASH']='coupled';os.environ['ROUTE1_PHASE_FEED']='thermal_neutral'
from coupled_hem_plasma import advect,update_thermo,EV,KB,HEAVY,MASS
from axisymmetric_hem_fv import step,stable_dt
from split_absorption_current import current_absorption
from numba import set_num_threads
set_num_threads(12)
flag=sys.argv[1];sys.argv[1]='wave_audit_'+flag
out=R/'results'/sys.argv[1];out.mkdir(exist_ok=True)
g=np.load(R/'results/HC431/grid.npz');re=g['radial_edges_m'];rc=g['r_m'];zc=g['z_m'];dr=np.diff(re);dz=np.diff(g['axial_edges_m']);vol=g['cell_volume_m3']
e=np.load(R/'data/Cu_HEM_wide_20261110/eos.npz');lt=np.log10(e['temperatures_K']);lr=np.log10(e['densities_kg_m3']);tab=np.ascontiguousarray(e['table'][:,2:].reshape(len(lt),len(lr),7))
d=np.load(json.loads((R/'results/HC431/status.json').read_text())['frame']);U,W,S=d['U'].copy(),d['W'].copy(),d['S'].copy();t=float(d['time_s']);stop=94.25e-9
def augment(W,U,S):
 if flag!='electrons':return W
 base_n=U[:,:,1]/(.79*.0280134+.21*.0319988)*6.02214076e23+U[:,:,0]*W[:,:,5]/MASS[10]
 dp=np.maximum(0,(S[:,:,HEAVY].sum(2)-base_n)*KB*W[:,:,0])
 W[:,:,2]=np.sqrt(W[:,:,2]**2+5/3*(2/3*S[:,:,19]*EV+dp)/U[:,:,:2].sum(2))
 return W
W=augment(W,U,S);steps=0;reject=0;start=time.time();failed=False
while t<stop-1e-20:
 alpha=current_absorption(S,W);dt=min(stable_dt(W,dr,dz),stop-t);old=U.copy();oldS=S.copy()
 divu=np.gradient(W[:,:,9],rc,axis=0)+W[:,:,9]/rc[:,None]+np.gradient(W[:,:,3],zc,axis=1)
 for attempt in range(24):
  U,da,de,dm,bad,Fr,Fz,Qe=step(old.copy(),W,rc,zc,dr,dz,re,vol,t,dt,.1,alpha)
  if bad:dt*=.5;reject+=1;continue
  S=advect(oldS,old,U,Fr,Fz,vol,re,dz,dt)
  if np.any(S<-1e-12*np.maximum(abs(oldS),1)):dt*=.5;reject+=1;continue
  S[:,:,19]*=np.exp(-2/3*divu*dt);S[:,:,19]+=dt*Qe/EV;S=np.maximum(S,0)
  try:nw,ns=update_thermo(U,S,lt,lr,tab,W,phase=True)
  except RuntimeError:dt*=.5;reject+=1;continue
  W,S=augment(nw,U,ns),ns;break
 else:failed=True;break
 t+=dt;steps+=1
report=dict(wave=flag,time_ns=t*1e9,completed=not failed,steps=steps,rejected=reject,seconds=time.time()-start,Tmin_K=float(W[:,:,0].min()),front_temperature_K=float(W[0,265,0]),max_temperature_K=float(W[:,:,0].max()))
(out/'audit.json').write_text(json.dumps(report,indent=2));np.savez_compressed(out/'state.npz',U=U,W=W,S=S,time_s=t);print(json.dumps(report),flush=True)
