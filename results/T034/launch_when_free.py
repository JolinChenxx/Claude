"""Wait without reserving MPI cores; launch the original batch through job.ps1 exactly once."""
import datetime, hashlib, json, os, subprocess, sys, time
from pathlib import Path
import yaml
from lipsim.workstation_capacity import capacity_reader, available

ROOT=Path(__file__).resolve().parents[2]; RAW=Path('D:/sim_runs'); card=ROOT/'tasks/T034-v1rs-oat-high.md'
read=capacity_reader(ROOT)
def h(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def preflight():
    assert 'LIPSIM_OVERRIDES' not in os.environ
    cfg=yaml.safe_load((ROOT/'configs/batches/T034.yaml').read_text()); specs=cfg['numerics']['runs']
    assert len({s['id'] for s in specs})==len(specs)==8 and all(s['procs']==8 and s['start_it']==s['picard']==2 for s in specs)
    assert h(Path(cfg['pluto_exe']))==json.loads((ROOT/'results/T033/table-launch.json').read_text())['executable_sha256']
    assert Path(cfg['mpiexec']).is_file() and not Path(cfg['root']).exists()
    variants=json.loads((ROOT/'results/T033/table-verification.json').read_text())['variants']; checked={}
    for s in specs:
        assert (ROOT/s['case']).is_file() and all((Path(s['prev_run'])/f).is_file() for f in ['surface_transmission.npz','source/source_meta.json'])
        d=Path(s.get('tables',cfg['tables']));
        if d.name not in checked:
            m=json.loads((d/'manifest.json').read_text()); checked[d.name]={f:h(d/f) for f in ['eos.bin','opt.bin','pop.bin']}; assert all(m['files'][f]['sha256']==v for f,v in checked[d.name].items()); assert d.name=='v1c' or all(variants[d.name]['sha256'][f]==v for f,v in checked[d.name].items())
    return checked

def main():
    checks=preflight(); e={str(i):2 for i in range(18)}; rows=[dict(batch='T033',run=str(i),procs='8',picard_it='2',returncode='0',ini_sha256='x',dir='x') for i in range(18)]
    for k in range(19):
        cap=available(e,rows[:k]+rows[:k]); assert cap==8*((60-min(56,(18-k)*8))//8) and cap+min(56,(18-k)*8)<=60
    if sys.argv[1:]==['--check-only']:
        print(json.dumps(dict(table_hashes=checks,capacity_cores=read(),scheduler_cases=19),indent=2)); return
    assert not sys.argv[1:]; prev=None
    while True:
        state=yaml.safe_load(card.read_text(encoding='utf-8').split('---',2)[1]); assert state['status']=='running' and state['owner']=='codex'; assert not (RAW/'T034').exists(), 'Already launched; do not restart'
        cap=read()
        if cap!=prev: print(datetime.datetime.now().astimezone().isoformat(), 'available MPI cores:',cap,flush=True); prev=cap
        if cap>=16: break
        time.sleep(15)
    preflight(); env={**os.environ,'LIPSIM_T034_SHARED_CAP':'1'}
    cmd=f'{sys.executable} -B -m lipsim.batch configs/batches/T034.yaml --max-cores 16'
    subprocess.run(['powershell.exe','-NoProfile','-File',str(ROOT/'scripts/win/job.ps1'),'start','-Id','T034','-Threads','1','-Cmd',cmd],cwd=ROOT,env=env,check=True)
if __name__=='__main__': main()
