"""Run the authorized low G1 continuation in its independent directory."""
from pathlib import Path
import datetime, hashlib, json, subprocess, time

run=Path('D:/sim_runs/T016e_r/low_thin_G1/it0')
meta=json.loads((run/'restart_preflight.json').read_text())
exe=Path('D:/sim_tools/pluto_lip/build_v1d/mpi/pluto.exe')
assert hashlib.sha256(exe.read_bytes()).hexdigest()==meta['executable_sha256']
assert hashlib.sha256((run/'pluto.ini').read_bytes()).hexdigest()==meta['ini_after_sha256']
for name,h in meta['hashes'].items():
    assert hashlib.sha256((run/name).read_bytes()).hexdigest()==h, name
command=['C:/Program Files/Microsoft MPI/Bin/mpiexec.exe','-n','16',str(exe),'-restart',str(meta['last_dbl_index']),'-dec','16','1']
record=dict(command=command,started=datetime.datetime.now().astimezone().isoformat(),restart_time_ns=meta['restart_record']['time_ns'],state='starting')
path=run/'restart_run.json'
with path.open('x') as f:json.dump(record,f,indent=2)
start=time.monotonic()
with (run/'restart_mpi.log').open('xb') as log:
    process=subprocess.Popen(command,cwd=run,stdout=log,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
    record.update(pid=process.pid,state='running')
    path.write_text(json.dumps(record,indent=2)+'\n')
    print(json.dumps(record),flush=True)
    rc=process.wait()
record.update(state='finished' if rc==0 else 'failed',returncode=rc,finished=datetime.datetime.now().astimezone().isoformat(),seconds=time.monotonic()-start)
path.write_text(json.dumps(record,indent=2)+'\n')
print(json.dumps(record),flush=True)
raise SystemExit(rc)
