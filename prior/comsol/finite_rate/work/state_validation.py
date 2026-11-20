"""Charge, stored energy, composition and EOS checks on solved fields."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
import csv

def run(case):
    meta=json.loads((RES/f'{case}_fields.json').read_text())
    f=np.memmap(RES/f'{case}_fields.bin',dtype=meta['dtype'],shape=tuple(meta['shape']),mode='r')
    r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z'])
    ints=np.genfromtxt(RES/f'{case}_integrals.csv',delimiter=',',names=True);rows=[]
    for i,t in enumerate(meta['times_s']):
        a=np.asarray(f[i],float);vol=lambda x:float(volume_integral(x,r,z,a[10]))
        cu=np.maximum(a[33],0);weight=vol(cu);ne=np.maximum(a[1],0)
        charge=vol(abs(a[1]-a[18]-a[27]))/max(vol(ne),1e-300)
        mass=vol(abs(MCU*a[11:19].sum(0)-cu))/max(weight,1e-300)
        active=(cu>cu.max()*1e-4)&(ne>1e16)
        emitting=a[12:18].sum(0);emitmask=emitting>emitting.max()*1e-4
        localclosure=vol(abs(a[38])*emitmask)/max(vol(abs(a[32])*emitmask),1e-300) if a.shape[0]>38 else 0.
        rows.append(dict(numerical_ns=t*1e9,charge_relative_residual=charge,Cu_state_relative_residual=mass,
            Cu_weighted_Tg_K=vol(a[0]*cu)/max(weight,1e-300),Cu_weighted_Te_K=vol(a[19]*cu)/max(weight,1e-300),
            active_Te_min_K=float(a[19][active].min()) if active.any() else 0,
            active_Te_max_K=float(a[19][active].max()) if active.any() else 0,
            negative_electron_stored_J=float(abs(ints['negative_electron_J'][i])),
            electron_stored_J=float(ints['electron_stored_J'][i]),
            emitting_region_closure_fraction=localclosure,closure_abs_J=float(ints['abs_energy_closure_J'][i]),
            closure_fraction=float(ints['abs_energy_closure_J'][i]/max(abs(ints['background_thermal_J'][i]),1e-300))))
    with (RES/f'{case}_state_validation.csv').open('w',newline='',encoding='utf-8') as file:
        writer=csv.DictWriter(file,fieldnames=list(rows[0]));writer.writeheader();writer.writerows(rows)
    oneT=bool(np.allclose(f[-1,0],f[-1,19],rtol=1e-6))
    summary=dict(last_numerical_ns=rows[-1]['numerical_ns'],last=rows[-1],one_temperature=oneT,
        max_charge_relative_residual=max(x['charge_relative_residual'] for x in rows),
        max_Cu_state_relative_residual=max(x['Cu_state_relative_residual'] for x in rows),
        max_closure_fraction=max(x['closure_fraction'] for x in rows),
        max_emitting_region_closure_fraction=max(x['emitting_region_closure_fraction'] for x in rows),
        max_negative_electron_stored_J=max(x['negative_electron_stored_J'] for x in rows),
        scope='Charge neutrality is imposed algebraically (Cu+ plus LTE air electrons); this checks exported consistency, not independent electron continuity. Total energy conservation is checked separately including solid and boundary fluxes. In one-temperature runs stored ee is unused auxiliary data; actual electron energy is electron_J in integrals.csv.')
    write_json(RES/f'{case}_state_validation.json',summary)
    plt=setup_plot();fig,axs=plt.subplots(1,3,figsize=(15,4.5),layout='constrained');ts=np.array([x['numerical_ns'] for x in rows])-90
    for key,label in [('Cu_weighted_Tg_K','Cu weighted gas T'),('Cu_weighted_Te_K','Cu weighted electron T')]:axs[0].plot(ts,[x[key] for x in rows],label=label)
    axs[0].set_ylabel('K')
    for key,label in [('charge_relative_residual','Charge export residual'),('Cu_state_relative_residual','Cu state sum residual'),('closure_fraction','EOS energy closure residual')]:axs[1].semilogy(ts,np.maximum([x[key] for x in rows],1e-16),label=label)
    axs[1].set_ylabel('Relative residual')
    for key,label in [('electron_stored_J','Stored electron energy'),('negative_electron_stored_J','Absolute negative stored energy')]:axs[2].semilogy(ts,np.maximum([abs(x[key]) for x in rows],1e-30),label=label)
    axs[2].set_ylabel('J')
    for ax in axs:ax.set_xlabel('Experimental delay, shift 90 ns (ns)');ax.grid(alpha=.2);ax.legend(fontsize=7)
    fig.suptitle(case+' | transport + reaction + energy closure diagnostics')
    fig.savefig(FIG/f'{case}_state_validation.png');plt.close(fig)
    print(summary)

if __name__=='__main__':run(sys.argv[1])
