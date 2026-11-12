"""Paper-style color map of already frozen grayscale data; no refitting."""
from pathlib import Path
import sys
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;case=sys.argv[1]
a=np.load(R/'results'/f'{case}_global_camera/images.npz');t=a['times_ns'];out=R/'figures'/case
camera_zero=float(a['camera_zero_model_ns']) if 'camera_zero_model_ns' in a else 118.
for full in [False,True]:
    selected=np.arange(len(t)) if full else np.unique([np.argmin(abs(t-v)) for v in [0,20,60,150,260,400,580,740]])
    columns=10 if full else len(selected);groups=int(np.ceil(len(selected)/columns))
    fig,ax=plt.subplots(2*groups,columns,figsize=(2*columns,3.8*groups),layout='constrained',squeeze=False)
    for axis in ax.flat:axis.axis('off')
    for n,i in enumerate(selected):
        block,col=divmod(n,columns)
        for row,key in enumerate(['experiment','prediction']):
            artist=ax[2*block+row,col].imshow(a[key][i],cmap='turbo',vmin=0,vmax=255)
            if row:ax[2*block+row,col].axhline(a[key].shape[1]-.5,c='white',lw=.8)
            if col==0:ax[2*block+row,col].text(-.07,.5,'S4' if row==0 else case,rotation=90,ha='right',va='center',transform=ax[2*block+row,col].transAxes)
        ax[2*block,col].set_title(f'{t[i]:g} ns')
    fig.colorbar(artist,ax=ax.ravel().tolist(),ticks=[0,27,100,220,255],label='Frozen grayscale response',fraction=.012,pad=.008)
    fig.suptitle(f'{case}: same spatial scale,{camera_zero:g}ns time shift,10ns exposure,one frozen early gain\nWhite line: model copper plane. S4 has no independent target marker; no vertical translation applied.',fontsize=11)
    fig.savefig(out/('S4_all_frames_frozen.png' if full else 'S4_paper_style_frozen.png'),dpi=160);plt.close(fig)
