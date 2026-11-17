"""Run the two authorized diagnostic restarts, each limited to 3600 seconds."""
import concurrent.futures
import datetime
import json
import pathlib
import subprocess
import time

ROOT = pathlib.Path('D:/sim_runs/T016b')
EXE = 'D:/sim_tools/pluto_lip/build_v1c/mpi/pluto.exe'
MPI = 'C:/Program Files/Microsoft MPI/Bin/mpiexec.exe'


def run(member, restart):
    directory = ROOT / member
    result_path = directory / 'diagnostic_run.json'
    if result_path.exists():
        raise RuntimeError(f'Refusing duplicate run: {member}')
    command = [MPI, '-n', '8', EXE, '-restart', str(restart), '-dec', '8', '1']
    record = dict(member=member, restart=restart, command=command,
                  started=datetime.datetime.now().astimezone().isoformat(),
                  limit_seconds=3600, state='starting')
    result_path.write_text(json.dumps(record, indent=2))
    start = time.monotonic()
    with (directory / 'mpi_output.log').open('wb') as log:
        process = subprocess.Popen(command, cwd=directory, stdout=log,
                                   stderr=subprocess.STDOUT,
                                   creationflags=subprocess.CREATE_NO_WINDOW)
        record.update(pid=process.pid, state='running')
        result_path.write_text(json.dumps(record, indent=2))
        try:
            code = process.wait(timeout=3600)
            record.update(state='finished', timed_out=False, returncode=code)
        except subprocess.TimeoutExpired:
            killed = subprocess.run(['taskkill.exe', '/PID', str(process.pid), '/T', '/F'],
                                    capture_output=True, text=True)
            record.update(state='time_limit', timed_out=True,
                          taskkill_returncode=killed.returncode,
                          taskkill_stdout=killed.stdout, taskkill_stderr=killed.stderr)
            try:
                record['returncode'] = process.wait(timeout=30)
            except subprocess.TimeoutExpired:
                record.update(state='termination_failed', returncode=None)
    record.update(seconds=round(time.monotonic() - start, 3),
                  ended=datetime.datetime.now().astimezone().isoformat())
    result_path.write_text(json.dumps(record, indent=2) + '\n')
    print(json.dumps(record), flush=True)
    return record


if __name__ == '__main__':
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        futures = [pool.submit(run, member, restart) for member, restart in
                   [('low_thin_G0', 141), ('low_trap_G0', 142)]]
        results = [future.result() for future in futures]
    (ROOT / 'diagnostic_results.json').write_text(json.dumps(results, indent=2) + '\n')
    raise SystemExit(0 if all(r['returncode'] == 0 and not r['timed_out'] for r in results) else 1)
