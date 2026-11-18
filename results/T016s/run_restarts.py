"""Run the authorized A/B restarts concurrently, each limited to 2400 seconds."""
import concurrent.futures
import datetime
import hashlib
import json
import pathlib
import subprocess
import time

ROOT = pathlib.Path('D:/sim_runs/T016s')
EXE = 'D:/sim_tools/pluto_lip/build_v1d/mpi/pluto.exe'
MPI = 'C:/Program Files/Microsoft MPI/Bin/mpiexec.exe'


def run(member, restart):
    directory = ROOT / member
    result_path = directory / 'diagnostic_run.json'
    command = [MPI, '-n', '8', EXE, '-restart', str(restart), '-dec', '8', '1']
    record = dict(member=member, restart=restart, command=command,
                  started=datetime.datetime.now().astimezone().isoformat(),
                  limit_seconds=2400, state='starting')
    with result_path.open('x', encoding='utf-8') as result:
        json.dump(record, result, indent=2)
    start = time.monotonic()
    with (directory / 'mpi_output.log').open('xb') as log:
        process = subprocess.Popen(command, cwd=directory, stdout=log,
                                   stderr=subprocess.STDOUT,
                                   creationflags=subprocess.CREATE_NO_WINDOW)
        record.update(pid=process.pid, state='running')
        result_path.write_text(json.dumps(record, indent=2), encoding='utf-8')
        print(json.dumps(record), flush=True)
        try:
            code = process.wait(timeout=2400)
            record.update(state='finished', timed_out=False, returncode=code)
        except subprocess.TimeoutExpired:
            killed = subprocess.run(['taskkill.exe', '/PID', str(process.pid), '/T', '/F'],
                                    capture_output=True, text=True, encoding='mbcs', errors='replace')
            record.update(state='time_limit', timed_out=True,
                          taskkill_returncode=killed.returncode,
                          taskkill_stdout=killed.stdout, taskkill_stderr=killed.stderr)
            try:
                record['returncode'] = process.wait(timeout=30)
            except subprocess.TimeoutExpired:
                record.update(state='termination_failed', returncode=None)
    record.update(seconds=round(time.monotonic() - start, 3),
                  ended=datetime.datetime.now().astimezone().isoformat())
    result_path.write_text(json.dumps(record, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(record), flush=True)
    return record


if __name__ == '__main__':
    manifest = json.loads((ROOT / 'preflight.json').read_text(encoding='utf-8'))
    assert hashlib.sha256(pathlib.Path(EXE).read_bytes()).hexdigest() == manifest['executable_sha256']
    for item in manifest['copies']:
        directory = ROOT / item['member']
        assert not (directory / 'diagnostic_run.json').exists(), 'Refusing duplicate run'
        for name, expected in item['hashes'].items():
            assert hashlib.sha256((directory / name).read_bytes()).hexdigest() == expected, name
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        futures = [pool.submit(run, member, manifest['restart']) for member in ['A', 'B']]
        results = [future.result() for future in futures]
    (ROOT / 'diagnostic_results.json').write_text(json.dumps(results, indent=2) + '\n', encoding='utf-8')
    # A successful authorized timed stop is recorded in JSON; wrapper rc=1 is expected then.
    raise SystemExit(0 if all(r['returncode'] == 0 and not r['timed_out'] for r in results) else 1)
