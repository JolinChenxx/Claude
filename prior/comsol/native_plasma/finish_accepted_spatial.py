"""Optics from a short restart using actual accepted steps, including its last step."""
from pathlib import Path
import sys,os,time,json,subprocess
R=Path(__file__).resolve().parent
case,previous,joined=sys.argv[1:4]
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case+'post')
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
while True:
    p=R/'logs'/f'{case}_solver.log'
    s=p.read_text(encoding='utf-8',errors='replace') if p.exists() else ''
    if '总时间:' in s or 'Total time:' in s:break
    time.sleep(30)
py('run.py','java','MeshStateAudit',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_mesh_audit.csv'))
py('run.py','java','ExportMovingOptics',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}mapped'),'113')
py('run.py','java','ExportDrivenFeedback',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_feedback.csv'))
d=json.loads((R/'results'/f'{case}mapped_fields.json').read_text())
(R/'results'/f'{case}_stage.json').write_text(json.dumps(dict(case=case,saved_end_ns=d['times_s'][-1]*1e9,two_way_converged=False,accepted_steps=True),indent=2))
py('join_mapped_chain.py',previous,case,joined)
print('ACCEPTED SPATIAL POST COMPLETE',case,d['times_s'][-1]*1e9,flush=True)
