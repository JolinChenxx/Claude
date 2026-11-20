"""One fixed linear radiance display; identical coordinates and no image fitting.

This auxiliary view avoids saturated size-biased camera fits and log displays
that conceal a physical brightness decline. It does not replace calibrated
metrics, photon counts or the explicitly stated uniform geometry limitation.
"""
from pathlib import Path
import sys,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
def main(case):
 a=np.load(R/'results'/f'{case}_global_images.npz');e=np.load(OLD/'experiment/S4_reconstructed.npz')
 t=a['experiment_times_ns'];p=a['photons_upper_525nm'];rad=a['radiance'];early=t<=80
 vmax=float(np.max(rad[early]));px=float(e['pixel_um']);x=a['x_um'];z=a['z_um']
 pick=np.arange(len(t)) if len(t)<=8 else np.unique([np.argmin(abs(t-v)) for v in [0,20,60,150,260,400,580,740]])
 fig,axes=plt.subplots(3,len(pick),figsize=(2.2*len(pick),8.5),layout='constrained',squeeze=False)
 extent=[x[0]-px/2,x[-1]+px/2,z[-1]-px/2,z[0]+px/2]
 maxlit=0
 for im in rad: 
  mask=im>.02*vmax
  if mask.any():maxlit=max(maxlit,float(z[np.where(mask)[0]].max()),float(abs(x[np.where(mask)[1]]).max()))
 zoom=max(60.,min(400.,np.ceil(maxlit/20)*20))
 for col,k in enumerate(pick):
  ei=np.flatnonzero(e['times_ns']==t[k])[0]
  axes[0,col].imshow(e['gray'][ei],vmin=0,vmax=255,cmap='turbo',extent=extent)
  for row in [1,2]:
   artist=axes[row,col].imshow(rad[k],vmin=0,vmax=vmax,cmap='turbo',extent=extent)
   axes[row,col].axhline(0,c='white',lw=.6)
  axes[0,col].set_title(f'{t[k]:g} ns');axes[1,col].text(.02,.95,f'{p[k].sum():.2g} photons',va='top',color='white',fontsize=7,transform=axes[1,col].transAxes)
  axes[2,col].set_xlim(-zoom,zoom);axes[2,col].set_ylim(0,2*zoom)
  axes[2,col].set_xticks([-zoom,0,zoom]);axes[2,col].tick_params(labelsize=7)
  axes[2,col].set_xlabel('x (um)',fontsize=8)
 for row in [0,1]:
  for ax in axes[row]:ax.set_xticks([]);ax.set_yticks([])
 for ax in axes[2,1:]:ax.set_yticks([])
 axes[0,0].set_ylabel('S4 experiment');axes[1,0].set_ylabel('Model: S4 length scale');axes[2,0].set_ylabel('Model zoom: height (um)')
 fig.colorbar(artist,ax=axes[1:].ravel().tolist(),label='Physical radiance [W m^-2 sr^-1]; fixed linear display',fraction=.02)
 fig.suptitle(f'{case}: native global Plasma, assumed uniform half-ellipsoid\nOne linear scale from early radiance maximum; no framewise gain. Geometry does not predict an elevated bright core.',fontsize=11)
 out=R/'figures'/case;out.mkdir(exist_ok=True);fig.savefig(out/'S4_physical_linear_trend.png',dpi=160);plt.close(fig)
 report=dict(case=case,auxiliary_diagnostic_only=True,linear_vmax_W_m2_sr=vmax,normalization='single maximum physical radiance in complete0-80ns gates, fixed for every frame',zoom_um=zoom,identical_scale_rows=[0,1],zoom_row=2,pixel_peak_time_ns=float(t[np.argmax(p.max((1,2)))]),total_photon_peak_time_ns=float(t[np.argmax(p.sum((1,2)))]))
 (R/'results'/f'{case}_linear_trend_display.json').write_text(json.dumps(report,indent=2))
 print(json.dumps(report))
if __name__=='__main__':main(sys.argv[1])
