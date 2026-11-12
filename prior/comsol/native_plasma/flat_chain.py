"""Continue the fixed-plane baseline across saved milestones, without accepting physics.

The initial case is already running. A separate checkpoint watcher requests its
normal stop after 123 ns. Subsequent stages end at explicit saved milestones.
"""
from pathlib import Path
import subprocess,sys,os,json,time,datetime
R=Path(__file__).resolve().parent
first=sys.argv[1] if len(sys.argv)>1 else 'F01'
numa=sys.argv[2] if len(sys.argv)>2 else '0'
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA=numa)
deadline=datetime.datetime.fromisoformat(json.loads((R/'round2_session.json').read_text())['deadline_utc'].replace('Z','+00:00'))

def finished(case):
    p=R/'logs'/f'{case}_solver.log'
    if not p.exists():return False
    s=p.read_text(encoding='utf8',errors='replace')
    return any(x in s for x in ['总时间:','Total time:','鎬绘椂闂?'])

def post(case):
    f=(R/'logs'/f'{case}_post_round2.log').open('wb')
    p=subprocess.Popen([sys.executable,'-X','utf8',str(R/'post_round2.py'),case],stdout=f,stderr=subprocess.STDOUT,env=env)
    f.close()
    print('POST_LAUNCHED',case,p.pid,flush=True)

previous=first
while not finished(previous):time.sleep(30)
if not (R/'models'/f'{previous}.mph').exists():raise RuntimeError(f'{previous} did not save a model')
post(previous)
stages=[(f'F{int(first[1:])+i:02d}',end,step) for i,end,step in [(1,203,1),(2,403,2),(3,1000,2)]]
for case,end,step in stages:
    if (R/'logs'/f'{first}_chain.stop').exists():break
    if datetime.datetime.now(datetime.timezone.utc)>=deadline:break
    if (R/'models'/f'{case}_ready.mph').exists():raise RuntimeError(f'{case} already exists; inspect before restarting the chain')
    cmd=[sys.executable,'-X','utf8',str(R/'run.py'),'java','ContinueStage',str(R/'models'/f'{previous}.mph'),str(R/'models'/f'{case}_ready.mph'),str(end),str(step)]
    subprocess.run(cmd,env=env,check=True)
    print('SOLVE_START',case,end,flush=True)
    code=subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'solve',case],env=env).returncode
    if (R/'models'/f'{case}.mph').exists():post(case)
    if (R/'logs'/f'{first}_chain.stop').exists():break
    if code:raise RuntimeError(f'{case} stopped with code {code}; inspect saved state')
    # COMSOL can report exit 0 after an orderly requested stop. Do not skip an
    # unfinished milestone just because a saved MPH exists.
    import re
    log=(R/'logs'/f'{case}_solver.log').read_text(encoding='utf8',errors='replace')
    ts=re.findall(r'^[ \t]*\d+\s+([\d.eE+-]+)\s+[\d.eE+-]+(?:\s+out)?\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+',log,re.M)
    if not ts or float(ts[-1])*1e9 < end-1e-4:
        raise RuntimeError(f'{case} stopped before {end} ns; no next stage launched')
    previous=case
print('FLAT_CHAIN_FINISHED',previous,flush=True)
