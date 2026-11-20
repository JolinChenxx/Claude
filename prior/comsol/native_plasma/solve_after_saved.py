"""Start a prepared fresh model once a specified predecessor has saved."""
from pathlib import Path
import os,sys,time,subprocess
R=Path(__file__).resolve().parent
prev,case,numa=sys.argv[1:4]
while True:
    p=R/'logs'/f'{prev}_solver.log'
    s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    if '总时间:' in s or 'Total time:' in s:break
    time.sleep(15)
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA=numa,ROUTE1_LOG_TAG=case)
with (R/'logs'/f'{case}_stall_watch.log').open('wb') as log:
    subprocess.Popen([sys.executable,'-X','utf8',str(R/'watch_stalled_case.py'),case],stdout=log,stderr=subprocess.STDOUT)
subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'solve',case],env=env,check=True)
