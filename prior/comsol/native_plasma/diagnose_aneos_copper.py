"""Evaluate the published legacy Cu ANEOS parameters; diagnostic, not adopted.

Parameters come from M-ANEOS ANDATA.f, library entry 9, which upstream
explicitly labels obsolete. Its old Debye temperature is in eV; the modern
input parser requires kelvin. No parameters are fitted to S4.
"""
from pathlib import Path
import sys,json,csv,time
import numpy as np
R=Path(__file__).resolve().parent
sys.path.insert(0,str(R/'vendor/pyaneos_reference'))
from pyaneos.input import AneosInput
from pyaneos.setup import setup_material
from pyaneos.phase import aneosv_point
from pyaneos import TCONV
ui=np.zeros(48)
ui[:24]=np.array([1,4,8.94,0,0,-3.94e5,1.99,.0271,1.489,2,5.25e10,-4.637e9,6e12,.7,4.4e11,*([0]*7),2.055e9,-8.217],dtype=np.float32).astype(float)
ui[7]*=1.160564e4
inp=AneosInput(neos=1,title='Legacy Cu ANDATA library 9; diagnostic only',ui=ui,zzs=np.array([29.]),cot=np.array([1.]))
print('BUILDING_LEGACY_CU',flush=True);started=time.time();m=setup_material(inp)
print('BUILT',time.time()-started,'Tc_K',m.tct*TCONV,'dome_points',len(m.ttwo),flush=True)
out=R/'results/legacy_Cu_ANEOS_diagnostic';out.mkdir(exist_ok=True)
rows=[]
np.savez_compressed(out/'dome_state.npz',T_eV=m.ttwo,rsol_gcc=m.rsol,rvap_gcc=m.rvap,ack=m.ack,Tc_eV=m.tct)
print('DOME_START',m.ttwo[:3],m.rsol[:3],m.rvap[:3],flush=True)
for T,rhol,rhov in zip(m.ttwo,m.rsol,m.rvap):
    if T<=0 or rhol<=0 or rhov<=0:continue
    if abs(T-m.tct)<1e-10:continue
    rl,_,_=aneosv_point(float(T),float(rhol)*(1+1e-7),m)
    rv,_,_=aneosv_point(float(T),float(rhov)*(1-1e-7),m)
    rows.append({'T_K':T*TCONV,'rho_liquid_kg_m3':rhol*1000,'rho_vapor_kg_m3':rhov*1000,'p_liquid_Pa':rl.p*.1,'p_vapor_Pa':rv.p*.1,'e_liquid_J_kg':rl.e*1e-4,'e_vapor_J_kg':rv.e*1e-4,'h_liquid_J_kg':(rl.e+rl.p/rhol)*1e-4,'h_vapor_J_kg':(rv.e+rv.p/rhov)*1e-4})
with (out/'coexistence.csv').open('w',newline='') as f:w=csv.DictWriter(f,fieldnames=rows[0]);w.writeheader();w.writerows(rows)
report={'source':'https://github.com/isale-code/M-ANEOS/blob/master/src/ANDATA.f','library_entry':9,'upstream_label':'COPPER/melt [obsolete]','adopted_in_COMSOL':False,'critical_temperature_K':m.tct*TCONV,'critical_density_kg_m3':float(m.rsol[0])*1000,'highest_subcritical_pressure_Pa':rows[0]['p_liquid_Pa'],'coexistence_points':len(rows),'modern_ui48_cgs_K':ui.tolist(),'legacy_unit_conversion':'V8 Debye temperature: 0.0271 eV converted to K for modern ANEOS input','limitations':['Legacy obsolete parameter set, not a validated production Cu EOS','Includes equilibrium electronic contribution; cannot add to native Plasma chemical energy without separating terms','Reference diagnostic only; no S4 fitting']}
(out/'summary.json').write_text(json.dumps(report,indent=2),encoding='utf8');print(json.dumps(report,indent=2),flush=True)
