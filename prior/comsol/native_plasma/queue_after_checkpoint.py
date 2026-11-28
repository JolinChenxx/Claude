"""Queue a fresh case on the same NUMA node after a complete saved checkpoint."""
from pathlib import Path
import re,sys,time,subprocess,datetime,json
R=Path(__file__).resolve().parent
previous,case,numa=sys.argv[1:4]
minimum_ns=float(sys.argv[4]) if len(sys.argv)>4 else 123.
deadline=datetime.datetime.fromisoformat(json.loads((R/'round2_session.json').read_text())['deadline_utc'].replace('Z','+00:00'))
while True:
    p=R/'logs'/f'{previous}_solver.log';s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    if '总时间:' in s or 'Total time:' in s:break
    if datetime.datetime.now(datetime.timezone.utc)>=deadline:raise RuntimeError('Session deadline reached')
    time.sleep(30)
ts=re.findall(r'^[ \t]*\d+\s+([\d.eE+-]+)\s+[\d.eE+-]+(?:\s+out)?\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+',s,re.M)
if not ts or float(ts[-1])<minimum_ns*1e-9-1e-15:raise RuntimeError('Previous case ended before requested checkpoint')
print('PREVIOUS_CHECKPOINT_COMPLETE',previous,'STARTING',case,flush=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'run_checkpoint_chain.py'),case,numa],check=True)
