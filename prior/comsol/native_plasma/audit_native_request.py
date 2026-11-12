"""Capture a live native input and compare its completed local reaction output."""
from pathlib import Path
import sys,json,time,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from coupled_hem_plasma import MASS,ENERGY,CV,EV
worker=R/'results'/sys.argv[1];prefix=sys.argv[2]
while True:
    raw=(worker/'request.properties').read_text();p=dict(s.split('=',1) for s in raw.splitlines() if '=' in s)
    if p['id'].startswith('step'+prefix) and p['id'].endswith('_0'):
        a=np.loadtxt(worker/'state.txt')
        if (worker/'request.properties').read_text()==raw:break
    time.sleep(5)
key=p['id'];out=R/'results/reaction_input_audit';out.mkdir(exist_ok=True)
np.savez_compressed(out/f'{key}_input.npz',state=a)
while not (worker/f'complete_{key}.json').exists():
    if (worker/f'error_{key}.txt').exists():raise RuntimeError((worker/f'error_{key}.txt').read_text())
    time.sleep(5)
m=json.loads((worker/f'complete_{key}.json').read_text());d=np.fromfile(worker/f'output_{key}.bin',dtype='>f8').reshape(m['shape'])
change=abs(d[0]/a[:,2]-1);indices=np.argsort(change)[-5:][::-1];rows=[]
for k in indices:
    n0=a[k,7:];n1=d[19:,k]
    e0=a[k,3]*a[k,2]+(n0*ENERGY).sum()+a[k,5]*EV
    e1=(a[k,3]+((n1-n0)*CV).sum())*d[0,k]+(n1*ENERGY).sum()+d[2,k]*EV
    rows.append(dict(slot=int(k),T0_K=a[k,2],T1_K=d[0,k],relative_T_change=float(change[k]),ne0_m3=a[k,4],ne1_m3=d[1,k],Te1_K=d[17,k],cv_input=a[k,3],mass0=float((n0*MASS).sum()),mass1=float((n1*MASS).sum()),energy0_J_m3=float(e0),energy1_J_m3=float(e1),external_energy_J_m3=float(d[5,k]),energy_residual_J_m3=float(e1-e0-d[5,k]),initial_species=n0.tolist(),final_species=n1.tolist()))
report=dict(request=key,worker=sys.argv[1],time_step_s=float(p['dt_s']),largest_temperature_changes=rows)
(out/f'{key}_audit.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
