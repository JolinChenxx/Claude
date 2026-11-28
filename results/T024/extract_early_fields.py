"""T024 step 3: exact early field windows and original budget rows <=80 ns."""
from datetime import datetime
from pathlib import Path
import csv
import hashlib
import io
import json
import sys

import numpy as np
import psutil

REPO = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO / 'src'))
from lipsim.io import pluto

RUN = Path('D:/sim_runs/T024/high_thin_G0/it0')
OUT = REPO / 'results/T024/extract'
FIELDS = ['rho', 'T', 'prs', 'tr1', 'tr2', 'vx1', 'vx2', 'qlas', 'qrad']


def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def main():
    assert not (OUT / 'manifest.json').exists(), 'Extraction already recorded'
    proc = psutil.Process(37412)
    assert abs(proc.create_time() - 1795503549.0785756) < .01
    budget_capture = (RUN / 'budget.csv').read_bytes()
    lines = budget_capture.splitlines(keepends=True)
    if not lines[-1].endswith(b'\n'):
        lines.pop()
    budget = list(csv.DictReader(io.StringIO(b''.join(lines).decode('utf-8'))))
    assert float(budget[-1]['t_ns']) >= 80
    assert len(budget[0]) == 17 and {'E_bd_store', 'E_bd_transfer'} <= set(budget[0])
    selected_rows = [r for r in budget if float(r['t_ns']) <= 80]
    assert all(np.isfinite(float(v)) for r in selected_rows for v in r.values())
    assert all(float(b['t_ns']) > float(a['t_ns']) for a, b in zip(budget, budget[1:]))
    selected_bytes = lines[0] + b''.join(line for line, row in zip(lines[1:], budget) if float(row['t_ns']) <= 80)
    assert selected_bytes == b''.join(lines[:len(selected_rows) + 1])
    grid = pluto.read_grid(RUN / 'grid.out')
    index = pluto.read_index(RUN / 'dbl.out')
    assert index[-1][1] >= 80 and len(grid.x1) >= 31 and len(grid.x2) >= 41
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / '.gitattributes').write_text('* -text\n', encoding='utf-8')
    record = {
        'recorded': datetime.now().astimezone().isoformat(),
        'authority': 'tasks/T024-breakdown-2t.md step 3',
        'member': 'high_thin_G0', 'iteration': 0,
        'reference_script': 'results/T018c/extract_it2_fields.py',
        'field_order': '[j,i]', 'fields': FIELDS,
        'index_convention': 'Zero-based physical cells, inclusive i=0..30,j=0..40; no ghost cells',
        'units': 'Original PLUTO output/code values unchanged; x1/x2 in um, time in ns',
        'reader_sha256': sha(REPO / 'src/lipsim/io/pluto.py'),
        'snapshots': [],
    }
    for requested in [30, 40, 60, 80]:
        n, actual, endian, names = min(index, key=lambda r: abs(r[1] - requested))
        assert set(FIELDS) <= set(names)
        source = RUN / f'data.{n:04d}.dbl'
        before = source.stat()
        assert before.st_size == len(grid.x1) * len(grid.x2) * len(names) * 8
        source_sha = sha(source)
        snap = pluto.read_snapshot(RUN, n, grid)
        assert snap.t == actual
        arrays = {name: snap.vars[name][:41, :31].copy() for name in FIELDS}
        arrays.update(x1=grid.x1[:31].copy(), x2=grid.x2[:41].copy(),
                      i=np.arange(31), j=np.arange(41), t_ns=np.array(actual), nfile=np.array(n))
        assert all(arrays[name].shape == (41, 31) for name in FIELDS)
        assert all(np.isfinite(a).all() for a in arrays.values())
        target = OUT / f'it0_high_thin_G0_{actual:.6f}ns.npz'
        assert not target.exists()
        np.savez_compressed(target, **arrays)
        with np.load(target) as check:
            assert set(check.files) == set(arrays)
            assert all(np.array_equal(check[k], v) for k, v in arrays.items())
        after = source.stat()
        assert (before.st_size, before.st_mtime_ns) == (after.st_size, after.st_mtime_ns)
        assert sha(source) == source_sha
        record['snapshots'].append({
            'requested_t_ns': requested, 'actual_t_ns': actual, 'snapshot_number': n,
            'index_line': next(line for line in (RUN / 'dbl.out').read_text().splitlines() if line.split()[0] == str(n)),
            'source_path': str(source), 'source_bytes': before.st_size,
            'source_sha256': source_sha, 'grid_sha256': sha(RUN / 'grid.out'),
            'archive': target.name, 'archive_sha256': sha(target),
            'shape': [41, 31], 'all_finite': True,
            'tr2_min': float(arrays['tr2'].min()), 'tr2_max': float(arrays['tr2'].max()),
        })
    target = OUT / 'budget_through_80ns.csv'
    assert not target.exists()
    target.write_bytes(selected_bytes)
    assert target.read_bytes() == selected_bytes
    record['budget'] = {
        'source': str(RUN / 'budget.csv'), 'archive': target.name,
        'rows': len(selected_rows), 'columns': list(budget[0]),
        'first_t_ns': float(selected_rows[0]['t_ns']),
        'last_t_ns': float(selected_rows[-1]['t_ns']),
        'live_capture_last_t_ns': float(budget[-1]['t_ns']),
        'source_capture_sha256': hashlib.sha256(budget_capture).hexdigest(),
        'archive_sha256': sha(target), 'all_finite': True,
        'policy': 'Original header and complete rows <=80 ns copied byte-for-byte; no interpolation or extrapolation',
    }
    (OUT / 'manifest.json').write_text(json.dumps(record, indent=2) + '\n', encoding='utf-8')
    assert json.loads((OUT / 'manifest.json').read_text()) == record
    total = sum(p.stat().st_size for p in (REPO / 'results/T024').rglob('*') if p.is_file())
    assert total < 20 * 1024**2
    print(json.dumps({'snapshots': record['snapshots'], 'budget': record['budget'], 'card_bytes': total}, indent=2))


if __name__ == '__main__':
    main()
