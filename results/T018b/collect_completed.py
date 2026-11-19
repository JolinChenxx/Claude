"""Archive completed T018b iteration evidence without modifying raw data."""
from pathlib import Path
from collections import deque
import argparse
import csv
import datetime as dt
import hashlib
import json
import math
import re
import shutil

REPO = Path(__file__).resolve().parents[2]
ROOT = Path('D:/sim_runs/T018b')


def digest(path):
    h = hashlib.sha256()
    with path.open('rb') as stream:
        while chunk := stream.read(8 * 1024 * 1024):
            h.update(chunk)
    return h.hexdigest()


def collect(record):
    member, iteration = record['run'], int(record['picard_it'])
    run = ROOT / member / f'it{iteration}'
    out = REPO / 'results/T018b' / member / f'it{iteration}'
    if (out / 'completion.json').exists():
        prior = json.loads((out / 'completion.json').read_text(encoding='utf-8'))
        assert prior['registry_row'] == record
        return {'run': str(run), 'state': 'already_collected'}
    assert record['returncode'] == '0'
    logs = sorted(run.glob('pluto.*.log'), key=lambda p: int(p.name.split('.')[1]))
    assert [int(p.name.split('.')[1]) for p in logs] == list(range(8))
    for log in logs:
        with log.open('rb') as stream:
            stream.seek(max(0, log.stat().st_size - 8192))
            assert b'> Done' in stream.read(), str(log)
    with (run / 'budget.csv').open(newline='', encoding='utf-8') as stream:
        budget = list(csv.DictReader(stream))
    assert budget and all(math.isfinite(float(v)) for row in budget for v in row.values())
    final = {k: float(v) for k, v in budget[-1].items()}
    assert final['t_ns'] == 1300
    source = json.loads((run / 'source/source_meta.json').read_text(encoding='utf-8'))
    assert source['phase_explosion'] is True
    ini = (run / 'pluto.ini').read_text(encoding='utf-8')
    assert re.search(r'(?m)^\s*rmax_par\s+1000(?:\s|$)', ini)
    results, tails = [], {}
    for log in logs:
        before = log.stat()
        h = hashlib.sha256()
        count = warnings = 0
        carry = b''
        last = deque(maxlen=50)
        with log.open('rb') as stream:
            while chunk := stream.read(8 * 1024 * 1024):
                h.update(chunk)
                lines = (carry + chunk).split(b'\n')
                carry = lines.pop()
                count += len(lines)
                warnings += sum(b'ConsToPrim' in line for line in lines)
                last.extend(lines[-50:])
        if carry:
            count += 1
            warnings += int(b'ConsToPrim' in carry)
            last.append(carry)
        after = log.stat()
        assert (before.st_size, before.st_mtime_ns) == (after.st_size, after.st_mtime_ns)
        tail = b'\n'.join(last) + b'\n'
        assert b'> Done' in tail
        item = {'rank': int(log.name.split('.')[1]), 'file': str(log),
                'bytes': after.st_size, 'mtime_ns': after.st_mtime_ns,
                'sha256': h.hexdigest(), 'line_count': count,
                'ConsToPrim_lines': warnings, 'Done_in_tail': True}
        results.append(item)
        tails[log.name + '.tail.txt'] = tail
        print(json.dumps({'run': member, **item}), flush=True)
    for item in results:
        stat = Path(item['file']).stat()
        assert (stat.st_size, stat.st_mtime_ns) == (item['bytes'], item['mtime_ns'])
    out.mkdir(parents=True, exist_ok=True)
    hashes = {}
    for source_name, target_name in [('budget.csv', 'budget.csv'), ('pluto.ini', 'pluto.ini'),
                                    ('source/source_meta.json', 'source_meta.json'), ('dbl.out', 'dbl.out')]:
        src, dst = run / source_name, out / target_name
        shutil.copy2(src, dst)
        hashes[target_name] = digest(dst)
        assert hashes[target_name] == digest(src)
    for name, tail in tails.items():
        (out / name).write_bytes(tail)
    total = sum(item['ConsToPrim_lines'] for item in results)
    completion = {'collected': dt.datetime.now().astimezone().isoformat(timespec='seconds'),
                  'run': f'T018b/{member}/it{iteration}', 'registry_row': record,
                  'original_batch_launch_commit': 'eaa401f',
                  'registry_commit_is_completion_time_head': True,
                  'final_budget': final, 'ConsToPrim_total_all_ranks': total,
                  'rank_logs': results, 'copied_file_sha256': hashes,
                  'input_checks': {'phase_explosion': True, 'rmax_par1000': True},
                  'solver_completion': 'rc0 and1300ns; not an independent numerical/physical acceptance verdict',
                  'postprocessing': 'Pending prescribed diagnostics and image_v1/image_v2.'}
    for name, obj in [('completion.json', completion), ('ConsToPrim_counts.json', {'total': total, 'ranks': results})]:
        path = out / name
        path.write_text(json.dumps(obj, indent=2) + '\n', encoding='utf-8')
        assert json.loads(path.read_text(encoding='utf-8')) == obj
    return {'run': completion['run'], 'state': 'collected', 'final_budget': final,
            'ConsToPrim_total': total, 'wall_seconds': record['seconds']}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--iteration', type=int, required=True)
    args = parser.parse_args()
    with (REPO / 'results/run_registry.csv').open(newline='', encoding='utf-8-sig') as stream:
        rows = [r for r in csv.DictReader(stream) if r['batch'] == 'T018b'
                and r['picard_it'] == str(args.iteration) and r['returncode'] == '0']
    assert len({r['run'] for r in rows}) == len(rows)
    assert all(r['run'] in ['low_thin_G0', 'low_trap_G0'] for r in rows)
    summary = [collect(row) for row in rows]
    (ROOT / f'it{args.iteration}_collection.json').write_text(json.dumps(summary, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(summary, indent=2), flush=True)


if __name__ == '__main__':
    main()
