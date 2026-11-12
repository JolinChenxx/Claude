from pathlib import Path
import os,sys,subprocess,json
R=Path(__file__).resolve().parent;source,case=sys.argv[1:3]
start=float(sys.argv[3]);end=float(sys.argv[4])
env=os.environ.copy();env.update(ROUTE1_THREADS=os.environ.get('ROUTE1_THREADS','32'),ROUTE1_NUMA=os.environ.get('ROUTE1_NUMA','0'),ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
py('run.py','java','ContinueSaved',str(R/'models'/f'{source}.mph'),str(R/'models'/f'{case}_ready.mph'),f'{start}[ns]',f'range({start}[ns],1[ns],{end}[ns])')
if os.environ.get('ROUTE1_GAS_JACOBIAN')=='every':
 py('run.py','compile','RefreshGasJacobian')
 py('run.py','java','RefreshGasJacobian',str(R/'models'/f'{case}_ready.mph'),str(R/'models'/f'{case}_ready.mph'))
py('run.py','solve',case)
py('run.py','java','MeshStateAudit',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_mesh_audit.csv'))
py('run.py','java','ExportMovingOptics',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}mapped'),str(start))
py('plot_mapped_fields.py',case+'mapped')
py('run.py','java','ExportDrivenFeedback',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_feedback.csv'))
(R/'results'/f'{case}_stage.json').write_text(json.dumps({'source':source,'case':case,'requested_start_ns':start,'requested_end_ns':end,'two_way_converged':False},indent=2))
