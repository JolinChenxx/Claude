"""Independent conservative finite-volume check of the COMSOL axial EOS model.

HLLC flux resolves the resting copper-air contact; explicit conduction and
tabulated energy inversion avoid logarithmic trace-tail stiffness. This is a
material/numerical cross-check, NOT a native Plasma solution or S4 image.
"""
from pathlib import Path
import json,time,sys
import numpy as np
from numba import njit
R=Path(__file__).resolve().parent

@njit
def interp(T,rho,lgT,lgR,tab):
    lt=np.log10(max(T,200.));lr=np.log10(max(rho,1e-10))
    i=min(max(np.searchsorted(lgT,lt)-1,0),len(lgT)-2);j=min(max(np.searchsorted(lgR,lr)-1,0),len(lgR)-2)
    a=min(1.,max(0.,(lt-lgT[i])/(lgT[i+1]-lgT[i])));b=min(1.,max(0.,(lr-lgR[j])/(lgR[j+1]-lgR[j])))
    return (1-a)*((1-b)*tab[i,j]+b*tab[i,j+1])+a*((1-b)*tab[i+1,j]+b*tab[i+1,j+1])

@njit
def thermo(U,lgT,lgR,tab):
    nc=U.shape[0];W=np.zeros((nc,8))
    for i in range(nc):
        rc=max(U[i,0],0.);ra=max(U[i,1],0.);rho=rc+ra;u=U[i,2]/rho;target=(U[i,3]-.5*rho*u*u)
        lr=np.log10(max(rc,1e-10));j=min(max(np.searchsorted(lgR,lr)-1,0),len(lgR)-2);b=min(1.,max(0.,(lr-lgR[j])/(lgR[j+1]-lgR[j])))
        lo=0;hi=len(lgT)-1
        while hi-lo>1:
            k=(lo+hi)//2;en=rc*((1-b)*tab[k,j,1]+b*tab[k,j+1,1])+ra*717.5*10**lgT[k]
            if en<target:lo=k
            else:hi=k
        el=(1-b)*tab[lo,j,1]+b*tab[lo,j+1,1];eh=(1-b)*tab[hi,j,1]+b*tab[hi,j+1,1]
        delta=lgT[hi]-lgT[lo];aa=.5
        for k in range(5):
            T=10**(lgT[lo]+aa*delta);en=rc*(el+aa*(eh-el))+ra*717.5*T
            den=rc*(eh-el)+ra*717.5*T*np.log(10.)*delta;aa=min(1.,max(0.,aa-(en-target)/max(den,1e-30)))
        T=10**(lgT[lo]+aa*delta);q=interp(T,rc,lgT,lgR,tab);ag=min(1.,max(1e-12,q[5])) if rc>1e-12 else 1.
        p=max(q[0],0.) if rc>1e-12 else 0.;p+=ra*287*T/ag
        cs=max(q[3],np.sqrt(1.4*287*T)*ag);fl=.5*(1+(T-1358)/np.sqrt((T-1358)**2+100))
        kappa=(1-ag)*(385*(1-fl)+165*fl)+ag*.026*(T/300)**.7
        W[i]=np.array([T,p,cs,u,kappa,q[4],ag,rc*q[2]+ra*717.5])
    return W

@njit
def hllc(UL,UR,WL,WR):
    rl=UL[0]+UL[1];rr=UR[0]+UR[1];ul=WL[3];ur=WR[3];pl=WL[1];pr=WR[1]
    sl=min(ul-WL[2],ur-WR[2]);sr=max(ul+WL[2],ur+WR[2])
    fl=UL*ul;fr=UR*ur;fl[2]+=pl;fr[2]+=pr;fl[3]+=pl*ul;fr[3]+=pr*ur
    if sl>=0:return fl
    if sr<=0:return fr
    den=rl*(sl-ul)-rr*(sr-ur);sm=(pr-pl+rl*ul*(sl-ul)-rr*ur*(sr-ur))/den
    if sm>=0:
        fact=(sl-ul)/(sl-sm);us=UL*fact;us[2]=rl*fact*sm;us[3]=rl*fact*(UL[3]/rl+(sm-ul)*(sm+pl/(rl*(sl-ul))))
        return fl+sl*(us-UL)
    fact=(sr-ur)/(sr-sm);us=UR*fact;us[2]=rr*fact*sm;us[3]=rr*fact*(UR[3]/rr+(sm-ur)*(sm+pr/(rr*(sr-ur))))
    return fr+sr*(us-UR)

