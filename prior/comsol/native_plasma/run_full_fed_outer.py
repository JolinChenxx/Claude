"""Use only the complete, actually saved upstream history; no tail extrapolation."""
from pathlib import Path
import sys,os,time,subprocess
R=Path(__file__).resolve().parent
while not (R/'results/G44_source_summary.json').exists():
    # The source postprocessor writes the CSV before its summary. A complete
    # normal batch exit is checked as well by its own entry point.
    if (R/'results/G44_global.csv').exists():
        import numpy as np
        a=np.loadtxt(R/'results/G44_global.csv',delimiter=',',skiprows=1,usecols=[0])
        if a[-1]>=1e-6-1e-15:break
    time.sleep(30)
env=os.environ.copy();env.update(ROUTE1_THREADS='4',ROUTE1_NUMA='0',ROUTE1_LOG_TAG='G47')
def py(name,*args):subprocess.run([sys.executable,'-X','utf8',str(R/name),*args],env=env,check=True)
py('prepare_source_feed.py','G21','G22','G23','G26','G44')
py('run.py','java','PrepareFedOuterPlume',str(R/'models/G16_ready.mph'),str(R/'models/G47_ready.mph'),str(R/'data/source_feed_table.txt'),'1000')
py('run.py','solve','G47')
py('post_global_round2.py','G47')
py('calibrate_global_camera.py','G47')
py('animate_frozen_comparison.py','G47')
