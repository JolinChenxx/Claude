"""Merge completed fixed-physics stages and produce the provisional early S4 plot.

Only waits for real saved/exported data; no missing exposure is synthesized.
"""
from pathlib import Path
import csv,sys,json,time,subprocess,datetime
R=Path(__file__).resolve().parent
out,*cases=sys.argv[1:]
deadline=datetime.datetime.fromisoformat(json.loads((R/'round2_session.json').read_text())['deadline_utc'].replace('Z','+00:00'))
required=[R/'results'/f'{c}_verified_summary.json' for c in cases]
while not all(p.exists() for p in required):
    if datetime.datetime.now(datetime.timezone.utc)>=deadline:raise RuntimeError('Session ended before requested exports completed')
    time.sleep(30)
radiation_cases=[c for c in cases if json.loads((R/'results'/f'{c}_fields.json').read_text())['times_s'][-1]>=113e-9-1e-16]
if not radiation_cases:raise RuntimeError('No stage reaches the first exposure')
while not all((R/'results'/f'{c}_radiation/method.json').exists() for c in radiation_cases):
    if datetime.datetime.now(datetime.timezone.utc)>=deadline:raise RuntimeError('Session ended before requested radiation exports completed')
    time.sleep(30)
for c in cases:
    summary=json.loads((R/'results'/f'{c}_verified_summary.json').read_text())
    if summary.get('accepted') is False:raise RuntimeError(f'{c} is explicitly rejected')
for suffix in ['history','balances','mechanism','mesh_audit']:
    rows=[];header=None
    for c in cases:
        with (R/'results'/f'{c}_{suffix}.csv').open(newline='') as f:
            reader=csv.reader(f);h=next(reader)
            if header is not None and h!=header:raise ValueError(f'{suffix} column mismatch')
            header=h;rows.extend(reader)
    rows.sort(key=lambda row:float(row[0]));unique=[]
    for row in rows:
        if not unique or float(row[0])-float(unique[-1][0])>1e-16:unique.append(row)
    with (R/'results'/f'{out}_{suffix}.csv').open('w',newline='') as f:
        w=csv.writer(f);w.writerow(header);w.writerows(unique)
def run(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],check=True)
run('summarize_round2.py',out)
run('merge_radiation.py',out,*radiation_cases)
run('camera_native.py',out,'--provisional')
(R/'results'/f'{out}_stages.json').write_text(json.dumps({'case':out,'source_stages':cases,'radiation_stages':radiation_cases,'calibration':'provisional unless a previously frozen global calibration exists','merge':'saved data, earliest duplicate retained, no gap filling'},indent=2))
print('SAVED_STAGES_COLLECTED',out,flush=True)
