"""Retain evidence of the obsolete gain cap without overwriting old artifacts."""
from pathlib import Path
import json
import numpy as np
from camera_s4 import fit_global_gain,gate,blur
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'results'
if __name__=='__main__':
 e=np.load(ROOT/'experiment/S4_reconstructed.npz');a=np.load(RES/'H00_radiation/radiance.npz');old=json.loads((RES/'H00_camera/calibration.json').read_text())
 early=e['times_ns']<=80;target=np.sort(e['gray'][early].reshape(early.sum(),-1),axis=1);records=[]
 for shift in range(60,121,2):
  raw=blur(gate(a['times_s']*1e9,a['radiance'][:,0],e['times_ns'][early]+shift),1)
  x=np.sort(raw.reshape(early.sum(),-1),axis=1);g,mse=fit_global_gain(x,target,old['background'])
  records.append({'shift_ns':shift,'gain':g,'mse':mse,'log_gain_relative_to_peak_255':float(np.log(max(g*x.max()/255,1e-300)))})
 oldraw=blur(gate(a['times_s']*1e9,a['radiance'][:,0],e['times_ns'][early]+old['time_shift_ns']),1)
 report={'case':'H00','numerical_result_is_diagnostic':True,'old_calibration_unchanged':old,'old_optimizer_logg_at_cap':float(np.log(old['gain']*oldraw.max()/255)),'old_cap':18,'new_uncapped_calibration':min(records,key=lambda x:x['mse']),'all_new_offsets':records,'warning':'Do not use H00 as final evidence; numerical/interface audit failed. This records the calibration algorithm correction only.'}
 (RES/'H00_gain_cap_audit.json').write_text(json.dumps(report,indent=2));print(json.dumps({k:v for k,v in report.items() if k not in ['old_calibration_unchanged','all_new_offsets']},indent=2))
