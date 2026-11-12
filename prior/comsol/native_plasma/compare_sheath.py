"""Compare saved solutions at the same time after normal sheath refinement."""
from pathlib import Path
import csv
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

root=Path(__file__).resolve().parent
fig,axes=plt.subplots(1,3,figsize=(12,3.8),layout='constrained')
for case,label,style in [('P61','10 nm first layer','-'),('P63','2 nm first layer','--')]:
    rows=list(csv.DictReader((root/'results'/f'{case}_sheath_profile.csv').open()))
    rows=[r for r in rows if float(r['r_m'])<1e-6]
    x=np.array([float(r['dz_from_wall_m'])*1e9 for r in rows])
    for ax,key in zip(axes,['plas.ne','plas.V','plas.Te']):
        y=np.array([float(r[key]) for r in rows])
        ax.semilogx(x,y,style,label=label)
axes[0].set_yscale('log')
for ax,title in zip(axes,['Electron density [m^-3]','Potential [V]','Electron temperature [eV]']):
    ax.set(xlabel='Height above actual copper surface [nm]',ylabel=title,xlim=(1,1e4));ax.grid(alpha=.2);ax.legend(fontsize=8)
fig.suptitle('90.2 ns saved states at r = 0.1 um; native Plasma sheath refinement\nNo S4 match or independent electron-density validation yet',fontsize=11)
path=root/'figures'/'P61_P63_sheath_comparison.png';fig.savefig(path,dpi=170);print(path)
