"""Compare matched-time native fields on a common physical color scale."""
from pathlib import Path
import json,sys,numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
R=Path(__file__).resolve().parent
base,changed=sys.argv[1:3];target=float(sys.argv[3]) if len(sys.argv)>3 else 88.
data=[]
for case in [base,changed]:
    p=R/'results'/f'{case}_fields';d=json.loads(p.with_suffix('.json').read_text());a=np.memmap(p.with_suffix('.bin'),dtype='>f4',mode='r',shape=tuple(d['shape']));i=int(np.argmin(abs(np.array(d['times_s'])*1e9-target)))
    if abs(d['times_s'][i]*1e9-target)>.01:raise ValueError('No common saved time')
    data.append((d,a[i]))
fields=[('rhoCopper','Cu density [kg/m3]',1e-4),('plas.ne','Electron density [1/m3]',1e14),('Qlaser','Laser absorption [W/m3]',1e2)]
fig,axs=plt.subplots(2,3,figsize=(11,7),layout='constrained')
for j,(field,label,lo) in enumerate(fields):
    values=[a[d['expressions'].index(field)] for d,a in data];norm=LogNorm(lo,max(float(np.nanmax(v)) for v in values))
    for i,((d,a),v) in enumerate(zip(data,values)):
        ax=axs[i,j];im=ax.pcolormesh(d['r_um'],d['z_um'],np.ma.maximum(np.ma.masked_invalid(v),lo),norm=norm,cmap='inferno',shading='auto');ax.set(xlim=(0,60),ylim=(0,80),xlabel='r [um]',ylabel='z [um]',title=([base+': ground-state feed',changed+': thermal surface-state feed'][i])+'\n'+label);fig.colorbar(im,ax=ax)
note=sys.argv[4] if len(sys.argv)>4 else 'Before the first S4 exposure; no camera gain or fitted reaction rate'
fig.suptitle(f'Native fields at model time {target:g} ns; shared physical color scales\n{note}')
out=R/'figures'/f'{base}_vs_{changed}_{target:g}ns.png';fig.savefig(out,dpi=160);print(out)
