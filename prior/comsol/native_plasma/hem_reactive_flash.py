"""Energy-conserving equilibrium phase projection for the split material model.

The root includes the internal energy of newly vaporized neutral copper and the
energy returned by condensation. Existing gas populations remain non-LTE.
No additional ions, electrons, or fitted release factors are introduced.
"""
import os,numpy as np
from numba import njit, prange
from hem_common_thermo import mixture

KB=1.380649e-23
EV=1.602176634e-19
MCU=.063546/6.02214076e23
LEVELS=np.array([0.,1.388948,1.6422256,3.7858977,3.816692,6.1911751,6.1920252])*EV
WEIGHTS=np.array([2.,6.,4.,2.,4.,4.,6.])
NEUTRALIZE_ATTACHED=os.environ.get('ROUTE1_PHASE_CHARGE_PARTNERS')=='1'
PLASMA_WAVE_BOUND=os.environ.get('ROUTE1_PLASMA_WAVE_BOUND')=='1'
CHECK_TEMPERATURE_HINT=os.environ.get('ROUTE1_FLASH_CHECK_HINT','1')=='1'

@njit
def phase_state(s, want, T, thermal):
    out=s.copy()
    got=np.sum(s[10:18])
    if len(s)>20:got+=np.sum(s[20:])
    shrink=min(1.,want/max(got,1e-100))
    out[10:18]*=shrink
    removed=s[17]-out[17]
    if len(s)>20:
        out[20:]*=shrink
        for j in range(20,len(s)):removed+=(j-18)*(s[j]-out[j])
    free_used=min(removed,max(s[18],0.))
    if NEUTRALIZE_ATTACHED:
        # Fast neutralization accompanying equilibrium condensation: a Cu ion
        # takes free electrons first, then the electron bound to O- or O2-.
        # O/O2 remain in the gas; all nuclei and charge are preserved. Binding
        # energy is already in ENERGY and hence in the same energy root.
        missing=removed-free_used
        for ion,neutral in [(8,3),(9,1)]:
            used=min(missing,max(out[ion],0.))
            out[ion]-=used;out[neutral]+=used;missing-=used
        if missing>1e-10*max(removed,1.):
            # Such a state already lacks negative-charge partners. It requires
            # an electric-current closure, not an invented electron source.
            out[:]=np.nan
            return out
    ef=max(0.,1.-free_used/max(s[18],1e-100))
    out[18:20]*=ef
    born=max(want-got,0.)
    if thermal:
        weights=WEIGHTS*np.exp(-LEVELS/(KB*T))
        out[10:17]+=born*weights/weights.sum()
    else:
        out[10]+=born
    return out

@njit
def energy_state(T, rc, ra, s, lgT, lgR, tab, energy, cv, thermal):
    q=mixture(T,rc,ra,lgT,lgR,tab)
    ss=phase_state(s,rc*q[2]/MCU,T,thermal)
    # All copper heavy species have 3/2 kB translational heat capacity.
    # Once copper phase mass is consistent, only air dissociation changes Cv.
    dcv=np.sum(ss[:10]*cv[:10])-ra*717.5
    total=q[0]+dcv*T+np.sum(ss[:18]*energy[:18])+ss[19]*EV
    if len(s)>20:total+=np.sum(ss[20:]*energy[18:])
    return total,q,ss

