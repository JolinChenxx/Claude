from pathlib import Path
import sys,os,subprocess,time
R=Path(__file__).resolve().parent;case=sys.argv[1]
env=os.environ.copy();env.setdefault('ROUTE1_THREADS','16');env.setdefault('ROUTE1_NUMA','0');env['ROUTE1_LOG_TAG']=case
log=R/'logs'/f'BuildAxialPlume_{case}_java.log'
while True:
    s=log.read_text(encoding='utf8',errors='replace') if log.exists() else ''
    if 'AXIAL PLASMA READY' in s:break
    if 'Exception:' in s or 'java.lang.' in s:raise RuntimeError(s[-3000:])
    time.sleep(20)
def py(name,*args):subprocess.run([sys.executable,'-X','utf8',str(R/name),*args],env=env,check=True)
py('run.py','solve',case)
py('run.py','java','ExportAxialPlume',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_axial.csv'))
py('post_axial.py',case)
