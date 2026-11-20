"""Test the documented thermal outgoing-state closure with the same copper history."""
from pathlib import Path
import json, os, subprocess, sys
R=Path(__file__).resolve().parent;case=sys.argv[1];input_case=sys.argv[2] if len(sys.argv)>2 else 'P203'
env=os.environ.copy();env.update(ROUTE1_THREADS=os.environ.get('ROUTE1_THREADS','16'),ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
py('run.py','compile','SurfaceStateFeed')
py('run.py','java','SurfaceStateFeed',str(R/'models'/f'{input_case}_ready.mph'),str(R/'models'/f'{case}_ready.mph'),'133[ns]')
py('run.py','solve',case)
py('run.py','java','MeshStateAudit',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_mesh_audit.csv'))
py('run.py','java','ExportMovingOptics',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}mapped'),'80')
py('plot_mapped_fields.py',case+'mapped')
py('run.py','java','ExportPlasmaLedger',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_plasma_ledger.csv'))
(R/'results'/f'{case}_stage.json').write_text(json.dumps(dict(case=case,source_copper='C201',physics_change='Documented outgoing thermal Cu states on P203 photoionization model',two_way_converged=False,rate_fitting=False,limitation='Excitation enthalpy enters gas Qmass but source copper history is first iterate; require returning the extra source enthalpy before total energy validation'),indent=2),encoding='utf-8')
