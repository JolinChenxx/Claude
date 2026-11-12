"""Same formal line transport with frequency-independent factors cached per ray.

The reference implementation remains in radiation_s4.py for comparison.
No opacity, spectral, or spatial sampling threshold is changed here.
"""
import numpy as np
from numba import njit,prange
H=6.62607015e-34;C=299792458.;K=1.380649e-23

@njit(parallel=True,cache=True)
def transfer_line(jj,TT,vv,ds,nu,A,mass,step_v=200.):
 npix,ny=jj.shape;out=np.zeros(npix);taumax=np.zeros(npix);thin=np.zeros(npix)
 for p in prange(npix):
  peak=0.;column=0.;tau_est=0.;vmax=0.;tmax=0.;tmin=1e100
  for y in range(ny):peak=max(peak,jj[p,y]);column+=jj[p,y]*ds[y]
  thin[p]=column
  if column<=0:continue
  for y in range(ny):
   if jj[p,y]>peak*1e-10:
    t=TT[p,y];b=2*H*nu**3/C**2/np.expm1(min(H*nu/(K*t),700.))
    sig=nu/C*np.sqrt(K*t/mass);tau_est+=jj[p,y]/max(b,1e-280)/(sig*np.sqrt(2*np.pi))*ds[y]
    vmax=max(vmax,abs(vv[p,y]));tmax=max(tmax,t);tmin=min(tmin,t)
  taumax[p]=tau_est
  if tau_est<.005:out[p]=column;continue
  maxsig=np.sqrt(K*tmax/mass);vlim=vmax+7*maxsig
  dv=min(step_v,np.sqrt(K*tmin/mass)/3);nv=max(3,int(np.ceil(2*vlim/dv)));dv=2*vlim/nv
  bb=np.empty(ny);fw2=np.empty(ny);gauss=np.empty(ny);lorentz=np.empty(ny);gwidth=np.empty(ny)
  for y in range(ny):
   if jj[p,y]<peak*1e-12:continue
   t=TT[p,y];sig=nu/C*np.sqrt(K*t/mass);gamma=A/(4*np.pi)
   fg=2*np.sqrt(2*np.log(2.))*sig;fl=2*gamma
   fw=(fg**5+2.69269*fg**4*fl+2.42843*fg**3*fl**2+4.47163*fg**2*fl**3+.07842*fg*fl**4+fl**5)**.2
   eta=min(1.,max(0.,1.36603*fl/fw-.47719*(fl/fw)**2+.11116*(fl/fw)**3))
   bb[y]=max(2*H*nu**3/C**2/np.expm1(min(H*nu/(K*t),700.)),1e-280)
   fw2[y]=(fw/2)**2;gauss[y]=(1-eta)*np.sqrt(4*np.log(2.)/np.pi)/fw
   lorentz[y]=eta*(fw/2)/np.pi;gwidth[y]=4*np.log(2.)/(fw*fw)
  for f in range(nv):
   vel=-vlim+(f+.5)*dv;I=0.
   for y in range(ny):
    j=jj[p,y]
    if j<peak*1e-12:continue
    dn=nu/C*(vel-vv[p,y]);phi=gauss[y]*np.exp(-gwidth[y]*dn*dn)+lorentz[y]/(dn*dn+fw2[y])
    alpha=j*phi/bb[y];tau=max(alpha*ds[y],0.)
    if tau<1e-6:I=I*np.exp(-tau)+j*phi*ds[y]*(1-tau/2+tau*tau/6)
    else:I=I*np.exp(-tau)+j*phi/alpha*(-np.expm1(-tau))
   out[p]+=I*nu/C*dv
 return out,taumax,thin
