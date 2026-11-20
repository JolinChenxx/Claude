from pathlib import Path
import sys,os,time,subprocess
R=Path(__file__).resolve().parent;env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG='P215')
def py(script,*a):subprocess.run([sys.executable,'-X','utf8',str(R/script),*map(str,a)],env=env,check=True)
py('run.py','java','PrepareRadialResolution',R/'models/P212.mph',R/'models/P215_ready.mph')
while True:
 s=(R/'logs/P214_solver.log').read_text(encoding='utf-8',errors='replace')
 if '总时间:' in s or 'Total time:' in s:break
 time.sleep(30)
py('run.py','solve','P215')
py('run.py','java','ExportNative',R/'models/P215.mph',R/'results/P215')
py('run.py','java','MeshStateAudit',R/'models/P215.mph',R/'results/P215_mesh_audit.csv')
py('run.py','java','ExportMovingOptics',R/'models/P215.mph',R/'results/P215mapped','113')
py('plot_mapped_fields.py','P215mapped')
