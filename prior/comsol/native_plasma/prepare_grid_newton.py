from pathlib import Path
import os,sys,subprocess
R=Path(__file__).resolve().parent;env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_LOG_TAG='S202')
def py(*args):subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),*args],env=env,check=True)
py('compile','GridNewtonReuse')
py('java','GridNewtonReuse',str(R/'models/S201c_ready.mph'),str(R/'models/S202_ready.mph'))
