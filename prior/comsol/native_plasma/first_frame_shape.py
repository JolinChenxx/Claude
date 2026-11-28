"""Spatial diagnostic only; per-image fractions are NOT camera calibration."""
from pathlib import Path
import sys,json
import numpy as np
R=Path(__file__).resolve().parent;case=sys.argv[1]
old=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
sys.path.insert(0,str(old/'work'))
from camera_s4 import gate,blur
from scipy.ndimage import label
exp=np.load(old/'experiment/S4_reconstructed.npz');a=np.load(R/'results'/f'{case}_radiation/radiance.npz');px=float(exp['pixel_um'])
obs=exp['gray'][0].astype(float);bg=float(np.median(np.r_[obs[:5,:5].ravel(),obs[:5,-5:].ravel()]));obs=np.maximum(obs-bg,0)
sim=blur(gate(a['times_s']*1e9,a['radiance'],[118]),1)[0]
rows=[]
for frac in [.1,.2,.5]:
 row={'fraction_of_own_peak':frac}
 for name,field in [('experiment',obs),('simulation',sim)]:
  lab,n=label(field>=frac*field.max());sizes=np.bincount(lab.ravel());sizes[0]=0;mask=lab==sizes.argmax();iz,ix=np.where(mask)
  row[name]={'half_width_um':float((ix.max()-ix.min()+1)*px/2),'vertical_extent_um':float((iz.max()-iz.min()+1)*px),'area_um2':float(mask.sum()*px*px)}
 row['half_width_error_um']=abs(row['simulation']['half_width_um']-row['experiment']['half_width_um']);row['comparison_tolerance_um']=max(.2*row['experiment']['half_width_um'],2*px);rows.append(row)
out={'case':case,'note':'Largest connected component at fractions of each image own peak; background subtracted. This compares shape only and is not a calibrated intensity match.','experiment_background':bg,'experiment_peak_above_background':float(obs.max()),'rows':rows}
(R/'results'/f'{case}_first_frame_shape.json').write_text(json.dumps(out,indent=2));print(json.dumps(out,indent=2))
