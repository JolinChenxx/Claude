"""Freeze one early-window gain for a global-model chain and evaluate S4.

The native populations are solved; the uniform half-ellipsoid spatial closure
remains an assumption. No translation, time shift, per-frame gain, or late fit.
"""
from pathlib import Path
import csv,json,sys
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

R=Path(__file__).resolve().parent
OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
sys.path.insert(0,str(OLD/'work'))
from camera_s4 import fit_global_gain,response
from extract_experiment import metrics,contour_mask

def main(case):
    a=np.load(R/'results'/f'{case}_global_images.npz')
    e=np.load(OLD/'experiment/S4_reconstructed.npz')
    times=a['experiment_times_ns'];alltimes=e['times_ns'];early=times<=80
    two_zone='model_kind' in a and 'two-zone' in str(a['model_kind'])
    axial='model_kind' in a and 'axial FV' in str(a['model_kind'])
    grid='model_kind' in a and 'axisymmetric FV' in str(a['model_kind'])
    geometry=str(a['spatial_geometry']) if 'spatial_geometry' in a else 'coarse axisymmetric finite volumes (4r x6z)' if grid else 'axial finite volumes with prescribed stream-tube area' if axial else 'two uniform hemispherical zones' if two_zone else 'uniform half-ellipsoid'
    camera_zero=float(a['camera_zero_model_ns']) if 'camera_zero_model_ns' in a else 118.
    if 'model_kind' in a and 'one-way' in str(a['model_kind']):geometry+=' with prescribed source'
    if not np.array_equal(times[early],alltimes[alltimes<=80]):
        print('CALIBRATION_PENDING: complete0-80ns gates required');return
    idx=np.array([np.flatnonzero(alltimes==t)[0] for t in times])
    obs=e['gray'][idx];rad=a['radiance'];px=float(e['pixel_um'])
    base=case.split('-')[0];frozen=R/'results'/f'{base}_global_camera'
    frozen.mkdir(exist_ok=True);calpath=frozen/'calibration.json'
    if calpath.exists():
        fit=json.loads(calpath.read_text())
        assert fit['time_shift_ns']==camera_zero and fit['calibration_times_ns']==times[early].tolist()
    else:
        bg=min(26.,float(np.median(np.r_[obs[:,:5,:5].ravel(),obs[:,:5,-5:].ravel()])))
        gain,mse=fit_global_gain(np.sort(rad[early].reshape(early.sum(),-1),axis=1),np.sort(obs[early].reshape(early.sum(),-1),axis=1),bg)
        fit=dict(gain=gain,background=bg,time_shift_ns=camera_zero,calibration_times_ns=times[early].tolist(),calibration_sorted_pixel_mse=mse,source_case=case,gate_ns=10,passband_nm=[475,525],PSF_FWHM_pixels=1,description='One frozen early-window gain; sorted pixels avoid fitting a target-plane translation; no late-frame fitting')
        calpath.write_text(json.dumps(fit,indent=2))
    predicted=response(rad,fit['gain'],fit['background'])
    rows=[]
    for i,t in enumerate(times):
        for threshold in (27,100,220):
            sim=metrics(predicted[i],pixel_um=px,threshold=threshold)
            exp=metrics(obs[i],pixel_um=px,threshold=threshold)
            mask=contour_mask(predicted[i],threshold)
            row=dict(time_ns=float(t),threshold=threshold,split='calibration' if t<=80 else 'prediction',**sim)
            row.update({'experiment_'+k:v for k,v in exp.items()})
            for name in ('radius_um','bright_height_um'):
                row[name+'_pass']=bool(abs(sim[name]-exp[name])<=max(.2*exp[name],2*px))
            row['area_relative_error']=sim['area_mm2']/exp['area_mm2']-1 if exp['area_mm2'] else None
            photons=a['photons_upper_525nm'][i]
            row['ideal_photons_total']=float(photons.sum())
            row['bright_pixels_below_one_ideal_photon_fraction']=float(np.mean(photons[mask]<1)) if mask.any() else None
            rows.append(row)
    out=R/'results'/f'{case}_global_camera';out.mkdir(exist_ok=True)
    with (out/'metrics.csv').open('w',newline='') as f:
        w=csv.DictWriter(f,fieldnames=rows[0]);w.writeheader();w.writerows(rows)
    np.savez_compressed(out/'images.npz',times_ns=times,experiment=obs,prediction=predicted,pixel_um=px,camera_zero_model_ns=camera_zero)
    summary={'case':case,'calibration_file':str(calpath),'native_global_populations':True,'geometry':geometry,'spatial_plasma_prediction':grid,'spatially_converged':False,'axial_transport_resolved':axial or grid,'transverse_dynamics_resolved':grid,'complete_frames':len(times),'target_position_note':'Simulation target is crop bottom; S4 target plane is unmarked, so absolute vertical registration is not validated','thresholds':{}}
    for threshold in (27,100,220):
        rr=[r for r in rows if r['threshold']==threshold]
        visible=[r for r in rr if r['experiment_above_threshold_pixels']>0]
        late=[r for r in visible if r['time_ns']>80]
        summary['thresholds'][str(threshold)]={
            'visible_frames':len(visible),'radius_pass_count':sum(r['radius_um_pass'] for r in visible),
            'height_pass_count':sum(r['bright_height_um_pass'] for r in visible),
            'area_within30percent_count':sum(abs(r['area_relative_error'])<=.3 for r in visible),
            'late_visible_frames':len(late),'late_radius_pass_count':sum(r['radius_um_pass'] for r in late),
            'last_simulated_visible_frame_ns':max((r['time_ns'] for r in rr if r['above_threshold_pixels']>0),default=None),
            'last_experimental_visible_frame_ns':max((r['time_ns'] for r in rr if r['experiment_above_threshold_pixels']>0),default=None)}
    (out/'summary.json').write_text(json.dumps(summary,indent=2))
    figdir=R/'figures'/case;figdir.mkdir(exist_ok=True)
    choose=np.arange(len(times)) if len(times)<=8 else np.unique([int(np.argmin(abs(times-x))) for x in (0,30,60,150,260,400,580,740)])
    fig,axes=plt.subplots(2,len(choose),figsize=(max(11,2*len(choose)),5.5),squeeze=False,layout='constrained')
    for col,i in enumerate(choose):
        axes[0,col].imshow(obs[i],cmap='inferno',vmin=0,vmax=255)
        axes[1,col].imshow(predicted[i],cmap='inferno',vmin=0,vmax=255)
        axes[0,col].set_title(f'{times[i]:g} ns')
        axes[1,col].text(.03,.97,f"{a['photons_upper_525nm'][i].sum():.2g} ideal photons",va='top',color='white',fontsize=7,transform=axes[1,col].transAxes)
        axes[1,col].axhline(predicted.shape[1]-.5,color='cyan',lw=1)
    for ax in axes.flat:ax.set_xticks([]);ax.set_yticks([])
    axes[0,0].set_ylabel('S4 experiment');axes[1,0].set_ylabel('Coarse axisymmetric FV' if grid else 'Axial FV transport' if axial else 'Two-zone closure' if two_zone else 'Uniform bulk closure')
    limitation='First-order spatial transport; no spatial convergence; fixed target' if grid else 'Axial advection solved; transverse area assumed; S4 target registration unvalidated' if axial else 'Internal core and target gap NOT predicted'
    fig.suptitle(f'{case}: one early-window gain, frozen for later frames; identical length scale\nNative Plasma, {geometry}; {limitation}',fontsize=10)
    fig.savefig(figdir/'S4_global_frozen_camera.png',dpi=170);plt.close(fig)
    fig,axes=plt.subplots(2,3,figsize=(13,7),layout='constrained')
    primary=[r for r in rows if r['threshold']==27]
    for ax,key,label in zip(axes.flat,('radius_um','area_mm2','mean_gray','bright_height_um','integrated_gray','ideal_photons_total'),('Radius at27 (um)','Area at27 (mm2)','Mean gray above27','Height span at27 (um)','Integrated gray','Ideal collected photons /10ns')):
        val=[r[key] or 0 for r in primary];ax.plot(times,val,'o-',ms=3,label='Model')
        if key!='ideal_photons_total':ax.plot(times,[r['experiment_'+key] or 0 for r in primary],'s-',ms=3,label='S4')
        else:ax.set_yscale('symlog',linthresh=1)
        ax.axvspan(0,80,color='gray',alpha=.12);ax.set_xlabel('Experimental time (ns)');ax.set_ylabel(label);ax.grid(alpha=.2);ax.legend(fontsize=8)
    fig.suptitle(f'{case}: frozen response; shaded0-80ns used for gain calibration only\nHeight span is translation independent; S4 has no independent target-plane marker',fontsize=11)
    fig.savefig(figdir/'global_camera_metrics.png',dpi=170);plt.close(fig)
    print(json.dumps(summary,indent=2))

if __name__=='__main__':main(sys.argv[1])
