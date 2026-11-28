"""Wait for a named saved result, then export fields, mesh validity and balances."""
from pathlib import Path
import subprocess,sys,os,time
r=Path(__file__).resolve().parent;case=sys.argv[1];log=r/'logs'/f'{case}_solver.log'
while not log.exists() or not any(v in log.read_text(encoding='utf8',errors='replace') for v in ['鎬绘椂闂?','总时间:','Total time:']):time.sleep(20)
env=os.environ.copy();env['ROUTE1_THREADS']='16'
for cls,suffix,extra in [('MeshStateAudit','_mesh_audit.csv',[]),('ExportNative','',[]),('PlasmaDiagnostic','_diagnostic.csv',[]),('ElectrodeAudit','_electrode.csv',[]),('ExportBalances','_balances.csv',['trace'])]:
 subprocess.run([sys.executable,'-X','utf8',str(r/'run.py'),'java',cls,str(r/'models'/f'{case}.mph'),str(r/'results'/(case+suffix)),*extra],env=env,check=True)
print('POSTCASE COMPLETE',case,flush=True)
