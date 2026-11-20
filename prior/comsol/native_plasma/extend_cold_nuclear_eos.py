"""Continue the same copper nuclear EOS below200K for the expanding cold front."""
from pathlib import Path
import sys,pickle,json,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R/'vendor/pyaneos_reference'))
from pyaneos.phase import aneosv_point
from pyaneos import TCONV
src=R/'data/Cu_HEM_monotone_20261110';dst=R/'data/Cu_HEM_wide_20261110';dst.mkdir(exist_ok=True)
d=np.load(src/'eos.npz');audit=json.loads((src/'audit.json').read_text());m=pickle.loads((R/'results/Cu_nuclear_ANEOS_audit/material.pkl').read_bytes());m.no_tension=True;m.invalidate_nt()
ts=np.geomspace(10.,190.,60);rows=[]
for T in ts:
    for rho in d['densities_kg_m3']:
        q,lo,hi=aneosv_point(float(T/TCONV),float(rho/1000),m);lo*=1000;hi*=1000
        if q.kpa==2 and 0<lo<hi and lo<=rho<=hi:
            xv=np.clip((1/rho-1/hi)/(1/lo-1/hi),0,1);ag=np.clip(xv*rho/lo,0,1)
        else:xv=ag=0.
        row=[np.log10(T),np.log10(rho),q.p*.1,q.e*1e-4-audit['e_reference_J_kg'],q.cv*1e-4/TCONV,q.cs*.01,float(xv),float(ag),float(q.kpa)]
        if not np.isfinite(row).all() or row[4]<=0 or row[2]<-1e-4:raise ValueError(row)
        rows.append(row)
low=np.array(rows);p=low[:,2].reshape(len(ts),len(d['densities_kg_m3']));oldp=p.copy();p[:]=np.maximum.accumulate(p,axis=1)
table=np.vstack([low,d['table']]);np.savez_compressed(dst/'eos.npz',table=table,temperatures_K=np.r_[ts,d['temperatures_K']],densities_kg_m3=d['densities_kg_m3'])
audit.update(cold_extension='Same nuclear ANEOS evaluated10-190K; existing200K-1e6K rows unchanged. Resolves an actual accepted expansion front reaching200K; no temperature clamp.',old_rows_identical=bool(np.array_equal(table[len(low):],d['table'])),cold_min_cv_J_kg_K=float(low[:,4].min()),cold_max_pressure_monotonicity_adjustment_Pa=float(np.max(p-oldp)),shape=[len(ts)+len(d['temperatures_K']),len(d['densities_kg_m3'])])
(dst/'audit.json').write_text(json.dumps(audit,indent=2));print(json.dumps({k:audit[k] for k in ['cold_extension','old_rows_identical','cold_min_cv_J_kg_K','cold_max_pressure_monotonicity_adjustment_Pa','shape']}),flush=True)
