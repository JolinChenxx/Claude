"""Archive the authorized v1b shutdown and validate v1c before production."""
from pathlib import Path
import csv
import datetime
import hashlib
import io
import json
import shutil
import numpy as np
from lipsim.tables.build import read_bin

repo = Path(__file__).resolve().parents[2]
now = datetime.datetime.now().astimezone().isoformat()
def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def save(path, data):
    path.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')

for batch in ('T016h', 'T016r2'):
    dest = repo / 'results' / batch / 'stopped'
    stop = json.loads((dest / 'stop.json').read_text(encoding='utf-8-sig'))
    assert not stop['remaining']
    members = []
    for member in sorted(Path('D:/sim_runs', batch).glob('*_G*')):
        iteration = max(member.glob('it*'), key=lambda p: int(p.name[2:]))
        out = dest / member.name / iteration.name
        out.mkdir(parents=True, exist_ok=True)
        raw = (iteration / 'budget.csv').read_bytes()
        complete = raw[:raw.rfind(b'\n') + 1]
        rows = list(csv.DictReader(io.StringIO(complete.decode())))
        last = rows[-1]
        copied = {}
        for name in ('budget.csv', 'pluto.ini', 'dbl.out', 'source/source_meta.json'):
            p = iteration / name
            if p.exists():
                q = out / p.name
                shutil.copy2(p, q)
                copied[p.name] = sha(q)
        log = iteration / 'pluto.0.log'
        with log.open('rb') as stream:
            stream.seek(max(0, log.stat().st_size - 8192))
            (out / 'pluto.0.log.tail.txt').write_bytes(stream.read())
        members.append(dict(member=member.name, iteration=iteration.name, last_budget=last,
                            incomplete_trailing_bytes=len(raw)-len(complete),
                            raw_directory=str(iteration), copied_sha256=copied,
                            opt_sha256=sha(iteration / 'lip_tables/opt.bin')))
    save(dest / 'summary.json', dict(recorded=now, stopped_at=stop['stopped_at'],
         reason=stop['reason'], data_deleted=False, members=members))
    print(batch, [(m['member'], m['iteration'], m['last_budget']['t_ns']) for m in members])

tables = Path('D:/sim_runs/tables/v1c')
out = repo / 'results/T016e'
manifest = json.loads((tables / 'manifest.json').read_text())
assert manifest['diff_model'] == 'v2'
validation = dict(validated=now, diff_model=manifest['diff_model'], files={})
for name, info in manifest['files'].items():
    path = tables / name
    digest = sha(path)
    assert digest == info['sha256']
    axes, fields = read_bin(path)
    assert tuple(map(len, axes.values())) == (81, 145, 14)
    assert all(np.isfinite(a).all() for a in axes.values())
    assert list(fields) == info['fields']
    assert all(a.shape == (81, 145, 14) and np.isfinite(a).all() for a in fields.values())
    if name == 'opt.bin':
        assert {'D_cuair', 'dh_dY'} <= fields.keys()
    validation['files'][name] = dict(sha256=digest, shape=[81,145,14],
        fields={k: dict(min=float(a.min()), max=float(a.max()), finite=True) for k,a in fields.items()})
job = Path('D:/sim_runs/T016e_tables')
assert (job / 'exitcode.txt').read_text().strip() == '0'
logs = out / 'tables_completion'
logs.mkdir(exist_ok=True)
for name in ('job.json', 'exitcode.txt', 'stdout.log', 'stderr.log'):
    shutil.copy2(job / name, logs / name)
shutil.copy2(tables / 'manifest.json', out / 'manifest.json')
save(out / 'table_validation.json', validation)
print('v1c: all binary shapes, finite axes/fields, manifest hashes and rc=0 verified')
