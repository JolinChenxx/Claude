"""Low-cost local snapshots; hourly reporting/PDF deadlines for this running job."""
from pathlib import Path
from datetime import datetime,timezone,timedelta
import time,json,subprocess,sys
R=Path(__file__).resolve().parent;tz=timezone(timedelta(hours=8));done=set();last_hour=None
deadlines={'20261109_2300':datetime(2026,11,9,23,tzinfo=tz),'20261110_0700':datetime(2026,11,10,7,tzinfo=tz)}
while True:
 now=datetime.now(tz)
 content=json.loads((R/'night_report_content.json').read_text(encoding='utf-8'))
 p=subprocess.run([sys.executable,'-X','utf8',str(R/'status.py'),*content['cases']],capture_output=True,text=True,encoding='utf-8')
 snapshot={'beijing':now.isoformat(),'solver_status':p.stdout.strip().splitlines()}
 (R/'results/night_latest_status.json').write_text(json.dumps(snapshot,ensure_ascii=False,indent=2),encoding='utf-8')
 hour=now.strftime('%Y%m%d_%H00')
 if now.minute==0 and hour!=last_hour:
  (R/'results'/f'night_hour_{hour}.json').write_text(json.dumps(snapshot,ensure_ascii=False,indent=2),encoding='utf-8');last_hour=hour
 for label,due in deadlines.items():
  if now>=due and label not in done:
   log=R/'logs'/f'pdf_{label}.log'
   with log.open('w',encoding='utf-8') as f:subprocess.run([sys.executable,'-X','utf8',str(R/'make_night_report.py'),label],stdout=f,stderr=subprocess.STDOUT)
   done.add(label)
 if len(done)==len(deadlines):break
 time.sleep(30)
