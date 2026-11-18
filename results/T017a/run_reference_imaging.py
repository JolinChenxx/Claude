"""Run T016e reference imaging as an independent job.ps1 background job."""
from pathlib import Path
import ctypes
from ctypes import wintypes
import datetime
import hashlib
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
out = Path('D:/sim_runs/T017a_image_v2_01')
out.mkdir(parents=True, exist_ok=True)
state = out / 'stage.json'
if state.exists():
    previous = json.loads(state.read_text())
    if previous['state'] == 'finished':
        print('T017a reference stage already completed', flush=True)
        raise SystemExit(0)
    raise RuntimeError('T017a stage incomplete or failed; inspect stage.json before retry')

def now():
    return datetime.datetime.now().astimezone().isoformat()

record = dict(state='running', started=now(), label='reference imaging v2',
              authority='3f5e08b / T017a_now.md',
              job='T017a_image_v2_01',
              commit=subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=repo, text=True).strip(),
              inputs={name:hashlib.sha256((repo/name).read_bytes()).hexdigest()
                      for name in ('configs/imaging/v2.yaml', 'data/params/registry.yaml',
                                   'src/lipsim/image/forward.py')}, commands=[])
state.write_text(json.dumps(record, indent=2)+'\n')
try:
    for series in ('high', 'low'):
        command = [sys.executable, str(repo/'scripts/image_members.py'),
                   'D:/sim_runs/T016e', '--series', series,
                   '--tables', 'D:/sim_runs/tables/v1c', '--workers', '16',
                   '--scenarios', 'configs/imaging/v2.yaml', '--outname', 'image_v2']
        entry = dict(series=series, command=command, started=now())
        print('T017a reference', series, flush=True)
        result = subprocess.run(command, cwd=repo)
        entry.update(finished=now(), returncode=result.returncode)
        record['commands'].append(entry)
        state.write_text(json.dumps(record, indent=2)+'\n')
        if result.returncode:
            raise RuntimeError(f'T017a {series} failed: {result.returncode}')
    record.update(state='finished', finished=now())
except BaseException as exc:
    record.update(state='failed', finished=now(), error=str(exc))
    raise
finally:
    state.write_text(json.dumps(record, indent=2)+'\n')
