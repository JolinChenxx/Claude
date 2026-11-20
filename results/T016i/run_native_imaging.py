"""Run the prescribed native-grid imaging batches sequentially at low priority."""
from pathlib import Path
import ctypes
from ctypes import wintypes
import datetime
import json
import subprocess
import sys

kernel32 = ctypes.WinDLL('kernel32', use_last_error=True)
kernel32.GetCurrentProcess.restype = wintypes.HANDLE
kernel32.SetPriorityClass.argtypes = [wintypes.HANDLE, wintypes.DWORD]
kernel32.SetPriorityClass.restype = wintypes.BOOL
if not kernel32.SetPriorityClass(kernel32.GetCurrentProcess(), 0x4000):
    raise ctypes.WinError(ctypes.get_last_error())

repo = Path(__file__).resolve().parents[2]
out = Path('D:/sim_runs/T016i_native_01')
out.mkdir(parents=True, exist_ok=True)
records = []
for batch, table in [('T016e', 'v1c'), ('T015', 'v1'), ('T016r2', 'v1b'),
                     ('T016h', 'v1b'), ('T014', 'v0')]:
    for series in ('high', 'low'):
        command = [sys.executable, str(repo / 'scripts/image_members.py'),
                   f'D:/sim_runs/{batch}', '--series', series,
                   '--tables', f'D:/sim_runs/tables/{table}', '--workers', '16',
                   '--scenarios', 'configs/imaging/v1.yaml', '--outname', 'image_v1']
        started = datetime.datetime.now().astimezone().isoformat()
        print('Running:', command, flush=True)
        result = subprocess.run(command, cwd=repo)
        records.append(dict(batch=batch, series=series, command=command, started=started,
                            finished=datetime.datetime.now().astimezone().isoformat(),
                            returncode=result.returncode))
        (out / 'batch_summary.json').write_text(json.dumps(records, indent=2)+'\n')
        print('Return code:', result.returncode, batch, series, flush=True)
raise SystemExit(0 if all(r['returncode'] == 0 for r in records) else 1)
