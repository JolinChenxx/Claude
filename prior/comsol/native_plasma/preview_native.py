"""First-frame physical radiance preview. Never fits or freezes camera gain."""
from pathlib import Path
import sys,json,os
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
R=Path(__file__).resolve().parent
OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
sys.path.insert(0,str(OLD/'work'))
from camera_s4 import gate,blur
case=sys.argv[1]
instant='--instant' in sys.argv
requested_time=float(sys.argv[sys.argv.index('--time')+1]) if '--time' in sys.argv else None
camera_zero=float(os.environ.get('ROUTE1_CAMERA_ZERO_NS','118'))
exp=np.load(OLD/'experiment/S4_reconstructed.npz')
a=np.load(R/'results'/f'{case}_radiation/radiance.npz')
t=a['times_s']*1e9
if instant:
    k=len(t)-1 if requested_time is None else int(np.argmin(abs(t-requested_time)))
    if requested_time is not None and abs(t[k]-requested_time)>1e-5:raise ValueError('Requested instantaneous time is not saved')
    raw=a['radiance'][k];label=f'Instantaneous {t[k]:.1f} ns; NOT an exposure comparison'
else:
    if t[0]>camera_zero-5+1e-6 or t[-1]<camera_zero+5-1e-6:raise ValueError('Incomplete first exposure; no extrapolated preview')
    raw=gate(t,a['radiance'],[camera_zero])[0];label=f'{camera_zero-5:g}-{camera_zero+5:g} ns exposure; experimental 0 ns'
mesh_file=R/'results'/f'{case.removesuffix("mapped")}_mesh_audit.csv'
if mesh_file.exists():
    mesh=np.genfromtxt(mesh_file,delimiter=',',names=True)
    check=(abs(mesh['time_s']*1e9-t[k])<1e-5) if instant else ((mesh['time_s']*1e9>=camera_zero-5-1e-5)&(mesh['time_s']*1e9<=camera_zero+5+1e-5))
    if np.any(mesh['cminAllspatialrelVol'][check]<=0):raise ValueError('Mesh inversion in requested image interval; no accepted preview')
moving_audit=R/'results'/f'{case}_radiation/moving_optical_audit.json'
if moving_audit.exists():
    audit=json.loads(moving_audit.read_text())
    label+=f"; bulk only, distance >= {audit['excluded_distance_um']} um above surface"
rad=blur(raw[None],1)[0]
px=float(exp['pixel_um']);omega=2*np.pi*(1-np.sqrt(1-.4**2))
photons=rad*omega*(px*1e-6)**2*10e-9*525e-9/(6.62607015e-34*299792458.)
x=a['x_um'];z=a['z_um'];extent=[x[0]-px/2,x[-1]+px/2,0,z[0]+px/2]
fig,ax=plt.subplots(1,3,figsize=(13,5),layout='constrained')
im=ax[0].imshow(exp['gray'][0],origin='upper',extent=extent,vmin=0,vmax=255,cmap='inferno');fig.colorbar(im,ax=ax[0],label='Reconstructed gray level')
ax[0].set_title('S4 experimental 0 ns')
for p,values,title,units in [(ax[1],rad,'Native excited-state radiation','W / m2 / sr'),(ax[2],photons,'Ideal collected photons','photons / pixel / 10 ns')]:
    top=max(values.max(),1e-20);im=p.imshow(np.maximum(values,top*1e-8),extent=extent,origin='upper',norm=LogNorm(top*1e-6,top),cmap='inferno');fig.colorbar(im,ax=p,label=units);p.set_title(title)
    if p is ax[2] and values.min()<1<values.max():p.contour(x,z,values,levels=[1],colors=['cyan'],linewidths=.8)
for p in ax:p.set(xlabel='x [um]',ylabel='z [um]')
fig.suptitle(f'{case}: {label}\n475-525 nm; 1 pixel PSF; no fitted gain; NA=0.4, throughput=QE=1\nSame length scale; S4 target-plane position is not independently registered')
out=R/'figures'/case;out.mkdir(exist_ok=True)
fig.savefig(out/('radiance_instant_preview.png' if instant else 'S4_first_frame_physical.png'),dpi=170)
report={'case':case,'instantaneous_only':instant,'saved_radiation_times_ns':t.tolist(),'peak_radiance_W_m2_sr':float(rad.max()),'ideal_total_photons_per_10ns':float(photons.sum()),'ideal_peak_photons_per_pixel':float(photons.max()),'pixels_over_one_photon':int((photons>=1).sum()),'no_camera_calibration':True,'limitations':['Ideal collection assumptions; experimental throughput is unknown','Instantaneous photon estimates assume unchanged radiance for 10 ns' if instant else 'Complete 10 ns gate integrated from saved radiation samples','No independently measured electron density or absolute camera calibration','Published crop has no independent target-plane marker']}
if moving_audit.exists():report['moving_optics']=audit
report['instantaneous_time_ns']=float(t[k]) if instant else None
report_name=f'{case}_radiance_instant_preview.json' if instant else f'{case}_radiance_preview.json'
(R/'results'/report_name).write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))
