"""Run the four prescribed T019 images with two 16-worker slots via job.ps1.

Scheduling only: no changes to the component model, tables or numerical settings.
--check-only validates the frozen plan and capacity without starting computation.
"""
from pathlib import Path
import argparse
import concurrent.futures as cf
import datetime as dt
import hashlib
import json
import os
import re
import subprocess

import psutil

REPO = Path('D:/Codex/2026-11-11/wo/outputs/Claude')
ROOT = Path('D:/sim_runs')
JOB = ROOT / 'T019_split_01'
PLAN = REPO / 'results/T019/plan.json'
PYTHON = Path('D:/sim_tools/envs/lipsim/python.exe')
WORKERS = 16
SLOTS = 2
RESERVATION = WORKERS * SLOTS


def now():
    return dt.datetime.now().astimezone().isoformat()


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def save(path, value):
    temporary = path.with_suffix('.tmp')
    temporary.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')
    temporary.replace(path)


def capacity():
    reservations = {}
    identities = []
    covered = {os.getpid()}
    for path in ROOT.glob('*/job.json'):
        meta = json.loads(path.read_text(encoding='utf-8-sig'))
        if (path.parent / 'exitcode.txt').exists():
            continue
        try:
            p = psutil.Process(meta['pid'])
            if abs(p.create_time()-dt.datetime.fromisoformat(meta['started']).timestamp()) > 5:
                continue
            children = p.children(recursive=True)
            if path.parent == JOB:
                covered.update([p.pid] + [c.pid for c in children])
                continue
            if not children:
                continue
            cmd = meta['cmd'].replace('\\', '/').lower()
            if 'lipsim.batch' in cmd:
                match = re.search(r'--max-cores\s+(\d+)', cmd)
                if not match:
                    raise RuntimeError('Unknown solver reservation: ' + meta['id'])
                cores = int(match[1])
            elif any(s in cmd for s in ['postprocessing/run_it', 'run_native_imaging.py',
                                        'run_reference_imaging.py', 'image_members.py']):
                cores = 16
            elif (path.parent / 'resource_reservation.json').exists():
                cores = int(json.loads((path.parent / 'resource_reservation.json').read_text())['cores'])
            else:
                raise RuntimeError('Unclassified active job: ' + meta['id'])
            reservations[meta['id']] = cores
            covered.update([p.pid] + [c.pid for c in children])
            identities.append(dict(job=meta['id'], pid=p.pid, created=p.create_time(),
                                   children=[c.pid for c in children], cores=cores))
        except psutil.NoSuchProcess:
            continue
    ranks = []
    for p in psutil.process_iter(['pid', 'name']):
        try:
            name = (p.info['name'] or '').lower()
            if name == 'pluto.exe':
                ranks.append(p.pid)
                if p.pid not in covered:
                    reservations[f'uncovered_solver_{p.pid}'] = 1
            elif name == 'python.exe' and p.pid not in covered:
                args = p.cmdline()
                if 'lipsim.image' in args:
                    if '--workers' not in args:
                        raise RuntimeError('Unknown imaging workers: ' + str(p.pid))
                    reservations[f'uncovered_image_{p.pid}'] = int(args[args.index('--workers')+1])
        except psutil.NoSuchProcess:
            continue
    used = sum(reservations.values())
    return dict(checked=now(), reservations=reservations, wrapper_identities=identities,
                pluto_ranks=len(ranks), reserved_other_cores=used, requested_cores=RESERVATION,
                including_own_reservation=used+RESERVATION, free_before=64-used,
                can_start=used+RESERVATION <= 64)


def ensure_no_duplicate(command):
    target = os.path.normcase(os.path.abspath(command[command.index('--out')+1]))
    for p in psutil.process_iter(['pid', 'name']):
        try:
            if (p.info['name'] or '').lower() != 'python.exe':
                continue
            args = p.cmdline()
            if '--out' in args and os.path.normcase(os.path.abspath(args[args.index('--out')+1])) == target:
                raise RuntimeError('Output already active: PID ' + str(p.pid) + ' ' + target)
        except psutil.NoSuchProcess:
            continue


def verify_frozen_inputs(plan):
    for row in plan['frozen_files']:
        assert sha(row['path']) == row['sha256'], 'Input/code changed: ' + row['path']
    for row in plan['commands']:
        for file in row['snapshot_files']:
            stat = Path(file['path']).stat()
            assert stat.st_size == file['bytes'] and stat.st_mtime_ns == file['mtime_ns'], file['path']


