"""Energy and conservation budgets for exported spatial kinetics."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
import argparse

def run(case):
    a=np.genfromtxt(RES/f'{case}_integrals.csv',delimiter=',',names=True);t=a['t_s'];dt=np.diff(t)
    integ=lambda q:np.r_[0,np.cumsum((q[1:]+q[:-1])*.5*dt)]
    collision=integ(a['collision_W']);rad=integ(a['escape_W']);laser=integ(a['laser_W']);elastic=integ(a['elastic_W']);quench=integ(a['quench_W'])
    mass_error=(a['copper_kg']-a['background_copper_kg'])/a['background_copper_kg']
    neg=np.abs(a['negative_copper_kg'])/np.maximum(a['copper_kg'],1e-40)
    meta=json.loads((RES/f'{case}_fields.json').read_text());f=np.memmap(RES/f'{case}_fields.bin',dtype=meta['dtype'],shape=tuple(meta['shape']),mode='r')
    r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z']);bad=[]
    oneT=bool(np.allclose(f[-1,0],f[-1,19],rtol=1e-6))
    for i in range(len(t)):
        native=np.asarray(f[i],float);ne=native[1];T=native[0];raw=native[31]
        # Conservative stored ee should be inspected separately from clipped Te.
        negative_fraction=float(volume_integral(np.maximum(-raw,0)*ne,r,z,native[10])/max(volume_integral(abs(raw)*ne,r,z,native[10]),1e-300))
        bad.append(0. if oneT else negative_fraction)
    plt=setup_plot();fig,axs=plt.subplots(1,3,figsize=(17,4.5),layout='constrained');tn=t*1e9-90
    for key,label in [('chemical_J','Cu excitation + ionization'),('electron_J','electron thermal')]:axs[0].semilogy(tn,np.maximum(a[key],1e-30),label=label)
    for values,label in [(collision,'net e -> reactions'),(rad,'escaping radiation'),(laser,'absorbed laser'),(elastic,'heavy -> e exchange'),(quench,'quenching -> heavy')]:axs[1].plot(tn,values*1e6,label=label)
    axs[2].plot(tn,100*mass_error,label='Cu mass vs prescribed flow');axs[2].plot(tn,100*neg,label='negative Cu / total')
    if not oneT:axs[2].plot(tn,100*np.array(bad),label='negative electron-energy proxy')
    for ax,title,y in zip(axs,['Stored non-equilibrium energy','Integrated energy transfers','Conservation / positivity diagnostics'],['J','microjoule','percent']):ax.set(title=title,xlabel='Experimental delay, shift 90 ns (ns)',ylabel=y);ax.grid(alpha=.2);ax.legend(fontsize=7)
    fig.suptitle(case+' | prescribed-flow 1T can borrow energy from a thermostat; brighter light alone is not energy closure')
    fig.savefig(FIG/f'{case}_energy.png');plt.close(fig)
    write_json(RES/f'{case}_energy_summary.json',dict(last_numerical_ns=float(t[-1]*1e9),max_copper_mass_discrepancy=float(abs(mass_error).max()),max_negative_Cu_fraction=float(neg.max()),max_negative_electron_energy_proxy=max(bad),
        net_electron_to_reactions_J=float(collision[-1]),escaping_radiation_J=float(rad[-1]),laser_absorbed_J=float(laser[-1]),elastic_to_electrons_J=float(elastic[-1]),quench_to_heavy_J=float(quench[-1]),
        initial_chemical_J=float(a['chemical_J'][0]),final_chemical_J=float(a['chemical_J'][-1]),initial_electron_J=float(a['electron_J'][0]),final_electron_J=float(a['electron_J'][-1]),
        one_temperature=oneT,unused_electron_equation=oneT,
        scope='Integrated exported source terms. Open boundaries/evaporation and diffusion are not included in this source-only ledger; not a claimed global energy closure. In 1T the auxiliary ee is ignored and actual electron energy is 1.5 ne k Tg.'))

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('case');a=p.parse_args();run(a.case)
