"""Invert the published S2 color bar, preserving JPEG uncertainty and all S4 frames."""
from pathlib import Path
import csv,json
import numpy as np
from PIL import Image
from scipy.spatial import cKDTree
from scipy.ndimage import binary_opening,binary_closing,label
ROOT=Path(__file__).resolve().parents[1];D=ROOT/'experiment'
TIMES=[0,20,30,50,60,70,80,150,160,180,200,250,260,300,310,330,350,360,400,420,450,500,510,530,580,610,630,730,740]
PIXEL_UM=400/36.
def contour_mask(gray,threshold=27):
 mask=binary_closing(binary_opening(gray>threshold,structure=np.ones((3,3))),structure=np.ones((3,3)))
 ids,num=label(mask)
 if num:
  sizes=np.bincount(ids.ravel());sizes[0]=0;mask=ids==sizes.argmax()
 else:mask=np.zeros_like(mask)
 return mask

def metrics(gray,pixel_um=PIXEL_UM,threshold=27):
 mask=contour_mask(gray,threshold)
 yy,xx=np.where(mask)
 return dict(radius_um=float(max(mask.sum(axis=1),default=0)*pixel_um/2),area_mm2=float(mask.sum()*pixel_um**2/1e6),mean_gray=float(gray[mask].mean()) if len(yy) else None,integrated_gray=float(gray.sum()),top_um=float((gray.shape[0]-.5-yy.min())*pixel_um) if len(yy) else None,bottom_um=float((gray.shape[0]-.5-yy.max())*pixel_um) if len(yy) else None,above_threshold_pixels=int(mask.sum()),bright_height_um=float((yy.max()-yy.min()+1)*pixel_um) if len(yy) else 0.)
def main():
 s2=np.array(Image.open(D/'S2_original.jpeg').convert('RGB'));s4=np.array(Image.open(D/'S4_original.jpeg').convert('RGB'))
 # Original embedded image coordinates; avoid the dark border and tick labels.
 raw=np.median(s2[254:563,450:459],axis=1)
 g=np.arange(256);y=np.linspace(308,0,256);lut=np.stack([np.interp(y,np.arange(len(raw)),raw[:,c]) for c in range(3)],axis=1)
 tree=cKDTree(lut);frames=[];rgbframes=[];errors=[];crops=[];rows=[]
 xs=[10,109,210,309,408];ys=[21,146,271,396,521,646]
 for k,t in enumerate(TIMES):
  x=xs[k%5];y0=ys[k//5];rgb=s4[y0:y0+82,x:x+84,:];dist,gray=tree.query(rgb.reshape(-1,3));gray=gray.reshape(82,84).astype(float)
  frames.append(gray);rgbframes.append(rgb);errors.append(dist.reshape(82,84));crops.append({'time_ns':t,'bbox_xyxy':[x,y0,x+84,y0+82]})
  row={'time_ns':t,'split':'calibration' if t<=80 else 'holdout'}
  for threshold in [27,100,220]:
   row.update({f'{key}_thr{threshold}':value for key,value in metrics(gray,threshold=threshold).items()})
  row.update(color_RGB_rmse=float(np.sqrt(np.mean(dist**2))),color_distance_p95=float(np.percentile(dist,95)))
  rows.append(row)
 np.savez_compressed(D/'S4_reconstructed.npz',times_ns=TIMES,gray=np.array(frames),rgb=np.array(rgbframes),color_error=np.array(errors),lut_rgb=lut,pixel_um=PIXEL_UM)
 with (D/'S4_metrics.csv').open('w',newline='') as f:
  w=csv.DictWriter(f,fieldnames=rows[0].keys());w.writeheader();w.writerows(rows)
 manifest={'frame_count':29,'original_frame_size':[82,84],'scale_bar_um':400,'scale_bar_center_endpoints_pixels':[[428.5,692],[464.5,692]],'scale_bar_length_pixels':36,'pixel_um':PIXEL_UM,'scale_uncertainty':'Endpoint length +/-1 pixel; hence +/-2.8% linear scale before contour uncertainty.','cropping':crops,'colorbar_source':'S2 original embedded image, x450:459, y254:563, top255/bottom0','gray_status':'Reconstructed from published JPEG pseudocolor, NOT original CCD measurements.','thresholds':[27,100,220],'morphology':'3x3 square opening then closing; largest connected component','RGB_distance_p95':float(np.percentile(np.array(errors),95)),'missing_surface_reference':'S4 does not mark the copper surface. Heights use common bottom of frame as provisional origin; vertical translation is not independently verified.','all_frames_retained':29,'S7_conflicts':'Use S4 image measurements as primary; quoted radius/area/time conflicts are not independent fitting targets.'}
 (D/'reconstruction.json').write_text(json.dumps(manifest,indent=2)+'\n')
 print(json.dumps({'frame_count':29,'pixel_um':PIXEL_UM,'peak_radius':max(rows,key=lambda r:r['radius_um_thr27']),'peak_area':max(rows,key=lambda r:r['area_mm2_thr27'])},indent=2))
if __name__=='__main__':main()
