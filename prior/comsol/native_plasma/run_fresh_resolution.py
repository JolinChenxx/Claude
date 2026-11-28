from pathlib import Path
import sys,os,subprocess
R=Path(__file__).resolve().parent;env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG='P216')
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*map(str,args)],env=env,check=True)
py('run.py','java','FreshDrivenResolution',R/'models/P215_ready.mph',R/'models/P216_ready.mph')
py('run.py','solve','P216')
py('run.py','java','ExportNative',R/'models/P216.mph',R/'results/P216')
py('finish_driven_post.py','P216')
