"""Compare actual overlapping radiation states, without inventing a camera gate."""
from pathlib import Path
import sys,json,numpy as np
import matplotlib;matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
R=Path(__file__).resolve().parent
base,variant=sys.argv[1:3]
a=np.load(R/'results'/f'{base}_radiation/radiance.npz');b=np.load(R/'results'/f'{variant}_radiation/radiance.npz')
t=float(min(a['times_s'][-1],b['times_s'][-1]));x=a['x_um'];z=a['z_um'];px=float(a['pixel_um'])
assert np.array_equal(x,b['x_um']) and np.array_equal(z,b['z_um'])
def interp(d):
    ts=d['times_s'];assert ts[0]<=t<=ts[-1]
    hi=int(np.searchsorted(ts,t));hi=min(max(hi,1),len(ts)-1);w=(t-ts[hi-1])/(ts[hi]-ts[hi-1])
    return (1-w)*d['radiance'][hi-1]+w*d['radiance'][hi]
frames=[interp(a),interp(b)];peak=max(q.max() for q in frames)
fig,axes=plt.subplots(1,2,figsize=(10,5),layout='constrained');rows=[]
for ax,q,label in zip(axes,frames,[base,variant]):
    im=ax.imshow(np.maximum(q,peak*1e-8),origin='upper',extent=[x[0]-px/2,x[-1]+px/2,z[-1]-px/2,z[0]+px/2],norm=LogNorm(max(peak*1e-7,1e-30),peak),cmap='inferno')
    ax.set(xlim=(-80,80),ylim=(-10,150),xlabel='x [um]',ylabel='Physical z [um]',title=label)
    total=float(q.sum());rows.append(dict(case=label,peak_W_m2_sr=float(q.max()),image_integral_W_sr=float(total*(px*1e-6)**2),intensity_centroid_z_um=float((q.sum(1)*z).sum()/total) if total>0 else None))
fig.colorbar(im,ax=list(axes),label='Instantaneous radiance [W / m2 / sr]')
fig.suptitle(f'Overlapping saved states at model {t*1e9:.3f} ns\n475-525 nm; same physical color scale; NOT a complete 10 ns camera exposure\nComputed copper predictor; gas-to-copper feedback for thermal feed pending',fontsize=10)
out=R/'figures/surface_feed_comparison';out.mkdir(exist_ok=True)
fig.savefig(out/'instantaneous_same_time.png',dpi=170);plt.close(fig)
result=dict(model_time_ns=t*1e9,rows=rows,peak_ratio=rows[1]['peak_W_m2_sr']/rows[0]['peak_W_m2_sr'],integral_ratio=rows[1]['image_integral_W_sr']/rows[0]['image_integral_W_sr'],complete_exposure=False,camera_gain_used=False)
(R/'results/instantaneous_surface_feed_comparison.json').write_text(json.dumps(result,indent=2));print(json.dumps(result))
