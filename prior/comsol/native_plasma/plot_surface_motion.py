"""Actual moving interface profiles at saved times, with explicit deformation scale."""
from pathlib import Path
import sys,numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;case=sys.argv[1]
s=np.genfromtxt(R/'results'/f'{case}_surface.csv',delimiter=',',names=True)
if len(sys.argv)>2:s=s[s['time_s']<=float(sys.argv[2])*1e-9+1e-16]
t=np.unique(s['time_s']);targets=np.unique([t[abs(t-x).argmin()] for x in np.linspace(max(t[0],t[-1]-20e-9),t[-1],3)])
fig,ax=plt.subplots(1,3,figsize=(12,3.8),layout='constrained')
for tt in targets:
    q=s[np.isclose(s['time_s'],tt,atol=1e-16,rtol=0)];q=q[np.argsort(q['r_m'])];q=q[q['r_m']<=60e-6]
    _,idx=np.unique(np.round(q['r_m'],13),return_index=True);q=q[idx];label=f'{tt*1e9:.1f} ns'
    ax[0].plot(q['r_m']*1e6,q['z_m']*1e9,label=label)
    ax[1].plot(q['r_m']*1e6,q['T_K'],label=label)
    ax[2].plot(q['r_m']*1e6,np.sqrt(q['ur_m_s']**2+q['uz_m_s']**2),label=label)
for aa,lab in zip(ax,['Surface displacement [nm]','Surface temperature [K]','Liquid speed [m/s]']):
    aa.set(xlabel='r [um]',ylabel=lab,xlim=(0,60));aa.grid(alpha=.2);aa.legend()
fig.suptitle(f'{case}: actual moving mesh and molten Cu; last saved time {t[-1]*1e9:.1f} ns\nReference plane z=0; displacement is shown in nanometres, not to physical aspect ratio')
p=R/'figures'/case;p.mkdir(exist_ok=True);fig.savefig(p/'surface_motion.png',dpi=160)
