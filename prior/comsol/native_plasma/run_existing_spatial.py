"""Run the already built native grid with sufficient Java heap, then export."""
from pathlib import Path
import os,sys,subprocess
R=Path(__file__).resolve().parent;case=sys.argv[1]
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case,ROUTE1_JAVA_HEAP='24g')
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
py('run.py','solve',case)
log=(R/'logs'/f'{case}_solver.log').read_text(encoding='utf-8',errors='replace')
if any(s in log for s in ['/*****错误','/*****Error','Exception:']):
    raise RuntimeError('COMSOL reported a solver error despite exit code zero; retain MPH for repair, do not export as a physical result')
py('run.py','java','ExportSpatialGrid',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_axial.csv'))
py('post_spatial_grid.py',case)
print('EXISTING SPATIAL GRID EXPORTED',case,flush=True)
