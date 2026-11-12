"""Save complete early gates, then continue identical native axial equations."""
from pathlib import Path
import sys,subprocess,os,time
R=Path(__file__).resolve().parent
first,second=sys.argv[1:]
env=os.environ.copy();env.update(ROUTE1_THREADS='16',ROUTE1_NUMA='0',ROUTE1_LOG_TAG=second)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
py('checkpoint_run.py',first,'205')
while True:
    s=(R/'logs'/f'{first}_solver.log').read_text(encoding='utf8',errors='replace')
    if 'Total time:' in s or '\u603b\u65f6\u95f4:' in s:break
    time.sleep(20)
py('run.py','java','ContinueGlobalPlume',str(R/'models'/f'{first}.mph'),str(R/'models'/f'{second}_ready.mph'),'1000','2','1')
py('run.py','solve',second)
py('run.py','java','ExportAxialPlume',str(R/'models'/f'{second}.mph'),str(R/'results'/f'{second}_axial.csv'))
while not (R/'results'/f'{first}_axial.csv').exists():time.sleep(20)
py('post_axial.py',first,second)
