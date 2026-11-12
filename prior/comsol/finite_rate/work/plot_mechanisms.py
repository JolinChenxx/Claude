import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from plot_baseline import panel_plot
from morphology import quantiles,experimental_signal
import csv,argparse

def series(case,key='photon_radiance'):
    base=RES/'R2_B64A_LTE';tb=np.load(base/'axes.npz')['times_s'];vb=np.load(base/(key+'.npy'),mmap_mode='r')[:,0]
    if case=='LTE':return tb,vb
    folder=RES/f'{case}_radiation';tc=np.load(folder/'axes.npz')['times_s'];vc=np.load(folder/(key+'.npy'),mmap_mode='r')[:,0]
    m=tb<tc[0]-1e-15
    return np.r_[tb[m],tc],np.concatenate([vb[m],vc])

def run(cases):
    exp=np.load(SOURCE/'experiment/S4_reconstructed.npz');ids=[np.argmin(abs(exp['times_ns']-ns)) for ns in PANELS]
    plt=setup_plot();from matplotlib.colors import LogNorm
    data={};rows=[]
    factor=OMEGA*(PIXEL*1e-6)**2
    for case in ['LTE']+cases:
        t,pr=series(case);tr,rad=series(case,'radiance');maps={}
        for shift in SHIFTS:
            q=blur(gate(t,pr,exp['times_ns']+shift))*factor;maps[f'shift_{int(shift)}']=q
            for ns,im in zip(exp['times_ns'],q):
                rows.append(dict(case=case,shift_ns=shift,experiment_ns=float(ns),peak_photons=float(im.max()),pixels_ge_1=int((im>=1).sum()),pixels_ge_10=int((im>=10).sum()),**metrics(im),**quantiles(im)))
        data[case]=maps['shift_90'][ids]
        if case!='LTE':
            np.savez_compressed(RES/f'{case}_gated.npz',times_experiment_ns=exp['times_ns'],**maps)
            energy=blur(gate(tr,rad,PANELS+90,average=True))
            panel_plot(energy,data[case],exp['gray'][ids],f'{case}_absolute.png',f'{case} | 475-525 nm, ideal collection | inherited LTE before 120 numerical ns | shift 90 ns, no fit')
    with (RES/'R2_mechanism_metrics.csv').open('w',newline='',encoding='utf-8') as f:
        w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
    fig,axs=plt.subplots(1,3,figsize=(17,4.8),layout='constrained')
    for case in data:
        rr=[r for r in rows if r['case']==case and r['shift_ns']==90]
        ts=np.array([r['experiment_ns'] for r in rr]);val=np.array([r['total'] for r in rr]);good=val>=1
        axs[0].semilogy(ts,np.maximum(val,1e-40),'o-',label=case)
        axs[1].plot(ts,np.where(good,[r['width90_um'] for r in rr],np.nan),'o-',label=case)
        axs[2].semilogy(ts,np.maximum(val/val.max(),1e-10),'o-',label=case)
    obs=np.array([experimental_signal(im) for im in exp['gray']]);rel=obs.sum((1,2));rel/=rel.max()
    axs[2].semilogy(exp['times_ns'],rel,'k-',lw=2,label='Experiment relative only')
    axs[0].axhline(1,color='gray',ls=':');axs[0].set_ylim(1e-15,1e8);axs[2].set_ylim(1e-6,2)
    for a,title,y in zip(axs,['Absolute model photons','Width of observable predicted light','Relative brightness evolution'],['photons / 10 ns','90% signal width (um)','relative to each global peak']):
        a.set(title=title,xlabel='Experimental delay (ns)',ylabel=y);a.grid(alpha=.2);a.legend(fontsize=7)
    fig.savefig(FIG/'R2_mechanism_comparison.png');plt.close(fig)
    fig,axs=plt.subplots(len(data)+1,len(PANELS),figsize=(23,2.5*(len(data)+1)),layout='constrained')
    h,w=obs.shape[-2:];pmax=max(10,max(a.max() for a in data.values()));norm=LogNorm(1e-6,pmax)
    for j,ns in enumerate(PANELS):
        axs[0,j].imshow(exp['gray'][ids[j]],vmin=0,vmax=255,cmap='turbo');axs[0,j].set_title(f'{ns:g} ns')
    axs[0,0].set_ylabel('Experiment\nrelative gray')
    for i,(case,a) in enumerate(data.items(),1):
        mid=a.shape[-1]//2;a=a[:,-h:,mid-w//2:mid+(w-w//2)]
        for j,im in enumerate(a):
            artist=axs[i,j].imshow(np.ma.masked_less_equal(im,0),norm=norm,cmap='inferno')
            axs[i,j].text(.03,.96,f'{im.sum():.2e} photons',transform=axs[i,j].transAxes,va='top',color='white',fontsize=7)
        axs[i,0].set_ylabel(case)
    for a in axs.ravel():a.set_xticks([]);a.set_yticks([])
    fig.colorbar(artist,ax=axs[1:].ravel().tolist(),label='Expected photons / pixel / 10 ns',fraction=.015,pad=.01)
    fig.suptitle('Shared fixed photon scale | NA 0.4, ideal efficiency | 90 ns alignment convention | target not registered')
    fig.savefig(FIG/'R2_mechanism_frames.png');plt.close(fig)
    write_json(RES/'R2_mechanism_plot_notes.json',dict(cases=list(data),assumptions=['No fitted gain or time shift.','Kinetics inherits LTE at numerical 120 ns; earlier gates use LTE.','Photon expectation uses ideal transmission and efficiency.','Experiment absolute target plane and brightness calibration absent.','Below one whole-image photon, shape metrics are not presented as observable.']))

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('cases',nargs='*');a=p.parse_args();run(a.cases)
