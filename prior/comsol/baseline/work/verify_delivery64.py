"""Check the final saved artifacts and their provenance before delivery."""
from pathlib import Path
import datetime,json
import numpy as np
R=Path(__file__).resolve().parents[1]
def read(path):return json.loads(path.read_text(encoding='utf8'))

def main():
    state=read(R/'delivery_state.json')
    if state.get('phase')!='ready_for_final_review':raise RuntimeError('Calculation/postprocessing still active')
    if any((R/p).exists() for p in ['worker.lock','coordinator64.lock']):raise RuntimeError('Coordinator or worker still active')
    cases=[state[k] for k in ['main_case','grid_case','time_case','extra_grid_case']]
    base=read(R/f'results/{cases[0]}_camera/calibration.json');out={}
    for c in cases:
        a=read(R/f'results/{c}_audit.json');m=read(R/f'results/{c}_fields.json')
        p=read(R/f'results/{c}_postprocess.json');camera=read(R/f'results/{c}_camera/calibration.json')
        qa=read(R/f'figures/qa_{c}.json');vis=read(R/f'results/{c}_visible_summary.json')
        t=np.asarray(m['times_s']);size=(R/f'results/{c}_fields.bin').stat().st_size
        checks={'full_audit':a['status']=='passed' and all(a['checks'].values()),
            'time_unique_increasing':bool(np.all(np.diff(t)>0)),
            'full_time_range':bool(abs(t[0])<1e-16 and abs(t[-1]-2.12e-6)<1e-16),
            'saved_times_match':len(t)==a['metadata']['saved_times'],
            'field_binary_size':size==int(np.prod(m['shape']))*np.dtype(m['dtype']).itemsize,
            'post_complete':p['case']==c and p['numerical_status']=='passed' and not p['diagnostic'] and p['temporal_export_stride']==1,
            'frozen_camera':all(camera[k]==base[k] for k in ['gain','time_shift_ns','background','gate_ns','PSF_FWHM_pixels','passband_nm']),
            'eight_pdf_audits':len(qa)==8 and all(x['returncode']==0 for q in qa.values() for x in q.values()),
            'full_visible_budget':abs(vis['time_range_s'][-1]-2.12e-6)<1e-16 and vis['full_FE_domain_with_ALE_J'],
            'model_and_view':all((R/'work'/n).stat().st_size>1_000_000 for n in [c+'.mph',c+'_view.mph']),
            'physical_animation':(R/f'figures/{c}_physical_evolution.gif').is_file()}
        out[c]={'checks':checks,'saved_times':len(t),'fields_bytes':size}
        if not all(checks.values()):raise RuntimeError((c,checks))
    fine=cases[-1];source='G264R1C1'
    sm=read(R/f'results/{source}_fields.json');fm=read(R/f'results/{fine}_fields.json');n=len(sm['times_s'])
    same_times=np.array_equal(sm['times_s'],fm['times_s'][:n])
    sd=np.memmap(R/f'results/{source}_fields.bin',dtype=sm['dtype'],mode='r',shape=tuple(sm['shape']))
    fd=np.memmap(R/f'results/{fine}_fields.bin',dtype=fm['dtype'],mode='r',shape=tuple(fm['shape']))
    identical=same_times and all(np.array_equal(sd[i],fd[i]) for i in range(n))
    identity={'source':source,'complete_case':fine,'common_saved_states':n,'time_arrays_equal':bool(same_times),
              'all_exported_fields_bitwise_equal':bool(identical),'method':'Native float32 export, every field and grid point at all common saved states.'}
    if not identical:raise RuntimeError('Final fine export differs from S4 diagnostic; inspect before acceptance')
    (R/'results/extra_grid_history_export_identity64.json').write_text(json.dumps(identity,indent=2),encoding='utf8')
    result={'checked_beijing':datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat(),
            'cases':out,'fine_S4_history_identity':identity,'artifact_checks_passed':True,
            'scope':'Artifact integrity and provenance only; numerical convergence and experimental agreement are separate conclusions.'}
    (R/'results/delivery_verification64.json').write_text(json.dumps(result,indent=2),encoding='utf8')
    print(json.dumps(result,indent=2))

if __name__=='__main__':main()
