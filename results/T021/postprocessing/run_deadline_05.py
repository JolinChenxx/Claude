"""Authorized remaining postprocessing, isolated from future ws pulls."""
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime
from pathlib import Path
import argparse
import csv
import hashlib
import json
import math
import os
import subprocess
import time
import psutil

MAIN=Path(__file__).resolve().parents[3]
RUNTIME=Path('D:/sim_tools/postprocess-runtime-04')
PYTHON=Path('D:/sim_tools/envs/lipsim/python.exe')
TABLES=Path('D:/sim_runs/tables/v1c')
JOB=Path('D:/sim_runs/T021_T023_post_05')
EVIDENCE=MAIN/'results/T021/postprocessing/deadline_05_preflight.json'
RESUME=MAIN/'results/T021/postprocessing/queue04_exit/reuse_diagnostics.json'
SLOTS=[('T021','high_thin_G0','it2'),('T023','low_thin_G0','latest'),
       ('T023','high_thin_G0','it2'),('T023','high_trap_G0','it0'),
       ('T023','high_trap_G0','it1'),('T023','low_thin_G0','it0'),
       ('T023','low_trap_G0','it0'),('T021','high_thin_G0','it1')]


def now():return datetime.now().astimezone().isoformat()


def sha(path):
    h=hashlib.sha256()
    with Path(path).open('rb') as f:
        for block in iter(lambda:f.read(1024*1024),b''):h.update(block)
    return h.hexdigest()


