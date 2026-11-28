from pathlib import Path
import sys

import run_priority_08 as queue


def main(iteration, check=False):
    assert iteration in ('it0', 'it1')
    base, q = queue.base, queue.q
    slot = ('T029', 'low_thin_G0', iteration)
    job = queue.JOB.with_name('T029_low_' + iteration + '_post_09')
    summary_path = job / 'postprocess_summary.json'
    output = job / 'products'
    assert not summary_path.exists() and not output.exists(), job
    old = q.load(queue.JOB / 'postprocess_summary.json')
    assert old['workers'] == 8 and not any(q.key(r['commands'][0]) == slot for r in old['admissions'])
    run = queue.admission(slot)
    assert run is not None, slot
    frozen = q.load(base.EVIDENCE)
    base.verify(frozen, run)
    assert base.subprocess.check_output(['git', '-C', str(base.RUNTIME), 'rev-parse', 'HEAD'], text=True).strip() == frozen['runtime_commit']
    raw = Path(run['registry_row']['dir'])
    for step in run['commands']:
        cmd = step['command']
        pos = cmd.index('--out') + 1
        cmd[pos] = str(output / Path(cmd[pos]).relative_to(raw))
        if step['name'] == 'peak_lift':
            cmd[cmd.index(str(raw / 'image_v2'))] = str(output / 'image_v2')
    if check:
        print(slot, 'verified; outputs:', output)
        return
    env = q.configure()
    proc = q.psutil.Process()
    start = 8 if iteration == 'it0' else 16
    lanes = sorted(proc.cpu_affinity())[start:start + 8]
    assert len(lanes) == 8 and not set(lanes).intersection(old['affinity'])
    proc.cpu_affinity(lanes)
    assert sorted(proc.cpu_affinity()) == lanes
    base.JOB = job
    output.mkdir(exist_ok=False)
    s = dict(job_id=job.name, started=base.now(), state='running', active=slot,
             pid=proc.pid, workers=8, affinity=lanes, output_root=str(output),
             runner_sha256=base.sha(__file__), runtime_commit=frozen['runtime_commit'],
             admissions=[run], commands=[], milestones_ready_for_collection=[])
    try:
        for cmd in run['commands']:
            s['stage'] = cmd['name']
            base.save(summary_path, s)
            base.verify(frozen, run)
            result = base.execute(cmd, env)
            s['commands'].append(result)
            assert result['returncode'] == 0, result['log']
        base.verify(frozen, run)
        s['milestones_ready_for_collection'].append(dict(batch=slot[0], member=slot[1], iteration=iteration, finished=base.now()))
        s.pop('active'); s.pop('stage')
        s.update(state='completed', finished=base.now())
    except Exception as exc:
        s.update(state='failed', error=repr(exc), finished=base.now())
        raise
    finally:
        base.save(summary_path, s)


if __name__ == '__main__':
    assert len(sys.argv) == 2 or sys.argv[2:] == ['--check-only']
    main(sys.argv[1], len(sys.argv) == 3)
