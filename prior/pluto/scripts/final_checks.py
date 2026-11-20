"""Small, predeclared resolution and imaging sensitivity comparisons."""
import numpy as np
from common import ROOT,atomic_json
from postprocess import project,camera,measurements

def compare_grids(results):
    comparisons=[]
    exp=np.load(ROOT/'data/experimental/s4_frames.npz')
    for fine in [r for r in results if r['parameters']['level']=='fine']:
        same=[r for r in results if r['parameters']['level']!='fine' and
              r['parameters']['parameters']==fine['parameters']['parameters']]
        if not same:continue
        lower=max(same,key=lambda r:r['parameters']['level']=='medium')
        p=list(fine['camera'].values())
        a=camera(project(ROOT/'runs'/fine['run_id']),p,exp['times_ns'],*exp['gray'].shape[1:])
        b=camera(project(ROOT/'runs'/lower['run_id']),p,exp['times_ns'],*exp['gray'].shape[1:])
        pix=float(exp['pixel_um'])
        ra=np.array([measurements(im,pix)['radius_um'] for im in a])
        rb=np.array([measurements(im,pix)['radius_um'] for im in b])
        comparisons.append(dict(fine=fine['run_id'],lower=lower['run_id'],
            same_camera=True,pixel_rmse_255=float(np.sqrt(np.mean((a-b)**2))/255),
            median_radius_difference_um=float(np.median(np.abs(ra-rb))),
            fine_radius_um=ra.tolist(),lower_radius_um=rb.tolist(),
            notable_grid_dependence=bool(np.median(np.abs(ra-rb))>2*pix)))
    atomic_json(ROOT/'reports/refinement_comparison.json',comparisons)
    return comparisons

def imaging_sensitivity(folder):
    exp=np.load(ROOT/'data/experimental/s4_frames.npz')
    syn=np.load(folder/'synthetic_frames.npz')
    wide=project(folder,band_nm=(400.,700.),cache_name='projection_band400_700.npz')
    alternate=camera(wide,syn['camera_params'],exp['times_ns'],*exp['gray'].shape[1:])
    reference=syn['gray'];pix=float(exp['pixel_um'])
    a=np.array([measurements(im,pix)['radius_um'] for im in alternate])
    b=np.array([measurements(im,pix)['radius_um'] for im in reference])
    summary=dict(run_id=folder.name,baseline_band_nm=[480,520],alternate_band_nm=[400,700],
        fixed_camera=True,representative_line_unchanged=True,
        scope='Continuum-band sensitivity only; not a calibrated spectrum or detector response.',
        pixel_rmse_255=float(np.sqrt(np.mean((alternate-reference)**2))/255),
        median_radius_difference_um=float(np.median(np.abs(a-b))))
    np.savez_compressed(folder/'band_sensitivity.npz',times_ns=exp['times_ns'],gray=alternate)
    atomic_json(ROOT/'reports/imaging_sensitivity.json',summary)
    return summary
