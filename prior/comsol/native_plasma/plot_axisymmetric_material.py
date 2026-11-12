"""Actual saved 2D material fields. This is not a synthetic-light image."""
from pathlib import Path
import json,sys
import numpy as np
import matplotlib;matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
R=Path(__file__).resolve().parent;case=sys.argv[1];p=R/'results'/case
g=np.load(p/'grid.npz');r=g['r_m']*1e6;z=g['z_m']*1e6;v=g['cell_volume_m3']
h=json.loads((p/'history.json').read_text())
if h[0].get('source'):
 prior=json.loads((R/'results'/h[0]['source']/'history.json').read_text());h=[s for s in prior if s['saved_time_ns']<h[0]['saved_time_ns']-1e-6]+h
t=np.array([s['saved_time_ns'] for s in h]);targets=[80,90,100,110,120,t[-1]] if t[-1]<=125 else [90,110,130,150,170,t[-1]]
sel=np.unique([abs(t-a).argmin() for a in targets if a<=t[-1]+1e-6])
fig,ax=plt.subplots(2,len(sel),figsize=(3*len(sel),7.2),constrained_layout=True,squeeze=False)
rx=np.r_[-r[::-1],r];ze=g['axial_edges_m']*1e6;re=g['radial_edges_m']*1e6;rex=np.r_[-re[:0:-1],re]
metrics=[]
for j,k in enumerate(sel):
 d=np.load(h[k]['frame']);U=d['U'];W=d['W'];cv=U[:,:,0]*W[:,:,5];cc=U[:,:,0]-cv
 for row,(field,norm,cmap) in enumerate([(cv,LogNorm(1e-4,1e3),'magma'),(W[:,:,0],LogNorm(293,60000),'inferno')]):
  f=np.maximum(np.r_[field[::-1],field].T,norm.vmin);im=ax[row,j].pcolormesh(rex,ze,f,norm=norm,cmap=cmap,rasterized=True)
  if cc.max()>100:ax[row,j].contour(rx,z,np.r_[cc[::-1],cc].T,levels=[100],colors='cyan',linewidths=.7)
  ax[row,j].axhline(0,color='white',ls='--',lw=.5);ax[row,j].set(xlim=(-100,100),ylim=(-5,200),aspect='equal',xlabel='r [um]',title=f'{t[k]:.0f} ns model')
  if j==0:ax[row,j].set_ylabel('z above initial copper surface [um]')
  if j==len(sel)-1:fig.colorbar(im,ax=ax[row,:],shrink=.85,label='Gas-phase Cu [kg/m3]' if row==0 else 'Material temperature [K]')
 mask=cv>1e-3;above=z>0
 metrics.append({'model_ns':float(t[k]),'vapor_mass_kg':float((cv*v).sum()),'condensed_Cu_above_surface_kg':float((cc[:,above]*v[:,above]).sum()),'vapor_front_1e_3_kg_m3_um':float(np.where(mask,z[None,:],-1e6).max()),'vapor_radius_1e_3_kg_m3_um':float(np.where(mask,r[:,None],0).max()),'vapor_centroid_z_um':float((cv*v*z[None,:]).sum()/(cv*v).sum()),'max_condensed_temperature_K':float(W[:,:,0][cc>100].max())})
fig.suptitle(f'{case}: independent axisymmetric material predictor\nNot native Plasma or S4 light; cyan: condensed Cu 100 kg/m3; dashed: initial surface',fontsize=12)
out=R/'figures'/case;out.mkdir(exist_ok=True);fig.savefig(out/'material_evolution.png',dpi=150);plt.close(fig)
(p/'material_metrics.json').write_text(json.dumps({'case':case,'native_plasma_solved':False,'time_series':metrics},indent=2));print(json.dumps(metrics[-1]))
