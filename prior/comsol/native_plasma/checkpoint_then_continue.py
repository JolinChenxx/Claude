"""Save an unchanged model, then remove an unnecessarily restrictive step cap.

The new segment retains adaptive error control and the exact saved initial state.
This utility changes solver settings only; it must not be used after physics edits.
"""
from pathlib import Path
import os,sys,subprocess,time
R=Path(__file__).resolve().parent
prev,case,numa,checkpoint,end,maxstep=sys.argv[1:7]
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA=numa,ROUTE1_LOG_TAG=case)
subprocess.run([sys.executable,'-X','utf8',str(R/'checkpoint_run.py'),prev,checkpoint],check=True)
while True:
    s=(R/'logs'/f'{prev}_solver.log').read_text(encoding='utf8',errors='replace')
    if '总时间:' in s or 'Total time:' in s:break
    time.sleep(10)
subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java','ContinueStage',str(R/'models'/f'{prev}.mph'),str(R/'models'/f'{case}_ready.mph'),end,'1',maxstep],env=env,check=True)
with (R/'logs'/f'{case}_stall_watch.log').open('wb') as log:
    subprocess.Popen([sys.executable,'-X','utf8',str(R/'watch_stalled_case.py'),case],stdout=log,stderr=subprocess.STDOUT)
subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'solve',case],env=env,check=True)
