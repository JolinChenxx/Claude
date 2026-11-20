"""Physical fields of saved material/native-reaction split states."""
from pathlib import Path
import sys,json
import numpy as np
sys.path.insert(0,str(Path(__file__).resolve().parent))
from split_history import read_history
from split_saved_emission import visible_emissivity
import matplotlib;matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
R=Path(__file__).resolve().parent;case=sys.argv[1];p=R/'results'/case
g=np.load(p/'grid.npz');r=g['r_m']*1e6;z=g['z_m']*1e6;vol=g['cell_volume_m3']
h=read_history(case);times=np.array([a['saved_time_ns'] for a in h])
sel=np.unique([abs(times-t).argmin() for t in [90,110,130,150,170,times[-1]] if t<=times[-1]+1e-6])
re=g['radial_edges_m']*1e6;ze=g['axial_edges_m']*1e6;rex=np.r_[-re[:0:-1],re];rx=np.r_[-r[::-1],r]
fields=[];metrics=[]
for k in sel:
 d=np.load(h[k]['frame']);U,W,S=d['U'],d['W'],d['S'];ag=np.maximum(W[:,:,6],1e-30)
 cv=U[:,:,0]*W[:,:,5];cc=U[:,:,0]-cv;ne=S[:,:,18]/ag;Te=2*S[:,:,19]/(3*np.maximum(S[:,:,18],1e-100))*11604.51812
 j=visible_emissivity(S,W)
 fields.append((cv,ne,W[:,:,0],Te,j,cc))
 metrics.append(dict(model_ns=float(times[k]),vapor_mass_ng=float((cv*vol).sum()*1e12),visible_power_W=float((j*vol).sum()*4*np.pi),ne_max_gas_m3=float(ne[ag>1e-6].max()),vapor_centroid_z_um=float((cv*vol*z[None,:]).sum()/max((cv*vol).sum(),1e-100))))
spec=[('Gas-phase Cu [kg/m3]',LogNorm(1e-4,1e3),'magma'),('Gas electron density [1/m3]',LogNorm(1e6,1e28),'viridis'),('Gas/material temperature [K]',LogNorm(293,1e6),'inferno'),('Electron temperature [K]',LogNorm(293,1e6),'inferno'),('Saved-state emissivity [W/m3/sr]',LogNorm(1e-6,max(1e-5,max(f[4].max() for f in fields))),'magma')]
fig,axes=plt.subplots(len(sel),5,figsize=(17,3.4*len(sel)+1),layout='constrained',squeeze=False)
ax=axes.T
for col,(k,fs) in enumerate(zip(sel,fields)):
 for row,(label,norm,cmap) in enumerate(spec):
  value=fs[row];image=ax[row,col].pcolormesh(rex,ze,np.maximum(np.r_[value[::-1],value].T,norm.vmin),norm=norm,cmap=cmap,rasterized=True)
  ax[row,col].contour(rx,z,np.r_[fs[5][::-1],fs[5]].T,levels=[100],colors='cyan',linewidths=.6)
  ax[row,col].axhline(0,color='white',ls='--',lw=.6);ax[row,col].set(xlim=(-100,100),ylim=(-5,200),aspect='equal',xlabel='r [um]')
  if col==0:ax[row,col].set_title(label,fontsize=10)
  if row==0:ax[row,col].set_ylabel(f'Model {times[k]:g} ns\nz from initial Cu plane [um]')
  if col==len(sel)-1:fig.colorbar(image,ax=ax[row,:],shrink=.85,label=label)
fig.suptitle(f'{case}: saved material + native Plasma reaction states\nShared scale in each column; cyan: condensed Cu contour. No spatial electric drift, sheath or electron diffusion.',fontsize=11)
out=R/'figures'/case;out.mkdir(exist_ok=True);fig.savefig(out/'split_physical_fields.png',dpi=145);plt.close(fig)
(p/'physical_metrics.json').write_text(json.dumps(metrics,indent=2));print(json.dumps(metrics[-1]))
