"""Load a code-only speed improvement at the next actual saved checkpoint."""
from pathlib import Path
import os,sys,time,subprocess,json
R=Path(__file__).resolve().parent;case,source,end=sys.argv[1:4]
target=float(sys.argv[4]) if len(sys.argv)>4 else 0.
while target:
 status=json.loads((R/'results'/source/'status.json').read_text())
 if status['saved_time_ns']>=target-1e-6:break
 err=(R/'logs'/f'{source}.err').read_text(encoding='utf8',errors='replace')
 if 'Traceback' in err:raise RuntimeError('Source failed before requested milestone')
 time.sleep(15)
(R/'results'/source/'stop_requested.txt').write_text('Load validated same-equation implementation or scheduling change at next saved state.\n')
while True:
 log=(R/'logs'/f'{source}.log').read_text(encoding='utf8',errors='replace')
 err=(R/'logs'/f'{source}.err').read_text(encoding='utf8',errors='replace')
 if 'Traceback' in err:raise RuntimeError('Source failed before requested checkpoint')
 if 'STOPPED_AT_SAVED_FRAME' in log:break
 time.sleep(15)
env=os.environ.copy();env.update(ROUTE1_PHASE_CHARGE_PARTNERS='1',ROUTE1_CURRENT_ABSORPTION='1',ROUTE1_PLASMA_WAVE_BOUND='1')
subprocess.run([sys.executable,'-X','utf8',str(R/'start_split_continuation.py'),case,source,end,'Cu_HEM_wide_20261110','--parallel-hot']+[flag for flag in ['--four-hot','--actual-removal','--trace-air','--direct-trace','--balanced-hot'] if flag in sys.argv],env=env,check=True)
print('SAME_EQUATION_RESTART_STARTED',case,flush=True)
