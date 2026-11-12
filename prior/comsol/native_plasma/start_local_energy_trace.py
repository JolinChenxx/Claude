"""Test local total-energy normalization on the exact failed trace-air input."""
from pathlib import Path
import os,sys,json,time,subprocess,numpy as np
R=Path(__file__).resolve().parent;src=R/'results/R434_operator';name=sys.argv[1] if len(sys.argv)>1 else 'R435';dst=R/'results'/f'{name}_operator';dst.mkdir(exist_ok=False)
(dst/'state.txt').write_bytes((src/'state.txt').read_bytes());cfg=json.loads((src/'worker_config.json').read_text());cfg['local_energy_scale']=True;(dst/'worker_config.json').write_text(json.dumps(cfg,indent=2))
env=os.environ.copy();env.update(ROUTE1_THREADS='4',ROUTE1_LOG_TAG=name)
with (R/'logs'/f'{name}_launch.log').open('w') as out,(R/'logs'/f'{name}_launch.err').open('w') as err:p=subprocess.Popen([sys.executable,'-X','utf8',str(R/'run.py'),'java','NativeReactionWorker',str(R/'models/R401_ready.mph'),str(dst),'4','reactionOnly','multicharge','actualElectronRemoval','directInputStartup' if '--direct' in sys.argv else 'localEnergyScale'],env=env,stdout=out,stderr=err)
while not (dst/'ready.txt').exists():
 if p.poll() is not None:raise RuntimeError('Worker setup failed')
 time.sleep(2)
a=np.loadtxt(dst/'state.txt');birth=3e-16*a[:,15]*a[:,10]+3.3e-16*a[:,16]*a[:,10];first=min(2.5e-12,float(np.min(.1*a[birth>0,4]/birth[birth>0])))
ident='local_energy_air96p5';(dst/'request.properties').write_text(f'id={ident}\ndt_s=2.5e-10\nfirst_step_s={first:.17g}\nsave=true\n')
print('FIRST_STEP',first,flush=True)
while not (dst/f'complete_{ident}.json').exists():
 if (dst/f'error_{ident}.txt').exists():raise RuntimeError((dst/f'error_{ident}.txt').read_text(encoding='utf8'))
 time.sleep(2)
print((dst/f'complete_{ident}.json').read_text(),flush=True)
