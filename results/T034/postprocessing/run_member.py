from pathlib import Path
import sys
import json
import yaml

ROOT = Path(__file__).resolve().parents[3]
sys.path.insert(0, str(ROOT / 'results/T029/postprocessing'))
import run_priority_08 as previous


def main(member, check=False):
    specs = yaml.safe_load((ROOT / 'configs/batches/T034.yaml').read_text())
    spec, = [r for r in specs['numerics']['runs'] if r['id'] == member]
    series = member.split('_')[0]
    tables = Path(spec.get('tables', specs['tables']))
    iteration = 'it2' if series == 'high' else 'it1'
    slot = ('T034', member, iteration)
    base, q = previous.base, previous.q
    job = Path('D:/sim_runs') / ('T034_' + member + '_post')
    path = job / 'postprocess_summary.json'
    assert not path.exists(), 'Existing imaging record; inspect before relaunch'
    run = previous.admission(slot)
    assert run and run['registry_row']['nec'] == 'thin'
    frozen = q.load(base.EVIDENCE)
    base.verify(frozen, run)
    head = base.subprocess.check_output(['git', '-C', str(base.RUNTIME), 'rev-parse', 'HEAD'], text=True)
    assert head.strip() == frozen['runtime_commit']
    raw = Path(run['registry_row']['dir'])
    override_file = raw / 'overrides.json'
    assert json.loads(override_file.read_text()) == {k: float(v) for k, v in spec['overrides'].items()}
    run['input_sha256'][str(override_file)] = base.sha(override_file)
    manifest = json.loads((tables / 'manifest.json').read_text())
    for fname in ('eos.bin','opt.bin','pop.bin'):
        digest = base.sha(tables / fname)
        assert digest == manifest['files'][fname]['sha256']
        run['input_sha256'][str(tables / fname)] = digest
    for item in run['commands']:
        cmd = item['command']
        if '--tables' in cmd:
            cmd[cmd.index('--tables') + 1] = str(tables)
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
