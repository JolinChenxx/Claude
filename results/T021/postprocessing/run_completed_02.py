"""Queue completed high-it0/low-it1 diagnostics behind the existing image job."""
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
JOB = Path('D:/sim_runs/T021_post_completed_02')
PREDECESSOR = Path('D:/sim_runs/T021_post_low_it0_01')
PREDECESSOR_PID = 10588
PREDECESSOR_CREATED = 1795484727.808
EVIDENCE = REPO / 'results/T021/postprocessing/completed_02_preflight.json'
TARGETS = [('high_thin_G0', 'it0', 'high', 800), ('low_thin_G0', 'it1', 'low', 1300)]


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


def commands(member, iteration, series):
    run = Path('D:/sim_runs/T021') / member / iteration
    diag = run / 'diagnostics'
    return [
        ('emission', [str(PYTHON), str(REPO / 'scripts/diag_emission.py'), str(run), '--tables', str(TABLES), '--times', '100', '150', '200', '250', '300', '400', '500', '600', '700', '800', '1000', '1200', '--filter', '510', '12', '--out', str(diag / 'diag_emission.csv')]),
        ('core_energy', [str(PYTHON), str(REPO / 'scripts/core_energy.py'), str(run), '--tables', str(TABLES), '--nec', 'thin', '--out', str(diag / 'core_energy.csv')]),
        ('shock_track', [str(PYTHON), str(REPO / 'scripts/shock_track.py'), str(run), '--out', str(diag / 'shock_track.csv')]),
        ('image_v2', [str(PYTHON), '-m', 'lipsim.image', str(run), '--series', series, '--tables', str(TABLES), '--out', str(run / 'image_v2'), '--workers', '16', '--scenarios', str(REPO / 'configs/imaging/v2.yaml')])
    ]


def preflight():
    meta = json.loads((PREDECESSOR / 'job.json').read_text(encoding='utf-8-sig'))
    assert meta['id'] == PREDECESSOR.name and meta['pid'] == PREDECESSOR_PID
    inputs = [PYTHON, TABLES / 'eos.bin', TABLES / 'opt.bin', REPO / 'configs/imaging/v2.yaml']
    inputs += sorted((REPO / 'src/lipsim').rglob('*.py'))
    inputs += [REPO / f'scripts/{name}.py' for name in ['diag_emission', 'core_energy', 'shock_track']]
    plans = []
    for member, iteration, series, end in TARGETS:
        run = Path('D:/sim_runs/T021') / member / iteration
        completion = json.loads((REPO / 'results/T021' / member / iteration / 'completion.json').read_text())
        assert completion['registry_row']['returncode'] == '0' and completion['final_budget']['t_ns'] == end
        assert sha(run / 'budget.csv') == completion['files']['budget.csv']['sha256']
        inputs += [run / p for p in ['budget.csv', 'source/source_meta.json', 'pluto.ini', 'grid.out', 'dbl.out']]
        for name, command in commands(member, iteration, series):
            assert not Path(command[command.index('--out') + 1]).exists(), 'Output already exists'
            plans.append({'member': member, 'iteration': iteration, 'name': name, 'command': command})
    assert not (JOB / 'postprocess_summary.json').exists(), 'Queue already started'
    return {'recorded': now(), 'job_id': JOB.name, 'predecessor': {'id': PREDECESSOR.name, 'pid': PREDECESSOR_PID, 'creation_epoch_seconds': PREDECESSOR_CREATED}, 'max_workers': 16, 'commands': plans, 'input_sha256': {str(p): sha(p) for p in inputs}, 'runner_sha256': sha(Path(__file__)), 'resource_policy': 'Wait in this background queue until predecessor is completed rc0 and its wrapper exits. Then each member runs 3 serial-thread diagnostics concurrently followed by one16-worker image job; members are sequential. No production stop. BelowNormal, OMP/BLAS1.', 'emission_time_note': 'Requested times unchanged at100-1200ns. The high run ends at800ns, so requests beyond800 use the supplied script nearest-available snapshot behavior; no extrapolation or simulation extension.'}


def verify_inputs(frozen):
    assert sha(Path(__file__)) == frozen['runner_sha256'], 'Runner changed'
    for path, digest in frozen['input_sha256'].items():
        assert sha(path) == digest, 'Input changed while queued: ' + path


def predecessor_done():
    try:
        alive = abs(psutil.Process(PREDECESSOR_PID).create_time() - PREDECESSOR_CREATED) < 0.01
    except psutil.NoSuchProcess:
        alive = False
    if alive:
        return False
    exitfile = PREDECESSOR / 'exitcode.txt'
    if not exitfile.exists():
        raise RuntimeError('Predecessor wrapper disappeared without exitcode; no work started')
    assert exitfile.read_text().strip() == '0', 'Predecessor failed; no new work started'
    summary = json.loads((PREDECESSOR / 'postprocess_summary.json').read_text())
    assert summary['state'] == 'completed' and len(summary['commands']) == 4
    assert all(r['returncode'] == 0 for r in summary['commands'])
    return True


def execute(plan, env):
    name = '_'.join([plan['member'], plan['iteration'], plan['name']])
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
    current = preflight()
    if args.check_only:
        save(EVIDENCE, current)
        print(json.dumps({'job_id': JOB.name, 'commands': len(current['commands']), 'inputs': len(current['input_sha256']), 'predecessor': current['predecessor'], 'max_workers': 16}))
        return 0
    frozen = json.loads(EVIDENCE.read_text())
    assert current['input_sha256'] == frozen['input_sha256'] and current['runner_sha256'] == frozen['runner_sha256']
    JOB.mkdir(parents=True, exist_ok=True)
    psutil.Process().nice(psutil.BELOW_NORMAL_PRIORITY_CLASS)
    summary = {'job_id': JOB.name, 'started': now(), 'state': 'waiting_for_predecessor', 'preflight': current, 'commands': []}
    path = JOB / 'postprocess_summary.json'
    save(path, summary)
    env = dict(os.environ)
    env.update(PYTHONPATH=str(REPO / 'src'), PYTHONUTF8='1', PYTHONUNBUFFERED='1', OMP_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1')
    try:
        print('Waiting in background for', PREDECESSOR.name, flush=True)
        while not predecessor_done():
            time.sleep(30)
        summary.update(state='running', predecessor_completed_checked=now())
        save(path, summary)
        for member, iteration, _, _ in TARGETS:
            verify_inputs(frozen)
            plans = [r for r in frozen['commands'] if r['member'] == member and r['iteration'] == iteration]
            for plan in plans:
                assert not Path(plan['command'][plan['command'].index('--out') + 1]).exists(), 'Duplicate output'
            with ThreadPoolExecutor(max_workers=3) as pool:
                futures = [pool.submit(execute, plan, env) for plan in plans[:3]]
                for future in as_completed(futures):
                    summary['commands'].append(future.result())
                    save(path, summary)
            assert all(r['returncode'] == 0 for r in summary['commands']), 'Diagnostic failed'
            verify_inputs(frozen)
            summary['commands'].append(execute(plans[3], env))
            save(path, summary)
            assert summary['commands'][-1]['returncode'] == 0, 'Image stage failed'
        summary.update(state='completed', finished=now())
        save(path, summary)
        return 0
    except Exception as exc:
        summary.update(state='failed', finished=now(), error=repr(exc))
        save(path, summary)
        raise


if __name__ == '__main__':
    raise SystemExit(main())
