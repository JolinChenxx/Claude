"""Fixed 90 ns mapping; one early-window gain, then unchanged later prediction.

Default computes a candidate gain only. --freeze creates the shared calibration;
an existing calibration is always reused and never overwritten by this script.
"""
from pathlib import Path
import sys,json,csv,numpy as np
import matplotlib;matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent
sys.path.insert(0,str(R))
OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
sys.path.insert(0,str(OLD/'work'))
from camera_s4 import gate,blur,response,fit_global_gain
from extract_experiment import metrics,contour_mask
from s4_temporal_geometry import peak_landmark,extinction_landmark,copper_geometry

def extra_metrics(image,px,z):
    signal=np.maximum(image-background,0);total=signal.sum()
    yy,xx=np.indices(image.shape)
    yc=int(np.clip(np.rint((signal*yy).sum()/total),0,len(z)-1)) if total else len(z)-1
    xc=int(np.clip(np.rint((signal*xx).sum()/total),0,image.shape[1]-1)) if total else image.shape[1]//2
    patch=image[max(yc-1,0):yc+2,max(xc-1,0):xc+2]
    return dict(peak_gray=float(image.max()),center_3x3_mean_gray=float(patch.mean()),light_centroid_z_um=float((signal*z[:,None]).sum()/total) if total else None)

case=sys.argv[1];out=R/'results'/f'{case}_camera90';out.mkdir(exist_ok=True)
e=np.load(OLD/'experiment/S4_reconstructed.npz');a=np.load(R/'results'/f'{case}_radiation/radiance.npz');t=a['times_s']*1e9;et=e['times_ns'];px=float(e['pixel_um']);early=et<=80
available=(et+85>=t.min()-1e-6)&(et+95<=t.max()+1e-6)
if not np.all(available[early]):
    (out/'status.json').write_text(json.dumps(dict(case=case,status='early_calibration_incomplete',complete_experiment_times_ns=et[available].tolist(),required_early_times_ns=et[early].tolist(),gain_fitted=False),indent=2))
    print('S4_GAIN_NOT_FITTED_EARLY_GATES_INCOMPLETE',case);sys.exit(0)
corners=np.concatenate([e['gray'][:,:5,:5].ravel(),e['gray'][:,:5,-5:].ravel()]);background=min(float(np.median(corners)),26.)
master=R/'results/s4_camera_fixed_90ns.json';frozen=master.exists()
if frozen:
    fit=json.loads(master.read_text());gain=fit['gain'];background=fit['background']
    if fit['time_shift_ns']!=90:raise ValueError('Shared calibration uses a different time convention')
else:
    raw=blur(gate(t,a['radiance'],et[early]+90),1)
    gain,mse=fit_global_gain(np.sort(raw.reshape(early.sum(),-1),axis=1),np.sort(e['gray'][early].reshape(early.sum(),-1),axis=1),background)
    fit=dict(reference_case=case,time_shift_ns=90.,gain=gain,background=background,calibration_frames_ns=et[early].tolist(),gate_ns=10,passband_nm=[475,525],PSF_FWHM_pixels=1,early_sorted_pixel_MSE=mse,method='One nonnegative gain fitted to the7 early image intensity distributions; no spatial or temporal fit; common saturation at255')
    if '--freeze' in sys.argv:
        with master.open('x',encoding='utf8') as f:json.dump(fit,f,indent=2)
        frozen=True
(out/'calibration.json').write_text(json.dumps(dict(fit,frozen_shared=frozen),indent=2))
times=et[available];obs=e['gray'][available];raw=blur(gate(t,a['radiance'],times+90),1);images=response(raw,gain,background);z=a['z_um'];x=a['x_um']
if images.shape!=obs.shape:raise ValueError('Experimental and physical camera pixel grids must match')
factor=2*np.pi*(1-np.sqrt(1-.4**2))*(px*1e-6)**2*10e-9*525e-9/(6.62607015e-34*299792458)
rows=[]
surfaces=copper_geometry(case,times+90)
for k,ns in enumerate(times):
    for threshold in [27,100,220]:
        sm=metrics(images[k],px,threshold);em=metrics(obs[k],px,threshold);ph=raw[k]*factor;mask=contour_mask(images[k],threshold)
        row=dict(case=case,experiment_ns=int(ns),model_center_ns=float(ns+90),threshold=threshold,split='calibration' if ns<=80 else 'prediction',**sm)
        row.update({f'experiment_{key}':v for key,v in em.items()});row.update(extra_metrics(images[k],px,z));row.update({f'experiment_{key}':v for key,v in extra_metrics(obs[k],px,z).items()})
        surface=surfaces[k]
        bottom=float(z[np.where(mask)[0]].min()-px/2) if mask.any() else None
        top=surface['copper_connected_top_um'] if surface else None
        row.update(copper_axis_top_um=surface['copper_axis_top_um'] if surface else None,copper_connected_top_um=top,
                   light_bottom_physical_z_um=bottom,light_bottom_above_copper_top_um=bottom-top if bottom is not None and top is not None else None,
                   light_centroid_above_copper_top_um=row['light_centroid_z_um']-top if row['light_centroid_z_um'] is not None and top is not None else None)
        for key in ['radius_um','bright_height_um']:
            row[key+'_tolerance']=max(.2*em[key],2*px);row[key+'_within_target']=abs(sm[key]-em[key])<=row[key+'_tolerance']
        row['area_relative_error']=abs(sm['area_mm2']/em['area_mm2']-1) if em['area_mm2'] else None
        row.update(ideal_collected_photons=float(ph.sum()),peak_radiance_W_m2_sr=float(raw[k].max()),bright_mask_mean_ideal_photons=float(ph[mask].mean()) if mask.any() else None,bright_mask_fraction_below_one_photon=float((ph[mask]<1).mean()) if mask.any() else None)
        rows.append(row)
