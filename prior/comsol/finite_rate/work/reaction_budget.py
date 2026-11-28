"""Volume-integrated sources of visible upper levels, separated by reaction."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from kinetic_screen import Rates
from rate_tables import G
import csv

def run(case,quench=0.,trapping=True):
    meta=json.loads((RES/f'{case}_fields.json').read_text());f=np.memmap(RES/f'{case}_fields.bin',dtype=meta['dtype'],shape=tuple(meta['shape']),mode='r');r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z']);ts=np.array(meta['times_s']);rates=Rates();rows=[]
    for ns in [120,160,240,350,500,700,900]:
        if ns>ts[-1]*1e9+.01:continue
        k=np.argmin(abs(ts*1e9-ns));a=np.array(f[k],float);T=a[0];Te=np.maximum(a[19],1);ne=a[1];n=np.maximum(a[11:19],0);jac=a[10];L=a[28]
        vals=np.array([np.interp(Te.ravel(),rates.T,v).reshape(Te.shape) for v in rates.values]);get=lambda name:vals[rates.idx[name]]
        for level in [4,5,6]:
            src={key:np.zeros_like(T) for key in ['collisional_excitation','collisional_deexcitation_in','radiative_recombination','three_body_recombination','radiative_cascade_in','radiative_loss','ionization_loss','collisional_loss','heavy_quench_loss','heavy_thermal_excitation']}
            mair=28.97*1.66053906660e-27
            nair=np.maximum(a[3]-a[3]*np.clip(a[2],0,1),0)/mair
            kheavy=quench*np.pi*(3e-10)**2*np.sqrt(8*K*np.maximum(T,1)/(np.pi*(MCU*mair/(MCU+mair))))
            src['heavy_quench_loss']=kheavy*nair*n[level]
            src['heavy_thermal_excitation']=kheavy*nair*n[0]*G[level]/G[0]*np.exp(-rates.net['energies_eV'][level]*EV/(K*np.maximum(T,1)))
            for pair in rates.net['pairs']:
                lo,up=pair['lo'],pair['up']
                if up==level:src['collisional_excitation']+=ne*n[lo]*get(pair['up_func']);src['collisional_loss']+=ne*n[up]*get(pair['dn_func'])
                if lo==level:src['collisional_deexcitation_in']+=ne*n[up]*get(pair['dn_func']);src['collisional_loss']+=ne*n[lo]*get(pair['up_func'])
            src['radiative_recombination']=ne*n[7]*get(f'rr{level}');src['three_body_recombination']=ne**2*n[7]*get(f'tbr{level}');src['ionization_loss']=ne*n[level]*get(f'ion{level}')
            for line in rates.net['lines']:
                lo,up=line['lo'],line['up'];nu=line['nu'];A=line['A']
                tau=np.maximum(0,C*C*A/(8*np.pi*nu*nu)*(n[lo]*G[up]/G[lo]-n[up])*L/(nu/C*np.sqrt(K*T/MCU)*np.sqrt(2*np.pi)))
                beta=np.where(tau<1e-4,1-tau/2,(-np.expm1(-tau))/np.maximum(tau,1e-300)) if trapping else np.ones_like(tau);rate=A*beta*n[up]
                if up==level:src['radiative_loss']+=rate
                if lo==level:src['radiative_cascade_in']+=rate
            totaln=float(volume_integral(n[level],r,z,jac));loss=float(volume_integral(src['radiative_loss'],r,z,jac))
            row=dict(numerical_ns=float(ts[k]*1e9),level=level,population=totaln,effective_radiative_lifetime_ns=totaln/max(loss,1e-300)*1e9,**{key+'_s':float(volume_integral(v,r,z,jac)) for key,v in src.items()});rows.append(row)
    with (RES/f'{case}_reaction_budget.csv').open('w',newline='',encoding='utf-8') as file:
        w=csv.DictWriter(file,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
    plt=setup_plot();fig,axs=plt.subplots(1,3,figsize=(16,4.8),layout='constrained')
    for lev in [4,5,6]:
        rr=[r for r in rows if r['level']==lev];t=np.array([r['numerical_ns'] for r in rr])-90
        axs[0].semilogy(t,[r['population'] for r in rr],'o-',label=f'level {lev}')
        axs[1].semilogy(t,[r['effective_radiative_lifetime_ns'] for r in rr],'o-',label=f'level {lev}')
    rr=[r for r in rows if r['level']==4];tn=np.array([r['numerical_ns'] for r in rr])-90
    for key in ['collisional_excitation','radiative_recombination','three_body_recombination','radiative_cascade_in','radiative_loss','heavy_quench_loss']:
        axs[2].semilogy(tn,np.maximum([r[key+'_s'] for r in rr],1e-20),'o-',label=key)
    axs[2].set_ylim(1e7,1e19)
    for ax,title,y in zip(axs,['Visible upper-state populations','Local escape-model lifetime','4p upper state feeding and escape'],['Cu atoms','ns','events / s']):ax.set(title=title,xlabel='Experimental delay, shift 90 ns (ns)',ylabel=y);ax.grid(alpha=.2);ax.legend(fontsize=6)
    fig.suptitle(case+' | level 4: 4p, feeds 510.6 nm; levels 5/6: upper green-line levels | '+('escape-length approximation' if trapping else 'untrapped radiative-loss bound'))
    fig.savefig(FIG/f'{case}_reaction_budget.png');plt.close(fig)
    print(rows[-3:])

if __name__=='__main__':run(sys.argv[1],float(sys.argv[2]) if len(sys.argv)>2 else 0.,bool(int(sys.argv[3])) if len(sys.argv)>3 else True)
