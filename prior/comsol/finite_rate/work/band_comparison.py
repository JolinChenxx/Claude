import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *

def run(cases):
    plt=setup_plot();fig,axs=plt.subplots(1,len(cases),figsize=(6*len(cases),4.8),squeeze=False,layout='constrained');rows=[]
    for ax,case in zip(axs[0],cases):
        folder=RES/('R2_B64A_LTE' if case=='LTE' else case+'_radiation');state=json.loads((folder/'progress.json').read_text());n=state['finished'];t=np.array([a['t_ns'] for a in state['rows']])*1e-9
        p=np.load(folder/'photon_radiance.npy',mmap_mode='r')[:n].sum((2,3));centres=t[1:-1]*1e9;centres=centres[(centres>=t[0]*1e9+5)&(centres<=t[-1]*1e9-5)]
        photons=gate(t,p,centres)*OMEGA*(PIXEL*1e-6)**2
        for b,(lo,hi) in enumerate(BANDS):ax.semilogy(centres-90,np.maximum(photons[:,b],1e-35),label=f'{lo:g}-{hi:g} nm')
        ax.axhline(1,color='gray',ls=':');ax.set(title=case,xlabel='Experimental delay, shift 90 ns (ns)',ylabel='Ideal photons / 10 ns',ylim=(1e-15,1e7));ax.legend();ax.grid(alpha=.2)
        for ns,counts in zip(centres,photons):rows.append(dict(case=case,numerical_ns=float(ns),photons=counts.tolist()))
    fig.suptitle('Already-computed passband alternatives | non-LTE Cu lines lie near 510.6, 515.3, 521.8, 522.0 nm')
    fig.savefig(FIG/'R2_passband_comparison.png');plt.close(fig)
    write_json(RES/'R2_passband_comparison.json',dict(bands_nm=BANDS.tolist(),rows=rows,scope='Ideal top-hat bands; measured transmission curve is unavailable. Narrow 490-510 nm excludes the four resolved Cu lines; no line emission is invented inside it.'))

if __name__=='__main__':run(sys.argv[1:] or ['LTE','R2_CR1T'])
