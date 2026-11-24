"""Collect finished stages of queue 03 without waiting for its active stage."""
from pathlib import Path
from datetime import datetime
import csv
import hashlib
import json
import math
import shutil
import subprocess
import numpy as np
import pandas as pd
import yaml

REPO = Path(__file__).resolve().parents[3]
JOB = Path('D:/sim_runs/T021_T023_post_03')


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')


def copy_exact(source, target):
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
    return {'source': str(source), 'archive': target.relative_to(REPO).as_posix(), 'bytes': after.st_size, 'sha256': digest}


def main():
    summary = json.loads((JOB / 'postprocess_summary.json').read_text())
    evidence = REPO / 'results/T023/postprocessing/queue03_collection_provenance.json'
    drift = []
    for name, expected in summary['preflight']['input_sha256'].items():
        p = Path(name)
        actual = sha(p)
        if actual != expected:
            rel = p.relative_to(REPO).as_posix()
            assert rel in ['data/params/registry.yaml', 'src/lipsim/batch.py'], rel
            original = subprocess.check_output(['git', 'show', 'cc0380e:' + rel], cwd=REPO)
            assert hashlib.sha256(original).hexdigest() == expected
            frozen = REPO / 'results/T023/postprocessing/queue03_frozen_original' / rel
            frozen.parent.mkdir(parents=True, exist_ok=True)
            frozen.write_bytes(original)
            drift.append({'file': rel, 'frozen_sha256': expected, 'current_sha256': actual,
                          'original_archive': frozen.relative_to(REPO).as_posix()})
    assert len(drift) == 2
    save(evidence, {'recorded': datetime.now().astimezone().isoformat(), 'drift': drift,
                    'unchanged_input_count': len(summary['preflight']['input_sha256']) - len(drift),
                    'cause': 'Cloud T024 commit d3305dc added lotz_a_cu and batch ini parameters; observed after pull at 14:38 while high-it1 imaging was active. Completed high-it0 outputs and high-it1 diagnostics finished before this pull. Active runner remains unmodified and will fail its next input-hash check unless retired at the authorized completed-run boundary.',
                    'completed_commands': summary['commands']})
    batch, member = 'T023', 'high_thin_G0'
    out = REPO / 'results' / batch / member / 'it0'
    raw = Path('D:/sim_runs') / batch / member / 'it0'
    commands = {r['name']: r for r in summary['commands'] if (r['batch'], r['member'], r['iteration']) == (batch, member, 'it0')}
    assert commands['image_v2']['returncode'] == commands['peak_lift']['returncode'] == 0
    meta = yaml.safe_load((REPO / 'data/exp/meta.yaml').read_text(encoding='utf-8'))['sets']['high']
    times = sorted({r['t_ns'] for r in meta['frames']})
    image_summary = json.loads((raw / 'image_v2/image_summary.json').read_text())
    scenarios = {f'{b}_{m}_{f}' for b in 'abc' for m in ['thin', 'sa'] for f in ['baseline', 'low', 'high']}
    assert set(image_summary) == scenarios
    assert len(list((raw / 'image_v2').glob('frames_*.npz'))) == 6
    assert {p.stem.removeprefix('metrics_') for p in (raw / 'image_v2').glob('metrics_*.csv')} == scenarios
    files = []
    for source in sorted((raw / 'image_v2').iterdir()):
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
        else:
            assert source.name == 'image_summary.json'
        files.append({**copy_exact(source, out / 'image_v2' / source.name), **detail})
    files.append(copy_exact(Path(commands['image_v2']['log']), out / 'image_v2/image_v2.log'))
    files.append(copy_exact(Path(commands['peak_lift']['log']), out / 'peak_lift.log'))
    peak = pd.read_csv(out / 'peak_lift.csv')
    assert peak.t_ns.tolist() == times and np.isfinite(peak.to_numpy(dtype=float)).all()
    exp = pd.read_csv(REPO / 'results/surface/surface_rows.csv')
    exp = exp[exp.series == 'high'].groupby('t_ns', as_index=False).mean(numeric_only=True)
    for col, source in [('exp_lift_min_um', 'lower50_row'), ('exp_lift_max_um', 'lower10_row')]:
        assert np.allclose(peak[col], (exp[source] - exp.peak_row) * 10.724, rtol=0, atol=0.0500001, equal_nan=True)
    save(out / 'image_v2/validation.json', {'recorded': datetime.now().astimezone().isoformat(), 'commands': [commands['image_v2'], commands['peak_lift']], 'files': files, 'peak_sha256': sha(out / 'peak_lift.csv'), 'frame_count': len(times), 'scenario_count': len(scenarios), 'P2_unavailable': [k for k,v in image_summary.items() if not math.isfinite(v['P2_dt10_ns'])], 'policy': 'Original full image directory and peak_lift preserved; no recalculation or filling. Provenance drift is recorded separately; these stages completed before the update.'})
    raw = Path('D:/sim_runs') / batch / member / 'it1'
    out = REPO / 'results' / batch / member / 'it1/diagnostics'
    commands = {r['name']: r for r in summary['commands'] if (r['batch'],r['member'],r['iteration']) == (batch,member,'it1')}
    index = [line.split() for line in (raw / 'dbl.out').read_text().splitlines() if line.strip()]
    snaps = [float(r[1]) for r in index]
    assert len(snaps) == 321 and snaps[0] == 0 and snaps[-1] == 800
    files, checks = [], {}
    for name, filename in [('core_energy','core_energy.csv'), ('emission','diag_emission.csv'), ('shock_track','shock_track.csv')]:
        assert commands[name]['returncode'] == 0
        source = raw / 'diagnostics' / filename
        rows = list(csv.DictReader(source.open(newline='')))
        missing = {k:[i for i,r in enumerate(rows) if not r[k] or math.isnan(float(r[k]))] for k in rows[0]}
        assert all(not math.isinf(float(v)) for row in rows for v in row.values() if v)
        if name == 'core_energy':
            assert [float(r['t_ns']) for r in rows] == snaps[::4]
            for k in ['E_int','E_kin','T_mw','P_rad','V']: assert missing[k] == [0,1,2]
            for k in ['dEdt','P_other']: assert missing[k] == [0,1,2,3]
        elif name == 'shock_track':
            assert [float(r['t_ns']) for r in rows] == snaps
            assert missing['R_axis_um'] == missing['R_surf_um'] == list(range(6))
        else:
            requested = [100,150,200,250,300,400,500,600,700,800,1000,1200]
            assert [float(r['t_req_ns']) for r in rows] == requested and not any(missing.values())
            for row,t in zip(rows,requested):
                assert float(row['t_snap_ns']) == min(snaps,key=lambda s:abs(s-t))
                for prefix in ['frac ','T ','Y ']: assert abs(sum(float(v) for k,v in row.items() if k.startswith(prefix))-1)<1e-10
        checks[name] = {'rows':len(rows),'missing_row_indices':{k:v for k,v in missing.items() if v}}
        files += [copy_exact(source,out/filename),copy_exact(Path(commands[name]['log']),out/Path(commands[name]['log']).name)]
    save(out/'validation.json', {'recorded':datetime.now().astimezone().isoformat(),'commands':[commands[k] for k in checks],'files':files,'checks':checks,'policy':'Original missing values retained. High requests1000/1200ns reuse final800ns snapshot. Diagnostics finished before the T024 source update; provenance drift recorded separately.'})
    total = sum(p.stat().st_size for p in (REPO/'results/T023').rglob('*') if p.is_file())
    assert total < 20*1024**2
    print(json.dumps({'high_it0_image_seconds':1746.1480250999994,'high_it1_diagnostics':checks,'card_archive_bytes':total,'drift_files':[d['file'] for d in drift]}))


if __name__ == '__main__':
    main()
