"""Report integrated balances with explicit quadrature and validity limits."""
from pathlib import Path
import sys,json
import numpy as np
r=Path(__file__).resolve().parent;case=sys.argv[1]
h=np.loadtxt(r/'results'/f'{case}_history.csv',delimiter=',',skiprows=1)
e=np.loadtxt(r/'results'/f'{case}_electrode.csv',delimiter=',',skiprows=1)
b=np.loadtxt(r/'results'/f'{case}_balances.csv',delimiter=',',skiprows=1)
m=np.loadtxt(r/'results'/f'{case}_mesh_audit.csv',delimiter=',',skiprows=1)
t=h[:,0];integ=lambda v:float(np.trapz(v,t))
feed=integ(h[:,16]);gas=h[-1,8]-h[0,8];solid=h[-1,9]-h[0,9]
absorbed=integ(h[:,13]+h[:,14]);stored=float((h[-1,10:13]-h[0,10:13]).sum());radiated=integ(h[:,15]);work=integ(h[:,17]);outer=integ(b[:,3]+b[:,4]);q=e[:,1]+e[:,2]
bad=(m[:,1]<=0)|(m[:,2]<=0)|(m[:,3]<=0)
out={'case':case,'interval_ns':[float(t[0]*1e9),float(t[-1]*1e9)],'saved_mesh_times_checked':len(m),'saved_mesh_invalid_times_ns':(m[bad,0]*1e9).tolist(),'minimum_relative_volume':float(m[:,3].min()),'minimum_quality':float(m[:,2].min()),'Cu':{'surface_feed_kg':feed,'gas_change_kg':float(gas),'target_change_kg':float(solid),'gas_vs_feed_percent':float((gas-feed)/feed*100),'total_mass_drift_percent_of_feed':float((gas+solid)/feed*100)},'charge':{'initial_total_pC':float(q[0]*1e12),'final_total_pC':float(q[-1]*1e12),'drift_pC':float((q[-1]-q[0])*1e12),'final_residual_percent_of_metal_charge':float(q[-1]/abs(e[-1,1])*100),'note':'Direct gas+metal inventory. Thermionic emission and return nearly cancel; quadrature-sensitive wall-current trapezoids are not a conservation certificate.'},'energy':{'absorbed_J':absorbed,'stored_change_J':stored,'radiated_J':radiated,'work_function_J':work,'outer_heat_flux_J':outer,'residual_percent':(stored-absorbed+radiated+work+outer)/absorbed*100,'note':'Mechanical boundary work and dissipative partition not fully closed; no claim of final energy validation.'},'limits':['Only saved times checked for mesh validity, not every internal Newton iterate','No final mesh/time convergence verification','No independent electron density measurement']}
(r/'results'/f'{case}_verified_summary.json').write_text(json.dumps(out,indent=2));print(json.dumps(out,indent=2))

