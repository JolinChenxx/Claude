"""Compare physical gas light with a separately labelled condensed-light bound."""
from pathlib import Path
import sys,json
import numpy as np
import matplotlib;matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
R=Path(__file__).resolve().parent;case=sys.argv[1];old=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project';sys.path.insert(0,str(old/'work'))
from camera_s4 import gate,blur
a=np.load(R/'results'/f'{case}_radiation/radiance.npz');b=np.load(R/'results'/f'{case}_radiation/condensed_blackbody_upper_bound.npz');e=np.load(old/'experiment/S4_reconstructed.npz')
t=a['times_s']*1e9
if t.min()>85 or t.max()<95:raise ValueError('Incomplete first gate')
gas=blur(gate(t,a['radiance'],[90]),1)[0];bound=blur(gate(b['times_s']*1e9,b['radiance'],[90]),1)[0]
px=float(e['pixel_um']);x=a['x_um'];z=a['z_um'];extent=[x[0]-px/2,x[-1]+px/2,z[-1]-px/2,z[0]+px/2]
fig,ax=plt.subplots(1,3,figsize=(13.2,5.4),layout='constrained')
ax[0].imshow(e['gray'][0],origin='upper',extent=extent,vmin=0,vmax=255,cmap='inferno');ax[0].set_title('S4 experiment: 0 ns\nGray values 0-255')
norm=LogNorm(1e-5,max(1.,bound.max()));im=None
for k,(q,title) in enumerate([(gas,'Native gas emission'),(bound,'Condensed Cu blackbody UPPER BOUND')],1):
 im=ax[k].imshow(np.maximum(q,norm.vmin),origin='upper',extent=extent,norm=norm,cmap='inferno');ax[k].set_title(f'{title}\nPeak {q.max():.3g} W/m2/sr',fontsize=10)
for axis in ax:axis.set(xlabel='x [um]',ylabel='z [um]')
fig.colorbar(im,ax=list(ax[1:]),label='Physical radiance [W/m2/sr]; common scale')
fig.suptitle('85-95 ns saved-state exposure; 475-525 nm; model camera zero 90 ns\nUpper bound uses emissivity=1 and no foreground absorption. It is NOT added to gas light.',fontsize=11)
out=R/'figures'/case;out.mkdir(exist_ok=True);fig.savefig(out/'gas_vs_condensed_bound.png',dpi=155);plt.close(fig)
factor=2*np.pi*(1-np.sqrt(1-.4**2))*(px*1e-6)**2*10e-9*525e-9/(6.62607015e-34*299792458.)
report=dict(model_gate_ns=[85,95],gas_peak_W_m2_sr=float(gas.max()),gas_ideal_photons=float(gas.sum()*factor),condensed_upper_peak_W_m2_sr=float(bound.max()),condensed_upper_ideal_photons=float(bound.sum()*factor),condensed_bound_not_prediction=True)
(R/'results'/f'{case}_optical_components.json').write_text(json.dumps(report,indent=2));print(json.dumps(report))
