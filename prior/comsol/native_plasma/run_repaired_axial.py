"""Launch both physical source histories only after native overrides are audited."""
from pathlib import Path
import os,sys,subprocess,time
R=Path(__file__).resolve().parent
while True:
    s=(R/'logs/FixAxialNamespaces_A01fix_java.log').read_text(encoding='utf8',errors='replace')
    if 'AXIAL NAMESPACES AND SOURCES VERIFIED' in s:break
    if 'Exception:' in s or 'RuntimeException:' in s:raise RuntimeError(s[-2500:])
    time.sleep(20)
env=os.environ.copy();env.update(ROUTE1_THREADS='16',ROUTE1_NUMA='0')
jobs=[]
for script,args,case in [('run_axial.py',['A01'],'A01'),('run_axial_fed.py',[],'A02')]:
    log=(R/'logs'/f'{case}_driver_repaired.log').open('wb')
    p=subprocess.Popen([sys.executable,'-X','utf8',str(R/script),*args],env=env,stdout=log,stderr=subprocess.STDOUT)
    jobs.append((p,log,case));print('LAUNCHED',case,p.pid,flush=True)
for p,log,case in jobs:
    code=p.wait();log.close();print('EXIT',case,code,flush=True)
