"""Cheap job-local snapshots; deadlines create notices for the active task.

Does not message the user, launch physics changes, or claim a PDF was delivered.
"""
from pathlib import Path
from datetime import datetime,timezone,timedelta
import json,time,subprocess,sys
R=Path(__file__).resolve().parent;tz=timezone(timedelta(hours=8));last=None
while True:
    config=json.loads((R/'day_session_20261110.json').read_text(encoding='utf-8'))
    if not config['active']:break
    now=datetime.now(tz)
    p=subprocess.run([sys.executable,'-X','utf8',str(R/'status.py'),*config['cases']],capture_output=True,text=True,encoding='utf-8')
    snapshot={'beijing':now.isoformat(),'cases':[json.loads(x) for x in p.stdout.splitlines() if x.startswith('{')], 'pdf_due':[x for x in config['pdf_deadlines_beijing'] if now>=datetime.fromisoformat(x) and x not in config.get('pdf_completed',[])]}
    snapshot['split_material_cases']={}
    for case in config.get('split_material_cases',[]):
        path=R/'results'/case/'status.json'
        if path.exists():
            try:snapshot['split_material_cases'][case]=json.loads(path.read_text(encoding='utf-8'))
            except json.JSONDecodeError:pass # A snapshot may coincide with a file update.
        failures=[]
        for suffix in ['log','err']:
            log=R/'logs'/f'{case}.{suffix}'
            if log.exists():
                tail=log.read_text(encoding='utf-8',errors='replace')[-6000:]
                if 'Traceback' in tail:failures.append(str(log))
        if failures:
            snapshot['split_material_cases'].setdefault(case,{'case':case}).update(solver_failed=True,error_logs=failures)
    (R/'results/day_latest_status.json').write_text(json.dumps(snapshot,ensure_ascii=False,indent=2),encoding='utf-8')
    hour=now.strftime('%Y%m%d_%H00')
    if now.minute==0 and hour!=last:
        (R/'results'/f'day_hour_{hour}.json').write_text(json.dumps(snapshot,ensure_ascii=False,indent=2),encoding='utf-8');last=hour
    time.sleep(60)
