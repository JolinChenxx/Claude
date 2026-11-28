"""Axial material audit only; never label these curves as a plasma/S4 result."""
from pathlib import Path
import sys,json
import numpy as np
import matplotlib;matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;case=sys.argv[1];p=R/'results'/case
d=np.load(p/'states.npz');t=d['times_s']*1e9;x=d['x_m']*1e6;dx=d['dx_m'];U=d['U'];W=d['W'];out=R/'figures'/case;out.mkdir(exist_ok=True)
cv=U[:,:,0]*W[:,:,5];cc=U[:,:,0]-cv
data=[]
for i,ti in enumerate(t):
    dense=cc[i]>100;vapor=cv[i]>1e-3
    data.append({'model_ns':float(ti),'max_dense_temperature_K':float(W[i,dense,0].max()) if dense.any() else None,
                 'max_copper_temperature_K':float(W[i,U[i,:,0]>1e-3,0].max()),'vapor_column_kg_m2':float((cv[i]*dx).sum()),
                 'copper_above_initial_surface_kg_m2':float((U[i,x>0,0]*dx[x>0]).sum()),
                 'condensed_copper_above_initial_surface_kg_m2':float((cc[i,x>0]*dx[x>0]).sum()),
                 'gas_copper_front_1e_3_kg_m3_um':float(x[vapor].max()) if vapor.any() else None,
                 'total_energy_J_m2':float((U[i,:,3]*dx).sum()),'max_pressure_Pa':float(W[i,:,1].max()),
                 'minimum_temperature_K':float(W[i,:,0].min())})
fig,ax=plt.subplots(2,3,figsize=(13,7.6),constrained_layout=True)
sel=np.unique([np.argmin(abs(t-v)) for v in [0,60,80,90,100,120,140,160,t[-1]] if v<=t[-1]+1e-6])
for i in sel:
    label=f'{t[i]:.0f} ns';ax[0,0].plot(x,W[i,:,0],label=label);ax[0,1].semilogy(x,np.maximum(cv[i],1e-12));ax[0,2].plot(x,W[i,:,3]);ax[1,0].plot(x,W[i,:,5])
for a,title in zip([ax[0,0],ax[0,1],ax[0,2],ax[1,0]],['Temperature [K]','Gas-phase copper density [kg/m3]','Axial velocity [m/s]','Copper vapor mass fraction']):
    a.set(xlim=(-5,300),xlabel='z relative to initial surface [um]',title=title);a.axvline(0,color='k',lw=.5,alpha=.5)
ax[0,0].legend(fontsize=7);ax[0,1].set_ylim(1e-6,1e4)
ax[1,1].plot(t,[q['vapor_column_kg_m2'] for q in data],label='All vapor Cu')
ax[1,1].plot(t,[q['copper_above_initial_surface_kg_m2'] for q in data],label='Total Cu at z>0')
ax[1,1].plot(t,[q['condensed_copper_above_initial_surface_kg_m2'] for q in data],label='Condensed Cu at z>0')
ax[1,1].set(xlabel='Model time [ns]',ylabel='Column mass [kg/m2]');ax[1,1].legend(fontsize=7)
ax[1,2].plot(t,[q['max_dense_temperature_K'] for q in data],label='Dense condensed Cu')
ax[1,2].plot(t,[q['max_copper_temperature_K'] for q in data],label='Cu > 0.001 kg/m3')
ax[1,2].axhline(9030,color='k',ls='--',label='EOS critical T');ax[1,2].set(xlabel='Model time [ns]',ylabel='Maximum temperature [K]');ax[1,2].legend(fontsize=7)
fig.suptitle(f'{case}: independent 1D finite-volume material check, NOT COMSOL Plasma\nCommon-pressure equilibrium Cu-air EOS; no camera brightness or radial-shape prediction')
fig.savefig(out/'axial_material_check.png',dpi=170);plt.close(fig)
(p/'material_metrics.json').write_text(json.dumps({'case':case,'native_plasma_solved':False,'time_series':data},indent=2))
print(json.dumps(data[-1]))
