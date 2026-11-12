"""Shared emissivity/opacity interface, with non-LTE line absorption."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from numba import njit,prange

@njit(parallel=True,cache=True)
def _line_transfer(jj,aa,TT,vv,ds,nu,A,mass,step_v=200.):
    """j and alpha are frequency-integrated; alpha includes stimulated emission.

    No j/B closure: foreground absorption is retained even with zero emission.
    Weak gain from inversion is propagated; unresolved strong amplification fails.
    """
    npix,ny=jj.shape
    out=np.zeros(npix);thin=np.zeros(npix);taus=np.zeros(npix)
    for p in prange(npix):
        thin[p]=np.sum(jj[p]*ds)
        if thin[p]<=0:continue
        tau=0.;gain_tau=0.;vmax=0.;tmax=100.;tmin=1e30
        peakj=np.max(jj[p]);peaka=np.max(aa[p])
        for y in range(ny):
            temp=max(TT[p,y],100.)
            sig=nu/C*np.sqrt(K*temp/mass)
            tau+=abs(aa[p,y])*ds[y]/(sig*np.sqrt(2*np.pi))
            gain_tau+=max(-aa[p,y],0)*ds[y]/(sig*np.sqrt(2*np.pi))
            if jj[p,y]>peakj*1e-12 or aa[p,y]>peaka*1e-12:
                vmax=max(vmax,abs(vv[p,y]));tmin=min(tmin,temp);tmax=max(tmax,temp)
        taus[p]=tau
        # Absolute unresolved-light bound: at visible wavelengths 1e-10 W/m2/sr
        # is <2e-10 ideal photons per 11.11 um pixel per 10 ns. Using thin light
        # here changes a whole 116x90 frame by <2.2e-6 photons per line.
        # Require negligible possible stimulated gain before applying the bound.
        if thin[p]<1e-10 and gain_tau<.01:
            out[p]=thin[p];continue
        if tau<.005:
            out[p]=thin[p];continue
        vlim=vmax+7*np.sqrt(K*tmax/mass)
        dv=min(step_v,np.sqrt(K*tmin/mass)/3)
        nv=max(3,int(np.ceil(2*vlim/dv)));dv=2*vlim/nv
        fw2=np.empty(ny);gg=np.empty(ny);ll=np.empty(ny);gw=np.empty(ny)
        for y in range(ny):
            sig=nu/C*np.sqrt(K*max(TT[p,y],100.)/mass)
            fg=2*np.sqrt(2*np.log(2))*sig;fl=A/(2*np.pi)
            fw=(fg**5+2.69269*fg**4*fl+2.42843*fg**3*fl**2+4.47163*fg**2*fl**3+.07842*fg*fl**4+fl**5)**.2
            eta=min(1.,max(0.,1.36603*fl/fw-.47719*(fl/fw)**2+.11116*(fl/fw)**3))
            fw2[y]=(fw/2)**2;gg[y]=(1-eta)*np.sqrt(4*np.log(2)/np.pi)/fw
            ll[y]=eta*(fw/2)/np.pi;gw[y]=4*np.log(2)/(fw*fw)
        for k in range(nv):
            vel=-vlim+(k+.5)*dv;I=0.
            for y in range(ny):
                if jj[p,y]==0 and aa[p,y]==0:continue
                dn=nu/C*(vel-vv[p,y]);phi=gg[y]*np.exp(-gw[y]*dn*dn)+ll[y]/(dn*dn+fw2[y])
                j=jj[p,y]*phi;alpha=aa[p,y]*phi;tau=alpha*ds[y]
                if tau < -100:
                    I=np.nan
                elif abs(tau)<1e-6:I=I*np.exp(-tau)+j*ds[y]*(1-tau/2+tau*tau/6)
                else:I=I*np.exp(-tau)+j/alpha*(-np.expm1(-tau))
            out[p]+=I*nu/C*dv
    return out,thin,taus

def line_transfer(jj,aa,TT,vv,ds,nu,A,mass,step_v=200.):
    result=_line_transfer(jj,aa,TT,vv,ds,nu,A,mass,step_v)
    if not np.all(np.isfinite(result[0])):raise ValueError('Strong line gain requires a saturation model')
    return result

def line_coefficients(nlo,nup,glo,gup,A,nu):
    return H*nu*A*nup/(4*np.pi), C*C*A/(8*np.pi*nu*nu)*(nlo*gup/glo-nup)

def ff_bands(q,T,nquad=20):
    """Frequency quadrature returns energy and photon emissivity /sr /m3."""
    nodes,weights=np.polynomial.legendre.leggauss(nquad)
    energy=[];photons=[]
    for lo,hi in BANDS:
        a,b=C/(hi*1e-9),C/(lo*1e-9)
        je=np.zeros_like(q);jp=np.zeros_like(q)
        for x,w in zip(nodes,weights):
            nu=(a+b)/2+x*(b-a)/2
            j=q*H/(K*T)*np.exp(-H*nu/(K*T))*w*(b-a)/2
            je+=j;jp+=j/(H*nu)
        energy.append(je);photons.append(jp)
    return np.array(energy),np.array(photons)

@njit(parallel=True,cache=True)
def continuum_transfer(j,alpha,ds):
    out=np.zeros(j.shape[0])
    for p in prange(j.shape[0]):
        val=0.
        for y in range(j.shape[1]):
            tau=alpha[p,y]*ds[y]
            if abs(tau)<1e-6:val=val*np.exp(-tau)+j[p,y]*ds[y]*(1-tau/2+tau*tau/6)
            else:val=val*np.exp(-tau)+j[p,y]/alpha[p,y]*(-np.expm1(-tau))
        out[p]=val
    return out

def ff_images(q,T,rays):
    je,jp=ff_bands(q,T)
    thin_e=np.array([rays.project(a) for a in je]);thin_p=np.array([rays.project(a) for a in jp])
    nu=C/550e-9
    alpha=q*C*C/(2*K*T*nu**3)*(-np.expm1(-H*nu/(K*T)))
    tau=float(rays.project(alpha).max())
    if tau<1e-4:return thin_e.copy(),thin_p.copy(),thin_e,thin_p,tau
    e=np.zeros_like(thin_e);p=np.zeros_like(thin_p)
    nodes,weights=np.polynomial.legendre.leggauss(20)
    for bi,(lo,hi) in enumerate(BANDS):
        a,b=C/(hi*1e-9),C/(lo*1e-9)
        for x,w in zip(nodes,weights):
            nu=(a+b)/2+x*(b-a)/2
            j=q*H/(K*T)*np.exp(-H*nu/(K*T))
            alpha=q*C*C/(2*K*T*nu**3)*(-np.expm1(-H*nu/(K*T)))
            out=rays.image(continuum_transfer(rays.sample(j),rays.sample(alpha),rays.ds))*w*(b-a)/2
            e[bi]+=out;p[bi]+=out/(H*nu)
    return e,p,thin_e,thin_p,tau
