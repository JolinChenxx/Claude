"""Postprocess the first saved gas checkpoint and prepare actual return fields."""
from pathlib import Path
import os,sys,time,json,subprocess
R=Path(__file__).resolve().parent;case=sys.argv[1]
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_LOG_TAG=case+'_post',ROUTE1_CAMERA_ZERO_NS='90',ROUTE1_RADIATION_EXPOSURES_ONLY='1',ROUTE1_RADIATION_MIN_NS='80',ROUTE1_POST_THREADS='16')
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
while not (R/'results'/f'{case}_stage.json').exists():
 log=R/'logs'/f'{case}_solver.log'
 s=log.read_text(encoding='utf-8',errors='replace') if log.exists() else ''
 if case=='P216' and ('总时间:' in s or 'Total time:' in s):
  py('run.py','java','MeshStateAudit',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_mesh_audit.csv'))
  py('run.py','java','ExportMovingOptics',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}mapped'),'80')
  d=json.loads((R/'results'/f'{case}mapped_fields.json').read_text())
  (R/'results'/f'{case}_stage.json').write_text(json.dumps({'case':case,'saved_end_ns':d['times_s'][-1]*1e9,'fresh_changed_radial_mesh':True,'overlap_validation_pending':True}))
  break
 time.sleep(30)
meta=json.loads((R/'results'/f'{case}mapped_fields.json').read_text())
py('plot_mapped_fields.py',case+'mapped')
complete=meta['times_s'][-1]*1e9>=95-1e-5
if complete:
 py('radiation_native.py',case+'mapped');py('preview_native.py',case+'mapped')
py('run.py','java','ExportDrivenFeedback',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_feedback.csv'))
py('prepare_gas_feedback.py',case)
(R/'results'/f'{case}_post_complete.json').write_text(json.dumps({'case':case,'saved_end_ns':meta['times_s'][-1]*1e9,'complete_first_gate':complete,'first_gate_model_ns':[85,95],'camera_zero_assumed_ns':90,'two_way_converged':False},indent=2))
print('DRIVEN POST AND FEEDBACK COMPLETE',case,flush=True)
