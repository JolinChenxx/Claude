"""Show the saved ignition interval and its failed discrete charge check."""
from pathlib import Path
import sys,csv
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent
case=sys.argv[1]
h=np.loadtxt(R/'results'/f'{case}_history.csv',delimiter=',',skiprows=1,ndmin=2)
q=np.loadtxt(R/'results'/f'{case}_charge_rates_order2.csv',delimiter=',',skiprows=1,ndmin=2)
t=h[:,0]*1e9
fig,ax=plt.subplots(2,2,figsize=(11,7),layout='constrained')
ax[0,0].semilogy(t,h[:,3],color='tab:purple');ax[0,0].set(ylabel='Peak electron density [m$^{-3}$]')
ax[0,1].semilogy(t,h[:,14],label='plume');ax[0,1].semilogy(t,h[:,13],label='target');ax[0,1].set(ylabel='Absorbed laser power [W]');ax[0,1].legend()
for col,label in [(1,'target'),(4,'heavy gas'),(5,'electrons')]:ax[1,0].plot(t,h[:,col]/1000,label=label)
ax[1,0].set(ylabel='Peak temperature [1000 K]');ax[1,0].legend()
charge=1.602176634e-19*(q[:,2]-q[:,1])+q[:,12]
dcharge=1.602176634e-19*(q[:,4]-q[:,3])+q[:,13]
integral=np.r_[0,np.cumsum((dcharge[1:]+dcharge[:-1])/2*np.diff(q[:,0]))]
ax[1,1].plot(q[:,0]*1e9,(charge-charge[0])*1e12,label='saved charge, assembly order 2')
ax[1,1].plot(t,(h[:,18]+h[:,19]-h[0,18]-h[0,19])*1e12,label='saved charge, output quadrature')
ax[1,1].plot(q[:,0]*1e9,integral*1e12,'--',label='integrated equation derivative')
ax[1,1].set(ylabel='Change in total charge [pC]');ax[1,1].legend(fontsize=8)
for a in ax.flat:a.set(xlabel='Model time [ns]');a.grid(alpha=.2)
fig.suptitle(f'{case}: saved avalanche onset; diagnostic only, charge conservation fails\nNo complete S4 exposure in this interval',fontsize=13)
out=R/'figures'/case;out.mkdir(exist_ok=True)
fig.savefig(out/'ignition_and_charge_diagnosis.png',dpi=170)
print(out/'ignition_and_charge_diagnosis.png')
