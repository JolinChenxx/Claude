"""Collect completed queue06b milestone; never rerun any scientific calculation."""
from datetime import datetime
from pathlib import Path
import csv
import hashlib
import json
import math
import shutil
import sys

import numpy as np
import pandas as pd
import yaml

REPO = Path(__file__).resolve().parents[3]
RUNTIME = Path('D:/sim_tools/postprocess-runtime-04')
BATCH = sys.argv[1] if len(sys.argv) > 1 else 'T026'
ITERATION = sys.argv[2] if len(sys.argv) > 2 else 'it0'
MEMBER_JOB = BATCH in ('T030', 'T031', 'T033', 'T034')
assert len(sys.argv) == 4 if MEMBER_JOB else len(sys.argv) <= 3
assert BATCH in ('T025', 'T026', 'T027', 'T028', 'T029') or MEMBER_JOB
assert ITERATION in ('it0', 'it1') or (MEMBER_JOB and ITERATION == 'it2')
QUEUE07 = BATCH == 'T027' or (BATCH, ITERATION) == ('T026', 'it1')
JOB = Path('D:/sim_runs') / ('T026_T027_post_07' if QUEUE07 else 'T023_T025_T026_post_06b')
RUNNER = 'run_priority_07.py' if QUEUE07 else 'run_priority_06.py'
MEMBER = 'low_thin_G1' if BATCH == 'T028' else 'low_thin_G0'
if MEMBER_JOB:
    MEMBER = sys.argv[3]
    series, size = MEMBER.split('_', 1)
    models = ('rp', 'a10n', 'a30n', 'a100n') if BATCH == 'T030' else ('v1s', 'v1rs')
    if BATCH in ('T033', 'T034'):
        specs = yaml.safe_load((REPO/f'configs/batches/{BATCH}.yaml').read_text(encoding='utf-8'))['numerics']['runs']
        assert MEMBER in {spec['id'] for spec in specs}
    else:
        assert size in models
    assert series in ('low', 'high')
    assert ITERATION == ('it1' if series == 'low' else 'it2')
SERIES = MEMBER.split('_')[0]
RAW = Path('D:/sim_runs') / BATCH / MEMBER / ITERATION
OUT = REPO / 'results' / BATCH / MEMBER / ITERATION
PRODUCTS = RAW
RUNNER_PATH = REPO / 'results/T021/postprocessing' / RUNNER
if BATCH in ('T028', 'T029'):
    JOB = JOB.with_name('T029_T028_post_08' if BATCH == 'T028' else 'T029_low_' + ITERATION + '_post_09')
    RUNNER_PATH = REPO / 'results/T029/postprocessing' / ('run_priority_08.py' if BATCH == 'T028' else 'run_parallel_low_09.py')
    if BATCH == 'T029':
        PRODUCTS = JOB / 'products'
if MEMBER_JOB:
    JOB = JOB.with_name(f'{BATCH}_{MEMBER}_post')
    RUNNER_PATH = REPO / 'results' / BATCH / 'postprocessing/run_member.py'


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def copy(source, target):
    before = source.stat()
    digest = sha(source)
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.exists():
        assert sha(target) == digest, str(target)
    else:
        shutil.copyfile(source, target)
    after = source.stat()
    assert (before.st_size, before.st_mtime_ns) == (after.st_size, after.st_mtime_ns)
    assert sha(source) == sha(target) == digest
    return {'source': str(source), 'archive': target.relative_to(REPO).as_posix(),
            'bytes': after.st_size, 'sha256': digest}


