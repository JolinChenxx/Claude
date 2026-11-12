"""Non-LTE line transfer from native excited-state populations, SI radiance.

No LTE populations and no camera gain enter radiation. Doppler profiles,
radial velocity shifts and stimulated absorption use exported native fields.
Natural/Stark broadening remain explicit limitations. Free-bound continuum
uses the same approximate Kramers-Milne cross sections as the reaction model.
"""
from pathlib import Path
import json,sys,os
import numpy as np
from scipy.interpolate import RegularGridInterpolator
from numba import njit,prange,set_num_threads
R=Path(__file__).resolve().parent
sys.path.insert(0,str(R))
K=1.380649e-23;H=6.62607015e-34;C=299792458.;M=0.063546/6.02214076e23
EV=1.602176634e-19

@njit(parallel=True,cache=True)
def bilinear_sample(field,iz,ir,wz,wr,valid):
 out=np.empty(len(ir))
 for i in prange(len(ir)):
  if valid[i]:
   low=field[iz[i],ir[i]]*(1-wr[i])+field[iz[i],ir[i]+1]*wr[i]
   high=field[iz[i]+1,ir[i]]*(1-wr[i])+field[iz[i]+1,ir[i]+1]*wr[i]
   out[i]=low*(1-wz[i])+high*wz[i]
  else:out[i]=0.
 return out

@njit(parallel=True,cache=True)
def continuum_transfer_reference(ff,fb3,fb5,n3,n5,Te,ds,fb_tail,n_tail,nfreq=32):
 n,ny=ff.shape;out=np.zeros(n);th=np.zeros(n)
 I3=1.5352049;I5=1.5343548;Itail=.8587344
 for p in prange(n):
  for f in range(nfreq):
   lam=(475+(f+.5)*50/nfreq)*1e-9;E=H*C/(lam*EV);dE=H*C/(EV*lam*lam)*50e-9/nfreq;I=0.
   for y in range(ny):
    T=max(Te[p,y]*K/EV,.001)
    jf=ff[p,y]/T*np.exp(-E/T)
    jb3=fb3[p,y]/T*np.exp(-(E-I3)/T)
    jb5=fb5[p,y]/T*np.exp(-(E-I5)/T)
    j=jf+jb3+jb5+fb_tail[p,y]/T*np.exp(-(E-Itail)/T)
    # Photoionization opacity of the two modeled levels with visible edges.
    ab=n3[p,y]*6.3e-22*np.sqrt(13.605693/I3)*(I3/E)**3+n5[p,y]*6.3e-22*np.sqrt(13.605693/I5)*(I5/E)**3+n_tail[p,y]*6.3e-22*np.sqrt(13.605693/Itail)*(Itail/E)**3
    nu=C/lam;af=ff[p,y]*C*C/(2*K*max(Te[p,y],1.)*nu**3)*(-np.expm1(-E/T))
    alpha=ab+af;dt=alpha*ds[y]
    if dt<1e-6:I=I*np.exp(-dt)+j*ds[y]*(1-dt/2)
    else:I=I*np.exp(-dt)+j/alpha*(-np.expm1(-dt))
    th[p]+=j*ds[y]*dE
   out[p]+=I*dE
 return out,th

@njit(parallel=True,cache=True)
def continuum_transfer(ff,fb3,fb5,n3,n5,Te,ds,fb_tail,n_tail):
 # Both opacity terms decrease with photon energy throughout this passband.
 # The red edge therefore bounds every optical depth. Analytic thin transfer
 # has a relative attenuation error <= 1-exp(-tau_bound) <= 1e-5.
 n,ny=ff.shape;out=np.zeros(n);thin=np.zeros(n);thick=np.zeros(n,np.bool_)
 emin=H*C/(525e-9*EV);emax=H*C/(475e-9*EV);nu=C/525e-9
 for p in prange(n):
  bound=0.;I=0.
  for y in range(ny):
   T=max(Te[p,y]*K/EV,.001)
   band=np.exp(-emin/T)*(-np.expm1(-(emax-emin)/T))
   I+=(ff[p,y]*band+fb3[p,y]*np.exp(-(emin-1.5352049)/T)*(-np.expm1(-(emax-emin)/T))+fb5[p,y]*np.exp(-(emin-1.5343548)/T)*(-np.expm1(-(emax-emin)/T))+fb_tail[p,y]*np.exp(-(emin-.8587344)/T)*(-np.expm1(-(emax-emin)/T)))*ds[y]
   ab=n3[p,y]*6.3e-22*np.sqrt(13.605693/1.5352049)*(1.5352049/emin)**3+n5[p,y]*6.3e-22*np.sqrt(13.605693/1.5343548)*(1.5343548/emin)**3+n_tail[p,y]*6.3e-22*np.sqrt(13.605693/.8587344)*(.8587344/emin)**3
   af=ff[p,y]*C*C/(2*K*max(Te[p,y],1.)*nu**3)*(-np.expm1(-emin/T))
   bound+=(ab+af)*ds[y]
  thin[p]=I;out[p]=I;thick[p]=bound>1e-5
 indices=np.where(thick)[0]
 if len(indices):
  full,unattenuated=continuum_transfer_reference(ff[indices],fb3[indices],fb5[indices],n3[indices],n5[indices],Te[indices],ds,fb_tail[indices],n_tail[indices])
  for j in range(len(indices)):out[indices[j]]=full[j];thin[indices[j]]=unattenuated[j]
 return out,thin

