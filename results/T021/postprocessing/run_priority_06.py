"""Serialize the November 24 priority queue behind queue05's active member."""
import csv
import ctypes
import json
import msvcrt
import os
from pathlib import Path
import struct
import sys
import time

import psutil
import run_deadline_05 as base


OLD = base.JOB
JOB = OLD.with_name('T023_T025_T026_post_06')
CURRENT = ('T023', 'low_trap_G0', 'it0')
DEFER = ('T021', 'high_thin_G0', 'it1')
FIRST = [('T026','low_thin_G0','it0'), ('T025','high_thin_G0','latest')]
NEXT = [('T023','low_thin_G0','it1'), ('T026','high_thin_G0','it0')]
REST = [('T023','high_trap_G0','it2'), ('T023','low_trap_G0','it1'),
        ('T025','low_thin_G0','it0'), ('T025','low_thin_G0','it1'), DEFER]


def load(path):
    # The live coordinator publishes with os.replace; allow deletion while reading.
    api=ctypes.WinDLL('kernel32',use_last_error=True)
    create=api.CreateFileW
    create.argtypes=[ctypes.c_wchar_p,ctypes.c_uint32,ctypes.c_uint32,ctypes.c_void_p,ctypes.c_uint32,ctypes.c_uint32,ctypes.c_void_p]
    create.restype=ctypes.c_void_p
    handle=create(str(path),0x80000000,7,None,3,0,None)
    if handle==ctypes.c_void_p(-1).value:raise ctypes.WinError(ctypes.get_last_error())
    with os.fdopen(msvcrt.open_osfhandle(handle,os.O_RDONLY),'r',encoding='utf-8-sig') as f:
        return json.load(f)


def key(r):
    return r['batch'], r['member'], r['iteration']


def boundary(s):
    commands = [r for r in s['commands'] if key(r) == CURRENT]
    return (CURRENT in map(key,s['milestones_ready_for_collection']) and
            {r['name'] for r in commands} == {'core_energy','shock_track','emission','image_v2','peak_lift'} and
            all(r['returncode']==0 for r in commands) and not any(OLD.glob('T021_high_thin_G0_it1_*.log')))


def take_turn():
    path = OLD/'postprocess_summary.json'; meta = load(OLD/'job.json'); launch = load(OLD/'affinity_launch.json')
    assert meta['id']==OLD.name and meta['pid']==32784 and launch['pid']==18376
    assert 'launch_deadline_05.py' in meta['cmd']
    while True:
        s=load(path); done=set(map(key,s['milestones_ready_for_collection']))
        if s['state']=='completed':
            assert CURRENT in done and DEFER in done
            return 'queue05 finished naturally',done
        assert s['state']=='running',s['state']
        p=psutil.Process(launch['pid'])
        assert abs(p.create_time()-launch['creation_epoch']) < .01 and p.ppid()==meta['pid']
        assert Path(p.exe()).samefile(base.PYTHON) and Path(p.cmdline()[-1]).name=='launch_deadline_05.py'
        if boundary(s) and not p.children():
            p.suspend()
            stopped=False
            try:
                if boundary(load(path)) and not p.children():
                    # Only the coordinator. The shell wrapper records its actual exit code.
                    p.terminate(); stopped=True; p.wait(timeout=10)
                    return 'queue05 stopped at completed low-trap it0 boundary',done
            finally:
                if not stopped and p.is_running(): p.resume()
        peak=OLD/'T023_low_trap_G0_it0_peak_lift.log'
        time.sleep(.02 if peak.exists() and CURRENT not in done else 1)


def admit(row):
    run=base.admission(row); batch,member,it=row['batch'],row['run'],'it'+row['picard_it']
    if batch in ('T025','T026'):
        raw=Path(row['dir'])
        with (raw/'budget.csv').open(newline='') as f:
            budget=[{k:float(v) for k,v in r.items()} for r in csv.DictReader(f)]
        def residual(r):
            return sum(r[k]-budget[0][k] for k in ('E_internal','E_kinetic','E_rad','E_wall'))-sum(r[k]-budget[0][k] for k in ('E_src','E_gas_abs'))
        ref=residual(min(budget,key=lambda r:abs(r['t_ns']-300)))
        growth=max(residual(r) for r in budget if r['t_ns']>=300)-ref
        limit=max(2e-7,.1*abs(ref));run['post_window']={'reference_J':ref,'growth_J':growth,'limit_J':limit}
        assert growth<=limit,run['post_window']
        out=raw/'peak_lift.csv'; assert not out.exists()
        run['commands'].append(dict(batch=batch,member=member,iteration=it,name='peak_lift',command=[str(base.PYTHON),str(base.RUNTIME/'scripts/peak_lift.py'),str(raw/'image_v2'),'--series',member.split('_')[0],'--out',str(out)]))
    return run


