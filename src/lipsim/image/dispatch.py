"""Opt-in job delegation; no imaging/model calculations are changed here.

Future children of an already-running queue read the manifest afresh. A
non-owner may skip only after the owner's successful command and artifacts
are verified. Pending/failed delegation returns 75 without computing.
"""
from pathlib import Path
import datetime as dt
import hashlib
import json
import os
import sys

MANIFEST = Path(__file__).resolve().parents[3] / 'results/T018c/postprocessing/delegations.json'


def norm(value):
    return os.path.normcase(os.path.normpath(str(value).strip()))


def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def expected_files():
    return ({'image_summary.json'}
            | {f'frames_{f}_{mode}.npz' for f in 'abc' for mode in ('thin', 'sa')}
            | {f'metrics_{f}_{mode}_{s}.csv' for f in 'abc'
               for mode in ('thin', 'sa') for s in ('baseline', 'low', 'high')})


def output_records(output):
    output = Path(output)
    names = {p.name for p in output.iterdir() if p.is_file()}
    if names != expected_files():
        raise RuntimeError('Incomplete/unexpected delegated output files')
    records = []
    for name in sorted(names):
        path = output / name
        if path.stat().st_size == 0:
            raise RuntimeError('Empty delegated output: ' + name)
        records.append(dict(file=name, bytes=path.stat().st_size, sha256=sha(path)))
    return records


def completed_evidence(entry):
    """Validate real owner execution, never an administrative queue skip."""
    job = Path(entry['owner_job_dir'])
    if (job / 'exitcode.txt').read_text().strip() != '0':
        raise RuntimeError('Delegated owner did not exit successfully')
    metadata = json.loads((job / 'job.json').read_text(encoding='utf-8-sig'))
    if metadata['id'] != job.name or norm(metadata['dir']) != norm(job):
        raise RuntimeError('Delegated job identity mismatch')
    if entry['owner_runner'].replace('\\', '/') not in metadata['cmd'].replace('\\', '/'):
        raise RuntimeError('Delegated runner identity mismatch')
    summary = json.loads((job / 'postprocess_summary.json').read_text(encoding='utf-8'))
    if summary['state'] != 'completed' or summary['job_id'] != job.name:
        raise RuntimeError('Delegated owner is not complete')
    row, = [r for r in summary['commands'] if r['name'] == entry['name']
            and r.get('returncode') == 0]
    if [norm(x) for x in row['command'][3:]] != [norm(x) for x in entry['argv']]:
        raise RuntimeError('Delegated completed command mismatch')
    if row.get('execution') != 'computed_by_owner':
        raise RuntimeError('Delegated row lacks actual execution evidence')
    if summary['output_files'] != output_records(entry['output']):
        raise RuntimeError('Delegated output hashes changed')
    return dict(job_id=job.name, command=row, completed=summary['completed'])


def dispatch(argv=None, environ=None, manifest=MANIFEST):
    """Return None to compute normally, 0 for verified skip, 75 to defer."""
    argv = list(sys.argv[1:] if argv is None else argv)
    environ = os.environ if environ is None else environ
    if not Path(manifest).exists() or '--out' not in argv:
        return None
    output_index = argv.index('--out') + 1
    if output_index >= len(argv):
        return None  # Original CLI handles malformed invocations.
    entries = json.loads(Path(manifest).read_text(encoding='utf-8'))['delegations']
    matches = [e for e in entries if norm(e['output']) == norm(argv[output_index])]
    if not matches:
        return None
    if len(matches) != 1:
        raise RuntimeError('Duplicate image delegation')
    entry = matches[0]
    if [norm(x) for x in argv] != [norm(x) for x in entry['argv']]:
        print('DELEGATED_DEFERRED: exact imaging arguments do not match', flush=True)
        return 75  # num (EX_TEMPFAIL exit code)
    caller = environ.get('SIM_OUT_DIR', '').strip()
    if norm(caller) == norm(entry['owner_job_dir']):
        return None
    if norm(caller) != norm(entry['original_job_dir']):
        print('DELEGATED_DEFERRED: this output belongs to ' + entry['owner_job_dir'], flush=True)
        return 75  # num (EX_TEMPFAIL exit code)
    try:
        evidence = completed_evidence(entry)
    except (OSError, ValueError, KeyError, RuntimeError) as error:
        print('DELEGATED_DEFERRED: ' + str(error), flush=True)
        return 75  # num (EX_TEMPFAIL exit code)
    receipt = dict(action='verified_delegation_skip', checked=dt.datetime.now().astimezone().isoformat(),
                   original_job_dir=caller, output=entry['output'], owner=evidence)
    (Path(caller) / f"delegated_{entry['name']}.json").write_text(
        json.dumps(receipt, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(receipt), flush=True)
    return 0
