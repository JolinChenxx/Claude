"""Archive one completed T019 component; preserve local frames and provenance."""
from pathlib import Path
import argparse
import csv
import datetime as dt
import hashlib
import json
import math
import shutil

import numpy as np

REPO = Path(__file__).resolve().parents[2]
ROOT = REPO / 'results/T019'
JOB = Path('D:/sim_runs/T019_split_01')


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('member', choices=('high_thin_G0', 'low_thin_G0'))
    ap.add_argument('component', choices=('continuum', 'lines'))
    args = ap.parse_args()
    plan_path = ROOT / 'plan.json'
    plan = json.loads(plan_path.read_text(encoding='utf-8'))
    metadata = json.loads((JOB/'job.json').read_text(encoding='utf-8-sig'))
    status = json.loads((JOB/'postprocess_summary.json').read_text(encoding='utf-8'))
    runner = ROOT / 'run_split.py'
    assert metadata['id'] == plan['job_id'] == status['job_id'] == JOB.name
    assert Path(metadata['dir']) == JOB
    assert metadata['cmd'] == f'{Path("D:/sim_tools/envs/lipsim/python.exe")} {runner}'
    assert status['runner_sha256'] == plan['runner_sha256'] == sha(runner)
    assert status['plan_sha256'] == sha(plan_path)
    for row in plan['frozen_files']:
        assert sha(row['path']) == row['sha256'], row['path']
    source_input, = [r for r in plan['inputs'] if r['member'] == args.member]
    iteration = source_input['iteration']
    name = f'{args.member}_{iteration}_{args.component}'
    planned, = [r for r in plan['commands'] if r['name'] == name]
    evidence, = [r for r in status['commands'] if r['name'] == name]
    assert evidence.get('returncode') == 0 and evidence.get('ended')
    assert evidence['execution'] == 'computed_by_owner'
    assert evidence['command'] == planned['command']
    for row in planned['snapshot_files']:
        stat = Path(row['path']).stat()
        assert (stat.st_size, stat.st_mtime_ns) == (row['bytes'], row['mtime_ns']), row['path']
    command = evidence['command']
    outname = 'image_v2_' + args.component
    source = Path(source_input['run']) / outname
    assert Path(command[command.index('--out')+1]) == source
    assert command[command.index('--series')+1] == source_input['series']
    assert command[command.index('--workers')+1] == '16'
    destination = ROOT / args.member / outname
    assert not destination.exists(), 'Archive already exists; inspect rather than overwrite it.'
    expected = {f'{f}_{mode}_{blur}' for f in 'abc' for mode in ('thin', 'sa')
                for blur in ('baseline', 'low', 'high')}
    expected_frames = {f'frames_{f}_{mode}.npz' for f in 'abc' for mode in ('thin', 'sa')}
    expected_files = {f'metrics_{key}.csv' for key in expected} | expected_frames | {'image_summary.json'}
    assert {p.name for p in source.iterdir()} == expected_files
    summary = json.loads((source/'image_summary.json').read_text())
    assert set(summary) == expected
    expected_times = source_input['baseline']['thin']['times']
    assert source_input['baseline']['sa']['times'] == expected_times
    files = []
    # Validate every file before creating the archive directory.
    for path in sorted(source.iterdir()):
        record = {'file': path.name, 'bytes': path.stat().st_size, 'sha256': sha(path)}
        if path.suffix == '.npz':
            record.update(raw_path=str(path), arrays={})
            with np.load(path, allow_pickle=False) as arrays:
                for key in arrays.files:
                    array = arrays[key]
                    assert np.isfinite(array).all(), (path, key)
                    record['arrays'][key] = {'shape': list(array.shape), 'finite': True}
        elif path.suffix == '.csv':
            with path.open(newline='') as stream:
                rows = [{k: float(v) if v else math.nan for k, v in row.items()}
                        for row in csv.DictReader(stream)]
            times = [r['t_ns'] for r in rows]
            assert times == expected_times == sorted(set(times)), path
            for row in rows:
                assert math.isfinite(row['L_radiance']), (path, row['t_ns'])
                for key, value in row.items():
                    if not math.isfinite(value):
                        assert math.isnan(value)
                        assert (key in ('xc_um', 'zc_um', 'sigma_x2_um2', 'sigma_z2_um2')
                                and row['L'] == 0) or (key in ('thr_mean_gray', 'thr_top_um', 'thr_bottom_um')
                                and row['thr_area_mm2'] == 0), (path, row['t_ns'], key)
            record.update(rows=len(rows), first_t_ns=times[0], last_t_ns=times[-1])
        files.append(record)
    destination.mkdir(parents=True)
    for row in files:
        if not row['file'].endswith('.npz'):
            shutil.copy2(source/row['file'], destination/row['file'])
            assert sha(destination/row['file']) == sha(source/row['file']) == row['sha256']
    validation = {'collected': dt.datetime.now().astimezone().isoformat(),
                  'member': args.member, 'iteration': iteration, 'component': args.component,
                  'diagnostic_only': True, 'self_absorbed_additivity_required': False,
                  'job': metadata, 'completion_evidence': evidence,
                  'runner_sha256': status['runner_sha256'], 'plan_sha256': status['plan_sha256'],
                  'files': files, 'frame_policy': 'Six validated NPZ archives remain local under sim_runs.',
                  'P2_unavailable': [k for k, v in summary.items() if not math.isfinite(v['P2_dt10_ns'])],
                  'negative_P1': {k: v['P1_tau_ns'] for k, v in summary.items() if v['P1_tau_ns'] < 0},
                  'nonfinite_P1': [k for k, v in summary.items() if not math.isfinite(v['P1_tau_ns'])]}
    target = destination/'validation.json'
    target.write_text(json.dumps(validation, indent=2)+'\n', encoding='utf-8')
    assert json.loads(target.read_text()) == validation
    size = sum(p.stat().st_size for p in ROOT.rglob('*') if p.is_file())
    assert size <= 20*1024*1024, size
    print(json.dumps({'member': args.member, 'component': args.component, 'metrics': 18,
                      'rows': len(expected_times), 'local_finite_frame_archives': 6,
                      'P2_unavailable': len(validation['P2_unavailable']),
                      'negative_P1': validation['negative_P1'],
                      'nonfinite_P1': validation['nonfinite_P1'],
                      'baseline': {k: v for k, v in summary.items() if k.endswith('_baseline')},
                      'total_results_bytes': size}, indent=2))


if __name__ == '__main__':
    main()
