"""Integral balances on the actually saved time interval, with explicit limits."""
from pathlib import Path
import sys,json
import numpy as np
from scipy.integrate import cumulative_trapezoid
R=Path(__file__).resolve().parent;case=sys.argv[1]
a=np.loadtxt(R/'results'/f'{case}_history.csv',delimiter=',',skiprows=1,ndmin=2);t=a[:,0]
integ=lambda x:cumulative_trapezoid(x,t,initial=0)
feed=integ(a[:,16]);gasCu=a[:,8]-a[0,8];targetCu=a[:,9]-a[0,9]
energy=a[:,10]+a[:,11]+a[:,12];absorbed=integ(a[:,13]+a[:,14]);radiated=integ(a[:,15]);work=integ(a[:,17]);estimated_net=absorbed-radiated-work
qwall=integ(a[:,20]);qgas=a[:,19]-a[0,19]
out={'case':case,'last_saved_ns':float(t[-1]*1e9),'surface_peak_K':float(a[:,1].max()),'electron_peak_m3':float(a[:,3].max()),'gas_temperature_range_K':[float(a[:,6].min()),float(a[:,4].max())],
'mass':{'integrated_surface_feed_kg':float(feed[-1]),'gas_Cu_change_kg':float(gasCu[-1]),'target_Cu_change_kg':float(targetCu[-1]),'gas_minus_feed_kg':float(gasCu[-1]-feed[-1]),'total_Cu_change_kg':float(gasCu[-1]+targetCu[-1])},
'energy':{'absorbed_J':float(absorbed[-1]),'radiated_J':float(radiated[-1]),'work_function_J':float(work[-1]),'stored_change_J':float(energy[-1]-energy[0]),'residual_before_outer_and_mechanical_flux_J':float(energy[-1]-energy[0]-estimated_net[-1])},
'charge':{'integrated_wall_current_C':float(qwall[-1]),'gas_charge_change_C':float(qgas[-1]),'sum_before_outer_current_C':float(qwall[-1]+qgas[-1]),'saved_qmetal_C':float(a[-1,18])},
'limits':['Surface integrals corrected to axisymmetric 2*pi*r in exporter; pre-P18 saved qmetal used wrong integral measure.','Coarse output-time trapezoidal quadrature; far-boundary flux and mechanical boundary work not yet subtracted.','These residuals diagnose missing terms; they do not certify conservation.']}
(R/'results'/f'{case}_audit.json').write_text(json.dumps(out,indent=2));print(json.dumps(out,indent=2))
