from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[3]
sys.path.insert(0, str(ROOT / 'results/T029/postprocessing'))
import run_priority_08 as previous


def main(member, check=False):
    series, size = member.split('_')
    assert series in ('high', 'low') and size in ('v1s', 'v1rs')
    iteration = 'it2' if series == 'high' else 'it1'
    slot = ('T031', member, iteration)
    base, q = previous.base, previous.q
    job = Path('D:/sim_runs') / ('T031_' + member + '_post')
    path = job / 'postprocess_summary.json'
    assert not path.exists(), 'Existing imaging record; inspect before relaunch'
    run = previous.admission(slot)
    assert run and run['registry_row']['nec'] == 'thin'
    frozen = q.load(base.EVIDENCE)
    base.verify(frozen, run)
    head = base.subprocess.check_output(['git', '-C', str(base.RUNTIME), 'rev-parse', 'HEAD'], text=True)
    assert head.strip() == frozen['runtime_commit']
    raw = Path(run['registry_row']['dir'])
    if size == 'v1rs':
        run['input_sha256'][str(raw / 'droplets.csv')] = base.sha(raw / 'droplets.csv')
    steps = {c['name']: c for c in run['commands']}
    # The member suffix identifies the model; the registry supplies the NEC.
    core = steps['core_energy']['command']
    core[core.index('--nec') + 1] = run['registry_row']['nec']
    stages = ['image_v2', 'core_energy', 'shock_track', 'emission', 'peak_lift']
    assert set(stages) == set(steps)
    run['commands'] = [steps[name] for name in stages]
    if check:
        print(slot, 'verified', stages, '8 workers, thin NEC')
        return
    base.JOB = job
    env = q.configure()
    proc = q.psutil.Process()
    lanes = sorted(proc.cpu_affinity())[:8]
    assert len(lanes) == 8
    proc.cpu_affinity(lanes)
    s = dict(job_id=job.name, started=base.now(), state='running', active=slot,
             workers=8, affinity=lanes, runtime_commit=frozen['runtime_commit'],
             runner_sha256=base.sha(__file__), admissions=[run], commands=[], milestones_ready_for_collection=[])
    try:
        for name in stages:
            s['stage'] = name
            base.save(path, s)
            base.verify(frozen, run)
            result = base.execute(steps[name], env)
            s['commands'].append(result)
            assert result['returncode'] == 0, result['log']
            if name == 'image_v2':
                for mode in ('thin', 'sa'):
                    assert (raw / 'image_v2' / ('frames_b_' + mode + '.npz')).stat().st_size > 0
        base.verify(frozen, run)
        s['milestones_ready_for_collection'].append(dict(batch=slot[0], member=member, iteration=iteration, finished=base.now()))
        s.pop('active'); s.pop('stage')
        s.update(state='completed', finished=base.now())
    except Exception as exc:
        s.update(state='failed', error=repr(exc), finished=base.now())
        raise
    finally:
        base.save(path, s)


if __name__ == '__main__':
    assert len(sys.argv) == 2 or sys.argv[2:] == ['--check-only']
    main(sys.argv[1], len(sys.argv) == 3)
