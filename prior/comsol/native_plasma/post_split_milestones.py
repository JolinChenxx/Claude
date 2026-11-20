"""Refresh only when another requested exposure window is actually complete."""
from pathlib import Path
import sys,subprocess,os,json,time
R=Path(__file__).resolve().parent;case=sys.argv[1]
for target in map(float,sys.argv[2:]):
    while True:
        try:s=json.loads((R/'results'/case/'status.json').read_text())
        except (FileNotFoundError,json.JSONDecodeError):s={}
        if s.get('saved_time_ns',0)>=target-1e-6:break
        log=R/'logs'/f'{case}.log'
        err=R/'logs'/f'{case}.err'
        failed=(log.exists() and 'Traceback' in log.read_text(encoding='utf8',errors='replace')) or (err.exists() and 'Traceback' in err.read_text(encoding='utf8',errors='replace'))
        if failed:
            print('POST_WAIT_STOPPED_AT_SOLVER_ERROR',case,s.get('saved_time_ns'),flush=True);sys.exit(1)
        time.sleep(30)
    subprocess.run([sys.executable,'-X','utf8',str(R/'post_split_when_ready.py'),case,str(target)],check=True)
    subprocess.run([sys.executable,'-X','utf8',str(R/'diagnose_split_energy.py'),'HC416',case],check=True)
    print('EXPOSURE_MILESTONE_POSTED',case,target,flush=True)
