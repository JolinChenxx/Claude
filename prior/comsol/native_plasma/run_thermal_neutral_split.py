"""One isolated counterfactual: thermal neutral Cu at actual phase release."""
from pathlib import Path
import os,sys,subprocess,time,shutil
R=Path(__file__).resolve().parent;worker=R/'results/R404_operator';worker.mkdir(exist_ok=True)
if (worker/'ready.txt').exists():raise RuntimeError('Worker directory already in use')
shutil.copyfile(R/'results/R403_operator/state.txt',worker/'state.txt')
env=os.environ.copy();env.update(ROUTE1_THREADS='12',ROUTE1_LOG_TAG='R404',ROUTE1_MATERIAL_THREADS='12',ROUTE1_PHASE_FEED='thermal_neutral')
with (R/'logs/R404_worker_pipeline.log').open('wb') as f:
 p=subprocess.Popen([sys.executable,'-X','utf8',str(R/'run.py'),'java','NativeReactionWorker',str(R/'models/R401_ready.mph'),str(worker),'192'],env=env,stdout=f,stderr=subprocess.STDOUT)
 while not (worker/'ready.txt').exists():
  if p.poll() is not None:raise RuntimeError('R404 worker failed')
  time.sleep(5)
 subprocess.run([sys.executable,'-X','utf8',str(R/'coupled_hem_plasma.py'),'HC406','180','R404_operator'],env=env,check=True)
