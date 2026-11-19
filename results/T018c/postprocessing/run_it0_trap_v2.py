"""Single delegated image, with the unchanged prescribed T018c arguments."""
from pathlib import Path
import argparse
import datetime as dt
import hashlib
import json
import os
import subprocess
import sys

import psutil
import run_it0 as base

sys.path.insert(0, str(base.REPO / 'src'))
from lipsim.image.dispatch import MANIFEST, output_records

JOB = base.ROOT / 'T018c_post_it0_trap_v2_01'
base.JOB = JOB  # Capacity excludes only our own reservation.
NAME = 'high_trap_G0_image_v2'
COMMAND = dict(base.commands())[NAME]


def preflight():
    base.preflight()  # All input checks; only this command will be executed.
    entry, = [e for e in json.loads(MANIFEST.read_text())['delegations'] if e['name'] == NAME]
    assert entry['argv'] == COMMAND[3:]
    assert Path(entry['owner_job_dir']) == JOB
    assert entry['owner_runner'] == 'results/T018c/postprocessing/' + Path(__file__).name
    assert not (JOB / 'postprocess_summary.json').exists(), 'Refuse to reuse a prior launch'
    assert not Path(entry['output']).exists(), 'Refuse to overwrite prior image output'
    original = json.loads((Path(entry['original_job_dir']) / 'postprocess_summary.json').read_text())
    assert not any(r['name'] == NAME for r in original['commands']), 'Original queue already attempted target'
    return entry, base.capacity()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check-only', action='store_true')
    args = ap.parse_args()
    entry, available = preflight()
    print(json.dumps(available), flush=True)
    if args.check_only or not available['can_start']:
        return 0 if available['can_start'] else 75
    assert Path(os.environ['SIM_OUT_DIR'].strip()) == JOB, 'Launch via job.ps1 with correct ID'
    JOB.mkdir(parents=True, exist_ok=True)
    now = lambda: dt.datetime.now().astimezone().isoformat()
    row = dict(name=NAME, command=COMMAND, started=now(), attempt=1,
               execution='computed_by_owner', log=str(JOB / (NAME + '.attempt1.log')))
    summary = dict(state='running', job_id=JOB.name, started=now(), commands=[row],
                   runner_sha256=hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
                   initial_capacity=available, delegation=entry)
    summary_path = JOB / 'postprocess_summary.json'
    base.save(summary_path, summary)
    env = dict(os.environ)
    env.update(PYTHONPATH=str(base.REPO / 'src'), PYTHONUTF8='1', OMP_NUM_THREADS='1',
               OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1')
    prefix = base.PYTHON.parent
    env['PATH'] = os.pathsep.join([str(prefix), str(prefix / 'Library/bin'),
                                 str(prefix / 'Scripts'), env.get('PATH', '')])
    psutil.Process().nice(psutil.BELOW_NORMAL_PRIORITY_CLASS)
    print('start', NAME, flush=True)
    with Path(row['log']).open('wb') as stream:
        row['returncode'] = subprocess.run(COMMAND, cwd=base.REPO, env=env,
                                           stdout=stream, stderr=subprocess.STDOUT).returncode
    row['ended'] = now()
    summary['state'] = 'failed' if row['returncode'] else 'validating_output_inventory'
    base.save(summary_path, summary)
    if row['returncode']:
        return row['returncode']
    try:
        summary['output_files'] = output_records(entry['output'])
    except Exception as error:
        summary.update(state='failed_output_inventory', error=str(error))
        base.save(summary_path, summary)
        raise
    summary.update(state='completed', completed=now())
    base.save(summary_path, summary)
    print('finished', NAME, row['returncode'], flush=True)
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
