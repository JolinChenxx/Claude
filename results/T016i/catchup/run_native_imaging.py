"""Repeat the prescribed T016e native imaging after later solver completions.

Scheduling and provenance only; use job.ps1 for execution. --check-only does
not launch any computation. The original completed queue stays unchanged.
"""
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

REPO = Path(__file__).resolve().parents[3]
JOB = Path('D:/sim_runs/T016i_catchup_01')
PLAN = Path(__file__).with_name('plan.json')
PYTHON = Path('D:/sim_tools/envs/lipsim/python.exe')


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def now():
    return dt.datetime.now().astimezone().isoformat()


def save(path, value):
    tmp = path.with_suffix('.tmp')
    tmp.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')
    tmp.replace(path)


def finished(run):
    for name in ('pluto.log', 'pluto.0.log'):
        path = run / name
        if path.is_file():
            with path.open('rb') as stream:
                stream.seek(max(0, path.stat().st_size - 2000))
                if b'Done' in stream.read():
                    return True
    return False


def verify(plan, helper, series):
    for item in plan['frozen_files']:
        assert sha(item['path']) == item['sha256'], item['path']
    for item in plan['snapshot_files']:
        st = Path(item['path']).stat()
        assert (st.st_size, st.st_mtime_ns) == (item['bytes'], item['mtime_ns']), item['path']
    expected = [Path(r['run']) for r in plan['runs'] if r['series'] == series]
    actual = [p for p in sorted(Path('D:/sim_runs/T016e').glob(series + '_*/it*'))
              if finished(p) and not (p/'image_v1/image_summary.json').exists()]
    assert actual == expected, 'Pending run inventory changed: ' + repr(actual)
    for run in expected:
        assert not (run/'image_v1').exists(), 'Output already exists: ' + str(run)
        helper.ensure_no_duplicate(['--out', str(run/'image_v1')])
    cap = helper.capacity()
    assert cap['can_start'], 'Insufficient capacity: ' + json.dumps(cap)
    return cap


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check-only', action='store_true')
    args = parser.parse_args()
    plan = json.loads(PLAN.read_text(encoding='utf-8'))
    assert plan['job_id'] == JOB.name and plan['runner_sha256'] == sha(__file__)
    helper_path = REPO/'results/T019/run_split.py'
    assert sha(helper_path) == plan['capacity_helper_sha256']
    spec = importlib.util.spec_from_file_location('native_capacity', helper_path)
    helper = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(helper)
    helper.JOB = JOB
    helper.RESERVATION = 16
    caps = [verify(plan, helper, row['series']) for row in plan['commands']]
    print(json.dumps({'checked': now(), 'capacity': caps[-1], 'pending_runs': plan['runs']}), flush=True)
    if args.check_only:
        return 0
    assert not (JOB/'batch_summary.json').exists(), 'Do not overwrite an existing job'
    JOB.mkdir(exist_ok=True)
    save(JOB/'resource_reservation.json', {'cores': 16, 'slots': 1, 'workers_per_image': 16})
    env = dict(os.environ, PYTHONUTF8='1', PYTHONPATH=str(REPO/'src'),
               OMP_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1')
    prefix = PYTHON.parent
    env['PATH'] = os.pathsep.join([str(prefix), str(prefix/'Library/bin'), str(prefix/'Scripts'), env.get('PATH', '')])
    psutil.Process().nice(psutil.BELOW_NORMAL_PRIORITY_CLASS)
    save(JOB/'execution.json', {'job_id': JOB.name, 'started': now(),
         'runner_sha256': sha(__file__), 'plan_sha256': sha(PLAN), 'capacity': caps[-1]})
    records = []
    for row in plan['commands']:
        verify(plan, helper, row['series'])
        command = row['command']
        assert command == [str(PYTHON), str(REPO/'scripts/image_members.py'),
            'D:/sim_runs/T016e', '--series', row['series'], '--tables', 'D:/sim_runs/tables/v1c',
            '--workers', '16', '--scenarios', 'configs/imaging/v1.yaml', '--outname', 'image_v1']
        record = {'batch': 'T016e', 'series': row['series'], 'command': command, 'started': now()}
        print('Running:', command, flush=True)
        result = subprocess.run(command, cwd=REPO, env=env)
        record.update(finished=now(), returncode=result.returncode)
        records.append(record)
        save(JOB/'batch_summary.json', records)
        print('Return code:', result.returncode, 'T016e', row['series'], flush=True)
        if result.returncode:
            return result.returncode
    return 0


if __name__ == '__main__':
    sys.exit(main())
