"""Isobaric additive-volume copper/air closure for the independent FV audit.

Solve p_Cu(T,rho_intrinsic)*(1-rho_Cu/rho_intrinsic)=rho_air*R_air*T.
Use monotone density brackets, so a Newton iterate cannot leave the EOS table.
No plasma, ionization, fitted mass injection or camera emissivity is included.
"""
import numpy as np
from numba import njit

@njit
def mixture(T,rc,ra,lgT,lgR,tab):
    lt=np.log10(T)
    it=min(max(np.searchsorted(lgT,lt)-1,0),len(lgT)-2)
    a=(lt-lgT[it])/(lgT[it+1]-lgT[it])
    if rc<=0:
        return ra*717.5*T,ra*287*T,0.,1.,np.sqrt(1.4*287*T),1.,717.5*ra
    low=np.log10(max(rc,1e-10));high=lgR[-1]
    if ra>0:
        # Binary search table rows; the stable EOS pressure is nondecreasing.
        jl=max(0,np.searchsorted(lgR,low)-1);jh=len(lgR)-1
        while jh-jl>1:
            j=(jl+jh)//2
            pj=(1-a)*tab[it,j,0]+a*tab[it+1,j,0]
            f=pj*(1-rc/10**lgR[j])-ra*287*T
            if f<0:jl=j
            else:jh=j
        low=max(low,lgR[jl]);high=lgR[jh]
        pl=(1-a)*tab[it,jl,0]+a*tab[it+1,jl,0]
        ph=(1-a)*tab[it,jh,0]+a*tab[it+1,jh,0]
        slope=(ph-pl)/(lgR[jh]-lgR[jl]);lr=(low+high)/2
        for k in range(48):
            pc=pl+slope*(lr-lgR[jl]);ac=rc/10**lr
            f=pc*(1-ac)-ra*287*T
            if f>0:high=lr
            else:low=lr
            df=slope*(1-ac)+pc*ac*np.log(10.)
            trial=lr-f/max(df,1e-100)
            if trial<=low or trial>=high:trial=(low+high)/2
            if abs(f)<1e-9*max(pc,ra*287*T,1.):break
            if high-low<1e-13:
                lr=(low+high)/2
                break
            lr=trial
    else:
        lr=low
    j=min(max(np.searchsorted(lgR,lr)-1,0),len(lgR)-2)
    b=(lr-lgR[j])/(lgR[j+1]-lgR[j])
    q=(1-a)*((1-b)*tab[it,j]+b*tab[it,j+1])+a*((1-b)*tab[it+1,j]+b*tab[it+1,j+1])
    ac=min(1.,max(0.,rc/10**lr));ag=min(1.,max(0.,1-ac+ac*q[5]))
    # Upper bound for explicit CFL; not a reported equilibrium mixture sound speed.
    cs=max(q[3],np.sqrt(1.4*287*T))
    return rc*q[1]+ra*717.5*T,q[0],q[4],ag,cs,ac,rc*q[2]+ra*717.5

@njit
def thermo(U,lgT,lgR,tab):
    W=np.zeros((len(U),9))
    for i in range(len(U)):
        rc=U[i,0];ra=U[i,1];rho=rc+ra;u=U[i,2]/rho
        target=U[i,3]-.5*rho*u*u
        if rc<=0:
            T=target/(ra*717.5)
        else:
            lo=0;hi=len(lgT)-1
            while hi-lo>1:
                mid=(hi+lo)//2
                e=mixture(10**lgT[mid],rc,ra,lgT,lgR,tab)[0]
                if e<target:lo=mid
                else:hi=mid
            tl=10**lgT[lo];th=10**lgT[hi]
            el=mixture(tl,rc,ra,lgT,lgR,tab)[0]
            eh=mixture(th,rc,ra,lgT,lgR,tab)[0]
            T=tl+(th-tl)*(target-el)/max(eh-el,1e-100)
            # Secant inversion bounded by a known energy bracket.
            for k in range(9):
                q=mixture(T,rc,ra,lgT,lgR,tab);f=q[0]-target
                if abs(f)<1e-8*max(abs(target),rho*1e4):break
                if f>0:th=T;eh=q[0]
                else:tl=T;el=q[0]
                T=tl+(th-tl)*(target-el)/max(eh-el,1e-100)
        if not (10**lgT[0]<=T<=10**lgT[-1]):
            raise ValueError('Internal energy outside EOS temperature range')
        q=mixture(T,rc,ra,lgT,lgR,tab)
        fl=.5*(1+(T-1358)/np.sqrt((T-1358)**2+100))
        kappa=(1-q[3])*(385*(1-fl)+165*fl)+q[3]*.026*(T/300)**.7
        W[i]=np.array([T,q[1],q[4],u,kappa,q[2],q[3],q[6],q[5]])
    return W
