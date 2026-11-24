"""Recheck frozen inputs and unfinished-member reservations before one launch."""
from collections import Counter
from datetime import datetime
from pathlib import Path
import csv
import hashlib
import json
import psutil
import yaml

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'results/T026'


def main():
    assert not Path('D:/sim_runs/T026').exists(), 'Existing T026 state: inspect, never relaunch blindly'
    preflight = json.loads((OUT / 'preflight.json').read_text())
    for filename, expected in preflight['input_sha256'].items():
        assert hashlib.sha256(Path(filename).read_bytes()).hexdigest() == expected, filename
    registry = list(csv.DictReader((ROOT / 'results/run_registry.csv').open()))
    reservations = []
    for batch_id in ['T023', 'T025']:
        batch = yaml.safe_load((ROOT / f'configs/batches/{batch_id}.yaml').read_text())
        for member in batch['numerics']['runs']:
            final = int(member.get('picard', 0))
            rows = [r for r in registry if (r['batch'], r['run'], int(r['picard_it'])) == (batch_id, member['id'], final)]
            assert len(rows) <= 1
            finished = bool(rows and rows[0]['returncode'] == '0')
            if finished:
                run = Path(rows[0]['dir'])
                budget = list(csv.DictReader((run / 'budget.csv').open()))
                assert float(budget[-1]['t_ns']) == member['tstop_ns']
                with (run / 'pluto.0.log').open('rb') as stream:
                    stream.seek(max(0, (run / 'pluto.0.log').stat().st_size - 8192))
                    assert b'> Done' in stream.read()
            reservations.append({'batch': batch_id, 'member': member['id'], 'final_iteration': final,
                                 'final_rc0_registered': finished, 'reserved_cores': 0 if finished else int(member['procs'])})
    protected = []
    for name, pid, epoch in [('T023', 14496, 1795484727.6840496), ('T025', 5984, 1795508264.593215),
                             ('T021_T023_post_05', 32784, 1795505005.892073)]:
        assert abs(psutil.Process(pid).create_time() - epoch) < .01
        protected.append({'job': name, 'pid': pid, 'creation_epoch': epoch, 'identity_matches': True})
    ranks, launchers = [], []
    for process in psutil.process_iter(['pid', 'ppid', 'name', 'create_time']):
        if process.info['name'].lower() in ['pluto.exe', 'mpiexec.exe']:
            row = {**process.info, 'cwd': process.cwd(), 'command': process.cmdline()}
            ancestors = {p.pid for p in process.parents()}
            assert ancestors.intersection({14496, 5984}), ('Unknown production process', row)
            (ranks if process.info['name'].lower() == 'pluto.exe' else launchers).append(row)
    reserved = sum(r['reserved_cores'] for r in reservations)
    physical = psutil.cpu_count(logical=False)
    assert physical == 64 and len(ranks) <= reserved and physical - reserved >= 16
    evidence = {'recorded': datetime.now().astimezone().isoformat(), 'frozen_input_hashes_verified': len(preflight['input_sha256']),
                'physical_cores': physical, 'reserved_cores': reserved, 'free_slots': physical - reserved,
                'active_ranks': len(ranks), 'rank_counts_by_directory': dict(Counter(r['cwd'] for r in ranks)),
                'reservations': reservations, 'protected_jobs': protected, 'rank_processes': ranks, 'launchers': launchers,
                'launch_allowed': True, 'policy': 'Retain reservations through final Picard iteration, including source-generation gaps. No unknown MPI production process allowed.'}
    (OUT / 'launch_gate.json').write_text(json.dumps(evidence, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({k: v for k, v in evidence.items() if k not in ['rank_processes', 'launchers', 'reservations']}))


if __name__ == '__main__':
    main()
