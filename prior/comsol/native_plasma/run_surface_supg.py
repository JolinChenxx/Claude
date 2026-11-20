from pathlib import Path
import os,sys,subprocess
R=Path(__file__).resolve().parent;case=sys.argv[1]
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
py('run.py','java','PrepareSurfaceSUPG',str(R/'models/C201_ready.mph'),str(R/'models'/f'{case}_ready.mph'))
py('run.py','solve',case)
py('run.py','java','ExportMelt',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_surface.csv'))
py('plot_partitioned_copper.py',case)
py('run.py','java','MeshStateAudit',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_mesh_audit.csv'))
py('plot_copper_ledger.py',case)
