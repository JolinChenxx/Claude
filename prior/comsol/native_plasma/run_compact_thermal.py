from pathlib import Path
import os,subprocess,sys
R=Path(__file__).resolve().parent
case,source=sys.argv[1:3]
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_NUMA='0',ROUTE1_LOG_TAG=case)
def run(*args):subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),*args],env=env,check=True)
run('java','PrepareCompactThermalMesh',str(R/'models'/f'{source}_ready.mph'),str(R/'models'/f'{case}_ready.mph'),'1')
run('solve',case)
if case.startswith('H'):
    run('java','ExportThermalSource',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_thermal_source.csv'))
else:
    subprocess.run([sys.executable,'-X','utf8',str(R/'post_two_zone_optics.py'),case],env=env,check=True)
