"""Run an already built material-only case and export actual saved fields."""
from pathlib import Path
import os,sys,subprocess
R=Path(__file__).resolve().parent
case=sys.argv[1]
env=os.environ.copy();env.update(ROUTE1_THREADS=env.get('ROUTE1_THREADS','4'),ROUTE1_LOG_TAG=case)
for args in [('run.py','solve',case),('finish_axial_hem.py',case)]:
    subprocess.run([sys.executable,'-X','utf8',str(R/args[0]),*args[1:]],env=env,check=True)
