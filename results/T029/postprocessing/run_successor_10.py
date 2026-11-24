from pathlib import Path
import sys
import time

import run_priority_08 as previous

base, q = previous.base, previous.q
OLD = previous.JOB
JOB = OLD.with_name('T029_T028_post_10')
HIGH = ('T029', 'high_thin_G0', 'it0')
ORDER = [('T029', 'high_thin_G0', 'it1'), ('T029', 'high_thin_G0', 'it2'), ('T028', 'high_thin_G1', 'it0')]
LOW = [('T029', 'low_thin_G0', it) for it in ('it0', 'it1')]
FINISHED = {('T025', 'high_thin_G0', 'it0'), ('T028', 'low_thin_G1', 'it0')}


def coordinator():
    m = q.load(OLD / 'job.json')
    assert m['id'] == OLD.name and m['pid'] == 41948 and m['commit'] == '798896d'
    p = q.psutil.Process(41384)
    assert abs(p.create_time() - 1795543518.059969) < .001 and p.ppid() == m['pid']
    assert Path(p.exe()).samefile(base.PYTHON)
    assert p.cmdline()[1:] == ['-B', str(Path('results/T029/postprocessing/run_priority_08.py'))]
    return p


def idle(s):
    return ('active' not in s and 'stage' not in s and
            previous.old.complete(s, HIGH, previous.old.STAGES))


def timeline(s):
    assert s['state'] == 'running', s['state']
    assert {q.key(r['commands'][0]) for r in s['admissions']} == FINISHED | {HIGH}, 'Unexpected admission; do not stop queue 08'
    assert set(map(q.key, s['milestones_ready_for_collection'])) >= FINISHED


def boundary():
    p = coordinator()
    while p.is_running():
        s = q.load(OLD / 'postprocess_summary.json')
        timeline(s)
        if idle(s) and not p.children():
            p.suspend()
            retired = False
            try:
                s = q.load(OLD / 'postprocess_summary.json')
                timeline(s)
                if idle(s) and not p.children():
                    p.terminate(); retired = True; p.wait(timeout=10)
                    return dict(pid=p.pid, finished=base.now(), milestones=s['milestones_ready_for_collection'])
            finally:
                if not retired and p.is_running():
                    p.resume()
        time.sleep(.003 if s.get('stage') == 'peak_lift' else .1)
    raise RuntimeError('Coordinator exited before verified idle handover')


def preflight():
    frozen = q.load(base.EVIDENCE)
    base.verify(frozen)
    assert base.subprocess.check_output(['git','-C',str(base.RUNTIME),'rev-parse','HEAD'],text=True).strip() == frozen['runtime_commit']
    old = q.load(OLD / 'postprocess_summary.json')
    assert old['runner_sha256'] == base.sha(previous.__file__)
    timeline(old); coordinator()
    assert set(previous.ORDER) == set(ORDER + LOW + [HIGH]) | FINISHED
    leftovers = previous.completed_leftovers()
    for slot in LOW:
        job = OLD.with_name('T029_low_' + slot[2] + '_post_09')
        s = q.load(job / 'postprocess_summary.json')
        assert s['state'] == 'completed' and (job / 'exitcode.txt').read_text().strip() == '0'
        assert previous.old.complete(s, slot, previous.old.STAGES)
        receipt = q.load(previous.ROOT / 'results' / Path(*slot) / 'priority_delivery.json')
        assert q.key(receipt['milestone']) == slot
    return frozen, leftovers


def main():
    path = JOB / 'postprocess_summary.json'
    assert not path.exists()
    frozen, leftovers = preflight()
    if sys.argv[1:] == ['--check-only']:
        print('Verified coordinator identity, predecessor coverage, low deliveries and pinned runtime.')
        return
    assert not sys.argv[1:]
    env = q.configure(); base.JOB = JOB
    s = dict(job_id=JOB.name, started=base.now(), state='waiting_for_high_it0_boundary', workers=24,
             slots=ORDER, pending=list(ORDER), runner_sha256=base.sha(__file__), already_completed=leftovers,
             independent_low=LOW, preflight={k:frozen[k] for k in ('runtime_commit','input_sha256')},
             admissions=[], commands=[], milestones_ready_for_collection=[])
    base.save(path, s)
    try:
        s['transition'] = boundary(); base.save(path, s)
        while s['pending']:
            for slot in s['pending']:
                run = previous.old.admit(slot, None)
                if run is None:
                    continue
                base.verify(frozen, run); s.update(state='running', active=slot)
                s['admissions'].append(run)
                for cmd in run['commands']:
                    s['stage'] = cmd['name']; base.save(path, s)
                    base.verify(frozen, run)
                    result = base.execute(cmd, env); s['commands'].append(result); base.save(path, s)
                    assert result['returncode'] == 0, result['log']
                s['milestones_ready_for_collection'].append(dict(batch=slot[0], member=slot[1], iteration=slot[2], finished=base.now()))
                s['pending'].remove(slot); s.pop('active'); s.pop('stage'); base.save(path, s)
                break
            else:
                s.update(state='waiting_for_production'); base.save(path, s); time.sleep(30)
        s.update(state='completed', finished=base.now()); base.save(path, s)
    except Exception as exc:
        s.update(state='failed', finished=base.now(), error=repr(exc)); base.save(path, s)
        raise


if __name__ == '__main__':
    main()
