"""Finish the actual thermal predictor, then solve native gas stages quickly."""
from pathlib import Path
import sys,os,time,json,subprocess
import numpy as np
R=Path(__file__).resolve().parent
while True:
    p=R/'logs/ExportThermalSource_H01_java.log';s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    if 'THERMAL SAVED FEED EXPORTED' in s:break
    time.sleep(10)
a=np.loadtxt(R/'results/H01_thermal_source.csv',delimiter=',',skiprows=1)
assert a[-1,0]>=1e-6-1e-15
np.savetxt(R/'data/thermal_source_table.txt',a,fmt='%.14e')
energy=np.trapz(a[:,5],a[:,0]);out=np.trapz(a[:,2],a[:,0]);base=np.trapz(a[:,7],a[:,0]);stored=a[-1,6]-a[0,6]
report=dict(source_case='H01',saved_end_ns=float(a[-1,0]*1e9),target_absorbed_uJ=float(energy*1e6),evaporated_enthalpy_uJ=float(out*1e6),stored_energy_uJ=float(stored*1e6),energy_residual_percent=float((stored+out+base-energy)/energy*100),maximum_target_K=float(a[:,4].max()),coupling='Actual thermal predictor; no plume feedback. Reserved target laser share10percent, remainder for plasma')
(R/'results/H01_thermal_source_summary.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2),flush=True)
env=os.environ.copy();env.update(ROUTE1_THREADS='4',ROUTE1_NUMA='0',ROUTE1_LOG_TAG='G46')
def run(*args):subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),*args],env=env,check=True)
run('java','PreparePrescribedThermalPlasma',str(R/'models/G40_ready.mph'),str(R/'models/G46_ready.mph'),str(R/'data/thermal_source_table.txt'))
run('solve','G46')
subprocess.run([sys.executable,'-X','utf8',str(R/'post_two_zone_optics.py'),'G46'],env=env,check=True)
