"""Compare local escape closure with directional formal transfer.

The side-view value multiplied by 4 pi is an isotropic-equivalent luminosity,
not an angular integral. A large mismatch flags the escape-length approximation.
"""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent))
from common import *
from rate_tables import G
import csv

def run(case):
    meta=json.loads((RES/f'{case}_fields.json').read_text())
    f=np.memmap(RES/f'{case}_fields.bin',dtype=meta['dtype'],shape=tuple(meta['shape']),mode='r')
    r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z'])
    net=json.loads((ROOT/'data/network.json').read_text());trapping=meta.get('model_parameters',{}).get('trapping',1)>.5
    folder=RES/f'{case}_radiation';progress=json.loads((folder/'progress.json').read_text())
    ids=progress['ids'][:progress['finished']]
    photons=np.load(folder/'line_photon_radiance.npy',mmap_mode='r')
    rows=[]
    for k,it in enumerate(ids):
        a=np.asarray(f[it],float);n=np.maximum(a[11:19],0);T=np.maximum(a[0],1);jac=a[10];length=a[28]
        thin=0.;escaped=0.
        for line in net['lines']:
            if not 475<=line['wavelength_nm']<=525:continue
            lo,up=line['lo'],line['up'];nu,A=line['nu'],line['A']
            tau=np.maximum(0,C*C*A/(8*np.pi*nu*nu)*(n[lo]*G[up]/G[lo]-n[up])*length/(nu/C*np.sqrt(K*T/MCU)*np.sqrt(2*np.pi)))
            beta=np.where(tau<1e-4,1-tau/2,-np.expm1(-tau)/np.maximum(tau,1e-300)) if trapping else np.ones_like(tau)
            thin+=float(volume_integral(A*n[up],r,z,jac))
            escaped+=float(volume_integral(A*n[up]*beta,r,z,jac))
        side=float(photons[k,0].sum())*(PIXEL*1e-6)**2*4*np.pi
        rows.append(dict(numerical_ns=meta['times_s'][it]*1e9,Cu_thin_photons_s=thin,Cu_local_escape_photons_s=escaped,
                         side_view_isotropic_equivalent_line_photons_s=side,side_to_local_escape=side/max(escaped,1e-300)))
    with (RES/f'{case}_escape_comparison.csv').open('w',newline='',encoding='utf-8') as file:
        w=csv.DictWriter(file,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
    plt=setup_plot();fig,axs=plt.subplots(1,2,figsize=(12,4.5),layout='constrained');ts=np.array([x['numerical_ns'] for x in rows])-90
    for key,label in [('Cu_thin_photons_s','Cu local emission, no attenuation'),('Cu_local_escape_photons_s','Cu local escape closure'),('side_view_isotropic_equivalent_line_photons_s','4 pi x side-view line radiance integral')]:
        axs[0].semilogy(ts,[x[key] for x in rows],label=label)
    axs[1].semilogy(ts,[x['side_to_local_escape'] for x in rows]);axs[1].axhline(1,color='gray',ls=':')
    axs[0].set(ylabel='Photons / s',title='475-525 nm line emission and escape');axs[0].legend(fontsize=7)
    axs[1].set(ylabel='Directional equivalent / local escape',title='Closure consistency diagnostic')
    for ax in axs:ax.set_xlabel('Experimental delay, shift 90 ns (ns)');ax.grid(alpha=.2)
    fig.suptitle(case+' | side view is NOT an angular integral; includes LTE air lines (negligible late)')
    fig.savefig(FIG/f'{case}_escape_comparison.png');plt.close(fig)
    write_json(RES/f'{case}_escape_comparison_notes.json',dict(last_numerical_ns=rows[-1]['numerical_ns'],
        late_ratios=[x for x in rows if x['numerical_ns']>=240][::20],
        interpretation='Directional anisotropy, grid projection, line profile and approximate local escape length all affect this ratio. It is not a strict global radiative energy residual.'))
    print(rows[-1])

if __name__=='__main__':run(sys.argv[1])
