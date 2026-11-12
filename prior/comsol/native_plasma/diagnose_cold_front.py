"""Read-only decomposition of the cold-front thermodynamic stopping state."""
from pathlib import Path
import os,sys,json,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
os.environ['ROUTE1_MULTICHARGE']='1';os.environ['ROUTE1_PHASE_CHARGE_PARTNERS']='1'
from coupled_hem_plasma import ENERGY,EV,KB,HEAVY
g=np.load(R/'results/HC431/grid.npz');rows=[]
for case in ['HC431','HC434','HC435']:
 p=R/'results'/case
 path=p/'rejected_flash_state.npz'
 if case=='HC431':path=Path(json.loads((p/'status.json').read_text())['frame'])
 if not path.exists():continue
 d=np.load(path);U,W,S=d['U'],d['W'],d['S']
 for k in range(262,269):
  u,w,s=U[0,k],W[0,k],S[0,k];rho=u[:2].sum();ke=.5*(u[2]**2+u[3]**2)/rho
  chemical=float(s[HEAVY]@ENERGY);electron=s[19]*EV;pe=2/3*electron
  rows.append(dict(case=case,k=k,z_um=g['z_m'][k]*1e6,rho_Cu=u[0],rho_air=u[1],T=w[0],p=w[1],uz=u[3]/rho,cs=w[2],gas_fraction=w[6],vapor_fraction=w[5],kinetic_J_m3=ke,total_J_m3=u[4],internal_J_m3=u[4]-ke,chemical_J_m3=chemical,electron_J_m3=electron,pe_Pa=pe,ne=s[18],Te_eV=2/3*s[19]/max(s[18],1e-100),frozen_electron_sound_increment_ms=float(np.sqrt(5/3*pe/rho))))
(R/'results/cold_front_diagnosis.json').write_text(json.dumps(rows,indent=2))
for q in rows:
 if q['k']==265:print(json.dumps(q),flush=True)
