from pathlib import Path
import sys,os,subprocess
R=Path(__file__).resolve().parent
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_NUMA='1',ROUTE1_LOG_TAG='G51')
def run(*args):subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),*args],env=env,check=True)
run('java','PrepareScalarSurfaceCoupling',str(R/'models/G40_ready.mph'),str(R/'models/G51_ready.mph'))
run('solve','G51')
subprocess.run([sys.executable,'-X','utf8',str(R/'post_two_zone_optics.py'),'G51'],env=env,check=True)
