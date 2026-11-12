"""One bounded audit of the already fitted independent-material candidate."""
from pathlib import Path
import sys,pickle,json,time,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R/'vendor/pyaneos_reference'))
from pyaneos.phase import aneosv_point
from pyaneos import TCONV
folder=R/'results/Cu_ANEOS_extended_calibration'
m=pickle.loads((folder/'candidate.pkl').read_bytes());m.no_tension=True;m.invalidate_nt()
ts=np.unique(np.r_[np.geomspace(200,1e6,100),np.linspace(2500,8000,80)])
rs=np.unique(np.r_[np.geomspace(1e-10,15000,150),np.linspace(100,12000,100)])
p=np.empty((len(ts),len(rs)));bad=[];cvmin=1e99;start=time.time()
for i,T in enumerate(ts):
 for j,rho in enumerate(rs):
  try:
   q,lo,hi=aneosv_point(float(T/TCONV),float(rho/1000),m)
   p[i,j]=q.p*.1;cv=q.cv*1e-4/TCONV;cvmin=min(cvmin,cv)
   if not np.isfinite([q.p,q.e,q.cv,q.cs]).all() or q.p<-.001 or cv<=0:bad.append([T,rho,p[i,j],cv])
  except Exception as error:p[i,j]=np.nan;bad.append([T,rho,str(error)])
dp=np.diff(p,axis=1);scale=np.maximum(np.maximum(abs(p[:,:-1]),abs(p[:,1:])),1.)
report=dict(candidate='existing Cu_ANEOS_extended_calibration',material_fit=json.loads((folder/'summary.json').read_text())['actual'],temperature_density_shape=list(p.shape),bad_state_count=len(bad),first_bad_states=bad[:12],minimum_pressure_Pa=float(np.nanmin(p)),min_cv_J_kg_K=float(cvmin),largest_negative_pressure_increment_Pa=float(max(0,-np.nanmin(dp))),maximum_relative_pressure_drop=float(np.nanmax(np.maximum(-dp,0)/scale)),seconds=time.time()-start,adopted=False)
(folder/'stable_branch_audit.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
