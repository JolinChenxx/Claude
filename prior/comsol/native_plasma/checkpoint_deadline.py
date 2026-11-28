"""Normal COMSOL save after a wall-clock deadline; no result is invented."""
from pathlib import Path
from datetime import datetime
import sys,time,subprocess,json
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R));from run import COMSOL
case,deadline=sys.argv[1:3];deadline=datetime.fromisoformat(deadline)
while datetime.now()<deadline:time.sleep(min(30,max(0,(deadline-datetime.now()).total_seconds())))
if not (R/'models'/f'{case}.mph.status').exists():
 print('NO_ACTIVE_STATUS',case,flush=True);sys.exit(0)
p=subprocess.run([str(COMSOL/'bin/win64/comsolbatch.exe'),'-stop','100','-inputfile',str(R/'models'/f'{case}.mph')],capture_output=True)
print(json.dumps({'case':case,'deadline':deadline.isoformat(),'requested_at':datetime.now().isoformat(),'exit':p.returncode}),flush=True)
sys.exit(p.returncode)
