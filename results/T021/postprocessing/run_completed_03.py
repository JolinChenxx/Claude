"""Serialized T021/T023 postprocessing of seven verified completed runs."""
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime
from pathlib import Path
import argparse
import hashlib
import json
import os
import subprocess
import time
import psutil

REPO = Path(__file__).resolve().parents[3]
PYTHON = Path('D:/sim_tools/envs/lipsim/python.exe')
TABLES = Path('D:/sim_runs/tables/v1c')
JOB = Path('D:/sim_runs/T021_T023_post_03')
PREDECESSOR = Path('D:/sim_runs/T021_post_completed_02')
EVIDENCE = REPO / 'results/T021/postprocessing/completed_03_preflight.json'
TARGETS = [('T023', 'high_thin_G0', 'it0'), ('T023', 'high_thin_G0', 'it1'),
           ('T021', 'high_thin_G0', 'it2'), ('T023', 'high_trap_G0', 'it0'),
           ('T023', 'high_trap_G0', 'it1'), ('T023', 'low_trap_G0', 'it0'),
           ('T021', 'high_thin_G0', 'it1')]


def now():
    return datetime.now().astimezone().isoformat()


def sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def save(path, value):
    tmp = path.with_suffix('.tmp')
    tmp.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')
    tmp.replace(path)


def commands(batch, member, iteration):
    run = Path('D:/sim_runs') / batch / member / iteration
    diag = run / 'diagnostics'
    series, nec = member.split('_')[:2]
    plans = [
        ('emission', [str(PYTHON), str(REPO / 'scripts/diag_emission.py'), str(run), '--tables', str(TABLES), '--times', '100', '150', '200', '250', '300', '400', '500', '600', '700', '800', '1000', '1200', '--filter', '510', '12', '--out', str(diag / 'diag_emission.csv')]),
        ('core_energy', [str(PYTHON), str(REPO / 'scripts/core_energy.py'), str(run), '--tables', str(TABLES), '--nec', nec, '--out', str(diag / 'core_energy.csv')]),
        ('shock_track', [str(PYTHON), str(REPO / 'scripts/shock_track.py'), str(run), '--out', str(diag / 'shock_track.csv')]),
        ('image_v2', [str(PYTHON), '-m', 'lipsim.image', str(run), '--series', series, '--tables', str(TABLES), '--out', str(run / 'image_v2'), '--workers', '24', '--scenarios', str(REPO / 'configs/imaging/v2.yaml')])]
    if batch == 'T023':
        plans.append(('peak_lift', [str(PYTHON), str(REPO / 'scripts/peak_lift.py'), str(run / 'image_v2'), '--series', series, '--out', str(REPO / 'results' / batch / member / iteration / 'peak_lift.csv')]))
    return [{'batch': batch, 'member': member, 'iteration': iteration, 'name': name, 'command': command} for name, command in plans]


def predecessor_done():
    try:
        alive = abs(psutil.Process(31068).create_time() - 1795486665.8476477) < 0.01
    except psutil.NoSuchProcess:
        alive = False
    assert not alive, 'Previous queue still running; do not overlap'
    assert (PREDECESSOR / 'exitcode.txt').read_text().strip() == '0'
    summary = json.loads((PREDECESSOR / 'postprocess_summary.json').read_text())
    assert summary['state'] == 'completed' and len(summary['commands']) == 8
    assert all(r['returncode'] == 0 for r in summary['commands'])


def preflight():
    predecessor_done()
    assert not (JOB / 'job.json').exists() and not (JOB / 'postprocess_summary.json').exists(), 'Queue already started'
    inputs = [PYTHON, TABLES / 'eos.bin', TABLES / 'opt.bin', REPO / 'configs/imaging/v2.yaml',
              REPO / 'data/params/registry.yaml', REPO / 'results/surface/surface_rows.csv']
    inputs += sorted((REPO / 'src/lipsim').rglob('*.py'))
    inputs += sorted(p for p in (REPO / 'data/exp').rglob('*') if p.is_file() and 'paper' not in p.parts)
    inputs += [REPO / f'scripts/{n}.py' for n in ['diag_emission', 'core_energy', 'shock_track', 'peak_lift']]
    plans = []
    for batch, member, iteration in TARGETS:
        run = Path('D:/sim_runs') / batch / member / iteration
        completion_path = REPO / 'results' / batch / member / iteration / 'completion.json'
        completion = json.loads(completion_path.read_text())
        assert completion['registry_row']['returncode'] == '0'
        assert completion['final_budget']['t_ns'] == (800 if member.startswith('high') else 1300)
        assert not completion['nonfinite_budget_values']
        assert sha(run / 'budget.csv') == completion['files']['budget.csv']['sha256']
        inputs += [completion_path] + [run / p for p in ['budget.csv', 'source/source_meta.json', 'pluto.ini', 'grid.out', 'dbl.out']]
        for plan in commands(batch, member, iteration):
            assert not Path(plan['command'][plan['command'].index('--out') + 1]).exists(), 'Output already exists'
            plans.append(plan)
    return {'recorded': now(), 'job_id': JOB.name, 'commands': plans,
            'input_sha256': {str(p): sha(p) for p in inputs}, 'runner_sha256': sha(Path(__file__)),
            'max_workers': 24, 'predecessor': PREDECESSOR.name,
            'resource_policy': 'Claude priority note 2026-11-24 14:00: T023 high thin it0/it1 first, T021 high it2 next, then remaining trap/low and T021 high it1. Seven completed runs sequentially; three single-thread diagnostics concurrently, then 24-worker imaging, then T023 peak_lift. BelowNormal; OMP/BLAS1; no production intervention.',
            'time_policy': 'Emission requests 1000/1200 ns on 800 ns high runs retain the supplied nearest-final-snapshot behavior; no extrapolation or extension.'}


