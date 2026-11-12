"""Uniform actual-energy removal in every native reaction group."""
from pathlib import Path
import os,sys,json,time,subprocess
R=Path(__file__).resolve().parent
# R426 has already solved the exact formerly failed1024-cell batch.
p=R/'results/R426_operator';cfg=json.loads((p/'worker_config.json').read_text());cfg.pop('electron_unknown_scale',None);cfg.update(actual_electron_removal=True,trace_electron_scale=False);(p/'worker_config.json').write_text(json.dumps(cfg,indent=2))
v=json.loads((p/'trace_validation.json').read_text());v['scope']='Exact formerly failed1024-cell batch; actual electron removal energy, default original variable scales, identical rates and total-energy equation.';(p/'trace_validation.json').write_text(json.dumps(v,indent=2))
spec=[('R427','R416',6144,16),('R428','R415',6144,16),('R429','R414',256,4),('R430','R420',256,4),('R431','R422',256,4),('R432','R423',256,4),('R433','R418',256,8)]
started=[]
for name,source,slots,threads in spec:
 dst=R/'results'/f'{name}_operator';dst.mkdir(exist_ok=False);src=R/'results'/f'{source}_operator'
 (dst/'state.txt').write_bytes((src/'state.txt').read_bytes());cfg=json.loads((src/'worker_config.json').read_text());cfg.update(slots=slots,actual_electron_removal=True,trace_electron_scale=False);(dst/'worker_config.json').write_text(json.dumps(cfg,indent=2))
 env=os.environ.copy();env.update(ROUTE1_THREADS=str(threads),ROUTE1_LOG_TAG=name)
 args=[sys.executable,'-X','utf8',str(R/'run.py'),'java','NativeReactionWorker',str(R/'models/R401_ready.mph'),str(dst),str(slots//32)]+(['dependentCu'] if cfg['dependent_species']=='Cu' else [])+['reactionOnly','multicharge','actualElectronRemoval']
 with (R/'logs'/f'{name}_launch.log').open('w') as out,(R/'logs'/f'{name}_launch.err').open('w') as err:proc=subprocess.Popen(args,env=env,stdout=out,stderr=err)
 started.append((name,proc))
while not all((R/'results'/f'{name}_operator/ready.txt').exists() for name,_ in started):
 for name,proc in started:
  if proc.poll() is not None:raise RuntimeError(f'{name} setup failed')
 time.sleep(2)
(R/'results/corrected_energy_workers_ready.json').write_text(json.dumps(dict(workers=[q[0] for q in started]+['R426'],physics='Actual mean electron energy in removal term; rate-temperature floor unchanged.'),indent=2))
print('ALL_CORRECTED_REACTION_WORKERS_READY',flush=True)
