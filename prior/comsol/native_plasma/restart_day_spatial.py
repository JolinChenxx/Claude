"""First bounded restart comparison: same equations, fresh Newton Jacobian."""
from pathlib import Path
import os,sys,subprocess,time
R=Path(__file__).resolve().parent
source,case=sys.argv[1:3]
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*map(str,args)],env=env,check=True)
while True:
    s=(R/'logs'/f'{source}_solver.log').read_text(encoding='utf-8',errors='replace')
    if 'Total time:' in s or '总时间' in s:break
    time.sleep(15)
py('run.py','java','AcceptedTimeContinuation',R/'models'/f'{source}.mph',R/'models'/f'{case}_ready.mph','125')
py('run.py','java','RefreshGasJacobian',R/'models'/f'{case}_ready.mph',R/'models'/f'{case}_ready.mph')
p=subprocess.Popen([sys.executable,'-X','utf8',str(R/'run.py'),'solve',case],env=env)
start=time.monotonic()
while p.poll() is None and time.monotonic()-start<1200:time.sleep(20)
if p.poll() is None:py('checkpoint_run.py',case,'0')
p.wait()
if p.returncode:raise SystemExit(p.returncode)
py('run.py','java','StateStiffnessDiagnostic',R/'models'/f'{case}.mph',R/'results'/f'{case}_stiffness.csv')
py('run.py','java','ExportNative',R/'models'/f'{case}.mph',R/'results'/case)
