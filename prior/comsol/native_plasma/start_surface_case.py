"""Use NUMA 1 after the first photoionization comparison saves normally."""
from pathlib import Path
import subprocess,sys,os,time,json
R=Path(__file__).resolve().parent
log=R/'logs/F12_solver.log'
while True:
    s=log.read_text(encoding='utf8',errors='replace') if log.exists() else ''
    if '总时间:' in s or 'Total time:' in s:break
    time.sleep(15)
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG='F21')
code=subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'solve','F21'],env=env).returncode
print('F21_SOLVE_EXIT',code,flush=True)
if (R/'models/F21.mph').exists():
    env.pop('ROUTE1_LOG_TAG',None)
    with (R/'logs/F21_chain.log').open('wb') as f:
        subprocess.Popen([sys.executable,'-X','utf8',str(R/'flat_chain.py'),'F21','1'],stdout=f,stderr=subprocess.STDOUT,env=env)
    print('F21_CHAIN_LAUNCHED',flush=True)