@njit
def _reactive_flash_rows(U,S,hint,lgT,lgR,tab,energy,cv,thermal):
    W=np.empty_like(hint);out=np.empty_like(S);residual=np.empty(U.shape[:2])
    for i in range(U.shape[0]):
        for k in range(U.shape[1]):
            rc,ra=U[i,k,0],U[i,k,1];rho=rc+ra
            ur=U[i,k,2]/rho;uz=U[i,k,3]/rho
            target=U[i,k,4]-.5*rho*(ur*ur+uz*uz)
            s=S[i,k]
            # Check the saved temperature against exactly the same root residual
            # before constructing a bracket. Unchanged cold cells need one EOS
            # evaluation; a failed check follows the original bracketed solve.
            T=hint[i,k,0]
            total,q,ss=energy_state(T,rc,ra,s,lgT,lgR,tab,energy,cv,thermal)
            if not CHECK_TEMPERATURE_HINT or not np.isfinite(total) or abs(total-target)>1e-8*max(abs(target),rho*1e4):
                lo=min(max(np.searchsorted(lgT,np.log10(max(T,1.)))-1,0),len(lgT)-2)
                hi=lo+1;tl=10**lgT[lo];th=10**lgT[hi]
                el=energy_state(tl,rc,ra,s,lgT,lgR,tab,energy,cv,thermal)[0]
                eh=energy_state(th,rc,ra,s,lgT,lgR,tab,energy,cv,thermal)[0]
                while target<el and lo>0:
                    hi=lo;th=tl;eh=el;lo-=1;tl=10**lgT[lo]
                    el=energy_state(tl,rc,ra,s,lgT,lgR,tab,energy,cv,thermal)[0]
                while target>eh and hi<len(lgT)-1:
                    lo=hi;tl=th;el=eh;hi+=1;th=10**lgT[hi]
                    eh=energy_state(th,rc,ra,s,lgT,lgR,tab,energy,cv,thermal)[0]
                if target<el or target>eh:
                    residual[i,k]=np.nan;W[i,k]=hint[i,k];out[i,k]=s
                    continue
                T=tl+(th-tl)*(target-el)/max(eh-el,1e-100)
                for iteration in range(64):
                    total,q,ss=energy_state(T,rc,ra,s,lgT,lgR,tab,energy,cv,thermal)
                    err=total-target
                    if abs(err)<=1e-8*max(abs(target),rho*1e4):break
                    if err>0:th=T;eh=total
                    else:tl=T;el=total
                    # Safeguarded secant; bisection guarantees progress on curved branches.
                    trial=tl+(th-tl)*(target-el)/max(eh-el,1e-100)
                    T=(tl+th)/2 if iteration%4==3 else min(th-.1*(th-tl),max(tl+.1*(th-tl),trial))
            total,q,ss=energy_state(T,rc,ra,s,lgT,lgR,tab,energy,cv,thermal)
            err=total-target
            residual[i,k]=err
            out[i,k]=ss
            fl=.5*(1+(T-1358)/np.sqrt((T-1358)**2+100))
            kap=(1-q[3])*(385*(1-fl)+165*fl)+q[3]*.026*(T/300)**.7
            baseline_n=ra/(.79*.0280134+.21*.0319988)*6.02214076e23+rc*q[2]/MCU
            heavy_n=ss[:18].sum()
            if len(ss)>20:heavy_n+=ss[20:].sum()
            pe=2*ss[19]*EV/3
            dp_heavy=(heavy_n-baseline_n)*KB*T
            pressure=q[1]+pe+dp_heavy
            # Frozen two-temperature waves include electron pressure work.
            # q[4] bounds nuclear/air sound speed, not plasma sound speed.
            # Add gamma_e*p_e/rho; positive extra atomic-heavy pressure uses
            # the monatomic gamma upper bound. This is a numerical wave bound,
            # not the reported equilibrium mixture sound speed.
            wave=q[4]
            if PLASMA_WAVE_BOUND:wave=np.sqrt(wave*wave+5/3*(pe+max(dp_heavy,0.))/rho)
            W[i,k]=np.array([T,pressure,wave,uz,kap,q[2],q[3],q[6],q[5],ur])
    return W,out,residual

@njit(parallel=True)
def reactive_flash(U,S,hint,lgT,lgR,tab,energy,cv,thermal):
    W=np.empty_like(hint);out=np.empty_like(S);residual=np.empty(U.shape[:2])
    for i in prange(U.shape[0]):
        w,s,e=_reactive_flash_rows(U[i:i+1],S[i:i+1],hint[i:i+1],lgT,lgR,tab,energy,cv,thermal)
        W[i:i+1]=w;out[i:i+1]=s;residual[i:i+1]=e
    return W,out,residual
