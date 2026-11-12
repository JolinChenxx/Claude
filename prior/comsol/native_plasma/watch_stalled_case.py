"""Request a saved diagnostic checkpoint only after sustained time-step collapse."""
from pathlib import Path
import sys,time,re,subprocess,json,datetime
R=Path(__file__).resolve().parent;case=sys.argv[1]
deadline=datetime.datetime.fromisoformat(json.loads((R/'round2_session.json').read_text())['deadline_utc'].replace('Z','+00:00'))
pattern=re.compile(r'^[ \t]*\d+\s+([\d.eE+-]+)\s+([\d.eE+-]+)(?:\s+out)?\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+',re.M)
last_count=0;last_change=time.monotonic()
while datetime.datetime.now(datetime.timezone.utc)<deadline:
 p=R/'logs'/f'{case}_solver.log';s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
 if any(x in s for x in ['总时间:','鎬绘椂闂?','Total time:']):break
 steps=pattern.findall(s)
 if len(steps)!=last_count:last_count=len(steps);last_change=time.monotonic()
 collapse=len(steps)>12 and all(float(dt)<1e-14 for _,dt in steps[-4:])
 no_progress=bool(steps) and time.monotonic()-last_change>600
 if collapse or no_progress:
  record={'case':case,'reason':'four consecutive steps below 1e-14 s' if collapse else 'no accepted timestep for ten minutes','last_accepted_ns':float(steps[-1][0])*1e9,'utc':datetime.datetime.now(datetime.timezone.utc).isoformat()}
  (R/'results'/f'{case}_watch_stop.json').write_text(json.dumps(record,indent=2));print(record,flush=True)
  subprocess.run([sys.executable,'-X','utf8',str(R/'checkpoint_run.py'),case,'0'],check=True);break
 time.sleep(30)
