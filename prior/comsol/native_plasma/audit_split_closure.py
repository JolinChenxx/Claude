"""Local thermodynamic residuals, independent of a trivial global energy ledger."""
from pathlib import Path
import sys,json,numpy as np
from numba import njit
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from hem_common_thermo import mixture
from coupled_hem_plasma import MASS,ENERGY,CV,KB,EV
MASS=MASS[:18];ENERGY=ENERGY[:18];CV=CV[:18]
@njit
def nuclear(U,W,lt,lr,tab):
 out=np.zeros(U.shape[:2])
 for i in range(len(U)):
  for k in range(U.shape[1]):out[i,k]=mixture(W[i,k,0],U[i,k,0],U[i,k,1],lt,lr,tab)[0]
 return out
case=sys.argv[1];s=json.loads((R/'results'/case/'status.json').read_text());d=np.load(s['frame']);U,W,S=d['U'],d['W'],d['S'];g=np.load(R/'results'/case/'grid.npz');v=g['cell_volume_m3']
e=np.load(R/'data'/s.get('eos_directory','Cu_HEM_20261110')/'eos.npz');ts=e['temperatures_K'];rs=e['densities_kg_m3'];tab=np.ascontiguousarray(e['table'][:,2:].reshape(len(ts),len(rs),7))
en=nuclear(U,W,np.log10(ts),np.log10(rs),tab);chem=(S[:,:,:18]*ENERGY).sum(2)+S[:,:,19]*EV
base=U[:,:,1]*717.5+1.5*KB*U[:,:,0]*W[:,:,5]/MASS[10];dcv=(S[:,:,:18]*CV).sum(2)-base
extra_mass=np.zeros(U.shape[:2])
if S.shape[2]>20:
 chem+=(S[:,:,20:]*np.array([28.01877,64.85977,122.23977])*EV).sum(2)
 dcv+=S[:,:,20:].sum(2)*1.5*KB;extra_mass=S[:,:,20:].sum(2)*MASS[10]
kin=.5*(U[:,:,2]**2+U[:,:,3]**2)/(U[:,:,0]+U[:,:,1]);res=U[:,:,4]-kin-en-chem-dcv*W[:,:,0]
charge=S[:,:,[4,5,6,7,17]].sum(2)-S[:,:,[8,9,18]].sum(2)
if S.shape[2]>20:charge+=(S[:,:,20:]*np.array([2,3,4])).sum(2)
imax=np.unravel_index(np.argmax(abs(res)),res.shape)
report=dict(case=case,time_ns=s['saved_time_ns'],local_energy_abs_integral_J=float((abs(res)*v).sum()),local_energy_signed_integral_J=float((res*v).sum()),local_energy_abs_over_absorbed=float((abs(res)*v).sum()/s['absorbed_J']),max_energy_residual_J_m3=float(abs(res).max()),max_location_um=[float(g['r_m'][imax[0]]*1e6),float(g['z_m'][imax[1]]*1e6)],charge_C=float((charge*v).sum()*EV),Cu_species_mass_residual_kg=float(((S[:,:,10:18]*MASS[10:18]).sum(2)+extra_mass-U[:,:,0]*W[:,:,5]).__mul__(v).sum()),air_species_mass_residual_kg=float(((S[:,:,:10]*MASS[:10]).sum(2)-U[:,:,1]).__mul__(v).sum()))
(R/'results'/f'{case}_local_closure.json').write_text(json.dumps(report,indent=2));print(json.dumps(report))
