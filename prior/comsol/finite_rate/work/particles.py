"""Mass- and energy-constrained Cu particle radiation, independent temperature."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from scipy.special import spherical_jn,spherical_yn
from scipy.integrate import solve_ivp
from scipy.interpolate import RegularGridInterpolator
import csv

RHO=8940.;LV=5.3e6;MASS_CAP=1.871e-12

def optical_constants(lam):
    rows=[]
    for line in (ROOT/'data/references/Cu_Johnson.yml').read_text().splitlines():
        try:
            vals=[float(x) for x in line.split()]
            if len(vals)==3:rows.append(vals)
        except ValueError:pass
    tab=np.array(rows);wl=lam*1e6
    n=np.interp(wl,tab[:,0],tab[:,1]);k=np.interp(wl,tab[:,0],tab[:,2]);eps=(n+1j*k)**2
    end=(tab[-1,1]+1j*tab[-1,2])**2;omega0=2*np.pi*C/(tab[-1,0]*1e-6)
    gamma=end.imag/(1-end.real)*omega0;wp2=(1-end.real)*(omega0**2+gamma**2)
    omega=2*np.pi*C/lam
    eps=np.where(wl>tab[-1,0],1-wp2/(omega*(omega+1j*gamma)),eps)
    return np.sqrt(eps)

def cross_section(radius,lam):
    m=optical_constants(lam);x=2*np.pi*radius/lam;out=np.zeros_like(x)
    for i,xx in enumerate(x):
        if xx<1e-3:
            eps=m[i]**2;qabs=4*xx*((eps-1)/(eps+2)).imag
        else:
            n=np.arange(1,int(np.ceil(xx+4*xx**(1/3)+2))+1)
            psi=xx*spherical_jn(n,xx);dp=spherical_jn(n,xx)+xx*spherical_jn(n,xx,True)
            chi=xx*(spherical_jn(n,xx)+1j*spherical_yn(n,xx))
            dc=spherical_jn(n,xx)+1j*spherical_yn(n,xx)+xx*(spherical_jn(n,xx,True)+1j*spherical_yn(n,xx,True))
            z=m[i]*xx;pm=z*spherical_jn(n,z);dm=spherical_jn(n,z)+z*spherical_jn(n,z,True)
            an=(m[i]*pm*dp-psi*dm)/(m[i]*pm*dc-chi*dm)
            bn=(pm*dp-m[i]*psi*dm)/(pm*dc-m[i]*chi*dm)
            qext=2/xx**2*np.sum((2*n+1)*(an+bn).real)
            qsca=2/xx**2*np.sum((2*n+1)*(abs(an)**2+abs(bn)**2))
            qabs=max(qext-qsca,0.)
        out[i]=np.pi*radius**2*qabs
    return out

def enthalpy(T):
    T=np.asarray(T)
    return np.where(T<1346,385*(T-293.15),np.where(T<1366,385*(1346-293.15)+9800*(T-1346),385*(1346-293.15)+196000+572*(T-1366)))

def tables():
    radii=np.geomspace(1e-9,120e-9,56);temps=np.linspace(293.15,4500,180)
    lam=np.unique(np.r_[np.geomspace(.1879e-6,100e-6,650),np.linspace(450e-9,550e-9,101)])
    B=2*H*C*C/lam[None,:]**5/np.expm1(np.minimum(H*C/(lam[None,:]*K*temps[:,None]),700))
    allP=[];allN=[]
    band=(lam>=475e-9)&(lam<=525e-9)
    for radius in radii:
        spectrum=4*np.pi*cross_section(radius,lam)[None,:]*B
        allP.append(np.trapz(spectrum,x=lam,axis=1))
        allN.append(np.trapz(spectrum[:,band]*lam[None,band]/(H*C),x=lam[band],axis=1))
    power=RegularGridInterpolator((radii,temps),np.array(allP),bounds_error=False,fill_value=None)
    photons=RegularGridInterpolator((radii,temps),np.array(allN),bounds_error=False,fill_value=None)
    np.savez(RES/'R2_particle_optical_tables.npz',radius_m=radii,T_K=temps,power_W=allP,band_photons_s=allN)
    return power,photons

def run():
    power,photons=tables();plt=setup_plot()
    # Gas peak is an intentionally favorable heating history, not a particle trajectory.
    f,r,z,t=fields();sel=np.arange(0,len(t),5);peak=np.array([np.max(f[i,0]) for i in sel]);tt=t[sel]
    formation=240e-9;end=900e-9;outtimes=np.linspace(formation,end,331)
    rows=[];curves={};energy_errors=[]
    for a0 in (10e-9,30e-9,100e-9):
      for T0 in (1800.,2800.):
       for bath in ('ambient','gas_peak'):
        m0=4*np.pi/3*a0**3*RHO
        def rhs(time,y):
            mf,Tp=y[:2];mass=max(mf,1e-5)*m0;a=a0*max(mf,1e-5)**(1/3)
            Tg=293.15 if bath=='ambient' else max(293.15,float(np.interp(time,tt,peak)))
            Tp=max(Tp,293.15);pr=float(power([[a,min(Tp,4500)]])[0])
            ng=101325/(K*Tg);vg=np.sqrt(8*K*Tg/(np.pi*28.97*1.66053906660e-27))
            cfm=2.5*K*ng*vg*np.pi*a*a;cc=4*np.pi*a*(.025+.00002*max(Tg-293.15,0))
            qgas=(Tp-Tg)/(1/cfm+1/cc)
            psat=101325*np.exp(MCU*LV/K*(1/2840-1/Tp))
            loss=4*np.pi*a*a*psat*np.sqrt(MCU/(2*np.pi*K*Tp))
            cp=385. if Tp<1346 else (9800. if Tp<1366 else 572.)
            return [-loss/m0,(-pr-qgas-LV*loss)/(mass*cp),pr/m0,qgas/m0,(LV+float(enthalpy(Tp)))*loss/m0]
        def vanish(time,y):return y[0]-.01
        vanish.terminal=True;vanish.direction=-1
        sol=solve_ivp(rhs,(formation,end),[1,T0,0,0,0],rtol=2e-6,atol=[1e-9,1e-3,.01,.01,.01],events=vanish,max_step=2e-9,dense_output=True)
        valid=outtimes<=sol.t[-1];y=sol.sol(outtimes[valid]);mf,Tp=y[:2]
        rate=np.maximum(photons(np.column_stack([a0*np.maximum(mf,1e-5)**(1/3),np.maximum(Tp,293.15)])),0.)
        # Per kg of initial condensed Cu; evaporated particles shrink naturally.
        collected=rate/m0*MASS_CAP*OMEGA/(4*np.pi)*10e-9
        key=f'a{a0*1e9:g}_T{T0:g}_{bath}';curves[key]=(outtimes[valid]*1e9-90,Tp,collected,mf)
        balance=mf*enthalpy(Tp)-enthalpy(T0)+y[2]+y[3]+y[4]
        err=float(np.max(abs(balance))/max(abs(float(enthalpy(T0))),1));energy_errors.append(err)
        for time,temp,n,frac in zip(outtimes[valid],Tp,collected,mf):
            rows.append(dict(case=key,experiment_ns=float(time*1e9-90),radius_initial_nm=a0*1e9,T_initial_K=T0,bath=bath,
                T_particle_K=float(temp),remaining_mass_fraction=float(frac),ideal_collected_photons_10ns_at_mass_cap=float(n),mass_cap_kg=MASS_CAP))
    with (RES/'R2_particle_cooling.csv').open('w',newline='',encoding='utf-8') as file:
        w=csv.DictWriter(file,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
    fig,axes=plt.subplots(1,3,figsize=(17,5),layout='constrained')
    for key,(time,Tp,n,mf) in curves.items():
        if '_T2800_' not in key:continue
        axes[0].plot(time,Tp,label=key);axes[1].semilogy(time,np.maximum(n,1e-30),label=key)
    temp=np.linspace(1000,3500,201);massrows=[]
    for a in (10e-9,30e-9,100e-9):
        m=4*np.pi/3*a**3*RHO
        n=np.maximum(photons(np.column_stack([np.full_like(temp,a),temp])),1e-300)/m*OMEGA/(4*np.pi)*10e-9
        mass1=1/n
        axes[2].semilogy(temp,mass1*1e12,label=f'{a*1e9:g} nm, 1 photon')
        axes[2].semilogy(temp,2500*mass1*1e12,ls='--',label=f'{a*1e9:g} nm, 2500 photons')
        massrows.extend([dict(radius_nm=a*1e9,T_K=float(T),mass_for_1_photon_kg=float(mm),mass_for_2500_photons_kg=float(2500*mm)) for T,mm in zip(temp,mass1)])
    axes[1].axhline(1,color='black',ls=':');axes[1].axhline(2500,color='gray',ls='--',label='1 photon in each of 2500 pixels')
    axes[2].axhline(MASS_CAP*1e12,color='black',ls=':',label='Existing exported Cu: 1.871 ng')
    for ax,title,xlabel,ylabel in zip(axes,['Independent particle cooling','Ideal collected light: all 1.871 ng condensed','Mass needed for detectable-scale photons'],
        ['Experimental delay, shift 90 ns (ns)','Experimental delay, shift 90 ns (ns)','Particle temperature (K)'],['K','photons / 10 ns','initial condensed mass (ng)']):
        ax.set_title(title);ax.set_xlabel(xlabel);ax.set_ylabel(ylabel);ax.grid(alpha=.2);ax.legend(fontsize=6)
    axes[1].set_ylim(1e-6,1e7);axes[2].set_ylim(1e-6,1e9)
    fig.suptitle('Conditional particle contribution | formation at 150 ns, not measured | 475-525 nm, NA 0.4, ideal efficiency')
    fig.savefig(FIG/'R2_particle_budget.png');plt.close(fig)
    with (RES/'R2_particle_required_mass.csv').open('w',newline='',encoding='utf-8') as file:
        w=csv.DictWriter(file,fieldnames=list(massrows[0]));w.writeheader();w.writerows(massrows)
    write_json(RES/'R2_particle_summary.json',dict(status='conditional_feasibility_bound',mass_cap_kg=MASS_CAP,
        formation_experiment_ns=150,initial_temperatures_K=[1800,2800],radii_nm=[10,30,100],
        optical_model='Mie spheres, Johnson-Christy 1972 room-temperature Cu; matched Drude continuation beyond 1.937 um',
        heat_model='independent temperature; gas-collision/conduction resistances; radiation; evaporation; melting enthalpy',
        largest_energy_balance_relative_residual=max(energy_errors),
        limitations=['Particle formation and initial temperature are conditional, not predicted.',
            'Gas-peak bath is a favorable bound, not a spatial particle trajectory.',
            'Room-temperature optical constants approximate hot liquid particles.',
            'Mass cap is existing vapor-model Cu, not a bound on unmodeled molten spatter.',
            '2500 photons is a diagnostic image occupancy scale, not calibrated camera detection.'],
        scattering='No persistent 515 nm probe illumination is established for S4 plume-emission frames; scattering excluded from the emission prediction.'))
    print('PARTICLES energy balance maximum relative residual',max(energy_errors),flush=True)

if __name__=='__main__':run()
