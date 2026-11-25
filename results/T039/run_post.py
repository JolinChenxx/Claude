"""T039 member postprocessing. Replay matching T031 diagnostics; both time conventions."""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
from datetime import datetime

ROOT = Path(__file__).resolve().parents[2]
RT = Path('D:/sim_tools/T039-runtime-f90a4fa9')
RUNS = Path('D:/sim_runs')
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
now = lambda: datetime.now().astimezone().isoformat()


def main(series, check):
    assert series in ('low', 'high') and not os.getenv('LIPSIM_OVERRIDES')
    it = 'it1' if series == 'low' else 'it2'
    member = series + '_wf'
    raw = RUNS/'T039'/member/it
    job = RUNS/('T039_'+member+'_post')
    ref = RUNS/'T031'/(series+'_v1rs')/it
    receipt = json.loads((ROOT/'results/T031'/ref.parent.name/it/'priority_delivery.json').read_text())
    proof = json.loads((ROOT/'results/T039'/member/it/'completion.json').read_text()); assert proof['registry_row']['returncode']=='0' and proof['registry_row']['dir']==str(raw)
    for f,h in proof['sha256'].items(): assert sha(raw/f)==h, f
    tables = RUNS/'tables/v1c'; mf = json.loads((tables/'manifest.json').read_text()); assert all(sha(tables/f)==mf['files'][f]['sha256'] for f in ('eos.bin','opt.bin','pop.bin'))
    old = str(Path('D:/sim_tools/postprocess-runtime-04'))
    steps = {k:[s.replace(str(ref),str(raw)).replace(old,str(RT)) for s in v['command']] for k,v in receipt['commands'].items()}
    img=steps['image_v2']; assert img[3]==str(raw) and img[img.index('--workers')+1]=='8' and Path(img[img.index('--tables')+1])==tables
    shifted=[s.replace(str(raw/'image_v2'),str(raw/'image_v2_t28')) for s in img]+['--t-offset-ns','28']; peak=steps['peak_lift']; peak28=[str(raw/'image_v2_t28'/'peak_lift.csv') if s==str(raw/'peak_lift.csv') else s.replace(str(raw/'image_v2'),str(raw/'image_v2_t28')) for s in peak]
    sequence=[('image_v2',img),('peak_lift',peak),('shock_track',steps['shock_track']),('core_energy',steps['core_energy']),('emission',steps['emission']),('image_v2_t28',shifted),('peak_lift_t28',peak28)]
    for name,cmd in sequence:
        assert Path(cmd[0]).exists() and (cmd[1]=='-m' or Path(cmd[1]).is_file())
        assert not Path(cmd[cmd.index('--out')+1]).exists(), name
    if check:
        assert not job.exists(), job
        print('verified 7 stages', member); return
    assert job==Path(os.environ['SIM_OUT_DIR'])
    env=dict(os.environ, PYTHONPATH=str(RT/'src'),PYTHONUTF8='1',PYTHONDONTWRITEBYTECODE='1',OMP_NUM_THREADS='1',OPENBLAS_NUM_THREADS='1',MKL_NUM_THREADS='1',NUMEXPR_NUM_THREADS='1'); pre=Path(sys.executable).parent; env['PATH']=os.pathsep.join(map(str,[pre,pre/'Library/bin',pre/'Scripts',env['PATH']]))
    (raw/'diagnostics').mkdir(exist_ok=True)
    with (job/'stages.jsonl').open('x') as ledger:
        for name,cmd in sequence:
            rec=dict(stage=name,command=cmd,started=now()); print(name,flush=True)
            with (job/(name+'.log')).open('xb') as log: rc=subprocess.run(cmd,cwd=RT,env=env,stdout=log,stderr=subprocess.STDOUT).returncode
            rec.update(returncode=rc,finished=now()); print(json.dumps(rec),file=ledger,flush=True)
            if rc: sys.exit(rc)

if __name__=='__main__':
    main(sys.argv[1],sys.argv[2:]==['--check'])
