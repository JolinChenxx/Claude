"""Audit direct native startup, then compare to an accepted identical input."""
from pathlib import Path
import os,sys,json,time,numpy as np
sys.path.insert(0,str(Path(__file__).resolve().parent))
os.environ['ROUTE1_MULTICHARGE']='1'
from coupled_hem_plasma import MASS,ENERGY,CV,EV
R=Path(__file__).resolve().parent;w=R/'results/R436_operator'
a=np.loadtxt(w/'state.txt');key='local_energy_air96p5'
m=json.loads((w/f'complete_{key}.json').read_text());d=np.fromfile(w/f'output_{key}.bin',dtype='>f8').reshape(m['shape'])
n0=a[:,7:];n1=d[19:].T
charges=np.array([0,0,0,0,1,1,1,1,-1,-1,0,0,0,0,0,0,0,1,2,3,4])
e0=a[:,3]*a[:,2]+n0@ENERGY+a[:,5]*EV
e1=(a[:,3]+(n1-n0)@CV)*d[0]+n1@ENERGY+d[2]*EV
audit={'mass_max_relative':float(np.max(abs((n1-n0)@MASS)/np.maximum(n0@MASS,1e-100))),
 'charge_change_over_inventory':float(np.max(abs((n1-n0)@charges-(d[1]-a[:,4]))/np.maximum(n0@abs(charges)+a[:,4],1))),
 'energy_max_relative':float(np.max(abs(e1-e0-d[5])/np.maximum(abs(e0),1))),
 'finite':bool(np.isfinite(d).all()),'seconds':m['seconds']}
(w/'startup_conservation.json').write_text(json.dumps(audit,indent=2));print(audit,flush=True)
assert audit['finite'] and max(audit[k] for k in ['mass_max_relative','charge_change_over_inventory','energy_max_relative'])<1e-5
cfg=json.loads((w/'worker_config.json').read_text());cfg.update(local_energy_scale=False,direct_input_startup=True);(w/'worker_config.json').write_text(json.dumps(cfg,indent=2))
reference=R/'results/R434_operator';info=json.loads((reference/'comparison.json').read_text());idx=info['reference_indices']
all_a=np.loadtxt(reference/'reference_input.txt');x=a.copy();x[:len(idx)]=all_a[idx];x[len(idx):]=a[-1];x[:,0]=np.arange(len(x))
np.savetxt(w/'accepted_reference_input.txt',x,fmt='%.16e');np.savetxt(w/'state.txt',x,fmt='%.16e')
birth=3e-16*x[:,15]*x[:,10]+3.3e-16*x[:,16]*x[:,10];first=min(2.5e-12,float(np.min(.1*x[birth>0,4]/birth[birth>0])))
ident='direct_accepted_reference';(w/'request.properties').write_text(f'id={ident}\ndt_s=2.5e-10\nfirst_step_s={first:.17g}\nsave=false\n')
while not (w/f'complete_{ident}.json').exists():
 if (w/f'error_{ident}.txt').exists():raise RuntimeError((w/f'error_{ident}.txt').read_text())
 time.sleep(2)
mr=json.loads((R/'results/R427_operator'/f'complete_{info["reference"]}.json').read_text());ref=np.fromfile(R/'results/R427_operator'/f'output_{info["reference"]}.bin',dtype='>f8').reshape(mr['shape'])[:,idx]
mt=json.loads((w/f'complete_{ident}.json').read_text());test=np.fromfile(w/f'output_{ident}.bin',dtype='>f8').reshape(mt['shape'])[:,:len(idx)]
diff={mr['expressions'][k]:float(abs(test[k]-ref[k]).max()/max(abs(ref[k]).max(),1e-100)) for k in [0,1,2,6,7]}
report=dict(reference=info['reference'],peak_scaled_field_differences=diff,seconds=mt['seconds'],first_step_s=first)
(w/'comparison.json').write_text(json.dumps(report,indent=2));print(report,flush=True)
