"""Separate absorption and pixel PSF effects on the finite-rate morphology."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from morphology import quantiles
import csv

def run(case):
    folder=RES/f'{case}_radiation';t=np.load(folder/'axes.npz')['times_s'];delays=np.array([60.,150.,260.,740.]);factor=OMEGA*(PIXEL*1e-6)**2
    thin=gate(t,np.load(folder/'thin_photon_radiance.npy',mmap_mode='r')[:,0],delays+90)*factor
    rad=gate(t,np.load(folder/'photon_radiance.npy',mmap_mode='r')[:,0],delays+90)*factor
    stacks=[thin,rad,blur(rad)];names=['Optically thin','Formal self-absorption','Absorption + fixed PSF'];rows=[]
    plt=setup_plot();from matplotlib.colors import LogNorm
    fig,axs=plt.subplots(3,4,figsize=(16,12),layout='constrained');pmax=max(10,float(thin.max()),float(rad.max()))
    for i,(stack,name) in enumerate(zip(stacks,names)):
        for j,(ns,im) in enumerate(zip(delays,stack)):
            artist=axs[i,j].imshow(np.ma.masked_less_equal(im,0),norm=LogNorm(1e-6,pmax),cmap='inferno',extent=(-PIXEL*im.shape[1]/2,PIXEL*im.shape[1]/2,0,PIXEL*im.shape[0]))
            axs[i,j].set(xlim=(-450,450),ylim=(0,650),xlabel='x (um)',ylabel='z (um)')
            if i==0:axs[i,j].set_title(f'{ns:g} ns')
            axs[i,j].text(.02,.96,f'{im.sum():.3g} photons',transform=axs[i,j].transAxes,va='top',color='white',fontsize=8)
            if j==0:axs[i,j].set_ylabel(name+'\nz (um)')
            rows.append(dict(case=case,stage=name,experiment_ns=float(ns),**metrics(im),**quantiles(im)))
    fig.colorbar(artist,ax=axs.ravel().tolist(),fraction=.015,pad=.01,label='Ideal photons / pixel / 10 ns')
    fig.suptitle(case+' | same populations, exposure and spatial field | no fitted response | common fixed log scale')
    fig.savefig(FIG/f'{case}_shape_ablation.png');plt.close(fig)
    with (RES/f'{case}_shape_ablation.csv').open('w',newline='',encoding='utf-8') as file:
        writer=csv.DictWriter(file,fieldnames=list(rows[0]));writer.writeheader();writer.writerows(rows)
    print(rows[-4:])

if __name__=='__main__':run(sys.argv[1])
