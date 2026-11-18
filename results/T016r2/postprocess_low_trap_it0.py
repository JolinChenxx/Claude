"""Run prescribed diagnostics and imaging for completed low_trap_G0/it0."""
from pathlib import Path
import datetime
import json
import subprocess
import sys
import ctypes

# Keep this supplementary analysis below production priority; children inherit it.
if not ctypes.windll.kernel32.SetPriorityClass(ctypes.windll.kernel32.GetCurrentProcess(), 0x4000):
    raise OSError('Unable to set BelowNormal priority')

REPO = Path(__file__).resolve().parents[2]
RUN = Path('D:/sim_runs/T016r2/low_trap_G0/it0')
TABLES = 'D:/sim_runs/tables/v1b'
OUT = Path('D:/sim_runs/T016r2_post_trap_01')
OUT.mkdir(parents=True, exist_ok=True)
commands = [
    [sys.executable, str(REPO / 'scripts/diag_emission.py'), str(RUN),
     '--tables', TABLES, '--times', '200', '300', '500', '700', '1000', '1200',
     '--filter', '510', '12', '--out', str(OUT / 'low_trap_G0_it0_emission.csv')],
    [sys.executable, '-m', 'lipsim.image', str(RUN), '--series', 'low',
     '--tables', TABLES, '--out', str(RUN / 'image'), '--workers', '8'],
]
records = []
for command in commands:
    print('Running:', command, flush=True)
    started = datetime.datetime.now().astimezone().isoformat()
    result = subprocess.run(command, cwd=REPO)
    records.append(dict(command=command, started=started, returncode=result.returncode,
                        finished=datetime.datetime.now().astimezone().isoformat()))
    (OUT / 'postprocess_summary.json').write_text(json.dumps(records, indent=2)+'\n')
    print('Return code:', result.returncode, flush=True)
raise SystemExit(0 if all(r['returncode'] == 0 for r in records) else 1)
