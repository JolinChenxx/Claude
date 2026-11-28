"""LTE NIST bound-bound + free-free radiation and axisymmetric formal transfer.

SI throughout. Band radiance W m^-2 sr^-1; no per-frame normalization.
Natural+Doppler pseudo-Voigt, resolved bulk radial Doppler shifts. No fitted
Stark width, free-bound or molecular bands: reported model limitations.
"""
from pathlib import Path
import csv,json,argparse,time,os
import numpy as np
from scipy.interpolate import RegularGridInterpolator
from numba import njit,prange,set_num_threads
ROOT=Path(__file__).resolve().parents[1];DATA=ROOT/'data';RES=ROOT/'results'
K=1.380649e-23;H=6.62607015e-34;C=299792458.;EV=1.602176634e-19
ME=9.1093837139e-31;AMU=1.66053906660e-27
MASSES=np.array([63.546,14.0067,15.999])*AMU
BANDS=np.array([[475.,525.],[490.,510.],[450.,550.]])

def planck(nu,T):
 return 2*H*nu**3/C**2/np.expm1(np.minimum(H*nu/(K*np.maximum(T,1)),700))

class Atomic:
 def __init__(self):
  self.a=np.load(DATA/'atomic_levels.npz');self.lt=np.linspace(np.log(100),np.log(150000),1200);self.ts=np.exp(self.lt)
  self.q=np.empty((3,3,len(self.ts)))
  for j in range(3):
   for s in range(3):self.q[j,s]=(self.a[f'g{j}{s}'][:,None]*np.exp(-self.a[f'e{j}{s}'][:,None]/(K*self.ts))).sum(0)
  self.lines=[];rejected=[]
  for el,ename in enumerate(['Cu','N','O']):
   for stage,sname in enumerate(['I','II']):
    for row in csv.DictReader((DATA/f'{ename}_{sname}_visible_lines.tsv').open(),delimiter='\t'):
     try:
      def val(key):return float(row[key].strip('" []?'))
      wl=val('obs_wl_air(nm)') if row['obs_wl_air(nm)'].strip('" ') else val('ritz_wl_air(nm)')
      A=val('Aki(s^-1)');lo=val('Ei(eV)');up=val('Ek(eV)');gu=val('g_k')
      if up*EV>=self.a['chi'][el,stage]:rejected.append([ename,sname,wl,'upper above ionization threshold']);continue
      # Air-to-vacuum wavelength: Peck & Reeder 1972 standard dry air.
      sigma=1000/wl;n=1+1e-8*(8060.51+2480990/(132.274-sigma*sigma)+17455.7/(39.32957-sigma*sigma))
      nu=C/(wl*1e-9*n)
      self.lines.append(dict(element=el,stage=stage,wavelength_air_nm=wl,nu=nu,A=A,lower_eV=lo,upper_eV=up,gu=gu,mass=MASSES[el],accuracy=row['Acc']))
     except (ValueError,KeyError):continue
  # Natural transition width includes decay of BOTH participating levels.
  network_path=DATA/'network.json'
  if network_path.exists():
   network=json.loads(network_path.read_text());energies=np.array(network['energies_eV']);widths=np.zeros(len(energies))
   for transition in network['lines']:widths[transition['up']]+=transition['A']
   for line in self.lines:
    if line['element']==0 and line['stage']==0:
     lo=int(np.argmin(abs(energies-line['lower_eV'])));up=int(np.argmin(abs(energies-line['upper_eV'])))
     if abs(energies[lo]-line['lower_eV'])<1e-3 and abs(energies[up]-line['upper_eV'])<1e-3:
      line['radiative_width_s']=float(widths[lo]+widths[up])
  (DATA/'radiation_line_manifest.json').write_text(json.dumps({'source':'NIST ASD, downloaded TSV and full query URLs in data/','accepted':self.lines,'rejected':rejected,'partition':'Same truncated finite partition as LTE EOS. Line upper states below ionization threshold can exceed partition cutoff; quantified separately.','line_shape':'Natural plus thermal Doppler pseudo-Voigt; resolved Cu I uses total upper+lower radiative widths including network UV branches. Other incomplete lifetimes use transition A approximation. Radial bulk Doppler shifts. No electron-impact Stark broadening.'},indent=2))
 def populations(self,T,ne,rho,Y,xc):
  tq=np.log(np.maximum(T,100));q=np.array([[np.interp(tq,self.lt,self.q[j,s]) for s in range(3)] for j in range(3)])
  wN=.79*MASSES[1]/(.79*MASSES[1]+.21*MASSES[2]);N=np.array([rho*Y/MASSES[0],rho*(1-Y)*wN/MASSES[1],rho*(1-Y)*(1-wN)/MASSES[2]])
  saha=2*(2*np.pi*ME*K*T/H**2)**1.5
  ratios=saha*q[:,1:]/q[:,:-1]*np.exp(-self.a['chi'][:,:2,None,None]/(K*T)) /np.maximum(ne,1e-40)
  ratios=np.minimum(ratios,1e100);fac=1+ratios[:,0]+ratios[:,0]*ratios[:,1]
  n0=N/fac
  for j,D,tr,tv,g in [(1,9.759,2.88,3390,1),(2,5.116,2.07,2270,3)]:
   qm=g*T/(2*tr)/(-np.expm1(-tv/T));kd=(np.pi*MASSES[j]*K*T/H**2)**1.5*q[j,0]**2/qm*np.exp(-D*EV/(K*T));kd=np.maximum(kd,1e-260)
   n0[j]=2*N[j]/(fac[j]+np.sqrt(np.minimum(fac[j]**2+8*N[j]/kd,1e300)))
  # The interpolated original EOS Cu neutral fraction is authoritative.
  n0[0]=N[0]*(1-np.clip(xc,0,1));n1=n0*ratios[:,0]
  n1[0]=N[0]*np.clip(xc,0,1)/(1+ratios[0,1])
  return n0,n1,q
 def emissivities(self,fields):
  T,ne,Y,rho,xc=fields[:5];Y=np.clip(Y,0,1)
  n0,n1,q=self.populations(T,ne,rho,Y,xc)
  lines=[]
  for line in self.lines:
   j,s=line['element'],line['stage'];n=(n0 if s==0 else n1)[j]
   up=n*line['gu']*np.exp(-line['upper_eV']*EV/(K*T))/q[j,s]
   lines.append(H*line['nu']*line['A']*up/(4*np.pi))
  return np.asarray(lines),1.426e-40*np.sqrt(T)*ne*fields[8]/(4*np.pi)

