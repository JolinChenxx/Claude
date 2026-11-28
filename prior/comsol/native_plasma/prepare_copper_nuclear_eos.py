"""Material-only copper ANEOS audit, with equilibrium electronic terms disabled.

This is not an adopted production material. It isolates cold/nuclear energy so
that any later coupling cannot double count native Plasma electron/chemical
energy. No S4 data enter this computation.
"""
from pathlib import Path
import json,sys,pickle,time
import numpy as np
R=Path(__file__).resolve().parent
sys.path.insert(0,str(R/'vendor/pyaneos_reference'))
from pyaneos.input import AneosInput
from pyaneos.setup import setup_material
from pyaneos.phase import aneosv_point
from pyaneos import TCONV

def main():
    src=json.loads((R/'results/legacy_Cu_ANEOS_diagnostic/summary.json').read_text())
    ui=np.array(src['modern_ui48_cgs_K']);ui[1]=3
    start=time.time();print('NUCLEAR_CU_SETUP',flush=True)
    m=setup_material(AneosInput(neos=1,title='Legacy copper, nuclear-only audit, not adopted',ui=ui,zzs=np.array([29.]),cot=np.array([1.])))
    out=R/'results/Cu_nuclear_ANEOS_audit';out.mkdir(exist_ok=True)
    with (out/'material.pkl').open('wb') as f:pickle.dump(m,f)
    rows=[]
    for t,rl,rv in zip(m.ttwo,m.rsol,m.rvap):
        if t<=0 or rl<=0 or rv<=0 or abs(t-m.tct)<1e-10:continue
        a,_,_=aneosv_point(float(t),float(rl)*(1+1e-7),m)
        b,_,_=aneosv_point(float(t),float(rv)*(1-1e-7),m)
        rows.append([t*TCONV,rl*1000,rv*1000,b.p*.1,a.e*1e-4,b.e*1e-4,(a.e+a.p/rl)*1e-4,(b.e+b.p/rv)*1e-4])
    data=np.array(rows)[::-1]
    np.savetxt(out/'coexistence.csv',data,delimiter=',',header='T_K,rho_l_kg_m3,rho_v_kg_m3,p_Pa,e_l_J_kg,e_v_J_kg,h_l_J_kg,h_v_J_kg',comments='')
    tb=float(np.interp(np.log(101325),np.log(data[:,3]),data[:,0]))
    report={'EOS_type':3,'equilibrium_electronics':False,'critical_T_K':float(m.tct*TCONV),'normal_boiling_T_K':tb,'source_legacy_parameters':src['source'],'adopted_in_COMSOL':False,'seconds':time.time()-start,'limitation':'Legacy obsolete copper parameters; independent material validation/calibration still required; no S4 fitting'}
    (out/'summary.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2),flush=True)
if __name__=='__main__':main()
