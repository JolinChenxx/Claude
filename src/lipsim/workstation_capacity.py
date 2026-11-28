"""Workstation-only scheduling for T034; count queued as well as active T033 members."""
import csv
from pathlib import Path
import yaml


def available(expected, rows):
    done = {r.get('run') for r in rows if r.get('batch') == 'T033' and r.get('run') in expected
            and r.get('returncode') == '0' and r.get('picard_it') == str(expected[r['run']])
            and r.get('procs') == '8' and r.get('ini_sha256') and r.get('dir')}
    # A T033 row is appended only after its MPI process has exited. Deduplicate.
    t033 = min(56, 8 * (len(expected) - len(done)))
    return 8 * ((60 - t033) // 8)


def capacity_reader(repo):
    cfg = yaml.safe_load((repo/'configs/batches/T033.yaml').read_text(encoding='utf-8'))
    specs = cfg['numerics']['runs']; expected = {s['id']: s['picard'] for s in specs}
    assert len(expected) == len(specs) == 18 and all(s['procs']==8 and s['start_it']==s['picard'] for s in specs)
    def read():
        try:
            with (repo/'results/run_registry.csv').open(newline='') as f:
                return available(expected, csv.DictReader(f))
        except (OSError, csv.Error, UnicodeError):
            return 0  # transient file activity: no new admissions
    return read
