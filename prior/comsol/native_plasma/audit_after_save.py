"""Run the existing physical audits once a named batch has saved and exited."""
from pathlib import Path
import os, subprocess, sys, time

root = Path(__file__).resolve().parent
case = sys.argv[1]
log = root / 'logs' / f'{case}_solver.log'
while not log.exists() or '总时间:' not in log.read_text(encoding='utf8', errors='replace'):
    time.sleep(15)
env = os.environ.copy()
env['ROUTE1_THREADS'] = '16'
jobs = [
    ('ElectrodeAudit', f'{case}_electrode.csv'),
    ('PlasmaDiagnostic', f'{case}_diagnostic.csv'),
    ('SheathDiagnostic', case),
    ('ExportBalances', f'{case}_balances.csv', 'trace'),
]
for cls, output, *extra in jobs:
    print('AUDIT', case, cls, flush=True)
    subprocess.run([sys.executable, '-X', 'utf8', str(root/'run.py'), 'java', cls,
                    str(root/'models'/f'{case}.mph'), str(root/'results'/output), *extra],
                   env=env, check=True)
subprocess.run([sys.executable, '-X', 'utf8', str(root/'plot_sheath.py'), case], check=True)
print('AUDITS_SAVED', case, flush=True)
