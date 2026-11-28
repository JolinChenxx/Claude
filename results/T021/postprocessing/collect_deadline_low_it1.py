"""Deliver already-computed low-it1 frames requested by the 21:00 handoff."""
from pathlib import Path
from datetime import datetime
import hashlib
import json
import os
import shutil
import subprocess
import time
import numpy as np
import pandas as pd

REPO = Path(__file__).resolve().parents[3]
RAW = Path('D:/sim_runs/T021/low_thin_G0/it1/image_v2')
OUT = REPO / 'results/T021/low_thin_G0/it1'
PYTHON = 'D:/sim_tools/envs/lipsim/python.exe'


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    image = OUT / 'image_v2'
    prior = json.loads((image / 'validation.json').read_text())
    assert prior['command_evidence']['returncode'] == 0
    job = Path('D:/sim_runs/T021_post_completed_02')
    summary = json.loads((job / 'postprocess_summary.json').read_text())
    assert summary['state'] == 'completed'
    assert (job / 'exitcode.txt').read_text().strip() == '0'
    expected = {r['file']: r for r in prior['files']}
    records = []
    for source in sorted(RAW.iterdir()):
        assert source.is_file() and source.name in expected
        digest = sha(source)
        assert digest == expected[source.name]['sha256']
        if source.suffix == '.npz':
            with np.load(source) as arrays:
                assert set(arrays.files) == {'t_ns', 'radiance', 'um_per_px'}
                assert list(arrays['t_ns']) == prior['expected_times_ns']
                assert list(arrays['radiance'].shape) == expected[source.name]['radiance_shape']
                assert all(np.isfinite(arrays[k]).all() for k in arrays.files)
        target = image / source.name
        if target.exists():
            assert sha(target) == digest
        else:
            shutil.copyfile(source, target)
        assert sha(target) == sha(source) == digest
        records.append({'file': source.name, 'bytes': source.stat().st_size, 'sha256': digest})
    peak = OUT / 'peak_lift.csv'
    log = OUT / 'peak_lift.log'
    receipt = OUT / 'peak_lift_execution.json'
    command = [PYTHON, str(REPO / 'scripts/peak_lift.py'), str(RAW), '--series', 'low', '--out', str(peak)]
    env = dict(os.environ, PYTHONPATH=str(REPO / 'src'), PYTHONUTF8='1', OMP_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1')
    if peak.exists():
        execution = json.loads(receipt.read_text())
        assert execution['returncode'] == 0
        assert execution['sha256'] == sha(peak) and execution['log_sha256'] == sha(log)
    else:
        assert not log.exists() and not receipt.exists()
        started = datetime.now().astimezone().isoformat()
        begin = time.perf_counter()
        with log.open('wb') as stream:
            rc = subprocess.run(command, cwd=REPO, env=env, stdout=stream, stderr=subprocess.STDOUT).returncode
        execution = {'command': command, 'started': started, 'seconds': time.perf_counter() - begin,
                     'returncode': rc, 'sha256': sha(peak), 'log_sha256': sha(log)}
        receipt.write_text(json.dumps(execution, indent=2) + '\n', encoding='utf-8')
        assert rc == 0
    df = pd.read_csv(peak)
    exp = pd.read_csv(REPO / 'results/surface/surface_rows.csv')
    exp = exp.loc[exp.series == 'low'].groupby('t_ns', as_index=False).mean(numeric_only=True)
    times = exp.t_ns.tolist()
    assert df.t_ns.tolist() == times
    assert not np.isinf(df.to_numpy(dtype=float)).any()
    assert np.isfinite(df[['model_lift_thin_um', 'model_lift_sa_um']].to_numpy()).all()
    for dest, source in [('exp_lift_min_um', 'lower50_row'), ('exp_lift_max_um', 'lower10_row')]:
        original = (exp[source] - exp.peak_row) * 9.244
        assert np.allclose(df[dest], original, atol=0.0500001, rtol=0, equal_nan=True)
    missing = [{'t_ns': int(row.t_ns), 'field': key} for _, row in df.iterrows() for key in df.columns if pd.isna(row[key])]
    evidence = {'recorded': datetime.now().astimezone().isoformat(),
                'instruction': 'handoff/to_codex/deadline-2100-full-comparison.md',
                'original_image_validation': 'image_v2/validation.json',
                'source': str(RAW), 'files': records,
                'peak_lift': {**execution, 'rows': len(df), 'source_missing_values_preserved': missing},
                'policy': 'All six original compressed synthesized frame files delivered, with the full original image_v2 directory. No imaging rerun, raw solver-field copy, parameter change, or NPZ Syncthing copy.'}
    (OUT / 'deadline_delivery.json').write_text(json.dumps(evidence, indent=2) + '\n', encoding='utf-8')
    total = sum(p.stat().st_size for p in (REPO / 'results/T021').rglob('*') if p.is_file())
    assert total < 20 * 1024**2
    print(json.dumps({'files': len(records), 'npz_files': sum(r['file'].endswith('.npz') for r in records), 'peak_lift': evidence['peak_lift'], 'card_bytes': total}))


if __name__ == '__main__':
    main()
