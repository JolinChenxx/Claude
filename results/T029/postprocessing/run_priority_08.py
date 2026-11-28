"""Run the highest-priority ready member with at most eight image workers."""
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
sys.path.insert(0, str(ROOT / 'results/T021/postprocessing'))
import run_priority_07 as old

base = old.base
q = old.q
JOB = old.JOB.with_name('T029_T028_post_08')
ORDER = [('T029', s + '_thin_G0', 'it' + str(i)) for i in range(3)
         for s in ('high', 'low') if i < 2 or s == 'high']
ORDER += [('T028', s + '_thin_G1', 'it0') for s in ('high', 'low')]
ORDER += [('T025', 'high_thin_G0', 'it0')]


def completed_leftovers():
    jobs = [q.OLD, q.JOB, old.JOB]
    required = {('T021', 'high_thin_G0', 'it1')}
    required.update(('T023', s + '_trap_G0', 'it' + str(i))
                    for s, count in [('high', 3), ('low', 2)] for i in range(count))
    found = {}
    for job in jobs:
        summary = q.load(job / 'postprocess_summary.json')
        for item in summary['milestones_ready_for_collection']:
            slot = q.key(item)
            if slot in required:
                found[slot] = dict(job=job.name, milestone=item)
    assert found.keys() == required, required - found.keys()
    return list(found.values())


def admission(slot):
    run = old.admit(slot, None)
    if run is None:
        return None
    image, = [c['command'] for c in run['commands'] if c['name'] == 'image_v2']
    pos = image.index('--workers') + 1
    assert image[pos] == '24'
    image[pos] = '8'
    return run


def main():
    check = sys.argv[1:] == ['--check-only']
    assert check or not sys.argv[1:]
    frozen = q.load(base.EVIDENCE)
    base.verify(frozen)
    assert base.subprocess.check_output(['git', '-C', str(base.RUNTIME), 'rev-parse', 'HEAD'], text=True).strip() == frozen['runtime_commit']
    receipts = completed_leftovers()
    path = JOB / 'postprocess_summary.json'
    assert not path.exists(), 'Existing queue 08 evidence; do not overwrite'
    if check:
        for slot in ORDER:
            run = admission(slot)
            if run is not None:
                base.verify(frozen, run)
        print('Preflight passed: 8 workers; completed leftovers checked:', len(receipts))
        return
    env = q.configure()
    proc = q.psutil.Process()
    lanes = sorted(proc.cpu_affinity())[:8]
    assert len(lanes) == 8
    proc.cpu_affinity(lanes)
    assert sorted(proc.cpu_affinity()) == lanes
    base.JOB = JOB
    pending = list(ORDER)
    s = dict(job_id=JOB.name, started=base.now(), workers=8, affinity=lanes,
             runner_sha256=base.sha(__file__), slots=ORDER, already_completed=receipts,
             preflight={k: frozen[k] for k in ('runtime_commit', 'input_sha256')},
             state='running', admissions=[], commands=[], milestones_ready_for_collection=[])
    base.save(path, s)
    try:
        while pending:
            for slot in pending:
                run = admission(slot)
                if run is None:
                    continue
                base.verify(frozen, run)
                s.update(state='running', active=slot, pending=pending)
                s['admissions'].append(run)
                for cmd in run['commands']:
                    s['stage'] = cmd['name']; base.save(path, s)
                    base.verify(frozen, run)
                    result = base.execute(cmd, env)
                    s['commands'].append(result); base.save(path, s)
                    assert result['returncode'] == 0, result['log']
                s['milestones_ready_for_collection'].append(dict(batch=slot[0], member=slot[1], iteration=slot[2], finished=base.now()))
                pending.remove(slot); s.pop('active'); s.pop('stage'); base.save(path, s)
                break
            else:
                s.update(state='waiting_for_production', pending=pending); base.save(path, s)
                time.sleep(30)
        s.update(state='completed', finished=base.now()); base.save(path, s)
    except Exception as exc:
        s.update(state='failed', error=repr(exc), finished=base.now()); base.save(path, s)
        raise


if __name__ == '__main__':
    main()
