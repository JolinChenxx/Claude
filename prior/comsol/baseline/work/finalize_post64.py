"""Finalize completed numerical postprocessing after a separately repaired figure QA.

This never regenerates or substitutes radiance. Every required output must exist,
the full radiation summary and camera coverage must agree, and all figure QA must pass.
"""
from pathlib import Path
import json,time,sys
import numpy as np
R=Path(__file__).resolve().parents[1];RES=R/'results';case=sys.argv[1]
def load(p):return json.loads(p.read_text(encoding='utf-8-sig'))
a=load(RES/f'{case}_audit.json');rad=load(RES/f'{case}_radiation/summary.json');meta=load(RES/f'{case}_fields.json')
assert rad['processed_times']==len(meta['times_s']) and rad['absolute_emission_cutoff']==0
assert rad['radiance_storage_dtype']=='float64'
with np.load(RES/f'{case}_radiation/radiance.npz') as rr:
    assert np.isfinite(rr['radiance']).all() and rr['times_s'][-1]>=865e-9
with np.load(RES/f'{case}_camera/images.npz') as im:assert len(im['times_ns'])==29
load(RES/f'{case}_camera/summary.json');load(RES/f'{case}_camera/photon_scale.json');load(RES/f'{case}_settings.json')
qa=load(R/f'figures/qa_{case}.json')
assert len(qa)>=8 and all(v['returncode']==0 for pair in qa.values() for v in pair.values())
assert (R/f'work/{case}_view.mph').is_file() and (R/f'figures/{case}_S4.gif').is_file()
d={'case':case,'numerical_status':a['status'],'diagnostic':a['status']!='passed','reference_camera':None,'temporal_export_stride':1,'completed_epoch':time.time(),'completion_note':'Original numerical/camera pipeline completed; evolution-figure legend overlap repaired separately, all eight PDF audits rerun and passed. See original pipeline log and QA JSON.'}
(RES/f'{case}_postprocess.json').write_text(json.dumps(d,indent=2))
if case=='H15R64':
    req=R/'main_post_refresh_required.json'
    if req.exists():
        r=load(req);r.update(reference_case=case,completed_epoch=time.time(),output_namespace=case,flow_origin='6.2')
        (R/'main_post_refresh_completed.json').write_text(json.dumps(r,indent=2));req.unlink()
print('POSTPROCESS_COMPLETE_AFTER_QA_REPAIR',case)
