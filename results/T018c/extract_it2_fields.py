"""One-time read-only field/source extraction requested by handoff dea7dfe.

Source time profiles retain all 8001 times at r=0; complete source NPZs
remain local because four originals exceed the 20 MB/card archive limit.
"""
from pathlib import Path
import datetime as dt
import hashlib
import json
import shutil
import sys

import numpy as np

REPO = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO / 'src'))
from lipsim.io import pluto

OUT = REPO / 'results/T018c/extract'
ROOT = Path('D:/sim_runs/T018c')
FIELDS = ['rho', 'T', 'prs', 'tr1', 'vx1', 'vx2', 'qlas', 'qrad']


def sha(path):
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def last_diagnostics(path):
    """Read backwards to the last three complete matching lines per category."""
    patterns = {'lip vmax': b'lip vmax', 'lip dt-limit': b'lip dt-limit'}
    found = {k: [] for k in patterns}
    initial = path.stat()
    position = initial.st_size
    carry = b''
    scanned = 0
    with path.open('rb') as stream:
        stream.seek(max(0, position - 1))
        complete_last_line = stream.read(1) == b'\n'
        first = True
        while position > 0 and any(len(v) < 3 for v in found.values()):
            size = min(position, 1024 * 1024)
            position -= size
            stream.seek(position)
            chunk = stream.read(size)
            scanned += len(chunk)
            lines = (chunk + carry).split(b'\n')
            carry = lines.pop(0) if position else b''
            if first and not complete_last_line:
                lines.pop()  # Active trailing line may still be in flight.
            first = False
            for line in reversed(lines):
                for key, token in patterns.items():
                    if len(found[key]) < 3 and token in line:
                        found[key].append(line.decode('utf-8', errors='replace').rstrip('\r'))
    return {'path': str(path), 'initial_bytes': initial.st_size,
            'initial_mtime_ns': initial.st_mtime_ns, 'bytes_scanned_backwards': scanned,
            'last_three': {k: list(reversed(v)) for k, v in found.items()},
            'note': 'Fewer than three means fewer were present in the captured file extent; no fabricated lines.'}


