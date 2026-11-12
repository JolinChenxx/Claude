"""Physical radiance with one early maximum scale; separate from frozen camera fit."""
from pathlib import Path
import sys,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
for case in sys.argv[1:]:
    a=np.load(R/'results'/f'{case}_global_images.npz');e=np.load(OLD/'experiment/S4_reconstructed.npz');t=a['experiment_times_ns'];rad=a['radiance'];ph=a['photons_upper_525nm']
    select=np.unique([np.argmin(abs(t-v)) for v in [0,20,30,60,150,260,400,740]])
    vmax=float(rad[t<=80].max());px=float(e['pixel_um']);h,w=rad.shape[1:];extent=[-w*px/2,w*px/2,0,h*px]
    fig,ax=plt.subplots(2,len(select),figsize=(2.1*len(select),5.4),layout='constrained',squeeze=False)
    for col,i in enumerate(select):
        obs=e['gray'][np.flatnonzero(e['times_ns']==t[i])[0]]
        ax[0,col].imshow(obs,cmap='turbo',vmin=0,vmax=255,extent=extent)
        artist=ax[1,col].imshow(rad[i],cmap='turbo',vmin=0,vmax=vmax,extent=extent)
        ax[0,col].set_title(f'{t[i]:g} ns');ax[1,col].axhline(0,c='white',lw=1)
        ax[1,col].text(.02,.98,f'{ph[i].sum():.2g} photons',color='white',fontsize=7,va='top',transform=ax[1,col].transAxes)
    for aa in ax.flat:aa.set_xticks([]);aa.set_yticks([])
    ax[0,0].set_ylabel('S4');ax[1,0].set_ylabel(case+' physical radiance')
    fig.colorbar(artist,ax=ax[1].tolist(),label='Radiance [W m^-2 sr^-1]; one fixed LINEAR scale',fraction=.016,pad=.01)
    kind=str(a['model_kind']) if 'model_kind' in a else 'reduced model'
    fig.suptitle(f'{case}: {kind}\nSame spatial scale,118ns shift,10ns gates. Physical early maximum sets color range; NOT the camera calibration.',fontsize=10)
    fig.savefig(R/'figures'/case/'S4_physical_radiance.png',dpi=160);plt.close(fig)
    (R/'results'/f'{case}_physical_display.json').write_text(json.dumps(dict(vmin=0,vmax_W_m2_sr=vmax,scale='linear, fixed over all frames, maximum from0-80ns',camera_gain_changed=False,photon_assumed_NA=.4,photon_throughput_QE=1,photon_wavelength_upper_nm=525),indent=2))
