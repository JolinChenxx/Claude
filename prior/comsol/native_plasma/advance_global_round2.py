"""Checkpoint complete early gates, then advance unchanged global physics to 1 us."""
from pathlib import Path
import json,os,re,subprocess,sys,time
R=Path(__file__).resolve().parent
first,second=sys.argv[1:3]
checkpoint=sys.argv[3] if len(sys.argv)>3 else '203'
history=sys.argv[4:]
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_NUMA='0',ROUTE1_LOG_TAG=second)
subprocess.run([sys.executable,'-X','utf8',str(R/'checkpoint_run.py'),first,checkpoint],check=True)
while True:
    s=(R/'logs'/f'{first}_solver.log').read_text(encoding='utf8',errors='replace')
    if 'Total time:' in s or '总时间:' in s:break
    time.sleep(20)
rows=re.findall(r'^[ \t]*\d+\s+([\d.eE+-]+)\s+[\d.eE+-]+(?:\s+out)?\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+',s,re.M)
if not rows or float(rows[-1])*1e9<float(checkpoint)-1e-4:sys.exit('Global early segment incomplete')
subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java','ContinueGlobalPlume',str(R/'models'/f'{first}.mph'),str(R/'models'/f'{second}_ready.mph'),'1000','2','2'],env=env,check=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'solve',second],env=env,check=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'post_global_round2.py'),second,*history,first],env=env,check=True)
