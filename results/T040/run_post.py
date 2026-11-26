"""Run the T039 waveform imaging sequence against one completed T040 iteration."""
import argparse
import csv
import hashlib
import json
import os
from datetime import datetime
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[2]
RUNS = Path('D:/sim_runs')
RUNTIME = Path('D:/sim_tools/T039-runtime-f90a4fa9')
STAGES = ['image_v2', 'peak_lift', 'shock_track', 'core_energy', 'emission', 'image_v2_t28', 'peak_lift_t28']


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('member', choices=['low_wf_chain', 'high_wf_chain', 'high_wf_G1'])
    parser.add_argument('iteration', choices=['it0', 'it1', 'it2'])
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    series = args.member.split('_')[0]
    raw = RUNS / 'T040' / args.member / args.iteration
    job = RUNS / f'T040_{args.member}_{args.iteration}_post'
    with (ROOT / 'results/run_registry.csv').open(newline='') as registry:
        row, = [r for r in csv.DictReader(registry) if r['dir'] == str(raw) and r['batch'] == 'T040']
    assert row['returncode'] == '0' and not os.getenv('LIPSIM_OVERRIDES')
    assert hashlib.sha256((raw / 'pluto.ini').read_bytes()).hexdigest().startswith(row['ini_sha256'])
    end_time = float((raw / 'budget.csv').read_text().splitlines()[-1].split(',')[0])
    assert end_time == (1300 if series == 'low' else 800)
    original = RUNS / 'T039' / f'{series}_wf' / ('it1' if series == 'low' else 'it2')
    receipt = RUNS / f'T039_{series}_wf_post' / 'stages.jsonl'
    previous = [json.loads(line) for line in receipt.read_text().splitlines()]
    assert [rec['stage'] for rec in previous] == STAGES
    commands = []
    for rec in previous:
        assert rec['returncode'] == 0
        cmd = [arg.replace(str(original), str(raw)) for arg in rec['command']]
        dest = Path(cmd[cmd.index('--out') + 1])
        assert dest.is_relative_to(raw) and not dest.exists(), dest
        commands.append(cmd)
    if args.check:
        assert not job.exists(), job
        print(f'{raw}: validated production and {len(commands)} postprocessing stages')
        return
    assert job == Path(os.environ['SIM_OUT_DIR'])
    env = dict(os.environ, PYTHONPATH=str(RUNTIME / 'src'), PYTHONUTF8='1', PYTHONDONTWRITEBYTECODE='1',
               OMP_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1', NUMEXPR_NUM_THREADS='1')
    prefix = Path(sys.executable).parent
    env['PATH'] = os.pathsep.join(map(str, [prefix, prefix/'Library/bin', prefix/'Scripts', env['PATH']]))
    (raw / 'diagnostics').mkdir(exist_ok=True)
    with (job / 'stages.jsonl').open('x') as ledger:
        for stage, cmd in zip(STAGES, commands):
            record = dict(stage=stage, command=cmd, started=datetime.now().astimezone().isoformat())
            print(stage, flush=True)
            with (job / f'{stage}.log').open('xb') as log:
                rc = subprocess.run(cmd, cwd=RUNTIME, env=env, stdout=log, stderr=subprocess.STDOUT).returncode
            record.update(returncode=rc, finished=datetime.now().astimezone().isoformat())
            print(json.dumps(record), file=ledger, flush=True)
            if rc:
                sys.exit(rc)


if __name__ == '__main__':
    main()