with (out/'metrics.csv').open('w',newline='',encoding='utf8') as f:
    writer=csv.DictWriter(f,fieldnames=rows[0].keys());writer.writeheader();writer.writerows(rows)
summary=dict(case=case,frozen_shared_gain=frozen,complete_frames=len(times),total_frames=29,complete_experiment_times_ns=times.tolist(),time_shift_ns=90,position_note='Experiment uses crop-bottom origin; simulated z is physical. Experimental absolute height relative to copper is not independently registered.',thresholds={})
for threshold in [27,100,220]:
    rr=[r for r in rows if r['threshold']==threshold];visible=[r for r in rr if r['experiment_above_threshold_pixels']>0]
    summary['thresholds'][str(threshold)]=dict(visible_frames=len(visible),radius_pass=sum(r['radius_um_within_target'] for r in visible),height_pass=sum(r['bright_height_um_within_target'] for r in visible),radius_and_height_pass=sum(r['radius_um_within_target'] and r['bright_height_um_within_target'] for r in visible),area_pass=sum(r['area_relative_error'] is not None and r['area_relative_error']<=.3 for r in visible),experiment_last_visible_ns=max((r['experiment_ns'] for r in visible),default=None),simulated_last_visible_ns=max((r['experiment_ns'] for r in rr if r['above_threshold_pixels']>0),default=None),extinction_scope='Covered observed frames only')
summary['whole_sequence_acceptance_evaluated']=len(times)==29
rr=[r for r in rows if r['threshold']==27]
summary['peak_landmarks']={key:peak_landmark(times,[r['experiment_'+key] for r in rr],[r[key] for r in rr],len(times)==29) for key in ['radius_um','area_mm2','center_3x3_mean_gray','integrated_gray']}
for threshold in [27,100,220]:
    rr=[r for r in rows if r['threshold']==threshold]
    summary['thresholds'][str(threshold)]['extinction']=extinction_landmark(times,[r['experiment_above_threshold_pixels']>0 for r in rr],[r['above_threshold_pixels']>0 for r in rr],len(times)==29)
summary['copper_position_method']='At exposure center, connected rho_cond>100 kg/m3 and alpha_g<0.5 target; connected to domain bottom. Cell-edge contour, linear interpolation between saved states. Simulated positions only; no experimental target registration.'
(out/'summary.json').write_text(json.dumps(summary,indent=2));np.savez_compressed(out/'images.npz',times_ns=times,experiment=obs,prediction=images,radiance=raw,pixel_um=px)
figdir=R/'figures'/f'{case}_camera90';figdir.mkdir(exist_ok=True)
fig,axes=plt.subplots(2,3,figsize=(12,7),layout='constrained')
keys=[('radius_um','Radius [um]'),('area_mm2','Area [mm2]'),('bright_height_um','Height span [um]'),('center_3x3_mean_gray','Core gray level'),('integrated_gray','Integrated gray level'),('light_centroid_z_um','Light centroid z [um]')]
rr=[r for r in rows if r['threshold']==27]
for ax,(key,label) in zip(axes.flat,keys):
    ax.plot(times,[r['experiment_'+key] for r in rr],'o-',label='S4');ax.plot(times,[r[key] for r in rr],'s-',label=case)
    ax.axvspan(0,80,color='gray',alpha=.1);ax.set(xlabel='Experiment time [ns]',ylabel=label);ax.grid(alpha=.2);ax.legend(fontsize=8)
fig.suptitle(('Frozen' if frozen else 'Candidate')+' global gain; fixed90ns mapping; threshold27. Absolute experiment/copper alignment unavailable.',fontsize=10)
fig.savefig(figdir/'metrics.png',dpi=170);plt.close(fig)
extent=[x[0]-px/2,x[-1]+px/2,z[-1]-px/2,z[0]+px/2]
for start in range(0,len(times),4):
    ids=list(range(start,min(start+4,len(times))));fig,axes=plt.subplots(2,len(ids),figsize=(4*len(ids)+1,8.5),squeeze=False,layout='constrained')
    for col,k in enumerate(ids):
        for row,vals,label in [(0,obs[k],'S4'),(1,images[k],case)]:
            im=axes[row,col].imshow(vals,origin='upper',extent=extent,vmin=0,vmax=255,cmap='inferno');axes[row,col].set(title=f'{label}: {times[k]} ns',xlabel='x [um]',ylabel='z [um]')
    fig.colorbar(im,ax=list(axes.ravel()),label='Gray level; one shared gain')
    fig.suptitle(f'{case}: complete10ns exposures, fixed90ns mapping; '+('frozen' if frozen else 'candidate')+' gain\nEqual length scales; no per-frame normalization or spatial shift.',fontsize=10)
    fig.savefig(figdir/f'S4_page{start//4+1}.png',dpi=170);plt.close(fig)
print(json.dumps(summary),flush=True)