def main():
    assert not (OUT / 'priority_delivery.json').exists(), 'Milestone already collected'
    queue = json.loads((JOB / 'postprocess_summary.json').read_text())
    match = lambda r: (r['batch'], r['member'], r['iteration']) == (BATCH, MEMBER, ITERATION)
    milestone, = [r for r in queue['milestones_ready_for_collection'] if match(r)]
    run, = [r for r in queue['admissions'] if r['registry_row']['batch'] == BATCH and r['registry_row']['run'] == MEMBER and r['registry_row']['picard_it'] == ITERATION[2:]]
    commands = {r['name']: r for r in queue['commands'] if match(r)}
    commands.update({r['name']: r for r in run['reused_completed_commands']})
    assert set(commands) == {'image_v2', 'peak_lift', 'core_energy', 'shock_track', 'emission'}
    assert all(r['returncode'] == 0 for r in commands.values())
    for path, digest in run['input_sha256'].items():
        assert sha(path) == digest, path
    if BATCH == 'T029' or MEMBER_JOB:
        assert queue['state'] == 'completed' and (JOB / 'exitcode.txt').read_text().strip() == '0'
        preflight = json.loads((REPO / 'results/T021/postprocessing/deadline_05_preflight.json').read_text())
        assert preflight['runtime_commit'] == queue['runtime_commit']
    else:
        preflight = queue['preflight']
    for path, digest in preflight['input_sha256'].items():
        assert sha(path) == digest, path
    assert sha(RUNNER_PATH) == queue['runner_sha256']
    completion = json.loads((OUT / 'completion.json').read_text(encoding='utf-8'))
    assert completion['registry_row'] == run['registry_row']
    for name, item in completion['files'].items():
        assert sha(OUT / name) == sha(item['source']) == item['sha256']
    OUT.mkdir(parents=True, exist_ok=True)
    attributes = OUT / '.gitattributes'
    rules = attributes.read_text(encoding='utf-8') if attributes.exists() else ''
    for rule in ['image_v2/* -text\n', 'diagnostics/* -text\n', 'peak_lift.csv -text\n', 'peak_lift.log -text\n']:
        if rule not in rules:
            rules += rule
    attributes.write_text(rules, encoding='utf-8')
    meta = yaml.safe_load((RUNTIME / 'data/exp/meta.yaml').read_text(encoding='utf-8'))['sets'][SERIES]
    times = sorted({r['t_ns'] for r in meta['frames']})
    scenarios = {f'{b}_{m}_{d}' for b in 'abc' for m in ['thin', 'sa'] for d in ['baseline', 'low', 'high']}
    summary = json.loads((PRODUCTS / 'image_v2/image_summary.json').read_text())
    assert set(summary) == scenarios
    expected = {'image_summary.json'} | {f'frames_{b}_{m}.npz' for b in 'abc' for m in ['thin', 'sa']} | {f'metrics_{s}.csv' for s in scenarios}
    assert {p.name for p in (PRODUCTS / 'image_v2').iterdir()} == expected
    files = []
    raw_frames = []
    for source in sorted((PRODUCTS / 'image_v2').iterdir()):
        detail = {}
        if source.suffix == '.npz':
            with np.load(source) as arrays:
                assert set(arrays.files) == {'t_ns', 'radiance', 'um_per_px'}
                assert arrays['t_ns'].tolist() == times
                assert list(arrays['radiance'].shape) == [len(times), *meta['frame_px']]
                assert all(np.isfinite(arrays[k]).all() for k in arrays.files)
                detail['radiance_shape'] = list(arrays['radiance'].shape)
        elif source.suffix == '.csv':
            rows = list(csv.DictReader(source.open(newline='')))
            assert [float(r['t_ns']) for r in rows] == times
            missing = []
            for row in rows:
                for key, value in row.items():
                    number = float(value) if value else math.nan
                    if not math.isfinite(number):
                        assert math.isnan(number)
                        assert (key in ['xc_um', 'zc_um', 'sigma_x2_um2', 'sigma_z2_um2'] and float(row['L']) == 0) or (key in ['thr_mean_gray', 'thr_top_um', 'thr_bottom_um'] and float(row['thr_area_mm2']) == 0)
                        missing.append({'t_ns': float(row['t_ns']), 'field': key})
            detail['original_missing_values'] = missing
        if MEMBER_JOB and source.suffix == '.npz' and not source.name.startswith('frames_b_'):
            continue  # All six archives were checked; these cards deliver band b.
        if BATCH in ('T033', 'T034') and source.suffix == '.npz':
            raw_frames.append({'path': str(source), 'sha256': sha(source),
                               'shape': detail['radiance_shape'], 't_ns': times})
            continue
        files.append({**copy(source, OUT / 'image_v2' / source.name), **detail})
    files.append(copy(Path(commands['image_v2']['log']), OUT / 'image_v2/image_v2.log'))
    peak = pd.read_csv(PRODUCTS / 'peak_lift.csv')
    assert peak.t_ns.tolist() == times
    assert np.isfinite(peak[['t_ns', 'model_lift_thin_um', 'model_lift_sa_um']].to_numpy(dtype=float)).all()
    peak_missing = {k: peak.loc[peak[k].isna(), 't_ns'].tolist() for k in peak.columns if peak[k].isna().any()}
    exp = pd.read_csv(RUNTIME / 'results/surface/surface_rows.csv')
    exp = exp[exp.series == SERIES].groupby('t_ns', as_index=False).mean(numeric_only=True)
    for column, original in [('exp_lift_min_um', 'lower50_row'), ('exp_lift_max_um', 'lower10_row')]:
        assert np.allclose(peak[column], (exp[original] - exp.peak_row) * meta['um_per_px'], rtol=0, atol=.0500001, equal_nan=True)
    files.append(copy(PRODUCTS / 'peak_lift.csv', OUT / 'peak_lift.csv'))
    files.append(copy(Path(commands['peak_lift']['log']), OUT / 'peak_lift.log'))
    index = [line.split() for line in (RAW / 'dbl.out').read_text().splitlines() if line.strip()]
    snap_times = [float(r[1]) for r in index]
    tables = {}
    checks = {}
    for name, filename in [('core_energy', 'core_energy.csv'), ('shock_track', 'shock_track.csv'), ('emission', 'diag_emission.csv')]:
        source = PRODUCTS / 'diagnostics' / filename
        rows = list(csv.DictReader(source.open(newline='')))
        missing = {k: [i for i, r in enumerate(rows) if not r[k] or math.isnan(float(r[k]))] for k in rows[0]}
        assert all(not math.isinf(float(v)) for r in rows for v in r.values() if v)
        checks[name] = {'rows': len(rows), 'missing_row_indices_zero_based': {k: v for k, v in missing.items() if v}}
        tables[name] = rows
        files.append(copy(source, OUT / 'diagnostics' / filename))
        files.append(copy(Path(commands[name]['log']), OUT / 'diagnostics' / Path(commands[name]['log']).name))
    assert [float(r['t_ns']) for r in tables['shock_track']] == snap_times
    assert [float(r['t_ns']) for r in tables['core_energy']] == snap_times[::4]
    requested = [100, 150, 200, 250, 300, 400, 500, 600, 700, 800, 1000, 1200]
    assert [float(r['t_req_ns']) for r in tables['emission']] == requested
    assert not checks['emission']['missing_row_indices_zero_based']
    for row, target in zip(tables['emission'], requested):
        assert float(row['t_snap_ns']) == min(snap_times, key=lambda x: abs(x - target))
        for prefix in ['frac ', 'T ', 'Y ']:
            assert abs(sum(float(v) for k, v in row.items() if k.startswith(prefix)) - 1) < 1e-10
    evidence = {'recorded': datetime.now().astimezone().isoformat(), 'milestone': milestone,
                'commands': commands, 'files': files, 'diagnostic_checks': checks,
                'frame_count': len(times), 'peak_rows': len(peak), 'peak_original_missing_t_ns': peak_missing,
                'P2_unavailable': [k for k, v in summary.items() if not math.isfinite(v['P2_dt10_ns'])],
                'baseline_b': {k: v for k, v in summary.items() if k.startswith('b_') and k.endswith('baseline')},
                'input_sha256': run['input_sha256'],
                'policy': 'Full original image_v2, diagnostics, peak and logs. No calculation rerun, value adjustment or interpolation. Original no-core/no-front and experimental-bracket missing values and any unavailable P2 retained.'}
    if MEMBER_JOB:
        evidence['policy'] = 'Original band-b frames, every scenario metric, image summary, peak, diagnostics and logs archived; band-a/c frames remain in the raw directory.'
        evidence['raw_only_sha256'] = {str(p): sha(p) for p in sorted((RAW / 'image_v2').glob('frames_[ac]_*.npz'))}
    if BATCH in ('T033', 'T034'):
        assert len(raw_frames) == 2
        evidence['band_b_frame_manifest'] = raw_frames
        evidence['policy'] = 'Verified sensitivity frames remain in raw storage; paths, hashes, shapes and frame times are recorded here. Metrics, peak, diagnostics and logs are archived.'
    (OUT / 'priority_delivery.json').write_text(json.dumps(evidence, indent=2) + '\n', encoding='utf-8')
    total = sum(p.stat().st_size for p in (REPO / 'results' / BATCH).rglob('*') if p.is_file())
    assert total < 20 * 1024**2
    print(json.dumps({'frames': len(times), 'peak_rows': len(peak), 'diagnostics': checks, 'peak_original_missing_t_ns': peak_missing,
                      'P2_unavailable': len(evidence['P2_unavailable']), 'baseline_b': evidence['baseline_b'],
                      'image_seconds': commands['image_v2']['seconds'], 'peak_seconds': commands['peak_lift']['seconds'],
                      'card_bytes': total}, indent=2))


if __name__ == '__main__':
    main()
