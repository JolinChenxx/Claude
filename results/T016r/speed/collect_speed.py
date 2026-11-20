"""Read-only snapshots of the latest 20 complete step/Mach pairs.

Use progress.json for subsequent STATUS entries: t_ns is the latest budget,
while dt and Nrkl come from the latest logged step (log_t_ns).
"""
from pathlib import Path
import datetime
import json
import mmap
import re

OUT = Path(__file__).resolve().parent
MEMBERS = {
    'T016': ['high_thin_G0', 'high_trap_G0', 'high_thin_G1'],
    'T016r': ['low_thin_G0', 'low_trap_G0', 'low_thin_G1'],
}
progress = []
for batch, members in MEMBERS.items():
    for member in members:
        root = Path('D:/sim_runs') / batch / member
        iterations = sorted(root.glob('it[0-9]*'), key=lambda p: int(p.name[2:]))
        run = iterations[-1]
        log = run / 'pluto.0.log'
        pairs = []
        with log.open('rb') as stream:
            size = log.stat().st_size
            with mmap.mmap(stream.fileno(), size, access=mmap.ACCESS_READ) as data:
                end = size
                while len(pairs) < 20:
                    at = data.rfind(b'\nstep:', 0, end)
                    if at < 0:
                        at = 0 if end and data[:5] == b'step:' else -1
                        if at < 0:
                            break
                        start = 0
                    else:
                        start = at + 1
                    end = at
                    first = data.find(b'\n', start, size)
                    second = data.find(b'\n', first + 1, size) if first >= 0 else -1
                    if second >= 0:
                        pair = data[start:second + 1]
                        if re.search(rb'\[Mach\s*=.*Nrkl\s*=', pair):
                            pairs.append(pair)
                    if at == 0:
                        break
        pairs.reverse()
        (OUT / f'{batch}_{member}.txt').write_bytes(b''.join(pairs))
        latest = pairs[-1].decode('ascii') if pairs else ''
        step = re.search(r'step:(\d+);\s*t\s*=\s*([^;]+);\s*dt\s*=\s*([^;]+);', latest)
        nrkl = re.search(r'Nrkl\s*=\s*(\d+)', latest)
        budget = run / 'budget.csv'
        with budget.open('rb') as stream:
            stream.seek(max(0, budget.stat().st_size - 4096))
            complete = stream.read().split(b'\n')[:-1]
        row = next(line for line in reversed(complete) if line.strip())
        item = dict(batch=batch, member=member, iteration=run.name,
                    t_ns=row.decode().split(',')[0],
                    log_t_ns=step.group(2).strip() if step else None,
                    dt=step.group(3).strip() if step else None,
                    Nrkl=int(nrkl.group(1)) if nrkl else None,
                    last_logged_step=int(step.group(1)) if step else None,
                    pairs=len(pairs), source=str(log), snapshot_bytes=size)
        assert len(pairs) == 20 and step and nrkl, item
        progress.append(item)
result = dict(collected=datetime.datetime.now().astimezone().isoformat(), members=progress)
(OUT / 'progress.json').write_text(json.dumps(result, indent=2) + '\n')
print(json.dumps(result, indent=2))
