"""Prescribed T018c high thin it1 diagnostics and v1 imaging, via job.ps1.

Use --check-only for input/command/capacity preflight without computation.
Reserve 16 physical cores throughout; never stop or suspend another job.
"""
from pathlib import Path
import argparse
import datetime as dt
import hashlib
import json
import os
import re
import subprocess

import psutil

REPO = Path('D:/Codex/2026-11-11/wo/outputs/Claude')
ROOT = Path('D:/sim_runs')
JOB = ROOT / 'T018c_post_it1_thin_01'
PYTHON = Path('D:/sim_tools/envs/lipsim/python.exe')
TABLES = ROOT / 'tables/v1c'
MEMBERS = [('high_thin_G0', 'thin')]


def capacity():
    """Use current wrapper identities, reserving whole queues between children."""
    reservations = {}
    covered = set()
    identities = []
    for path in ROOT.glob('*/job.json'):
        meta = json.loads(path.read_text(encoding='utf-8-sig'))
        if (path.parent / 'exitcode.txt').exists():
            continue
        try:
            proc = psutil.Process(meta['pid'])
            expected = dt.datetime.fromisoformat(meta['started']).timestamp()
            if abs(proc.create_time() - expected) > 5:
                continue  # PID reuse, not this wrapper.
            children = proc.children(recursive=True)
        except psutil.NoSuchProcess:
            continue
        command = meta['cmd'].replace('\\', '/').lower()
        if path.parent == JOB:
            # The caller requests its own16 cores separately, avoiding double count.
            covered.update([proc.pid] + [p.pid for p in children])
            continue
        if not children:
            continue  # Inert wrapper from an authorized old stop.
        if 'lipsim.batch' in command:
            match = re.search(r'--max-cores\s+(\d+)', command)
            if match is None:
                raise RuntimeError('Unknown batch allocation: ' + meta['id'])
            cores = int(match.group(1))
        elif any(x in command for x in ['postprocessing/run_it', 'run_native_imaging.py',
                                        'run_reference_imaging.py', 'image_members.py']):
            cores = 16
        else:
            continue
        reservations[meta['id']] = cores
        covered.update([proc.pid] + [p.pid for p in children])
        identities.append({'job': meta['id'], 'pid': proc.pid, 'created': proc.create_time(),
                           'children': [p.pid for p in children], 'cores': cores})
    ranks = []
    for proc in psutil.process_iter(['pid', 'name']):
        try:
            name = (proc.info['name'] or '').lower()
            if name == 'pluto.exe':
                ranks.append(proc.pid)
                if proc.pid not in covered:
                    reservations[f'uncovered_solver_{proc.pid}'] = 1
            elif name == 'python.exe' and proc.pid not in covered:
                command = proc.cmdline()
                if 'lipsim.image' in command:
                    if '--workers' not in command:
                        raise RuntimeError('Unknown image worker count: ' + str(proc.pid))
                    reservations[f'uncovered_image_{proc.pid}'] = int(command[command.index('--workers') + 1])
        except psutil.NoSuchProcess:
            continue
    reserved = sum(reservations.values())
    return {'checked': dt.datetime.now().astimezone().isoformat(),
            'reservations': reservations, 'wrapper_identities': identities,
            'pluto_ranks': len(ranks), 'reserved_physical_cores': reserved,
            'free_physical_cores': 64 - reserved, 'requested_cores': 16,
            'can_start': reserved + 16 <= 64,
            'note': 'Excludes this runner; whole other queues retain their reservation during serial diagnostics.'}


def commands():
    for member, nec in MEMBERS:
        run = ROOT / 'T018c' / member / 'it1'
        diag = run / 'diagnostics'
        common = [str(PYTHON)]
        yield f'{member}_emission', common + [str(REPO / 'scripts/diag_emission.py'), str(run), '--tables', str(TABLES), '--times', '300', '500', '700', '1000', '--out', str(diag / 'diag_emission.csv')]
        yield f'{member}_core_energy', common + [str(REPO / 'scripts/core_energy.py'), str(run), '--tables', str(TABLES), '--nec', nec, '--out', str(diag / 'core_energy.csv')]
        yield f'{member}_shock_track', common + [str(REPO / 'scripts/shock_track.py'), str(run), '--out', str(diag / 'shock_track.csv')]
    for member, _ in MEMBERS:
        run = ROOT / 'T018c' / member / 'it1'
        for version in ['v1']:
            yield f'{member}_image_{version}', [str(PYTHON), '-m', 'lipsim.image', str(run), '--series', 'high', '--tables', str(TABLES), '--out', str(run / f'image_{version}'), '--workers', '16', '--scenarios', str(REPO / f'configs/imaging/{version}.yaml')]


