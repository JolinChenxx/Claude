"""Resume M120 with unchanged equations and a solver block split."""
from pathlib import Path
import sys,os,time,subprocess
R=Path(__file__).resolve().parent
while True:
    s=(R/'logs/M120_solver.log').read_text(encoding='utf8',errors='replace')
    if '总时间:' in s or 'Total time:' in s:break
    time.sleep(20)
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG='M121')
def run(*args):subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),*args],env=env,check=True)
run('java','ContinueStage',str(R/'models/M120.mph'),str(R/'models/M121_coupled_ready.mph'),'123','1','.2')
run('java','SplitMappedMoving',str(R/'models/M121_coupled_ready.mph'),str(R/'models/M121_ready.mph'))
run('solve','M121')
subprocess.run([sys.executable,'-X','utf8',str(R/'post_moving_round2.py'),'M121'],env=env,check=True)