def main():
    manifest_path = OUT / 'manifest.json'
    if manifest_path.exists():
        raise RuntimeError('Extraction already recorded; inspect instead of overwriting.')
    OUT.mkdir(parents=True, exist_ok=True)
    record = {'extracted': dt.datetime.now().astimezone().isoformat(),
              'authority_commit': 'dea7dfe', 'field_order': '[j, i]',
              'index_convention': 'Zero-based physical output cells, inclusive j=0..40, i=0..30; ghost cells are absent from dbl.',
              'field_units': 'Original PLUTO output/code values unchanged; x1/x2 in um and snapshot t in ns.',
              'members': {}, 'sources': [],
              'source_scope': 'Exact r=0 columns at every original source time, all fields, no interpolation or resampling. Full time-radius source NPZs retained locally; 20 MB/card prevents copying all four.'}
    for member in ['high_thin_G0', 'high_trap_G0']:
        run = ROOT / member / 'it2'
        grid = pluto.read_grid(run / 'grid.out')
        index = pluto.read_index(run / 'dbl.out')
        n, time, endian, names = index[-1]
        source = run / f'data.{n:04d}.dbl'
        before = source.stat()
        assert before.st_size == len(grid.x1) * len(grid.x2) * len(names) * 8
        snap = pluto.read_snapshot(run, n, grid)
        assert source.stat().st_size == before.st_size and source.stat().st_mtime_ns == before.st_mtime_ns
        assert len(grid.x1) >= 31 and len(grid.x2) >= 41
        arrays = {name: snap.vars[name][:41, :31].copy() for name in FIELDS}
        arrays.update(x1=grid.x1[:31].copy(), x2=grid.x2[:41].copy(),
                      i=np.arange(31), j=np.arange(41), t_ns=np.array(time), nfile=np.array(n))
        filename = f'it2_{member}_{time:.6f}ns.npz'
        target = OUT / filename
        assert not target.exists()
        np.savez_compressed(target, **arrays)
        with np.load(target) as actual:
            assert set(actual.files) == set(arrays)
            for key, values in arrays.items():
                assert np.array_equal(actual[key], values, equal_nan=True)
        extrema = {k: {'min': float(np.nanmin(v)), 'max': float(np.nanmax(v)),
                       'finite': bool(np.isfinite(v).all())} for k, v in arrays.items() if k in FIELDS}
        ranks = [last_diagnostics(run / f'pluto.{rank}.log') for rank in range(8)]
        ranks_path = OUT / f'it2_{member}_rank_diagnostics.json'
        ranks_path.write_text(json.dumps(ranks, indent=2) + '\n', encoding='utf-8')
        record['members'][member] = {'snapshot_number': n, 'snapshot_t_ns': time,
            'index_line': next(line for line in (run / 'dbl.out').read_text().splitlines() if line.split()[0] == str(n)),
            'source_path': str(source), 'source_bytes': before.st_size, 'source_sha256': sha(source),
            'grid_sha256': sha(run / 'grid.out'), 'archive': filename,
            'archive_sha256': sha(target), 'shape': [41, 31], 'statistics': extrema,
            'rank_diagnostics': ranks_path.name,
            'rank_diagnostic_counts': [{k: len(v) for k, v in rank['last_three'].items()} for rank in ranks]}
        for iteration in ['it1', 'it2']:
            srcdir = ROOT / member / iteration / 'source'
            destdir = OUT / 'source' / member / iteration
            destdir.mkdir(parents=True, exist_ok=True)
            meta = srcdir / 'source_meta.json'
            shutil.copy2(meta, destdir / meta.name)
            assert sha(meta) == sha(destdir / meta.name)
            full = srcdir / 'target_source.npz'
            with np.load(full) as z:
                r = z['r'].copy()
                assert r[0] == 0
                profiles = {}
                for key in z.files:
                    values = z[key]
                    if key == 't': profiles[key] = values.copy()
                    elif key == 'r': profiles[key] = values[:1].copy()
                    else:
                        assert values.shape == (len(z['t']), len(r))
                        profiles[key] = values[:, :1].copy()
            outprofile = destdir / 'target_source_axis_profile.npz'
            np.savez_compressed(outprofile, **profiles)
            with np.load(outprofile) as z:
                assert set(z.files) == set(profiles)
                for key, values in profiles.items():
                    assert np.array_equal(z[key], values, equal_nan=True)
            csv_files = []
            for csv in srcdir.glob('*.csv'):
                shutil.copy2(csv, destdir / csv.name)
                assert sha(csv) == sha(destdir / csv.name)
                csv_files.append(csv.name)
            record['sources'].append({'member': member, 'iteration': iteration,
                'metadata': str((destdir / meta.name).relative_to(OUT)), 'metadata_sha256': sha(meta),
                'binary_sha256_verified': sha(srcdir / 'target_source.bin') == json.loads(meta.read_text())['bin_sha256'],
                'full_source_path': str(full), 'full_source_bytes': full.stat().st_size,
                'full_source_sha256': sha(full), 'full_r_points': len(r),
                'profile': str(outprofile.relative_to(OUT)), 'profile_sha256': sha(outprofile),
                'profile_t_points': len(profiles['t']), 'profile_t_range_seconds': [float(profiles['t'][0]), float(profiles['t'][-1])],
                'profile_r_m': [0.0], 'profile_fields': list(profiles),
                'profile_all_finite': all(np.isfinite(v).all().item() for v in profiles.values()),
                'existing_source_csvs': csv_files})
    assert all(s['binary_sha256_verified'] for s in record['sources'])
    record['full_source_total_bytes'] = sum(s['full_source_bytes'] for s in record['sources'])
    manifest_path.write_text(json.dumps(record, indent=2) + '\n', encoding='utf-8')
    assert json.loads(manifest_path.read_text()) == record
    size = sum(p.stat().st_size for p in (REPO / 'results/T018c').rglob('*') if p.is_file())
    assert size < 20 * 1024 * 1024, size
    print(json.dumps({'members': {k: {'t_ns': v['snapshot_t_ns'], 'shape': v['shape'],
          'rank_counts': v['rank_diagnostic_counts']} for k, v in record['members'].items()},
          'full_source_total_bytes': record['full_source_total_bytes'], 'total_results_bytes': size}, indent=2))


if __name__ == '__main__':
    main()
