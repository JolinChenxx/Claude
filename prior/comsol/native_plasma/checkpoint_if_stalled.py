"""Save the named spatial run if it advances <1 ns in a 30-minute window."""
from pathlib import Path
import sys,time,re,json,subprocess
from datetime import datetime
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from run import COMSOL
case=sys.argv[1];duration=1800.;minimum_ns=1.
pattern=re.compile(r'^\s*\d+\s+([\d.eE+-]+)\s+[\d.eE+-]+(?:\s+out)?\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+',re.M)
first=None
while True:
    log=R/'logs'/f'{case}_solver.log'
    raw=log.read_text(encoding='utf8',errors='replace') if log.exists() else ''
    if '总时间:' in raw or 'Total time:' in raw:break
    matches=pattern.findall(raw)
    if matches:
        now=time.monotonic();accepted_ns=float(matches[-1])*1e9
        if first is None:first=(now,accepted_ns)
        if now-first[0]>=duration:
            record={'case':case,'at':datetime.now().isoformat(),'accepted_ns_log_precision':accepted_ns,'advance_ns':accepted_ns-first[1],'wall_seconds':now-first[0]}
            print(json.dumps(record),flush=True)
            if accepted_ns-first[1]<minimum_ns:
                p=subprocess.run([str(COMSOL/'bin/win64/comsolbatch.exe'),'-stop','100','-inputfile',str(R/'models'/f'{case}.mph')],capture_output=True)
                print('NORMAL_SAVE_REQUEST_EXIT',p.returncode,flush=True);sys.exit(p.returncode)
            first=(now,accepted_ns)
    time.sleep(60)
