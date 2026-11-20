"""Continue the same physics with an efficient, explicit time-step schedule."""
from pathlib import Path
import os,sys,subprocess,time,re,json,datetime
R=Path(__file__).resolve().parent
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG='F63')
subprocess.run([sys.executable,'-X','utf8',str(R/'checkpoint_run.py'),'F62','2'],check=True)
while True:
    s=(R/'logs/F62_solver.log').read_text(encoding='utf8',errors='replace')
    if '总时间:' in s or '鎬绘椂闂?' in s or 'Total time:' in s:break
    time.sleep(20)
subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java','ContinueStage',str(R/'models/F62.mph'),str(R/'models/F63_ready.mph'),'123','1','0.1','if(t<80[ns],1[ns],0.05[ns])'],env=env,check=True)
code=subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'solve','F63'],env=env).returncode
for case in ['F62','F63']:
    with (R/'logs'/f'{case}_post_round2.log').open('wb') as f:
        subprocess.Popen([sys.executable,'-X','utf8',str(R/'post_round2.py'),case],env=env,stdout=f,stderr=subprocess.STDOUT)
s=(R/'logs/F63_solver.log').read_text(encoding='utf8',errors='replace')
ts=re.findall(r'^[ \t]*\d+\s+([\d.eE+-]+)\s+[\d.eE+-]+(?:\s+out)?\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+',s,re.M)
if code or not ts or float(ts[-1])*1e9<123-1e-4:raise RuntimeError('F63 incomplete; saved diagnostics only')
subprocess.run([sys.executable,'-X','utf8',str(R/'advance_round2.py'),'F63','F64','1'],env=env,check=True)
