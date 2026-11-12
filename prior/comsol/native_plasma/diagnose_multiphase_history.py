"""Energy, hot-layer and material-front diagnostics from saved finite volumes."""
from pathlib import Path
import json,sys
import numpy as np
import matplotlib;matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;cases=sys.argv[1:];records=[]
for case in cases:
 p=R/'results'/case;g=np.load(p/'grid.npz');z=g['z_m'];r=g['r_m'];v=g['cell_volume_m3'];dz=np.diff(g['axial_edges_m'])
 for s in json.loads((p/'history.json').read_text()):
  d=np.load(s['frame']);U=d['U'];W=d['W'];cv=U[:,:,0]*W[:,:,5];cc=U[:,:,0]-cv;hot=(W[:,:,0]>.9*9030.317)&(cc>100)
  nhot=hot[0];thick=dz[nhot].sum();cs=W[0,nhot,2].mean() if nhot.any() else None
  records.append({'model_ns':float(d['time_s']*1e9),'case':case,'vapor_ng':float((cv*v).sum()*1e12),'above_total_ng':float((U[:,z>0,0]*v[:,z>0]).sum()*1e12),'above_condensed_ng':float((cc[:,z>0]*v[:,z>0]).sum()*1e12),'hot_condensed_ng':float((cc[hot]*v[hot]).sum()*1e12),'hot_axial_thickness_um':float(thick*1e6),'mechanical_release_ns':float(thick/cs*1e9) if cs else None,'max_dense_T_K':float(W[:,:,0][cc>100].max()),'max_T_K':float(W[:,:,0].max()),'gas_front_um':float(np.where(cv>1e-3,z[None,:],-1).max()*1e6),'gas_radius_um':float(np.where(cv>1e-3,r[:,None],0).max()*1e6),'vapor_centroid_um':float((cv*v*z[None,:]).sum()/max((cv*v).sum(),1e-100)*1e6)})
for s in records:
 if s['gas_front_um']<=-999999:s['gas_front_um']=None;s['gas_radius_um']=None
 if s['vapor_ng']<1e-9:s['vapor_centroid_um']=None
bytime={round(s['model_ns'],6):s for s in records};records=[bytime[t] for t in sorted(bytime)];t=np.array([s['model_ns'] for s in records]);out=R/'figures/multiphase_20261110';out.mkdir(exist_ok=True)
fig,ax=plt.subplots(2,2,figsize=(11.5,7),layout='constrained')
for key,label in [('vapor_ng','All vapor Cu'),('above_total_ng','All Cu at z>0'),('above_condensed_ng','Condensed Cu at z>0')]:ax[0,0].plot(t,[s[key] for s in records],label=label)
ax[0,0].set(ylabel='Mass [ng]');ax[0,0].legend()
for key,label in [('gas_radius_um','Gas Cu radial extent'),('gas_front_um','Gas Cu upper extent'),('vapor_centroid_um','Gas Cu mass centroid')]:ax[0,1].plot(t,[s[key] for s in records],label=label)
ax[0,1].set(ylabel='Distance [um]');ax[0,1].legend(fontsize=8)
for key,label in [('max_dense_T_K','Condensed Cu >100 kg/m3'),('max_T_K','All material')]:ax[1,0].plot(t,[s[key] for s in records],label=label)
ax[1,0].axhline(9030.317,color='k',ls='--',label='EOS critical T');ax[1,0].set(ylabel='Peak temperature [K]');ax[1,0].legend(fontsize=8)
ax[1,1].plot(t,[s['hot_condensed_ng'] for s in records],label='Condensed Cu at T>0.9 Tc');ax[1,1].set(ylabel='Hot condensed mass [ng]');ax[1,1].legend(fontsize=8)
for a in ax.flat:a.set(xlabel='Model time [ns]');a.axvline(90,color='gray',ls=':',lw=1);a.grid(alpha=.2)
fig.suptitle('Saved material evolution: no camera-brightness prediction\nAbove-surface condensed mass includes expansion; it is not a measured phase-explosion yield')
fig.savefig(out/'material_diagnostics.png',dpi=170);plt.close(fig)
hot=[s for s in records if s['hot_condensed_ng']>1e-6]
summary={'cases':cases,'native_plasma_solved':False,'hot_window_ns':[hot[0]['model_ns'],hot[-1]['model_ns']] if hot else None,'peak_vapor':max(records,key=lambda s:s['vapor_ng']),'peak_hot_condensed':max(records,key=lambda s:s['hot_condensed_ng']),'time_series':records,'nucleation_status':'Equilibrium EOS has no metastable path. A nucleation time cannot be inferred from these states without a metastable EOS and surface-tension data.'}
(R/'results/multiphase_material_diagnosis.json').write_text(json.dumps(summary,indent=2));print(json.dumps({k:v for k,v in summary.items() if k!='time_series'}))
