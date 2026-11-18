"""One sequential reference-imaging stage inside the job.ps1 native queue."""
from pathlib import Path
import datetime
import hashlib
import json
import subprocess
import sys

repo = Path(__file__).resolve().parents[2]
out = Path('D:/sim_runs/T017a_image_v2_01')
out.mkdir(parents=True, exist_ok=True)
state = out / 'stage.json'
if state.exists():
    previous = json.loads(state.read_text())
    if previous['state'] == 'finished':
        print('T017a reference stage already completed', flush=True)
        raise SystemExit(0)
    raise RuntimeError('T017a stage incomplete or failed; inspect stage.json before retry')

prior = json.loads(Path('D:/sim_runs/T016i_native_01/batch_summary.json').read_text())
required = {(batch, series) for batch in ('T016e', 'T015') for series in ('high', 'low')}
completed = {(item['batch'], item['series']) for item in prior if item['returncode'] == 0}
if not required.issubset(completed):
    raise RuntimeError('T016e/T015 native series have not all completed successfully')

def now():
    return datetime.datetime.now().astimezone().isoformat()

record = dict(state='running', started=now(), label='reference imaging v2',
              authority='ca2d75b / T017a_verified.md',
              parent_job='T016i_native_01',
              prerequisite_evidence=prior,
              parent_metadata=json.loads(Path('D:/sim_runs/T016i_native_01/job.json').read_text(encoding='utf-8-sig')),
              commit=subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=repo, text=True).strip(),
              inputs={name:hashlib.sha256((repo/name).read_bytes()).hexdigest()
                      for name in ('configs/imaging/v2.yaml', 'data/params/registry.yaml',
                                   'src/lipsim/image/forward.py')}, commands=[])
state.write_text(json.dumps(record, indent=2)+'\n')
try:
    with (out/'stdout.log').open('w') as stdout, (out/'stderr.log').open('w') as stderr:
        for series in ('high', 'low'):
            command = [sys.executable, str(repo/'scripts/image_members.py'),
                       'D:/sim_runs/T016e', '--series', series,
                       '--tables', 'D:/sim_runs/tables/v1c', '--workers', '16',
                       '--scenarios', 'configs/imaging/v2.yaml', '--outname', 'image_v2']
            entry = dict(series=series, command=command, started=now())
            print('T017a reference', series, flush=True)
            result = subprocess.run(command, cwd=repo, stdout=stdout, stderr=stderr)
            entry.update(finished=now(), returncode=result.returncode)
            record['commands'].append(entry)
            state.write_text(json.dumps(record, indent=2)+'\n')
            if result.returncode:
                raise RuntimeError(f'T017a {series} failed: {result.returncode}')
    record.update(state='finished', finished=now())
except BaseException as exc:
    record.update(state='failed', finished=now(), error=str(exc))
    raise
finally:
    state.write_text(json.dumps(record, indent=2)+'\n')
