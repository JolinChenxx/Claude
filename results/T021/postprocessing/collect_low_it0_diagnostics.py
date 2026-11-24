"""Archive completed T021 low-it0 diagnostics without rerunning calculations."""
from pathlib import Path
from datetime import datetime
import csv
import hashlib
import json
import math
import shutil

REPO = Path(__file__).resolve().parents[3]
RUN = Path('D:/sim_runs/T021/low_thin_G0/it0')
JOB = Path('D:/sim_runs/T021_post_low_it0_01')
OUT = REPO / 'results/T021/low_thin_G0/it0/diagnostics'


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    summary = json.loads((JOB / 'postprocess_summary.json').read_text(encoding='utf-8'))
    commands = {r['name']: r for r in summary['commands']}
    names = {'emission': 'diag_emission.csv', 'core_energy': 'core_energy.csv', 'shock_track': 'shock_track.csv'}
    for name in names:
        assert commands[name]['returncode'] == 0, name
    index = [line.split() for line in (RUN / 'dbl.out').read_text().splitlines() if line.strip()]
    times = [float(row[1]) for row in index]
    assert len(times) == 521 and times[0] == 0 and times[-1] == 1300
    assert all(b > a for a, b in zip(times, times[1:]))
    tables = {}
    checks = {}
    for name, filename in names.items():
        with (RUN / 'diagnostics' / filename).open(newline='') as stream:
            rows = list(csv.DictReader(stream))
        tables[name] = rows
        missing = {key: [i for i, row in enumerate(rows) if not row[key] or math.isnan(float(row[key]))] for key in rows[0]}
        for row in rows:
            assert all(not math.isinf(float(v)) for v in row.values() if v)
        checks[name] = {'rows': len(rows), 'missing_row_indices_zero_based': {k: v for k, v in missing.items() if v}, 'infinity_count': 0}
    shock = tables['shock_track']
    assert [float(row['t_ns']) for row in shock] == times
    core = tables['core_energy']
    assert [float(row['t_ns']) for row in core] == times[::4]
    assert len(core) == 131
    for key in ['E_int', 'E_kin', 'T_mw', 'P_rad', 'V']:
        assert checks['core_energy']['missing_row_indices_zero_based'].get(key, []) == [0, 1, 2, 3]
    for i in range(4):
        assert float(core[i]['M_Cu']) == 0
    for key in ['dEdt', 'P_other']:
        assert checks['core_energy']['missing_row_indices_zero_based'].get(key, []) == [0, 1, 2, 3, 4]
    emission = tables['emission']
    requested = [100, 150, 200, 250, 300, 400, 500, 600, 700, 800, 1000, 1200]
    assert [float(row['t_req_ns']) for row in emission] == requested
    assert not checks['emission']['missing_row_indices_zero_based']
    checks['emission']['nearest_snapshots'] = []
    for row, requested_time in zip(emission, requested):
        i = min(range(len(times)), key=lambda k: abs(times[k] - requested_time))
        assert float(row['t_snap_ns']) == times[i]
        checks['emission']['nearest_snapshots'].append({'requested_ns': requested_time, 'snapshot': int(index[i][0]), 'actual_ns': times[i]})
        for prefix in ['frac ', 'T ', 'Y ']:
            assert abs(sum(float(v) for k, v in row.items() if k.startswith(prefix)) - 1) < 1e-10
    OUT.mkdir(parents=True, exist_ok=True)
    sources = [RUN / 'diagnostics' / filename for filename in names.values()]
    sources += [JOB / (name + '.log') for name in names]
    files = []
    for source in sources:
        target = OUT / source.name
        before = source.stat()
        if target.exists():
            assert digest(target) == digest(source), 'Refuse to overwrite changed evidence: ' + str(target)
        else:
            shutil.copyfile(source, target)
        after = source.stat()
        assert (before.st_size, before.st_mtime_ns) == (after.st_size, after.st_mtime_ns)
        assert digest(source) == digest(target)
        files.append({'source': str(source), 'archive': str(target.relative_to(REPO)), 'bytes': after.st_size, 'sha256_raw_bytes': digest(target), 'source_mtime': datetime.fromtimestamp(after.st_mtime).astimezone().isoformat()})
    evidence = {'collected': datetime.now().astimezone().isoformat(), 'job_id': JOB.name, 'commands': [commands[n] for n in names], 'checks': checks, 'files': files, 'index_sha256': digest(RUN / 'dbl.out'), 'input_sha256': summary['preflight']['input_sha256'], 'missing_value_policy': 'Original empty fields retained. Core rows 0-3 have no Y>0.5 core; the first finite core row also lacks the centered derivative. Missing shock fronts remain missing; no interpolation or replacement.', 'image_v2': 'Separate stage remains in the same existing queue; these diagnostic files are complete independently of its state.'}
    (OUT / 'validation.json').write_text(json.dumps(evidence, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'collected': evidence['collected'], 'checks': checks, 'archived_bytes': sum(x['bytes'] for x in files)}, indent=2))


if __name__ == '__main__':
    main()
