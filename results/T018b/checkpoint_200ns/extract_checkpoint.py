from pathlib import Path
import csv, datetime, hashlib, io, json, mmap, re, shutil
import numpy as np
import psutil

repo = Path('D:/Codex/2026-11-11/wo/outputs/Claude')
root = Path('D:/sim_runs')
out = repo / 'results/T018b/checkpoint_200ns'
out.mkdir(parents=True, exist_ok=True)
now = datetime.datetime.now().astimezone()
new = root / 'T018b/low_thin_G0/it0'
old = root / 'T018/low_thin_G0/it0'
budgets = {}
hashes = {}
for label, directory in [('v1h', old), ('v1i', new)]:
    raw = (directory / 'budget.csv').read_bytes()
    raw = raw[:raw.rfind(b'\n') + 1]
    target = out / f'budget_{label}.csv'
    target.write_bytes(raw)
    hashes[target.name] = hashlib.sha256(raw).hexdigest()
    rows = list(csv.DictReader(io.StringIO(raw.decode('utf-8-sig'))))
    columns = list(rows[0])
    budgets[label] = {key: np.array([float(row[key]) for row in rows]) for key in columns}
    assert all(np.isfinite(values).all() for values in budgets[label].values())
    assert np.all(np.diff(budgets[label]['t_ns']) > 0)
    assert budgets[label]['t_ns'][-1] > 200

steps = []
diagnostics = []
source = new / 'pluto.0.log'
with source.open('rb') as handle:
    size = source.stat().st_size
    with mmap.mmap(handle.fileno(), size, access=mmap.ACCESS_READ) as mapped:
        pos = 0
        while True:
            pos = mapped.find(b'step:', pos)
            if pos < 0:
                break
            end = mapped.find(b'\n', pos)
            if end < 0:
                break
            line = mapped[pos:end].decode('utf-8', errors='replace').strip()
            match = re.fullmatch(r'step:(\d+); t = ([^;]+); dt = ([^;]+);.*', line)
            if match:
                mach_start = mapped.find(b'[Mach =', end)
                mach_end = mapped.find(b'\n', mach_start) if mach_start >= 0 else -1
                mach = mapped[mach_start:mach_end].decode().strip() if mach_end >= 0 else None
                steps.append({'offset': pos, 'line': line, 't_ns': float(match[2]), 'dt_ns': float(match[3]), 'mach_offset': mach_start, 'mach': mach})
            pos = end + 1
        pos = size
        while len(diagnostics) < 3:
            pos = mapped.rfind(b'lip dt-limit:', 0, pos)
            if pos < 0:
                break
            start = mapped.rfind(b'\n', 0, pos) + 1
            end = mapped.find(b'\n', pos)
            if end >= 0:
                diagnostics.append({'offset': start, 'line': mapped[start:end].decode().strip()})
with source.open('rb') as handle:
    for record in steps + diagnostics:
        handle.seek(record['offset'])
        assert handle.readline().decode().strip() == record['line']
        if record.get('mach'):
            handle.seek(record['mach_offset'])
            assert handle.readline().decode().strip() == record['mach']

snapshots = []
for line in (new / 'dbl.out').read_text().splitlines():
    fields = line.split()
    if len(fields) >= 4:
        snapshots.append({'nfile': int(fields[0]), 't_ns': float(fields[1]), 'dt_ns': float(fields[2]), 'step': int(fields[3])})
near_snap = {str(t): min(snapshots, key=lambda row: abs(row['t_ns'] - t)) for t in [100, 150, 200]}
near_step = {str(t): min(steps, key=lambda row: abs(row['t_ns'] - t)) for t in [100, 150, 200]}
mpi = psutil.Process(48700)
assert Path(mpi.cwd()).resolve() == new.resolve()
assert abs(mpi.create_time() - 1795076842.0309167) < 0.01
elapsed = now.timestamp() - mpi.create_time()
job = json.loads((root / 'T018b/job.json').read_text(encoding='utf-8-sig'))
batch_start = datetime.datetime.fromisoformat(job['started']).replace(tzinfo=now.tzinfo)
batch_elapsed = (now - batch_start).total_seconds()
last_t = float(budgets['v1i']['t_ns'][-1])
summary = {'captured': now.isoformat(timespec='seconds'), 'source_log': str(source), 'captured_log_bytes': size, 'last_budget_t_ns': last_t, 'mpi_pid': mpi.pid, 'mpi_created': mpi.create_time(), 'mpi_elapsed_seconds': elapsed, 'ns_per_wall_hour_since_mpi_start': last_t * 3600 / elapsed, 'batch_elapsed_seconds': batch_elapsed, 'ns_per_wall_hour_since_batch_start': last_t * 3600 / batch_elapsed, 'steps': steps, 'nearest_logged_steps': near_step, 'nearest_snapshots': near_snap, 'latest_dt_limits': diagnostics, 'budget_hashes': hashes, 'comparison_method': 'Linear interpolation at exactly100/150/200ns; signed percent =100*(v1i-v1h)/max(abs(v1i),abs(v1h),1e-30). Values in raw budget units. No acceptance tolerance imposed.'}
comparison = []
for t in [100, 150, 200]:
    for col in budgets['v1h']:
        if col == 't_ns':
            continue
        a = float(np.interp(t, budgets['v1h']['t_ns'], budgets['v1h'][col]))
        b = float(np.interp(t, budgets['v1i']['t_ns'], budgets['v1i'][col]))
        comparison.append({'t_ns': t, 'column': col, 'v1h': a, 'v1i': b, 'delta': b-a, 'signed_percent_maxabs': (b-a)/max(abs(a),abs(b),1e-30)*100})
