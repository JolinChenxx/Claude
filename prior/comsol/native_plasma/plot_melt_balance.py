"""Compare actual saved melt mass balance and pre-failure surface profiles."""
from pathlib import Path
import numpy as np
from scipy.integrate import cumulative_trapezoid
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent
fig,axs=plt.subplots(2,2,figsize=(11,8),layout='constrained')
for case,color in [('M07','darkorange'),('M09','steelblue'),('M11','forestgreen')]:
 a=np.loadtxt(R/'results'/f'{case}_melt_history.csv',delimiter=',',skiprows=1);a=a[a[:,0]<=110.01e-9]
 feed=cumulative_trapezoid(a[:,3],a[:,0],initial=0);loss=a[0,1]-a[:,1];good=feed>1e-15
 axs[0,0].plot(a[:,0]*1e9,loss*1e12,color=color,label=f'{case} Cu loss');axs[0,0].plot(a[:,0]*1e9,feed*1e12,'--',color=color,label=f'{case} evaporated')
 axs[0,1].plot(a[good,0]*1e9,100*(loss[good]-feed[good])/feed[good],color=color,label=case)
s=np.genfromtxt(R/'results/M11_surface.csv',delimiter=',',names=True);ts=np.unique(s['time_s'])
for target in [80,90,100,110]:
 t=ts[abs(ts*1e9-target).argmin()];a=s[s['time_s']==t];a=a[np.argsort(a['r_m'])]
 axs[1,0].plot(a['r_m']*1e6,a['z_m']*1e6,label=f'{target} ns');axs[1,1].plot(a['r_m']*1e6,a['T_K'],label=f'{target} ns')
axs[0,0].set(xlabel='Numerical time [ns]',ylabel='Cu mass [ng]',xlim=(80,110));axs[0,0].legend(fontsize=8)
axs[0,1].axhline(0,c='gray',lw=1);axs[0,1].set(xlabel='Numerical time [ns]',ylabel='(Cu loss - evaporation) / evaporation [%]',xlim=(80,110));axs[0,1].legend()
axs[1,0].set(xlabel='r [um]',ylabel='M11 surface height [um]',xlim=(0,30));axs[1,1].set(xlabel='r [um]',ylabel='M11 surface temperature [K]',xlim=(0,30))
for ax in axs[1]:ax.legend(fontsize=8)
for ax in axs.flat:ax.grid(alpha=.2)
fig.suptitle('Isolated melt diagnostics: surface kinematics and Cu mass balance\nM07: weak velocity; M09: mismatched displacement interpolation; M11: matched continuous displacement')
fig.savefig(R/'figures/melt_mass_balance_progress.png',dpi=160)
print(R/'figures/melt_mass_balance_progress.png')
