from pathlib import Path
import sys
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;case=sys.argv[1]
a=np.genfromtxt(R/'results'/f'{case}_surface.csv',delimiter=',',names=True);ts=np.unique(a['time_s'])
fig,axs=plt.subplots(2,2,figsize=(11,8),layout='constrained')
for t in [60,70,80,ts[-1]*1e9]:
 tt=ts[abs(ts*1e9-t).argmin()];s=a[a['time_s']==tt];ix=np.argsort(s['r_m']);s=s[ix]
 axs[0,0].plot(s['r_m']*1e6,s['T_K'],label=f'{tt*1e9:.2f} ns')
 axs[0,1].plot(s['r_m']*1e6,s['z_m']*1e6,label=f'{tt*1e9:.2f} ns')
 axs[1,1].plot(s['r_m']*1e6,s['uz_m_s'],label=f'{tt*1e9:.2f} ns')
mx=np.array([a['T_K'][a['time_s']==t].max() for t in ts]);axs[1,0].plot(ts*1e9,mx,color='firebrick');axs[1,0].axhline(1358,ls=':',color='gray',label='melting point');axs[1,0].axhline(2836,ls='--',color='gray',label='normal boiling point');axs[1,0].legend()
axs[0,0].set(xlim=(0,60),xlabel='Radius [um]',ylabel='Surface temperature [K]');axs[0,0].legend()
axs[0,1].set(xlim=(0,60),xlabel='Radius [um]',ylabel='Actual mesh surface height [um]');axs[0,1].legend()
axs[1,0].set(xlabel='Numerical time [ns]',ylabel='Peak surface temperature [K]')
axs[1,1].set(xlim=(0,60),xlabel='Radius [um]',ylabel='Liquid axial velocity [m/s]')
for ax in axs.flat:ax.grid(alpha=.2)
fig.suptitle(f'{case}: isolated melt / evaporation / moving-mesh diagnostic\nNo plasma pressure feedback; stopped at {ts[-1]*1e9:.2f} ns; not an S4 comparison')
fig.savefig(R/'figures'/f'{case}_melt_diagnostic.png',dpi=160)
print(R/'figures'/f'{case}_melt_diagnostic.png')
