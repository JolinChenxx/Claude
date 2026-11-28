"""Physical budgets from saved fixed-plane stages, without optical calibration."""
from pathlib import Path
import sys,csv,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from scipy.integrate import cumulative_trapezoid
R=Path(__file__).resolve().parent;out,*cases=sys.argv[1:]
def merge(suffix):
    rows=[];header=None
    for c in cases:
        with (R/'results'/f'{c}_{suffix}.csv').open() as f:
            r=csv.reader(f);h=next(r)
            if header is not None and header!=h:raise ValueError('Different columns')
            header=h;rows.extend([float(v) for v in row] for row in r)
    a=np.array(sorted(rows,key=lambda x:x[0]));a=a[np.r_[True,np.diff(a[:,0])>1e-16]]
    return header,a
hh,h=merge('history');mh,m=merge('mechanism');tm=m[:,0]*1e9
col=lambda k:m[:,mh.index(k)]
fig,ax=plt.subplots(2,2,figsize=(11,7.5),layout='constrained')
for name,label in [('mdotEvap','Evaporation'),('mdotReturn','Return'),('mdot','Net feed')]:
    ax[0,0].plot(tm,col('c.intSurf(c.'+name+')')*1e3,label=label)
ax[0,0].set(ylabel='Cu feed [ng / ns]',xlabel='Model time [ns]');ax[0,0].legend()
for j,label in [(13,'Target'),(14,'Plume')]:
    val=cumulative_trapezoid(h[:,j],h[:,0],initial=0)*1e6
    ax[0,1].semilogy(h[:,0]*1e9,np.maximum(val,1e-12),label=label)
ax[0,1].set(ylabel='Cumulative laser absorption [uJ]',xlabel='Model time [ns]',ylim=(1e-7,100));ax[0,1].legend()
labels=['Cu mass','Laser absorption','Visible emission'];fields=['rhoCopper','Qlaser','mechVisible'];positions=np.arange(3)
fractions={}
for j,(zone,label) in enumerate([('Wall','z < 2 um'),('Near','2 <= z < 50 um'),('Far','z >= 50 um')]):
    v=[col(f'c.intGas(c.mech{zone}*c.{field})')[-1]/col(f'c.intGas(c.mechAll*c.{field})')[-1] for field in fields]
    fractions[zone]=dict(zip(fields,v));ax[1,0].bar(positions+(j-1)*.25,np.maximum(v,1e-8),.25,label=label)
ax[1,0].set(xticks=positions,xticklabels=labels,yscale='log',ylim=(1e-7,2),ylabel='Fraction at final saved time');ax[1,0].legend(fontsize=8)
for radius in [10,25,50,100,200]:
    vals=col(f'c.intGas(c.mechR{radius})')/col('c.intGas(c.mechAll*c.rhoCopper)')
    ax[1,1].semilogy(tm,np.maximum(vals,1e-10),label=f'r > {radius} um')
ax[1,1].set(xlim=(75,tm[-1]),ylim=(1e-8,2),ylabel='Fraction of gas Cu beyond radius',xlabel='Model time [ns]');ax[1,1].legend(fontsize=8)
for aa in ax.flat:aa.grid(alpha=.2)
fig.suptitle(f'Ground-state Cu feed: saved solution through {tm[-1]:.1f} ns\nFixed target plane; physical budgets, no fitted optical gain; high-temperature Cu closure remains unvalidated')
p=R/'figures'/out;p.mkdir(exist_ok=True);fig.savefig(p/'supply_energy_diagnosis.png',dpi=160)
summary={'stages':cases,'last_ns':tm[-1],'final_spatial_fractions':fractions,'final_gas_Cu_ng':h[-1,8]*1e12,'mean_Cu_radius_um':col('c.intGas(c.rhoCopper*r)')[-1]/col('c.intGas(c.mechAll*c.rhoCopper)')[-1]*1e6,'mean_Cu_height_um':col('c.intGas(c.rhoCopper*z)')[-1]/col('c.intGas(c.mechAll*c.rhoCopper)')[-1]*1e6,'target_absorbed_uJ':float(np.trapz(h[:,13],h[:,0])*1e6),'plume_absorbed_uJ':float(np.trapz(h[:,14],h[:,0])*1e6)}
(R/'results'/f'{out}_supply_diagnosis.json').write_text(json.dumps(summary,indent=2));print(json.dumps(summary,indent=2))
