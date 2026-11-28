"""Check actual initial production inputs without waiting for solver completion."""
from pathlib import Path
import datetime
import hashlib
import json
import re
import csv
import yaml

repo = Path(__file__).resolve().parents[2]
out = repo / 'results/T016e'
batch = yaml.safe_load((repo / 'configs/batches/T016e.yaml').read_text())
manifest = json.loads((out / 'manifest.json').read_text())
job = json.loads((out / 'production_job_start.json').read_text(encoding='utf-8-sig'))
checks = []
for spec in batch['numerics']['runs']:
    run = Path(batch['root']) / spec['id'] / 'it0'
    ini = run / 'pluto.ini'
    entry = dict(member=spec['id'], procs=spec['procs'], ini_exists=ini.exists(), tables={})
    if ini.exists():
        entry['rmax_par'] = float(re.search(r'^rmax_par\s+(\S+)', ini.read_text(), re.M)[1])
        assert entry['rmax_par'] == 1000
        entry['ini_sha256'] = hashlib.sha256(ini.read_bytes()).hexdigest()
    for name in ('eos.bin', 'opt.bin'):
        path = run / 'lip_tables' / name
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        assert digest == manifest['files'][name]['sha256']
        entry['tables'][name] = digest
    budget = run / 'budget.csv'
    if budget.exists():
        raw = budget.read_bytes()
        rows = list(csv.DictReader(raw[:raw.rfind(b'\n')+1].decode().splitlines()))
        entry['t_ns'] = rows[-1]['t_ns'] if rows else None
    log = run / 'pluto.0.log'
    if log.exists():
        with log.open('rb') as stream:
            stream.seek(max(0, log.stat().st_size - 16384))
            tail = stream.read().decode(errors='replace')
        steps = re.findall(r'step:\s*(\d+).*?t\s*=\s*(\S+).*?dt\s*=\s*(\S+)(.*?)(?=step:|\Z)', tail, re.S)
        for step,t,dt,rest in reversed(steps):
            nrkl = re.search(r'Nrkl\s*=\s*(\d+)', rest)
            if nrkl:
                entry['latest_log'] = dict(step=int(step), t_ns=t, dt=dt, Nrkl=int(nrkl[1]))
                break
    checks.append(entry)
result = dict(checked=datetime.datetime.now().astimezone().isoformat(), job=job,
              max_cores=64, configured_ranks=sum(s['procs'] for s in batch['numerics']['runs']),
              all_initial_inis_verified=all(c['ini_exists'] for c in checks), members=checks)
(out / 'production_launch_checks.json').write_text(json.dumps(result, indent=2)+'\n')
print(json.dumps(result, indent=2))
