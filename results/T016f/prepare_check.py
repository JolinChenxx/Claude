"""Copy and hash the authorized T016d restart inputs, without changing originals."""
from pathlib import Path
import json, hashlib, shutil, datetime
repo=Path(__file__).resolve().parents[2]
src=Path('D:/sim_runs/T016d/high_trap_G0_it3'); dst=Path('D:/sim_runs/T016f/check')
assert not dst.exists(), 'Refuse to overwrite an existing check'
prior=json.loads((repo/'results/T016d/vmax/preflight.json').read_text())
prov=json.loads((repo/'results/T016f/build_provenance.json').read_text())
assert prov['make_returncode']==0 and len(prov['replaced_upstream_files'])==7
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
for name, digest in prov['problem_files'].items():
    assert sha(repo/'pluto/lip'/name)==digest, name
exe=Path('D:/sim_tools/pluto_lip/build_v1h/mpi/pluto.exe')
assert sha(exe)==prov['executable_sha256']
dst.mkdir(parents=True)
hashes={}
for name in prior['hashes']:
    source=src/name; target=dst/name; target.parent.mkdir(exist_ok=True,parents=True)
    before=sha(source); shutil.copy2(source,target); assert sha(target)==before and sha(source)==before
    hashes[name]=before
meta=dict(source=str(src),destination=str(dst),created=datetime.datetime.now().astimezone().isoformat(),
    last_dbl_index=prior['last_dbl_index'],restart_record=prior['restart_record'],hashes=hashes,
    executable_sha256=sha(exe),source_build='v1h',source_commit='65d04aa')
for p in (dst/'restart_preflight.json',repo/'results/T016f/check/restart_preflight.json'):
    p.write_text(json.dumps(meta,indent=2)+'\n')
print(json.dumps(meta,indent=2))
