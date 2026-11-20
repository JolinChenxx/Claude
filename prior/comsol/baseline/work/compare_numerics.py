"""Compare final saved solutions with frozen main-camera gain and time offset."""
from pathlib import Path
import argparse,csv,json
import numpy as np
from camera_s4 import gate,blur
from convergence_metrics import assess,combined
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'results'

def finite_json(value):
 if isinstance(value,dict):return {k:finite_json(v) for k,v in value.items()}
 if isinstance(value,list):return [finite_json(v) for v in value]
 if isinstance(value,(float,np.floating)) and not np.isfinite(value):return None
 return value
def compare(main,variants,output_stem='numerical_comparison'):
 if Path(output_stem).name!=output_stem:raise ValueError('Output stem must be a plain filename')
 base=np.load(RES/f'{main}_camera/images.npz');brows=list(csv.DictReader((RES/f'{main}_camera/metrics.csv').open()));brows=[r for r in brows if r['band']=='475_525' and r['PSF_FWHM_pixels']=='1' and r['threshold']=='27'];report={};allrows=[]
 fit=json.loads((RES/f'{main}_camera/calibration.json').read_text())
 for case in variants:
  ff=json.loads((RES/f'{case}_camera/calibration.json').read_text())
  for key in ['time_shift_ns','gain','background']:assert ff[key]==fit[key],f'Refitted response in {case}'
  rows=list(csv.DictReader((RES/f'{case}_camera/metrics.csv').open()));rows=[r for r in rows if r['band']=='475_525' and r['PSF_FWHM_pixels']=='1' and r['threshold']=='27'];assert [r['time_ns'] for r in rows]==[r['time_ns'] for r in brows]
  data={}
  for key in ['radius_um','area_mm2','integrated_gray']:
   bv=np.array([float(r[key]) for r in brows]);vv=np.array([float(r[key]) for r in rows])
   if key=='integrated_gray':
    # Remove identical fixed camera background for a physical brightness test.
    bv-=fit['background']*82*84;vv-=fit['background']*82*84
   active=bv>=max(bv.max()*.01,1e-20);rel=np.abs(vv-bv)/np.maximum(abs(bv),1e-30)
   new_signal=bool(np.any((~active)&(vv>max(bv.max()*.01,1e-20))))
   data[key]=assess(bv,vv,base['times_ns'],floor=1e-20)
   for i,t in enumerate(base['times_ns']):allrows.append({'variant':case,'time_ns':int(t),'quantity':key,'main_value':float(bv[i]),'variant_value':float(vv[i]),'relative_difference':float(rel[i]) if active[i] else None,'within_active_range':bool(active[i])})
  rad0=np.load(RES/f'{main}_radiation/radiance.npz');rad1=np.load(RES/f'{case}_radiation/radiance.npz');peak0=float(rad0['radiance'][:,0].max());peak1=float(rad1['radiance'][:,0].max())
  # Saturation can hide a change in actual radiance. Disclose the unsaturated
  # physical band brightness alongside the requested camera-response metrics.
  raw=[]
  try:
   for rr in [rad0,rad1]:
    gated=blur(gate(rr['times_s']*1e9,rr['radiance'][:,0],base['times_ns']+fit['time_shift_ns']),1)
    raw.append(gated.sum(axis=(1,2)))
   physical=assess(raw[0],raw[1],base['times_ns'])
   physical.update(main_peak=float(raw[0].max()),variant_peak=float(raw[1].max()),units='Sum of gated pixel radiance, W m^-2 sr^-1; before gain and saturation')
   threshold=physical.get('activity_threshold',float('inf'))
   for i,t in enumerate(base['times_ns']):
    active=bool(np.isfinite(raw[0][i]) and raw[0][i]>=threshold)
    relative=float(abs(raw[1][i]-raw[0][i])/max(abs(raw[0][i]),1e-100)) if active else None
    allrows.append({'variant':case,'time_ns':int(t),'quantity':'unsaturated_integrated_radiance','main_value':float(raw[0][i]),'variant_value':float(raw[1][i]),'relative_difference':relative,'within_active_range':active})
  except ValueError as exc:
   physical={'within_10pct':False,'status':'failed','reason':'invalid_or_incomplete_time_gate','detail':str(exc),'active_frames':0,'max_relative_change_above_1pct_peak':None}
  report[case]={'camera_response_frozen':True,'metrics':data,'unsaturated_physical_brightness':physical,'local_peak_radiance_relative_change':peak1/max(peak0,1e-100)-1,'all_primary_within_10pct':all(d['within_10pct'] for d in data.values()),'all_camera_and_physical_within_10pct':combined(data,physical),'numerical_audit':json.loads((RES/f'{case}_audit.json').read_text())['status']}
 report=finite_json(report)
 (RES/f'{output_stem}.json').write_text(json.dumps({'main':main,'variants':report,'threshold_note':'27; report nonzero signals above 1% of main peak, disclose zero-signal changes separately; no per-case response refit.'},indent=2,allow_nan=False))
 with (RES/f'{output_stem}.csv').open('w',newline='') as f:w=csv.DictWriter(f,fieldnames=allrows[0]);w.writeheader();w.writerows(allrows)
 print(json.dumps(report,indent=2));return report
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('main');p.add_argument('variants',nargs='+');p.add_argument('--output-stem',default='numerical_comparison');a=p.parse_args();compare(a.main,a.variants,a.output_stem)
