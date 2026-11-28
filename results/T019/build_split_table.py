"""Verify completed T019 archives and join exact, unnormalised radiance CSV values."""
from pathlib import Path
import csv
import datetime as dt
from decimal import Decimal
import hashlib
import json
import math

REPO = Path(__file__).resolve().parents[2]
ROOT = REPO / 'results/T019'
JOB = Path('D:/sim_runs/T019_split_01')


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def read_json(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))


def main():
    target = ROOT / 'split_table.csv'
    receipt = ROOT / 'completion.json'
    assert not target.exists() and not receipt.exists(), 'Inspect existing outputs before retrying.'
    plan = read_json(ROOT / 'plan.json')
    status = read_json(JOB / 'postprocess_summary.json')
    metadata = read_json(JOB / 'job.json')
    assert metadata['id'] == status['job_id'] == plan['job_id'] == JOB.name
    assert Path(metadata['dir']) == JOB
    assert metadata['cmd'] == f'{Path("D:/sim_tools/envs/lipsim/python.exe")} {ROOT / "run_split.py"}'
    assert (JOB / 'exitcode.txt').read_text().strip() == '0'
    assert status['state'] == 'completed' and status['ended'] and not status['pending_commands']
    assert status['runner_sha256'] == plan['runner_sha256'] == sha(ROOT / 'run_split.py')
    assert status['plan_sha256'] == sha(ROOT / 'plan.json')
    assert len(status['commands']) == len(plan['commands']) == 4
    for frozen in plan['frozen_files']:
        assert sha(frozen['path']) == frozen['sha256'], frozen['path']
    evidence = {c['name']: c for c in status['commands']}
    assert len(evidence) == 4
    for command in plan['commands']:
        actual = evidence[command['name']]
        assert actual['returncode'] == 0 and actual['ended']
        assert actual['execution'] == 'computed_by_owner'
        assert actual['command'] == command['command']
        for snapshot in command['snapshot_files']:
            st = Path(snapshot['path']).stat()
            assert (st.st_size, st.st_mtime_ns) == (snapshot['bytes'], snapshot['mtime_ns'])

    sources, archives, rows = [], [], []
    for source_input in plan['inputs']:
        member = source_input['member']
        validated = {}
        for component in ('continuum', 'lines'):
            directory = ROOT / member / ('image_v2_' + component)
            validation = read_json(directory / 'validation.json')
            assert validation['completion_evidence'] == evidence[f'{member}_{source_input["iteration"]}_{component}']
            assert validation['runner_sha256'] == status['runner_sha256']
            assert validation['plan_sha256'] == status['plan_sha256']
            assert not validation['nonfinite_P1']
            records = {f['file']: f for f in validation['files']}
            assert len(records) == 25
            assert len([f for f in records if f.endswith('.csv')]) == 18
            assert len([f for f in records if f.endswith('.npz')]) == 6
            for filename, record in records.items():
                if filename.endswith('.npz'):
                    assert all(a['finite'] for a in record['arrays'].values())
                else:
                    assert sha(directory / filename) == record['sha256']
                    assert sha(Path(source_input['run']) / ('image_v2_' + component) / filename) == record['sha256']
            validated[component] = records
            summary = read_json(directory / 'image_summary.json')
            archives.append({'member': member, 'component': component,
                             'validation': str(directory / 'validation.json'),
                             'validation_sha256': sha(directory / 'validation.json'),
                             'P2_unavailable': validation['P2_unavailable'],
                             'negative_P1': validation['negative_P1'],
                             'nonfinite_P1': validation['nonfinite_P1'],
                             'b_baseline_P1_ns': {mode: summary[f'b_{mode}_baseline']['P1_tau_ns']
                                                  for mode in ('thin', 'sa')}})
        for mode in ('thin', 'sa'):
            baseline = source_input['baseline'][mode]
            filename = f'metrics_b_{mode}_baseline.csv'
            assert sha(baseline['path']) == sha(baseline['archive']) == baseline['sha256']
            series = {}
            for component in ('all', 'continuum', 'lines'):
                path = Path(baseline['archive']) if component == 'all' else ROOT / member / ('image_v2_' + component) / filename
                expected_hash = baseline['sha256'] if component == 'all' else validated[component][filename]['sha256']
                assert sha(path) == expected_hash
                with path.open(newline='', encoding='utf-8') as stream:
                    values = list(csv.DictReader(stream))
                times = [Decimal(r['t_ns']) for r in values]
                assert times == [Decimal(str(t)) for t in baseline['times']] == sorted(set(times))
                assert all(math.isfinite(float(r['L_radiance'])) for r in values)
                series[component] = values
                sources.append({'member': member, 'mode': mode, 'component': component,
                                'path': str(path), 'sha256': expected_hash, 'rows': len(values)})
            for i, full in enumerate(series['all']):
                rows.append({'member': member, 'iteration': source_input['iteration'],
                             'series': source_input['series'], 't_ns': full['t_ns'],
                             'filter': 'b', 'defocus': 'baseline', 'mode': mode,
                             **{f'L_radiance_{c}': series[c][i]['L_radiance'] for c in series}})

    assert len(rows) == 118
    assert len({(r['member'], r['mode'], Decimal(r['t_ns'])) for r in rows}) == 118
    with target.open('x', newline='', encoding='utf-8') as stream:
        writer = csv.DictWriter(stream, fieldnames=list(rows[0]), lineterminator='\n')
        writer.writeheader()
        writer.writerows(rows)
    with target.open(newline='', encoding='utf-8') as stream:
        assert list(csv.DictReader(stream)) == rows
    completed = {'verified': dt.datetime.now().astimezone().isoformat(), 'job': metadata,
                 'wrapper_exitcode': 0, 'owner_summary': status,
                 'owner_summary_sha256': sha(JOB / 'postprocess_summary.json'),
                 'diagnostic_only': True, 'self_absorbed_additivity_required': False,
                 'metrics_csvs': 72, 'image_summaries': 4, 'validated_local_frame_archives': 24,
                 'archives': archives, 'sources': sources, 'split_table_rows': len(rows),
                 'split_table_sha256': sha(target), 'generator_sha256': sha(__file__),
                 'value_policy': 'Exact source CSV strings; no interpolation, renormalisation or rounding.',
                 'snapshot_policy': 'Frozen size/mtime fingerprints; no full raw-field rehash.',
                 'job_wall_seconds': (dt.datetime.fromisoformat(status['ended']) - dt.datetime.fromisoformat(status['started'])).total_seconds()}
    receipt.write_text(json.dumps(completed, indent=2) + '\n', encoding='utf-8')
    assert read_json(receipt) == completed
    size = sum(p.stat().st_size for p in ROOT.rglob('*') if p.is_file())
    assert size <= 20 * 1024 * 1024
    print(json.dumps({k: completed[k] for k in ('verified', 'split_table_rows', 'split_table_sha256', 'job_wall_seconds')}))
    print(json.dumps({'total_results_bytes': size, 'archives': archives}, indent=2))


if __name__ == '__main__':
    main()
