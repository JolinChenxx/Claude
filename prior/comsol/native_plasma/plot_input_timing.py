from pathlib import Path
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent
fig,ax=plt.subplots(1,3,figsize=(13,3.8),layout='constrained')
for case,label in [('H01','Finite pulse: half edges0/60ns'),('H04','Gaussian: half maxima0/60ns')]:
    a=np.loadtxt(R/'results'/f'{case}_thermal_source.csv',delimiter=',',skiprows=1);t=a[:,0]*1e9-118
    ax[0].plot(t,a[:,5]/.1/1000,label=label);ax[1].plot(t,a[:,1]*1e6,label=case);ax[2].plot(t,a[:,4],label=case)
t=np.linspace(-118,300,1000);power=400e-6/(60e-9*np.sqrt(np.pi/(4*np.log(2))))*np.exp(-4*np.log(2)*((t-2)/60)**2)
ax[0].plot(t,power/1000,':',label='Inherited Gaussian: peak2ns')
for q,label in zip(ax,['Incident laser [kW]','Evaporation [mg/s]','Maximum target [K]']):
    q.set_xlabel('Experimental time [ns]');q.set_ylabel(label);q.set_xlim(-80,250);q.grid(alpha=.2);q.legend(fontsize=7)
fig.suptitle('Unmeasured temporal-envelope sensitivity; all400uJ,60ns; camera shift stays118ns',fontsize=11)
out=R/'figures/extended_final';out.mkdir(exist_ok=True);fig.savefig(out/'input_timing_and_source.png',dpi=160)
