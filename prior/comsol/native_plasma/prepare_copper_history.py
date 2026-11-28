"""Transfer saved copper motion to gas; no paper-image trajectory or extrapolation."""
from pathlib import Path
import sys,json
import numpy as np
R=Path(__file__).resolve().parent
case=sys.argv[1]
data=np.genfromtxt(R/'results'/f'{case}_surface.csv',delimiter=',',names=True)
times=np.unique(data['time_s'])
evap_name='evap_kg_m2_s' if 'evap_kg_m2_s' in data.dtype.names else 'mdot_kg_m2_s'
if evap_name=='mdot_kg_m2_s' and (R/'results'/f'{case}_coupling_iteration.json').exists():
    raise ValueError('Feedback copper must export evaporation separately; net flux would double subtract returning Cu')
if not all(np.isfinite(data[n]).all() for n in ['r_m','z_m','T_K','ur_m_s','uz_m_s','mdot_kg_m2_s']):
    raise ValueError('Nonfinite copper history must be trimmed at actual valid saved states before transfer')
radial=np.unique(np.r_[np.arange(0,60.001,.25),np.arange(62,302,2),np.arange(310,1001,10)])
rows=[]
for t in times:
    d=data[data['time_s']==t]
    rr,idx=np.unique(d['r_m'],return_index=True);d=d[idx]
    vals=[np.interp(radial,rr*1e6,d[n])*scale for n,scale in [('z_m',1e6),('T_K',1),('ur_m_s',1),('uz_m_s',1),(evap_name,1)]]
    rows.append(np.column_stack([radial,np.full(len(radial),t*1e9),*vals]))
out=R/'data'/f'{case}_copper_history.txt'
np.savetxt(out,np.vstack(rows),fmt='%.14e')
(R/'results'/f'{case}_transfer.json').write_text(json.dumps(dict(source_case=case,saved_times_ns=(times*1e9).tolist(),end_ns=float(times[-1]*1e9),radial_points=len(radial),functions=['height_um','temperature_K','ur_m_s','uz_m_s','evap_kg_m2_s'],interpolation='linear in material radius/time; no extrapolation of solve interval',source='actual COMSOL copper solution',evaporation_source_column=evap_name,table=str(out)),indent=2))
print(out, 'END_NS',times[-1]*1e9,flush=True)
