"""Image the newly finished it2 with the prescribed original imaging defaults."""
import ctypes
from ctypes import wintypes
from pathlib import Path
import subprocess
import sys

k = ctypes.WinDLL('kernel32', use_last_error=True)
k.GetCurrentProcess.restype = wintypes.HANDLE
k.SetPriorityClass.argtypes = [wintypes.HANDLE, wintypes.DWORD]
k.SetPriorityClass.restype = wintypes.BOOL
if not k.SetPriorityClass(k.GetCurrentProcess(), 0x4000):
    raise ctypes.WinError(ctypes.get_last_error())
run = Path('D:/sim_runs/T016e/high_trap_G0/it2')
command = [sys.executable, '-m', 'lipsim.image', str(run), '--series', 'high',
           '--tables', 'D:/sim_runs/tables/v1c', '--out', str(run/'image'),
           '--workers', '8']
print('Running:', command, flush=True)
result = subprocess.run(command, cwd=Path(__file__).resolve().parents[2])
print('rc', result.returncode, run, flush=True)
raise SystemExit(result.returncode)
