"""Small animation of saved physical fields; fixed color scales across time."""
from pathlib import Path
import sys,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
from matplotlib.animation import FuncAnimation,PillowWriter
r=Path(__file__).resolve().parent;case=sys.argv[1];d=json.loads((r/'results'/f'{case}_fields.json').read_text());a=np.memmap(r/'results'/f'{case}_fields.bin',dtype='>f4',mode='r',shape=tuple(d['shape']));x=np.array(d['r_um']);z=np.array(d['z_um']);t=np.array(d['times_s']);ex=d['expressions'];s=np.genfromtxt(r/'results'/f'{case}_surface.csv',delimiter=',',names=True);ids=np.unique(np.linspace(0,len(t)-1,18).astype(int))
fig,axs=plt.subplots(1,3,figsize=(12,4),layout='constrained');plots=[];surf=[]
for ax,(name,lo,hi,title) in zip(axs,[('rhoCopper',1e-8,200,'Cu density [kg/m3]'),('plas.ne',1e9,1e24,'Electron density [1/m3]'),('jline3',1e-3,1e10,'510.55 nm emissivity [W/m3/sr]')]):
 im=ax.pcolormesh(x,z,np.maximum(a[ids[0],ex.index(name)],lo),norm=LogNorm(lo,hi),cmap='inferno',shading='auto');fig.colorbar(im,ax=ax);plots.append((im,name,lo));surf.append(ax.plot([],[],'c-',lw=1)[0]);ax.set(xlim=(0,100),ylim=(-3,100),xlabel='r [um]',ylabel='z [um]',title=title)
label=fig.suptitle('')
def update(ii):
 for im,name,lo in plots:im.set_array(np.maximum(a[ii,ex.index(name)],lo).ravel())
 ss=s[np.isclose(s['time_s'],t[ii],atol=1e-15,rtol=0)];ss=ss[np.argsort(ss['r_m'])]
 for line in surf:line.set_data(ss['r_m']*1e6,ss['z_m']*1e6)
 label.set_text(f'{case}: {t[ii]*1e9:.1f} ns; saved partial solution, not a validated S4 exposure')
 return [p[0] for p in plots]+surf+[label]
out=r/'figures'/case;out.mkdir(exist_ok=True);FuncAnimation(fig,update,frames=ids,blit=False).save(out/'fields_evolution.gif',writer=PillowWriter(fps=4),dpi=110);print(out/'fields_evolution.gif')
