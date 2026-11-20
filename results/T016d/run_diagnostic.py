"""Run only the checked restart, with the card's 1200-second wall limit."""
from pathlib import Path
import csv, datetime, hashlib, io, json, os, subprocess, time
import psutil

REPO = Path(__file__).resolve().parents[2]
RUN = Path('D:/sim_runs/T016d/high_trap_G0_it3')
EXE = Path('D:/sim_tools/pluto_lip/build_v1f/mpi/pluto.exe')
MPI = 'C:/Program Files/Microsoft MPI/Bin/mpiexec.exe'

def sha(p):
    with p.open('rb') as f:
        return hashlib.file_digest(f, 'sha256').hexdigest()

meta = json.loads((RUN/'restart_preflight.json').read_text())
assert sha(EXE) == meta['executable_sha256']
for name, h in meta['hashes'].items():
    assert sha(RUN/name) == h, name
path = RUN/'diagnostic_run.json'
command = [MPI, '-n', '8', str(EXE), '-restart', str(meta['last_dbl_index']), '-dec', '8', '1']
commit = subprocess.check_output(['git', 'rev-parse', '--short', 'HEAD'], cwd=REPO, text=True).strip()
record = dict(command=command, started=datetime.datetime.now().astimezone().isoformat(),
              limit_seconds=1200, state='starting', execution_commit=commit,
              restart_index=meta['last_dbl_index'], restart_time_ns=meta['restart_record']['time_ns'])
with path.open('x') as f:
    json.dump(record, f, indent=2)
env = os.environ.copy()
env.update(OMP_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1')
env['PATH'] = 'C:/Program Files/ANSYS Inc/v261/SCADE Rapid Prototyper/contrib/Msys64/mingw64/bin;' + env['PATH']
start = time.monotonic()
with (RUN/'mpi_output.log').open('xb') as log:
    p = subprocess.Popen(command, cwd=RUN, env=env, stdout=log, stderr=subprocess.STDOUT,
                         creationflags=subprocess.CREATE_NO_WINDOW | subprocess.BELOW_NORMAL_PRIORITY_CLASS)
    record.update(pid=p.pid, state='running')
    path.write_text(json.dumps(record, indent=2)+'\n')
    print(json.dumps(record), flush=True)
    try:
        rc = p.wait(timeout=1200)
        record.update(state='finished' if rc == 0 else 'failed', timed_out=False, returncode=rc)
    except subprocess.TimeoutExpired:
        targets = []
        for q in psutil.process_iter(['pid', 'name']):
            if q.info['name'] in ('mpiexec.exe', 'pluto.exe') and Path(q.cwd()) == RUN:
                targets.append((q.pid, q.create_time(), q.name()))
        assert any(pid == p.pid for pid, _, _ in targets)
        record.update(state='stopping_at_time_limit', timed_out=True, stop_targets=targets)
        path.write_text(json.dumps(record, indent=2)+'\n')
        for pid, created, name in sorted(targets, key=lambda r: r[2] != 'pluto.exe'):
            try:
                q = psutil.Process(pid)
                assert q.create_time() == created and Path(q.cwd()) == RUN
                q.kill()
            except psutil.NoSuchProcess:
                pass
        rc = p.wait(timeout=30)
        record.update(state='time_limit', returncode=rc)
record.update(seconds=time.monotonic()-start, finished=datetime.datetime.now().astimezone().isoformat())
path.write_text(json.dumps(record, indent=2)+'\n')
print(json.dumps(record), flush=True)
row = [record['started'], 'T016d', 'high_trap_G0_diag', 3, 'G0', 'trap',
       'configs/cases/v0_high.yaml', 8, rc, round(record['seconds'], 3), commit,
       sha(REPO/'data/params/registry.yaml')[:16], meta['hashes']['pluto.ini'][:16], str(RUN)]
buf = io.StringIO(newline=''); csv.writer(buf).writerow(row)
with (REPO/'results/run_registry.csv').open('a', encoding='utf8', newline='') as f:
    f.write(buf.getvalue())
raise SystemExit(0 if record['state'] in ('finished', 'time_limit') else 1)
