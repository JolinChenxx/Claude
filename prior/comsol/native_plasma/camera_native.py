"""S4 comparison using native non-LTE radiance, fixed timing and one frozen gain."""
from pathlib import Path
import sys,json,csv,os
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent
OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
sys.path.insert(0,str(OLD/'work'))
from camera_s4 import gate,blur,response,fit_global_gain
from extract_experiment import metrics,contour_mask

def exposure_available(t,lo,hi,max_gap):
 if lo<t[0]-1e-6 or hi>t[-1]+1e-6:return False
 left=max(0,np.searchsorted(t,lo,side='right')-1);right=min(len(t),np.searchsorted(t,hi,side='left')+1)
 return bool(np.all(np.diff(t[left:right])<=max_gap+1e-6))

def main(case,freeze=False,provisional=False):
 e=np.load(OLD/'experiment/S4_reconstructed.npz');d=np.load(R/'results'/f'{case}_radiation/radiance.npz')
 t=d['times_s']*1e9;te=e['times_ns'];obs=e['gray'];px=float(e['pixel_um']);rad=d['radiance']
 method=json.loads((R/'results'/f'{case}_radiation/method.json').read_text())
 full=np.array(method.get('saved_source_times_s',d['times_s']))*1e9
 max_gap=float(np.max(np.diff(full)))
 camera_zero=float(os.environ.get('ROUTE1_CAMERA_ZERO_NS','118'))
 available=np.array([exposure_available(t,ns+camera_zero-5,ns+camera_zero+5,max_gap) for ns in te]);early=te<=80
 if not np.all(available[early]):raise ValueError('Need complete early exposure interval before freezing calibration')
 raw=blur(gate(t,rad,te[available]+camera_zero),1);exp=obs[available];times=te[available]
 ref=Path(os.environ.get('ROUTE1_CAMERA_CALIBRATION',str(R/'data/camera_calibration_frozen.json')))
 if ref.exists():
  if freeze:raise ValueError('Calibration already frozen; use existing reference without --freeze')
  fit=json.loads(ref.read_text())
 else:
  if not freeze and not provisional:raise ValueError('Use --freeze for the selected main candidate, or --provisional for diagnosis')
  bg=min(float(np.median(np.concatenate([obs[:,:5,:5].ravel(),obs[:,:5,-5:].ravel()]))),26.)
  early_available=times<=80
  gain,mse=fit_global_gain(np.sort(raw[early_available].reshape(early_available.sum(),-1),axis=1),np.sort(exp[early_available].reshape(early_available.sum(),-1),axis=1),bg)
  fit={'reference_case':case,'gain':gain,'background':bg,'time_shift_ns':camera_zero,'gate_ns':10,'PSF_FWHM_pixels':1,'passband_nm':[475,525],'calibration_frames_ns':te[early].tolist(),'sorted_pixel_mse':mse,'method':'One global saturated linear response; early sorted-pixel distributions; no fitted translation or time shift'}
  fit['status']='frozen' if freeze else 'provisional, not validation'
  if freeze:ref.write_text(json.dumps(fit,indent=2))
 images=response(raw,fit['gain'],fit['background']);out=R/'results'/f'{case}_camera';out.mkdir(exist_ok=True)
 np.savez_compressed(out/'images.npz',times_ns=times,experiment=exp,prediction=images,radiance=raw,pixel_um=px)
 rows=[];ph=[];omega=2*np.pi*(1-np.sqrt(1-.4**2));factor=omega*(px*1e-6)**2*10e-9*525e-9/(6.62607015e-34*299792458.)
 for i,ns in enumerate(times):
  for threshold in [27,100,220]:
   sim=metrics(images[i],px,threshold);ex=metrics(exp[i],px,threshold)
   row={'case':case,'time_ns':int(ns),'threshold':threshold,'split':'calibration' if ns<=80 else 'holdout',**sim,**{'experiment_'+k:v for k,v in ex.items()}}
   for k in ['radius_um','bright_height_um']:
    row[k+'_error']=abs(sim[k]-ex[k]);row[k+'_tolerance']=max(.2*ex[k],2*px);row[k+'_pass']=row[k+'_error']<=row[k+'_tolerance']
   row['net_integrated_gray']=sim['integrated_gray']-fit['background']*images[i].size;row['experiment_net_integrated_gray']=ex['integrated_gray']-fit['background']*images[i].size
   row['area_relative_error']=sim['area_mm2']/ex['area_mm2']-1 if ex['area_mm2'] else None
   row['area_pass']=abs(row['area_relative_error'])<=.3 if row['area_relative_error'] is not None else sim['area_mm2']==0
   rows.append(row)
  mask=contour_mask(images[i]);photons=raw[i]*factor
  ph.append({'time_ns':int(ns),'peak_radiance_W_m2_sr':float(raw[i].max()),'ideal_total_photons':float(photons.sum()),'mean_photons_in_bright_mask':float(photons[mask].mean()) if mask.any() else None,'bright_fraction_below_one_photon':float((photons[mask]<1).mean()) if mask.any() else None})
 with (out/'metrics.csv').open('w',newline='') as f:w=csv.DictWriter(f,fieldnames=rows[0]);w.writeheader();w.writerows(rows)
 mainrows=[x for x in rows if x['threshold']==27];hold=[x for x in mainrows if x['split']=='holdout'];peaks={}
 for k in ['radius_um','area_mm2','net_integrated_gray']:
  s=max(mainrows,key=lambda q:q[k]);ex=max(mainrows,key=lambda q:q['experiment_'+k]);peaks[k]={'simulation_peak_ns':s['time_ns'],'experiment_peak_ns':ex['time_ns']}
 summary={'case':case,'calibration':fit,'observed_frames_covered':len(times),'total_observed_frames':len(te),'holdout_radius_pass':sum(q['radius_um_pass'] for q in hold),'holdout_count':len(hold),'peaks_within_covered_interval':peaks,'limitations':['No independent electron-density measurement','No independent target-plane registration in S4','JPEG reconstructed intensity; relative calibration only'],'ideal_photon_assumptions':{'NA':.4,'QE':1,'throughput':1,'wavelength_nm':525},'photons':ph}
 summary['calibration_status']=fit.get('status','frozen')
 summary['missing_frames_ns']=te[~available].tolist()
 summary['extinction_at_observed_frames']={}
 summary['visible_frame_scores']={}
 for threshold in [27,100,220]:
  rr=[q for q in rows if q['threshold']==threshold]
  summary['extinction_at_observed_frames'][str(threshold)]={'experiment_last_visible_ns':max((q['time_ns'] for q in rr if q['experiment_above_threshold_pixels']>0),default=None),'simulation_last_visible_ns':max((q['time_ns'] for q in rr if q['above_threshold_pixels']>0),default=None),'scope':'Covered observed frame times only'}
  vv=[q for q in rows if q['threshold']==threshold and q['experiment_above_threshold_pixels']>0]
  summary['visible_frame_scores'][str(threshold)]={'count':len(vv),'radius_pass':sum(q['radius_um_pass'] for q in vv),'height_span_pass':sum(q['bright_height_um_pass'] for q in vv),'both_lengths_pass':sum(q['radius_um_pass'] and q['bright_height_um_pass'] for q in vv),'area_pass':sum(q['area_pass'] for q in vv)}
 (out/'summary.json').write_text(json.dumps(summary,indent=2));figout=R/'figures'/case;figout.mkdir(exist_ok=True)
 def panels(indices,path):
  fig,axs=plt.subplots(2,len(indices),figsize=(len(indices)*1.8+1,4.6),squeeze=False,layout='constrained')
  for j,i in enumerate(indices):
   for row,values in enumerate([exp,images]):
    axs[row,j].imshow(values[i],vmin=0,vmax=255,cmap='inferno');axs[row,j].set_axis_off()
   axs[0,j].set_title(f'{times[i]} ns')
  for j,label in enumerate(['S4',case]):axs[j,0].text(-.1,.5,label,transform=axs[j,0].transAxes,rotation=90,va='center')
  fig.suptitle(f"475-525 nm; 10 ns gate; fixed {camera_zero:g} ns offset; {summary['calibration_status']}\nSame physical pixel scale; target plane not registered in S4")
  fig.savefig(path,dpi=170);plt.close(fig)
 chosen=np.unique(np.round(np.linspace(0,len(times)-1,min(8,len(times)))).astype(int))
 panels(chosen,figout/'S4_comparison.png')
 for start in range(0,len(times),8):panels(np.arange(start,min(start+8,len(times))),figout/f'S4_all_frames_page{start//8+1}.png')
 fig,axs=plt.subplots(2,2,figsize=(10,7),layout='constrained')
 for ax,k,title in zip(axs.flat,['radius_um','area_mm2','net_integrated_gray','bright_height_um'],['Bright radius [um]','Bright area [mm2]','Background-subtracted integrated intensity','Bright height [um]']):
  ax.plot(times,[q['experiment_'+k] for q in mainrows],'ko-',label='S4');ax.plot(times,[q[k] for q in mainrows],'r.-',label=case);ax.set(xlabel='Experimental time [ns]',ylabel=title);ax.grid(alpha=.2);ax.legend()
 fig.savefig(figout/'S4_metrics.png',dpi=160);plt.close(fig);print(json.dumps(summary,indent=2))
if __name__=='__main__':main(sys.argv[1],freeze='--freeze' in sys.argv,provisional='--provisional' in sys.argv)