@njit
def advance(U,x,dx,t,end,lgT,lgR,tab,absorb,max_steps):
    n=len(x);absorbed=0.;outenergy=0.;outmass=0.;steps=0;bad=0
    while t<end and steps<max_steps:
        W=thermo(U,lgT,lgR,tab)
        dt=end-t
        for i in range(n):
            dt=min(dt,.35*dx[i]/(abs(W[i,3])+W[i,2]),.35*W[i,7]*dx[i]**2/max(W[i,4],1e-12))
        F=np.zeros((n+1,4));F[0,2]=W[0,1];F[n]=U[-1]*W[-1,3];F[n,2]+=W[-1,1];F[n,3]+=W[-1,1]*W[-1,3]
        for i in range(1,n):
            F[i]=hllc(U[i-1],U[i],W[i-1],W[i]);km=2*W[i-1,4]*W[i,4]/(W[i-1,4]+W[i,4]);F[i,3]-=km*(W[i,0]-W[i-1,0])/(x[i]-x[i-1])
        Q=np.zeros(n);light=2*400e-6/(np.pi*(11e-6)**2)*np.sqrt(4*np.log(2)/np.pi)/60e-9*np.exp(-4*np.log(2)*((t+dt/2-120e-9)/60e-9)**2)
        for i in range(n-1,-1,-1):
            hot=.5*(1+(W[i,0]-9030.317)/np.sqrt((W[i,0]-9030.317)**2+10000))
            dense=min(1.,1-W[i,6]+hot*U[i,0]**2/(U[i,0]**2+1917.4313**2));alpha=dense/20e-9
            removed=light*(-np.expm1(-alpha*dx[i]));Q[i]=absorb*removed/dx[i];light-=removed
        for i in range(n):
            U[i]-=dt*(F[i+1]-F[i])/dx[i];U[i,3]+=dt*Q[i]
            sf=(1-W[i,5])*(.5*(1-(W[i,0]-1358)/np.sqrt((W[i,0]-1358)**2+100)))
            U[i,2]/=(1+dt*1e10*sf*sf)
            if U[i,0]<-1e-8 or U[i,1]<-1e-8:bad+=1
            if U[i,0]<0:U[i,0]=0
            if U[i,1]<0:U[i,1]=0
            absorbed+=dt*Q[i]*dx[i]
        outenergy+=dt*(F[n,3]-F[0,3]);outmass+=dt*(F[n,0]-F[0,0]);t+=dt;steps+=1
        if bad:break
    return U,t,absorbed,outenergy,outmass,steps,bad

def main():
    case=sys.argv[1] if len(sys.argv)>1 else 'HF301';end=float(sys.argv[2]) if len(sys.argv)>2 else 100.;absorb=float(sys.argv[3]) if len(sys.argv)>3 else .1
    out=R/'results'/case;out.mkdir(exist_ok=True)
    d=np.load(R/'data/Cu_HEM_20261110/eos.npz');T=d['temperatures_K'];rho=d['densities_kg_m3'];tab=np.ascontiguousarray(d['table'][:,2:].reshape(len(T),len(rho),7));lgT=np.log10(T);lgR=np.log10(rho)
    edges=np.unique(np.r_[np.linspace(-100,-5,81),np.linspace(-5,0,251),np.linspace(0,10,101),np.geomspace(10,1500,100)])*1e-6;x=(edges[:-1]+edges[1:])/2;dx=np.diff(edges)
    U=np.zeros((len(x),4));U[:,0]=np.where(x<0,8942.230330721954,0);U[:,1]=np.where(x>=0,101325/(287*293.15),0)
    for i in range(len(x)):U[i,3]=U[i,0]*interp(293.15,U[i,0],lgT,lgR,tab)[1]+U[i,1]*717.5*293.15
    initial_mass=(U[:,0]*dx).sum();initial_energy=(U[:,3]*dx).sum();states=[U.copy()];times=[0.];ws=[thermo(U,lgT,lgR,tab)];t=0.;ea=eo=mo=0.;steps=0;start=time.time()
    for stop in np.arange(2,end+1e-6,2):
        U,t,da,de,dm,st,bad=advance(U,x,dx,t,stop*1e-9,lgT,lgR,tab,absorb,1000000);ea+=da;eo+=de;mo+=dm;steps+=st
        w=thermo(U,lgT,lgR,tab);states.append(U.copy());times.append(t);ws.append(w)
        report={'case':case,'time_ns':t*1e9,'steps':steps,'seconds':time.time()-start,'Tmax_K':float(w[:,0].max()),'speedmax_m_s':float(np.abs(w[:,3]).max()),'absorbed_J_m2':ea,'energy_residual_J_m2':float((U[:,3]*dx).sum()-initial_energy-ea+eo),'mass_residual_kg_m2':float((U[:,0]*dx).sum()-initial_mass+mo),'negative_density_events':bad,'native_plasma_solved':False,'solver':'independent Python/Numba HLLC material check; not COMSOL'}
        (out/'status.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
        np.savez_compressed(out/'states.npz',times_s=times,x_m=x,dx_m=dx,U=np.asarray(states),W=np.asarray(ws))
        if bad:raise RuntimeError('Negative partial density; material check rejected')
if __name__=='__main__':main()
