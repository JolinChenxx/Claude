"""Launch the native spatial gas solve once actual copper checkpoint is exported."""
from pathlib import Path
import sys,os,json,time,subprocess
R=Path(__file__).resolve().parent;source=sys.argv[1];case=sys.argv[2]
env=os.environ.copy();env.update(ROUTE1_THREADS=os.environ.get('ROUTE1_THREADS','32'),ROUTE1_NUMA=os.environ.get('ROUTE1_NUMA','0'),ROUTE1_LOG_TAG=case,ROUTE1_CAMERA_ZERO_NS='90',ROUTE1_RADIATION_EXPOSURES_ONLY='1',ROUTE1_RADIATION_MIN_NS='80',ROUTE1_POST_THREADS='16')
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
while not (R/'results'/f'{source}_stage.json').exists():time.sleep(30)
py('plot_partitioned_copper.py',source);py('prepare_copper_history.py',source)
d=json.loads((R/'results'/f'{source}_transfer.json').read_text());end=d['end_ns']
py('run.py','java','PrepareDrivenPlasma',str(R/'models/M120_ready.mph'),str(R/'models'/f'{case}_ready.mph'),str(R/'data'/f'{source}_copper_history.txt'),f'{end:.10g}[ns]')
py('run.py','solve',case)
py('run.py','java','MeshStateAudit',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_mesh_audit.csv'))
py('run.py','java','ExportMovingOptics',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}mapped'),'80')
py('radiation_native.py',case+'mapped')
py('preview_native.py',case+'mapped')
(R/'results'/f'{case}_stage.json').write_text(json.dumps(dict(case=case,copper_source=source,camera_zero_model_ns=90,source_end_ns=end,two_way_converged=False,stage='spatial native Plasma driven by computed moving copper'),indent=2))
print('DRIVEN PLASMA EXPORTED',case,flush=True)
