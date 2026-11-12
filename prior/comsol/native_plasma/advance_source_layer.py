"""Continue the unchanged native source reservoir and audit combined history."""
from pathlib import Path
import os,sys,time,subprocess
R=Path(__file__).resolve().parent
previous,case,end,*history=sys.argv[1:]
while True:
    s=(R/'logs'/f'{previous}_solver.log').read_text(encoding='utf8',errors='replace')
    if 'Total time:' in s or '\u603b\u65f6\u95f4:' in s:break
    time.sleep(20)
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_NUMA='0',ROUTE1_LOG_TAG=case)
def run(*args):
    subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),*args],env=env,check=True)
run('java','ContinueGlobalPlume',str(R/'models'/f'{previous}.mph'),str(R/'models'/f'{case}_ready.mph'),end,'1','.5')
run('solve',case)
subprocess.run([sys.executable,'-X','utf8',str(R/'post_source_layer.py'),case,*history,previous],env=env,check=True)
