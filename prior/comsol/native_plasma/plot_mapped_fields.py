"""Physical coordinates and actual copper contour for a saved gas checkpoint."""
from pathlib import Path
import json,sys,os
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
R=Path(__file__).resolve().parent;case=sys.argv[1]
d=json.loads((R/'results'/f'{case}_fields.json').read_text())
a=np.memmap(R/'results'/f'{case}_fields.bin',dtype='>f4',mode='r',shape=tuple(d['shape']))
t=np.array(d['times_s'])*1e9;r=np.array(d['r_um']);z=np.array(d['z_um']);i=len(t)-1
h=np.array(d['surface_height_um'])[i];x=np.r_[-r[:0:-1],r];hh=np.r_[h[:0:-1],h]
xx=np.broadcast_to(x,(len(z),len(x)));zz=z[:,None]+hh[None,:]
xe=np.r_[x[0]-(x[1]-x[0])/2,(x[1:]+x[:-1])/2,x[-1]+(x[-1]-x[-2])/2]
ze=np.r_[max(0,z[0]-(z[1]-z[0])/2),(z[1:]+z[:-1])/2,z[-1]+(z[-1]-z[-2])/2]
xedge=np.broadcast_to(xe,(len(ze),len(xe)));zedge=ze[:,None]+np.interp(xe,x,hh)[None,:]
fields=[('rhoCopper','Cu density [kg/m3]',True),('plas.ne','Electron density [1/m3]',True),('Tg','Gas temperature [K]',False),('TeK','Electron temperature [K]',False),('Qlaser','Laser absorption [W/m3]',True),('jline3','Cu I 510.55 nm emissivity [W/m3/sr]',True)]
out=R/'figures'/case;out.mkdir(exist_ok=True);audit={}
for zoom in [300,30]:
 fig,axs=plt.subplots(2,3,figsize=(12.5,8),layout='constrained')
 for ax,(name,title,log) in zip(axs.flat,fields):
  v=np.array(a[i,d['expressions'].index(name)],dtype=float);audit[name]={'nonfinite_samples':int((~np.isfinite(v)).sum()),'minimum':float(np.nanmin(v)),'maximum':float(np.nanmax(v))}
  v=np.ma.masked_invalid(np.c_[v[:,:0:-1],v]);top=max(float(v.max()),1e-30)
  norm=LogNorm(max(top*1e-8,1e-30),max(top,1e-29)) if log else None
  im=ax.pcolormesh(xedge,zedge,np.ma.maximum(v,top*1e-8) if log else v,shading='flat',cmap='inferno',norm=norm)
  ax.fill_between(x,-10,hh,color='#6d7076',zorder=3);ax.plot(x,hh,color='cyan',lw=.7,zorder=4)
  ax.set(xlim=(-zoom,zoom),ylim=(-5,zoom*1.35),xlabel='r [um]',ylabel='Physical z [um]',title=title);ax.set_aspect('equal');fig.colorbar(im,ax=ax,shrink=.83)
 fig.suptitle(f'{case}: actual saved model time {t[i]:g} ns; instantaneous fields, not a camera exposure\nCopper contour is computed; first partitioned prediction, gas-to-copper feedback not converged',fontsize=11)
 fig.savefig(out/f'physical_fields_{zoom}um.png',dpi=165);plt.close(fig)
(R/'results'/f'{case}_field_plot_audit.json').write_text(json.dumps({'model_time_ns':float(t[i]),'fields':audit},indent=2))
print('MAPPED PHYSICAL FIELDS',case,float(t[i]),flush=True)
