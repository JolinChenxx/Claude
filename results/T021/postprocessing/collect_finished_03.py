"""Archive seven newly completed T021/T023 iterations; never run a solver."""
from collections import deque
from datetime import datetime
from pathlib import Path
import csv
import hashlib
import json
import math
import shutil

REPO = Path(__file__).resolve().parents[3]
TARGETS = [('T021', 'high_thin_G0', 1), ('T021', 'high_thin_G0', 2),
           ('T023', 'high_thin_G0', 0), ('T023', 'high_trap_G0', 0),
           ('T023', 'high_thin_G0', 1), ('T023', 'high_trap_G0', 1),
           ('T023', 'low_trap_G0', 0)]


def sha(path):
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def accounting(first, last):
    delta = last['E_internal'] + last['E_kinetic'] - first['E_internal'] - first['E_kinetic']
    net = sum(last[k] - first[k] for k in ['E_gas_abs', 'E_src']) - sum(last[k] - first[k] for k in ['E_rad', 'E_wall'])
    return {'delta_gas_energy_J': delta, 'net_recorded_sources_J': net,
            'residual_J': delta - net,
            'relative_to_abs_net_sources_pct': 100 * abs(delta - net) / abs(net) if net else None}


def collect(batch, member, iteration):
    run = Path('D:/sim_runs') / batch / member / f'it{iteration}'
    out = REPO / 'results' / batch / member / f'it{iteration}'
    assert not (out / 'completion.json').exists(), 'Already collected: ' + str(out)
    with (REPO / 'results/run_registry.csv').open(newline='') as stream:
        matches = [r for r in csv.DictReader(stream) if r['batch'] == batch and r['run'] == member and int(r['picard_it']) == iteration]
    assert len(matches) == 1 and matches[0]['returncode'] == '0'
    with (run / 'budget.csv').open(newline='') as stream:
        budget = [{k: float(v) for k, v in r.items()} for r in csv.DictReader(stream)]
    assert budget[-1]['t_ns'] == (800 if member.startswith('high') else 1300)
    assert all(b['t_ns'] > a['t_ns'] for a, b in zip(budget, budget[1:]))
    nonfinite = [{'row': i, 'field': k, 'value': str(v)} for i, r in enumerate(budget) for k, v in r.items() if not math.isfinite(v)]
    if batch == 'T023':
        assert len(budget[-1]) == 15 and 'M_bp_suppressed' in budget[-1]
    first, last = budget[0], budget[-1]
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
    for rank in range(int(matches[0]['procs'])):
        p = run / f'pluto.{rank}.log'
        before = p.stat()
        h, warnings, examples, tail = hashlib.sha256(), 0, [], deque(maxlen=24)
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
        logs.append({'rank': rank, 'raw_path': str(p), 'bytes': after.st_size,
                     'sha256': h.hexdigest(), 'ConsToPrim_lines': warnings,
                     'first_examples': examples, 'tail_archive': tail_name,
                     'tail_sha256': sha(out / tail_name)})
    checkpoints = []
    for requested in [200, 250, 300, 400]:
        row = min(budget, key=lambda r: abs(r['t_ns'] - requested))
        checkpoints.append({'requested_t_ns': requested, 'actual_t_ns': row['t_ns'], **accounting(first, row)})
    method = 'Delta(E_internal+E_kinetic) - Delta(E_gas_abs+E_src-E_rad-E_wall); no correction or unrecorded boundary-flux estimate'
    (out / 'energy_checkpoints.json').write_text(json.dumps({'method': method, 'checkpoints': checkpoints}, indent=2) + '\n', encoding='utf-8')
    source_meta = json.loads((run / 'source/source_meta.json').read_text())
    evidence = {'recorded': datetime.now().astimezone().isoformat(), 'registry_row': matches[0],
                'launch_job': json.loads((run.parents[1] / 'job.json').read_text(encoding='utf-8-sig')),
                'initial_budget': first, 'final_budget': last, 'budget_rows': len(budget),
                'nonfinite_budget_values': nonfinite, 'files': files, 'rank_logs': logs,
                'ConsToPrim_total': sum(r['ConsToPrim_lines'] for r in logs),
                'energy_closure': {'formula': method, **accounting(first, last)},
                'mass_Cu_minus_source_kg': last['M_Cu'] - last['M_src'],
                'wall_pressure_from_previous_run': source_meta.get('wall_pressure_from_previous_run'),
                'postprocessing': 'Pending serialized background queue; raw production already complete.'}
    (out / 'completion.json').write_text(json.dumps(evidence, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'batch': batch, 'member': member, 'iteration': iteration,
                      'seconds': matches[0]['seconds'], 'rows': len(budget),
                      'final_t_ns': last['t_ns'], 'nonfinite': nonfinite,
                      'ConsToPrim_total': evidence['ConsToPrim_total'],
                      'rank_counts': [r['ConsToPrim_lines'] for r in logs],
                      'energy_closure': evidence['energy_closure'],
                      'checkpoints': checkpoints,
                      'mass_Cu_minus_source_kg': evidence['mass_Cu_minus_source_kg'],
                      'M_bp_suppressed': last.get('M_bp_suppressed'),
                      'wall_pressure_from_previous_run': evidence['wall_pressure_from_previous_run']}), flush=True)


if __name__ == '__main__':
    for target in TARGETS:
        collect(*target)
