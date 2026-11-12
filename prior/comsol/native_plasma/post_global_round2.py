"""Export only a normally saved native global model, then plot its actual history."""
from pathlib import Path
import datetime, json, os, subprocess, sys, time
R=Path(__file__).resolve().parent
case=sys.argv[1]
env=os.environ.copy();env.update(ROUTE1_THREADS='4',ROUTE1_LOG_TAG=case+'_globalpost')
while True:
    p=R/'logs'/f'{case}_solver.log'
    s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    if 'Total time:' in s or '总时间:' in s:break
    deadline=datetime.datetime.fromisoformat(json.loads((R/'round2_session.json').read_text())['deadline_utc'].replace('Z','+00:00'))
    if datetime.datetime.now(datetime.timezone.utc)>deadline:sys.exit('No saved result before deadline')
    time.sleep(30)
subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java','ExportGlobalPlume',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_global.csv')],env=env,check=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'plot_global_plume.py'),*sys.argv[2:],case],env=env,check=True)
chain='-'.join([*sys.argv[2:],case])
if (R/'results'/f'{chain}_global_images.npz').exists():
    subprocess.run([sys.executable,'-X','utf8',str(R/'calibrate_global_camera.py'),chain],env=env,check=True)
    subprocess.run([sys.executable,'-X','utf8',str(R/'plot_global_trend.py'),chain],env=env,check=True)
