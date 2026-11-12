from pathlib import Path
import os,sys,subprocess,json
R=Path(__file__).resolve().parent;case=sys.argv[1]
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
py('run.py','java','AddTailPhoto',str(R/'models/P201_ready.mph'),str(R/'models'/f'{case}_ready.mph'),'1')
py('run.py','solve',case)
py('run.py','java','MeshStateAudit',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_mesh_audit.csv'))
py('run.py','java','ExportMovingOptics',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}mapped'),'80')
py('plot_mapped_fields.py',case+'mapped')
py('run.py','java','ExportDrivenFeedback',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_feedback.csv'))
py('run.py','java','ExportPlasmaLedger',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_plasma_ledger.csv'))
(R/'results'/f'{case}_stage.json').write_text(json.dumps({'case':case,'source_copper':'C201','physics_change':'Cu5d QSS excitation, recombination and 1064nm photoionization','rate_fitting':False,'two_way_converged':False},indent=2))
