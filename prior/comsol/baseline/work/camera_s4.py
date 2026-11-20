"""One gain, one time shift; calibration 0--80 ns only; no per-frame rescaling."""
from pathlib import Path
import argparse,csv,json
import numpy as np
from scipy.ndimage import gaussian_filter
from extract_experiment import metrics
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'results'

def gate(t,images,centres,width=10):
 # Piecewise-linear temporal radiance integrated exactly over the gate by
 # inserting every saved knot; no extrapolation outside the solved interval.
 out=[]
 for tc in centres:
  lo=tc-width/2;hi=tc+width/2
  if lo<t[0]-1e-6 or hi>t[-1]+1e-6:raise ValueError('Gate outside available solution')
  knots=np.r_[lo,t[(t>lo)&(t<hi)],hi];jj=[]
  for x in knots:
   i=min(max(np.searchsorted(t,x)-1,0),len(t)-2);w=(x-t[i])/(t[i+1]-t[i]);jj.append(images[i]*(1-w)+images[i+1]*w)
  out.append(np.trapz(jj,x=knots,axis=0)/width)
 return np.array(out)

def blur(images,fwhm):return gaussian_filter(images,(0,fwhm/2.354820045,fwhm/2.354820045),mode='constant') if fwhm else images.copy()
def response(images,gain,background):return np.clip(background+gain*images,0,255)

def fit_global_gain(signal,target,background):
 # Exactly minimize the piecewise quadratic saturation objective for g>=0.
 # Every interval between successive saturation breakpoints is considered.
 # Tail sums scaled by their largest remaining signal avoid cancellation and
 # underflow across LTE's very large radiance dynamic range.
 x=np.asarray(signal,dtype=float).ravel();y=np.asarray(target,dtype=float).ravel()
 if np.any(x<0) or not np.all(np.isfinite(x)):raise ValueError('Nonphysical calibration radiance')
 positive=x>0;zero_cost=float(np.sum((background-y[~positive])**2))
 if not positive.any():return 0.,zero_cost/len(x)
 xx=x[positive];yy=y[positive];order=np.argsort(xx)[::-1];xx=xx[order];yy=yy[order]
 n=len(xx);A=np.empty(n);B=np.empty(n);A[-1]=1.;B[-1]=yy[-1]-background
 for i in range(n-2,-1,-1):
  ratio=xx[i+1]/xx[i];A[i]=1+ratio*ratio*A[i+1];B[i]=yy[i]-background+ratio*B[i+1]
 tail=np.cumsum(((yy-background)**2)[::-1])[::-1]
 saturated=np.r_[0,np.cumsum((255-yy)**2)[:-1]]
 lower=np.r_[0,(255-background)*xx[1:]/xx[:-1]]
 u=np.clip(B/A,lower,255-background)
 costs=zero_cost+saturated+tail+u*u*A-2*u*B
 best=int(np.argmin(costs));gain=float(u[best]/xx[best])
 # Evaluate the selected candidate directly, rather than subtracting sums.
 mse=float(np.mean((response(x,gain,background)-y)**2))
 return gain,mse

