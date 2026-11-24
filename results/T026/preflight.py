"""Freeze the supplied T026 build/inputs and measure the production launch gate."""
from collections import Counter
from datetime import datetime
from pathlib import Path
import hashlib
import json

import psutil
import yaml
from lipsim import params as P
from lipsim.batch import make_ini

REPO = Path(__file__).resolve().parents[2]
OUT = REPO / 'results/T026'


def sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def main():
    assert not (OUT / 'preflight.json').exists(), 'Preserve previous preflight evidence'
    batch_path = REPO / 'configs/batches/T026.yaml'
    batch = yaml.safe_load(batch_path.read_text(encoding='utf-8'))
    build = json.loads((OUT / 'build_test_execution.json').read_text(encoding='utf-8'))
    assert all(r['returncode'] == 0 for r in build['commands'])
    assert sha(batch['pluto_exe']) == build['executable_sha256']
    assert not Path(batch['root']).exists(), 'T026 raw/job directory already exists; inspect before launch'
    inputs = [batch_path, REPO / 'configs/grids/levels.yaml', REPO / 'data/params/registry.yaml',
              REPO / 'scripts/build_pluto.py', REPO / 'scripts/win/job.ps1',
              REPO / 'tests/test_breakdown.py', REPO / 'tests/test_knight_bp.py',
              OUT / 'build_and_test.py', Path(__file__), Path(batch['pluto_exe']),
              Path(batch['mpiexec']), Path(batch['tables']) / 'eos.bin', Path(batch['tables']) / 'opt.bin']
    inputs += sorted(p for p in (REPO / 'pluto/lip').iterdir() if p.is_file())
    inputs += sorted((REPO / 'src/lipsim').rglob('*.py'))
    previews = []
    for spec in batch['numerics']['runs']:
        case_path = REPO / spec['case']
        inputs.append(case_path)
        case = P.resolve(yaml.safe_load(case_path.read_text(encoding='utf-8')))
        assert case['target']['droplets'] == 'upper'
        assert case['target']['droplet_release'] == 'energy_limited'
        assert case['target']['back_pressure'] is True
        assert spec['procs'] == 8 and spec['grid'] == 'G0' and spec['nec'] == 'thin'
        text = make_ini(case, spec['grid'], spec['nec'], spec['tstop_ns'],
                        spec.get('src_on', 1), spec.get('rmax_par'), 0.0)
        values = {r.split()[0]: float(r.split()[1]) for r in text.splitlines()
                  if r.split() and r.split()[0] in ['BD_ON', 'BACKP', 'DROP_TB', 'DROP_M', 'rmax_par']}
        assert values['BD_ON'] == 0 and values['BACKP'] == 1
        assert values['DROP_TB'] == 2835 and values['DROP_M'] > 0 and values['rmax_par'] == 1000
        path = OUT / 'preflight_ini' / (spec['id'] + '.ini')
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding='utf-8')
        previews.append({'member': spec['id'], 'parameters': values,
                         'ini': path.relative_to(REPO).as_posix(), 'ini_sha256': sha(path),
                         'scope': 'Preview with zero vapour only. Recheck actual generated ini with actual iteration source mass after production launch.'})
    ranks = []
    for process in psutil.process_iter(['pid', 'name', 'exe', 'create_time']):
        if (process.info['name'] or '').lower() == 'pluto.exe':
            ranks.append({**process.info, 'cwd': process.cwd()})
    physical = psutil.cpu_count(logical=False)
    assert physical == 64 and len(ranks) <= physical
    protected = []
    for name, pid, epoch in [('T023', 14496, 1795484727.6840496),
                              ('T025', 5984, 1795508264.593215),
                              ('T021_T023_post_05', 32784, 1795505005.892073)]:
        process = psutil.Process(pid)
        assert abs(process.create_time() - epoch) < .01
        protected.append({'job': name, 'pid': pid, 'creation_epoch': epoch, 'identity_matches': True})
    evidence = {'recorded': datetime.now().astimezone().isoformat(),
                'build_source_commit': build['source_commit'], 'executable_sha256': build['executable_sha256'],
                'members': previews, 'input_sha256': {str(p): sha(p) for p in inputs},
                'protected_jobs': protected,
                'resource_gate': {'physical_cores': physical, 'logical_cpus': psutil.cpu_count(),
                                  'active_pluto_ranks': len(ranks), 'free_production_core_slots': physical - len(ranks),
                                  'required_free_physical_cores': 16, 'launch_allowed': physical - len(ranks) >= 16,
                                  'rank_counts_by_directory': dict(Counter(r['cwd'] for r in ranks)), 'rank_processes': ranks,
                                  'policy': 'One primary physical-core slot per MPI rank; existing postprocessing uses its authorized secondary SMT lanes. Recheck all active production reservations before launch; do not stop or oversubscribe T023/T025.'},
                'command': 'D:\\sim_tools\\envs\\lipsim\\python.exe -m lipsim.batch configs/batches/T026.yaml --max-cores 16',
                'next_action': 'After at least16 primary production core slots are free, reverify frozen inputs and launch only through job.ps1. End that pass after immediate bookkeeping.'}
    (OUT / 'preflight.json').write_text(json.dumps(evidence, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'recorded': evidence['recorded'], 'frozen_inputs': len(inputs),
                      'members': previews, 'resource_gate': {k: v for k, v in evidence['resource_gate'].items()
                                                            if k != 'rank_processes'}}, indent=2))


if __name__ == '__main__':
    main()
