"""Continue an unchanged fixed-plane candidate through the remaining S4 stages."""
from pathlib import Path
import os,sys,subprocess,time,json,datetime,re
R=Path(__file__).resolve().parent
previous,first,numa=sys.argv[1:4]
completed_end=float(sys.argv[4]) if len(sys.argv)>4 else 123.
chain_file=R/'round2_chains.json'
chains=json.loads(chain_file.read_text()) if chain_file.exists() else {}
stage_cases=chains.get(first,[f'F{int(first[1:])+k:02d}' for k in range(3)])
if len(stage_cases)!=3 or len(set(stage_cases))!=3:raise ValueError('Need three unique continuation cases')
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA=numa)
deadline=datetime.datetime.fromisoformat(json.loads((R/'round2_session.json').read_text())['deadline_utc'].replace('Z','+00:00'))
def logtext(c):
    p=R/'logs'/f'{c}_solver.log';return p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
while not any(x in logtext(previous) for x in ['总时间:','Total time:']):time.sleep(20)
stages=[stage for stage in [(203,1,.5),(403,2,1),(1000,2,2)] if stage[0]>completed_end+1e-6]
for k,(end,output,maxstep) in enumerate(stages):
    deadline=datetime.datetime.fromisoformat(json.loads((R/'round2_session.json').read_text())['deadline_utc'].replace('Z','+00:00'))
    if datetime.datetime.now(datetime.timezone.utc)>=deadline:break
    case=stage_cases[k];env['ROUTE1_LOG_TAG']=case
    if (R/'models'/f'{case}_ready.mph').exists():raise RuntimeError('Stage already exists: '+case)
    subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java','ContinueStage',str(R/'models'/f'{previous}.mph'),str(R/'models'/f'{case}_ready.mph'),str(end),str(output),str(maxstep)],env=env,check=True)
    with (R/'logs'/f'{case}_stall_watch.log').open('wb') as f:
        subprocess.Popen([sys.executable,'-X','utf8',str(R/'watch_stalled_case.py'),case],env=env,stdout=f,stderr=subprocess.STDOUT)
    code=subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'solve',case],env=env).returncode
    if (R/'models'/f'{case}.mph').exists():
        with (R/'logs'/f'{case}_post_round2.log').open('wb') as f:subprocess.Popen([sys.executable,'-X','utf8',str(R/'post_round2.py'),case],stdout=f,stderr=subprocess.STDOUT,env=env)
    ts=re.findall(r'^[ \t]*\d+\s+([\d.eE+-]+)\s+[\d.eE+-]+(?:\s+out)?\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+',logtext(case),re.M)
    if code or not ts or float(ts[-1])*1e9<end-1e-4:raise RuntimeError(f'{case} stopped before requested endpoint; inspect saved result')
    previous=case
print('ADVANCE_COMPLETED',previous,flush=True)
