"""Same tabulated common-pressure EOS; previous T only accelerates bracketing."""
import numpy as np
from numba import njit,prange
from hem_common_thermo import mixture

@njit
def _warm_rows(U,lgT,lgR,tab,hint):
    nr,nz,_=U.shape;W=np.empty((nr,nz,10))
    for i in range(nr):
        for k in range(nz):
            rc=U[i,k,0];ra=U[i,k,1];rho=rc+ra
            ur=U[i,k,2]/rho;uz=U[i,k,3]/rho
            target=U[i,k,4]-.5*rho*(ur*ur+uz*uz)
            if rc<=0:T=target/(ra*717.5)
            else:
                lo=min(max(np.searchsorted(lgT,np.log10(max(hint[i,k,0],1.)))-1,0),len(lgT)-2)
                hi=lo+1;tl=10**lgT[lo];th=10**lgT[hi]
                el=mixture(tl,rc,ra,lgT,lgR,tab)[0]
                eh=mixture(th,rc,ra,lgT,lgR,tab)[0]
                # The previous temperature changes no bracket or root criterion.
                # Expand only if material/chemical updates crossed a table cell.
                while target<el and lo>0:
                    hi=lo;th=tl;eh=el;lo-=1;tl=10**lgT[lo]
                    el=mixture(tl,rc,ra,lgT,lgR,tab)[0]
                while target>eh and hi<len(lgT)-1:
                    lo=hi;tl=th;el=eh;hi+=1;th=10**lgT[hi]
                    eh=mixture(th,rc,ra,lgT,lgR,tab)[0]
                T=tl+(th-tl)*(target-el)/max(eh-el,1e-100)
                for iteration in range(9):
                    q=mixture(T,rc,ra,lgT,lgR,tab);f=q[0]-target
                    if abs(f)<1e-8*max(abs(target),rho*1e4):break
                    if f>0:th=T;eh=q[0]
                    else:tl=T;el=q[0]
                    T=tl+(th-tl)*(target-el)/max(eh-el,1e-100)
            if not (10**lgT[0]<=T<=10**lgT[-1]):raise ValueError('Internal energy outside EOS temperature range')
            q=mixture(T,rc,ra,lgT,lgR,tab)
            fl=.5*(1+(T-1358)/np.sqrt((T-1358)**2+100))
            kap=(1-q[3])*(385*(1-fl)+165*fl)+q[3]*.026*(T/300)**.7
            W[i,k]=np.array([T,q[1],q[4],uz,kap,q[2],q[3],q[6],q[5],ur])
    return W

@njit(parallel=True)
def fields_warm(U,lgT,lgR,tab,hint):
    W=np.empty((U.shape[0],U.shape[1],10))
    for i in prange(U.shape[0]):
        W[i:i+1]=_warm_rows(U[i:i+1],lgT,lgR,tab,hint[i:i+1])
    return W
