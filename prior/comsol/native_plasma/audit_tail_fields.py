"""Check eliminated-level timescale and inventory against actual saved fields."""
from pathlib import Path
import json,sys
import numpy as np
R=Path(__file__).resolve().parent;c=sys.argv[1]
d=json.loads((R/'results'/f'{c}_fields.json').read_text());a=np.memmap(R/'results'/f'{c}_fields.bin',dtype='>f4',mode='r',shape=tuple(d['shape']));ex=d['expressions'];rows=[]
for i,t in enumerate(d['times_s']):
 n=np.asarray(a[i,ex.index('tailPopulation')],float);loss=np.asarray(a[i,ex.index('tailUpperLoss')],float);cu=np.asarray(a[i,ex.index('nCuTotal')],float);photo=np.asarray(a[i,ex.index('tailQphoto')],float)
 use=np.isfinite(n)&np.isfinite(loss)&np.isfinite(cu)&(cu>1e18)&(n>max(np.nanmax(n)*1e-5,1))
 rows.append({'model_time_ns':t*1e9,'active_sample_count':int(use.sum()),'max_tail_fraction_of_parent':float(np.nanmax(a[i,ex.index('tailFraction')][use])) if use.any() else None,'max_tail_fraction_of_total_Cu':float(np.nanmax(n[use]/cu[use])) if use.any() else None,'max_relaxation_ns':float(np.nanmax(1e9/loss[use])) if use.any() else None,'max_photo_power_W_m3':float(np.nanmax(photo))})
p=R/'results'/f'{c}_tail_audit.json';p.write_text(json.dumps({'case':c,'rows':rows,'acceptance':'QSS requires population relaxation faster than changes in source and transport; omitted population inventory must remain small. No automatic acceptance from a finite solution.','cross_sections':'Hydrogenic approximation, not measured copper photoionization'},indent=2));print(p)
