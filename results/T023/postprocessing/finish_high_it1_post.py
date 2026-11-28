"""Preserve queue03 exit, finish only missing peak lift, collect successful image."""
from pathlib import Path
from datetime import datetime
import csv
import hashlib
import json
import math
import os
import shutil
import subprocess
import time
import numpy as np
import pandas as pd
import psutil
import yaml

REPO=Path(__file__).resolve().parents[3]
RUNTIME=Path('D:/sim_tools/postprocess-runtime-04')
JOB=Path('D:/sim_runs/T021_T023_post_03')
RAW=Path('D:/sim_runs/T023/high_thin_G0/it1')
OUT=REPO/'results/T023/high_thin_G0/it1'


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def copy(source,target):
    before=source.stat();digest=sha(source);target.parent.mkdir(parents=True,exist_ok=True)
    if target.exists():assert sha(target)==digest,str(target)
    else:shutil.copyfile(source,target)
    after=source.stat();assert (before.st_size,before.st_mtime_ns)==(after.st_size,after.st_mtime_ns)
    assert sha(target)==sha(source)==digest
    return {'source':str(source),'archive':target.relative_to(REPO).as_posix(),'sha256':digest,'bytes':after.st_size}


def main():
    assert not psutil.pid_exists(11136),'Inspect PID before collection; do not stop a reused PID'
    s=json.loads((JOB/'postprocess_summary.json').read_text());assert s['state']=='failed' and (JOB/'exitcode.txt').read_text().strip()=='1'
    assert 'Frozen input changed:' in s['error'] and 'registry.yaml' in s['error']
    rows=[r for r in s['commands'] if r['batch']=='T023' and r['member']=='high_thin_G0' and r['iteration']=='it1']
    image=next(r for r in rows if r['name']=='image_v2');assert image['returncode']==0 and not any(r['name']=='peak_lift' for r in rows)
    changed=[]
    for name,digest in s['preflight']['input_sha256'].items():
        p=Path(name)
        candidate=RUNTIME/p.relative_to(REPO) if p.is_relative_to(REPO) else p
        if sha(candidate)!=digest:
            rel=p.relative_to(REPO).as_posix();assert rel in ['data/params/registry.yaml','src/lipsim/batch.py'];changed.append(rel)
    old=yaml.safe_load((REPO/'results/T023/postprocessing/queue03_frozen_original/data/params/registry.yaml').read_text(encoding='utf-8'))
    new=yaml.safe_load((RUNTIME/'data/params/registry.yaml').read_text(encoding='utf-8'));assert set(new)-set(old)=={'lotz_a_cu'};new.pop('lotz_a_cu');assert new==old
    closed=REPO/'results/T021/postprocessing/queue03_exit';closed.mkdir(exist_ok=True);(closed/'.gitattributes').write_text('* -text\n',encoding='utf-8')
    for name in ['job.json','stdout.log','stderr.log','exitcode.txt','postprocess_summary.json']:copy(JOB/name,closed/name)
    receipt=OUT/'peak_lift_execution.json';peak=OUT/'peak_lift.csv';log=OUT/'peak_lift.log'
    assert not receipt.exists() and not peak.exists() and not log.exists()
    command=['D:/sim_tools/envs/lipsim/python.exe',str(RUNTIME/'scripts/peak_lift.py'),str(RAW/'image_v2'),'--series','high','--out',str(peak)]
    env=dict(os.environ,PYTHONPATH=str(RUNTIME/'src'),PYTHONUTF8='1',PYTHONDONTWRITEBYTECODE='1',OMP_NUM_THREADS='1',OPENBLAS_NUM_THREADS='1',MKL_NUM_THREADS='1')
    execution={'command':command,'started':datetime.now().astimezone().isoformat(),'runtime_commit':'1c71670','registry_comparison':'All existing registry entries identical; sole added Lotz key is unused by peak_lift. Remaining source difference batch.py is not invoked.'}
    start=time.perf_counter()
    with log.open('wb') as stream:execution['returncode']=subprocess.run(command,cwd=RUNTIME,env=env,stdout=stream,stderr=subprocess.STDOUT).returncode
    execution.update(seconds=time.perf_counter()-start,finished=datetime.now().astimezone().isoformat(),peak_sha256=sha(peak),log_sha256=sha(log))
    receipt.write_text(json.dumps(execution,indent=2)+'\n',encoding='utf-8');assert execution['returncode']==0
    meta=yaml.safe_load((RUNTIME/'data/exp/meta.yaml').read_text(encoding='utf-8'))['sets']['high'];times=sorted({r['t_ns'] for r in meta['frames']})
    scenarios={f'{b}_{m}_{f}' for b in 'abc' for m in ['thin','sa'] for f in ['baseline','low','high']}
    summary=json.loads((RAW/'image_v2/image_summary.json').read_text());assert set(summary)==scenarios
    assert len(list((RAW/'image_v2').glob('frames_*.npz')))==6
    assert {p.stem.removeprefix('metrics_') for p in (RAW/'image_v2').glob('metrics_*.csv')}==scenarios
    files=[]
    for source in sorted((RAW/'image_v2').iterdir()):
        detail={}
        if source.suffix=='.npz':
            with np.load(source) as a:
                assert set(a.files)=={'t_ns','radiance','um_per_px'} and a['t_ns'].tolist()==times
                assert list(a['radiance'].shape)==[len(times),*meta['frame_px']]
                assert all(np.isfinite(a[k]).all() for k in a.files);detail['radiance_shape']=list(a['radiance'].shape)
        elif source.suffix=='.csv':
            rows=list(csv.DictReader(source.open(newline='')));assert [float(r['t_ns']) for r in rows]==times
            missing=[]
            for r in rows:
                for k,v in r.items():
                    value=float(v) if v else math.nan
                    if not math.isfinite(value):
                        assert math.isnan(value)
                        assert (k in ['xc_um','zc_um','sigma_x2_um2','sigma_z2_um2'] and float(r['L'])==0) or (k in ['thr_mean_gray','thr_top_um','thr_bottom_um'] and float(r['thr_area_mm2'])==0)
                        missing.append({'t_ns':float(r['t_ns']),'field':k})
            detail['original_missing_values']=missing
        else:assert source.name=='image_summary.json'
        files.append({**copy(source,OUT/'image_v2'/source.name),**detail})
    files.append(copy(Path(image['log']),OUT/'image_v2/image_v2.log'))
    p=pd.read_csv(peak);assert p.t_ns.tolist()==times and np.isfinite(p.to_numpy(dtype=float)).all()
    exp=pd.read_csv(RUNTIME/'results/surface/surface_rows.csv');exp=exp[exp.series=='high'].groupby('t_ns',as_index=False).mean(numeric_only=True)
    for col,source in [('exp_lift_min_um','lower50_row'),('exp_lift_max_um','lower10_row')]:assert np.allclose(p[col],(exp[source]-exp.peak_row)*10.724,rtol=0,atol=.0500001,equal_nan=True)
    evidence={'recorded':datetime.now().astimezone().isoformat(),'image_command':image,'peak_command':execution,'files':files,'scenario_count':len(scenarios),'frame_count':len(times),'P2_unavailable':[k for k,v in summary.items() if not math.isfinite(v['P2_dt10_ns'])],'policy':'Image rc0 before expected queue hash failure. Original arrays/metrics/NaNs retained; only missing peak stage executed once in pinned runtime. No production or image rerun.'}
    (OUT/'image_v2/validation.json').write_text(json.dumps(evidence,indent=2)+'\n',encoding='utf-8')
    total=sum(p.stat().st_size for p in (REPO/'results/T023').rglob('*') if p.is_file());assert total<20*1024**2
    print(json.dumps({'image_seconds':image['seconds'],'peak_seconds':execution['seconds'],'frames':len(times),'P2_unavailable':len(evidence['P2_unavailable']),'archive_bytes':total,'baseline_b':{k:v for k,v in summary.items() if k.startswith('b_') and k.endswith('baseline')}}))


if __name__=='__main__':main()
