"""Start saved-data diagnostics after an orderly stop, without launching a solve."""
from pathlib import Path
import sys,time,subprocess
R=Path(__file__).resolve().parent;case=sys.argv[1]
while True:
    p=R/'logs'/f'{case}_solver.log';s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    if '总时间:' in s or 'Total time:' in s:break
    time.sleep(10)
subprocess.run([sys.executable,'-X','utf8',str(R/'post_round2.py'),case],check=True)
