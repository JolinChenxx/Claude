"""Actual saved melt profiles and conservation, including warm-start interval."""
from pathlib import Path
import sys
import numpy as np
from scipy.integrate import cumulative_trapezoid
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent
case=sys.argv[1]
s=np.genfromtxt(R/'results'/f'{case}_surface.csv',delimiter=',',names=True)
a=np.loadtxt(R/'results'/f'{case}_melt_history.csv',delimiter=',',skiprows=1)
ts=np.unique(s['time_s'])
fig,axs=plt.subplots(2,2,figsize=(11,8),layout='constrained')
for target in np.linspace(ts[0],ts[-1],5):
 t=ts[np.argmin(abs(ts-target))];b=s[s['time_s']==t];b=b[np.argsort(b['r_m'])]
 for ax,key,scale in [(axs[0,0],'z_m',1e6),(axs[0,1],'T_K',1),(axs[1,0],'uz_m_s',1)]:ax.plot(b['r_m']*1e6,b[key]*scale,label=f'{t*1e9:.0f} ns')
evap=cumulative_trapezoid(a[:,3],a[:,0],initial=0);loss=a[0,1]-a[:,1]
axs[1,1].plot(a[:,0]*1e9,loss*1e12,label='Cu volume loss')
axs[1,1].plot(a[:,0]*1e9,evap*1e12,'--',label='Integrated evaporation')
axs[0,0].set(ylabel='Actual surface height [um]',title='Recession and raised molten rim')
axs[0,1].set(ylabel='Surface temperature [K]')
axs[1,0].set(ylabel='Axial liquid velocity [m/s]')
axs[1,1].set(xlabel='Numerical time [ns]',ylabel=f'Cu mass transferred since {ts[0]*1e9:.0f} ns [ng]')
for ax in [axs[0,0],axs[0,1],axs[1,0]]:ax.set(xlim=(0,30),xlabel='Radius [um]')
for ax in axs.flat:ax.grid(alpha=.2);ax.legend(fontsize=8)
fig.suptitle(f'{case}: refined native liquid heat + P2/P1 melt flow + actual moving mesh\nIsolated melt diagnostic; no plasma feedback; saved {ts[0]*1e9:.0f}–{ts[-1]*1e9:.0f} ns; mass mismatch {(loss[-1]/evap[-1]-1)*100:.2f}%')
out=R/'figures'/f'{case}_melt_progress.png';fig.savefig(out,dpi=160);print(out)
