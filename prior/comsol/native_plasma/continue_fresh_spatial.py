from pathlib import Path
import os,sys,subprocess,json
R=Path(__file__).resolve().parent;source,case,end=sys.argv[1:4]
chain=sys.argv[4] if len(sys.argv)>4 else source
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*map(str,args)],env=env,check=True)
strategy=[os.environ['ROUTE1_CONTINUATION_STRATEGY']] if 'ROUTE1_CONTINUATION_STRATEGY' in os.environ else []
py('run.py','java','ContinueSpatialWindow',R/'models'/f'{source}.mph',R/'models'/f'{case}_ready.mph',end,*strategy)
py('run.py','solve',case)
py('run.py','java','MeshStateAudit',R/'models'/f'{case}.mph',R/'results'/f'{case}_mesh_audit.csv')
py('run.py','java','ExportMovingOptics',R/'models'/f'{case}.mph',R/'results'/f'{case}mapped','80')
py('run.py','java','ExportDrivenFeedback',R/'models'/f'{case}.mph',R/'results'/f'{case}_feedback.csv')
d=json.loads((R/'results'/f'{case}mapped_fields.json').read_text())
(R/'results'/f'{case}_stage.json').write_text(json.dumps({'source':source,'case':case,'saved_end_ns':d['times_s'][-1]*1e9,'radial_resolution_validation_pending':True}))
py('join_mapped_chain.py',chain,case,case+'chain')
