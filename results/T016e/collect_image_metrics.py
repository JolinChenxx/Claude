"""Archive completed image metrics; validate and retain frame arrays in sim_runs."""
from pathlib import Path
import argparse
import ast
import csv
import datetime
import hashlib
import json
import math
import shutil
import numpy as np

ap = argparse.ArgumentParser()
ap.add_argument('member')
ap.add_argument('iteration')
ap.add_argument('job')
a = ap.parse_args()
repo = Path(__file__).resolve().parents[2]
run = Path('D:/sim_runs/T016e') / a.member / a.iteration
source = run / 'image'
job = Path('D:/sim_runs') / a.job
if (job / 'postprocess_summary.json').exists():
    commands = json.loads((job / 'postprocess_summary.json').read_text())
else:
    # Single-image wrappers record the exact command and per-run return code.
    lines = (job / 'stdout.log').read_text(encoding='utf-8-sig').splitlines()
    assert (job / 'exitcode.txt').read_text().strip() == '0'
    assert 'rc 0 ' + str(run) in lines
    commands = [dict(command=ast.literal_eval(line.removeprefix('Running: ')), returncode=0)
                for line in lines if line.startswith('Running: ')]
matches = [r for r in commands if 'lipsim.image' in r['command']
           and Path(r['command'][r['command'].index('lipsim.image') + 1]) == run]
assert len(matches) == 1 and matches[0]['returncode'] == 0
assert 'D:/sim_runs/tables/v1c' in matches[0]['command']
assert matches[0]['command'][-2:] == ['--workers', '8']
out = repo / 'results/T016e' / a.member / a.iteration / 'image'
out.mkdir(parents=True, exist_ok=True)
expected = {f'{f}_{mode}_{scenario}' for f in 'abc'
            for mode in ('thin', 'sa') for scenario in ('baseline', 'low', 'high')}
summary = json.loads((source / 'image_summary.json').read_text())
assert set(summary) == expected
assert {p.stem.removeprefix('metrics_') for p in source.glob('metrics_*.csv')} == expected
frames = list(source.glob('frames_*.npz'))
assert len(frames) == 6
records = []
common_times = None
def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()
for p in sorted(source.iterdir()):
    assert p.suffix in ('.npz', '.csv', '.json')
    record = dict(file=p.name, bytes=p.stat().st_size, sha256=sha(p))
    if p.suffix == '.npz':
        record['raw_path'] = str(p)
        record['arrays'] = {}
        with np.load(p) as arrays:
            for key in arrays.files:
                arr = arrays[key]
                assert np.isfinite(arr).all(), (p, key)
                record['arrays'][key] = dict(shape=list(arr.shape), finite=True)
    else:
        dest = out / p.name
        shutil.copy2(p, dest)
        assert sha(p) == sha(dest)
        if p.suffix == '.csv':
            with p.open(newline='') as f:
                rows = [{k:float(v) if v else math.nan for k,v in row.items()}
                        for row in csv.DictReader(f)]
            times = [r['t_ns'] for r in rows]
            assert times and times == sorted(set(times))
            if common_times is None:
                common_times = times
            assert times == common_times
            for row in rows:
                for key,value in row.items():
                    if not math.isfinite(value):
                        assert math.isnan(value)
                        assert (key in ('xc_um','zc_um','sigma_x2_um2','sigma_z2_um2')
                                and row['L'] == 0) or (key in ('thr_mean_gray','thr_top_um','thr_bottom_um')
                                and row['thr_area_mm2'] == 0), (p, row['t_ns'], key)
            record.update(rows=len(rows), first_t_ns=times[0], last_t_ns=times[-1])
    records.append(record)
result = dict(collected=datetime.datetime.now().astimezone().isoformat(),
              completed_command=matches[0], files=records,
              frame_policy='Validated frame arrays retained in sim_runs to respect 20 MB/card limit',
              P2_unavailable=[k for k,v in summary.items() if not math.isfinite(v['P2_dt10_ns'])])
(out / 'validation.json').write_text(json.dumps(result,indent=2)+'\n')
size = sum(p.stat().st_size for p in (repo / 'results/T016e').rglob('*') if p.is_file())
assert size <= 20*1024*1024, size
print(json.dumps(dict(total_results_bytes=size, rows=len(common_times),
                     first_t_ns=common_times[0], last_t_ns=common_times[-1],
                     baseline={k:v for k,v in summary.items() if k.endswith('_baseline')},
                     P2_unavailable=result['P2_unavailable']), indent=2))
