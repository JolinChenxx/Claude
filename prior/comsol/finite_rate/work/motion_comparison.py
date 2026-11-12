import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from morphology import experimental_signal,quantiles

def run():
    exp=np.load(SOURCE/'experiment/S4_reconstructed.npz');t=exp['times_ns'];signals={threshold:np.array([experimental_signal(im,threshold) for im in exp['gray']]) for threshold in [5,8,20]};plt=setup_plot()
    fig,axs=plt.subplots(1,2,figsize=(13,4.8),layout='constrained');stored={}
    for threshold,a in signals.items():
        z=np.array([metrics(im)['z_um'] if im.sum() else np.nan for im in a]);anchor=np.argmin(abs(t-30));z-=z[anchor];stored[f'centroid_threshold_{threshold}']=z.tolist();axs[0].plot(t,z,'o-',label=f'Publication component: gray >{threshold}')
        width=np.array([quantiles(im)['width90_um'] for im in a]);axs[1].plot(t,width,'o-',label=f'gray >{threshold}')
    # Published high-fluence spatter speed; onset and plateau describe a kinematic
    # guide only, not a prescribed plume-centroid fit or measured target registration.
    guide=344*np.clip(t-50,0,450)*1e-3;axs[0].plot(t,guide,'k--',lw=2,label='344 m/s spatter guide, 50-500 ns')
    base=RES/'R2_B64A_LTE';bt=np.load(base/'axes.npz')['times_s'];bp=np.load(base/'photon_radiance.npy',mmap_mode='r')[:,0];p=blur(gate(bt,bp,t+90))*OMEGA*(PIXEL*1e-6)**2
    zz=np.array([metrics(im)['z_um'] for im in p]);zz-=zz[np.argmin(abs(t-30))];good=p.sum((1,2))>=1;axs[0].plot(t,np.where(good,zz,np.nan),'s-',label='LTE light, observable gates only')
    axs[0].set(title='Vertical motion relative to the 30 ns image',ylabel='displacement (um)');axs[1].set(title='Experimental width and segmentation uncertainty',ylabel='90% signal width (um)')
    for a in axs:a.set_xlabel('Experimental delay (ns)');a.grid(alpha=.2);a.legend(fontsize=7)
    fig.suptitle('Published images indicate upward motion | absolute target plane unresolved | spatter guide is not a fit')
    fig.savefig(FIG/'R2_motion_spatter_comparison.png');plt.close(fig)
    # JSON replaces missing high-threshold late-frame centroids with null.
    stored={k:[None if not np.isfinite(v) else v for v in vals] for k,vals in stored.items()}
    write_json(RES/'R2_motion_spatter_comparison.json',dict(experimental_ns=t.tolist(),**stored,spatter_guide_um=guide.tolist(),source='Laser & Photonics Reviews 2025,19,2400388, section 2.4; DOI 10.1002/lpor.202400388',
        evidence='Paper states high-fluence spatter/plume-bottom rising speed 344 m/s, plateau after about 500 ns; bright central region coincides with top of molten copper.',
        limitation='Plume centroid and spatter top are different observables. 50 ns onset guide is approximate; absolute target registration is unavailable. No new source term or mass is inferred from the overlay.'))

if __name__=='__main__':run()
