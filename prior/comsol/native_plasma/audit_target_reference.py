"""Measure the published high-fluence S14 composite relative to its target line.

Secondary reference only. Do not alter the inherited S4 scale or registration.
"""
from pathlib import Path
import json,csv,sys
import numpy as np
from PIL import Image
from scipy.spatial import cKDTree
from scipy.ndimage import binary_opening,binary_closing,label
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
e=np.load(OLD/'experiment/S4_reconstructed.npz');lut=e['lut_rgb'];tree=cKDTree(lut)
src=R/'figures/target_reference/supplement_page15_image0.jpeg'
im=np.array(Image.open(src).convert('RGB'));px=200/78
panels=[(150,0,193,188),(210,222,415,411),(330,445,639,634)]
rows=[];fig,axes=plt.subplots(1,3,figsize=(13,5),layout='constrained')
for ax,(tt,y0,y1,baseline) in zip(axes,panels):
    rgb=im[y0:y1,:267];dist,gray=tree.query(rgb.reshape(-1,3));gray=gray.reshape(rgb.shape[:2]);dist=dist.reshape(gray.shape)
    color=rgb.max(2).astype(float)-rgb.min(2).astype(float)
    z=(baseline-y0-np.arange(gray.shape[0]))*px;x=(np.arange(267)-133)*px
    ax.imshow(rgb,extent=[x[0],x[-1],z[-1],z[0]]);ax.axhline(0,c='white',ls='--',lw=1)
    ax.set_title(f'S14: {tt} ns');ax.set_xlabel('x (um)');ax.set_ylabel('Height above target (um)')
    for thr in [40,80,120]:
        mask=binary_closing(binary_opening((gray>thr)&(color>50)&(dist<65),np.ones((3,3))),np.ones((3,3)))
        ids,n=label(mask)
        if not n:continue
        sizes=np.bincount(ids.ravel());sizes[0]=0;mask=ids==sizes.argmax();yy,xx=np.where(mask)
        row=dict(experiment_time_ns=tt,threshold=thr,radius_um=float(mask.sum(1).max()*px/2),height_span_um=float((yy.max()-yy.min()+1)*px),bottom_above_target_um=float(z[yy.max()]),top_above_target_um=float(z[yy.min()]),centroid_height_um=float((gray[mask]*z[yy]).sum()/gray[mask].sum()))
        rows.append(row)
        if thr==80:ax.contour(x,z,mask.astype(float),levels=[.5],colors='magenta',linewidths=.6)
    ax.set_ylim(-10,450)
fig.suptitle('Published S14 composite: secondary target-position reference\nMagenta: reconstructed gray80 contour; white dashed: visible copper surface; no simulation data',fontsize=11)
fig.savefig(R/'figures/target_reference/S14_target_reference.png',dpi=170);plt.close(fig)
with (R/'results/S14_target_reference.csv').open('w',newline='') as f:
    w=csv.DictWriter(f,fieldnames=rows[0]);w.writeheader();w.writerows(rows)
compare=[]
sys.path.insert(0,str(OLD/'work'));from extract_experiment import metrics
for row in rows:
    tt=row['experiment_time_ns']
    if tt not in e['times_ns']:continue
    s4=metrics(e['gray'][np.flatnonzero(e['times_ns']==tt)[0]],float(e['pixel_um']),row['threshold'])
    compare.append(dict(time_ns=tt,threshold=row['threshold'],S14_radius_um=row['radius_um'],S4_radius_um=s4['radius_um'],S14_to_S4_radius_ratio=row['radius_um']/s4['radius_um'] if s4['radius_um'] else None))
report=dict(source=str(src),source_publication='Supplement S14, DOI10.1002/lpor.202400388',scale_bar_um=200,scale_bar_width_pixels=78,pixel_um=px,target_y_pixels=[p[3] for p in panels],times_ns=[p[0] for p in panels],thresholds=[40,80,120],comparison=compare,limitations=['S14 is an author-created composite, not independent raw simultaneous camera data','Color segmentation and target-line endpoint uncertainty about2-3pixels','S4 and S14 physical scales do not agree exactly for corresponding published contours; retain S4 as primary, no scale substitution','210ns composite has no exact210ns S4 frame; no temporal interpolation used'])
(R/'results/S14_target_reference.json').write_text(json.dumps(report,indent=2));print(json.dumps(dict(rows=rows,comparison=compare),indent=2))
