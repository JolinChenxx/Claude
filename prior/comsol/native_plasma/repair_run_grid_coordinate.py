from pathlib import Path
import os,sys,subprocess
R=Path(__file__).resolve().parent;env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_LOG_TAG='S201c')
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
py('run.py','compile','RepairGridCoordinate')
py('run.py','java','RepairGridCoordinate',str(R/'models/S201b_ready.mph'),str(R/'models/S201c_ready.mph'))
py('run_existing_spatial.py','S201c')
