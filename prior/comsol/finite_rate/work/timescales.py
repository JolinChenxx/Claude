import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from kinetic_screen import Rates
import csv

def run():
    hist=np.load(RES/'R2_material_histories.npz');t=hist['times_s'];h=hist['values'];rates=Rates();rows=[]
    plt=setup_plot();fig,axs=plt.subplots(1,3,figsize=(17,5),layout='constrained')
    for p,name in enumerate(hist['labels']):
        b=h[:,p];T=b[:,2];ne=b[:,3];rho=b[:,5];ion=b[:,6];nCu=rho*np.clip(b[:,4],0,1)/MCU
        cooling=1/np.maximum(abs(np.gradient(np.log(T),t)),1e-30)
        expand=1/np.maximum(abs(np.gradient(np.log(rho),t)),1e-30)
        values=np.array([rates.at(temp) for temp in T]);get=lambda key:values[:,rates.idx[key]]
        iontime=1/np.maximum(ne*get('ion0'),1e-300)
        rrtime=1/np.maximum(ne*sum(get(f'rr{i}') for i in range(7)),1e-300)
        tbr=1/np.maximum(ne*ne*sum(get(f'tbr{i}') for i in range(7)),1e-300)
        pair=next(a for a in rates.net['pairs'] if a['lo']==0 and a['up']==4)
        excite=1/np.maximum(ne*get(pair['up_func']),1e-300)
        air=np.maximum(rho*(1-b[:,4]),0)/(28.97*1.66053906660e-27);ven=np.sqrt(8*K*T/(np.pi*ME))
        ln=np.maximum(2,23-np.log(np.sqrt(np.maximum(ne*1e-6,1e-30))/np.maximum(K*T/EV,1e-4)**1.5))
        nen=(air+nCu*(1-ion))*1e-19*ven;nei=2.91e-12*ne*ln/np.maximum(K*T/EV,1e-4)**1.5
        elastic=1/np.maximum(2*ME/(28.97*1.66053906660e-27)*nen+2*ME/MCU*nei,1e-300)
        heavy=1/np.maximum(air*np.pi*(3e-10)**2*np.sqrt(8*K*T/(np.pi*(MCU*28.97*1.66053906660e-27/(MCU+28.97*1.66053906660e-27)))),1e-300)
        times=dict(cooling=cooling,expansion=expand,ground_excitation=excite,ground_ionization=iontime,radiative_recombination=rrtime,three_body_recombination=tbr,electron_heavy_exchange=elastic,geometric_air_quench=heavy)
        for key,val in times.items():axs[p].semilogy(t*1e9-90,np.clip(val*1e9,1e-3,1e12),label=key)
        axs[p].set(title=str(name),xlabel='Experimental delay, shift 90 ns (ns)',ylabel='Time scale (ns)',ylim=(1e-3,1e8));axs[p].grid(alpha=.2);axs[p].axhline(100,color='gray',ls=':')
        for i,tt in enumerate(t):rows.append(dict(parcel=str(name),numerical_ns=float(tt*1e9),T_K=float(T[i]),ne_m3=float(ne[i]),**{k+'_s':float(v[i]) for k,v in times.items()}))
    axs[-1].legend(fontsize=6);fig.suptitle('Inherited LTE material histories: rate scale comparison | geometric quenching is an upper-rate bound')
    fig.savefig(FIG/'R2_timescales.png');plt.close(fig)
    with (RES/'R2_timescales.csv').open('w',newline='',encoding='utf-8') as f:
        w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)

if __name__=='__main__':run()
