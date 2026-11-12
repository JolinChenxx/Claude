"""Lightweight saved-state watcher; exits for failure, milestone, or next hour.

Does not restart solvers, alter settings, or send user messages.
"""
from pathlib import Path
from datetime import datetime,timezone,timedelta
import sys,json,time
R=Path(__file__).resolve().parent;TZ=timezone(timedelta(hours=8))
due=datetime.now(TZ).replace(minute=0,second=0,microsecond=0)+timedelta(hours=1)
milestone=float(sys.argv[1]) if len(sys.argv)>1 else 115.
while True:
    cfg=json.loads((R/'day_session_20261110.json').read_text())
    case=cfg['split_material_cases'][-1];folder=R/'results'/case
    try:s=json.loads((folder/'status.json').read_text())
    except (FileNotFoundError,json.JSONDecodeError):s={}
    err=R/'logs'/f'{case}.err';error=err.read_text(encoding='utf8',errors='replace') if err.exists() else ''
    heartbeat=dict(beijing=datetime.now(TZ).isoformat(),case=case,saved_ns=s.get('saved_time_ns'),next_milestone_ns=milestone,next_hour=due.isoformat())
    (R/'results/active_watch.json').write_text(json.dumps(heartbeat,indent=2))
    reason='solver_failed' if 'Traceback' in error else 'milestone' if s.get('saved_time_ns',0)>=milestone-1e-6 else 'hourly_report' if datetime.now(TZ)>=due else None
    if reason:
        print(json.dumps(dict(heartbeat,event=reason,error_tail=error[-1500:] if reason=='solver_failed' else None)),flush=True);break
    time.sleep(min(60,max(1,(due-datetime.now(TZ)).total_seconds())))
