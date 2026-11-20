"""Generate SI nuclear/cold copper EOS tables for a compressible mechanism test.

The three-target fitted candidate has negative near-critical gas pressure and is
rejected for this calculation. Use the documented legacy nuclear-only ANEOS;
retain its 12% boiling-point mismatch rather than fitting any plume observation.
"""
from pathlib import Path
import sys,pickle,json,time
import numpy as np
from scipy.optimize import brentq
R=Path(__file__).resolve().parent
sys.path.insert(0,str(R/'vendor/pyaneos_reference'))
from pyaneos.phase import aneosv_point
from pyaneos import TCONV

def main():
    out=R/'data/Cu_HEM_20261110';out.mkdir(exist_ok=True)
    m=pickle.loads((R/'results/Cu_nuclear_ANEOS_audit/material.pkl').read_bytes())
    m.no_tension=True
    m.invalidate_nt()
    def point(T,rho):return aneosv_point(float(T/TCONV),float(rho/1000),m)
    rho_ref=brentq(lambda rho:point(293.15,rho)[0].p*.1-101325,8500,9500)
    eref=point(293.15,rho_ref)[0].e*1e-4
    valid=(m.ttwo>0)&(m.rvap>0)&(m.rsol>0)
    coexist=np.array([m.ttwo[valid]*TCONV,m.rvap[valid]*1000,m.rsol[valid]*1000]).T
    coexist=coexist[np.argsort(coexist[:,0])]
    temperatures=np.unique(np.r_[np.geomspace(200,60000,125),293.15,1200,1300,1350,1358,1365,1400,1500,np.linspace(7000,10000,61)])
    densities=np.unique(np.r_[np.geomspace(1e-10,15000,140),np.linspace(500,5000,46),np.linspace(6500,10500,81),rho_ref])
    rows=[];bad=[];start=time.time()
    for T in temperatures:
        for rho in densities:
            q,lo,hi=point(T,rho);lo*=1000;hi*=1000
            if q.kpa==2 and 0<lo<hi and lo<=rho<=hi:
                xv=np.clip((1/rho-1/hi)/(1/lo-1/hi),0,1)
                ag=np.clip(xv*rho/lo,0,1)
            elif T>=m.tct*TCONV or rho<=np.exp(np.interp(T,coexist[:,0],np.log(coexist[:,1]))):xv=ag=1.
            else:xv=ag=0.
            values=[q.p*.1,q.e*1e-4-eref,q.cv*1e-4/TCONV,q.cs*.01,float(xv),float(ag),float(q.kpa)]
            if not np.isfinite(values).all() or values[2]<=0 or values[0]<-1e3:bad.append([float(T),float(rho),*values])
            rows.append([np.log10(T),np.log10(rho),*values])
    data=np.asarray(rows)
    np.savez_compressed(out/'eos.npz',table=data,temperatures_K=temperatures,densities_kg_m3=densities)
    np.savetxt(out/'eos_comsol.txt',data,fmt='%.12e',header='log10(T/K) log10(rho/(kg/m3)) p_Pa e_J_kg cv_J_kg_K cs_m_s vapor_mass_fraction gas_volume_fraction phase_flag',comments='% ')
    audit={'material':'legacy ANEOS copper nuclear/cold contribution; no equilibrium electrons; NOTENSION equilibrium branch','source':'https://github.com/isale-code/M-ANEOS/blob/master/src/ANDATA.f','rho_initial_kg_m3':rho_ref,'e_reference_J_kg':eref,'critical_T_K':float(m.tct*TCONV),'normal_boiling_T_K':2515.07898309232,'reference_boiling_T_K':2836,'rejected_candidate':'Cu_ANEOS_material_calibration: at6500K and1000kg/m3 p=-143.6MPa; not used','shape':[len(temperatures),len(densities)],'invalid_states_count':len(bad),'invalid_states_first':bad[:20],'seconds':time.time()-start,'purpose':'mechanism comparison, not validated copper EOS or S4 fit'}
    (out/'audit.json').write_text(json.dumps(audit,indent=2),encoding='utf-8')
    print(json.dumps(audit,indent=2),flush=True)
if __name__=='__main__':main()
