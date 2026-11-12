"""Compact actual solver progress; accepted steps are not saved-solution claims."""
from pathlib import Path
import re,json,sys,datetime
R=Path(__file__).resolve().parent
for case in sys.argv[1:]:
 p=R/'logs'/f'{case}_solver.log'
 if not p.exists():print(json.dumps({'case':case,'state':'no solver log'}));continue
 s=p.read_text(encoding='utf8',errors='replace')
 rows=re.findall(r'^[ \t]*(\d+)\s+([\d.eE+-]+)\s+([\d.eE+-]+)(?:\s+out)?\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+',s,re.M)
 out={'case':case,'log_age_s':round(datetime.datetime.now().timestamp()-p.stat().st_mtime,1)}
 if rows:out.update(step=int(rows[-1][0]),accepted_ns=float(rows[-1][1])*1e9,dt_ns=float(rows[-1][2])*1e9)
 out['saved_and_exited']=any(x in s for x in ['总时间:','Total time:'])
 tail='\n'.join(s.splitlines()[-30:])
 stopped=any(x in tail for x in ['已停止','Solver stopped','Solver interrupted','瞬态求解器中断'])
 out['recent_error']=tail[-600:] if out['saved_and_exited'] and not stopped and any(x in tail for x in ['错误','Error:','Exception:']) else None
 if stopped:out['stopped_before_requested_end']=True
 exported=R/'results'/f'{case}_fields.json'
 if exported.exists():
  out['exported_last_saved_ns']=json.loads(exported.read_text(encoding='utf8'))['times_s'][-1]*1e9
 print(json.dumps(out,ensure_ascii=True))
