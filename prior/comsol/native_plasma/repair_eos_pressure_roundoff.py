"""Remove tiny negative compressibility in five tabulated coexistence points.

The shared-pressure copper/air inversion requires a nondecreasing isotherm.
Material energies, phase fractions and reaction data are retained unchanged.
"""
from pathlib import Path
import sys,os,json,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R));os.environ['ROUTE1_MULTICHARGE']='1'
from coupled_hem_plasma import ENERGY,CV
from hem_reactive_flash import _reactive_flash_rows
src=R/'data/Cu_HEM_extended_20261110';dst=R/'data/Cu_HEM_monotone_20261110';dst.mkdir(exist_ok=True)
d=np.load(src/'eos.npz');ts=d['temperatures_K'];rs=d['densities_kg_m3'];table=d['table'].copy();tab=table[:,2:].reshape(len(ts),len(rs),7)
old=tab[:,:,0].copy();tab[:,:,0]=np.maximum.accumulate(old,axis=1);ij=np.argwhere(tab[:,:,0]!=old)
changed=[dict(T_K=float(ts[i]),rho_kg_m3=float(rs[j]),old_pressure_Pa=float(old[i,j]),new_pressure_Pa=float(tab[i,j,0]),relative_change=float((tab[i,j,0]-old[i,j])/max(tab[i,j,0],1.))) for i,j in ij]
if max(x['relative_change'] for x in changed)>3e-6:raise ValueError('Unexpected material change beyond measured pressure roundoff')
np.savez_compressed(dst/'eos.npz',table=table,temperatures_K=ts,densities_kg_m3=rs)
lt=np.log10(ts);lr=np.log10(rs);tab=np.ascontiguousarray(tab);checks=[]
for name,path in [('rejected58','HC424/rejected_flash_state.npz'),('saved58','HC424/frames/frame_0001.npz'),('saved94p5','HC421/frames/frame_0002.npz')]:
    path=R/'results'/path
    if not path.exists():continue
    a=np.load(path);U,S,W=a['U'],a['S'],a['W']
    w,s,res=_reactive_flash_rows(U,S,W,lt,lr,tab,ENERGY,CV,True)
    scale=np.maximum(abs(U[:,:,4]),(U[:,:,0]+U[:,:,1])*1e4)
    checks.append(dict(name=name,maximum_local_energy_relative_residual=float(np.max(abs(res)/scale)),maximum_relative_T_change=float(np.max(abs(w[:,:,0]-W[:,:,0])/W[:,:,0])),maximum_absolute_T_change_K=float(np.max(abs(w[:,:,0]-W[:,:,0])))))
audit=json.loads((src/'audit.json').read_text());audit.update(pressure_monotonicity_repair=changed,reason='Tiny numerical downward pressure steps create multiple common-pressure roots in trace-copper/air cells. Nondecreasing envelope restores a continuous inversion.',nonpressure_columns_unchanged=True,energy_projection_checks=checks)
(dst/'audit.json').write_text(json.dumps(audit,indent=2));print(json.dumps(dict(changed=changed,checks=checks)),flush=True)
