"""Spatial origin of finite-rate light: matter, temperatures, charge, emission."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from render_kinetics import physical_all
from morphology import quantiles
import csv

def run(case):
    meta=json.loads((RES/f'{case}_fields.json').read_text());f=np.memmap(RES/f'{case}_fields.bin',dtype=meta['dtype'],shape=tuple(meta['shape']),mode='r')
    r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z']);ts=np.array(meta['times_s']);net=json.loads((ROOT/'data/network.json').read_text())
    folder=RES/f'{case}_radiation';rt=np.load(folder/'axes.npz')['times_s'];p=np.load(folder/'photon_radiance.npy',mmap_mode='r')[:,0]
    delays=np.array([60.,150.,260.,740.]);images=blur(gate(rt,p,delays+90))*OMEGA*(PIXEL*1e-6)**2
    plt=setup_plot();from matplotlib.colors import LogNorm
    fig,axs=plt.subplots(6,len(delays),figsize=(16,20),layout='constrained');rows=[]
    norms=[LogNorm(1e-7,100),None,None,LogNorm(1e12,1e24),LogNorm(1e-4,1e11),LogNorm(1e-6,2e3)]
    labels=['Cu density (kg/m3)','Gas temperature (K)','Electron temperature (K)','Electron density (m-3)','Local visible Cu emission (W/m3/sr)','Ideal photons / pixel / 10 ns']
    artists=[]
    for col,delay in enumerate(delays):
        i=np.argmin(abs(ts*1e9-(delay+90)));a=physical_all(np.asarray(f[i],float),r,z);j=np.zeros_like(a[0])
        for line in net['lines']:
            if 475<=line['wavelength_nm']<=525:j+=H*line['nu']*line['A']*np.maximum(a[11+line['up']],0)/(4*np.pi)
        values=[a[33],a[0],np.where(a[1]>1e16,a[19],np.nan),a[1],j]
        for row,val in enumerate(values):
            kw=dict(norm=norms[row]) if norms[row] is not None else dict(vmin=293,vmax=6500)
            im=axs[row,col].pcolormesh(r,z,np.ma.masked_invalid(np.ma.masked_less_equal(val,0)),cmap='inferno',shading='auto',**kw)
            if col==0:artists.append(im)
            axs[row,col].set(xlim=(0,450),ylim=(0,650),xlabel='r (um)',ylabel='z (um)')
            if row in (1,2,4):axs[row,col].contour(r,z,a[33],levels=[float(a[33].max())*.001],colors='cyan',linewidths=.5)
        ii=np.arange(0,len(r),8);jj=np.arange(0,len(z),10);rr,zz=np.meshgrid(r[ii],z[jj]);sel=a[33][np.ix_(jj,ii)]>a[33].max()*1e-5
        axs[0,col].quiver(rr[sel],zz[sel],a[5][np.ix_(jj,ii)][sel],a[6][np.ix_(jj,ii)][sel],color='cyan',scale=15000)
        im=axs[5,col].imshow(np.ma.masked_less_equal(images[col],0),norm=norms[5],cmap='inferno',extent=(-PIXEL*images.shape[2]/2,PIXEL*images.shape[2]/2,0,PIXEL*images.shape[1]))
        axs[5,col].set(xlim=(-450,450),ylim=(0,650),xlabel='x (um)',ylabel='z (um)')
        if col==0:artists.append(im)
        axs[0,col].set_title(f'Experimental {delay:g} ns')
        rays=Rays(r,z);cu_image=rays.project(np.maximum(a[33],0));j_image=rays.project(j)
        rows.append(dict(case=case,experiment_ns=float(delay),Cu_column_centroid_z_um=metrics(cu_image)['z_um'],Cu_column_width90_um=quantiles(cu_image)['width90_um'],
            thin_emission_centroid_z_um=metrics(j_image)['z_um'],thin_emission_width90_um=quantiles(j_image)['width90_um'],
            photon_centroid_z_um=metrics(images[col])['z_um'],photon_width90_um=quantiles(images[col])['width90_um'],photons=float(images[col].sum())))
    for row,label in enumerate(labels):fig.colorbar(artists[row],ax=axs[row].tolist(),label=label,fraction=.016,pad=.01)
    fig.suptitle(case+' | fixed physical scales | cyan: Cu density boundary / velocity | shift 90 ns | Te masked below ne=1e16 m-3')
    fig.savefig(FIG/f'{case}_shape_layers.png');plt.close(fig)
    with (RES/f'{case}_shape_layers.csv').open('w',newline='',encoding='utf-8') as file:
        writer=csv.DictWriter(file,fieldnames=list(rows[0]));writer.writeheader();writer.writerows(rows)
    print(rows)

if __name__=='__main__':run(sys.argv[1])
