"""Request a cooperative saved-state stop after a physical-time milestone."""
from pathlib import Path
import json,sys,time
R=Path(__file__).resolve().parent;case=sys.argv[1];target=float(sys.argv[2]);p=R/'results'/case
while True:
    try:s=json.loads((p/'status.json').read_text())
    except (FileNotFoundError,json.JSONDecodeError):s={}
    if s.get('saved_time_ns',0)>=target-1e-6:
        (p/'stop_requested.txt').write_text(f'Complete current accepted macrostep after saved {target} ns.\n')
        print('COOPERATIVE_STOP_REQUESTED',case,s['saved_time_ns'],flush=True);break
    time.sleep(20)
