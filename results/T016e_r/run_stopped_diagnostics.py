"""Diagnose preserved low-G1 snapshots after the authorized stop."""
from pathlib import Path
import datetime
import json
import os
import subprocess
import sys

repo = Path(__file__).resolve().parents[2]
run = Path('D:/sim_runs/T016e_r/low_thin_G1/it0')
out = Path('D:/sim_runs/T016e_r_stopped_diag_01')
out.mkdir(parents=True, exist_ok=True)
prefix = Path(sys.executable).parent
env = os.environ.copy()
env['PATH'] = os.pathsep.join([str(prefix), str(prefix/'Library/bin'),
                             str(prefix/'Scripts'), env.get('PATH', '')])
env['PYTHONPATH'] = str(repo/'src')
env.update(OMP_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1', PYTHONUTF8='1')
commands = [
    [sys.executable, 'scripts/shock_track.py', str(run), '--out', str(out/'shock_track.csv')],
    [sys.executable, 'scripts/core_energy.py', str(run), '--tables', 'D:/sim_runs/tables/v1c',
     '--nec', 'thin', '--out', str(out/'core_energy.csv')],
]
records = []
for command in commands:
    entry = dict(command=command, started=datetime.datetime.now().astimezone().isoformat())
    print('Running:', command, flush=True)
    result = subprocess.run(command, cwd=repo, env=env)
    entry.update(returncode=result.returncode, finished=datetime.datetime.now().astimezone().isoformat())
    records.append(entry)
    (out/'diagnostics_summary.json').write_text(json.dumps(records, indent=2)+'\n')
    print('Return code:', result.returncode, flush=True)
raise SystemExit(0 if all(r['returncode'] == 0 for r in records) else 1)
