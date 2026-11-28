import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from morphology import quantiles

def load(case):
    base=RES/'R2_B64A_LTE';bt=np.load(base/'axes.npz')['times_s'];bp=np.load(base/'photon_radiance.npy',mmap_mode='r')[:,0]
    if case=='LTE':return bt,bp
    folder=RES/f'{case}_radiation';st=json.loads((folder/'progress.json').read_text());t=np.array([a['t_ns'] for a in st['rows']])*1e-9;p=np.load(folder/'photon_radiance.npy',mmap_mode='r')[:len(t),0];early=bt<t[0]-1e-15
    return np.r_[bt[early],t],np.concatenate([bp[early],p])

def run():
    cases=['LTE','R2_CR1T','R2_COUPLED_FIN'];centres=np.arange(125.,156.,2.);factor=OMEGA*(PIXEL*1e-6)**2;data={}
    plt=setup_plot();fig,axs=plt.subplots(1,2,figsize=(12,4.5),layout='constrained')
    for case in cases:
        t,p=load(case);q=blur(gate(t,p,centres))*factor;data[case]=q.sum((1,2)).tolist()
        axs[0].semilogy(centres-90,q.sum((1,2)),'o-',label=case)
        axs[1].plot(centres-90,[quantiles(im)['width90_um'] for im in q],'o-',label=case)
    for ax,title,y in zip(axs,['Collected photons per 10 ns','90% photon width'],['ideal photons','um']):ax.set(title=title,xlabel='Experimental delay, shift 90 ns (ns)',ylabel=y);ax.grid(alpha=.2);ax.legend(fontsize=8)
    fig.suptitle('Coupled pilot window | two-temperature coupled model without electron conduction | no fitted gain')
    fig.savefig(FIG/'R2_pilot_photons.png');plt.close(fig);write_json(RES/'R2_pilot_photons.json',dict(experimental_ns=(centres-90).tolist(),photons=data))

if __name__=='__main__':run()
