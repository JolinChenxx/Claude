"""Animate actual complete camera gates using their frozen response."""
from pathlib import Path
import sys
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from PIL import Image
R=Path(__file__).resolve().parent;case=sys.argv[1]
a=np.load(R/'results'/f'{case}_global_camera/images.npz');times=a['times_ns'];px=float(a['pixel_um'])
frames=[];h,w=a['prediction'].shape[1:];extent=[-w*px/2,w*px/2,0,h*px]
for i,t in enumerate(times):
    fig,ax=plt.subplots(1,2,figsize=(8,4.8),layout='constrained')
    for j,(label,key) in enumerate([('S4 experiment','experiment'),('Native Plasma reduced model','prediction')]):
        ax[j].imshow(a[key][i],vmin=0,vmax=255,cmap='turbo',extent=extent);ax[j].set_title(label)
        ax[j].set_xlabel('Radial image coordinate [um]');ax[j].set_ylabel('Height above model target [um]' if j else 'S4 crop coordinate [um]')
    ax[1].axhline(0,c='white',lw=1)
    fig.suptitle(f'{case} | experimental time {t:g} ns | 10 ns exposure\nFixed gain and length scale; model geometry assumed. S4 target plane unmarked.',fontsize=10)
    fig.canvas.draw();frames.append(Image.fromarray(np.asarray(fig.canvas.buffer_rgba())[:,:,:3]).convert('P',palette=Image.Palette.ADAPTIVE));plt.close(fig)
dest=R/'figures'/case/'S4_frozen_comparison.gif';frames[0].save(dest,save_all=True,append_images=frames[1:],duration=250,loop=0,optimize=False)
print(dest)
