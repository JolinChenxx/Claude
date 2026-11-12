"""Partitioned grid predictor from an actual copper history; no extrapolation."""
from pathlib import Path
import os,sys,subprocess,json
R=Path(__file__).resolve().parent
source,case=sys.argv[1:3]
meta=json.loads((R/'results'/f'{source}_transfer.json').read_text(encoding='utf-8'))
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case,ROUTE1_JAVA_HEAP='24g')
def run(*args):subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),*args],env=env,check=True)
run('compile','GridPrescribedCopper')
run('java','GridPrescribedCopper',str(R/'models/S202_ready.mph'),str(R/'models'/f'{case}_ready.mph'),meta['table'],f"{meta['end_ns']:.9g}[ns]")
(R/'results'/f'{case}_source.json').write_text(json.dumps(dict(source=source,end_ns=meta['end_ns'],fixed_plane=True,state_enthalpy_feedback_converged=False,reason='S202 direct heat-coupled assembly required tens of minutes per Jacobian; partition copper using actual saved history'),indent=2),encoding='utf-8')
subprocess.run([sys.executable,'-X','utf8',str(R/'run_existing_spatial.py'),case],env=env,check=True)
