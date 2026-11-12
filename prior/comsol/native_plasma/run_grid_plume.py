from pathlib import Path
import sys,os,subprocess
R=Path(__file__).resolve().parent;case=sys.argv[1] if len(sys.argv)>1 else 'D01'
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='0',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
py('run.py','java','PrepareGridPlume',str(R/'models/A01_ready.mph'),str(R/'models'/f'{case}_ready.mph'))
py('run.py','solve',case)
py('run.py','java','ExportAxialPlume',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_axial.csv'))
py('post_grid_plume.py',case)
