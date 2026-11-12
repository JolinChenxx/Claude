"""Late visible Cu line decomposition through the same formal LOS operator."""
import sys,csv
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from render_kinetics import physical_all
from transport import line_coefficients,line_transfer
from rate_tables import G
from numba import set_num_threads

def run(cases,ns=830.):
    set_num_threads(8);net=json.loads((ROOT/'data/network.json').read_text());widths=np.zeros(8);rows=[]
    for line in net['lines']:widths[line['up']]+=line['A']
    for case in cases:
        meta=json.loads((RES/f'{case}_fields.json').read_text());f=np.memmap(RES/f'{case}_fields.bin',dtype=meta['dtype'],shape=tuple(meta['shape']),mode='r')
        r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z']);ts=np.array(meta['times_s']);i=np.argmin(abs(ts*1e9-ns))
        a=physical_all(np.asarray(f[i],float),r,z);pop=np.maximum(a[11:19],0);rays=Rays(r,z);T=rays.sample(a[0]);vr=(rays.sample(a[5]).reshape(rays.shape)*rays.vproj[None,:,:]).reshape(T.shape)
        group=[]
        for line in net['lines']:
            if not 475<=line['wavelength_nm']<=525:continue
            lo,up=line['lo'],line['up'];j,alpha=line_coefficients(pop[lo],pop[up],G[lo],G[up],line['A'],line['nu'])
            rad,thin,tau=line_transfer(rays.sample(j),rays.sample(alpha),T,vr,rays.ds,line['nu'],widths[lo]+widths[up],MCU)
            im=blur(rays.image(rad))/(H*line['nu'])*OMEGA*(PIXEL*1e-6)**2*1e-8
            group.append(dict(case=case,numerical_ns=float(ts[i]*1e9),wavelength_air_nm=line['wavelength_nm'],instantaneous_10ns_equivalent_photons=float(im.sum())))
        total=sum(x['instantaneous_10ns_equivalent_photons'] for x in group)
        for row in group:row['Cu_line_fraction']=row['instantaneous_10ns_equivalent_photons']/max(total,1e-300)
        rows.extend(group)
    with (RES/'R2_late_spectral_fingerprint.csv').open('w',newline='',encoding='utf-8') as file:
        writer=csv.DictWriter(file,fieldnames=list(rows[0]));writer.writeheader();writer.writerows(rows)
    plt=setup_plot();fig,axs=plt.subplots(1,2,figsize=(13,4.7),layout='constrained');wl=[x['wavelength_air_nm'] for x in rows[:4]];x=np.arange(4);width=.8/len(cases)
    for k,case in enumerate(cases):
        group=[r for r in rows if r['case']==case];position=x+(k-(len(cases)-1)/2)*width
        axs[0].bar(position,[r['instantaneous_10ns_equivalent_photons'] for r in group],width,label=case)
        axs[1].bar(position,[r['Cu_line_fraction'] for r in group],width,label=case)
    axs[0].set_yscale('log');axs[0].set_ylabel('Ideal instantaneous 10 ns equivalent photons');axs[1].set_ylabel('Fraction of resolved Cu line photons')
    for ax in axs:ax.set_xticks(x,[f'{v:.2f}' for v in wl]);ax.set_xlabel('Air wavelength (nm)');ax.legend(fontsize=8);ax.grid(alpha=.2,axis='y')
    fig.suptitle(f'Late Cu spectrum at numerical {ns:g} ns (experimental {ns-90:g} ns convention) | same absorption + PSF | no gate integration')
    fig.savefig(FIG/'R2_late_spectral_fingerprint.png');plt.close(fig)
    write_json(RES/'R2_late_spectral_fingerprint_notes.json',dict(scope='Instantaneous rate times 10 ns, clearly distinct from integrated exposure data. Cu lines only; same corrected natural widths and formal self-absorption. Diagnostic of which lines carry the late signal, not a full predicted spectrum.'))
    print(rows)

if __name__=='__main__':run(sys.argv[1:] or ['R2_COUPLED','R2_COUPLED_Q'])
