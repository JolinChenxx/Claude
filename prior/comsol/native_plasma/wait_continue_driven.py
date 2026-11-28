from pathlib import Path
import sys,time,json,subprocess
R=Path(__file__).resolve().parent;source,case,end=sys.argv[1:4]
while not (R/'results'/f'{source}_stage.json').exists():time.sleep(30)
d=json.loads((R/'results'/f'{source}mapped_fields.json').read_text())
start=d['times_s'][-1]*1e9
if start<95-1e-5:raise RuntimeError('Expected completed first gate before continuation')
subprocess.run([sys.executable,'-X','utf8',str(R/'continue_driven.py'),source,case,str(start),end],check=True)