with (out / 'budget_comparison.csv').open('w', newline='', encoding='utf-8') as handle:
    writer = csv.DictWriter(handle, fieldnames=list(comparison[0]))
    writer.writeheader()
    writer.writerows(comparison)
readback = list(csv.DictReader((out / 'budget_comparison.csv').open()))
assert len(readback) == len(comparison)
for original, actual in zip(comparison, readback):
    assert original['column'] == actual['column']
    for key in ['t_ns','v1h','v1i','delta','signed_percent_maxabs']:
        assert float(actual[key]) == original[key]
for name, value in [('checkpoint.json', summary)]:
    (out / name).write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')
shutil.copy2(new / 'source/source_meta.json', out / 'source_meta.json')
text = '# T018b low thin: 200ns checkpoint\n\n'
text += f"Captured {summary['captured']}; run continues, no parameter changes. Latest complete budget t={last_t:.4f}ns. Average throughput since MPI start={summary['ns_per_wall_hour_since_mpi_start']:.2f}ns/h over{elapsed:.1f}s; since batch launch={summary['ns_per_wall_hour_since_batch_start']:.2f}ns/h over{batch_elapsed:.1f}s. These include the early transient and do not predict remaining runtime.\n\n"
text += '## Time-step evidence\n\nLogging is every1000 steps. Exact100/150ns dt/Nrkl pairs were not recorded; all available step lines are reproduced below. Nearest step to both100 and150ns is129.45ns. Snapshot-index dt values near each requested time are separate evidence and do not carry Nrkl. No Nrkl interpolation.\n\n```text\n'
for row in steps:
    text += row['line'] + '\n' + (row['mach'] or 'Nrkl unavailable') + '\n'
text += '```\n\n| Requested ns | Snapshot ns | Snapshot step | Snapshot dt ns |\n|---:|---:|---:|---:|\n'
for t, row in near_snap.items():
    text += f"| {t} | {row['t_ns']:.6f} | {row['step']} | {row['dt_ns']:.7g} |\n"
text += '\nLatest recorded rank0 dt-limit (offset-verified, dt_src is seconds and differs from actual step dt):\n\n```text\n' + (diagnostics[0]['line'] if diagnostics else 'No recorded line') + '\n```\n\n'
text += '## Budget comparison\n\n' + summary['comparison_method'] + ' Tiny near-zero diagnostic values remain explicit; relative percentages alone are not an accuracy verdict.\n\n| t ns | Quantity | v1h | v1i | v1i-v1h | Signed % of maxabs |\n|---:|---|---:|---:|---:|---:|\n'
for row in comparison:
    text += f"| {row['t_ns']} | {row['column']} | {row['v1h']:.8e} | {row['v1i']:.8e} | {row['delta']:.4e} | {row['signed_percent_maxabs']:.5f} |\n"
text += '\nOriginal v1h run intentionally stopped per T018b step2; complete captured budgets retained with SHA256 in checkpoint.json. No fitting, tuning or pass/fail threshold was introduced.\n'
(out / 'COMPARISON_200ns.md').write_text(text, encoding='utf-8')
print(json.dumps({'captured': summary['captured'], 'last_t_ns': last_t, 'mpi_ns_per_hour': summary['ns_per_wall_hour_since_mpi_start'], 'steps': steps, 'snapshots': near_snap, 'latest_dt_limit': diagnostics[:1], 'key_comparison': [r for r in comparison if r['column'] in ['E_internal','E_kinetic','M_Cu','E_src','E_wall','E_gas_abs']]}, indent=2))
