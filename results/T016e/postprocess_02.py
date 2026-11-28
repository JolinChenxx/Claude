"""Run serial postprocessing for three completed T016e iterations."""
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
RUN = Path('D:/sim_runs/T016e/low_trap_G0/it0')
TABLES = 'D:/sim_runs/tables/v1c'
OUT = Path('D:/sim_runs/T016e_post_02')
OUT.mkdir(parents=True, exist_ok=True)
commands = [
    [sys.executable, str(REPO / 'scripts/diag_emission.py'),
     'D:/sim_runs/T016e/low_thin_G0/it0', '--tables', TABLES,
     '--times', '300', '500', '700', '1000', '--filter', '510', '12',
     '--out', str(OUT / 'low_thin_G0_it0_emission.csv')],
]
for member, iteration, series in [('low_trap_G0', 'it0', 'low'),
                                  ('low_thin_G0', 'it0', 'low'),
                                  ('high_trap_G0', 'it1', 'high')]:
    run = Path('D:/sim_runs/T016e') / member / iteration
    commands.append([sys.executable, '-m', 'lipsim.image', str(run),
                     '--series', series, '--tables', TABLES,
                     '--out', str(run / 'image'), '--workers', '8'])
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
