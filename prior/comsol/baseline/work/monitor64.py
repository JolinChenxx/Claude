"""Read-only compact view of actual campaign files, with optional bounded wait."""
from pathlib import Path
import json,time,sys,datetime
R=Path(__file__).resolve().parents[1]
wait=float(sys.argv[1]) if len(sys.argv)>1 else 0
if wait:time.sleep(min(wait,60))
def read(name):
    try:return json.loads((R/name).read_text(encoding='utf-8-sig'))
    except (FileNotFoundError,json.JSONDecodeError):return {}
s=read('status.json');d=read('delivery_state.json');c=read('session_64.json')
out={'beijing':datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat(timespec='seconds'),
     'phase':d.get('phase'),'case':s.get('case'),'state':s.get('state'),
     'physical_ns':round(s.get('last_accepted_time_s',0)*1e9,3),
     'segment_wall_s':round(s.get('elapsed_seconds',0),1),
     'solver_pid':s.get('solver_pid'),'error':s.get('error') or d.get('error'),
     'main':d.get('main_case'),'grid':d.get('grid_case'),'time':d.get('time_case')}
case=d.get('main_case') if d.get('phase') in ['baseline_64_postprocessing','main_postprocessing'] else s.get('case')
if 'postprocessing' in (d.get('phase') or ''):
    expected=d.get('extra_grid_case') if d.get('phase')=='extra_grid_postprocessing_64' else None
    if expected:case=expected
    candidates=[R/'logs'/f'{c}_post_stdout.txt' for c in ([expected] if expected else [d.get('main_case'),d.get('time_case'),d.get('grid_case')]) if c]
    candidates=[p for p in candidates if p.exists()]
    if candidates:
        newest=max(candidates,key=lambda p:p.stat().st_mtime)
        case=newest.name.removesuffix('_post_stdout.txt')
        out['post_case']=case
if case:
    p=R/'logs'/f'{case}_post_stdout.txt'
    if p.exists():out['post_tail']=p.read_text(errors='replace').splitlines()[-2:]
print(json.dumps(out,ensure_ascii=False),flush=True)
