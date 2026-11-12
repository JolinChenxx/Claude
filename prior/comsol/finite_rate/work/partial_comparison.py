"""Clearly marked preview using only completed radiation frames."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from morphology import experimental_signal,quantiles

def run(case):
    folder=RES/f'{case}_radiation';state=json.loads((folder/'progress.json').read_text());n=state['finished'];ts=np.array([r['t_ns'] for r in state['rows']])*1e-9
    pr=np.load(folder/'photon_radiance.npy',mmap_mode='r')[:n,0];base=RES/'R2_B64A_LTE';bt=np.load(base/'axes.npz')['times_s'];bp=np.load(base/'photon_radiance.npy',mmap_mode='r')[:,0];early=bt<ts[0]-1e-15
    t=np.r_[bt[early],ts];p=np.concatenate([bp[early],pr]);exp=np.load(SOURCE/'experiment/S4_reconstructed.npz')
    indices=np.where(exp['times_ns']+95<=t[-1]*1e9)[0];ns=exp['times_ns'][indices];factor=OMEGA*(PIXEL*1e-6)**2
    q=blur(gate(t,p,ns+90))*factor;l=blur(gate(bt,bp,ns+90))*factor
    plt=setup_plot();fig,axs=plt.subplots(1,3,figsize=(17,4.6),layout='constrained')
    for label,data in [('LTE',l),(case,q)]:
        totals=data.sum((1,2));axs[0].semilogy(ns,np.maximum(totals,1e-35),'o-',label=label)
        width=np.array([quantiles(im)['width90_um'] for im in data]);axs[1].plot(ns,np.where(totals>=1,width,np.nan),'o-',label=label)
        axs[2].semilogy(ns,totals/max(totals.max(),1e-300),'o-',label=label)
    obs=np.array([experimental_signal(im) for im in exp['gray']]);total=obs.sum((1,2));axs[2].semilogy(exp['times_ns'],total/total.max(),'k-',label='Experiment relative only')
    axs[0].axhline(1,color='gray',ls=':');axs[0].set_ylim(1e-15,1e7);axs[2].set_ylim(1e-5,2)
    for ax,title,y in zip(axs,['Absolute ideal collected photons','Width enclosing 90% photons','Relative temporal shape'],['photons / 10 ns','um','relative to global peak so far']):ax.set(title=title,xlabel='Experimental delay, shift 90 ns (ns)',ylabel=y);ax.legend(fontsize=8);ax.grid(alpha=.2)
    fig.suptitle(f'PROGRESS PREVIEW: {case}, completed radiation only through {t[-1]*1e9-95:.0f} ns experimental gate | ideal collection, no fit')
    fig.savefig(FIG/f'{case}_progress_comparison.png');plt.close(fig)
    write_json(RES/f'{case}_progress_comparison.json',dict(completed_numerical_ns=float(t[-1]*1e9),experimental_ns=ns.tolist(),LTE_photons=l.sum((1,2)).tolist(),CR_photons=q.sum((1,2)).tolist(),not_final=True))

if __name__=='__main__':run(sys.argv[1])
