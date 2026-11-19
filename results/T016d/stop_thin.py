"""Follow T016d_vmax.md: stop only the original thin it3 member."""
from pathlib import Path
import datetime, json, shutil
import psutil

repo = Path(__file__).resolve().parents[2]
source = Path('D:/sim_runs/T016e/high_thin_G0/it3')
out = repo/'results/T016e/high_thin_G0/stopped'
note = repo/'handoff/to_codex/T016d_vmax.md'
assert note.exists() and 'Stop it as well' in note.read_text(encoding='utf8')
assert not out.exists()
targets, others = [], []
for p in psutil.process_iter(['pid','name']):
    if p.info['name'] in ('mpiexec.exe','pluto.exe'):
        rec = dict(pid=p.pid,name=p.name(),cwd=p.cwd(),created=p.create_time())
        (targets if Path(p.cwd())==source else others).append(rec)
assert sum(x['name']=='mpiexec.exe' for x in targets)==1
assert sum(x['name']=='pluto.exe' for x in targets)==8
wrapper = psutil.Process(42332); created = wrapper.create_time()
out.mkdir(parents=True)
shutil.copy2(note,out/'stop_instruction.md')
(out/'processes_before.json').write_text(json.dumps(dict(targets=targets,others=others),indent=2))
for r in sorted(targets,key=lambda r:r['name']!='pluto.exe'):
    try:
        p=psutil.Process(r['pid'])
        assert p.create_time()==r['created'] and Path(p.cwd())==source
        p.kill()
    except psutil.NoSuchProcess:
        pass
pending=[]
for r in targets:
    try:
        p=psutil.Process(r['pid'])
        if p.create_time()==r['created']:pending.append(p)
    except psutil.NoSuchProcess:
        pass
_,alive=psutil.wait_procs(pending,timeout=10)
assert not alive
assert wrapper.is_running() and wrapper.create_time()==created
for r in others:
    p=psutil.Process(r['pid']);assert p.create_time()==r['created']
row=(source/'dbl.out').read_text().splitlines()[-1].split()
record=dict(stopped=datetime.datetime.now().astimezone().isoformat(),source=str(source),targets=targets,
    last_budget_t_ns=float((source/'budget.csv').read_text().splitlines()[-1].split(',')[0]),
    last_dbl_index=int(row[0]),last_dbl_t_ns=float(row[1]),last_dbl_step=int(row[3]),
    wrapper_preserved=wrapper.pid,other_processes_preserved=len(others),data_deleted=False)
(out/'summary.json').write_text(json.dumps(record,indent=2)+'\n')
for name in ['dbl.out','restart.out','pluto.ini','budget.csv']:
    shutil.copy2(source/name,out/name)
with (source/'pluto.0.log').open('rb') as f:
    f.seek(max(0,(source/'pluto.0.log').stat().st_size-10000))
    (out/'pluto.0.tail.log').write_bytes(f.read())
print(json.dumps(record,indent=2))
