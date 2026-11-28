"""Continue the active accepted trajectory through all S4 gates and1us.

Waits on this calculation only; reports errors to local logs for the active task.
No changes to physics, workers, camera gain or image registration.
"""
from pathlib import Path
import os,sys,json,time,subprocess
R=Path(__file__).resolve().parent
stages=[('HC454',180.,'HC455',835.),('HC455',835.,'HC456',1000.)]
for source,target,case,end in stages:
    while True:
        folder=R/'results'/source
        try:s=json.loads((folder/'status.json').read_text())
        except (FileNotFoundError,json.JSONDecodeError):s={}
        err=R/'logs'/f'{source}.err'
        if err.exists() and 'Traceback' in err.read_text(encoding='utf8',errors='replace'):
            print('QUEUE_STOPPED_AT_SOURCE_ERROR',source,s.get('saved_time_ns'),flush=True);sys.exit(1)
        if s.get('saved_time_ns',0)>=target-1e-6:break
        if (folder/'stop_requested.txt').exists():
            print('QUEUE_STOPPED_AT_REQUESTED_SOURCE_CHECKPOINT',source,flush=True);sys.exit(1)
        time.sleep(60)
    # Last native result was read before status publication; workers are now free.
    env=os.environ.copy();env['ROUTE1_PHASE_CHARGE_PARTNERS']='1';env['ROUTE1_CURRENT_ABSORPTION']='1';env['ROUTE1_PLASMA_WAVE_BOUND']='1'
    subprocess.run([sys.executable,'-X','utf8',str(R/'start_split_continuation.py'),case,source,str(end),'Cu_HEM_wide_20261110','--parallel-hot','--four-hot','--actual-removal','--direct-trace','--balanced-hot'],env=env,check=True)
    print('FULL_SEQUENCE_CONTINUATION_STARTED',case,end,flush=True)
