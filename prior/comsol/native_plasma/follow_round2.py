"""Export a saved first stage and continue only after its requested endpoint."""
from pathlib import Path
import os,sys,subprocess,time,re,datetime,json
R=Path(__file__).resolve().parent
case,next_case,numa=sys.argv[1:4]
required_end=float(sys.argv[4]) if len(sys.argv)>4 else 123.
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA=numa)
deadline=datetime.datetime.fromisoformat(json.loads((R/'round2_session.json').read_text())['deadline_utc'].replace('Z','+00:00'))
while True:
    p=R/'logs'/f'{case}_solver.log';s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    if any(x in s for x in ['总时间:','鎬绘椂闂?','Total time:']):break
    if datetime.datetime.now(datetime.timezone.utc)>=deadline:raise RuntimeError('Session deadline reached')
    time.sleep(30)
if not (R/'models'/f'{case}.mph').exists():raise RuntimeError('No saved MPH')
with (R/'logs'/f'{case}_post_round2.log').open('wb') as f:
    subprocess.Popen([sys.executable,'-X','utf8',str(R/'post_round2.py'),case],env=env,stdout=f,stderr=subprocess.STDOUT)
ts=re.findall(r'^[ \t]*\d+\s+([\d.eE+-]+)\s+[\d.eE+-]+(?:\s+out)?\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+',s,re.M)
if not ts or float(ts[-1])*1e9<required_end-1e-4:raise RuntimeError(f'{case} incomplete; diagnostic exports only')
if (R/'logs'/f'{case}_chain.stop').exists():raise RuntimeError('Continuation disabled by case stop marker')
subprocess.run([sys.executable,'-X','utf8',str(R/'advance_round2.py'),case,next_case,numa,str(required_end)],env=env,check=True)
