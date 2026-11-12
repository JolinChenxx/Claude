"""Start isolated native Cu0-Cu4+ operators and validate a real hot input batch."""
from pathlib import Path
import os,sys,subprocess,json,time,numpy as np
R=Path(__file__).resolve().parent
for name,source,rows,threads,dep in [('R412','R404',192,8,False),('R413','R408',128,8,True),('R414','R410',8,4,True)]:
    p=R/'results'/f'{name}_operator';p.mkdir(exist_ok=True)
    a=np.loadtxt(R/'results'/f'{source}_operator/state.txt')
    a=np.c_[a,np.full((len(a),3),1e-20)]
    np.savetxt(p/'state.txt',a,fmt='%.16e')
    (p/'worker_config.json').write_text(json.dumps(dict(slots=32*rows,dependent_species='Cu' if dep else 'N2',multicharge=True,abstract_potential_solved=False,source='data/copper_multicharge_sources.json'),indent=2))
    env=os.environ.copy();env.update(ROUTE1_THREADS=str(threads),ROUTE1_LOG_TAG=name)
    with (R/'logs'/f'{name}_launch.log').open('w') as out,(R/'logs'/f'{name}_launch.err').open('w') as err:
        args=[sys.executable,'-X','utf8',str(R/'run.py'),'java','NativeReactionWorker',str(R/'models/R401_ready.mph'),str(p),str(rows)]
        if dep:args.append('dependentCu')
        args+=['reactionOnly','multicharge']
        subprocess.Popen(args,stdout=out,stderr=err,env=env)
for name in ['R412','R413','R414']:
    p=R/'results'/f'{name}_operator'
    while not (p/'ready.txt').exists():
        log=(R/'logs'/f'NativeReactionWorker_{name}_java.log')
        if log.exists() and 'Exception:' in log.read_text(encoding='utf8',errors='replace'):raise RuntimeError(f'{name} build failed; inspect log')
        time.sleep(3)
p=R/'results/R414_operator';key='multicharge_hot_audit'
(p/'request.properties').write_text(f'id={key}\ndt_s=2.5e-10\nsave=true\n')
while not (p/f'complete_{key}.json').exists():
    if (p/f'error_{key}.txt').exists():raise RuntimeError((p/f'error_{key}.txt').read_text())
    time.sleep(3)
inp=np.loadtxt(p/'state.txt');meta=json.loads((p/f'complete_{key}.json').read_text());d=np.fromfile(p/f'output_{key}.bin',dtype='>f8').reshape(meta['shape']);n=d[19:].T;old=inp[:,7:];charges=np.array([0,0,0,0,1,1,1,1,-1,-1,0,0,0,0,0,0,0,1,2,3,4]);charge_delta=(n@charges-d[1])-(old@charges-inp[:,4])
cu0=old[:,10:].sum(1);cu1=n[:,10:].sum(1);report=dict(native_seconds=meta['seconds'],max_Cu_relative_change=float((abs(cu1-cu0)/np.maximum(cu0,1.)).max()),max_charge_delta_over_initial_heavy=float((abs(charge_delta)/np.maximum(old.sum(1),1.)).max()),max_Cu2_number_m3=float(n[:,18].max()),max_Cu3_number_m3=float(n[:,19].max()),max_Cu4_number_m3=float(n[:,20].max()),max_Te_K=float(d[17].max()),minimum_species_m3=float(n.min()),input='R414_operator/state.txt',saved_mph=f'R414_operator/native_operator_{key}.mph',scope='Actual hot local inputs; no plume transport, same published rate prescription')
(R/'results/multicharge_local_validation.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
