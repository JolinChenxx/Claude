"""Stop only the explicitly authorized member and preserve a checked restart copy."""
from pathlib import Path
import datetime, hashlib, json, re, shutil, struct, sys
import psutil

repo = Path(__file__).resolve().parents[2]
source = Path('D:/sim_runs/T016e/low_thin_G1/it0')
dest = Path('D:/sim_runs/T016e_r/low_thin_G1/it0')
out = repo / 'results/T016e/low_thin_G1/stopped'
assert not dest.exists()
out.mkdir(parents=True, exist_ok=True)
note = repo / 'handoff/to_codex/low_G1_restart.md'
assert note.exists()
shutil.copy2(note, out / 'stop_instruction.md')
targets = []
others = []
for p in psutil.process_iter(['pid', 'name']):
    if p.info['name'] not in ('mpiexec.exe', 'pluto.exe'):
        continue
    cwd = p.cwd()
    rec = dict(pid=p.pid, name=p.name(), cwd=cwd, created=p.create_time())
    (targets if Path(cwd) == source else others).append(rec)
if '--resume-after-stop' in sys.argv:
    assert not targets, 'Authorized member must already be stopped'
    saved=json.loads((out/'processes_before.json').read_text())
    targets=saved['targets']; others=saved['others']
assert sum(r['name']=='mpiexec.exe' for r in targets)==1
assert sum(r['name']=='pluto.exe' for r in targets)==16
(out/'processes_before.json').write_text(json.dumps(dict(targets=targets,others=others),indent=2))
# Kill exact solver ranks first; MPI may exit automatically when ranks disappear.
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
gone,alive=psutil.wait_procs(pending,timeout=10)
assert not alive
assert psutil.pid_exists(42332) and psutil.pid_exists(31276), 'Batch wrapper must remain alive'
for r in others:
    p=psutil.Process(r['pid'])
    assert p.create_time()==r['created']
row=(source/'dbl.out').read_text().splitlines()[-1]
fields=row.split(); index=int(fields[0]); step=int(fields[3]); t=float(fields[1])
restart=(source/'restart.out').read_bytes()
assert len(restart)%128==0
matches=[]
for offset in range(0,len(restart),128):
    v=struct.unpack_from('<17i4xdd40s',restart,offset)
    if v[1]==index and v[0]==step and abs(v[17]-t)<0.0001:
        matches.append(dict(record=offset//128,step=v[0],time_ns=v[17],dt=v[18]))
assert len(matches)==1
assert (source/f'data.{index:04d}.dbl').stat().st_size==260*400*9*8
for name in ['budget.csv','dbl.out','pluto.ini','restart.out']:
    shutil.copy2(source/name,out/name)
with (source/'pluto.0.log').open('rb') as f:
    f.seek(max(0,(source/'pluto.0.log').stat().st_size-5000))
    (out/'pluto.0.tail.log').write_bytes(f.read())
# Full copy retains early frames for later imaging, not just the restart frame.
shutil.copytree(source,dest)
original=(dest/'pluto.ini').read_bytes()
changed,n=re.subn(rb'(?m)^rmax_par 1000\r?$',b'rmax_par 4000',original)
assert n==1
(dest/'pluto.ini').write_bytes(changed)
def sha(p):
    with p.open('rb') as f:return hashlib.file_digest(f,'sha256').hexdigest()
names=['grid.out','restart.out','dbl.out',f'data.{index:04d}.dbl']
names += [p.relative_to(source).as_posix() for p in (source/'lip_tables').rglob('*') if p.is_file()]
hashes={name:sha(source/name) for name in names}
assert all(sha(dest/name)==h for name,h in hashes.items())
exe=Path('D:/sim_tools/pluto_lip/build_v1d/mpi/pluto.exe')
assert sha(exe)=='0f9e454c13685412da7fce91c104789caf76f002e7685dd17e6fcd8d088481ca'
record=dict(stopped=datetime.datetime.now().astimezone().isoformat(),source=str(source),destination=str(dest),last_t_ns=float((source/'budget.csv').read_text().splitlines()[-1].split(',')[0]),last_dbl_index=index,restart_record=matches[0],targets=targets,other_solver_processes_preserved=len(others),hashes=hashes,ini_before_sha256=sha(source/'pluto.ini'),ini_after_sha256=sha(dest/'pluto.ini'),executable_sha256=sha(exe),change='rmax_par 1000 -> 4000 only',all_previous_frames_copied=True)
(out/'summary.json').write_text(json.dumps(record,indent=2)+'\n')
(dest/'restart_preflight.json').write_text(json.dumps(record,indent=2)+'\n')
print(json.dumps(record,indent=2))
