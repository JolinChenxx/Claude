"""Check pressure inversion on the actual near-pure-Cu rejected cell and saves."""
from pathlib import Path
import os,sys,json,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R));os.environ['ROUTE1_MULTICHARGE']='1'
from coupled_hem_plasma import ENERGY,CV
from hem_reactive_flash import _reactive_flash_rows
d=np.load(R/'data/Cu_HEM_monotone_20261110/eos.npz');lt=np.log10(d['temperatures_K']);lr=np.log10(d['densities_kg_m3']);tab=np.ascontiguousarray(d['table'][:,2:].reshape(len(lt),len(lr),7))
rows=[]
for case,kind in [('HC426','rejected'),('HC426','saved'),('HC424','saved')]:
    folder=R/'results'/case;path=folder/'rejected_flash_state.npz' if kind=='rejected' else Path(json.loads((folder/'status.json').read_text())['frame'])
    d=np.load(path);U,S,W=d['U'],d['S'],d['W'];w,s,res=_reactive_flash_rows(U,S,W,lt,lr,tab,ENERGY,CV,True);scale=np.maximum(abs(U[:,:,4]),(U[:,:,0]+U[:,:,1])*1e4)
    rows.append(dict(case=case,state=kind,maximum_relative_energy_residual=float(np.max(abs(res)/scale)),maximum_absolute_T_change_K=float(np.max(abs(w[:,:,0]-W[:,:,0]))),maximum_relative_T_change=float(np.max(abs(w[:,:,0]-W[:,:,0])/W[:,:,0]))))
report=dict(change='Common-pressure safeguarded Newton: up to48 instead of12 iterations, same residual target; density bracket stopping at1e-13 in log10rho.',reason='At trace air density2e-29kg/m3, old12-step cutoff left spurious6.6e-7 air volume; repeated evaluation jumped between near-pure-copper roots.',rows=rows)
(R/'results/common_pressure_precision_audit.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
