"""T023 step3: exact early near-wall output slices, same format as T018c."""
from pathlib import Path
from datetime import datetime
import hashlib
import json
import sys
import numpy as np

REPO = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO / 'src'))
from lipsim.io import pluto

RUN = Path('D:/sim_runs/T023/high_thin_G0/it0')
OUT = REPO / 'results/T023/extract'
FIELDS = ['rho', 'T', 'prs', 'tr1', 'vx1', 'vx2', 'qlas', 'qrad']


def sha(path):
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def main():
    assert not (OUT / 'manifest.json').exists(), 'Extraction already recorded'
    grid = pluto.read_grid(RUN / 'grid.out')
    index = pluto.read_index(RUN / 'dbl.out')
    assert index[-1][1] >= 79, '80ns snapshot not available yet; do not wait or extrapolate'
    OUT.mkdir(parents=True, exist_ok=True)
    record = {'recorded': datetime.now().astimezone().isoformat(), 'member': 'high_thin_G0', 'iteration': 0, 'reference_script': 'results/T018c/extract_it2_fields.py', 'field_order': '[j,i]', 'index_convention': 'Zero-based physical output cells, inclusive i=0..30 and j=0..40; no ghost cells', 'units': 'Original PLUTO output/code values unchanged; x1/x2 in um, snapshot time in ns', 'snapshots': []}
    for requested in [40, 60, 80]:
        n, actual_time, endian, names = min(index, key=lambda r: abs(r[1] - requested))
        source = RUN / f'data.{n:04d}.dbl'
        before = source.stat()
        assert before.st_size == len(grid.x1) * len(grid.x2) * len(names) * 8
        snap = pluto.read_snapshot(RUN, n, grid)
        arrays = {name: snap.vars[name][:41, :31].copy() for name in FIELDS}
        arrays.update(x1=grid.x1[:31].copy(), x2=grid.x2[:41].copy(), i=np.arange(31), j=np.arange(41), t_ns=np.array(actual_time), nfile=np.array(n))
        assert all(arrays[name].shape == (41, 31) for name in FIELDS)
        assert all(np.isfinite(a).all() for a in arrays.values())
        target = OUT / f'it0_high_thin_G0_{actual_time:.6f}ns.npz'
        assert not target.exists()
        np.savez_compressed(target, **arrays)
        with np.load(target) as check:
            assert set(check.files) == set(arrays)
            assert all(np.array_equal(check[key], value) for key, value in arrays.items())
        source_sha = sha(source)
        after = source.stat()
        assert (before.st_size, before.st_mtime_ns) == (after.st_size, after.st_mtime_ns)
        record['snapshots'].append({'requested_t_ns': requested, 'actual_t_ns': actual_time, 'snapshot_number': n, 'index_line': next(line for line in (RUN / 'dbl.out').read_text().splitlines() if line.split()[0] == str(n)), 'source_path': str(source), 'source_bytes': before.st_size, 'source_sha256': source_sha, 'grid_sha256': sha(RUN / 'grid.out'), 'archive': target.name, 'archive_sha256': sha(target), 'shape': [41, 31], 'all_finite': True})
    (OUT / 'manifest.json').write_text(json.dumps(record, indent=2) + '\n', encoding='utf-8')
    assert sum(p.stat().st_size for p in (REPO / 'results/T023').rglob('*') if p.is_file()) < 20 * 1024**2
    print(json.dumps(record, indent=2))


if __name__ == '__main__':
    main()
