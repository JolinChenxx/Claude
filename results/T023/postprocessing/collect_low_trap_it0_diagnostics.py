"""Validate/archive completed low-trap-it0 diagnostics without rerunning physics."""
from pathlib import Path
from datetime import datetime
import csv
import hashlib
import json
import math
import shutil

REPO = Path(__file__).resolve().parents[3]
RUN = Path('D:/sim_runs/T023/low_trap_G0/it0')
JOB = Path('D:/sim_runs/T021_T023_post_05')
OUT = REPO / 'results/T023/low_trap_G0/it0/diagnostics'
NAMES = {'emission': 'diag_emission.csv', 'core_energy': 'core_energy.csv',
         'shock_track': 'shock_track.csv'}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    summary = json.loads((JOB / 'postprocess_summary.json').read_text(encoding='utf-8'))
    commands = {r['name']: r for r in summary['commands']
                if r['batch'] == 'T023' and r['member'] == 'low_trap_G0' and r['iteration'] == 'it0'}
    for name in NAMES:
        assert commands[name]['returncode'] == 0, name
    for filename, expected in summary['preflight']['input_sha256'].items():
        assert digest(Path(filename)) == expected, filename
    index = [line.split() for line in (RUN / 'dbl.out').read_text().splitlines() if line.strip()]
    times = [float(row[1]) for row in index]
    assert len(times) == 521 and times[0] == 0 and times[-1] == 1300
    assert all(b > a for a, b in zip(times, times[1:]))
    tables, checks = {}, {}
    for name, filename in NAMES.items():
        with (RUN / 'diagnostics' / filename).open(newline='') as stream:
            rows = list(csv.DictReader(stream))
        assert rows
        tables[name] = rows
        missing = {key: [i for i, row in enumerate(rows)
                         if not row[key] or math.isnan(float(row[key]))] for key in rows[0]}
        assert all(not math.isinf(float(v)) for row in rows for v in row.values() if v)
        checks[name] = {'rows': len(rows), 'missing_row_indices_zero_based':
                        {k: v for k, v in missing.items() if v}, 'infinity_count': 0}
    assert [float(row['t_ns']) for row in tables['shock_track']] == times
    for key in ['R_axis_um', 'R_surf_um']:
        assert checks['shock_track']['missing_row_indices_zero_based'][key] == list(range(11))
    core = tables['core_energy']
    assert len(core) == 131
    assert [float(row['t_ns']) for row in core] == times[::4]
    # Inspect this member's actual no-core prefix, rather than reusing low-it2 counts.
    for key in ['E_int', 'E_kin', 'T_mw', 'P_rad', 'V']:
        assert checks['core_energy']['missing_row_indices_zero_based'][key] == list(range(4))
    assert [i for i, row in enumerate(core) if float(row['M_Cu']) == 0] == list(range(4))
    for key in ['dEdt', 'P_other']:
        assert checks['core_energy']['missing_row_indices_zero_based'][key] == list(range(5))
    requested = [100, 150, 200, 250, 300, 400, 500, 600, 700, 800, 1000, 1200]
    emission = tables['emission']
    assert [float(row['t_req_ns']) for row in emission] == requested
    assert not checks['emission']['missing_row_indices_zero_based']
    checks['emission']['nearest_snapshots'] = []
    for row, t_req in zip(emission, requested):
        i = min(range(len(times)), key=lambda k: abs(times[k] - t_req))
        assert float(row['t_snap_ns']) == times[i]
        checks['emission']['nearest_snapshots'].append(
            {'requested_ns': t_req, 'snapshot': int(index[i][0]), 'actual_ns': times[i]})
        for prefix in ['frac ', 'T ', 'Y ']:
            assert abs(sum(float(v) for k, v in row.items() if k.startswith(prefix)) - 1) < 1e-10
    assert len({row['t_snap_ns'] for row in emission}) == len(requested)
    OUT.mkdir(parents=True, exist_ok=True)
    sources = [RUN / 'diagnostics' / name for name in NAMES.values()]
    sources += [Path(commands[name]['log']) for name in NAMES]
    files = []
    for source in sources:
        target = OUT / source.name
        before = source.stat()
        expected = digest(source)
        if target.exists():
            assert digest(target) == expected, 'Refuse to overwrite changed evidence: ' + str(target)
        else:
            shutil.copyfile(source, target)
        after = source.stat()
        assert (before.st_size, before.st_mtime_ns) == (after.st_size, after.st_mtime_ns)
        assert digest(source) == digest(target) == expected
        files.append({'source': str(source), 'archive': target.relative_to(REPO).as_posix(),
                      'bytes': after.st_size, 'sha256_raw_bytes': expected,
                      'source_mtime': datetime.fromtimestamp(after.st_mtime).astimezone().isoformat()})
    evidence = {
        'collected': datetime.now().astimezone().isoformat(), 'job_id': JOB.name,
        'commands': [commands[n] for n in NAMES], 'checks': checks, 'files': files,
        'index_sha256': digest(RUN / 'dbl.out'),
        'input_sha256': summary['preflight']['input_sha256'],
        'frozen_inputs_rechecked': True,
        'missing_value_policy': 'Original empty fields retained: no Y>0.5 core at rows 0-3; centered derivative also unavailable at row 4; shock fronts absent at rows 0-10. No filling or interpolation.',
        'emission_time_note': 'Each requested time maps to a distinct nearest snapshot of the completed 1300 ns run.',
        'image_v2': 'Independent following stage in the existing queue; not collected or rerun here.'}
    (OUT / 'validation.json').write_text(json.dumps(evidence, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'collected': evidence['collected'], 'checks': checks,
                      'archived_bytes': sum(x['bytes'] for x in files)}, indent=2))


if __name__ == '__main__':
    main()
