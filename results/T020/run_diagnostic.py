"""Execute the two prescribed T020 serial diagnostics via job.ps1, without model changes."""
from pathlib import Path
import argparse
import datetime as dt
import hashlib
import importlib.util
import json
import os
import subprocess
import sys

import psutil

REPO = Path(__file__).resolve().parents[2]
ROOT = REPO / 'results/T020'
JOB = Path('D:/sim_runs/T020_diag_01')
PYTHON = Path('D:/sim_tools/envs/lipsim/python.exe')


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def now():
    return dt.datetime.now().astimezone().isoformat()


def save(path, value):
    temporary = path.with_suffix('.tmp')
    temporary.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')
    temporary.replace(path)


def preflight(plan):
    assert plan['job_id'] == JOB.name and plan['runner_sha256'] == sha(__file__)
    for row in plan['frozen_files']:
        assert sha(row['path']) == row['sha256'], row['path']
    helper_path = REPO / 'results/T019/run_split.py'
    assert sha(helper_path) == plan['capacity_helper_sha256']
    spec = importlib.util.spec_from_file_location('diagnostic_capacity', helper_path)
    helper = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(helper)
    helper.JOB, helper.RESERVATION = JOB, 2
    assert len(plan['commands']) == 2
    for row in plan['commands']:
        expected_times = [100, 150, 200, 250, 300, 400, 500, 600, 700, 800]
        if row['series'] == 'low':
            expected_times += [1000, 1200]
        assert row['times'] == expected_times
        assert row['command'] == [str(PYTHON), str(REPO / 'scripts/diag_emission.py'), row['run'],
            '--tables', str(Path('D:/sim_runs/tables/v1c')), '--times', *map(str, expected_times),
            '--filter', '510', '12', '--out', str(ROOT / (row['member'] + '_diag.csv'))]
        assert not Path(row['command'][-1]).exists(), 'Do not overwrite existing diagnostic output.'
        assert not (ROOT / (row['member'] + '.log')).exists(), 'Do not overwrite an existing log.'
        for snapshot in row['snapshots']:
            stat = Path(snapshot['path']).stat()
            assert (stat.st_size, stat.st_mtime_ns) == (snapshot['bytes'], snapshot['mtime_ns'])
        helper.ensure_no_duplicate(row['command'])
    available = helper.capacity()
    assert available['can_start'], 'Insufficient capacity: ' + json.dumps(available)
    return available


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check-only', action='store_true')
    args = ap.parse_args()
    plan_path = ROOT / 'plan.json'
    plan = json.loads(plan_path.read_text(encoding='utf-8'))
    capacity = preflight(plan)
    if args.check_only:
        print(json.dumps({'checked': now(), 'capacity': capacity, 'commands': len(plan['commands'])}))
        return 0
    JOB.mkdir(exist_ok=True)
    summary_path = JOB / 'postprocess_summary.json'
    assert not summary_path.exists(), 'Existing job requires inspection, not a restart.'
    env = dict(os.environ, PYTHONPATH=str(REPO / 'src'), PYTHONUTF8='1', PYTHONUNBUFFERED='1',
               OMP_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1')
    prefix = PYTHON.parent
    env['PATH'] = os.pathsep.join([str(prefix), str(prefix / 'Library/bin'), str(prefix / 'Scripts'), env.get('PATH', '')])
    psutil.Process().nice(psutil.BELOW_NORMAL_PRIORITY_CLASS)
    save(JOB / 'resource_reservation.json', {'cores': 2, 'slots': 2, 'workers_per_command': 1})
    summary = {'job_id': JOB.name, 'started': now(), 'state': 'running',
               'runner_sha256': sha(__file__), 'plan_sha256': sha(plan_path),
               'initial_capacity': capacity, 'reserved_cores': 2, 'commands': []}
    save(summary_path, summary)
    processes = []
    try:
        for row in plan['commands']:
            log = ROOT / (row['member'] + '.log')
            stream = log.open('xb')
            try:
                process = subprocess.Popen(row['command'], cwd=REPO, env=env, stdout=stream, stderr=subprocess.STDOUT)
            finally:
                stream.close()
            record = {'name': row['member'], 'command': row['command'], 'started': now(),
                      'pid': process.pid, 'log': str(log), 'execution': 'computed_by_owner'}
            try:
                record['created'] = psutil.Process(process.pid).create_time()
            except psutil.NoSuchProcess:
                pass
            processes.append((process, record))
            summary['commands'].append(record)
            save(summary_path, summary)
            print('start', row['member'], process.pid, flush=True)
    except Exception as exc:
        summary['launch_error'] = repr(exc)
        save(summary_path, summary)
    # This wait occurs only inside the detached job.ps1 runner.
    for process, record in processes:
        record.update(returncode=process.wait(), ended=now())
        save(summary_path, summary)
        print('finished', record['name'], record['returncode'], flush=True)
    success = len(processes) == 2 and 'launch_error' not in summary and all(r['returncode'] == 0 for _, r in processes)
    summary.update(state='completed' if success else 'failed', ended=now())
    save(summary_path, summary)
    return 0 if success else 1


if __name__ == '__main__':
    sys.exit(main())