def configure():
    receipt=load(OLD/'affinity_launch.json');api=ctypes.WinDLL('kernel32',use_last_error=True)
    api.GetCurrentThread.restype=ctypes.c_void_p
    api.GetThreadGroupAffinity.argtypes=[ctypes.c_void_p,ctypes.c_void_p]
    buf=ctypes.create_string_buffer(16);assert api.GetThreadGroupAffinity(api.GetCurrentThread(),buf)
    assert struct.unpack_from('<H',buf.raw,8)[0]==receipt['processor_group']
    lanes=receipt['logical_processors'];assert len(set(lanes))==24
    p=psutil.Process();p.cpu_affinity(lanes);p.nice(psutil.BELOW_NORMAL_PRIORITY_CLASS);assert p.cpu_affinity()==lanes
    prefix=base.PYTHON.parent
    return dict(os.environ,PATH=os.pathsep.join(map(str,[prefix,prefix/'Library/bin',prefix/'Scripts',os.environ['PATH']])),PYTHONPATH=str(base.RUNTIME/'src'),PYTHONUTF8='1',PYTHONUNBUFFERED='1',PYTHONDONTWRITEBYTECODE='1',OMP_NUM_THREADS='1',OPENBLAS_NUM_THREADS='1',MKL_NUM_THREADS='1')


def main():
    frozen=load(base.EVIDENCE);base.verify(frozen)
    assert base.subprocess.check_output(['git','-C',str(base.RUNTIME),'rev-parse','HEAD'],text=True).strip()==frozen['runtime_commit']
    env=configure()
    if sys.argv[1:]==['--check-only']:
        for slot in FIRST:
            run=admit(base.selected(*slot)); print(slot,run['post_window'],flush=True)
        return
    assert not sys.argv[1:]
    path=JOB/'postprocess_summary.json'; assert not path.exists()
    s={'job_id':JOB.name,'started':base.now(),'state':'waiting_for_queue05_boundary','slots':FIRST+NEXT+REST,'runner_sha256':base.sha(__file__),'workers':24,'preflight':{'runtime_commit':frozen['runtime_commit'],'input_sha256':frozen['input_sha256']},'admissions':[],'commands':[],'milestones_ready_for_collection':[]};base.save(path,s)
    try:
        s['transition'],done=take_turn();base.JOB=JOB
        for group in [[FIRST[0]],[FIRST[1]],NEXT,REST]:
            pending=list(group)
            while pending:
                for slot in pending:
                    if slot in done:
                        pending.remove(slot);break
                    row=base.selected(*slot)
                    if row is None:continue
                    run=admit(row);base.verify(frozen,run)
                    s.update(state='running',active=[row['batch'],row['run'],'it'+row['picard_it']]);s['admissions'].append(run);s.pop('pending',None);base.save(path,s)
                    for plan in run['commands']:
                        base.verify(frozen,run);s['stage']=plan['name'];base.save(path,s)
                        item=base.execute(plan,env);s['commands'].append(item);base.save(path,s);assert item['returncode']==0,item['log']
                    batch,member,it=s.pop('active');done.add((batch,member,it));s.pop('stage')
                    s['milestones_ready_for_collection'].append(dict(batch=batch,member=member,iteration=it,finished=base.now()));base.save(path,s)
                    pending.remove(slot);break
                else:
                    s.update(state='waiting_for_completed_production',pending=pending);base.save(path,s);time.sleep(30)
        s.update(state='completed',finished=base.now());base.save(path,s)
    except Exception as exc:
        s.update(state='failed',finished=base.now(),error=repr(exc));base.save(path,s);raise


if __name__=='__main__':main()