@njit(cache=True)
def slab(I,j,alpha,ds):
 tau=max(alpha*ds,0.)
 if tau<1e-6:return I*np.exp(-tau)+j*ds*(1-tau/2+tau*tau/6)
 return I*np.exp(-tau)+j/alpha*(-np.expm1(-tau))

@njit(parallel=True,cache=True)
def transfer_line_reference(jj,TT,vv,ds,nu,A,mass,step_v=200.):
 """Independent rays in row-major order; natural+Doppler spectral integration."""
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
  # Resolve the coldest emitting Doppler core, independent of the camera gain.
  dv=min(step_v,np.sqrt(K*tmin/mass)/3);nv=max(3,int(np.ceil(2*vlim/dv)));dv=2*vlim/nv
  for f in range(nv):
   vel=-vlim+(f+.5)*dv;I=0.
   for y in range(ny):
    j=jj[p,y]
    if j<peak*1e-12:continue
    t=TT[p,y];sig=nu/C*np.sqrt(K*t/mass);gamma=A/(4*np.pi)
    fg=2*np.sqrt(2*np.log(2.))*sig;fl=2*gamma
    fw=(fg**5+2.69269*fg**4*fl+2.42843*fg**3*fl**2+4.47163*fg**2*fl**3+.07842*fg*fl**4+fl**5)**.2
    eta=min(1.,max(0.,1.36603*fl/fw-.47719*(fl/fw)**2+.11116*(fl/fw)**3))
    dn=nu/C*(vel-vv[p,y]);phi=(1-eta)*np.sqrt(4*np.log(2.)/np.pi)/fw*np.exp(-4*np.log(2.)*(dn/fw)**2)+eta*(fw/2)/np.pi/(dn*dn+(fw/2)**2)
    b=2*H*nu**3/C**2/np.expm1(min(H*nu/(K*t),700.));alpha=j*phi/max(b,1e-280)
    I=slab(I,j*phi,alpha,ds[y])
   out[p]+=I*nu/C*dv
 return out,taumax,thin

# Frequency-independent factors are cached without changing any sampling.
# Real H15 ray comparisons: results/transport_cache_audit.json.
from line_transfer_fast import transfer_line

@njit(parallel=True,cache=True)
def transfer_ff_reference(q,TT,ds):
 out=np.zeros((3,q.shape[0]))
 for p in prange(q.shape[0]):
  if np.max(q[p])<=0:continue
  for f in range(40):
   lam=(450+(f+.5)*2.5)*1e-9;nu=C/lam;dnu=C/lam**2*2.5e-9;I=0.
   for y in range(q.shape[1]):
    T=TT[p,y];j=q[p,y]*H/(K*T)*np.exp(-H*nu/(K*T));b=2*H*nu**3/C**2/np.expm1(min(H*nu/(K*T),700.))
    I=slab(I,j,j/max(b,1e-280),ds[y])
   out[2,p]+=I*dnu
   if 475e-9<lam<525e-9:out[0,p]+=I*dnu
   if 490e-9<lam<510e-9:out[1,p]+=I*dnu
 return out

