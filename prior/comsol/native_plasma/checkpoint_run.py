"""Ask a running COMSOL batch job to save normally at a physical-time milestone.

This only observes the named route1 job; it does not restart or accept results.
"""
from pathlib import Path
import sys, time, re, subprocess, datetime
sys.path.insert(0, str(Path(__file__).resolve().parent))
from run import COMSOL, ROOT

case, target = sys.argv[1], float(sys.argv[2])
log = ROOT / 'logs' / f'{case}_solver.log'
pattern = re.compile(r'^[ \t]*\d+\s+([\d.eE+-]+)\s+[\d.eE+-]+(?:\s+out)?\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+', re.M)
while True:
    content = log.read_text(encoding='utf8', errors='replace') if log.exists() else ''
    if '总时间:' in content or 'Total time:' in content:
        print('BATCH_ALREADY_FINISHED', case, flush=True)
        break
    times = pattern.findall(content)
    if times and float(times[-1]) * 1e9 >= target:
        print('SAVE_REQUEST', datetime.datetime.now().isoformat(), case, times[-1], flush=True)
        result = subprocess.run([str(COMSOL/'bin/win64/comsolbatch.exe'), '-stop', '100', '-inputfile', str(ROOT/'models'/f'{case}.mph')], capture_output=True)
        print('STOP_COMMAND_EXIT', result.returncode, flush=True)
        if result.returncode:
            print(result.stdout.decode('utf8', errors='replace'), result.stderr.decode('utf8', errors='replace'))
            sys.exit(result.returncode)
        break
    time.sleep(20)
