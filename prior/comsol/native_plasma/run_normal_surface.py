from pathlib import Path
import os,sys,subprocess
R=Path(__file__).resolve().parent;case='C214';env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*map(str,args)],env=env,check=True)
py('run.py','compile','NormalFluxSurface')
py('run.py','java','NormalFluxSurface',R/'models/C201_ready.mph',R/'models/C214_ready.mph')
py('run.py','solve',case)
py('run.py','java','ExportMelt',R/'models'/f'{case}.mph',R/'results'/f'{case}_surface.csv')
py('plot_partitioned_copper.py',case)
py('run.py','java','MeshStateAudit',R/'models'/f'{case}.mph',R/'results'/f'{case}_mesh_audit.csv')
