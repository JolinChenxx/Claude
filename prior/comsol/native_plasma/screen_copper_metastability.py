"""Expose the existing ANEOS homogeneous branch for later nucleation work.

No EOS refit and no production-table change. A stable equilibrium mixture
cannot by itself supply a metastable-liquid pressure history or nucleation rate.
"""
from pathlib import Path
import sys,pickle,json,numpy as np
from scipy.optimize import brentq
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R/'vendor/pyaneos_reference'))
from pyaneos.eval import aneos1
from pyaneos import TCONV
m=pickle.loads((R/'results/Cu_nuclear_ANEOS_audit/material.pkl').read_bytes());nt=m.as_nt()
co=np.loadtxt(R/'results/Cu_nuclear_ANEOS_audit/coexistence.csv',delimiter=',',skiprows=1)
rho=np.geomspace(.01,9500,800);rows=[];curves=[]
for T in [5000.,6500.,7500.,8000.,8500.,8800.,9000.]:
    def raw(d):return aneos1(T/TCONV,d/1000,nt,float(nt.ack[71]),with_electronic=False)
    q=[raw(d) for d in rho];p=np.array([v.p*.1 for v in q]);der=np.array([v.dpdr*1e-4 for v in q]);cv=np.array([v.cv*1e-4/TCONV for v in q])
    intervals=np.flatnonzero(der[:-1]*der[1:]<0)
    roots=[brentq(lambda d:raw(d).dpdr,rho[k],rho[k+1]) for k in intervals]
    curves.append([T,p,der])
    rows.append(dict(T_K=T,spinodal_densities_kg_m3=roots,spinodal_pressures_Pa=[raw(d).p*.1 for d in roots],
                     saturation_rho_liquid_kg_m3=float(np.interp(T,co[:,0],co[:,1])) if T<=co[-1,0] else None,
                     saturation_pressure_Pa=float(np.interp(T,co[:,0],co[:,3])) if T<=co[-1,0] else None,
                     negative_cv_count=int(np.count_nonzero(cv<=0))))
out=R/'results/Cu_metastable_screen';out.mkdir(exist_ok=True)
np.savez_compressed(out/'homogeneous_isotherms.npz',rho_kg_m3=rho,temperatures_K=np.array([q[0] for q in curves]),pressure_Pa=np.array([q[1] for q in curves]),dpdrho=np.array([q[2] for q in curves]))
report=dict(source='Same nuclear-only legacy M-ANEOS Cu parameters; raw ANEOS1 before the Maxwell phase-equilibrium construction.',critical_T_K=float(m.tct*TCONV),rows=rows,scope='Material diagnostic only. Negative dP/drho marks an unstable homogeneous branch; negative pressure is not inserted into the stable HEM production table.',nucleation_limit='No nucleation time is inferred from an equilibrium HEM path. Finite-rate release additionally requires an evolved metastable liquid state, a sourced surface-tension law, and nucleation/growth kinetics; temperature alone or a fixed release multiplier is insufficient.')
(out/'summary.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
