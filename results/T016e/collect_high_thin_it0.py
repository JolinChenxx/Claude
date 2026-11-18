"""Collect immutable evidence for the completed high_thin_G0/it0 run."""
from pathlib import Path
import csv
import datetime
import hashlib
import json
import shutil

REPO = Path(__file__).resolve().parents[2]
RUN = Path('D:/sim_runs/T016e/high_thin_G0/it0')
OUT = Path(__file__).resolve().parent / 'high_thin_G0' / 'it0'
OUT.mkdir(parents=True, exist_ok=True)
with (REPO / 'results/run_registry.csv').open(newline='') as stream:
    records = [r for r in csv.DictReader(stream) if r['batch'] == 'T016e'
               and r['run'] == 'high_thin_G0' and r['picard_it'] == '0']
assert len(records) == 1 and records[0]['returncode'] == '0', records
with (RUN / 'pluto.0.log').open('rb') as stream:
    stream.seek(-2000, 2)
    assert b'> Done' in stream.read()
logs = sorted(RUN.glob('pluto.*.log'), key=lambda p: int(p.name.split('.')[1]))
assert [int(p.name.split('.')[1]) for p in logs] == list(range(8))
counts = []
for log in logs:
    digest = hashlib.sha256()
    n = 0
    samples = []
    carry = b''
    before = log.stat()
    with log.open('rb') as stream:
        while chunk := stream.read(8 * 1024 * 1024):
            digest.update(chunk)
            lines = (carry + chunk).split(b'\n')
            carry = lines.pop()
            for line in lines:
                if b'ConsToPrim' in line:
                    n += 1
                    if len(samples) < 3:
                        samples.append(line.decode('utf-8', errors='replace'))
        if b'ConsToPrim' in carry:
            n += 1
            if len(samples) < 3:
                samples.append(carry.decode('utf-8', errors='replace'))
    after = log.stat()
    assert (before.st_size, before.st_mtime_ns) == (after.st_size, after.st_mtime_ns)
    item = dict(rank=int(log.name.split('.')[1]), bytes=after.st_size,
                sha256=digest.hexdigest(), ConsToPrim_lines=n, first_samples=samples)
    counts.append(item)
    print(json.dumps(item), flush=True)
    with log.open('rb') as stream:
        stream.seek(max(0, after.st_size - 4096))
        tail = stream.read()
    (OUT / (log.name + '.tail.txt')).write_bytes(tail)

shutil.copy2(RUN / 'budget.csv', OUT / 'budget.csv')
shutil.copy2(RUN / 'source/source_meta.json', OUT / 'source_meta.json')
with (OUT / 'budget.csv').open(newline='') as stream:
    budget = list(csv.DictReader(stream))
last = {k: float(v) for k, v in budget[-1].items()}
source = json.loads((OUT / 'source_meta.json').read_text())
abs_model = source['budget_J']['abs']
abs_pluto = last['E_targ_abs']
summary = dict(collected=datetime.datetime.now().astimezone().isoformat(),
               run=str(RUN), registry_record=records[0], final_budget=last,
               target_model_abs_J=abs_model, PLUTO_E_targ_abs_J=abs_pluto,
               absorption_relative_difference=(abs_model-abs_pluto)/abs_pluto,
               M_Cu_relative_to_M_src=(last['M_Cu']-last['M_src'])/last['M_src'],
               total_ConsToPrim_lines=sum(c['ConsToPrim_lines'] for c in counts),
               all_rank_logs=counts)
(OUT / 'completion.json').write_text(json.dumps(summary, indent=2)+'\n', encoding='utf-8')
print(json.dumps({k:v for k,v in summary.items() if k!='all_rank_logs'}, indent=2))
