"""Sensitivity of published-image reconstruction, not invented CCD error bars."""
from pathlib import Path
import csv,json
import numpy as np
from scipy.ndimage import label
from scipy.spatial import cKDTree
from extract_experiment import metrics
ROOT=Path(__file__).resolve().parents[1];D=ROOT/'experiment';a=np.load(D/'S4_reconstructed.npz');lut=a['lut_rgb'];tree=cKDTree(lut);rows=[]
for i,t in enumerate(a['times_ns']):
 gray=a['gray'][i];rgb=a['rgb'][i].reshape(-1,3);dist,near=tree.query(rgb,k=2)
 gray2=near[:,1].reshape(gray.shape).astype(float);rawmask=gray>27;ids,n=label(rawmask)
 if n:sizes=np.bincount(ids.ravel());sizes[0]=0;rawmask=ids==sizes.argmax()
 r={'time_ns':int(t),'radius_um_main':metrics(gray)['radius_um'],'area_mm2_main':metrics(gray)['area_mm2'],'radius_um_scale_low':metrics(gray,pixel_um=400/37)['radius_um'],'radius_um_scale_high':metrics(gray,pixel_um=400/35)['radius_um'],'area_mm2_scale_low':metrics(gray,pixel_um=400/37)['area_mm2'],'area_mm2_scale_high':metrics(gray,pixel_um=400/35)['area_mm2'],'radius_um_no_morphology':max(rawmask.sum(1))*float(a['pixel_um'])/2,'area_mm2_no_morphology':rawmask.sum()*float(a['pixel_um'])**2/1e6,'radius_um_second_LUT_neighbor':metrics(gray2)['radius_um'],'area_mm2_second_LUT_neighbor':metrics(gray2)['area_mm2'],'gray_nearest_neighbor_mean_difference':float(np.abs(gray-gray2).mean())}
 rows.append(r)
with (D/'reconstruction_sensitivity.csv').open('w',newline='') as f:
 w=csv.DictWriter(f,fieldnames=rows[0]);w.writeheader();w.writerows(rows)
report={'scale_interval':'Bar endpoints 400 um / (36 +/- 1) pixels; not a confidence interval.','morphology':'Main 3x3 opening+closing largest component vs largest component without opening/closing.','LUT':'Nearest vs second-nearest RGB colorbar match; JPEG and published pseudocolor uncertainty, not raw camera noise.','max_morphology_radius_difference_um':max(abs(r['radius_um_main']-r['radius_um_no_morphology']) for r in rows),'max_LUT_radius_difference_um':max(abs(r['radius_um_main']-r['radius_um_second_LUT_neighbor']) for r in rows),'height_reference':'Unverified crop-bottom origin; absolute top/bottom not an independent spatial fit constraint.'}
(D/'uncertainty_audit.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
