"""Lagrangian history screening; spatial predictions come from COMSOL transport."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from rate_tables import ENERGIES,G,CHI,GION
from scipy.integrate import solve_ivp
import csv

class Rates:
    def __init__(self):
        self.net=json.loads((ROOT/'data/network.json').read_text());a=np.load(ROOT/'data/rates.npz')
        self.T=a['T'];self.names=self.net['functions'];self.values=np.array([a[k] for k in self.names]);self.idx={k:i for i,k in enumerate(self.names)}
    def at(self,T):
        T=np.clip(T,self.T[0],self.T[-1]);i=np.clip(np.searchsorted(self.T,T)-1,0,len(self.T)-2);w=(T-self.T[i])/(self.T[i+1]-self.T[i])
        return self.values[:,i]*(1-w)+self.values[:,i+1]*w

def sample_point(frame,r,z,pos):
    rr,zz=pos;ir=int(np.clip(np.searchsorted(r,rr)-1,0,len(r)-2));wr=(rr-r[ir])/(r[ir+1]-r[ir]);wr=np.clip(wr,0,1)
    physical_z=(frame[9,:,ir]*(1-wr)+frame[9,:,ir+1]*wr)*1e6
    iz=int(np.clip(np.searchsorted(physical_z,zz)-1,0,len(z)-2));wz=np.clip((zz-physical_z[iz])/(physical_z[iz+1]-physical_z[iz]),0,1)
    return ((frame[:,iz,ir]*(1-wr)+frame[:,iz,ir+1]*wr)*(1-wz)+(frame[:,iz+1,ir]*(1-wr)+frame[:,iz+1,ir+1]*wr)*wz)

def histories():
    f,r,z,t=fields();it=int(np.argmin(abs(t-120e-9)));first=np.array(f[it],float)
    copper=first[3]*np.clip(first[2],0,1)
    seeds=[np.unravel_index(np.argmax(first[0]),first[0].shape)]
    from legacy_radiation import Atomic
    atom=Atomic();jl,_=atom.emissivities(first[:9]);emission=sum(j for j,line in zip(jl,atom.lines) if 475<=line['wavelength_air_nm']<=525)
    edge=np.where(emission>.01*emission.max(),np.sqrt(r[None,:]**2+(first[9]*1e6)**2),-1)
    outer=np.unravel_index(np.argmax(edge),edge.shape)
    weights=copper*first[10]*r[None,:]*np.gradient(r)[None,:]*np.gradient(z)[:,None]
    centroid=[float((weights*r[None,:]).sum()/weights.sum()),float((weights*first[9]*1e6).sum()/weights.sum())]
    core=seeds[0];pos=np.array([[r[core[1]],first[9,core[0],core[1]]*1e6],centroid,[r[outer[1]],first[9,outer[0],outer[1]]*1e6]]);saved=[]
    ids=np.where((t>=119.999e-9)&(t<=900.001e-9))[0]
    for n,it in enumerate(ids):
        frame=np.array(f[it],float);vals=np.array([sample_point(frame,r,z,p) for p in pos]);saved.append(np.c_[pos.copy(),vals])
        if n+1<len(ids):
            dt=t[ids[n+1]]-t[it];pos+=vals[:,[5,6]]*dt*1e6;pos[:,0]=np.clip(pos[:,0],.05,649);pos[:,1]=np.clip(pos[:,1],-2.,999.)
    result=np.asarray(saved)
    np.savez(RES/'R2_material_histories.npz',times_s=t[ids],values=result,labels=['temperature_core','copper_mass_centroid','emission_edge_1pct'])
    return t[ids],result

def run():
    rates=Rates();t,hist=histories();rows=[];allcurves={};checks=[]
    for parcel in range(3):
      background=hist[:,parcel];ncu=background[:,5]*np.clip(background[:,4],0,1)/MCU
      # Physical compression inferred from the actual density on the material path.
      divu=-np.gradient(np.log(np.maximum(background[:,5],1e-30)),t)
      for twoT in (False,True):
       for quench in (0.,1.):
        init=background[0];Tg=init[2];ion=float(np.clip(init[6],0,1))
        w=G*np.exp(-ENERGIES*EV/(K*Tg));f0=np.r_[(1-ion)*w/w.sum(),ion]
        nair=max(init[3]-ncu[0]*ion,0.);u0=1.5*K*Tg*(ion+nair/max(ncu[0],1))
        def evaluate(time,state,details=False):
            b=np.array([np.interp(time,t,background[:,j]) for j in range(background.shape[1])]);Tg=b[2]
            N=max(b[5]*np.clip(b[4],0,1)/MCU,1.);nair=max(b[3]-N*np.clip(b[6],0,1),0.)
            pop=np.maximum(state[:8],0);ne=N*pop[7]+nair
            Te=max(1.,2*state[8]/(3*K*max(ne/N,1e-30))) if twoT else Tg
            vals=rates.at(Te);get=lambda name:vals[rates.idx[name]]
            dn=np.zeros(8);qc=0.;qrr=0.;phot=0.;rad=0.
            for pair in rates.net['pairs']:
                lo,up=pair['lo'],pair['up'];flow=ne*(get(pair['up_func'])*pop[lo]-get(pair['dn_func'])*pop[up])
                dn[lo]-=flow;dn[up]+=flow;qc+=flow*(ENERGIES[up]-ENERGIES[lo])*EV
            for i in range(7):
                flow=ne*get(f'ion{i}')*pop[i]-ne*ne*get(f'tbr{i}')*pop[7]
                rr=ne*get(f'rr{i}')*pop[7]
                dn[i]+=-flow+rr;dn[7]+=flow-rr;qc+=flow*(CHI-ENERGIES[i])*EV
                qrr+=ne*pop[7]*get(f'rrE{i}');rad+=rr*(CHI-ENERGIES[i])*EV+ne*pop[7]*get(f'rrE{i}')
            for line in rates.net['lines']:
                lo,up=line['lo'],line['up'];nu=line['nu'];A=line['A']
                tau=max(0,C*C*A/(8*np.pi*nu*nu)*N*(pop[lo]*G[up]/G[lo]-pop[up])*10e-6/(nu/C*np.sqrt(K*Tg/MCU)*np.sqrt(2*np.pi)))
                beta=-np.expm1(-tau)/tau if tau>1e-5 else 1-tau/2
                flow=A*beta*pop[up];dn[lo]+=flow;dn[up]-=flow;rad+=flow*H*nu
                if 475<=line['wavelength_nm']<=525:phot+=flow
            nn=max(b[5]*(1-np.clip(b[4],0,1))/(28.97*1.66053906660e-27),0.)
            kh=quench*np.pi*(3e-10)**2*np.sqrt(8*K*Tg/(np.pi*(MCU*28.97*1.66053906660e-27/(MCU+28.97*1.66053906660e-27))))
            for i in range(1,7):
                flow=nn*kh*(pop[i]-pop[0]*G[i]/G[0]*np.exp(-ENERGIES[i]*EV/(K*Tg)))
                dn[i]-=flow;dn[0]+=flow
            ven=np.sqrt(8*K*Te/(np.pi*ME));nuen=(nn+N*(1-pop[7]))*1e-19*ven
            lnl=max(2.,23-np.log(np.sqrt(max(ne*1e-6,1e-30))/max(K*Te/EV,1e-4)**1.5))
            nuei=2.91e-12*ne*lnl/max(K*Te/EV,1e-4)**1.5
            nue=2*ME/(28.97*1.66053906660e-27)*nuen+2*ME/MCU*nuei
            elastic=1.5*K*(ne/N)*nue*(Tg-Te)
            du=-2/3*state[8]*np.interp(time,t,divu)-qc-qrr+elastic
            if details:return Te,N,ne,phot*N,rad*N,1/max(nue,1e-300)
            return np.r_[dn,du]
        sol=solve_ivp(evaluate,(t[0],t[-1]),np.r_[f0,u0],method='BDF',t_eval=t,rtol=2e-5,atol=np.r_[np.full(8,1e-12),1e-25],max_step=2e-9)
        if not sol.success:raise RuntimeError(sol.message)
        key=f'p{parcel}_'+('2T' if twoT else '1T')+f'_q{quench:g}';allcurves[key]=sol.y
        checks.append(dict(case=key,maximum_fraction_sum_error=float(np.max(abs(sol.y[:8].sum(0)-1))),minimum_fraction=float(sol.y[:8].min())))
        for j,time in enumerate(t):
            Te,N,ne,phot,rad,tau=evaluate(time,sol.y[:,j],True)
            rows.append(dict(case=key,t_ns=float(time*1e9),parcel=parcel,twoT=twoT,quench_probability=quench,
                r_um=float(background[j,0]),z_um=float(background[j,1]),Tg_K=float(background[j,2]),Te_K=float(Te),
                nCu_m3=float(N),ne_m3=float(ne),ion_fraction=float(sol.y[7,j]),escaping_band_photons_m3_s=float(phot),
                escaping_power_W_m3=float(rad),electron_relaxation_ns=float(tau*1e9)))
        print('SCREEN',key,'Tend',rows[-1]['Te_K'],'xend',rows[-1]['ion_fraction'],flush=True)
    with (RES/'R2_kinetic_screen.csv').open('w',newline='',encoding='utf-8') as file:
        w=csv.DictWriter(file,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
    np.savez_compressed(RES/'R2_kinetic_screen.npz',times_s=t,**allcurves)
    write_json(RES/'R2_kinetic_screen_checks.json',dict(checks=checks,scope='material-history screening; no fresh copper injection or diffusion; no laser heating after initialization; spatial predictions require PDE results',
        initial_condition='LTE populations at numerical 120 ns, an optimistic inherited ion/energy reservoir, not an independently modeled laser breakdown'))
    plt=setup_plot();fig,axes=plt.subplots(2,3,figsize=(16,8),layout='constrained')
    for key in allcurves:
        if not key.startswith('p0_'):continue
        group=[x for x in rows if x['case']==key]
        for ax,field in zip(axes.flat,['Te_K','ion_fraction','escaping_band_photons_m3_s','electron_relaxation_ns','nCu_m3','escaping_power_W_m3']):
            ax.semilogy([x['t_ns']-90 for x in group],[max(x[field],1e-100) for x in group],label=key)
    for ax,title in zip(axes.flat,['Electron temperature (K)','Cu ion fraction','Escaping band photons / m3 / s','Electron thermal relaxation (ns)','Cu number density / m3','Escaping power / m3']):
        ax.set_title(title);ax.set_xlabel('Experimental delay, shift 90 ns (ns)');ax.grid(alpha=.2);ax.legend(fontsize=7)
    fig.suptitle('Material-history screening, core parcel | inherited LTE state at numerical 120 ns | no spatial-image claim')
    fig.savefig(FIG/'R2_kinetic_screen.png');plt.close(fig)

if __name__=='__main__':run()