def calibrate(case,reference=None):
 e=np.load(ROOT/'experiment/S4_reconstructed.npz');a=np.load(RES/f'{case}_radiation/radiance.npz');t=a['times_s']*1e9;rad=a['radiance'];te=e['times_ns'];obs=e['gray'];early=te<=80
 # Published crop contains no independent surface marker. Sorted-pixel
 # calibration fits the brightness distribution, avoiding an invented image
 # translation. It does not use late radii, areas or brightness.
 corners=np.concatenate([obs[:,:5,:5].ravel(),obs[:,:5,-5:].ravel()]);bg=float(np.median(corners));bg=min(bg,26.)
 if reference:
  fit=json.loads(Path(reference).read_text());shift=fit['time_shift_ns'];gain=fit['gain'];bg=fit['background'];scores=[]
 else:
  target=np.sort(obs[early].reshape(early.sum(),-1),axis=1);scores=[]
  for shift in np.arange(60,120.01,2):
   pred=blur(gate(t,rad[:,0],te[early]+shift),1);flat=np.sort(pred.reshape(early.sum(),-1),axis=1)
   gain,mse=fit_global_gain(flat,target,bg)
   scores.append(dict(time_shift_ns=float(shift),gain=gain,mse=mse))
  fit=min(scores,key=lambda x:x['mse']);shift=fit['time_shift_ns'];gain=fit['gain']
  fit=dict(fit,background=bg,calibration_frames_ns=te[early].tolist(),holdout_frames_ns=te[~early].tolist(),gate_ns=10,PSF_FWHM_pixels=1,passband_nm=[475,525],gain_method='Global minimum of the piecewise-quadratic saturated response over g>=0, using sorted-pixel early-frame distributions. No arbitrary gain cap and no fitted spatial translation.',time_convention='experimental_ns = numerical_ns - time_shift_ns',saturation='One linear gain, measured common background, clip to [0,255]',reference_case=case)
 pred={};rows=[]
 for bi,name in enumerate(['475_525','490_510','450_550']):
  raw=gate(t,rad[:,bi],te+shift)
  for psf in ([0,1,2] if bi==0 else [1]):
   key=f'{name}_psf{psf}';image=response(blur(raw,psf),gain,bg);pred[key]=image
   for i,ns in enumerate(te):
    for threshold in [27,100,220]:
     row=dict(case=case,band=name,PSF_FWHM_pixels=psf,time_ns=int(ns),numerical_gate_center_ns=float(ns+shift),split='calibration' if ns<=80 else 'holdout',threshold=threshold)
     row.update(metrics(image[i],threshold=threshold));ex=metrics(obs[i],threshold=threshold)
     row.update({f'experiment_{k}':v for k,v in ex.items()})
     row['radius_absolute_error_um']=abs(row['radius_um']-ex['radius_um']);row['radius_tolerance_um']=max(.2*ex['radius_um'],2*float(e['pixel_um']))
     row['radius_within_target']=row['radius_absolute_error_um']<=row['radius_tolerance_um']
     row['height_absolute_error_um']=abs(row['bright_height_um']-ex['bright_height_um']);row['height_tolerance_um']=max(.2*ex['bright_height_um'],2*float(e['pixel_um']))
     row['height_within_target']=row['height_absolute_error_um']<=row['height_tolerance_um']
     row['net_integrated_gray']=row['integrated_gray']-bg*image[i].size;row['experiment_net_integrated_gray']=ex['integrated_gray']-bg*obs[i].size
     row['area_relative_error']=row['area_mm2']/ex['area_mm2']-1 if ex['area_mm2'] else None
     rows.append(row)
 out=RES/f'{case}_camera';out.mkdir(exist_ok=True)
 (out/'calibration.json').write_text(json.dumps(fit,indent=2));(out/'calibration_scan.json').write_text(json.dumps(scores,indent=2))
 np.savez_compressed(out/'images.npz',times_ns=te,experiment=obs,pixel_um=e['pixel_um'],**pred)
 with (out/'metrics.csv').open('w',newline='') as f:
  w=csv.DictWriter(f,fieldnames=rows[0]);w.writeheader();w.writerows(rows)
 main=[r for r in rows if r['band']=='475_525' and r['PSF_FWHM_pixels']==1 and r['threshold']==27 and r['split']=='holdout']
 earlyimg=pred['475_525_psf1'][early];early_rmse=float(np.sqrt(np.mean((np.sort(earlyimg.reshape(early.sum(),-1),axis=1)-np.sort(obs[early].reshape(early.sum(),-1),axis=1))**2)))
 primary=[r for r in rows if r['band']=='475_525' and r['PSF_FWHM_pixels']==1 and r['threshold']==27]
 # Ideal photon count before any intensifier gain, using the reported NA=.4,
 # perfect throughput/QE and 525 nm to maximize photons per band energy.
 # This is a physical scale diagnostic, not an additional fit parameter.
 from extract_experiment import contour_mask
 omega=2*np.pi*(1-np.sqrt(1-.4**2));photon_factor=omega*(float(e['pixel_um'])*1e-6)**2*10e-9*525e-9/(6.62607015e-34*299792458.)
 unscaled=blur(gate(t,rad[:,0],te+shift),1);photon_rows=[]
 for i,ns in enumerate(te):
  mask=contour_mask(pred['475_525_psf1'][i],27);photons=unscaled[i]*photon_factor
  photon_rows.append(dict(time_ns=int(ns),ideal_band_photons_total=float(photons.sum()),bright_mask_pixels=int(mask.sum()),bright_mask_mean_ideal_photons=float(photons[mask].mean()) if mask.any() else None,bright_mask_fraction_below_one_ideal_photon=float(np.mean(photons[mask]<1)) if mask.any() else None))
 (out/'photon_scale.json').write_text(json.dumps({'assumed_NA':.4,'throughput_and_QE':1,'wavelength_for_photon_upper_scale_nm':525,'gate_ns':10,'interpretation':'Ideal collection estimate before intensifier gain. A smooth fitted bright region dominated by much less than one photon/pixel is not physically validated by an arbitrary global gain. It is not a Poisson shot-noise simulation.','frames':photon_rows},indent=2))
 peak_report={}
 for key in ['radius_um','area_mm2','mean_gray','integrated_gray']:
  exv=np.array([r['experiment_'+key] if r['experiment_'+key] is not None else 0 for r in primary]);simv=np.array([r[key] if r[key] is not None else 0 for r in primary]);ie=int(exv.argmax());is_=int(simv.argmax())
  before=float(te[ie-1]) if ie else float(te[ie]);after=float(te[ie+1]) if ie+1<len(te) else float(te[ie]);allowed=max(float(te[ie])-before,after-float(te[ie]))
  peak_report[key]={'experiment_peak_time_ns':int(te[ie]),'simulated_peak_time_ns':int(te[is_]),'experiment_peak_value':float(exv[ie]),'simulated_peak_value':float(simv[is_]),'neighbor_frame_interval_ns':allowed,'time_peak_within_neighbor_interval':bool(abs(float(te[is_])-float(te[ie]))<=allowed)}
 extinct={}
 for threshold in [27,100,220]:
  rr=[r for r in rows if r['band']=='475_525' and r['PSF_FWHM_pixels']==1 and r['threshold']==threshold]
  extinct[str(threshold)]={'experiment_last_visible_ns':max((r['time_ns'] for r in rr if r['experiment_above_threshold_pixels']>0),default=None),'simulation_last_visible_ns':max((r['time_ns'] for r in rr if r['above_threshold_pixels']>0),default=None),'scope':'Only the 29 observed S4 frame times; a continuous extinction time is not inferred.'}
 summary={'case':case,'calibration':fit,'calibration_sorted_pixel_RMSE_counts':early_rmse,'holdout_radius_pass_count':sum(r['radius_within_target'] for r in main),'holdout_frame_count':len(main),'holdout_visible_experiment_frames':sum(r['experiment_above_threshold_pixels']>0 for r in main),'holdout_visible_radius_pass_count':sum(r['radius_within_target'] and r['experiment_above_threshold_pixels']>0 for r in main),'holdout_peak_simulated_area_mm2':max(r['area_mm2'] for r in main),'holdout_peak_experiment_area_mm2':max(r['experiment_area_mm2'] for r in main),'peak_timing':peak_report,'extinction_at_observed_frames':extinct,'height_limitation':'Published frames have no surface mark. Experimental top/bottom use crop-bottom origin; simulated origin is the physical target plane. Absolute heights are not independently registered.','early_data_not_independent_validation':True}
 (out/'summary.json').write_text(json.dumps(summary,indent=2));print(json.dumps(summary,indent=2));return summary

