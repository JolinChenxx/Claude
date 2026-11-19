"""Execute the specific Claude stop note and preserve a verified restart copy."""
from pathlib import Path
import datetime, hashlib, json, shutil, struct
import psutil

REPO = Path(__file__).resolve().parents[2]
SOURCE = Path('D:/sim_runs/T016e/high_trap_G0/it3')
DEST = Path('D:/sim_runs/T016d/high_trap_G0_it3')
OUT = REPO / 'results/T016e/high_trap_G0/stopped'
NOTE = REPO / 'handoff/to_codex/T016d_stop.md'
EXE = Path('D:/sim_tools/pluto_lip/build_v1f/mpi/pluto.exe')

def sha(p):
    with p.open('rb') as f:
        return hashlib.file_digest(f, 'sha256').hexdigest()

assert NOTE.exists() and 'Stop **only** T016e/high_trap_G0/it3' in NOTE.read_text(encoding='utf8')
assert not DEST.exists() and not OUT.exists(), 'Refuse duplicate stop/copy'
prov = json.loads((EXE.parent / 'build_provenance.json').read_text())
assert prov['make_returncode'] == 0 and len(prov['replaced_upstream_files']) == 7
assert sha(EXE) == prov['executable_sha256']
for name, h in prov['problem_files'].items():
    assert sha(REPO / 'pluto/lip' / name) == h, name
targets, others = [], []
for p in psutil.process_iter(['pid', 'name']):
    if p.info['name'] in ('mpiexec.exe', 'pluto.exe'):
        record = dict(pid=p.pid, name=p.name(), cwd=p.cwd(), created=p.create_time())
        (targets if Path(p.cwd()) == SOURCE else others).append(record)
assert sum(r['name'] == 'mpiexec.exe' for r in targets) == 1
assert sum(r['name'] == 'pluto.exe' for r in targets) == 8
wrapper = psutil.Process(42332)
wrapper_created = wrapper.create_time()
OUT.mkdir(parents=True)
shutil.copy2(NOTE, OUT / 'stop_instruction.md')
(OUT / 'processes_before.json').write_text(json.dumps(dict(targets=targets, others=others), indent=2))
for r in sorted(targets, key=lambda r: r['name'] != 'pluto.exe'):
    try:
        p = psutil.Process(r['pid'])
        assert p.create_time() == r['created'] and Path(p.cwd()) == SOURCE
        p.kill()
    except psutil.NoSuchProcess:
        pass
pending = []
for r in targets:
    try:
        p = psutil.Process(r['pid'])
        if p.create_time() == r['created']:
            pending.append(p)
    except psutil.NoSuchProcess:
        pass
_, alive = psutil.wait_procs(pending, timeout=10)
assert not alive
assert wrapper.is_running() and wrapper.create_time() == wrapper_created
preserved, exited_independently = [], []
for r in others:
    try:
        p = psutil.Process(r['pid'])
        assert p.create_time() == r['created']
        preserved.append(r['pid'])
    except psutil.NoSuchProcess:
        exited_independently.append(r['pid'])
row = (SOURCE / 'dbl.out').read_text().splitlines()[-1].split()
index, time_ns, step = int(row[0]), float(row[1]), int(row[3])
data = SOURCE / f'data.{index:04d}.dbl'
assert data.stat().st_size == 140 * 210 * 9 * 8
restart = (SOURCE / 'restart.out').read_bytes()
assert len(restart) % 128 == 0
matches = []
for offset in range(0, len(restart), 128):
    v = struct.unpack_from('<17i4xdd40s', restart, offset)
    if v[1] == index and v[0] == step and abs(v[17] - time_ns) < 0.0001:
        matches.append(dict(record=offset//128, step=v[0], time_ns=v[17], dt=v[18]))
assert len(matches) == 1
names = ['pluto.ini', 'grid.out', 'restart.out', 'dbl.out', data.name]
names += [p.relative_to(SOURCE).as_posix() for p in (SOURCE/'lip_tables').rglob('*') if p.is_file()]
hashes = {n: sha(SOURCE/n) for n in names}
record = dict(stopped=datetime.datetime.now().astimezone().isoformat(), source=str(SOURCE),
              destination=str(DEST), last_budget_t_ns=float((SOURCE/'budget.csv').read_text().splitlines()[-1].split(',')[0]),
              last_dbl_index=index, restart_record=matches[0], targets=targets,
              other_processes_preserved=preserved, other_processes_exited_independently=exited_independently,
              wrapper_preserved=wrapper.pid, hashes=hashes, executable_sha256=sha(EXE), inputs_changed=False)
(OUT/'summary.json').write_text(json.dumps(record, indent=2)+'\n')
for name in ['budget.csv', 'dbl.out', 'pluto.ini', 'restart.out']:
    shutil.copy2(SOURCE/name, OUT/name)
with (SOURCE/'pluto.0.log').open('rb') as f:
    f.seek(max(0, (SOURCE/'pluto.0.log').stat().st_size-8000))
    (OUT/'pluto.0.tail.log').write_bytes(f.read())
DEST.mkdir(parents=True)
for name in names:
    dest = DEST/name
    dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(SOURCE/name, dest)
    assert sha(dest) == hashes[name]
(DEST/'restart_preflight.json').write_text(json.dumps(record, indent=2)+'\n')
(REPO/'results/T016d/restart_preflight.json').write_text(json.dumps(record, indent=2)+'\n')
print(json.dumps(record, indent=2))
