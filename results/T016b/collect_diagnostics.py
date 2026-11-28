"""Read completed diagnostic runs; preserve tails and exact error text."""
from pathlib import Path
from collections import deque
import hashlib
import json
import shutil

OUT = Path(__file__).resolve().parent
RAW = Path('D:/sim_runs/T016b')
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
preflight = json.loads((OUT / 'preflight.json').read_text())
summaries = []
for copy in preflight['copies']:
    member = copy['member']
    src, dst = RAW / member, OUT / member
    dst.mkdir(exist_ok=True)
    run = json.loads((src / 'diagnostic_run.json').read_text())
    assert run['state'] in ('finished', 'time_limit')
    for name in ['diagnostic_run.json', 'mpi_output.log', 'dbl.out']:
        shutil.copy2(src / name, dst / name)
        assert sha(src / name) == sha(dst / name)
    logs = sorted(src.glob('pluto.[0-9]*.log'))
    assert {p.name for p in logs} == {f'pluto.{i}.log' for i in range(8)}
    log_records = []
    for log in logs:
        tail, errors = deque(maxlen=40), deque(maxlen=200)
        counts = dict(lines=0, matching_errors=0, ConsToPrim=0)
        digest = hashlib.sha256()
        for n, line in enumerate(log.open('rb'), 1):
            digest.update(line)
            tail.append(line)
            counts['lines'] += 1
            counts['ConsToPrim'] += int(b'ConsToPrim' in line)
            if line.startswith(b'!') and b'ConsToPrim: rhoe < 0' not in line:
                errors.append(str(n).encode() + b': ' + line)
                counts['matching_errors'] += 1
        (dst / (log.stem + '.tail.log')).write_bytes(b''.join(tail))
        (dst / (log.stem + '.errors.log')).write_bytes(b''.join(errors))
        log_records.append(dict(file=log.name, sha256=digest.hexdigest(),
                                bytes=log.stat().st_size, **counts,
                                kept_errors=len(errors)))
    budget = src / 'budget.csv'
    budget_lines = budget.read_bytes().splitlines(keepends=True) if budget.exists() else []
    (dst / 'budget.tail.csv').write_bytes(b''.join(budget_lines[-10:]))
    output_lines = (src / 'dbl.out').read_text().splitlines()
    last = output_lines[-1].split()
    record = dict(member=member, returncode=run['returncode'],
                  timed_out=run['timed_out'], seconds=run['seconds'],
                  last_output=dict(file=f'data.{int(last[0]):04d}.dbl',
                                   step=int(last[3]), t_ns=float(last[1]),
                                   record=output_lines[-1]),
                  last_budget_row=budget_lines[-1].decode().strip() if budget_lines else None,
                  budget_rows=len(budget_lines), budget_has_header=False,
                  rank_logs=log_records)
    # The two original failed runs are static; verify all selected originals,
    # and the copied ini/tables. Restart/output metadata may be rewritten by PLUTO.
    record['original_inputs_unchanged'] = all(
        sha(Path(copy['source']) / name) == expected
        for name, expected in copy['files_sha256'].items())
    record['copied_ini_tables_unchanged'] = all(
        sha(src / name) == expected
        for name, expected in copy['files_sha256'].items()
        if name == 'pluto.ini' or name.startswith('lip_tables'))
    assert record['original_inputs_unchanged'] and record['copied_ini_tables_unchanged']
    (dst / 'summary.json').write_text(json.dumps(record, indent=2) + '\n')
    summaries.append(record)
for name in ['diagnostic_results.json', 'job.json', 'exitcode.txt', 'stdout.log', 'stderr.log']:
    shutil.copy2(RAW / name, OUT / name)
(OUT / 'diagnostic_summary.json').write_text(json.dumps(summaries, indent=2) + '\n')
assert sum(p.stat().st_size for p in OUT.rglob('*') if p.is_file()) <= 20_000_000
print(json.dumps([{k: v for k, v in r.items() if k != 'rank_logs'} for r in summaries], indent=2))