def verify():
 t=np.array([0.,2.,4.,6.,8.,10.]);v=np.broadcast_to(t[:,None,None],(6,2,2));assert np.allclose(gate(t,v,[5],10),5)
 assert np.allclose(response(np.array([0,1,2]),100,3),[3,103,203]);assert np.array_equal(response(np.array([3]),100,3),[255])
 g,mse=fit_global_gain(np.array([1.,2.,3.]),np.array([5.,7.,9.]),3);assert abs(g-2)<1e-12 and mse<1e-20
 g,mse=fit_global_gain(np.array([1e-25,1e-25,1.]),np.array([50.,100.,255.]),2)
 assert abs(g/7.3e26-1)<1e-12 and abs(mse-1250/3)<1e-8
 grid=np.exp(np.linspace(-30,80,100000));x=np.array([1e-25,1e-25,1.]);y=np.array([50.,100.,255.]);brute=np.mean((response(grid[:,None]*x,1.,2)-y)**2,axis=1).min();assert mse<=brute+1e-8
 (RES/'camera_unit_audit.json').write_text(json.dumps({'piecewise_linear_gate':'pass','global_response_and_saturation':'pass','zero_signal_background':'pass','exact_uncapped_gain':'pass','gain_far_above_previous_cap':'pass','dense_independent_gain_scan':'pass'},indent=2))

if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('case',nargs='?');p.add_argument('--reference');p.add_argument('--verify',action='store_true');a=p.parse_args()
 if a.verify:verify()
 elif a.case:calibrate(a.case,a.reference)
