"""Resume a saved client after the existing native request has finished."""
from pathlib import Path
import os,sys,time,subprocess,json
R=Path(__file__).resolve().parent
case,end,worker,source=sys.argv[1:5];w=R/'results'/worker
if (R/'results'/case/'status.json').exists():raise RuntimeError('Refuse to overwrite an existing case')
request=w/'request.properties'
if request.exists():
    props=dict(line.split('=',1) for line in request.read_text().splitlines() if '=' in line)
    key=props['id']
    while not (w/f'complete_{key}.json').exists():
        if (w/f'error_{key}.txt').exists():raise RuntimeError((w/f'error_{key}.txt').read_text())
        time.sleep(5)
env=os.environ.copy();env['ROUTE1_EOS_INVERSION']='warm'
print('RESUMING',case,'FROM_SAVED',source,'EOS_WARM_BRACKET_ONLY',flush=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'coupled_hem_plasma.py'),case,end,worker,source],env=env,check=True)
