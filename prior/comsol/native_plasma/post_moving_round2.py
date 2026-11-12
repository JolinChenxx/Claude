"""Saved moving-surface fields and diagnostics, without fixed-plane ledger claims."""
from pathlib import Path
import os,sys,subprocess,time,json
R=Path(__file__).resolve().parent;case=sys.argv[1]
while True:
    p=R/'logs'/f'{case}_solver.log'
    s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    if any(k in s for k in ['鎬绘椂闂?','总时间:','Total time:']):break
    time.sleep(30)
env=os.environ.copy();env.update(ROUTE1_THREADS='16',ROUTE1_LOG_TAG=case)
for cls,suffix,extra in [('MeshStateAudit','_mesh_audit.csv',[]),('ExportNative','',[]),('ExportMelt','_melt_surface.csv',[]),('ExportMechanism','_mechanism.csv',['5']),('ExportBalances','_balances.csv',['trace']),('ExportMovingMechanics','_moving_work.csv',[])]:
    subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java',cls,str(R/'models'/f'{case}.mph'),str(R/'results'/(case+suffix)),*extra],env=env,check=True)
    if cls=='ExportNative':
        subprocess.run([sys.executable,'-X','utf8',str(R/'plot_native.py'),case,'250'],env=env,check=True)
        subprocess.run([sys.executable,'-X','utf8',str(R/'plot_surface_motion.py'),case],env=env,check=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'audit_native.py'),case],env=env,check=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'summarize_moving_round2.py'),case],env=env,check=True)
(R/'results'/f'{case}_stage.json').write_text(json.dumps({'case':case,'stage':'real moving surface','mesh':'shared radial nodes and quadratic surface mapping','physics_reference':'ground-state F80/F87','validation':'diagnostic mass/charge/energy and mesh fields; mechanical boundary terms require full moving audit','optics':'fixed spatial exported fields only; no claim of resolved moving nanometre sheath radiation'},indent=2))
print('MOVING_DIAGNOSTICS_EXPORTED',case,flush=True)
