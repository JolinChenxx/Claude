"""Continue the actual predictor; queue its main solve behind gas feedback."""
from pathlib import Path
import sys,os,time,subprocess
R=Path(__file__).resolve().parent
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='0',ROUTE1_LOG_TAG='C213')
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*map(str,args)],env=env,check=True)
py('run.py','java','ContinueCopperNewton',R/'models/C201.mph',R/'models/C213_ready.mph','133[ns]','range(133[ns],0.5[ns],175[ns])')
while True:
    s=(R/'logs/P211_solver.log').read_text(encoding='utf-8',errors='replace')
    if '总时间:' in s or 'Total time:' in s:break
    time.sleep(30)
py('run.py','solve','C213')
py('run.py','java','ExportMelt',R/'models/C213.mph',R/'results/C213_surface.csv')
py('plot_partitioned_copper.py','C213')
