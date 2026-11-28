import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from legacy_radiation import Atomic,physical_grid
import csv

def run():
    plt=setup_plot()
    from matplotlib.colors import LogNorm
    atom=Atomic();allrows=[]
    for case in ('B64A','G64AC2','G264R1C5'):
        f,r,z,t=fields(case);rays=Rays(r,z);panels=[]
        for ns in PANELS:
            it=int(np.argmin(abs(t-(ns+90)*1e-9)));phys=physical_grid(np.array(f[it],float),r,z)
            jl,ff=atom.emissivities(phys)
            j=sum(j for j,line in zip(jl,atom.lines) if 475<=line['wavelength_air_nm']<=525)
            col=rays.project(phys[3]*np.clip(phys[2],0,1));image=rays.project(j)
            # Native meridional section and column projection have different units.
            centre=np.stack([phys[3]*np.clip(phys[2],0,1),phys[0],phys[1],j])
            section=np.array([RegularGridInterpolator((z,r),a,bounds_error=False,fill_value=0)(
                np.stack(np.meshgrid(rays.z,rays.x,indexing='ij'),axis=-1)).reshape(len(rays.z),len(rays.x)) for a in centre])
            section=np.concatenate([section[:,:,::-1],section],axis=2)[:,::-1]
            panels.append((section,col,image,phys))
            row=dict(case=case,experiment_ns=float(ns),numerical_ns=float(t[it]*1e9),shift_ns=90.)
            for name,a in [('copper_column',col),('thin_emission',image)]:
                row.update({f'{name}_{key}':v for key,v in metrics(a).items()})
                # Width enclosing 90% flux is independent of arbitrary weak-tail thresholds.
                cdf=np.cumsum(a.sum(0));cdf/=max(cdf[-1],1e-300)
                row[name+'_width90_um']=float((np.searchsorted(cdf,.95)-np.searchsorted(cdf,.05))*PIXEL)
            row['Tmax_K']=float(phys[0].max());row['ne_max_m3']=float(phys[1].max())
            row['copper_mass_export_kg']=float(volume_integral(phys[3]*np.clip(phys[2],0,1),r,z))
            row['line_photon_upper_estimate_10ns']=float(image.sum()*OMEGA*(PIXEL*1e-6)**2*10e-9*525e-9/(H*C))
            allrows.append(row)
        fig,axes=plt.subplots(6,len(PANELS),figsize=(23,15),layout='constrained')
        labels=['Cu mass density (kg/m3)','Gas temperature (K)','LTE electrons (m-3)','Local line emission (W/m3/sr)','Cu column (kg/m2)','Thin line radiance (W/m2/sr)']
        for row in range(6):
            values=[item[0][row] if row<4 else item[row-3] for item in panels]
            peak=max(float(a.max()) for a in values)
            if row==1:kwargs=dict(vmin=293,vmax=7000)
            else:kwargs=dict(norm=LogNorm(max(peak*1e-8,1e-300),max(peak,1e-299)))
            for k,(ns,a) in enumerate(zip(PANELS,values)):
                extent=(-len(rays.x)*PIXEL,len(rays.x)*PIXEL,0,len(rays.z)*PIXEL)
                im=axes[row,k].imshow(np.ma.masked_less_equal(a,0),extent=extent,cmap='inferno',**kwargs)
                axes[row,k].set_xlim(-400,400);axes[row,k].set_ylim(0,650)
                if row==0:axes[row,k].set_title(f'{ns:g} ns')
                if k==0:axes[row,k].set_ylabel(labels[row]+'\nz (um)')
                else:axes[row,k].set_yticklabels([])
                if row==5:axes[row,k].set_xlabel('x (um)')
                else:axes[row,k].set_xticklabels([])
            fig.colorbar(im,ax=axes[row,:],fraction=.014,pad=.005)
        fig.suptitle(f'{case}: material / temperature / emission / projection | fixed scale within each row | shift 90 ns (not calibrated)')
        fig.savefig(FIG/f'R2_{case}_shape_layers.png');plt.close(fig)
        print('SHAPE',case,flush=True)
    with (RES/'R2_shape_metrics.csv').open('w',newline='',encoding='utf-8') as file:
        w=csv.DictWriter(file,fieldnames=list(allrows[0]));w.writeheader();w.writerows(allrows)
    fig,axes=plt.subplots(1,3,figsize=(16,4.5),layout='constrained')
    for case in ('B64A','G64AC2','G264R1C5'):
        a=[x for x in allrows if x['case']==case]
        for ax,key in zip(axes,['copper_column_width90_um','thin_emission_width90_um','thin_emission_z_um']):
            values=np.array([x[key] for x in a],float)
            if key.startswith('thin_emission'):
                values=np.where([x['line_photon_upper_estimate_10ns']>=1 for x in a],values,np.nan)
            ax.plot(PANELS,values,'o-',label=case)
    for ax,title in zip(axes,['Copper width enclosing 90% mass','Line-emission width enclosing 90% flux','Line-emission centroid above target']):
        ax.set_title(title);ax.set_xlabel('Experimental delay with 90 ns convention (ns)');ax.set_ylabel('um');ax.grid(alpha=.2);ax.legend()
    fig.savefig(FIG/'R2_shape_mesh_comparison.png');plt.close(fig)
    write_json(RES/'R2_shape_summary.json',dict(shift_ns=90,spatial_registration='simulation target z=0; experiment target not registered',
        width_definition='central interval containing 90% of projected quantity; not camera threshold radius',cases=['B64A','G64AC2','G264R1C5']))

if __name__=='__main__':run()