@njit(parallel=True,cache=True)
def transfer_ff(q,TT,ds):
 """Analytic band integral when an absorption bound is below 1e-4.

 No emission floor. The free-free opacity decreases with frequency, hence
 its value at 550 nm bounds every frequency in all three bands. The thin
 branch has relative attenuation error at most 1e-4; all other rays retain
 the original frequency-resolved formal solution.
 """
 out=np.zeros((3,q.shape[0]));numin=C/550e-9
 for p in prange(q.shape[0]):
  if np.max(q[p])<=0:continue
  tau_bound=0.
  for y in range(q.shape[1]):
   T=TT[p,y]
   tau_bound+=q[p,y]*C*C/(2*K*T*numin**3)*(-np.expm1(-H*numin/(K*T)))*ds[y]
  if tau_bound<1e-4:
   for bi in range(3):
    lo=BANDS[bi,0]*1e-9;hi=BANDS[bi,1]*1e-9
    for y in range(q.shape[1]):
     T=TT[p,y]
     out[bi,p]+=q[p,y]*(np.exp(-H*C/(hi*K*T))-np.exp(-H*C/(lo*K*T)))*ds[y]
   continue
  for f in range(40):
   lam=(450+(f+.5)*2.5)*1e-9;nu=C/lam;dnu=C/lam**2*2.5e-9;I=0.
   for y in range(q.shape[1]):
    T=TT[p,y];j=q[p,y]*H/(K*T)*np.exp(-H*nu/(K*T));b=2*H*nu**3/C**2/np.expm1(min(H*nu/(K*T),700.))
    I=slab(I,j,j/max(b,1e-280),ds[y])
   out[2,p]+=I*dnu
   if 475e-9<lam<525e-9:out[0,p]+=I*dnu
   if 490e-9<lam<510e-9:out[1,p]+=I*dnu
 return out

def physical_grid(f,r,z):
 # Inverse ALE along each fixed R line. Includes actual moving coordinates.
 if f.shape[0]<11 or np.max(abs(f[9]-z[:,None]*1e-6))<1e-12:return f[:9]
 out=np.empty_like(f[:9])
 for ir in range(len(r)):
  zp=f[9,:,ir]*1e6
  if not np.all(np.diff(zp)>0):raise ValueError('ALE mapping folded')
  for e in range(9):out[e,:,ir]=np.interp(z,zp,f[e,:,ir])
 return out

class Rays:
 def __init__(self,r,z,pixel=400/36,dy_factor=1):
  self.x=(np.arange(42)+.5)*pixel;self.z=(np.arange(82)+.5)*pixel
  # Dense near-axis sampling resolves the 11 um source, coarse far field.
  y=np.unique(np.r_[np.arange(0,30,1*dy_factor),np.arange(30,150,3*dy_factor),np.arange(150,651,7*dy_factor)])
  edges=np.r_[-y[:0:-1],y];self.y=(edges[1:]+edges[:-1])/2;self.ds=np.diff(edges)*1e-6
  rr=np.sqrt(self.x[:,None]**2+self.y[None,:]**2)
  self.points=np.stack(np.broadcast_arrays(self.z[:,None,None],rr[None,:,:]),axis=-1).reshape(-1,2)
  self.shape=(82,42,len(self.y));self.r=r;self.native_z=z;self.vproj=self.y[None,:]/rr
 def sample(self,a):
  return RegularGridInterpolator((self.native_z,self.r),a,bounds_error=False,fill_value=0)(self.points).reshape(self.shape).reshape(-1,len(self.y))
 def image(self,a):
  half=a.reshape(82,42);return np.concatenate([half[:,::-1],half],axis=1)[::-1]

