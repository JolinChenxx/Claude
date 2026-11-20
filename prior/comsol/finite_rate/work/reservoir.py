"""Separate stored Cu excitation/ionization and compare to modeled radiation."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from rate_tables import ENERGIES,CHI
import csv

def run(case):
    meta=json.loads((RES/f'{case}_fields.json').read_text());f=np.memmap(RES/f'{case}_fields.bin',dtype=meta['dtype'],shape=tuple(meta['shape']),mode='r')
    r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z']);ts=np.array(meta['times_s']);rows=[]
    ints=np.genfromtxt(RES/f'{case}_integrals.csv',delimiter=',',names=True)
    escaped=np.r_[0,np.cumsum((ints['escape_W'][1:]+ints['escape_W'][:-1])*.5*np.diff(ts))]
    for i,t in enumerate(ts):
        a=np.asarray(f[i],float);counts=volume_integral(np.maximum(a[11:19],0),r,z,a[10])
        metastable=float(np.dot(counts[1:3],ENERGIES[1:3])*EV);upper=float(np.dot(counts[3:7],ENERGIES[3:7])*EV);ion=float(counts[7]*CHI*EV)
        rows.append(dict(numerical_ns=t*1e9,metastable_J=metastable,upper_excited_J=upper,ionization_J=ion,
            electron_J=float(ints['electron_J'][i]),cumulative_local_escape_J=float(escaped[i]),
            ionization_500nm_photon_energy_equivalent=ion/(H*C/500e-9)))
    with (RES/f'{case}_reservoir.csv').open('w',newline='',encoding='utf-8') as file:
        w=csv.DictWriter(file,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
    plt=setup_plot();fig,axs=plt.subplots(1,2,figsize=(13,4.6),layout='constrained');tn=ts*1e9-90
    for key,label in [('metastable_J','Cu 3d metastable excitation'),('upper_excited_J','Cu 4p / green upper excitation'),('ionization_J','Cu ionization'),('electron_J','Electron thermal')]:
        axs[0].semilogy(tn,np.maximum([x[key] for x in rows],1e-30),label=label)
    axs[1].plot(tn,escaped*1e9,label='Cumulative local escaping radiation')
    axs[0].set(ylabel='Stored energy (J)',title='Available reservoirs, including open-boundary transport')
    axs[1].set(ylabel='nJ',title='All network lines + recombination, local escape closure')
    for ax in axs:ax.set_xlabel('Experimental delay, shift 90 ns (ns)');ax.grid(alpha=.2);ax.legend(fontsize=7)
    fig.suptitle(case+' | reservoir sufficiency does not establish the rate or spectrum of energy release')
    fig.savefig(FIG/f'{case}_reservoir.png');plt.close(fig)
    write_json(RES/f'{case}_reservoir_summary.json',dict(initial=rows[0],final=rows[-1],
        initial_ionization_over_integrated_escape=rows[0]['ionization_J']/max(escaped[-1],1e-300),
        initial_upper_excitation_over_integrated_escape=rows[0]['upper_excited_J']/max(escaped[-1],1e-300),
        notes='Reservoirs integrated on exported field grid; source integral on COMSOL mesh. The 500 nm equivalent is an energy upper bound, not a predicted photon yield; evaporation and boundary loss prevent interpreting reservoir changes alone as emitted light.'))
    print(rows[0],rows[-1])

if __name__=='__main__':run(sys.argv[1])
