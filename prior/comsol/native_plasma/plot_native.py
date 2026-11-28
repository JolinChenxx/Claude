"""Plot only saved native COMSOL solutions; no display gain for physical fields."""
from pathlib import Path
import sys,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
R=Path(__file__).resolve().parent
case=sys.argv[1];prefix=R/'results'/case
zoom=float(sys.argv[2]) if len(sys.argv)>2 else None
d=json.loads(Path(str(prefix)+'_fields.json').read_text())
a=np.memmap(str(prefix)+'_fields.bin',dtype='>f4',mode='r',shape=tuple(d['shape']))
t=np.array(d['times_s'])*1e9;r=np.array(d['r_um']);z=np.array(d['z_um']);ex=d['expressions']
out=R/'figures'/case;out.mkdir(exist_ok=True)
s=np.genfromtxt(str(prefix)+'_surface.csv',delimiter=',',names=True)
for target in np.unique(np.r_[np.linspace(t[0],t[-1],4),t[-1]]):
 i=int(abs(t-target).argmin());fig,axs=plt.subplots(2,3,figsize=(13,8),layout='constrained')
 for ax,(name,title,log,lo) in zip(axs.flat,[('rhoCopper','Cu mass density [kg/m3]',True,1e-8),('plas.ne','Electron density [1/m3]',True,1e8),('Tg','Gas temperature [K]',False,293),('TeK','Electron temperature [K]',False,293),('Qlaser','Laser absorption [W/m3]',True,1e4),('jline3','Cu I 510.55 nm emissivity [W/m3/sr]',True,1e-4)]):
  v=np.array(a[i,ex.index(name)],dtype=float);v=np.ma.masked_invalid(v)
  if log:
   vmax=max(float(v.max()),lo*10);norm=LogNorm(lo,vmax);v=np.ma.maximum(v,lo)
   im=ax.pcolormesh(r,z,v,norm=norm,cmap='inferno',shading='auto')
  else:im=ax.pcolormesh(r,z,v,vmin=min(lo,float(v.min())),vmax=max(lo+1,float(v.max())),cmap='inferno',shading='auto')
  surf=s[np.isclose(s['time_s'],t[i]*1e-9,atol=1e-15,rtol=0)];order=np.argsort(surf['r_m']);ax.plot(surf['r_m'][order]*1e6,surf['z_m'][order]*1e6,'c-',lw=1)
  ax.set(xlim=(0,300),ylim=(-10,500),xlabel='r [um]',ylabel='z [um]',title=title);fig.colorbar(im,ax=ax)
  if zoom is not None:ax.set(xlim=(0,zoom),ylim=(-1,zoom))
 fig.suptitle(f'{case}: numerical time {t[i]:.1f} ns (experiment offset: -118 ns)')
 fig.savefig(out/(f'fields_{t[i]:07.2f}ns'+('_zoom' if zoom is not None else '')+'.png'),dpi=160);plt.close(fig)
h=np.genfromtxt(str(prefix)+'_history.csv',delimiter=',',names=True,deletechars='')
# Load numeric matrix separately because COMSOL expressions contain parentheses.
h=np.loadtxt(str(prefix)+'_history.csv',delimiter=',',skiprows=1,ndmin=2)
fig,axs=plt.subplots(2,2,figsize=(10,7),layout='constrained')
axs[0,0].plot(h[:,0]*1e9,h[:,1],label='surface');axs[0,0].plot(h[:,0]*1e9,h[:,4],label='gas');axs[0,0].plot(h[:,0]*1e9,h[:,5],label='electrons');axs[0,0].set(ylabel='Peak temperature [K]');axs[0,0].legend()
axs[0,1].semilogy(h[:,0]*1e9,h[:,3]);axs[0,1].set(ylabel='Peak electron density [1/m3]')
axs[1,0].plot(h[:,0]*1e9,h[:,2]*1e6);axs[1,0].set(ylabel='Maximum surface height [um]')
axs[1,1].plot(h[:,0]*1e9,h[:,8]*1e12);axs[1,1].set(ylabel='Gas-phase Cu mass [ng]')
for ax in axs.flat:ax.set(xlabel='Numerical time [ns]');ax.grid(alpha=.2)
fig.savefig(out/'histories.png',dpi=160)
print(out)
