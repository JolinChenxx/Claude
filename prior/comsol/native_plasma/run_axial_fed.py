from pathlib import Path
import sys,os,subprocess,time,json
import numpy as np
R=Path(__file__).resolve().parent
while not (R/'models/A01_ready.mph').exists():time.sleep(30)
sys.path.insert(0,str(R));from plot_global_plume import load
d=load('G21')
for case in ['G22','G23','G26','G44']:
    q=load(case);keep=d['time_s']<q['time_s'][0]-1e-15;d={n:np.r_[v[keep],q[n]] for n,v in d.items()}
table=np.loadtxt(R/'data/source_feed_table.txt');assert np.allclose(table[:,0],d['time_s'],atol=1e-15,rtol=0)
# Frozen-composition sonic exit from the actual source-reservoir state.
rho=d['c.Mgas']/d['c.Vp'];pressure=d['c.pPl'];gamma=d['c.gammaNozzle']
ratio=np.maximum((2/(gamma+1))**(gamma/(gamma-1)),np.minimum(1,101325/pressure))
speed=np.sqrt(np.maximum(2*gamma/(gamma-1)*pressure/rho*(1-ratio**((gamma-1)/gamma)),0))
# Source outflow is uniform over a hemisphere: area-mean axial projection=1/2.
np.savetxt(R/'data/axial_source_outflow_table.txt',np.column_stack([table,.5*speed]),fmt='%.14e')
env=os.environ.copy();env.setdefault('ROUTE1_THREADS','16');env.setdefault('ROUTE1_NUMA','0');env['ROUTE1_LOG_TAG']='A02'
def py(name,*args):subprocess.run([sys.executable,'-X','utf8',str(R/name),*args],env=env,check=True)
py('run.py','java','PrepareAxialSourceOutflow',str(R/'models/A01_ready.mph'),str(R/'models/A02_ready.mph'),str(R/'data/axial_source_outflow_table.txt'))
py('run.py','solve','A02')
py('run.py','java','ExportAxialPlume',str(R/'models/A02.mph'),str(R/'results/A02_axial.csv'))
py('post_axial.py','A02')