@njit(parallel=True,cache=True)
def line_transfer_reference(jj,alpha0,TT,vel,ds,wl):
 n,ny=jj.shape;out=np.zeros(n);thin=np.zeros(n);taumax=np.zeros(n)
 for p in prange(n):
  tau=0.;peak=0.;vmax=0.;bmax=0.;bmin=1e30
  for y in range(ny):
   thin[p]+=jj[p,y]*ds[y];tau+=alpha0[p,y]*ds[y];peak=max(peak,jj[p,y])
  taumax[p]=tau
  if tau<.005:out[p]=thin[p];continue
  if peak==0:continue
  for y in range(ny):
   if jj[p,y]>peak*1e-10 or alpha0[p,y]*ds[y]>.0001:
    b=np.sqrt(2*K*max(TT[p,y],150.)/M);bmax=max(bmax,b);bmin=min(bmin,b);vmax=max(vmax,abs(vel[p,y]))
  lim=vmax+7*bmax;nv=max(32,min(1500,int(np.ceil(2*lim/(bmin/3)))));dv=2*lim/nv
  for f in range(nv):
   v=-lim+(f+.5)*dv;I=0.
   for y in range(ny):
    b=np.sqrt(2*K*max(TT[p,y],150.)/M);profile=np.exp(-((v-vel[p,y])/b)**2)
    alpha=alpha0[p,y]*profile;j=jj[p,y]*profile/(np.sqrt(np.pi)*b);dt=alpha*ds[y]
    if dt<1e-6:I=I*np.exp(-dt)+j*ds[y]*(1-dt/2)
    else:I=I*np.exp(-dt)+j/alpha*(-np.expm1(-dt))
   out[p]+=I*dv
 return out,thin,taumax

@njit(parallel=True,cache=True)
def line_transfer(jj,alpha0,TT,vel,ds,wl):
 n,ny=jj.shape;out=np.zeros(n);thin=np.zeros(n);taumax=np.zeros(n)
 for p in prange(n):
  tau=0.;peak=0.;vmax=0.;bmax=0.;bmin=1e30
  for y in range(ny):
   thin[p]+=jj[p,y]*ds[y];tau+=alpha0[p,y]*ds[y];peak=max(peak,jj[p,y])
  taumax[p]=tau
  if tau<.005:out[p]=thin[p];continue
  if peak==0:continue
  lo=ny;hi=0
  for y in range(ny):
   # Outside this span, total omitted emission is <= 1e-10 of the
   # optically thin ray and total omitted optical depth is <= 1e-10.
   if jj[p,y]*ds[y]>thin[p]*1e-10/ny or alpha0[p,y]*ds[y]>1e-10/ny:
    lo=min(lo,y);hi=max(hi,y+1)
   if jj[p,y]>peak*1e-10 or alpha0[p,y]*ds[y]>.0001:
    b=np.sqrt(2*K*max(TT[p,y],150.)/M);bmax=max(bmax,b);bmin=min(bmin,b);vmax=max(vmax,abs(vel[p,y]))
  lim=vmax+7*bmax;nv=max(32,min(1500,int(np.ceil(2*lim/(bmin/3)))));dv=2*lim/nv
  for f in range(nv):
   v=-lim+(f+.5)*dv;I=0.
   for y in range(lo,hi):
    b=np.sqrt(2*K*max(TT[p,y],150.)/M);profile=np.exp(-((v-vel[p,y])/b)**2)
    alpha=alpha0[p,y]*profile;j=jj[p,y]*profile/(np.sqrt(np.pi)*b);dt=alpha*ds[y]
    if dt<1e-6:I=I*np.exp(-dt)+j*ds[y]*(1-dt/2)
    else:I=I*np.exp(-dt)+j/alpha*(-np.expm1(-dt))
   out[p]+=I*dv
 return out,thin,taumax

