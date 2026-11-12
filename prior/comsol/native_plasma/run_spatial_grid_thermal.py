from pathlib import Path
import sys,os,subprocess
R=Path(__file__).resolve().parent;case=sys.argv[1] if len(sys.argv)>1 else 'S201'
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='0',ROUTE1_LOG_TAG=case)
def run(*args):subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),*args],env=env,check=True)
run('java','BuildSpatialGridThermal',str(R/'models/G40_ready.mph'),str(R/'models'/f'{case}_ready.mph'))
raw=R/'models'/f'{case}_unpatched_ready.mph'
(R/'models'/f'{case}_ready.mph').replace(raw)
run('java','RepairSpatialGridCoupling',str(raw),str(R/'models'/f'{case}_ready.mph'))
run('solve',case)
print('SPATIAL GRID SAVED',case,flush=True)