def save(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    tmp=path.with_suffix('.tmp');tmp.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8');tmp.replace(path)


def selected(batch,member,iteration):
    with (MAIN/'results/run_registry.csv').open(newline='') as f:rows=list(csv.DictReader(f))
    rows=[r for r in rows if r.get('batch')==batch and r.get('run')==member and r.get('returncode')=='0' and (r.get('picard_it') or '').isdigit()]
    if iteration!='latest':rows=[r for r in rows if 'it'+r['picard_it']==iteration]
    if not rows:return None
    maximum=max(int(r['picard_it']) for r in rows);rows=[r for r in rows if int(r['picard_it'])==maximum]
    assert len(rows)==1,'Ambiguous completed registry row'
    return rows[0]


def plans(batch,member,iteration):
    raw=Path('D:/sim_runs')/batch/member/iteration;series,nec=member.split('_')[:2];diag=raw/'diagnostics'
    commands=[('core_energy',[str(PYTHON),str(RUNTIME/'scripts/core_energy.py'),str(raw),'--tables',str(TABLES),'--nec',nec,'--out',str(diag/'core_energy.csv')]),
              ('shock_track',[str(PYTHON),str(RUNTIME/'scripts/shock_track.py'),str(raw),'--out',str(diag/'shock_track.csv')]),
              ('emission',[str(PYTHON),str(RUNTIME/'scripts/diag_emission.py'),str(raw),'--tables',str(TABLES),'--times','100','150','200','250','300','400','500','600','700','800','1000','1200','--filter','510','12','--out',str(diag/'diag_emission.csv')]),
              ('image_v2',[str(PYTHON),'-m','lipsim.image',str(raw),'--series',series,'--tables',str(TABLES),'--out',str(raw/'image_v2'),'--workers','24','--scenarios',str(RUNTIME/'configs/imaging/v2.yaml')])]
    if batch=='T023' or (batch=='T021' and member=='high_thin_G0' and iteration=='it2'):
        commands.append(('peak_lift',[str(PYTHON),str(RUNTIME/'scripts/peak_lift.py'),str(raw/'image_v2'),'--series',series,'--out',str(MAIN/'results'/batch/member/iteration/'peak_lift.csv')]))
    return [{'batch':batch,'member':member,'iteration':iteration,'name':name,'command':command} for name,command in commands]


def admission(row):
    batch,member,iteration=row['batch'],row['run'],'it'+row['picard_it'];raw=Path(row['dir'])
    assert raw.resolve()==(Path('D:/sim_runs')/batch/member/iteration).resolve()
    frozen={str(raw/p):sha(raw/p) for p in ['budget.csv','source/source_meta.json','pluto.ini','grid.out','dbl.out']}
    assert frozen[str(raw/'pluto.ini')].startswith(row['ini_sha256'])
    with (raw/'budget.csv').open(newline='') as f:budget=[{k:float(v) for k,v in r.items()} for r in csv.DictReader(f)]
    assert budget and budget[-1]['t_ns']==(800 if member.startswith('high') else 1300)
    assert all(math.isfinite(v) for r in budget for v in r.values())
    assert all(b['t_ns']>a['t_ns'] for a,b in zip(budget,budget[1:]))
    with (raw/'pluto.0.log').open('rb') as f:f.seek(max(0,(raw/'pluto.0.log').stat().st_size-8192));tail=f.read()
    assert b'> Done' in tail
    first,last=budget[0],budget[-1]
    delta=last['E_internal']+last['E_kinetic']-first['E_internal']-first['E_kinetic']
    net=sum(last[k]-first[k] for k in ['E_gas_abs','E_src'])-sum(last[k]-first[k] for k in ['E_rad','E_wall'])
    closure=100*abs(delta-net)/abs(net) if net else None
    if batch=='T023':assert closure is not None and closure<=2,'T023 closure above authorized threshold'
    command_list=plans(batch,member,iteration)
    reused=[]
    if (batch,member,iteration)==('T021','high_thin_G0','it2'):
        receipt=json.loads(RESUME.read_text());reused=receipt['commands']
        assert len(reused)==3 and all(r['returncode']==0 for r in reused)
        assert {r['name'] for r in reused}=={'core_energy','shock_track','emission'}
        for name,digest in receipt['output_sha256'].items():assert sha(name)==digest,name
        for r in reused:
            expected=next(p for p in command_list if p['name']==r['name'])
            assert expected['command']==r['command']
        command_list=[p for p in command_list if p['name'] not in {r['name'] for r in reused}]
    for p in command_list:
        output=Path(p['command'][p['command'].index('--out')+1])
        if reused and p['name']=='image_v2':
            assert output.is_dir() and not list(output.iterdir()),'Failed image directory must remain empty'
        else:assert not output.exists(),'Existing output; inspect before resuming: '+p['name']
    for name,digest in frozen.items():assert sha(name)==digest,name
    return {'recorded':now(),'registry_row':row,'final_t_ns':last['t_ns'],'budget_rows':len(budget),'closure_pct':closure,'input_sha256':frozen,'commands':command_list,'reused_completed_commands':reused}


def verify(frozen,run=None):
    assert sha(Path(__file__))==frozen['runner_sha256']
    for name,digest in frozen['input_sha256'].items():assert sha(name)==digest,'Pinned runtime input changed: '+name
    if run:
        for name,digest in run['input_sha256'].items():assert sha(name)==digest,'Completed raw input changed: '+name


def check():
    assert not JOB.exists(),'Job directory already exists'
    old=Path('D:/sim_runs/T021_T023_post_04')
    assert not psutil.pid_exists(31064) and (old/'exitcode.txt').read_text().strip()=='1'
    assert json.loads((old/'postprocess_summary.json').read_text())['state']=='failed'
    assert (MAIN/'results/T023/high_thin_G0/it1/peak_lift_execution.json').exists()
    commit=subprocess.check_output(['git','-C',str(RUNTIME),'rev-parse','HEAD'],text=True).strip();assert commit.startswith('1c71670')
    inputs=[RESUME,PYTHON,TABLES/'eos.bin',TABLES/'opt.bin',RUNTIME/'configs/imaging/v2.yaml',RUNTIME/'data/params/registry.yaml',RUNTIME/'results/surface/surface_rows.csv']
    inputs+=sorted((RUNTIME/'src/lipsim').rglob('*.py'))
    inputs+=sorted(p for p in (RUNTIME/'data/exp').rglob('*') if p.is_file() and 'paper' not in p.parts)
    inputs += [RUNTIME/f'scripts/{n}.py' for n in ['core_energy','shock_track','diag_emission','peak_lift']]
    ready=[];pending=[];seen=set()
    for batch,member,it in SLOTS:
        row=selected(batch,member,it)
        if row is None:pending.append([batch,member,it]);continue
        key=(batch,member,'it'+row['picard_it'])
        if key not in seen:ready.append(admission(row));seen.add(key)
    assert pending in [[],[['T023','high_thin_G0','it2']]],pending
    return {'recorded':now(),'runtime':str(RUNTIME),'runtime_commit':commit,'slots':SLOTS,'ready_at_preflight':ready,'pending_production':pending,'input_sha256':{str(p):sha(p) for p in inputs},'runner_sha256':sha(Path(__file__)),'workers':24,'policy':'Sequential members,3 single-thread diagnostics then24-worker image then peak. BelowNormal,OMP/BLAS1. Latest low chosen from completed registry at its stage; preserve old low-it0 remainder if latest is newer. Wait for high-it2 production only in this background wrapper; no production process stop. Each admitted run must have rc0,final budget,rank0 Done,stable hashes; T023 closure<=2%. Pinned source tree is never pulled.'}


def execute(plan,env):
    name='_'.join([plan['batch'],plan['member'],plan['iteration'],plan['name']]);log=JOB/(name+'.log')
    row={**plan,'started':now(),'log':str(log)};begin=time.perf_counter();print('start',name,flush=True)
    with log.open('wb') as f:row['returncode']=subprocess.run(plan['command'],cwd=RUNTIME,env=env,stdout=f,stderr=subprocess.STDOUT).returncode
    row.update(seconds=time.perf_counter()-begin,finished=now());print(json.dumps(row),flush=True);return row


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check-only',action='store_true');args=parser.parse_args()
    if args.check_only:
        f=check();save(EVIDENCE,f);print(json.dumps({'ready_runs':len(f['ready_at_preflight']),'pending_production':f['pending_production'],'inputs':len(f['input_sha256']),'workers':24}));return
    f=json.loads(EVIDENCE.read_text());assert not (JOB/'postprocess_summary.json').exists();verify(f)
    psutil.Process().nice(psutil.BELOW_NORMAL_PRIORITY_CLASS)
    env=dict(os.environ,PYTHONPATH=str(RUNTIME/'src'),PYTHONUTF8='1',PYTHONUNBUFFERED='1',PYTHONDONTWRITEBYTECODE='1',OMP_NUM_THREADS='1',OPENBLAS_NUM_THREADS='1',MKL_NUM_THREADS='1')
    summary={'job_id':JOB.name,'started':now(),'state':'running','preflight':f,'admissions':[],'commands':[],'milestones_ready_for_collection':[]};path=JOB/'postprocess_summary.json';save(path,summary);done=set()
    try:
        for batch,member,it in SLOTS:
            row=selected(batch,member,it)
            if row is None:
                summary.update(state='waiting_for_completed_production',waiting_for=[batch,member,it],waiting_since=now());save(path,summary)
                while row is None:time.sleep(30);row=selected(batch,member,it)
                summary['state']='running';summary.pop('waiting_for',None)
            key=(batch,member,'it'+row['picard_it'])
            if key in done:continue
            run=admission(row);verify(f,run);summary['admissions'].append(run);save(path,summary)
            with ThreadPoolExecutor(max_workers=3) as pool:
                for future in as_completed([pool.submit(execute,p,env) for p in run['commands'] if p['name'] in {'core_energy','shock_track','emission'}]):summary['commands'].append(future.result());save(path,summary)
            assert all(r['returncode']==0 for r in summary['commands']),'Diagnostic failed'
            for p in [p for p in run['commands'] if p['name'] not in {'core_energy','shock_track','emission'}]:
                verify(f,run);summary['commands'].append(execute(p,env));save(path,summary);assert summary['commands'][-1]['returncode']==0,'Postprocessing failed'
            done.add(key);summary['milestones_ready_for_collection'].append({'batch':key[0],'member':key[1],'iteration':key[2],'finished':now(),'next_action':'Collect full image directory and diagnostics/peak; push priority handoff without waiting for remaining queue.'});save(path,summary)
        summary.update(state='completed',finished=now());save(path,summary)
    except Exception as exc:
        summary.update(state='failed',finished=now(),error=repr(exc));save(path,summary);raise


if __name__=='__main__':main()
