"""Validate completed native image arrays and archive compact metrics only."""
from pathlib import Path
from datetime import datetime
import csv
import hashlib
import json
import math
import shutil
import numpy as np
import yaml

REPO = Path(__file__).resolve().parents[3]
SOURCE = Path('D:/sim_runs/T021/low_thin_G0/it0/image_v2')
JOB = Path('D:/sim_runs/T021_post_low_it0_01')
OUT = REPO / 'results/T021/low_thin_G0/it0/image_v2'


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    job = json.loads((JOB / 'postprocess_summary.json').read_text())
    assert job['state'] == 'completed' and len(job['commands']) == 4
    assert (JOB / 'exitcode.txt').read_text().strip() == '0'
    assert all(r['returncode'] == 0 for r in job['commands'])
    expected = {f'{band}_{mode}_{focus}' for band in 'abc' for mode in ['thin', 'sa'] for focus in ['baseline', 'low', 'high']}
    summary = json.loads((SOURCE / 'image_summary.json').read_text())
    assert set(summary) == expected
    assert {p.stem.removeprefix('metrics_') for p in SOURCE.glob('metrics_*.csv')} == expected
    meta = yaml.safe_load((REPO / 'data/exp/meta.yaml').read_text(encoding='utf-8'))['sets']['low']
    times = sorted({r['t_ns'] for r in meta['frames']})
    frames = sorted(SOURCE.glob('frames_*.npz'))
    assert len(frames) == 6
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / '.gitattributes').write_text('*.csv -text\n*.json -text\n*.log -text\n', encoding='utf-8')
    records = []
    for source in sorted(SOURCE.iterdir()):
        record = {'file': source.name, 'bytes': source.stat().st_size, 'sha256': sha(source)}
        if source.suffix == '.npz':
            with np.load(source) as arrays:
                assert set(arrays.files) == {'t_ns', 'radiance', 'um_per_px'}
                assert list(arrays['t_ns']) == times
                assert list(arrays['radiance'].shape) == [len(times), *meta['frame_px']]
                assert all(np.isfinite(arrays[k]).all() for k in arrays.files)
                record['radiance_shape'] = list(arrays['radiance'].shape)
                record['finite'] = True
            record['raw_path'] = str(source)
        else:
            assert source.suffix in {'.csv', '.json'}
            if source.suffix == '.csv':
                with source.open(newline='') as stream:
                    rows = [{k: float(v) if v else math.nan for k, v in r.items()} for r in csv.DictReader(stream)]
                assert [r['t_ns'] for r in rows] == times
                missing = []
                for row in rows:
                    for key, value in row.items():
                        if not math.isfinite(value):
                            assert math.isnan(value)
                            assert (key in ['xc_um', 'zc_um', 'sigma_x2_um2', 'sigma_z2_um2'] and row['L'] == 0) or (key in ['thr_mean_gray', 'thr_top_um', 'thr_bottom_um'] and row['thr_area_mm2'] == 0)
                            missing.append({'t_ns': row['t_ns'], 'field': key})
                record.update(rows=len(rows), allowed_missing_values=missing)
            target = OUT / source.name
            assert not target.exists(), 'Already archived: ' + str(target)
            shutil.copyfile(source, target)
            assert sha(target) == record['sha256']
        records.append(record)
    for name in ['image_v2.log', 'postprocess_summary.json']:
        source, target = JOB / name, OUT / name
        shutil.copyfile(source, target)
        assert sha(source) == sha(target)
        records.append({'file': name, 'bytes': target.stat().st_size, 'sha256': sha(target)})
    evidence = {'recorded': datetime.now().astimezone().isoformat(), 'job_id': JOB.name, 'command_evidence': next(r for r in job['commands'] if r['name'] == 'image_v2'), 'expected_times_ns': times, 'scenario_count': len(expected), 'frame_count_per_scenario': len(times), 'files': records, 'summary_P2_unavailable': [k for k, v in summary.items() if not math.isfinite(v['P2_dt10_ns'])], 'policy': 'Original missing metrics and NaN decay summaries retained, not fabricated; all six radiance arrays finite. Full NPZ frames retained locally and hashed; compact metrics and summary archived.'}
    (OUT / 'validation.json').write_text(json.dumps(evidence, indent=2) + '\n', encoding='utf-8')
    assert sum(p.stat().st_size for p in (REPO / 'results/T021').rglob('*') if p.is_file()) < 20 * 1024**2
    print(json.dumps({'recorded': evidence['recorded'], 'scenario_count': len(expected), 'frames_per_scenario': len(times), 'shape': meta['frame_px'], 'P2_unavailable': evidence['summary_P2_unavailable'], 'baseline_b': {k: v for k, v in summary.items() if k.startswith('b_') and k.endswith('_baseline')}}, indent=2))


if __name__ == '__main__':
    main()