def preflight(plan):
    assert plan['job_id'] == JOB.name and plan['parallel_slots'] == SLOTS
    assert len(plan['commands']) == 4 and SLOTS <= 3
    assert plan['runner_sha256'] == sha(__file__)
    verify_frozen_inputs(plan)
    targets = []
    for row in plan['commands']:
        args = row['command']
        assert args[:3] == [str(PYTHON), '-m', 'lipsim.image']
        assert args[args.index('--workers')+1] == str(WORKERS)
        assert Path(args[args.index('--tables')+1]) == ROOT/'tables/v1c'
        target = Path(args[args.index('--out')+1])
        assert not target.exists(), 'Existing output requires explicit review: ' + str(target)
        targets.append(target)
        ensure_no_duplicate(args)
    assert len(set(targets)) == 4
    return capacity()


def execute(row, env):
    log = JOB / (row['name'] + '.log')
    with log.open('xb') as output:
        result = subprocess.run(row['command'], cwd=REPO, env=env,
                                stdout=output, stderr=subprocess.STDOUT)
    return dict(returncode=result.returncode, ended=now(), log=str(log))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check-only', action='store_true')
    args = ap.parse_args()
    plan = json.loads(PLAN.read_text(encoding='utf-8'))
    available = preflight(plan)
    print(json.dumps(available), flush=True)
    if args.check_only:
        return 0 if available['can_start'] else 75
    if not available['can_start']:
        print('Insufficient capacity; no computation launched.', flush=True)
        return 75
    JOB.mkdir(exist_ok=True)
    summary_path = JOB/'postprocess_summary.json'
    assert not summary_path.exists(), 'Existing job summary requires review; do not overwrite it'
    save(JOB/'resource_reservation.json', dict(cores=RESERVATION, slots=SLOTS, workers_per_image=WORKERS))
    env = dict(os.environ)
    prefix = PYTHON.parent
    env.update(PYTHONPATH=str(REPO/'src'), PYTHONUTF8='1', OMP_NUM_THREADS='1',
               OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1')
    env['PATH'] = os.pathsep.join([str(prefix), str(prefix/'Library/bin'), str(prefix/'Scripts'), env.get('PATH', '')])
    psutil.Process().nice(psutil.BELOW_NORMAL_PRIORITY_CLASS)
    summary = dict(job_id=JOB.name, started=now(), state='running', runner_sha256=sha(__file__),
                   plan_sha256=sha(PLAN), initial_capacity=available, reserved_cores=RESERVATION,
                   parallel_slots=SLOTS, commands=[])
    save(summary_path, summary)
    pending = list(plan['commands'])
    futures = {}
    failure = False
    with cf.ThreadPoolExecutor(max_workers=SLOTS) as pool:
        while pending or futures:
            while pending and len(futures) < SLOTS and not failure:
                item = pending[0]
                try:
                    verify_frozen_inputs(plan)
                    available = capacity()
                    if not available['can_start']:
                        raise RuntimeError('Capacity changed: ' + json.dumps(available))
                    ensure_no_duplicate(item['command'])
                    output = Path(item['command'][item['command'].index('--out')+1])
                    assert not output.exists(), 'Output appeared before launch: ' + str(output)
                except Exception as error:
                    failure = True
                    summary['scheduler_error'] = repr(error)
                    break
                pending.pop(0)
                row = dict(name=item['name'], command=item['command'], started=now(),
                           log=str(JOB/(item['name']+'.log')), execution='computed_by_owner')
                summary['commands'].append(row)
                save(summary_path, summary)
                print('start', row['name'], flush=True)
                futures[pool.submit(execute, row, env)] = row
            if not futures:
                break
            done, _ = cf.wait(futures, return_when=cf.FIRST_COMPLETED)
            for future in done:
                row = futures.pop(future)
                try:
                    row.update(future.result())
                    failure |= row['returncode'] != 0
                except Exception as error:
                    row.update(error=repr(error), ended=now())
                    failure = True
                print('finished', row['name'], row.get('returncode'), flush=True)
            summary['pending_commands'] = [r['name'] for r in pending]
            save(summary_path, summary)
            # A failure prevents new launches; existing images finish without intervention.
    summary.update(state='failed' if failure else 'completed', ended=now(),
                   pending_commands=[r['name'] for r in pending])
    save(summary_path, summary)
    return 1 if failure else 0


if __name__ == '__main__':
    raise SystemExit(main())
