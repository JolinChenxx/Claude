"""Instantaneous physical radiation evolution; never substitutes incomplete camera gates."""
from pathlib import Path
import sys,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;cases=sys.argv[1:]
sys.path.insert(0,str(R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project/work'))
from camera_s4 import blur
fig,ax=plt.subplots(2,2,figsize=(11,7),layout='constrained');report=[]
for case in cases:
 a=np.load(R/'results'/f'{case}_radiation/radiance.npz');q=blur(a['radiance'],1);x=a['x_um'];z=a['z_um'];t=a['times_s']*1e9;dx=abs(np.diff(x).mean())*1e-6;dz=abs(np.diff(z).mean())*1e-6
 power=q.sum(axis=(1,2))*dx*dz;peak=q.max(axis=(1,2));centroid=[];radius=[];order=np.argsort(abs(x))
 photon_factor=(2*np.pi*(1-np.sqrt(1-.4**2)))*dx*dz*10e-9*525e-9/(6.62607015e-34*299792458.)
 for f in q:
  mask=f*photon_factor>=1
  if mask.sum()<3:radius.append(np.nan);centroid.append(np.nan);continue
  selected=f*mask
  radius.append(abs(x[order[np.searchsorted(np.cumsum(selected.sum(axis=0)[order]),.9*selected.sum())]]))
  centroid.append((selected.sum(axis=1)*z).sum()/selected.sum())
 radius=np.array(radius);centroid=np.array(centroid)
 for p,values in zip(ax.flat,[peak,power,radius,centroid]):p.plot(t,values,'.-',label=case,ms=4)
 report.append(dict(case=case,times_ns=t.tolist(),peak_radiance_W_m2_sr=peak.tolist(),image_integrated_power_W_sr=power.tolist(),radius90_um=radius.tolist(),emission_centroid_z_um=centroid.tolist()))
for p,title,unit in zip(ax.flat,['Peak instantaneous radiance','Image-integrated instantaneous radiance','90% radius within photon-qualified pixels','Centroid within photon-qualified pixels'],['W / m2 / sr','W / sr','um','um']):
 p.set(xlabel='Model time [ns]',ylabel=unit,title=title);p.legend(fontsize=8);p.grid(alpha=.2)
for p in ax[0]:p.set_yscale('log')
fig.suptitle('Actual instantaneous radiation; 475-525 nm; 1 pixel PSF; no fitted gain; NOT camera exposures\nShape uses >=1 ideal photon/pixel per constant 10 ns (NA=0.4, QE=throughput=1); omitted if <3 pixels.\nThis removes undetectable background from shape metrics; bulk emission >=0.1 um above copper.')
out=R/'figures'/'physical_comparison';out.mkdir(exist_ok=True);fig.savefig(out/'radiance_evolution.png',dpi=170)
(R/'results'/'radiance_evolution.json').write_text(json.dumps(dict(rows=report,instantaneous=True,camera_calibrated=False),indent=2),encoding='utf-8')
print('RADIANCE EVOLUTION EXPORTED',flush=True)
