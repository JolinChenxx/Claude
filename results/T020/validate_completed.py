"""Validate T020 output/provenance without evaluating or modifying the LTE model."""
from pathlib import Path
import csv
import datetime as dt
import hashlib
import json
import math
import re

REPO = Path(__file__).resolve().parents[2]
ROOT = REPO / 'results/T020'
JOB = Path('D:/sim_runs/T020_diag_01')


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def read_json(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))


def main():
    target = ROOT / 'completion.json'
    assert not target.exists(), 'Inspect existing completion evidence instead of overwriting it.'
    plan = read_json(ROOT / 'plan.json')
    summary = read_json(JOB / 'postprocess_summary.json')
    metadata = read_json(JOB / 'job.json')
    launch = read_json(ROOT / 'launch.json')
    assert metadata == launch['job']
    assert metadata['id'] == plan['job_id'] == summary['job_id'] == JOB.name
    assert Path(metadata['dir']) == JOB
    assert (JOB / 'exitcode.txt').read_text().strip() == '0'
    assert summary['state'] == 'completed' and 'launch_error' not in summary
    assert summary['runner_sha256'] == plan['runner_sha256'] == sha(ROOT / 'run_diagnostic.py')
    assert summary['plan_sha256'] == launch['plan_sha256'] == sha(ROOT / 'plan.json')
    for row in plan['frozen_files']:
        assert sha(row['path']) == row['sha256'], row['path']
    assert sha(REPO / 'results/T019/run_split.py') == plan['capacity_helper_sha256']
    records = {r['name']: r for r in summary['commands']}
    assert len(records) == len(summary['commands']) == len(plan['commands']) == 2
    validations = []
    for command in plan['commands']:
        actual = records[command['member']]
        assert actual['command'] == command['command'] and actual['returncode'] == 0
        assert actual['execution'] == 'computed_by_owner' and actual['ended']
        for snapshot in command['snapshots']:
            st = Path(snapshot['path']).stat()
            assert (st.st_size, st.st_mtime_ns) == (snapshot['bytes'], snapshot['mtime_ns'])
        output = Path(command['command'][-1])
        log = Path(actual['log'])
        assert output.parent == log.parent == ROOT
        with output.open(newline='', encoding='utf-8') as stream:
            reader = csv.DictReader(stream)
            rows = list(reader)
            fields = reader.fieldnames
        assert len(rows) == len(command['times'])
        assert len(fields) == len(set(fields)) == 26
        assert all(set(r) == set(fields) for r in rows)
        numeric = [{k: float(v) for k, v in r.items()} for r in rows]
        assert all(math.isfinite(v) for r in numeric for v in r.values())
        assert [r['t_req_ns'] for r in numeric] == command['times']
        assert [r['t_snap_ns'] for r in numeric] == [s['actual_t_ns'] for s in command['snapshots']]
        assert len(set(r['t_snap_ns'] for r in numeric)) == len(rows)
        errors = {}
        for prefix in ('frac ', 'T ', 'Y '):
            columns = [k for k in fields if k.startswith(prefix)]
            assert columns
            assert all(-1e-12 <= r[k] <= 1 + 1e-12 for r in numeric for k in columns)
            error = max(abs(math.fsum(r[k] for k in columns) - 1) for r in numeric)
            assert error < 1e-12, (command['member'], prefix, error)
            errors[prefix] = error
        assert all(r['P_total_W_sr'] > 0 and r['T_emw_K'] > 0 and r['ne_emw_m3'] > 0
                   and 0 <= r['Y_emw'] <= 1 for r in numeric)
        lines = log.read_text(encoding='utf-8').splitlines()
        assert len(lines) == len(rows) and all(line.startswith('t=') for line in lines)
        logged_times = re.findall(r'^t=(\d+) ns', '\n'.join(lines), re.M)
        assert logged_times == [f'{r["t_snap_ns"]:.0f}' for r in numeric]
        validations.append({'member': command['member'], 'iteration': command['iteration'],
                            'csv': str(output), 'csv_sha256': sha(output), 'rows': len(rows),
                            'columns': fields, 'all_values_finite': True,
                            'log': str(log), 'log_sha256': sha(log), 'log_lines': len(lines),
                            'fraction_sum_max_abs_errors': errors,
                            'max_requested_actual_offset_ns': max(abs(r['t_req_ns'] - r['t_snap_ns']) for r in numeric),
                            'selected_rows': [r for r in numeric if r['t_req_ns'] in (300, 400, 800, 1200)]})
    assert (JOB / 'stderr.log').stat().st_size == 0
    completion = {'verified': dt.datetime.now().astimezone().isoformat(), 'job': metadata,
                  'wrapper_exitcode': 0, 'summary': summary,
                  'owner_summary_sha256': sha(JOB / 'postprocess_summary.json'),
                  'validator_sha256': sha(__file__), 'validations': validations,
                  'diagnostic_only': True, 'lte_criteria_evaluated': False,
                  'snapshot_policy': 'Original nearest-snapshot selection; frozen size/mtime fingerprints.',
                  'consistency_tolerance': '1e-12 for dimensionless fraction roundoff only; no physical acceptance threshold.',
                  'scheduler_wall_seconds': (dt.datetime.fromisoformat(summary['ended']) - dt.datetime.fromisoformat(summary['started'])).total_seconds()}
    target.write_text(json.dumps(completion, indent=2) + '\n', encoding='utf-8')
    assert read_json(target) == completion
    size = sum(p.stat().st_size for p in ROOT.rglob('*') if p.is_file())
    assert size < 20 * 1024 * 1024
    print(json.dumps({'verified': completion['verified'], 'scheduler_wall_seconds': completion['scheduler_wall_seconds'],
                      'results_bytes': size, 'members': [{k: v[k] for k in ('member', 'rows', 'csv_sha256',
                          'log_lines', 'fraction_sum_max_abs_errors', 'max_requested_actual_offset_ns')} for v in validations]}, indent=2))


if __name__ == '__main__':
    main()
