"""One targeted axial-mesh comparison at the observed spatial front."""
from pathlib import Path
import os,sys,subprocess,json
R=Path(__file__).resolve().parent;case='P224'
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*map(str,args)],env=env,check=True)
py('run.py','java','RefinePlumeFront',R/'models/P223_ready.mph',R/'models/P224_ready.mph')
(R/'results/P224_configuration.json').write_text(json.dumps(dict(source='P223_ready',change='Double axial cells in2-80um band; initial-state recomputation; unchanged source/reactions/sheath',observed_front_um=11.04),indent=2))
with (R/'logs/P224_stall_watch.log').open('w') as out,(R/'logs/P224_stall_watch.err').open('w') as err:
    subprocess.Popen([sys.executable,'-X','utf8',str(R/'checkpoint_if_stalled.py'),case],stdout=out,stderr=err)
py('run.py','solve',case)
py('run.py','java','ExportMovingOptics',R/'models'/f'{case}.mph',R/'results'/f'{case}mapped','80')
py('run.py','java','ExportDrivenFeedback',R/'models'/f'{case}.mph',R/'results'/f'{case}_feedback.csv')
d=json.loads((R/'results'/f'{case}mapped_fields.json').read_text());(R/'results'/f'{case}_stage.json').write_text(json.dumps(dict(case=case,saved_end_ns=d['times_s'][-1]*1e9,fixed_plane=True)))
env.update(ROUTE1_CAMERA_ZERO_NS='90',ROUTE1_RADIATION_EXPOSURES_ONLY='1',ROUTE1_RADIATION_MIN_NS='80',ROUTE1_POST_THREADS='16')
for script in ['plot_mapped_fields.py','radiation_native.py','preview_gate_sequence.py']:py(script,case+'mapped')
