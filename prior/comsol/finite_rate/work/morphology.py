"""Matched spatial diagnostics; no fitted translations or brightness gains."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from scipy.ndimage import label,binary_fill_holes
from legacy_radiation import physical_grid
import csv

def quantiles(img):
    a=np.maximum(img,0);total=a.sum()
    if total<=0:return dict(width90_um=np.nan,height90_um=np.nan,z05_um=np.nan,z95_um=np.nan)
    xx=(np.arange(a.shape[1])+.5-a.shape[1]/2)*PIXEL;zz=(np.arange(a.shape[0])+.5)*PIXEL
    cx=np.cumsum(a.sum(0))/total;cz=np.cumsum(a[::-1].sum(1))/total
    x=np.interp([.05,.95],cx,xx);z=np.interp([.05,.95],cz,zz)
    return dict(width90_um=float(x[1]-x[0]),height90_um=float(z[1]-z[0]),z05_um=float(z[0]),z95_um=float(z[1]))

def experimental_signal(gray,threshold=8.):
    a=np.maximum(gray.astype(float)-2,0)
    support=a>threshold;support[:4]=False;support[-4:]=False;support[:,:4]=False;support[:,-4:]=False
    lab,n=label(support);seed=a.copy();seed[:a.shape[0]//3]=0;seed[~support]=0
    if seed.max()==0:return np.zeros_like(a)
    component=lab[np.unravel_index(seed.argmax(),a.shape)]
    return a*binary_fill_holes(lab==component)

def run():
    folder=RES/'R2_B64A_LTE';ax=np.load(folder/'axes.npz');t=ax['times_s']
    rad=np.load(folder/'radiance.npy',mmap_mode='r')[:,0];thin=np.load(folder/'thin_radiance.npy',mmap_mode='r')[:,0]
    photon=np.load(folder/'photon_radiance.npy',mmap_mode='r')[:,0]
    r=gate(t,rad,PANELS+90,average=True);u=gate(t,thin,PANELS+90,average=True);b=blur(r)
    p=blur(gate(t,photon,PANELS+90))*OMEGA*(PIXEL*1e-6)**2
    old_gain=json.loads((SOURCE/'results/B64A_camera/calibration.json').read_text())['gain']
    old=np.clip(old_gain*b+2,0,255)-2
    exp=np.load(SOURCE/'experiment/S4_reconstructed.npz');ids=[np.argmin(abs(exp['times_ns']-ns)) for ns in PANELS]
    obs=np.array([experimental_signal(im) for im in exp['gray'][ids]])
    rows=[]
    for name,stack in [('thin',u),('self_absorbed',r),('with_psf',b),('historical_gain_saturated',old),('experiment',obs)]:
        for ns,img in zip(PANELS,stack):rows.append(dict(model=name,experiment_ns=ns,**metrics(img),**quantiles(img)))
    for threshold in (5.,20.):
        for ns,im in zip(PANELS,exp['gray'][ids]):
            rows.append(dict(model=f'experiment_threshold_{threshold:g}',experiment_ns=ns,**metrics(experimental_signal(im,threshold)),**quantiles(experimental_signal(im,threshold))))
    with (RES/'R2_morphology_metrics.csv').open('w',newline='',encoding='utf-8') as f:
        w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
    plt=setup_plot();from matplotlib.colors import LogNorm
    fig,axes=plt.subplots(5,len(PANELS),figsize=(23,12),layout='constrained')
    h,w=obs.shape[-2:];mid=r.shape[-1]//2
    extent=(-w*PIXEL/2,w*PIXEL/2,0,h*PIXEL)
    stacks=[obs,u,r,b,old];labels=['Experiment (relative)','No self-absorption','With self-absorption','Absorption + 1-pixel PSF','Historical gain 4.42e67\nnot a photon prediction']
    for row,(name,stack) in enumerate(zip(labels,stacks)):
        if row>0:stack=stack[:,-h:,mid-w//2:mid+(w-w//2)]
        kwargs=dict(vmin=0,vmax=255) if row in (0,4) else dict(norm=LogNorm(1e-6,max(1e-5,float(stack.max()))))
        for i,im in enumerate(stack):
            artist=axes[row,i].imshow(im,cmap='inferno',extent=extent,**kwargs)
            axes[row,i].set_xticks([]);axes[row,i].set_yticks([])
            if row==0:axes[row,i].set_title(f'{PANELS[i]:g} ns')
            if row==3:axes[row,i].text(.03,.96,f'{p[i].sum():.2e} photons',transform=axes[row,i].transAxes,color='white',va='top',fontsize=7)
        axes[row,0].set_ylabel(name);fig.colorbar(artist,ax=axes[row,:],fraction=.012,pad=.01)
    fig.suptitle('Shape attribution | matched physical field of view | shift 90 ns, target plane not registered | rows 2-4: W m-2 sr-1')
    fig.savefig(FIG/'R2_shape_ablation.png');plt.close(fig)
    fig,axs=plt.subplots(1,3,figsize=(16,4.5),layout='constrained')
    for name in ['experiment','thin','self_absorbed','with_psf','historical_gain_saturated']:
        rr=[x for x in rows if x['model']==name]
        valid=np.ones(len(rr),bool) if name in ['experiment','historical_gain_saturated'] else p.sum((1,2))>=1
        for a,key in zip(axs,['width90_um','height90_um','z_um']):
            v=np.array([x[key] for x in rr]);v=v-v[1] if key=='z_um' else v
            a.plot(PANELS,np.where(valid,v,np.nan),'o-',label=name)
    for a,title in zip(axs,['Width enclosing 90% signal','Height enclosing 90% signal','Centroid displacement relative to 30 ns']):
        a.set_title(title);a.set_xlabel('Experimental delay (ns)');a.set_ylabel('um');a.grid(alpha=.2);a.legend(fontsize=7)
    fig.savefig(FIG/'R2_shape_metrics.png');plt.close(fig)
    # Material motion and emission profiles on the expanded field.
    f,nr,nz,nt=fields();rays=Rays(nr,nz)
    fig,axs=plt.subplots(2,3,figsize=(15,8),layout='constrained')
    for col,ns in enumerate([30,80,260]):
        k=int(np.argmin(abs(nt*1e9-(ns+90))));a=physical_grid(np.array(f[k],float),nr,nz);cu=a[3]*np.clip(a[2],0,1)
        im=axs[0,col].pcolormesh(nr,nz,np.ma.masked_less_equal(cu,0),shading='auto',norm=LogNorm(1e-7,100),cmap='magma')
        ii=np.arange(0,len(nr),10);jj=np.arange(0,len(nz),12);rr,zz=np.meshgrid(nr[ii],nz[jj]);mask=cu[np.ix_(jj,ii)]>1e-5
        axs[0,col].quiver(rr[mask],zz[mask],a[5][np.ix_(jj,ii)][mask],a[6][np.ix_(jj,ii)][mask],color='cyan',scale=30000)
        axs[0,col].set(xlim=(0,350),ylim=(0,550),title=f'{ns} ns: Cu density + velocity',xlabel='r (um)',ylabel='z (um)')
        panel=int(np.where(PANELS==ns)[0][0]);xx=ax['x_um'];zz=ax['z_um']
        axs[1,col].semilogy(xx,np.maximum(p[panel].sum(0),1e-30),label='Radial marginal photons')
        axs[1,col].semilogy(zz,np.maximum(p[panel].sum(1),1e-30),label='Axial marginal photons')
        axs[1,col].set(xlim=(-350,550),ylim=(1e-10,1e5),xlabel='x or z (um)',ylabel='Photons / row or column / 10 ns');axs[1,col].legend(fontsize=8)
    fig.colorbar(im,ax=axs[0],label='Cu kg/m3');fig.suptitle('Material expands while visible LTE emission remains near the surface')
    fig.savefig(FIG/'R2_velocity_profiles.png');plt.close(fig)
    write_json(RES/'R2_morphology_notes.json',dict(experiment_component='Fixed gray threshold 8 above background 2, connected component containing lower-frame bright maximum; thresholds 5/20 saved.',absolute_target_registration=False,detachment='Only centroid displacement and height span are identifiable; physical target detachment is not established.',old_gain='Historical diagnostic only; fixed 90 ns shift, no reoptimization.',unobservable_shape_mask='Model curves omitted after whole-image expected photons <1 per gate.'))

if __name__=='__main__':run()
