"""Continue the unchanged moving model, export each actual saved interval."""
from pathlib import Path
import os,sys,subprocess,time,json,datetime,re
R=Path(__file__).resolve().parent
previous,first,numa=sys.argv[1:4]
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA=numa)
deadline=datetime.datetime.fromisoformat(json.loads((R/'round2_session.json').read_text())['deadline_utc'].replace('Z','+00:00'))
def read(c):
    p=R/'logs'/f'{c}_solver.log'
    return p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
while not any(x in read(previous) for x in ['Total time:','总时间：','总时间:']):
    if datetime.datetime.now(datetime.timezone.utc)>=deadline:sys.exit('Deadline before first saved interval')
    time.sleep(30)
def accepted(c):
    rows=re.findall(r'^[ \t]*\d+\s+([\d.eE+-]+)\s+[\d.eE+-]+(?:\s+out)?\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+',read(c),re.M)
    return float(rows[-1])*1e9 if rows else -1
if accepted(previous)<123-1e-4:sys.exit('Incomplete first exposure; no automatic continuation')
for k,(end,outstep,maxstep) in enumerate([(203,1,.5),(403,2,1),(1000,2,2)]):
    deadline=datetime.datetime.fromisoformat(json.loads((R/'round2_session.json').read_text())['deadline_utc'].replace('Z','+00:00'))
    if datetime.datetime.now(datetime.timezone.utc)>=deadline:break
    case=f'M{int(first[1:])+k}';env['ROUTE1_LOG_TAG']=case
    if (R/'models'/f'{case}_ready.mph').exists():raise RuntimeError('Case already exists: '+case)
    subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java','ContinueStage',str(R/'models'/f'{previous}.mph'),str(R/'models'/f'{case}_ready.mph'),str(end),str(outstep),str(maxstep)],env=env,check=True)
    with (R/'logs'/f'{case}_stall_watch.log').open('wb') as f:
        subprocess.Popen([sys.executable,'-X','utf8',str(R/'watch_stalled_case.py'),case],stdout=f,stderr=subprocess.STDOUT,env=env)
    code=subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'solve',case],env=env).returncode
    if (R/'models'/f'{case}.mph').exists():
        with (R/'logs'/f'{case}_post_moving.log').open('wb') as f:
            subprocess.Popen([sys.executable,'-X','utf8',str(R/'post_moving_round2.py'),case],stdout=f,stderr=subprocess.STDOUT,env=env)
    if code or accepted(case)<end-1e-4:raise RuntimeError('Stopped early: '+case)
    previous=case
print('MOVING_ADVANCE_SAVED',previous,flush=True)
