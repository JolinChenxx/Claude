"""Scheduling-only tests: no real fields, images or computations are used."""
from pathlib import Path
import json
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'src'))
from lipsim.image.dispatch import dispatch, expected_files, output_records


def setup_case(tmp_path):
    owner = tmp_path / 'owner'
    original = tmp_path / 'original'
    owner.mkdir()
    original.mkdir()
    output = tmp_path / 'image_v2'
    argv = [str(tmp_path / 'run'), '--series', 'high', '--out', str(output), '--workers', '16']
    entry = dict(name='test_image_v2', output=str(output), argv=argv,
                 owner_job_dir=str(owner), original_job_dir=str(original),
                 owner_runner='results/test/run_it0.py')
    manifest = tmp_path / 'delegations.json'
    manifest.write_text(json.dumps(dict(delegations=[entry])))
    return entry, manifest, {'SIM_OUT_DIR': str(original)}


def finish_owner(entry):
    owner = Path(entry['owner_job_dir'])
    output = Path(entry['output'])
    output.mkdir()
    for name in expected_files():
        (output / name).write_bytes(b'synthetic scheduling fixture; not physics data\n')
    (owner / 'job.json').write_text(json.dumps(dict(id=owner.name, dir=str(owner),
                                                  cmd='python ' + entry['owner_runner'])))
    (owner / 'exitcode.txt').write_text('0')
    (owner / 'postprocess_summary.json').write_text(json.dumps(dict(
        state='completed', job_id=owner.name, completed='test-time',
        commands=[dict(name=entry['name'], returncode=0, execution='computed_by_owner',
                       command=['python', '-m', 'lipsim.image'] + entry['argv'])],
        output_files=output_records(output))))


def test_unrelated_passthrough(tmp_path):
    entry, manifest, env = setup_case(tmp_path)
    assert dispatch(['other', '--out', str(tmp_path / 'elsewhere')], env, manifest) is None
    assert dispatch(entry['argv'], env, tmp_path / 'missing.json') is None


def test_designated_owner_only(tmp_path):
    entry, manifest, env = setup_case(tmp_path)
    assert dispatch(entry['argv'], {'SIM_OUT_DIR': entry['owner_job_dir'] + ' '}, manifest) is None
    assert dispatch(entry['argv'], {}, manifest) == 75
    assert dispatch(entry['argv'], env, manifest) == 75
    assert not Path(entry['output']).exists()


def test_mismatch_fails_closed_even_for_owner(tmp_path):
    entry, manifest, env = setup_case(tmp_path)
    modified = entry['argv'][:-1] + ['8']
    assert dispatch(modified, {'SIM_OUT_DIR': entry['owner_job_dir']}, manifest) == 75


def test_finished_owner_verified_skip_and_receipt(tmp_path):
    entry, manifest, env = setup_case(tmp_path)
    finish_owner(entry)
    assert dispatch(entry['argv'], env, manifest) == 0
    receipt = json.loads((Path(entry['original_job_dir']) / 'delegated_test_image_v2.json').read_text())
    assert receipt['action'] == 'verified_delegation_skip'
    assert receipt['owner']['job_id'] == 'owner'


def test_failed_owner_or_changed_artifacts_never_skip(tmp_path):
    entry, manifest, env = setup_case(tmp_path)
    finish_owner(entry)
    owner = Path(entry['owner_job_dir'])
    (owner / 'exitcode.txt').write_text('1')
    assert dispatch(entry['argv'], env, manifest) == 75
    (owner / 'exitcode.txt').write_text('0')
    (Path(entry['output']) / 'image_summary.json').write_text('changed')
    assert dispatch(entry['argv'], env, manifest) == 75


def test_missing_artifact_never_skip(tmp_path):
    entry, manifest, env = setup_case(tmp_path)
    finish_owner(entry)
    (Path(entry['output']) / 'frames_a_sa.npz').unlink()
    assert dispatch(entry['argv'], env, manifest) == 75


def test_administrative_rc0_is_not_compute_success(tmp_path):
    entry, manifest, env = setup_case(tmp_path)
    finish_owner(entry)
    path = Path(entry['owner_job_dir']) / 'postprocess_summary.json'
    summary = json.loads(path.read_text())
    summary['commands'][0]['execution'] = 'skipped'
    path.write_text(json.dumps(summary))
    assert dispatch(entry['argv'], env, manifest) == 75