def main(case):
 prefix=R/'results'/case;d=json.loads(Path(str(prefix)+'_fields.json').read_text())
 a=np.memmap(str(prefix)+'_fields.bin',dtype='>f4',mode='r',shape=tuple(d['shape']));ex=d['expressions']
 rr=np.r_[0,d['r_um']];zz=np.r_[0,d['z_um']];t=np.array(d['times_s'])
 moving='surface_height_um' in d
 # A separate, explicitly reported lower optical layer is excluded for moving
 # surfaces. Its thin emitted power is audited below, rather than treating
 # unresolved nanometre rays as a complete moving-sheath calculation.
 moving_cut=float(os.environ.get('ROUTE1_MOVING_OPTICAL_CUT_UM','.1')) if moving else 0.
 omitted=[]
 old=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
 exp=np.load(old/'experiment/S4_reconstructed.npz');px=float(exp['pixel_um']);nz,nx=exp['gray'].shape[1:]
 x=(np.arange(nx)-(nx-1)/2)*px;z=(np.arange(nz)[::-1]+.5)*px
 # Symmetric adaptive sightline grid, 0.5 um in the dense core.
 los_limit=float(d['r_um'][-1])
 edges=np.unique(np.r_[-los_limit,np.arange(-los_limit,-100,10),np.arange(-100,-30,3),np.arange(-30,30.01,.5),np.arange(33,100,3),np.arange(100,los_limit,10),los_limit])
 yy=(edges[1:]+edges[:-1])/2;ds=np.diff(edges)*1e-6
 # Integrate finite object-plane pixels. Resolve the first few micrometres
 # above the target, which pixel-centre-only rays otherwise miss completely.
 quadrature={n:np.polynomial.legendre.leggauss(n) for n in [2,3,8,16]}
 sx=[];sz=[];pixel=[];weight=[]
 for iz,zc in enumerate(z):
  for ix,xc in enumerate(x):
   q,w=quadrature[8 if abs(xc)<60 and zc<100 else 2]
   if moving and iz==nz-1 and abs(xc)<60:
    hmax=max(np.max(d['surface_height_um']),0.)
    bounds=sorted(set([0.,min(px,hmax+moving_cut+.2),min(px,1.),min(px,3.),px]))
    zq,zw=quadrature[16]
    for lo,hi in zip(bounds[:-1],bounds[1:]):
     for j,yq in enumerate(zq):
      for k,xq in enumerate(q):
       sx.append(xc+xq*px/2);sz.append((lo+hi)/2+yq*(hi-lo)/2);pixel.append(iz*nx+ix);weight.append(zw[j]*(hi-lo)/px*w[k]/4)
   elif min(d['z_um'])<.01 and iz==nz-1 and abs(xc)<60:
    # Split the bottom pixel in z to retain emission from the resolved planar
    # nanometre sheath; an 8-point rule alone starts about 0.22 um above it.
    bounds=[0,.002,.01,.05,.2,1,3,px];zq,zw=quadrature[3]
    for lo,hi in zip(bounds[:-1],bounds[1:]):
     for j,yq in enumerate(zq):
      for k,xq in enumerate(q):
       sx.append(xc+xq*px/2);sz.append((lo+hi)/2+yq*(hi-lo)/2);pixel.append(iz*nx+ix);weight.append(zw[j]*(hi-lo)/px*w[k]/4)
   else:
    for j,zq in enumerate(q):
     for k,xq in enumerate(q):sx.append(xc+xq*px/2);sz.append(zc+zq*px/2);pixel.append(iz*nx+ix);weight.append(w[j]*w[k]/4)
 sx=np.array(sx);sz=np.array(sz);pixel=np.array(pixel);weight=np.array(weight)
 radius=np.sqrt(sx[:,None]**2+yy[None,:]**2)
 sample_r=radius.ravel();sample_z=np.broadcast_to(sz[:,None],radius.shape).ravel()
 ir=np.clip(np.searchsorted(rr,sample_r,side='right')-1,0,len(rr)-2).astype(np.int32)
 iz=np.clip(np.searchsorted(zz,sample_z,side='right')-1,0,len(zz)-2).astype(np.int32)
 wr=(sample_r-rr[ir])/(rr[ir+1]-rr[ir]);wz=(sample_z-zz[iz])/(zz[iz+1]-zz[iz])
 valid=(sample_r>=rr[0])&(sample_r<=rr[-1])&(sample_z>=zz[0])&(sample_z<=zz[-1])
 fixed_valid=valid.copy()
 projection=yy[None,:]/np.maximum(radius,1e-30)
 def bin_pixels(values):return np.bincount(pixel,weights=values*weight,minlength=nz*nx).reshape(nz,nx)
 chemistry=json.loads((R/'data/chemistry.json').read_text());rad=[];thin=[];tau=[];condensed_upper=[];lines=chemistry['lines'][2:6]
 rate=np.loadtxt(R/'data/log_rates.txt');mean=np.loadtxt(R/'data/rr_mean_energy.txt');rrs=[q for q in chemistry['reactions'] if q['name'].endswith('rr')]
 out=R/'results'/f'{case}_radiation';out.mkdir(exist_ok=True)
 set_num_threads(int(os.environ.get('ROUTE1_POST_THREADS','32')))
 camera_zero=float(os.environ.get('ROUTE1_CAMERA_ZERO_NS','118'))
 tmin=float(os.environ.get('ROUTE1_RADIATION_MIN_NS','-inf'));tmax=float(os.environ.get('ROUTE1_RADIATION_MAX_NS','inf'))
 candidates=np.flatnonzero((t*1e9>=tmin-1e-6)&(t*1e9<=tmax+1e-6))
 if os.environ.get('ROUTE1_RADIATION_EXPOSURES_ONLY','0')=='1':
  # Keep each full exposure and the saved knots that bracket its edges.
  # Disconnected exposures are explicitly recorded, never interpolated across.
  keep=np.zeros(len(t),dtype=bool)
  for center in exp['times_ns']+camera_zero:
   lo=center-5;hi=center+5
   if hi<t[0]*1e9-1e-6 or lo>t[-1]*1e9+1e-6:continue
   left=max(0,np.searchsorted(t*1e9,lo,side='right')-1)
   right=min(len(t),np.searchsorted(t*1e9,hi,side='left')+1)
   keep[left:right]=True
  candidates=candidates[keep[candidates]]
  if len(candidates)==0:
   # A solved stage may lie entirely between two camera gates. Retain its
   # real endpoints so stage merging remains explicit without inventing data.
   candidates=np.array([0,len(t)-1],dtype=int)
 stride=max(1,int(os.environ.get('ROUTE1_RADIATION_STRIDE','1')))
 indices=np.unique(np.r_[candidates[::stride],candidates[-1:]])
 spacing=float(os.environ.get('ROUTE1_RADIATION_MIN_SPACING_NS','0'))
 if spacing>0 and len(indices)>1:
  # Dense accepted-step diagnostics need not generate thousands of optical
  # fields. Retain real samples near exposure edges plus knots <= spacing apart.
  chosen=[indices[0]]
  edges_ns=np.r_[exp['times_ns']+camera_zero-5,exp['times_ns']+camera_zero+5]
  edge_indices=set()
  for edge in edges_ns:
   before=indices[t[indices]*1e9<=edge];after=indices[t[indices]*1e9>=edge]
   if len(before):edge_indices.add(int(before[-1]))
   if len(after):edge_indices.add(int(after[0]))
  for ii in indices[1:]:
   if t[ii]*1e9-t[chosen[-1]]*1e9>=spacing-1e-6 or int(ii) in edge_indices:chosen.append(ii)
  indices=np.unique(np.r_[chosen,indices[-1]])
 if not len(indices):raise ValueError('No saved times in requested radiation interval')
 for count,i in enumerate(indices):
  if moving:
   height=np.interp(sample_r,rr,np.r_[d['surface_height_um'][i][0],d['surface_height_um'][i]])
   distance=sample_z-height
   iz=np.clip(np.searchsorted(zz,distance,side='right')-1,0,len(zz)-2).astype(np.int32)
   wz=(distance-zz[iz])/(zz[iz+1]-zz[iz])
   valid=(sample_r<=rr[-1])&(distance>=moving_cut)&(distance<=zz[-1])
   # Looking toward increasing y: opaque copper hides every gas sample
   # behind the last solid crossing on that ray, including a raised rim.
   solid=(distance<0).reshape(-1,len(yy))
   occluded=np.maximum.accumulate(solid[:,::-1],axis=1)[:,::-1]
   valid &= ~occluded.ravel()
  def sample(name):
   field=np.array(a[i,ex.index(name)],dtype=float);field=np.pad(field,((1,0),(1,0)),mode='edge')
   field=np.nan_to_num(field,nan=0,posinf=0,neginf=0)
   return bilinear_sample(field,iz,ir,wz,wr,valid).reshape(-1,len(yy))
  if d.get('gas_volume_fraction_radiation',False):
   valid=fixed_valid.copy()
   solid=(sample('alphaGas')<.5)&(sample('rhoCondensed')>100)
   # Independent upper-bound diagnostic: an opaque copper crossing may emit
   # thermal continuum. Unit emissivity and no foreground absorption deliberately
   # overestimate it. Never add this bound to the native gas radiance or fit.
   has_surface=solid.any(axis=1);last=len(yy)-1-np.argmax(solid[:,::-1],axis=1)
   surfT=np.maximum(sample('Tg')[np.arange(len(last)),last],1.)
   bb=np.zeros(len(last));wavelength=(475+(np.arange(64)+.5)*50/64)*1e-9
   for lam in wavelength:
    bb+=2*H*C*C/lam**5/np.expm1(np.minimum(H*C/(lam*K*surfT),700))*50e-9/64
   condensed_upper.append(bin_pixels(np.where(has_surface,bb,0.)))
   occluded=np.maximum.accumulate(solid[:,::-1],axis=1)[:,::-1]
   valid &= ~occluded.ravel()
  Tg=np.maximum(sample('Tg'),150);Te=np.maximum(sample('TeK'),1);ur=sample('urP');v=ur*projection
  gas_fraction=np.clip(sample('alphaGas'),0,1) if d.get('gas_volume_fraction_radiation',False) else 1.
  ff=np.maximum(sample('Qff'),0)*gas_fraction/(4*np.pi)
  ne=np.maximum(sample('plas.ne'),0);ni=np.maximum(sample('nCu_1p'),0);logT=np.log10(np.maximum(Te*K/EV,.015));fb=[]
  for state in ['CuH3','CuH5']:
   col=chemistry['rate_columns'].index(state+'_rr')+1;mi=[q['name'] for q in rrs].index(state+'rr')+1
   alpha=10**np.interp(logT,rate[:,0],rate[:,col]);Eph=10**np.interp(logT,mean[:,0],mean[:,mi]);fb.append(ne*ni*alpha*Eph*EV*gas_fraction/(4*np.pi))
  fb_tail=np.zeros_like(ff);n_tail=np.zeros_like(ff)
  if 'tailPopulation' in ex:
   tail=np.loadtxt(R/'data/tail5d_log_rates.txt');n_tail=np.maximum(sample('tailPopulation'),0)
   alpha=10**np.interp(logT,tail[:,0],tail[:,5]);Eph=10**np.interp(logT,tail[:,0],tail[:,6]);fb_tail=ne*ni*alpha*Eph*EV/(4*np.pi)
  total,unattenuated=continuum_transfer(ff,fb[0],fb[1],np.maximum(sample('nCuH3'),0)*gas_fraction,np.maximum(sample('nCuH5'),0)*gas_fraction,Te,ds,fb_tail,n_tail);maxtau=np.zeros_like(total)
  for li,line in enumerate(lines,3):
   j=np.maximum(sample('jline'+str(li)),0)*gas_fraction;alpha=np.maximum(sample('tauLine'+str(li)+'/w0'),0)*gas_fraction
   I,ith,ta=line_transfer(j,alpha,Tg,v,ds,line['lambda_nm']*1e-9);total+=I;unattenuated+=ith;maxtau=np.maximum(maxtau,ta)
  rad.append(bin_pixels(total));thin.append(bin_pixels(unattenuated));tau.append(bin_pixels(maxtau))
  if moving:
   def native(name):return np.maximum(np.asarray(a[i,ex.index(name)],dtype=float),0.)
   evT=np.maximum(native('TeK')*K/EV,.001);elo=H*C/(525e-9*EV);ehi=H*C/(475e-9*EV)
   emiss=native('Qff')/(4*np.pi)*(np.exp(-elo/evT)-np.exp(-ehi/evT))
   for li in range(3,7):emiss+=native('jline'+str(li))
   for state,threshold in [('CuH3',1.5352049),('CuH5',1.5343548)]:
    col=chemistry['rate_columns'].index(state+'_rr')+1;mi=[q['name'] for q in rrs].index(state+'rr')+1
    alpha=10**np.interp(np.log10(np.maximum(evT,.015)),rate[:,0],rate[:,col]);energy=10**np.interp(np.log10(np.maximum(evT,.015)),mean[:,0],mean[:,mi])
    emiss+=native('plas.ne')*native('nCu_1p')*alpha*energy*EV/(4*np.pi)*(np.exp(-(elo-threshold)/evT)-np.exp(-(ehi-threshold)/evT))
   radial=np.trapz(np.pad(emiss,((0,0),(1,0)),mode='edge')*rr[None,:]*1e-6,rr*1e-6,axis=1)*2*np.pi
   radial=np.r_[radial[0],radial];mask=zz<=moving_cut+1e-12
   loss=float(np.trapz(radial[mask],zz[mask]*1e-6))
   invalid_layer=int(np.count_nonzero(~np.isfinite(emiss[np.array(d['z_um'])<=moving_cut+1e-12])))
   represented=float(rad[-1].sum()*(px*1e-6)**2)
   omitted.append({'time_ns':float(t[i]*1e9),'excluded_thin_power_W_sr':loss if np.isfinite(loss) else None,'image_power_W_sr':represented,'excluded_over_image':loss/max(represented,1e-300) if np.isfinite(loss) else None,'unresolved_layer_samples':invalid_layer,'layer_audit_available':bool(np.isfinite(loss))})
  print('RADIATION',count+1,len(indices),'time_ns',t[i]*1e9,flush=True)
 np.savez_compressed(out/'radiance.npz',times_s=t[indices],radiance=np.array(rad),optically_thin=np.array(thin),line_tau_bound=np.array(tau),pixel_um=px,x_um=x,z_um=z,pixel_quadrature_orders=np.array([2,8]))
 if condensed_upper:
  np.savez_compressed(out/'condensed_blackbody_upper_bound.npz',times_s=t[indices],radiance=np.array(condensed_upper),pixel_um=px,x_um=x,z_um=z)
  (out/'condensed_blackbody_upper_bound.json').write_text(json.dumps({'scope':'Opaque continuous copper thermal-radiance upper bound only; NOT added to gas light','emissivity':1,'foreground_absorption':False,'surface':'nearest condensed crossing with alphaGas<0.5 and rhoCondensed>100 kg/m3','temperature':'actual saved material temperature','band_nm':[475,525],'limitations':['not a droplet-cloud emissivity model','not energy-coupled','phase threshold and optical skin remain unresolved']} ,indent=2))
 if moving:
  (out/'moving_optical_audit.json').write_text(json.dumps({'coordinates':'r, z minus actual surface height; physical line of sight with opaque target occultation','excluded_distance_um':moving_cut,'scope':'bulk radiation only; excluded layer estimated from optically thin volume emissivity, not included in radiance','samples':omitted,'limitations':['sampled-volume quadrature is an emission estimate, not a rigorous numerical bound','camera crop starts at initial z=0; recessed emission below the crop is omitted','full moving-sheath ray integration remains unresolved']},indent=2))
 (out/'method.json').write_text(json.dumps({'camera_zero_model_ns':float(os.environ.get('ROUTE1_CAMERA_ZERO_NS','118')),'units':'W m^-2 sr^-1','band_nm':[475,525],'populations':'native non-LTE reaction transport, with optional eliminated 5d tail state','profiles':'thermal Doppler plus radial flow','continuum':'free-free and Kramers-Milne free-bound spectral transport, including optional 5d continuum; analytic band integral for optical depth bound <=1e-5, otherwise 32 band wavelengths','near_wall_pixels':'piecewise z quadrature at 0, 0.002, 0.01, 0.05, 0.2, 1, 3 um when nanometre samples are available','saved_source_times_s':t.tolist(),'radiation_source_indices':indices.tolist(),'exposures_only':os.environ.get('ROUTE1_RADIATION_EXPOSURES_ONLY','0')=='1','limitations':['no Stark/natural profile wings','hydrogenic approximate Cu photoionization and recombination','no absolute experimental radiometric calibration','experimental target plane not independently registered']},indent=2))
 print(out)
if __name__=='__main__':main(sys.argv[1])
