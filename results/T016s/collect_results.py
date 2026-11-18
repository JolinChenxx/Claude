"""Collect the fixed-duration A/B results without changing raw solver files."""
from pathlib import Path
import datetime
import hashlib
import json
import re
import shutil

OUT = Path(__file__).resolve().parent
RAW = Path('D:/sim_runs/T016s')


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def copy_exact(source, target):
    shutil.copy2(source, target)
    expected = sha(source)
    assert sha(target) == expected
    return expected


def main():
    preflight = json.loads((OUT / 'preflight.json').read_text(encoding='utf-8'))
    restart_time = preflight['restart_time_ns']
    exe = Path(preflight['executable'])
    assert sha(exe) == preflight['executable_sha256']
    # The restarted analysis writes rows without a header. Preserve row bytes,
    # and record the column definitions from the source run separately.
    with (Path(preflight['source']) / 'budget.csv').open('rb') as stream:
        columns = stream.readline().decode('ascii').strip().split(',')
    assert columns[0] == 't_ns' and len(columns) == 14
    (OUT / 'budget_columns.json').write_text(json.dumps(columns, indent=2) + '\n')
    members = []
    outputs = {}
    for member in ['A', 'B']:
        src, dst = RAW / member, OUT / member
        run = json.loads((src / 'diagnostic_run.json').read_text(encoding='utf-8'))
        assert run['state'] == 'time_limit' and run['timed_out']
        assert run['taskkill_returncode'] == 0 and run['limit_seconds'] == 2400
        copies = {}
        for name in ['diagnostic_run.json', 'mpi_output.log', 'dbl.out', 'grid.out', 'pluto.ini']:
            copies[name] = copy_exact(src / name, dst / name)
        raw = (src / 'budget.csv').read_bytes()
        complete = raw[:raw.rfind(b'\n') + 1]
        selected = []
        for line in complete.splitlines(keepends=True):
            values = line.decode('ascii').strip().split(',')
            assert len(values) == len(columns)
            if float(values[0]) > restart_time:
                selected.append(line)
        assert selected
        (dst / 'budget.csv').write_bytes(b''.join(selected))
        last_budget = dict(zip(columns, selected[-1].decode('ascii').strip().split(',')))
        pairs = []
        lines = (src / 'pluto.0.log').read_bytes().splitlines(keepends=True)
        for first, second in zip(lines, lines[1:]):
            if first.startswith(b'step:') and re.search(rb'\[Mach.*Nrkl\s*=', second):
                pairs.append(first + second)
        assert pairs
        (dst / 'last30_step_nrkl.log').write_bytes(b''.join(pairs[-30:]))
        ranks = sorted(src.glob('pluto.[0-9]*.log'))
        assert {p.name for p in ranks} == {f'pluto.{i}.log' for i in range(8)}
        rank_records = []
        totals = dict(ConsToPrim=0, negative_E=0, other_bang=0, all_bang=0)
        for log in ranks:
            data = log.read_bytes()
            counts = dict(ConsToPrim=0, negative_E=0, other_bang=0, all_bang=0)
            errors = []
            for number, line in enumerate(data.splitlines(keepends=True), 1):
                ctp = b'ConsToPrim' in line
                negative = bool(re.search(rb'E\s*<\s*0', line))
                bang = b'!' in line
                counts['ConsToPrim'] += int(ctp)
                counts['negative_E'] += int(negative)
                counts['all_bang'] += int(bang)
                counts['other_bang'] += int(bang and not ctp and not negative)
                if ctp or negative or bang:
                    errors.append(str(number).encode() + b': ' + line)
            for name in totals:
                totals[name] += counts[name]
            digest = copy_exact(log, dst / log.name)
            (dst / (log.stem + '.errors.log')).write_bytes(b''.join(errors))
            rank_records.append(dict(file=log.name, bytes=len(data), sha256=digest, counts=counts))
        initial = next(item for item in preflight['copies'] if item['member'] == member)
        immutable = {name: sha(src / name) == expected
                     for name, expected in initial['hashes'].items()
                     if name == 'pluto.ini' or name.startswith('lip_tables/') or name.startswith('data.')}
        assert all(immutable.values())
        output_rows = {}
        for row in (src / 'dbl.out').read_text(encoding='ascii').splitlines():
            fields = row.split()
            index = int(fields[0])
            path = src / f'data.{index:04d}.dbl'
            if path.exists() and path.stat().st_size == preflight['checkpoint_bytes']:
                output_rows[index] = dict(index=index, t_ns=float(fields[1]),
                                          step=int(fields[3]), variables=fields[6:], record=row)
        outputs[member] = output_rows
        record = dict(member=member, rmax_par=initial['rmax_par'],
                      returncode=run['returncode'], termination='authorized 2400-second limit',
                      started=run['started'], ended=run['ended'], seconds=run['seconds'],
                      restart_time_ns=restart_time, last_budget=last_budget,
                      elapsed_simulation_ns=float(last_budget['t_ns']) - restart_time,
                      budget_rows_after_restart=len(selected), budget_has_header=False,
                      raw_budget_sha256=sha(src / 'budget.csv'),
                      copied_budget_sha256=sha(dst / 'budget.csv'),
                      incomplete_budget_trailing_bytes=len(raw) - len(complete),
                      requested_step_pairs=30, available_step_pairs=len(pairs),
                      collected_step_pairs=min(30, len(pairs)),
                      last_logged_pair=pairs[-1].decode('ascii').strip(),
                      latest_complete_output=output_rows[max(output_rows)],
                      all_rank_warning_counts=totals, rank_logs=rank_records,
                      immutable_inputs_verified=immutable, copied_sha256=copies)
        members.append(record)
    common = max(set(outputs['A']) & set(outputs['B']))
    assert common > preflight['restart']
    common_records = []
    for member in ['A', 'B']:
        filename = f'data.{common:04d}.dbl'
        source = RAW / member / filename
        item = dict(member=member, **outputs[member][common], raw_path=str(source),
                    bytes=source.stat().st_size, sha256=sha(source))
        if item['bytes'] < 20_000_000:
            copy_exact(source, OUT / member / filename)
            item['copied_to'] = f'results/T016s/{member}/{filename}'
        common_records.append(item)
    for name in ['diagnostic_results.json', 'job.json', 'exitcode.txt', 'stdout.log', 'stderr.log']:
        copy_exact(RAW / name, OUT / name)
    result = dict(collected=datetime.datetime.now().astimezone().isoformat(),
                  execution_commit='acdc0ce', members=members, latest_common_index=common,
                  common_outputs=common_records,
                  common_output_time_difference_ns=abs(common_records[1]['t_ns'] - common_records[0]['t_ns']),
                  warning_count_definition='Per-line matches across all eight rank logs: ConsToPrim; E with optional whitespace < 0; other ! excludes those two categories.',
                  budget_note='Headerless rows preserved after restart; columns in budget_columns.json. Cumulative analysis counters restart from zero; compare interval increments, not absolute pre-restart totals.',
                  step_pair_note='Only newly logged pairs exist in restarted logs. All available pairs are supplied when fewer than 30; no source-run history is appended.')
    (OUT / 'summary.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
    for item in members:
        (OUT / item['member'] / 'summary.json').write_text(json.dumps(item, indent=2) + '\n', encoding='utf-8')
    total = sum(p.stat().st_size for p in OUT.rglob('*') if p.is_file())
    assert total <= 20_000_000, total
    print(json.dumps(dict(total_result_bytes=total, latest_common_index=common,
                         common_outputs=common_records,
                         members=[{k: v for k, v in m.items() if k not in ['rank_logs', 'copied_sha256']} for m in members]), indent=2))


if __name__ == '__main__':
    main()
