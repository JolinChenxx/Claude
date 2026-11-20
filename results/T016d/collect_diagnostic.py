"""Collect every rank after a bounded diagnostic, preserving raw snapshots."""
from pathlib import Path
import argparse, datetime, hashlib, json, shutil
import psutil

ap = argparse.ArgumentParser()
ap.add_argument('--phase', choices=['v1f', 'vmax'], default='v1f')
a = ap.parse_args()
repo = Path(__file__).resolve().parents[2]
run = Path('D:/sim_runs/T016d/high_trap_G0_it3')
out = repo/'results/T016d'/('final_v1f' if a.phase == 'v1f' else 'vmax')
record_name = 'diagnostic_run.json' if a.phase == 'v1f' else 'vmax_run.json'
record = json.loads((run/record_name).read_text())
assert record['state'] in ('finished', 'time_limit', 'failed')
for p in psutil.process_iter(['pid', 'name']):
    if p.info['name'] in ('mpiexec.exe', 'pluto.exe'):
        assert Path(p.cwd()) != run, 'Solver still active'
out.mkdir(parents=True, exist_ok=True)
archive = run.parent/('archive_' + a.phase)
archive.mkdir(exist_ok=True)
summary = dict(collected=datetime.datetime.now().astimezone().isoformat(), run=record, ranks=[])
logs = sorted(run.glob('pluto.*.log'))
assert len(logs) == 8
for p in logs:
    raw = p.read_bytes()
    if not (archive/p.name).exists():
        shutil.copy2(p, archive/p.name)
    assert (archive/p.name).read_bytes() == raw, 'Existing archive must be unchanged'
    prefix_bytes = 0
    if a.phase == 'vmax':
        previous = (run.parent/'archive_v1f'/p.name).read_bytes()
        assert raw.startswith(previous), 'Expected the verified v1f log prefix'
        prefix_bytes = len(previous)
    data = raw[prefix_bytes:]
    lines = data.decode(errors='replace').splitlines()
    steps = [i for i,x in enumerate(lines) if x.startswith('step:')][-20:]
    pairs = []
    for i in steps:
        pairs.append(lines[i])
        pairs.extend(x for x in lines[i+1:i+6] if 'Nrkl' in x)
    dt = [x for x in lines if '! lip dt-limit' in x]
    vmax = [x for x in lines if '! lip vmax' in x]
    other = [x for x in lines if '!' in x and 'ConsToPrim' not in x]
    for label, selected in [('dt_limit',dt),('vmax',vmax),('step_nrkl',pairs),('other_warnings',other)]:
        (out/(p.stem+'.'+label+'.log')).write_text('\n'.join(selected)+ ('\n' if selected else ''))
    summary['ranks'].append(dict(rank_log=p.name, bytes=len(data), sha256=hashlib.sha256(data).hexdigest(),
        combined_bytes=len(raw), combined_sha256=hashlib.sha256(raw).hexdigest(), excluded_prefix_bytes=prefix_bytes,
        dt_limit_count=len(dt), vmax_count=len(vmax), steps_collected=len(steps),
        last_step=lines[steps[-1]] if steps else None, ConsToPrim=sum('ConsToPrim' in x for x in lines),
        other_count=len(other), raw_archive=str(archive/p.name)))
for name in [record_name,'dbl.out','restart.out','grid.out','pluto.ini','budget.csv','mpi_output.log','vmax_mpi.log']:
    p = run/name
    if p.exists():
        shutil.copy2(p,archive/name)
        if p.stat().st_size <= 1024*1024:
            shutil.copy2(p,out/name)
summary['budget_exists'] = (run/'budget.csv').exists()
summary['last_dbl_line'] = (run/'dbl.out').read_text().splitlines()[-1]
(out/'summary.json').write_text(json.dumps(summary,indent=2)+'\n')
assert sum(p.stat().st_size for p in (repo/'results/T016d').rglob('*') if p.is_file()) < 20*1024*1024
print(json.dumps(summary,indent=2))
