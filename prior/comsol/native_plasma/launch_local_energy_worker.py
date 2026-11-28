"""Numerically normalize independent energy equations using the failed real batch."""
from pathlib import Path
import sys,os,json,subprocess,time
R=Path(__file__).resolve().parent;dst=R/'results/R421_operator';dst.mkdir(exist_ok=False)
(dst/'state.txt').write_bytes((R/'results/failed_cold_air95p75_input.txt').read_bytes())
cfg=json.loads((R/'results/R416_operator/worker_config.json').read_text());cfg['local_energy_scale']=True;(dst/'worker_config.json').write_text(json.dumps(cfg,indent=2))
env=os.environ.copy();env.update(ROUTE1_THREADS='16',ROUTE1_LOG_TAG='R421')
with (R/'logs/R421_launch.log').open('w') as o,(R/'logs/R421_launch.err').open('w') as e:
 p=subprocess.Popen([sys.executable,'-X','utf8',str(R/'run.py'),'java','NativeReactionWorker',str(R/'models/R401_ready.mph'),str(dst),'192','reactionOnly','multicharge','localEnergyScale'],env=env,stdout=o,stderr=e)
while not (dst/'ready.txt').exists():
 if p.poll() is not None:raise RuntimeError('R421 setup failed')
 time.sleep(3)
(dst/'request.properties').write_text('id=failed95p75_normalized\ndt_s=2.5e-10\nsave=true\n')
while not (dst/'complete_failed95p75_normalized.json').exists():
 if (dst/'error_failed95p75_normalized.txt').exists():raise RuntimeError((dst/'error_failed95p75_normalized.txt').read_text())
 time.sleep(3)
print('NORMALIZED_REAL_BATCH_SOLVED',flush=True)
