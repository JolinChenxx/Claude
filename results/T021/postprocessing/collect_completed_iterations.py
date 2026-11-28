"""Collect finished T021 high-it0/low-it1 evidence; no solver changes."""
from collections import deque
from datetime import datetime
from pathlib import Path
import csv
import hashlib
import json
import math
import shutil

REPO = Path(__file__).resolve().parents[3]
RAW = Path('D:/sim_runs/T021')
TARGETS = [('high_thin_G0', 0, 800), ('low_thin_G0', 1, 1300)]


def sha(path):
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def collect(member, iteration, stop_time):
    run = RAW / member / f'it{iteration}'
    out = REPO / 'results/T021' / member / f'it{iteration}'
    assert not (out / 'completion.json').exists(), 'Already collected: ' + str(out)
    with (REPO / 'results/run_registry.csv').open(newline='') as stream:
        matches = [r for r in csv.DictReader(stream) if r['batch'] == 'T021' and r['run'] == member and int(r['picard_it']) == iteration]
    assert len(matches) == 1 and matches[0]['returncode'] == '0'
    with (run / 'budget.csv').open(newline='') as stream:
        budget = [{k: float(v) for k, v in r.items()} for r in csv.DictReader(stream)]
    assert budget[-1]['t_ns'] == stop_time
    assert all(all(math.isfinite(v) for v in r.values()) for r in budget)
    assert all(b['t_ns'] > a['t_ns'] for a, b in zip(budget, budget[1:]))
    first, last = budget[0], budget[-1]
    delta = last['E_internal'] + last['E_kinetic'] - first['E_internal'] - first['E_kinetic']
    net = sum(last[k] - first[k] for k in ['E_gas_abs', 'E_src']) - sum(last[k] - first[k] for k in ['E_rad', 'E_wall'])
    residual = delta - net
    closure = {'formula': 'Delta(E_internal+E_kinetic) - Delta(E_gas_abs+E_src-E_rad-E_wall)', 'delta_gas_energy_J': delta, 'net_recorded_sources_J': net, 'residual_J': residual, 'relative_to_abs_net_sources_pct': 100 * abs(residual) / abs(net), 'exceeds_Claude_2pct_escalation_threshold': abs(residual) / abs(net) > 0.02, 'note': 'Endpoint bookkeeping from budget.csv; not a separate physical acceptance judgment. No unrecorded correction or boundary-flux estimate is applied.'}
    out.mkdir(parents=True, exist_ok=True)
    (out / '.gitattributes').write_text('# Preserve original evidence bytes.\n*.csv -text\n*.json -text\n*.ini -text\n*.log -text\n', encoding='utf-8')
    files = {}
    for source, filename in [(run / 'budget.csv', 'budget.csv'), (run / 'source/source_meta.json', 'source_meta.json'), (run / 'pluto.ini', 'pluto.ini')]:
        before = source.stat()
        target = out / filename
        if target.exists():
            assert sha(target) == sha(source)
        else:
            shutil.copyfile(source, target)
        after = source.stat()
        assert (before.st_size, before.st_mtime_ns) == (after.st_size, after.st_mtime_ns)
        assert sha(source) == sha(target)
        files[filename] = {'source': str(source), 'bytes': target.stat().st_size, 'sha256': sha(target)}
    logs = []
    for rank in range(8):
        p = run / f'pluto.{rank}.log'
        before = p.stat()
        h = hashlib.sha256()
        warnings = 0
        examples = []
        tail = deque(maxlen=24)
        with p.open('rb') as stream:
            for line in stream:
                h.update(line)
                tail.append(line)
                if b'ConsToPrim' in line:
                    warnings += 1
                    if len(examples) < 3:
                        examples.append(line.decode('utf-8', errors='replace').rstrip())
        after = p.stat()
        assert (before.st_size, before.st_mtime_ns) == (after.st_size, after.st_mtime_ns)
        if rank == 0:
            assert b'> Done' in b''.join(tail)
        tail_name = f'pluto.{rank}.tail.log'
        (out / tail_name).write_bytes(b''.join(tail))
        logs.append({'rank': rank, 'raw_path': str(p), 'bytes': after.st_size, 'sha256': h.hexdigest(), 'ConsToPrim_lines': warnings, 'first_examples': examples, 'tail_archive': tail_name, 'tail_sha256': sha(out / tail_name)})
    evidence = {'recorded': datetime.now().astimezone().isoformat(), 'registry_row': matches[0], 'launch_source_commit': '4b0bac441769b2b2e63de47e6354af048aaee156', 'initial_budget': first, 'final_budget': last, 'budget_rows': len(budget), 'nonfinite_budget_values': [], 'files': files, 'rank_logs': logs, 'ConsToPrim_total': sum(r['ConsToPrim_lines'] for r in logs), 'energy_closure': closure, 'mass_Cu_minus_source_kg': last['M_Cu'] - last['M_src'], 'postprocessing': 'Pending existing low-it0 image queue; no duplicate output or model change.'}
    (out / 'completion.json').write_text(json.dumps(evidence, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'member': member, 'iteration': iteration, 'rc': matches[0]['returncode'], 'seconds': matches[0]['seconds'], 'rows': len(budget), 'final_t_ns': last['t_ns'], 'ConsToPrim_total': evidence['ConsToPrim_total'], 'rank_counts': [r['ConsToPrim_lines'] for r in logs], 'energy_closure': closure, 'mass_Cu_minus_source_kg': evidence['mass_Cu_minus_source_kg']}, indent=2), flush=True)


if __name__ == '__main__':
    for target in TARGETS:
        collect(*target)
