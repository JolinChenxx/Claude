import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
import csv

def panel_plot(energy, photons, experiment, name, caption, pixel=PIXEL):
    plt=setup_plot()
    from matplotlib.colors import LogNorm
    # Match physical pixel size AND field of view to the experimental crop.
    height,width=experiment.shape[-2:];mid=energy.shape[-1]//2
    energy=energy[:,-height:,mid-width//2:mid+(width-width//2)]
    photons=photons[:,-height:,mid-width//2:mid+(width-width//2)]
    fig,axes=plt.subplots(4,len(PANELS),figsize=(23,10.5),layout='constrained')
    vmax=max(float(energy.max()),1e-300);pmax=max(10.,float(photons.max()))
    for k,ns in enumerate(PANELS):
        ims=[]
        ims.append(axes[0,k].imshow(experiment[k],vmin=0,vmax=255,cmap='turbo',origin='upper'))
        extent=(-energy.shape[2]*pixel/2,energy.shape[2]*pixel/2,0,energy.shape[1]*pixel)
        ims.append(axes[1,k].imshow(energy[k],vmin=0,vmax=vmax,cmap='inferno',extent=extent))
        ims.append(axes[2,k].imshow(np.ma.masked_less_equal(photons[k],0),norm=LogNorm(1e-6,pmax),cmap='inferno',extent=extent))
        ims.append(axes[3,k].imshow(photons[k],vmin=0,vmax=pmax,cmap='inferno',extent=extent))
        for row in (2,3):
            levels=[lev for lev in [1,10] if photons[k].min()<lev<photons[k].max()]
            if levels:axes[row,k].contour(photons[k][::-1],levels=levels,colors=['cyan']*len(levels),linewidths=.5,extent=extent)
        axes[0,k].set_title(f'{ns:g} ns')
        axes[2,k].text(.03,.96,f'total={photons[k].sum():.2e}',transform=axes[2,k].transAxes,va='top',color='white',fontsize=7)
        for row in range(4):
            axes[row,k].set_xticks([]);axes[row,k].set_yticks([])
            if k==len(PANELS)-1:fig.colorbar(ims[row],ax=axes[row,:],fraction=.018,pad=.01)
    for ax,label in zip(axes[:,0],['Experiment\npublication gray','Band radiance\nW m$^{-2}$ sr$^{-1}$','Ideal photons / pixel\n10 ns; fixed log scale','Ideal photons / pixel\n10 ns; fixed linear scale']):ax.set_ylabel(label)
    fig.suptitle(caption,fontsize=13)
    fig.savefig(FIG/name);plt.close(fig)

def run(preview=False,case='B64A'):
    exp=np.load(SOURCE/'experiment'/'S4_reconstructed.npz')
    ids=[int(np.argmin(abs(exp['times_ns']-x))) for x in PANELS]
    if preview:
        a=np.load(SOURCE/'results'/f'{case}_radiation'/'radiance.npz')
        t=a['times_s'];rad=a['radiance'][:,0]
        e=blur(gate(t,rad,PANELS+90,average=True))
        p=e*10e-9*OMEGA*(PIXEL*1e-6)**2*525e-9/(H*C)
        panel_plot(e,p,exp['gray'][ids],'R2_absolute_preview.png',
            'Read-only baseline preview | no gain | shift 90 ns is a convention | photons use 525 nm UPPER bound, not spectral counts')
        return
    folder=RES/f'R2_{case}_LTE';a=np.load(folder/'axes.npz');t=a['times_s']
    rad=np.load(folder/'radiance.npy',mmap_mode='r')[:,0]
    pr=np.load(folder/'photon_radiance.npy',mmap_mode='r')[:,0]
    e=blur(gate(t,rad,PANELS+90,average=True));p=blur(gate(t,pr,PANELS+90))*OMEGA*(PIXEL*1e-6)**2
    panel_plot(e,p,exp['gray'][ids],f'R2_{case}_absolute.png',
        f'{case} LTE | no fitted gain | 475-525 nm | spectral photon counts, ideal collection | shift 90 ns (unmeasured)')
    rows=[];maps={}
    for shift in SHIFTS:
        allp=blur(gate(t,pr,np.asarray(exp['times_ns'])+shift))*OMEGA*(PIXEL*1e-6)**2
        maps[f'photons_shift_{int(shift)}']=allp
        for ns,img in zip(exp['times_ns'],allp):
            row=dict(experiment_ns=float(ns),shift_ns=shift,photon_total=float(img.sum()),photon_peak=float(img.max()),
                pixels_ge_1=int((img>=1).sum()),pixels_ge_10=int((img>=10).sum()),
                p_any_photon=float(-np.expm1(-img.sum())))
            row.update({f'moment_{key}':v for key,v in metrics(img).items()});rows.append(row)
    np.savez_compressed(folder/'gate_photons.npz',times_experiment_ns=exp['times_ns'],**maps)
    with (folder/'gate_metrics.csv').open('w',newline='',encoding='utf-8') as file:
        w=csv.DictWriter(file,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
    plt=setup_plot();fig,axes=plt.subplots(1,3,figsize=(16,4.7),layout='constrained')
    lum=np.genfromtxt(folder/'luminosity.csv',delimiter=',',names=True)
    for shift in SHIFTS:
        group=[x for x in rows if x['shift_ns']==shift]
        axes[0].semilogy([x['experiment_ns'] for x in group],[max(x['photon_total'],1e-300) for x in group],label=f'shift {shift:g} ns')
        emitted=gate(lum['t_ns']*1e-9,lum['photons_475_525_s'],np.asarray(exp['times_ns'])+shift)
        axes[1].semilogy(exp['times_ns'],np.maximum(emitted,1e-300),label=f'shift {shift:g} ns')
    axes[0].axhline(1,color='black',ls=':',label='1 photon / exposure')
    axes[1].axhline(1,color='black',ls=':')
    obs=exp['gray'].astype(float);bg=np.median(np.concatenate([obs[:,:5,:5].ravel(),obs[:,:5,-5:].ravel()]))
    signal=np.maximum(obs-bg,0).sum((1,2));axes[2].plot(exp['times_ns'],signal,'o-',label='Published image, background removed')
    for ax,title,y in zip(axes,['Ideal collected photons','All-direction emitted photons (thin upper bound)','Experiment brightness'],['photons / 10 ns','photons / 10 ns','summed gray counts']):
        ax.set_title(title);ax.set_xlabel('Experimental delay (ns)');ax.set_ylabel(y);ax.grid(alpha=.2);ax.legend(fontsize=8)
    fig.savefig(FIG/f'R2_{case}_photon_decay.png');plt.close(fig)
    write_json(folder/'camera_summary.json',dict(no_fit=True,photon_counts='ideal expectations, not observed counts',
        time_shifts_ns=list(SHIFTS),instrument_threshold_unknown=True,experiment_absolute_calibration=False,
        frames=rows))

if __name__=='__main__':
    import argparse
    p=argparse.ArgumentParser();p.add_argument('--preview',action='store_true');p.add_argument('--case',default='B64A');a=p.parse_args();run(a.preview,a.case)
