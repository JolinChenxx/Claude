from pathlib import Path
import os,sys,subprocess,json
import numpy as np
R=Path(__file__).resolve().parent
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_NUMA='1',ROUTE1_LOG_TAG='H04')
def run(*args):subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),*args],env=env,check=True)
if not (R/'results/H04_thermal_source.csv').exists():
    run('java','PrepareLeadingHalfGaussian',str(R/'models/H01_ready.mph'),str(R/'models/H04_ready.mph'))
    run('solve','H04')
    run('java','ExportThermalSource',str(R/'models/H04.mph'),str(R/'results/H04_thermal_source.csv'))
a=np.loadtxt(R/'results/H04_thermal_source.csv',delimiter=',',skiprows=1)
np.savetxt(R/'data/H04_thermal_source_table.txt',a,fmt='%.14e')
env.update(ROUTE1_THREADS='4',ROUTE1_LOG_TAG='G49')
run('java','PrepareLeadingHalfGaussian',str(R/'models/G46_ready.mph'),str(R/'models/G49_ready.mph'),str(R/'data/H04_thermal_source_table.txt'))
run('solve','G49')
subprocess.run([sys.executable,'-X','utf8',str(R/'post_two_zone_optics.py'),'G49'],env=env,check=True)
