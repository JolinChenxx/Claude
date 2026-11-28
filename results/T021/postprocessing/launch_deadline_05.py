"""Apply Claude's SMT/production-priority instruction before queue05 spawns."""
import ctypes
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import runpy
import struct

import psutil


PREFIX = Path('D:/sim_tools/envs/lipsim')
os.environ['PATH'] = os.pathsep.join([str(PREFIX), str(PREFIX/'Library/bin'), str(PREFIX/'Scripts'), os.environ.get('PATH', '')])
HERE = Path(__file__).resolve().parent
JOB = Path('D:/sim_runs/T021_T023_post_05')
kernel = ctypes.WinDLL('kernel32', use_last_error=True)
kernel.GetCurrentThread.restype = ctypes.c_void_p
kernel.GetThreadGroupAffinity.argtypes = [ctypes.c_void_p, ctypes.c_void_p]
affinity = ctypes.create_string_buffer(16)
assert kernel.GetThreadGroupAffinity(kernel.GetCurrentThread(), affinity)
group = struct.unpack_from('<H', affinity.raw, 8)[0]
size = ctypes.c_ulong()
kernel.GetLogicalProcessorInformationEx(0, None, ctypes.byref(size))
buffer = ctypes.create_string_buffer(size.value)
assert kernel.GetLogicalProcessorInformationEx(0, buffer, ctypes.byref(size))
offset = 0
siblings = []
while offset < size.value:
    relation, length = struct.unpack_from('<II', buffer.raw, offset)
    count = struct.unpack_from('<H', buffer.raw, offset + 30)[0]
    for index in range(count):
        mask, item_group = struct.unpack_from('<QH', buffer.raw, offset + 32 + 16 * index)
        logical = [i for i in range(64) if mask >> i & 1]
        if item_group == group and len(logical) == 2:
            siblings.append(logical[1])
    offset += length
selected = sorted(siblings)[:24]
assert len(selected) == 24, 'Expected 24 SMT sibling lanes in the current processor group'
process = psutil.Process()
process.cpu_affinity(selected)
process.nice(psutil.BELOW_NORMAL_PRIORITY_CLASS)
assert process.cpu_affinity() == selected
receipt = {
    'recorded': datetime.now().astimezone().isoformat(),
    'pid': process.pid, 'creation_epoch': process.create_time(),
    'processor_group': group, 'logical_processors': selected,
    'priority': 'BelowNormal',
    'runtime_path_prefix': [str(PREFIX),str(PREFIX/'Library/bin'),str(PREFIX/'Scripts')],
    'launcher_sha256': hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
    'note': '24 secondary SMT lanes; child processes inherit affinity and priority. Production affinities unchanged; no exclusive-core claim.',
}
(JOB / 'affinity_launch.json').write_text(json.dumps(receipt, indent=2) + '\n', encoding='utf-8')
print(json.dumps(receipt), flush=True)
runpy.run_path(str(HERE / 'run_deadline_05.py'), run_name='__main__')
