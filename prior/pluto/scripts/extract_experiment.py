"""Digitize S4 without inventing resolution beyond the embedded JPEG."""
from pathlib import Path
import json, csv
import numpy as np
from PIL import Image
from scipy import ndimage
from scipy.spatial import cKDTree
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT/'data/experimental'
TIMES = [0,20,30,50,60,70,80,150,160,180,200,250,260,300,310,
         330,350,360,400,420,450,500,510,530,580,610,630,730,740]
CALIBRATION = [20,60,150,260,400,580]

def measurements(a, pixel_um, threshold=27):
    mask=a>threshold
    labels,n=ndimage.label(mask)
    if n:
        sizes=ndimage.sum(mask,labels,np.arange(1,n+1))
        mask=labels==(1+np.argmax(sizes))
    yy,xx=np.indices(a.shape)
    signal=np.maximum(a-5,0)
    weight=signal.sum()
    xc=float((signal*xx).sum()/max(weight,1e-12))
    yc=float((signal*yy).sum()/max(weight,1e-12))
    if mask.sum()<3:
        rad=width=height=area=volume=avg=0.
    else:
        rows=np.flatnonzero(mask.any(1)); cols=np.flatnonzero(mask.any(0))
        rowwidth=[]
        for row in mask:
            x=np.flatnonzero(row)
            rowwidth.append(x[-1]-x[0]+1 if len(x) else 0)
        width=max(rowwidth)*pixel_um
        height=(rows[-1]-rows[0]+1)*pixel_um
        rad=width/2
        area=mask.sum()*pixel_um**2/1e6
        volume=np.sum(np.pi*(np.asarray(rowwidth)*pixel_um/2)**2)*pixel_um/1e9
        avg=float(a[mask].mean())
    return dict(radius_um=rad,width_um=width,height_um=height,area_mm2=area,
        apparent_volume_mm3=volume,mean_above27=avg,peak=float(a.max()),
        integrated_gray=float(signal.sum()),centroid_x_px=xc,centroid_y_px=yc)

def main():
    a=np.array(Image.open(OUT/'s4_source.jpeg').convert('RGB'))
    colored=(a.min(2)<200)&(a.max(2)>45)&((a.max(2).astype(int)-a.min(2))>40)
    lab,n=ndimage.label(colored)
    boxes=[]
    for sl in ndimage.find_objects(lab):
        if sl is None:continue
        ys,xs=sl; w=xs.stop-xs.start;h=ys.stop-ys.start
        if 60<w<100 and 60<h<100:boxes.append([xs.start,ys.start,xs.stop,ys.stop])
    boxes=sorted(boxes,key=lambda q:(round(q[1]/120),q[0]))
    if len(boxes)!=29:raise RuntimeError(f'Expected 29 panels, detected {len(boxes)}')
    # Scale-bar interior excludes frame/text. The native black line occupies 36 pixels.
    roi=a[686:695,423:474]
    dark=(roi.max(2)<100)
    spans=[]
    for row in dark:
        runs=ndimage.find_objects(ndimage.label(row)[0])
        spans += [s[0].stop-s[0].start for s in runs if s and s[0].stop-s[0].start>10]
    scale_px=max(spans)
    pixel_um=400/scale_px
    lut=(plt.get_cmap('jet')(np.linspace(0,1,256))[:,:3]*255)
    tree=cKDTree(lut)
    gray=[]; native=[];metrics=[];errors=[]
    for t,(x0,y0,x1,y1) in zip(TIMES,boxes):
        im=a[y0+1:y1-1,x0+1:x1-1]
        dist,g=tree.query(im.reshape(-1,3))
        g=g.reshape(im.shape[:2]).astype(float)
        # Preserve JPEG native sampling; place variable-sized crops into common canvas.
        native.append(im);gray.append(g);errors.append(float(np.median(dist)))
        m=measurements(g,pixel_um);m.update(time_ns=t,calibration=t in CALIBRATION)
        metrics.append(m)
    h=min(g.shape[0] for g in gray);w=min(g.shape[1] for g in gray)
    gray=np.stack([g[:h,:w] for g in gray]);rgb=np.stack([g[:h,:w] for g in native])
    np.savez_compressed(OUT/'s4_frames.npz',gray=gray,rgb=rgb,times_ns=TIMES,pixel_um=pixel_um)
    with (OUT/'metrics.csv').open('w',newline='',encoding='utf-8') as f:
        wr=csv.DictWriter(f,fieldnames=list(metrics[0]));wr.writeheader();wr.writerows(metrics)
    (OUT/'manifest.json').write_text(json.dumps(dict(
        source='lpor202400388-sup-0001-suppmat.pdf',page=6,embedded_size=list(a.shape[:2]),
        panel_boxes=boxes,times_ns=TIMES,calibration_times_ns=CALIBRATION,
        scale_bar_um=400,scale_bar_native_pixels=scale_px,pixel_um=pixel_um,
        grayscale_recovery='Nearest RGB in matplotlib jet LUT; JPEG approximation, not raw CCD data',
        median_rgb_lut_residual=errors,nominal_threshold=27,
        notes=['0 ns is an observed frame, not assumed a dark exposure.',
               'S7 is from the same experiment, not independent validation.',
               'One native pixel is the minimum spatial quantization uncertainty.']
    ),indent=2),encoding='utf-8')
    fig,axes=plt.subplots(6,5,figsize=(10,12),facecolor='white')
    for i,ax in enumerate(axes.flat):
        ax.axis('off')
        if i<len(TIMES):ax.imshow(rgb[i]);ax.set_title(f'{TIMES[i]} ns',fontsize=10)
        else:ax.text(.1,.5,f'400 um = {scale_px} native px\n{pixel_um:.2f} um/pixel\nPDF digitization',fontsize=10)
    fig.suptitle('Experimental S4 - native JPEG panels',fontsize=15)
    fig.tight_layout();fig.savefig(ROOT/'figures/experimental_s4.png',dpi=160);plt.close(fig)
    fig,ax=plt.subplots(2,2,figsize=(10,7))
    for a1,key,label in zip(ax.flat,['radius_um','mean_above27','area_mm2','centroid_y_px'],
            ['Visible radius (um)','Mean grayscale > 27','Visible area (mm2)','Centroid y (native pixels)']):
        a1.plot(TIMES,[m[key] for m in metrics],'o-');a1.set(xlabel='Delay (ns)',ylabel=label);a1.grid(alpha=.25)
    fig.suptitle('Digitized trends - approximate grayscale, common threshold')
    fig.tight_layout();fig.savefig(ROOT/'figures/experimental_metrics.png',dpi=160);plt.close(fig)
    print(json.dumps({'panels':len(boxes),'scale_px':scale_px,'pixel_um':pixel_um,'frame_shape':[h,w]}))

if __name__=='__main__':main()
