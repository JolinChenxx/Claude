"""Near-wall diagnostic from actual saved native Plasma fields."""
from pathlib import Path
import csv,sys
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent
case=sys.argv[1]
rows=list(csv.DictReader((R/'results'/f'{case}_sheath_profile.csv').open()))
a={k:np.array([float(x[k]) for x in rows]) for k in rows[0]}
fig,axs=plt.subplots(2,2,figsize=(10,7),layout='constrained')
for radius in np.unique(a['r_m']):
 s=a['r_m']==radius;x=a['dz_from_wall_m'][s]*1e6;label=f'r = {radius*1e6:g} um'
 axs[0,0].loglog(x,a['plas.ne'][s],label=label)
 axs[0,1].semilogx(x,a['plas.V'][s],label=label)
 axs[1,0].loglog(x,a['sqrt(epsilon0_const*plas.Te/(e_const*plas.ne))'][s]*1e6,label=label)
 axs[1,1].semilogx(x,a['plas.Te'][s],label=label)
axs[1,0].loglog(a['dz_from_wall_m'][:101]*1e6,a['h'][:101]*1e6,'k--',label='Mesh h at r=0.1 um')
for ax,title in zip(axs.flat,['Electron density [m^-3]','Potential [V]','Debye length and mesh h [um]','Electron temperature [eV]']):
 ax.set(xlabel='Height above moving copper surface [um]',ylabel=title);ax.grid(alpha=.2);ax.legend(fontsize=8)
time_rows=list(csv.DictReader((R/'results'/f'{case}_charge_quadrature.csv').open()))
last_ns=float(time_rows[-1]['time_s'])*1e9
fig.suptitle(f'{case}: {last_ns:g} ns saved state, near-wall diagnostic\nNot an S4 match; mesh h is the element size, not the normal layer thickness')
fig.savefig(R/'figures'/f'{case}_sheath_diagnostic.png',dpi=160)
print(R/'figures'/f'{case}_sheath_diagnostic.png')
