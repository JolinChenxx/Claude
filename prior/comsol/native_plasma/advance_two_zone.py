"""Save an early checkpoint, continue unchanged physics, and render both stages."""
from pathlib import Path
import sys,os,time,subprocess
R=Path(__file__).resolve().parent
first,second,numa,*history=sys.argv[1:]
env=os.environ.copy();env.update(ROUTE1_THREADS=os.environ.get('ROUTE1_THREADS','32'),ROUTE1_NUMA=numa,ROUTE1_LOG_TAG=second)
subprocess.run([sys.executable,'-X','utf8',str(R/'checkpoint_run.py'),first,'203'],check=True)
while True:
    s=(R/'logs'/f'{first}_solver.log').read_text(encoding='utf8',errors='replace')
    if 'Total time:' in s or '\u603b\u65f6\u95f4:' in s:break
    time.sleep(20)
def run(*args):subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),*args],env=env,check=True)
run('java','ContinueGlobalPlume',str(R/'models'/f'{first}.mph'),str(R/'models'/f'{second}_ready.mph'),'1000','2','2')
# Export and render the completed early stage while the continuation runs.
log=(R/'logs'/f'{first}_two_optics.log').open('wb')
post=subprocess.Popen([sys.executable,'-X','utf8',str(R/'post_two_zone_optics.py'),first,*history],env=env,stdout=log,stderr=subprocess.STDOUT)
run('solve',second)
post.wait();log.close()
subprocess.run([sys.executable,'-X','utf8',str(R/'post_two_zone_optics.py'),second,*history,first],env=env,check=True)
