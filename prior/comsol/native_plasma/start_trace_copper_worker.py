"""Test tighter absolute accuracy on the exact failed cold-copper batch."""
from pathlib import Path
import os,sys,subprocess,json,time,numpy as np
R=Path(__file__).resolve().parent;name=sys.argv[1] if len(sys.argv)>1 else 'R424';dst=R/'results'/f'{name}_operator';dst.mkdir(exist_ok=False)
raw=(R/'results/failed_cold_Cu95_input.txt').read_bytes();(dst/'state.txt').write_bytes(raw)
cfg=json.loads((R/'results/R417_operator/worker_config.json').read_text());cfg['actual_electron_removal']='--actual-removal' in sys.argv;cfg['trace_electron_scale']='--actual-removal' not in sys.argv;(dst/'worker_config.json').write_text(json.dumps(cfg,indent=2))
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_LOG_TAG=name)
with (R/'logs'/f'{name}_launch.log').open('w') as out,(R/'logs'/f'{name}_launch.err').open('w') as err:
 p=subprocess.Popen([sys.executable,'-X','utf8',str(R/'run.py'),'java','NativeReactionWorker',str(R/'models/R401_ready.mph'),str(dst),'32','dependentCu','reactionOnly','multicharge']+(['actualElectronRemoval'] if '--actual-removal' in sys.argv else ['traceElectronScale']),env=env,stdout=out,stderr=err)
while not (dst/'ready.txt').exists():
 if p.poll() is not None:raise RuntimeError('Worker setup failed')
 time.sleep(2)
ident='traceCu95';(dst/'request.properties').write_text(f'id={ident}\ndt_s=2.5e-10\nsave=true\n')
while not (dst/f'complete_{ident}.json').exists():
 if (dst/f'error_{ident}.txt').exists():raise RuntimeError((dst/f'error_{ident}.txt').read_text(encoding='utf8'))
 time.sleep(2)
meta=json.loads((dst/f'complete_{ident}.json').read_text());a=np.loadtxt(dst/'state.txt');d=np.fromfile(dst/f'output_{ident}.bin',dtype='>f8').reshape(meta['shape'])
charges=np.array([0,0,0,0,1,1,1,1,-1,-1,0,0,0,0,0,0,0,1,2,3,4]);q0=a[:,7:]@charges-a[:,4];q1=charges@d[19:]-d[1]
report=dict(seconds=meta['seconds'],failed_row505_final_ne=float(d[1,505]),failed_row505_final_Te_K=float(d[17,505]),T_range=d[0,[np.argmin(d[0]),np.argmax(d[0])]].tolist(),electron_min=float(d[1].min()),energy_min=float(d[2].min()),max_local_charge_change_fraction=float(np.max(abs(q1-q0)/np.maximum(abs(charges)@abs(d[19:])+abs(d[1]),1.))),scope='Identical failed1024-cell input and same reaction coefficients. Actual electron removal energy replaces rate-temperature floor; default original variable scales retained.' if '--actual-removal' in sys.argv else 'Identical failed input; tightened electron absolute scales only.')
(dst/'trace_validation.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
