"""All fully covered S4 gates, shared physical scale without camera calibration."""
from pathlib import Path
import sys,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
R=Path(__file__).resolve().parent;old=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
sys.path.insert(0,str(old/'work'))
from camera_s4 import gate,blur
case=sys.argv[1];zero=90.
exp=np.load(old/'experiment/S4_reconstructed.npz');a=np.load(R/'results'/f'{case}_radiation/radiance.npz')
t=a['times_s']*1e9;et=exp['times_ns'];use=(et+zero-5>=t.min()-1e-6)&(et+zero+5<=t.max()+1e-6)
et=et[use]
if not len(et):
 (R/'results'/f'{case}_gate_sequence.json').write_text(json.dumps(dict(case=case,rows=[],saved_radiance_range_ns=[float(t.min()),float(t.max())],reason='No complete S4 exposure; no extrapolation or partial gate rendered',no_camera_calibration=True),indent=2))
 print('NO COMPLETE S4 GATE',case,flush=True);sys.exit(0)
frames=blur(gate(t,a['radiance'],et+zero),1);px=float(exp['pixel_um']);x=a['x_um'];z=a['z_um']
rows=[]
for k,q in enumerate(frames):
 photons=q*(2*np.pi*(1-np.sqrt(1-.4**2)))*(px*1e-6)**2*10e-9*525e-9/(6.62607015e-34*299792458.)
 rows.append(dict(experiment_time_ns=float(et[k]),model_center_ns=float(et[k]+zero),peak_radiance_W_m2_sr=float(q.max()),ideal_photons=float(photons.sum()),pixels_ge_one_photon=int((photons>=1).sum())))
 selected=q*(photons>=1);order=np.argsort(abs(x))
 if (photons>=1).sum()>=3:
  rows[-1].update(photon_qualified_radius90_um=float(abs(x[order[np.searchsorted(np.cumsum(selected.sum(axis=0)[order]),.9*selected.sum())]])),photon_qualified_centroid_z_um=float((selected.sum(axis=1)*z).sum()/selected.sum()))
peak=float(frames.max());extent=[x[0]-px/2,x[-1]+px/2,z[-1]-px/2,z[0]+px/2]
out=R/'figures'/case;out.mkdir(exist_ok=True)
def draw(indices,filename):
 n=len(indices);fig,ax=plt.subplots(2,n,figsize=(max(9,4*n+1),8.5),squeeze=False,layout='constrained')
 for col,k in enumerate(indices):
  q=frames[k];time=et[k]
  ax[0,col].imshow(exp['gray'][np.flatnonzero(use)[k]],origin='upper',extent=extent,vmin=0,vmax=255,cmap='inferno')
  im=ax[1,col].imshow(np.maximum(q,peak*1e-8),origin='upper',extent=extent,norm=LogNorm(max(peak*1e-6,1e-30),max(peak,1e-29)),cmap='inferno')
  ax[0,col].set_title(f'S4: {time:g} ns');ax[1,col].set_title(f"{case}: {time:g} ns\nPeak {q.max():.3g} W/m2/sr",fontsize=11)
  for p in ax[:,col]:p.set(xlabel='x [um]',ylabel='z [um]')
 fig.colorbar(im,ax=list(ax[1]),label='Radiance [W/m2/sr]; shared scale')
 fig.suptitle('Complete 10 ns exposures; 475-525 nm; camera zero assumed at model 90 ns\nTop: experimental gray levels. Bottom: physical radiance; no fitted camera gain.\nEqual spatial scales. The experimental copper plane is not independently registered.',fontsize=10)
 fig.savefig(out/filename,dpi=160);plt.close(fig)
chosen=np.unique(np.linspace(0,len(et)-1,min(4,len(et))).astype(int))
draw(chosen,'S4_complete_gates_physical.png')
if len(et)>4:
 for start in range(0,len(et),4):draw(np.arange(start,min(start+4,len(et))),f'S4_complete_gates_page{start//4+1}.png')
(R/'results'/f'{case}_gate_sequence.json').write_text(json.dumps(dict(case=case,rows=rows,shared_display_vmax_W_m2_sr=peak,no_camera_calibration=True,bulk_emission_min_surface_distance_um=.1),indent=2),encoding='utf-8')
print('COMPLETE S4 GATES',et.tolist(),flush=True)
