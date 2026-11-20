"""Run prescribed diagnostics and imaging for completed high_thin_G0/it2."""
from pathlib import Path
import datetime
import json
import subprocess
import sys
import ctypes
from ctypes import wintypes

# Keep this supplementary analysis below production priority; children inherit it.
kernel32 = ctypes.WinDLL('kernel32', use_last_error=True)
kernel32.GetCurrentProcess.restype = wintypes.HANDLE
kernel32.SetPriorityClass.argtypes = [wintypes.HANDLE, wintypes.DWORD]
kernel32.SetPriorityClass.restype = wintypes.BOOL
if not kernel32.SetPriorityClass(kernel32.GetCurrentProcess(), 0x4000):
    raise ctypes.WinError(ctypes.get_last_error())

REPO = Path(__file__).resolve().parents[2]
RUN = Path('D:/sim_runs/T016e/high_thin_G0/it2')
TABLES = 'D:/sim_runs/tables/v1c'
OUT = Path('D:/sim_runs/T016e_post_high_thin_03')
OUT.mkdir(parents=True, exist_ok=True)
commands = [
    [sys.executable, str(REPO / 'scripts/diag_emission.py'), str(RUN),
     '--tables', TABLES, '--times', '200', '300', '500', '700',
     '--filter', '510', '12', '--out', str(OUT / 'high_thin_G0_it2_emission.csv')],
    [sys.executable, '-m', 'lipsim.image', str(RUN), '--series', 'high',
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
