"""Validate and archive the requested completed sensitivity postprocessing."""
from pathlib import Path
import csv
import datetime
import hashlib
import json
import math
import shutil
import numpy as np

REPO = Path(__file__).resolve().parents[2]
SOURCE = Path('D:/sim_runs/T016r2/low_trap_G0/it0/image')
JOB = Path('D:/sim_runs/T016r2_post_trap_01')
OUT = REPO / 'results/T016r2/low_trap_G0/it0/image'
OUT.mkdir(parents=True, exist_ok=True)
assert (JOB / 'exitcode.txt').read_text().strip() == '0'
commands = json.loads((JOB / 'postprocess_summary.json').read_text())
assert len(commands) == 2 and all(r['returncode'] == 0 for r in commands)
assert all('D:/sim_runs/tables/v1b' in r['command'] for r in commands)
assert commands[1]['command'][-2:] == ['--workers', '8']
expected = {f'{f}_{mode}_{scenario}' for f in 'abc'
            for mode in ('thin', 'sa') for scenario in ('baseline', 'low', 'high')}
summary = json.loads((SOURCE / 'image_summary.json').read_text())
assert set(summary) == expected
assert {p.stem.removeprefix('metrics_') for p in SOURCE.glob('metrics_*.csv')} == expected
assert len(list(SOURCE.glob('frames_*.npz'))) == 6
records = []
def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
for source in sorted(SOURCE.iterdir()):
    assert source.suffix in ('.npz', '.csv', '.json'), source
    dest = OUT / source.name
    shutil.copy2(source, dest)
    assert sha(source) == sha(dest)
    item = dict(file=source.name, bytes=dest.stat().st_size, sha256=sha(dest))
    if source.suffix == '.npz':
        with np.load(dest) as arrays:
            item['arrays'] = {}
            for key in arrays.files:
                arr = arrays[key]
                assert np.isfinite(arr).all(), (source.name, key)
                item['arrays'][key] = dict(shape=list(arr.shape), finite=True)
    elif source.suffix == '.csv':
        with dest.open(newline='') as stream:
            rows = [{k: float(v) if v else math.nan for k,v in r.items()}
                    for r in csv.DictReader(stream)]
        assert len(rows) == 30
        times = [r['t_ns'] for r in rows]
        assert times == sorted(set(times)) and times[0] == 10 and times[-1] == 1230
        missing = {}
        for row in rows:
            for key,value in row.items():
                if not math.isfinite(value):
                    assert math.isnan(value)
                    allowed = (key in ('xc_um', 'zc_um', 'sigma_x2_um2', 'sigma_z2_um2')
                               and row['L'] == 0) or (key in ('thr_mean_gray','thr_top_um','thr_bottom_um')
                                                     and row['thr_area_mm2'] == 0)
                    assert allowed, (source.name, row['t_ns'], key)
                    missing.setdefault(key, []).append(row['t_ns'])
        item.update(rows=len(rows), first_t_ns=times[0], last_t_ns=times[-1],
                    undefined_for_empty_signal_or_region=missing)
    records.append(item)
diag = REPO / 'results/T016r2/diag/low_trap_G0_it0.csv'
diag.parent.mkdir(exist_ok=True)
source = JOB / 'low_trap_G0_it0_emission.csv'
shutil.copy2(source, diag)
assert sha(source) == sha(diag)
with diag.open(newline='') as stream:
    rows = [{k:float(v) for k,v in r.items()} for r in csv.DictReader(stream)]
assert [r['t_req_ns'] for r in rows] == [200,300,500,700,1000,1200]
assert all(math.isfinite(v) for r in rows for v in r.values())
for name in ('job.json','exitcode.txt','stdout.log','stderr.log','postprocess_summary.json'):
    shutil.copy2(JOB / name, OUT / ('job_' + name))
result = dict(collected=datetime.datetime.now().astimezone().isoformat(), returncode=0,
              scenario_count=len(summary), files=records, diagnostic_sha256=sha(diag),
              diagnostic_rows=len(rows), diagnostic_all_finite=True,
              P2_unavailable=[k for k,v in summary.items() if not math.isfinite(v['P2_dt10_ns'])])
(OUT / 'validation.json').write_text(json.dumps(result,indent=2)+'\n')
size = sum(p.stat().st_size for p in (REPO / 'results/T016r2').rglob('*') if p.is_file())
assert size <= 20*1024*1024, size
print(json.dumps(dict(total_results_bytes=size, scenario_count=len(summary),
    arrays=[r for r in records if 'arrays' in r],
    baseline={k:v for k,v in summary.items() if k.endswith('_baseline')},
    P2_unavailable=result['P2_unavailable'], diagnostic_at_700=rows[3]),indent=2))
