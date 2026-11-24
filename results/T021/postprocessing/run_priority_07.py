from pathlib import Path
import sys
import time

import psutil
import run_priority_06 as q

base = q.base
OLD = q.JOB
JOB = OLD.with_name('T026_T027_post_07')
CURRENT = ('T025', 'low_thin_G0', 'it1')
LAST = ('T021', 'high_thin_G0', 'it1')
ORDER = [('T026','high_thin_G0','it1'), ('T026','high_thin_G0','it2'), ('T026','low_thin_G0','it1')]
ORDER += [('T027',s+'_thin_G0','it'+str(i)) for i in range(3) for s in ('high','low') if i < 2 or s == 'high']
ORDER += [('T025','high_thin_G0','it2'), LAST]
STAGES = {'core_energy','shock_track','emission','image_v2','peak_lift'}


def complete(s, slot, stages):
    cmds = [r for r in s['commands'] if q.key(r) == slot]
    return (slot in map(q.key,s['milestones_ready_for_collection']) and
            len(cmds) == len(stages) and {r['name'] for r in cmds} == stages and
            all(r['returncode'] == 0 for r in cmds))


def boundary():
    meta = q.load(OLD/'job.json'); path = OLD/'postprocess_summary.json'
    assert meta['id'] == OLD.name and meta['pid'] == 14288
    assert Path(meta['cmd'].split()[-1]).name == 'run_priority_06.py'
    while True:
        s = q.load(path)
        if s['state'] == 'completed':
            assert complete(s,CURRENT,STAGES) and complete(s,LAST,STAGES-{'peak_lift'})
            return s,'old queue completed naturally'
        assert s['state'] == 'running', s['state']
        if complete(s,CURRENT,STAGES) and not any(OLD.glob('T021_high_thin_G0_it1_*.log')):
            p = psutil.Process(41948)
            assert abs(p.create_time()-1795518211.1627965) < .01 and p.ppid() == meta['pid']
            assert Path(p.exe()).samefile(base.PYTHON) and Path(p.cmdline()[-1]).name == 'run_priority_06.py'
            if not p.children():
                p.suspend(); stopped = False
                try:
                    s = q.load(path)
                    if complete(s,CURRENT,STAGES) and not p.children() and not any(OLD.glob('T021_high_thin_G0_it1_*.log')):
                        p.terminate(); stopped = True; p.wait(timeout=10)
                        return s,'old coordinator stopped after T025 low it1; no child running'
                finally:
                    if not stopped and p.is_running(): p.resume()
        time.sleep(.02 if s.get('stage') == 'peak_lift' else .5)


def admit(slot, previous):
    row = base.selected(*slot)
    if row is None: return None
    if slot == LAST and previous and complete(previous, LAST, STAGES-{'peak_lift'}):
        before, = [r for r in previous['admissions'] if r['registry_row'] == row]
        run = dict(before, commands=[], reused_completed_commands=[r for r in previous['commands'] if q.key(r)==slot])
    else:
        run = q.admit(row)
    covered = {c['name'] for c in run['commands'] + run['reused_completed_commands']}
    if 'peak_lift' not in covered:
        raw = Path(row['dir']); target = raw/'peak_lift.csv'
        assert not target.exists(), str(target)
        run['commands'].append(dict(batch=slot[0], member=slot[1], iteration=slot[2], name='peak_lift',
            command=[str(base.PYTHON),str(base.RUNTIME/'scripts/peak_lift.py'),str(raw/'image_v2'),
                     '--series',slot[1].split('_')[0],'--out',str(target)]))
    assert {c['name'] for c in run['commands'] + run['reused_completed_commands']} == STAGES
    return run


def main():
    frozen = q.load(base.EVIDENCE); base.verify(frozen)
    assert base.subprocess.check_output(['git','-C',str(base.RUNTIME),'rev-parse','HEAD'],text=True).strip()==frozen['runtime_commit']
    if sys.argv[1:] == ['--check-only']:
        assert not JOB.exists()
        for slot in ORDER:
            if slot != LAST: admit(slot, None)
        print('Eligible priority-3 inputs verified.'); return
    assert not sys.argv[1:]
    env = q.configure(); path = JOB/'postprocess_summary.json'; assert not path.exists()
    s = dict(job_id=JOB.name, started=base.now(), state='waiting_for_06b_boundary', slots=ORDER,
             runner_sha256=base.sha(__file__), workers=24, preflight={k:frozen[k] for k in ('runtime_commit','input_sha256')},
             admissions=[], commands=[], milestones_ready_for_collection=[])
    base.save(path,s)
    try:
        previous,s['transition'] = boundary(); base.JOB = JOB
        assert complete(previous,CURRENT,STAGES)
        pending = list(ORDER)
        while pending:
            for slot in pending:
                run = admit(slot,previous)
                if run is None: continue
                base.verify(frozen,run); s.update(state='running',active=slot)
                s.pop('pending',None); s['admissions'].append(run); base.save(path,s)
                for c in run['commands']:
                    base.verify(frozen,run); s['stage']=c['name']; base.save(path,s)
                    result=base.execute(c,env); s['commands'].append(result); base.save(path,s)
                    assert result['returncode']==0, result['log']
                s['milestones_ready_for_collection'].append(dict(batch=slot[0],member=slot[1],iteration=slot[2],finished=base.now()))
                s.pop('active'); s.pop('stage'); pending.remove(slot); base.save(path,s); break
            else:
                s.update(state='waiting_for_production',pending=pending); base.save(path,s); time.sleep(30)
        s.update(state='completed',finished=base.now()); base.save(path,s)
    except Exception as exc:
        s.update(state='failed',error=repr(exc),finished=base.now()); base.save(path,s); raise


if __name__ == '__main__':
    main()
