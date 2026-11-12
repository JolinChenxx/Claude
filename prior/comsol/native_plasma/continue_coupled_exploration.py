from pathlib import Path
import os,sys,time,subprocess
R=Path(__file__).resolve().parent
while True:
    s=(R/'logs/G40_solver.log').read_text(encoding='utf8',errors='replace')
    if 'Total time:' in s or '\u603b\u65f6\u95f4:' in s:break
    time.sleep(20)
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_NUMA='1',ROUTE1_LOG_TAG='G53')
def py(name,*args):subprocess.run([sys.executable,'-X','utf8',str(R/name),*args],env=env,check=True)
py('run.py','java','ContinueGlobalPlume',str(R/'models/G40.mph'),str(R/'models/G53_ready.mph'),'1000','1','2','0.001')
post=subprocess.Popen([sys.executable,'-X','utf8',str(R/'post_two_zone.py'),'G40'],env=env,stdout=(R/'logs/G40_saved_post.log').open('wb'),stderr=subprocess.STDOUT)
py('run.py','solve','G53');post.wait()
py('post_two_zone_optics.py','G53','G40')
