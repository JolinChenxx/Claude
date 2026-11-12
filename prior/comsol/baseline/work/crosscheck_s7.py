"""Digitize S7 scatter markers as a crosscheck, never an additional fit target."""
from pathlib import Path
import csv,json
import numpy as np
from PIL import Image
from scipy.ndimage import label,center_of_mass
ROOT=Path(__file__).resolve().parents[1];D=ROOT/'experiment';im=np.array(Image.open(D/'S7_original.png').convert('RGB'));rows=[]
# Pixel calibrations read from the native embedded image axis tick centres.
# S7 has more scatter points/repeats than the 29 published S4 frame selections.
panels=[('mean_gray',(110,45,412,378),(126.5,385.,0,800),(381.,58.5,20,90)),('radius_um',(931,43,1453,381),(948.,1407.,0,1400),(373.,65.,0,300)),('area_mm2',(110,501,665,854),(126.5,616.,0,1400),(850.,534.,0,.25))]
for name,(x0,y0,x1,y1),xcal,ycal in panels:
 rgb=im[y0:y1,x0:x1];mask=(rgb.max(2)<85)&(rgb.max(2)-rgb.min(2)<25);ids,n=label(mask)
 for k in range(1,n+1):
  yy,xx=np.where(ids==k);area=len(xx)
  if not (14<=area<=180 and 3<=np.ptp(xx)+1<=20 and 3<=np.ptp(yy)+1<=20):continue
  x=x0+xx.mean();y=y0+yy.mean();t=(x-xcal[0])/(xcal[1]-xcal[0])*(xcal[3]-xcal[2])+xcal[2];v=(y-ycal[0])/(ycal[1]-ycal[0])*(ycal[3]-ycal[2])+ycal[2]
  if t< -10:continue
  rows.append(dict(quantity=name,time_ns=float(t),value=float(v),marker_x_pixel=float(x),marker_y_pixel=float(y),marker_area_pixel=area))
with (D/'S7_digitized_crosscheck.csv').open('w',newline='') as f:w=csv.DictWriter(f,fieldnames=rows[0]);w.writeheader();w.writerows(rows)
report={'role':'Crosscheck only; not fitted jointly with S4. Tick calibration and marker selection approximately 1-2 pixels, not raw data.','markers':{name:len([r for r in rows if r['quantity']==name]) for name,*_ in panels},'S7_peak_radius_um':max(r['value'] for r in rows if r['quantity']=='radius_um'),'S7_peak_area_mm2':max(r['value'] for r in rows if r['quantity']=='area_mm2'),'S4_peak_area_mm2':None,'conflicts':'S7 scatter includes extra/repeated times. S7 area ordinate reaches about 0.27 mm2, incompatible with supplementary text 0.0296 mm2. S4 has no 280 ns frame; do not impose text 280 ns as a common fit target.'}
with (D/'S4_metrics.csv').open() as f:report['S4_peak_area_mm2']=max(float(r['area_mm2_thr27']) for r in csv.DictReader(f))
(D/'S7_crosscheck.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