def verify_inputs(frozen):
    assert sha(Path(__file__)) == frozen['runner_sha256'], 'Runner changed'
    for path, expected in frozen['input_sha256'].items():
        assert sha(path) == expected, 'Frozen input changed: ' + path


def execute(plan, env):
    name = '_'.join([plan['batch'], plan['member'], plan['iteration'], plan['name']])
    row = {**plan, 'started': now(), 'log': str(JOB / (name + '.log'))}
    start = time.perf_counter()
    print('start', name, flush=True)
    with Path(row['log']).open('wb') as stream:
        row['returncode'] = subprocess.run(row['command'], cwd=REPO, env=env, stdout=stream, stderr=subprocess.STDOUT).returncode
    row.update(finished=now(), seconds=time.perf_counter() - start)
    print(json.dumps(row), flush=True)
    return row


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check-only', action='store_true')
    args = parser.parse_args()
    if args.check_only:
        frozen = preflight()
        save(EVIDENCE, frozen)
        print(json.dumps({'job_id': JOB.name, 'commands': len(frozen['commands']), 'inputs': len(frozen['input_sha256']), 'max_workers': 24}))
        return 0
    frozen = json.loads(EVIDENCE.read_text())
    assert not (JOB / 'postprocess_summary.json').exists(), 'Queue already executed'
    predecessor_done()
    verify_inputs(frozen)
    JOB.mkdir(parents=True, exist_ok=True)
    psutil.Process().nice(psutil.BELOW_NORMAL_PRIORITY_CLASS)
    summary = {'job_id': JOB.name, 'started': now(), 'state': 'running', 'preflight': frozen, 'commands': []}
    path = JOB / 'postprocess_summary.json'
    save(path, summary)
    env = dict(os.environ)
    env.update(PYTHONPATH=str(REPO / 'src'), PYTHONUTF8='1', PYTHONUNBUFFERED='1', OMP_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1')
    try:
        for batch, member, iteration in TARGETS:
            verify_inputs(frozen)
            plans = [p for p in frozen['commands'] if (p['batch'], p['member'], p['iteration']) == (batch, member, iteration)]
            for plan in plans:
                assert not Path(plan['command'][plan['command'].index('--out') + 1]).exists(), 'Duplicate output'
            with ThreadPoolExecutor(max_workers=3) as pool:
                for future in as_completed([pool.submit(execute, p, env) for p in plans[:3]]):
                    summary['commands'].append(future.result())
                    save(path, summary)
            assert all(r['returncode'] == 0 for r in summary['commands']), 'Diagnostic failure'
            for plan in plans[3:]:
                verify_inputs(frozen)
                summary['commands'].append(execute(plan, env))
                save(path, summary)
                assert summary['commands'][-1]['returncode'] == 0, 'Postprocessing failure'
            if (batch, member, iteration) in [('T023', 'high_thin_G0', 'it1'), ('T021', 'high_thin_G0', 'it2')]:
                summary.setdefault('milestones_ready_for_collection', []).append({'batch': batch, 'member': member, 'iteration': iteration, 'finished': now(), 'next_action': 'Collect and validate compact outputs; write/push milestone handoff on the next pass.'})
                save(path, summary)
        summary.update(state='completed', finished=now())
        save(path, summary)
        return 0
    except Exception as exc:
        summary.update(state='failed', finished=now(), error=repr(exc))
        save(path, summary)
        raise


if __name__ == '__main__':
    raise SystemExit(main())