def run(case,stride=1,limit=None,images=True,ray_factor=1):
 meta=json.loads((RES/f'{case}_fields.json').read_text());f=np.memmap(RES/f'{case}_fields.bin',dtype=meta['dtype'],mode='r',shape=tuple(meta['shape']))
 if f.nbytes!=(RES/f'{case}_fields.bin').stat().st_size:raise ValueError('Incomplete field export')
 r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z']);times=np.array(meta['times_s'])
 atom=Atomic();rays=Rays(r,z,dy_factor=ray_factor);ids=np.arange(0,len(times),stride)
 if limit:ids=ids[:limit]
 # All luminosity/energy nodes remain included. Formal camera transfer is
 # needed only through the latest S4 gate (740+120+5 ns), plus its bracket.
 image_last=min(len(times)-1,int(np.searchsorted(times,865e-9))+stride-1)
 output=RES/f'{case}_radiation';output.mkdir(exist_ok=True)
 luminosity=[];image_times=[];imgs=[];opacity=[];omitted=[];ff_lum=[]
 for it in ids:
  start=time.time();native=np.array(f[it],dtype=float);make_image=images and it<=image_last
  T=native[0]
  if make_image:
   phys=physical_grid(native,r,z);T=phys[0];jl,ff=atom.emissivities(phys)
  # Volume integral from the exported native FE interpolation with physical J.
  # Tiny omitted r<.05 um core is extrapolated for quadrature; reported error
  # by independent field-grid sensitivity, never claimed as FE integration.
  jnat,ffnat=atom.emissivities(native[:9]);jac=native[10] if native.shape[0]>10 else 1
  L=4*np.pi*np.trapz(np.trapz(jnat*jac*r[None,None,:]*2*np.pi*1e-6,x=r*1e-6,axis=2),x=z*1e-6,axis=1)
  row=[times[it]]+[sum(v for v,line in zip(L,atom.lines) if lo<=line['wavelength_air_nm']<=hi) for lo,hi in BANDS]
  luminosity.append(row)
  nt=native[0];ffrow=[]
  for lo,hi in BANDS:
   bandfraction=np.exp(-H*C/(hi*1e-9*K*nt))-np.exp(-H*C/(lo*1e-9*K*nt))
   ffrow.append(4*np.pi*np.trapz(np.trapz(ffnat*bandfraction*jac*r[None,:]*2*np.pi*1e-6,x=r*1e-6,axis=1),x=z*1e-6))
  ff_lum.append([times[it],*ffrow])
  if make_image:
   tr=rays.sample(T);tr=np.maximum(tr,100);vr=rays.sample(phys[5]).reshape(rays.shape)*rays.vproj[None,:,:];vr=vr.reshape(tr.shape)
   out=transfer_ff(rays.sample(ff),tr,rays.ds);tax=0.;total=sum(L);selected=set()
   # Preserve each passband separately: a weak narrow-band line must not be
   # discarded just because the 510/515/522 nm Cu lines dominate the wide band.
   fractions=[]
   for lo,hi in BANDS:
    bandids=[i for i,line in enumerate(atom.lines) if lo<=line['wavelength_air_nm']<=hi];bandids=sorted(bandids,key=lambda i:L[i],reverse=True);bandtotal=sum(L[i] for i in bandids);kept=0.
    for li in bandids:
     if kept>=.9999*bandtotal:break
     kept+=L[li];selected.add(li)
    fractions.append(max(0,1-kept/max(bandtotal,1e-100)) if bandtotal>1e-100 else 0.)
   for li in selected:
    line=atom.lines[li];arr,tau,thin=transfer_line(rays.sample(jl[li]),tr,vr,rays.ds,line['nu'],line['A'],line['mass']);tax=max(tax,float(tau.max()))
    for bi,(lo,hi) in enumerate(BANDS):
     if lo<=line['wavelength_air_nm']<=hi:out[bi]+=arr
   img=np.array([rays.image(a) for a in out]);imgs.append(img);image_times.append(times[it]);opacity.append(tax);omitted.append(max(fractions))
  if it%20==0 or limit:print(f'RADIATION {case} {it}/{len(times)} T={T.max():.1f} Lvis={row[1]:.4g} elapsed={time.time()-start:.2f}s',flush=True)
  if len(luminosity)%10==0:np.savetxt(output/'line_luminosity_W.csv',luminosity,delimiter=',',header='t_s,line_475_525_W,line_490_510_W,line_450_550_W',comments='')
 np.savetxt(output/'line_luminosity_W.csv',luminosity,delimiter=',',header='t_s,line_475_525_W,line_490_510_W,line_450_550_W',comments='')
 np.savetxt(output/'continuum_luminosity_W.csv',ff_lum,delimiter=',',header='t_s,ff_475_525_W,ff_490_510_W,ff_450_550_W',comments='')
 if images:np.savez_compressed(output/'radiance.npz',times_s=image_times,radiance=imgs,bands_nm=BANDS,peak_tau_upper_bound=opacity,omitted_line_fraction=omitted,pixel_um=400/36)
 eline=np.trapz(np.array(luminosity)[:,1:],x=times[ids],axis=0);eff=np.trapz(np.array(ff_lum)[:,1:],x=times[ids],axis=0)
 audit=json.loads((RES/f'{case}_audit.json').read_text());ratio=(eline+eff)/(audit['absorbed_uJ']*1e-6)
 summary={'case':case,'processed_times':len(ids),'line_emitted_energy_J':eline.tolist(),'continuum_emitted_energy_J':eff.tolist(),'visible_upper_bound_to_absorbed_ratio':ratio.tolist(),'radiative_feedback_required_upper_bound':bool(np.max(ratio)>.05),'energy_status':'Unattenuated local LTE emission is an upper bound on escaping visible radiation. If this bound is below 5%, absorption/escape cannot trigger the feedback threshold.','images':images,'camera_time_range_s':[float(image_times[0]),float(image_times[-1])] if image_times else None,'camera_window_note':'All 29 S4 gates and allowed time shifts are covered. Luminosity integrates every exported time; formal transfer stops after the last needed gate bracket.','opacity_estimate':'Sum of local line-centre opacities is a conservative bound; includes bulk frequency offsets only in formal transfer.','line_selection_max_omitted_fraction':max(omitted,default=None),'model':'LTE NIST ASD; finite EOS partitions; natural+Doppler pseudo-Voigt; free-free; no free-bound, Stark or molecular bands.'}
 summary.update(absolute_emission_cutoff=0,radiance_storage_dtype='float64',free_free_thin_absorption_error_bound=1e-4)
 summary['ray_grid_factor']=ray_factor
 summary['all_allowed_S4_gates_covered']=bool(image_times and image_times[0]<=55e-9 and image_times[-1]>=865e-9)
 if not summary['all_allowed_S4_gates_covered']:summary['camera_window_note']='Partial diagnostic export; only gates bracketed by the actual saved image time range may be evaluated.'
 (output/'summary.json').write_text(json.dumps(summary,indent=2))
 return summary

