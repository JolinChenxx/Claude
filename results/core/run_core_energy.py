"""Sequential read-only diagnostics requested by Claude in 9a3ea07."""
from pathlib import Path
import datetime
import hashlib
import json
import subprocess
import sys

repo = Path(__file__).resolve().parents[2]
out = repo / 'results/core'
records = []
cases = [('T016e', 'high_thin_G0', 'it1', 'v1c', 'thin'),
         ('T016e', 'high_trap_G0', 'it1', 'v1c', 'trap'),
         ('T016e', 'low_thin_G0', 'it0', 'v1c', 'thin'),
         ('T016e', 'low_trap_G0', 'it0', 'v1c', 'trap'),
         ('T015', 'high_trap_G0', 'it2', 'v1', 'trap')]
for batch, member, iteration, table, nec in cases:
    stem = f'{member}_{iteration}' if batch == 'T016e' else f'{batch}_{member}_{iteration}'
    csv = out / f'{stem}.csv'
    command = [sys.executable, str(repo / 'scripts/core_energy.py'),
               f'D:/sim_runs/{batch}/{member}/{iteration}', '--tables',
               f'D:/sim_runs/tables/{table}', '--nec', nec, '--every', '4',
               '--out', str(csv)]
    record = dict(batch=batch, member=member, iteration=iteration, command=command,
                  started=datetime.datetime.now().astimezone().isoformat())
    print('Running', command, flush=True)
    with (out / f'{stem}.log').open('w', encoding='utf-8') as log:
        result = subprocess.run(command, cwd=repo, stdout=log, stderr=subprocess.STDOUT)
    record.update(returncode=result.returncode,
                  finished=datetime.datetime.now().astimezone().isoformat())
    if csv.exists():
        record['csv_sha256'] = hashlib.sha256(csv.read_bytes()).hexdigest()
    records.append(record)
    (out / 'batch_summary.json').write_text(json.dumps(records, indent=2)+'\n')
    print('rc', result.returncode, stem, flush=True)
raise SystemExit(0 if all(r['returncode'] == 0 for r in records) else 1)
