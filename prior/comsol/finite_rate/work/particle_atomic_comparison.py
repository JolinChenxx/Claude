"""Conditional particle-only envelope against atomic reference; no double sum."""
import sys,csv
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from plot_mechanisms import series

def run(cases):
    particles=list(csv.DictReader((RES/'R2_particle_spatial.csv').open()));times=np.array([150.,260.,400.,580.,740.]);atom={};rows=[]
    for case in cases:
        t,p=series(case);atom[case]=blur(gate(t,p,times+90)).sum((1,2))*OMEGA*(PIXEL*1e-6)**2
    plt=setup_plot();fig,axs=plt.subplots(1,2,figsize=(13,4.6),layout='constrained')
    for index,(case,vals) in enumerate(atom.items()):axs[0].semilogy(times,vals,'o-',color=['black','gray'][index%2],label=case+' atomic reference')
    for index,name in enumerate(dict.fromkeys(x['case'] for x in particles)):
        rr=[x for x in particles if x['case']==name];vals=np.array([float(x['ideal_photons_10ns']) for x in rr])
        color=f'C{index+1}';axs[0].semilogy(times,np.maximum(vals,1e-30),'o--',color=color,label=name+' particle only')
        for ai,(case,ref) in enumerate(atom.items()):
            ratio=vals/ref;axs[1].semilogy(times,np.maximum(ratio,1e-30),marker='o',ls=['--',':'][ai%2],color=color,label=name+' / '+case)
            for ns,value,base,rel,row in zip(times,vals,ref,ratio,rr):rows.append(dict(particle_case=name,atomic_reference=case,experiment_ns=float(ns),particle_photons=float(value),atomic_photons=float(base),ratio=float(rel),particle_mass_ng=float(row['particle_kg'])*1e12,particle_temperature_K=float(row['T_particle_K'])))
    axs[0].set(title='Independent contributions, not a combined prediction',ylabel='Ideal photons / 10 ns',ylim=(1e-6,1e6))
    axs[1].axhline(.1,color='black',ls=':',label='10% contribution criterion');axs[1].set(title='Conditional particle / atomic reference ratio',ylabel='Ratio',ylim=(1e-8,1e5))
    for ax in axs:ax.set_xlabel('Experimental delay, shift 90 ns (ns)');ax.legend(fontsize=6);ax.grid(alpha=.2)
    fig.suptitle('Particles: 10% of B64A vapor Cu, assumed 2800 K formation at 150 ns | same ideal optics | no nucleation prediction')
    fig.savefig(FIG/'R2_particle_atomic_comparison.png');plt.close(fig)
    with (RES/'R2_particle_atomic_comparison.csv').open('w',newline='',encoding='utf-8') as file:
        writer=csv.DictWriter(file,fieldnames=list(rows[0]));writer.writeheader();writer.writerows(rows)
    write_json(RES/'R2_particle_atomic_comparison_notes.json',dict(scope='Separate conditional particle envelopes vs gas-only atomic reference. Curves are NOT summed: particle allocation depletes gas Cu and changes energy, requiring a coupled phase calculation for a combined prediction.',
        exceeds_10pct=[x for x in rows if x['ratio']>=.1],
        conclusion='The assumed hot-particle cases can exceed 10% at early formation times; nanoparticles cannot be categorically excluded. Long-lived contribution depends strongly on radius and thermal history.'))
    print([x for x in rows if x['ratio']>=.1])

if __name__=='__main__':run(sys.argv[1:] or ['R2_COUPLED'])
