"""Short accepted-step replay of an observed stall, then export its actual endpoint."""
from pathlib import Path
import sys,os,subprocess,time,re
R=Path(__file__).resolve().parent
prev,case,numa=sys.argv[1:4]
end_ns=sys.argv[4] if len(sys.argv)>4 else '92'
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA=numa,ROUTE1_LOG_TAG=case)
def finished(c):
    p=R/'logs'/f'{c}_solver.log';s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    return '总时间:' in s or 'Total time:' in s
while not finished(prev):time.sleep(10)
def java(cls,*args):subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java',cls,*map(str,args)],env=env,check=True)
java('AcceptedTimeContinuation',R/'models'/f'{prev}.mph',R/'models'/f'{case}_ready.mph',end_ns)
p=subprocess.Popen([sys.executable,'-X','utf8',str(R/'run.py'),'solve',case],env=env)
started=time.monotonic();requested=False
pattern=re.compile(r'^[ \t]*\d+\s+([\d.eE+-]+)\s+([\d.eE+-]+)(?:\s+out)?\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+\s+\d+',re.M)
while p.poll() is None:
    time.sleep(20)
    lp=R/'logs'/f'{case}_solver.log';s=lp.read_text(encoding='utf8',errors='replace') if lp.exists() else '';steps=pattern.findall(s)
    stalled=len(steps)>12 and all(float(dt)<1e-14 for _,dt in steps[-4:])
    if not requested and (stalled or time.monotonic()-started>900):
        subprocess.run([sys.executable,'-X','utf8',str(R/'checkpoint_run.py'),case,'0'],check=True);requested=True
env['ROUTE1_THREADS']='8'
java('StateStiffnessDiagnostic',R/'models'/f'{case}.mph',R/'results'/f'{case}_stiffness.csv')
java('ExportNative',R/'models'/f'{case}.mph',R/'results'/case)
print('ACTUAL_STALL_ENDPOINT_EXPORTED',case,flush=True)