def verify():
 for tau in [0,1e-9,.01,1.,100.]:
  val=slab(2.,3.,tau,1.);expected=2*np.exp(-tau)+(3*(-np.expm1(-tau))/tau if tau else 3)
  assert abs(val-expected)<1e-9*max(expected,1)
 # A homogeneous LTE slab must return B*(1-exp(-tau_nu)).
 T=7000.;nu=C/510.55e-9;A=2e6;mass=MASSES[0];sig=nu/C*np.sqrt(K*T/mass);B=planck(nu,T)
 j=B*sig*np.sqrt(2*np.pi)*3/1e-3
 a,t,thin=transfer_line(np.full((1,100),j),np.full((1,100),T),np.zeros((1,100)),np.full(100,1e-5),nu,A,mass,50)
 v=np.linspace(-1e4,1e4,20001);phi=np.exp(-.5*(v/np.sqrt(K*T/mass))**2)/(sig*np.sqrt(2*np.pi));ref=np.trapz(B*(-np.expm1(-j*phi/B*1e-3)),x=v*nu/C)
 assert abs(a[0]/ref-1)<.02,(a,ref)
 faint,_,_=transfer_line(np.full((1,4),1e-50),np.full((1,4),T),np.zeros((1,4)),np.full(4,1e-6),nu,A,mass)
 assert np.isclose(faint[0]/4e-56,1,rtol=1e-12)
 faint_ff=transfer_ff(np.full((1,4),1e-50),np.full((1,4),T),np.full(4,1e-6))
 assert np.all(faint_ff>0)
 result={'slab_thin_thick':'pass','LTE_homogeneous_line_relative_error':float(a[0]/ref-1),'sub_1e30_emissivity_line_and_continuum_preserved':True,'axisymmetry':'Rays use sqrt(x^2+y^2), paired sightline cells; image mirror is exact by construction.'}
 (RES/'radiation_unit_audit.json').write_text(json.dumps(result,indent=2));print(result)

if __name__=='__main__':
 set_num_threads(int(os.environ.get('S4_POST_THREADS','16')))
 p=argparse.ArgumentParser();p.add_argument('case',nargs='?');p.add_argument('--stride',type=int,default=1);p.add_argument('--limit',type=int);p.add_argument('--budget-only',action='store_true');p.add_argument('--verify',action='store_true');p.add_argument('--ray-factor',type=float,default=1);a=p.parse_args()
 if not np.isfinite(a.ray_factor) or a.ray_factor<=0:p.error('Ray factor must be positive and finite')
 if a.verify:verify()
 elif a.case:print(json.dumps(run(a.case,a.stride,a.limit,not a.budget_only,a.ray_factor),indent=2))