def ensure_no_duplicate(command):
    """Reject an active process targeting this exact output; never stop it."""
    target = os.path.normcase(os.path.abspath(command[command.index('--out') + 1]))
    for process in psutil.process_iter(['pid', 'name']):
        try:
            if (process.info['name'] or '').lower() != 'python.exe':
                continue
            args = process.cmdline()
            if '--out' in args:
                other = os.path.normcase(os.path.abspath(args[args.index('--out') + 1]))
                if other == target:
                    raise RuntimeError('Output already owned by active PID ' + str(process.pid) + ': ' + target)
        except psutil.NoSuchProcess:
            continue


def preflight():
    for path in [PYTHON, TABLES / 'eos.bin', TABLES / 'opt.bin', REPO / 'configs/imaging/v1.yaml', REPO / 'configs/imaging/v2.yaml', REPO / 'scripts/diag_emission.py', REPO / 'scripts/core_energy.py', REPO / 'scripts/shock_track.py']:
        if not path.is_file():
            raise RuntimeError('Missing required input: ' + str(path))
    for member, _ in MEMBERS:
        complete = json.loads((REPO / f'results/T018c/{member}/it1/completion.json').read_text(encoding='utf-8'))
        if complete['registry_row']['returncode'] != '0' or complete['final_budget']['t_ns'] != 800:
            raise RuntimeError('Solver not complete: ' + member)
        run = ROOT / 'T018c' / member / 'it1'
        for name in ['grid.out', 'dbl.out', 'pluto.ini', 'source/source_meta.json']:
            if not (run / name).is_file():
                raise RuntimeError('Missing run input: ' + str(run / name))
    plan = list(commands())
    assert len(plan) == 4 and len({n for n, _ in plan}) == 4
    for _, command in plan:
        ensure_no_duplicate(command)
    return capacity()


def save(path, summary):
    temporary = path.with_suffix('.tmp')
    temporary.write_text(json.dumps(summary, indent=2) + '\n', encoding='utf-8')
    temporary.replace(path)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check-only', action='store_true')
    args = ap.parse_args()
    available = preflight()
    print(json.dumps(available), flush=True)
    if args.check_only:
        return 0 if available['can_start'] else 75
    if not available['can_start']:
        print('No 16-core slot; no computation launched.', flush=True)
        return 75
    JOB.mkdir(parents=True, exist_ok=True)
    summary_path = JOB / 'postprocess_summary.json'
    summary = json.loads(summary_path.read_text(encoding='utf-8')) if summary_path.exists() else {'commands': []}
    summary.update(state='running', job_id=JOB.name, runner_sha256=hashlib.sha256(Path(__file__).read_bytes()).hexdigest(), initial_capacity=available)
    summary.setdefault('started', dt.datetime.now().astimezone().isoformat())
    save(summary_path, summary)
    env = dict(os.environ)
    env.update(PYTHONPATH=str(REPO / 'src'), PYTHONUTF8='1', OMP_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1')
    prefix = PYTHON.parent
    env['PATH'] = os.pathsep.join([str(prefix), str(prefix / 'Library/bin'), str(prefix / 'Scripts'), env.get('PATH', '')])
    psutil.Process().nice(psutil.BELOW_NORMAL_PRIORITY_CLASS)
    for name, command in commands():
        if any(row['name'] == name and row.get('returncode') == 0 for row in summary['commands']):
            continue
        available = capacity()
        if not available['can_start']:
            summary.update(state='deferred_capacity', last_capacity=available)
            save(summary_path, summary)
            print('Capacity changed; remaining commands deferred.', flush=True)
            return 75
        attempt = sum(row['name'] == name for row in summary['commands']) + 1
        logfile = JOB / f'{name}.attempt{attempt}.log'
        row = {'name': name, 'command': command, 'started': dt.datetime.now().astimezone().isoformat(), 'attempt': attempt, 'log': str(logfile)}
        summary['commands'].append(row)
        save(summary_path, summary)
        ensure_no_duplicate(command)
        print('start', name, flush=True)
        with logfile.open('wb') as stream:
            row['returncode'] = subprocess.run(command, cwd=REPO, env=env, stdout=stream, stderr=subprocess.STDOUT).returncode
        row['ended'] = dt.datetime.now().astimezone().isoformat()
        if row['returncode'] != 0:
            summary['state'] = 'failed'
        save(summary_path, summary)
        print('finished', name, row['returncode'], flush=True)
        if row['returncode'] != 0:
            return row['returncode']
    summary.update(state='completed', completed=dt.datetime.now().astimezone().isoformat())
    save(summary_path, summary)
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
