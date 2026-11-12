"""One complete S4 first gate: original spatial baseline and new material branch."""
from pathlib import Path
import sys,json,numpy as np
import matplotlib;matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
R=Path(__file__).resolve().parent;OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project';sys.path.insert(0,str(OLD/'work'))
from camera_s4 import gate,blur
case=sys.argv[1];names=['P221chainmapped',case+'native'];images=[];files=[]
for name in names:
    a=np.load(R/'results'/f'{name}_radiation/radiance.npz');times=a['times_s']*1e9
    if times.min()>85+1e-6 or times.max()<95-1e-6:raise ValueError('First10ns gate incomplete')
    images.append(blur(gate(times,a['radiance'],np.array([90.])),1)[0]);files.append(a)
e=np.load(OLD/'experiment/S4_reconstructed.npz');px=float(e['pixel_um']);a=files[-1]
extent=[a['x_um'][0]-px/2,a['x_um'][-1]+px/2,a['z_um'][-1]-px/2,a['z_um'][0]+px/2]
peak=max(x.max() for x in images);norm=LogNorm(max(peak*1e-8,1e-8),peak)
fig,ax=plt.subplots(1,3,figsize=(12,5.8))
fig.subplots_adjust(left=.06,right=.985,top=.79,bottom=.25,wspace=.30)
im0=ax[0].imshow(e['gray'][0],origin='upper',extent=extent,vmin=0,vmax=255,cmap='inferno');ax[0].set_title('S4: experiment0ns',fontsize=11)
fig.colorbar(im0,ax=ax[0],location='bottom',shrink=.85,label='Published gray level')
factor=2*np.pi*(1-np.sqrt(1-.4**2))*(px*1e-6)**2*10e-9*525e-9/(6.62607015e-34*299792458)
rows=[]
for axis,name,q in zip(ax[1:],names,images):
    im=axis.imshow(np.maximum(q,norm.vmin),origin='upper',extent=extent,norm=norm,cmap='inferno')
    axis.set_title(f'{name}\nPeak {q.max():.3g} W/m2/sr; {q.sum()*factor:.3g} photons',fontsize=10)
    rows.append(dict(case=name,peak_radiance=float(q.max()),ideal_collected_photons=float(q.sum()*factor)))
fig.colorbar(im,ax=list(ax[1:]),location='bottom',shrink=.85,label='Physical radiance [W/m2/sr]; same scale for both simulations')
for axis in ax:axis.set(xlabel='x [um]',ylabel='z [um]')
fig.suptitle('First complete exposure: model 85-95 ns, 475-525 nm\nEqual spatial scales; no camera gain fitted. Experimental copper plane is not independently registered.',fontsize=11,y=.98)
out=R/'figures'/case;out.mkdir(exist_ok=True);fig.savefig(out/'first_gate_baseline_comparison.png',dpi=180);plt.close(fig)
(R/'results'/case/'first_gate_baseline_comparison.json').write_text(json.dumps(dict(rows=rows,common_physical_range=[norm.vmin,norm.vmax]),indent=2))
