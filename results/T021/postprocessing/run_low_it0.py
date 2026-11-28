"""T021 low it0 diagnostics and image_v2; launch only through job.ps1."""
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime
from pathlib import Path
import argparse
import hashlib
import json
import os
import subprocess
import time

REPO = Path(__file__).resolve().parents[3]
PYTHON = Path('D:/sim_tools/envs/lipsim/python.exe')
RUN = Path('D:/sim_runs/T021/low_thin_G0/it0')
TABLES = Path('D:/sim_runs/tables/v1c')
JOB = Path('D:/sim_runs/T021_post_low_it0_01')
EVIDENCE = REPO / 'results/T021/postprocessing/low_it0_preflight.json'


def now():
    return datetime.now().astimezone().isoformat()


def sha(path):
    digest = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            digest.update(block)
    return digest.hexdigest()


def commands():
    diag = RUN / 'diagnostics'
    return [
        ('emission', [str(PYTHON), str(REPO / 'scripts/diag_emission.py'), str(RUN), '--tables', str(TABLES), '--times', '100', '150', '200', '250', '300', '400', '500', '600', '700', '800', '1000', '1200', '--filter', '510', '12', '--out', str(diag / 'diag_emission.csv')]),
        ('core_energy', [str(PYTHON), str(REPO / 'scripts/core_energy.py'), str(RUN), '--tables', str(TABLES), '--nec', 'thin', '--out', str(diag / 'core_energy.csv')]),
        ('shock_track', [str(PYTHON), str(REPO / 'scripts/shock_track.py'), str(RUN), '--out', str(diag / 'shock_track.csv')]),
        ('image_v2', [str(PYTHON), '-m', 'lipsim.image', str(RUN), '--series', 'low', '--tables', str(TABLES), '--out', str(RUN / 'image_v2'), '--workers', '16', '--scenarios', str(REPO / 'configs/imaging/v2.yaml')]),
    ]


def preflight():
    completion = json.loads((REPO / 'results/T021/low_thin_G0/it0/completion.json').read_text(encoding='utf-8'))
    assert completion['registry_row']['returncode'] == '0'
    assert completion['final_budget']['t_ns'] == 1300
    assert sha(RUN / 'budget.csv') == completion['files']['budget.csv']['sha256']
    inputs = [PYTHON, RUN / 'budget.csv', RUN / 'pluto.ini', RUN / 'grid.out', RUN / 'dbl.out', RUN / 'source/source_meta.json', TABLES / 'eos.bin', TABLES / 'opt.bin', REPO / 'configs/imaging/v2.yaml']
    inputs += sorted((REPO / 'src/lipsim').rglob('*.py'))
    inputs += [REPO / ('scripts/' + name + '.py') for name in ['diag_emission', 'core_energy', 'shock_track']]
    for path in inputs:
        assert path.is_file(), str(path)
    plan = commands()
    for _, command in plan:
        output = Path(command[command.index('--out') + 1])
        assert not output.exists(), 'Output already exists: ' + str(output)
    result = {'recorded': now(), 'job_id': JOB.name, 'run': str(RUN), 'commands': [{'name': n, 'command': c} for n, c in plan], 'input_sha256': {str(p): sha(p) for p in inputs}, 'runner_sha256': sha(Path(__file__)), 'max_postprocessing_workers': 16, 'diagnostics_parallelism': 3, 'priority': 'BelowNormal', 'resource_policy': '3 serial-thread diagnostic processes, then 16 image workers; compatible with the T023 hyperthread allowance; never stop production.'}
    return result


def save(path, value):
    tmp = path.with_suffix('.tmp')
    tmp.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')
    tmp.replace(path)


def execute(name, command, env):
    row = {'name': name, 'command': command, 'started': now(), 'log': str(JOB / (name + '.log'))}
    start = time.perf_counter()
    print('start', name, flush=True)
    with Path(row['log']).open('wb') as stream:
        row['returncode'] = subprocess.run(command, cwd=REPO, env=env, stdout=stream, stderr=subprocess.STDOUT).returncode
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
        print(json.dumps({'job_id': JOB.name, 'commands': current['commands'], 'frozen_inputs': len(current['input_sha256']), 'max_workers': 16}))
        return 0
    frozen = json.loads(EVIDENCE.read_text(encoding='utf-8'))
    assert current['input_sha256'] == frozen['input_sha256'], 'Inputs changed after preflight'
    assert current['runner_sha256'] == frozen['runner_sha256'], 'Runner changed after preflight'
    JOB.mkdir(parents=True, exist_ok=True)
    summary_path = JOB / 'postprocess_summary.json'
    assert not summary_path.exists(), 'Existing queue must be inspected before retry'
    import psutil
    psutil.Process().nice(psutil.BELOW_NORMAL_PRIORITY_CLASS)
    env = dict(os.environ)
    env.update(PYTHONPATH=str(REPO / 'src'), PYTHONUTF8='1', PYTHONUNBUFFERED='1', OMP_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1')
    summary = {'job_id': JOB.name, 'started': now(), 'state': 'running', 'preflight': current, 'commands': []}
    save(summary_path, summary)
    plan = commands()
    with ThreadPoolExecutor(max_workers=3) as pool:
        futures = [pool.submit(execute, name, command, env) for name, command in plan[:3]]
        for future in as_completed(futures):
            summary['commands'].append(future.result())
            save(summary_path, summary)
    if any(row['returncode'] != 0 for row in summary['commands']):
        summary.update(state='failed', finished=now())
        save(summary_path, summary)
        return 1
    for path, digest in frozen['input_sha256'].items():
        assert sha(path) == digest, 'Input changed before imaging: ' + path
    summary['commands'].append(execute(*plan[3], env))
    code = summary['commands'][-1]['returncode']
    summary.update(state='completed' if code == 0 else 'failed', finished=now())
    save(summary_path, summary)
    return code


if __name__ == '__main__':
    raise SystemExit(main())
