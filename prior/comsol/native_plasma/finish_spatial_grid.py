"""Finish the in-progress 80-cell build, repair old nozzle terms, solve and export."""
from pathlib import Path
import os,sys,time,subprocess
R=Path(__file__).resolve().parent;case=sys.argv[1]
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
log=R/'logs'/f'BuildSpatialGridThermal_{case}_java.log'
while True:
    text=log.read_text(encoding='utf-8',errors='replace') if log.exists() else ''
    if 'SPATIAL THERMAL GRID READY' in text:break
    if 'Exception' in text or 'OutOfMemoryError' in text:raise RuntimeError(text[-4000:])
    time.sleep(30)
ready=R/'models'/f'{case}_ready.mph';original=R/'models'/f'{case}_unpatched_ready.mph'
ready.replace(original)
py('run.py','java','RepairSpatialGridCoupling',str(original),str(ready))
py('run.py','solve',case)
py('run.py','java','ExportSpatialGrid',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_axial.csv'))
py('post_spatial_grid.py',case)
print('SPATIAL GRID EXPORTED',case,flush=True)
