"""Run the prescribed T018b it0 postprocessing after a physical-core slot opens.

Launch through scripts/win/job.ps1, never by waiting in a heartbeat foreground.
Use --check-only for a read-only capacity check. No solver/dispatcher is stopped.
"""
from pathlib import Path
import argparse
import datetime as dt
import json
import os
import subprocess
import sys

import psutil

REPO = Path('D:/Codex/2026-11-11/wo/outputs/Claude')
ROOT = Path('D:/sim_runs')
JOB = ROOT / 'T018b_post_it0_01'
PYTHON = Path('D:/sim_tools/envs/lipsim/python.exe')
TABLES = ROOT / 'tables/v1c'


def same_process(pid, created):
    try:
        proc = psutil.Process(pid)
        return proc if abs(proc.create_time() - created) < 5 else None
    except psutil.NoSuchProcess:
        return None


def capacity():
    for pid, created in [(39740, 1795063781.6827476), (41556, 1795063848.8141325)]:
        proc = same_process(pid, created)
        if proc is not None and proc.status() != 'stopped':
            raise RuntimeError(f'Old imaging dispatcher {pid} is not held; re-evaluate allocation')
    reserved = 0
    reservations = {}
    for name in ['T016e', 'T018b', 'T018c']:
        meta = json.loads((ROOT / name / 'job.json').read_text(encoding='utf-8-sig'))
        created = dt.datetime.fromisoformat(meta['started']).timestamp()
        live = same_process(meta['pid'], created)
        reservations[name] = 16 if live and live.children(recursive=True) else 0
        reserved += reservations[name]
    reference = same_process(24040, 1795067589.6210315)
    reservations['active_reference_image'] = 16 if reference else 0
    reserved += reservations['active_reference_image']
    pluto_count = 0
    for proc in psutil.process_iter(['name']):
        if (proc.info['name'] or '').lower() == 'pluto.exe':
            pluto_count += 1
    if pluto_count > sum(reservations[name] for name in ['T016e', 'T018b', 'T018c']):
        raise RuntimeError('Unaccounted solver ranks; re-evaluate allocation')
    return {'reservations': reservations, 'pluto_ranks': pluto_count,
            'free_physical_cores': 64 - reserved, 'requested_cores': 16,
            'can_start': reserved <= 48}


def commands():
    for member, nec in [('low_thin_G0', 'thin'), ('low_trap_G0', 'trap')]:
        run = ROOT / 'T018b' / member / 'it0'
        diag = run / 'diagnostics'
        common = [str(PYTHON)]
        yield f'{member}_emission', common + [str(REPO / 'scripts/diag_emission.py'), str(run), '--tables', str(TABLES), '--times', '300', '500', '700', '1000', '--out', str(diag / 'diag_emission.csv')]
        yield f'{member}_core_energy', common + [str(REPO / 'scripts/core_energy.py'), str(run), '--tables', str(TABLES), '--nec', nec, '--out', str(diag / 'core_energy.csv')]
        yield f'{member}_shock_track', common + [str(REPO / 'scripts/shock_track.py'), str(run), '--out', str(diag / 'shock_track.csv')]
    for member in ['low_thin_G0', 'low_trap_G0']:
        run = ROOT / 'T018b' / member / 'it0'
        common = [str(PYTHON)]
        for version in ['v1', 'v2']:
            yield f'{member}_image_{version}', common + ['-m', 'lipsim.image', str(run), '--series', 'low', '--tables', str(TABLES), '--out', str(run / f'image_{version}'), '--workers', '16', '--scenarios', str(REPO / f'configs/imaging/{version}.yaml')]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check-only', action='store_true')
    args = ap.parse_args()
    available = capacity()
    print(json.dumps(available), flush=True)
    if args.check_only:
        return 0
    if not available['can_start']:
        print('No 16-core slot; no computation launched.', flush=True)
        return 75
    for member in ['low_thin_G0', 'low_trap_G0']:
        completion = json.loads((REPO / f'results/T018b/{member}/it0/completion.json').read_text(encoding='utf-8'))
        assert completion['registry_row']['returncode'] == '0'
        assert completion['final_budget']['t_ns'] == 1300
    JOB.mkdir(parents=True, exist_ok=True)
    summary_path = JOB / 'postprocess_summary.json'
    summary = json.loads(summary_path.read_text(encoding='utf-8')) if summary_path.exists() else {'commands': []}
    env = dict(os.environ)
    env.update(PYTHONPATH=str(REPO / 'src'), PYTHONUTF8='1', OMP_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1')
    prefix = PYTHON.parent
    env['PATH'] = os.pathsep.join([str(prefix), str(prefix / 'Library/bin'), str(prefix / 'Scripts'), env.get('PATH', '')])
    psutil.Process().nice(psutil.BELOW_NORMAL_PRIORITY_CLASS)
    for name, command in commands():
        if any(row['name'] == name and row.get('returncode') == 0 for row in summary['commands']):
            continue
        if not capacity()['can_start']:
            print('Capacity changed; remaining commands deferred.', flush=True)
            return 75
        attempt = sum(row['name'] == name for row in summary['commands']) + 1
        row = {'name': name, 'command': command, 'started': dt.datetime.now().astimezone().isoformat(), 'attempt': attempt}
        logfile = JOB / f'{name}.attempt{attempt}.log'
        print('start', name, flush=True)
        with logfile.open('wb') as stream:
            row['returncode'] = subprocess.run(command, cwd=REPO, env=env, stdout=stream, stderr=subprocess.STDOUT).returncode
        row['ended'] = dt.datetime.now().astimezone().isoformat()
        row['log'] = str(logfile)
        summary['commands'].append(row)
        summary_path.write_text(json.dumps(summary, indent=2) + '\n', encoding='utf-8')
        print('finished', name, row['returncode'], flush=True)
        if row['returncode'] != 0:
            return row['returncode']
    summary['completed'] = dt.datetime.now().astimezone().isoformat()
    summary_path.write_text(json.dumps(summary, indent=2) + '\n', encoding='utf-8')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
