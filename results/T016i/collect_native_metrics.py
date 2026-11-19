"""Archive completed native-grid metrics using per-run rc=0 evidence."""
from pathlib import Path
import argparse
import csv
import datetime
import hashlib
import json
import math
import shutil
import numpy as np

ap = argparse.ArgumentParser()
ap.add_argument('batch')
ap.add_argument('member')
ap.add_argument('iteration')
ap.add_argument('job')
ap.add_argument('--outname', choices=('image_v1', 'image_v2'), default='image_v1')
a = ap.parse_args()
repo = Path(__file__).resolve().parents[2]
run = Path('D:/sim_runs') / a.batch / a.member / a.iteration
source = run / a.outname
job = Path('D:/sim_runs') / a.job
log = (job / 'stdout.log').read_text(encoding='utf-8-sig')
evidence = 'rc 0 ' + str(run)
if evidence not in log.splitlines():
    # A suspended dispatcher cannot print rc until it resumes. Read the
    # exited child's Windows process object, which it still holds open.
    import ctypes as ct
    from ctypes import wintypes as wt
    holds = json.loads((Path('D:/sim_runs/T018') / 'imaging_dispatch_holds.json').read_text())
    hold, = [h for h in holds if h['job'] == a.job]
    cmd = hold['active_command']
    assert Path(cmd[cmd.index('lipsim.image') + 1]) == run
    assert Path(cmd[cmd.index('--out') + 1]) == source
    k = ct.WinDLL('kernel32', use_last_error=True)
    k.OpenProcess.restype = wt.HANDLE
    handle = wt.HANDLE(k.OpenProcess(0x1000, False, hold['active_image_pid']))
    if not handle:
        raise ct.WinError(ct.get_last_error())
    try:
        times = [wt.FILETIME() for _ in range(4)]
        rc = wt.DWORD()
        assert k.GetProcessTimes(handle, *(ct.byref(t) for t in times))
        created = ((times[0].dwHighDateTime << 32) | times[0].dwLowDateTime) / 1e7 - 11644473600
        assert abs(created - hold['active_image_created']) < 0.00001
        assert k.GetExitCodeProcess(handle, ct.byref(rc)) and rc.value == 0
        evidence = dict(method='GetExitCodeProcess', pid=hold['active_image_pid'],
                        created=created, command=cmd, exit_code=rc.value)
    finally:
        k.CloseHandle(handle)
metadata = json.loads((job / 'job.json').read_text(encoding='utf-8-sig'))
expected_runner = 'run_native_imaging.py' if a.outname == 'image_v1' else 'run_reference_imaging.py'
assert expected_runner in metadata['cmd']
if a.outname == 'image_v2':
    stage = json.loads((job / 'stage.json').read_text())
    assert stage['job'] == a.job and stage['label'] == 'reference imaging v2'
    assert stage['commit'].startswith(metadata['commit'])
out = repo / 'results' / a.batch / a.member / a.iteration / a.outname
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
              completion_evidence=evidence, job=metadata, files=records,
              frame_policy='Validated frame arrays retained in sim_runs to respect 20 MB/card limit',
              P2_unavailable=[k for k,v in summary.items() if not math.isfinite(v['P2_dt10_ns'])])
if a.outname == 'image_v2':
    result['reference_stage'] = stage
(out / 'validation.json').write_text(json.dumps(result,indent=2)+'\n')
size = sum(p.stat().st_size for p in (repo / 'results' / a.batch).rglob('*') if p.is_file())
assert size <= 20*1024*1024, size
print(json.dumps(dict(total_results_bytes=size, rows=len(common_times),
                     first_t_ns=common_times[0], last_t_ns=common_times[-1],
                     baseline={k:v for k,v in summary.items() if k.endswith('_baseline')},
                     P2_unavailable=result['P2_unavailable']), indent=2))
