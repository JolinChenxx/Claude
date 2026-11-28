"""Independent material calibration trial; never fits any plume image.

Candidate ANEOS parameters are constrained by Tb=2836 K and the Demin2019
critical estimate (6550 K, 0.16 GPa). Report independent latent heat/density
checks before deciding whether any candidate can be used in a coupled model.
"""
from pathlib import Path
import sys,json,pickle,time
import numpy as np
from scipy.optimize import least_squares
R=Path(__file__).resolve().parent
sys.path.insert(0,str(R/'vendor/pyaneos_reference'))
from pyaneos.input import AneosInput
from pyaneos.setup import setup_material
from pyaneos.phase import aneosv_point
from pyaneos import TCONV
out=R/'results/Cu_ANEOS_material_calibration';out.mkdir(exist_ok=True)
src=json.loads((R/'results/legacy_Cu_ANEOS_diagnostic/summary.json').read_text())
base=np.array(src['modern_ui48_cgs_K']);base[1]=3;base[11]=1358.;base[22]=204000.*1e4;base[23]=-8.0
records=[]
def evaluate(x):
    ui=base.copy();ui[10]=x[0]*1e10;ui[12]=x[1]*1e12;ui[13]=x[2]
    m=setup_material(AneosInput(neos=1,title='Cu material calibration trial, nuclear only',ui=ui,zzs=np.array([29.]),cot=np.array([1.])))
    rows=[]
    for t,rl,rv in zip(m.ttwo,m.rsol,m.rvap):
        if t<=0 or rl<=0 or rv<=0 or abs(t-m.tct)<1e-10:continue
        al,_,_=aneosv_point(float(t),float(rl)*(1+1e-7),m)
        av,_,_=aneosv_point(float(t),float(rv)*(1-1e-7),m)
        rows.append([t*TCONV,rl*1000,rv*1000,av.p*.1,(al.e+al.p/rl)*1e-4,(av.e+av.p/rv)*1e-4])
    a=np.array(rows);a=a[np.argsort(a[:,0])]
    if len(a)<10 or np.any(a[:,3]<=0):raise ValueError('Invalid coexistence table')
    tb=np.interp(np.log(101325),np.log(a[:,3]),a[:,0]);pc=aneosv_point(float(m.tct),float(m.rsol[0]),m)[0].p*.1
    return np.array([tb,m.tct*TCONV,pc]),m,a,ui
def fun(x):
    try:
        vals,_,_,_=evaluate(x)
        res=np.log(vals/np.array([2836.,6550.,160e6]))
        records.append(dict(x=x.tolist(),values=vals.tolist(),residual=res.tolist()))
        print(json.dumps(records[-1]),flush=True)
        return res
    except Exception as e:
        print('FAILED_MATERIAL',x,str(e),flush=True)
        return np.array([10.,10.,10.])
fit=least_squares(fun,[5.8,6.,.7],bounds=([4.,-15.,.3],[8.,25.,.95]),diff_step=1e-3,max_nfev=35,ftol=1e-5,xtol=1e-5,gtol=1e-5)
vals,m,a,ui=evaluate(fit.x)
with (out/'candidate.pkl').open('wb') as f:pickle.dump(m,f)
np.savetxt(out/'coexistence.csv',a,delimiter=',',header='T_K,rho_l_kg_m3,rho_v_kg_m3,p_Pa,h_l_J_kg,h_v_J_kg',comments='')
report=dict(adopted_in_COMSOL=False,fitted_only_to_independent_material_targets=True,targets=dict(Tb_K=2836,Tc_K=6550,Pc_Pa=160e6),actual=vals.tolist(),parameters_ui48=ui.tolist(),fit_success=bool(fit.success),fit_residual=fit.fun.tolist(),latent_heat_at_Tb_J_kg=float(np.interp(2836,a[:,0],a[:,5]-a[:,4])),liquid_density_at_Tm_kg_m3=float(np.interp(1358,a[:,0],a[:,1])),critical_density_kg_m3=float(m.rsol[0]*1000),source='Legacy ANEOS functional form; Tb and latent reference inherited documented Cu data; critical estimate Demin2019; these critical parameters are not direct experimental measurements',limitations=['No full independently validated copper EOS','Material fit is not plasma validation','Supercritical interface treatment must be specified before using in COMSOL'],evaluations=records)
(out/'summary.json').write_text(json.dumps(report,indent=2));print('FINAL',json.dumps({k:v for k,v in report.items() if k!='evaluations'}),flush=True)
