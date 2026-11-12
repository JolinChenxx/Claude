from pathlib import Path
import os,sys,json,subprocess
R=Path(__file__).resolve().parent;case=sys.argv[1]
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='0',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
py('run.py','compile','PrepareFixedDriven')
py('run.py','java','PrepareFixedDriven',str(R/'models/P203_ready.mph'),str(R/'models'/f'{case}_ready.mph'))
py('run.py','solve',case)
py('run.py','java','MeshStateAudit',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_mesh_audit.csv'))
py('run.py','java','ExportMovingOptics',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}mapped'),'80')
py('plot_mapped_fields.py',case+'mapped')
(R/'results'/f'{case}_stage.json').write_text(json.dumps(dict(case=case,source_copper='C201',physics_change='Flat stationary interface, same thermal and evaporation history; no molten surface velocity',two_way_converged=False),indent=2),encoding='utf-8')
py('finish_driven_post.py',case)
py('preview_gate_sequence.py',case+'mapped')
