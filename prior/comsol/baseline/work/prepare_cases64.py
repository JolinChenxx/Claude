from pathlib import Path
import subprocess,sys,json,time
R=Path(__file__).resolve().parents[1];W=R/'work'
def call(*a):subprocess.run([sys.executable,str(W/'run_standalone.py'),*map(str,a)],cwd=W,check=True)
seed=W/'VTE_ready.mph';cases=[]
for case,mesh,step,end in [('B64',1,.5,None),('G64',.75,.5,None),('T64',1,.25,None),('BENCH64',1,.5,180)]:
    dest=W/(case+'_ready.mph')
    if dest.exists():raise FileExistsError(dest)
    args=['Prepare64',seed,dest,str(mesh),str(step)]
    if end is not None:args.append(str(end))
    call(*args);call('Inspect64',dest);call('ExportSettings',dest,R/'results'/(case+'_initial_settings.json'))
    cases.append({'case':case,'mesh_factor':mesh,'maxstep_ns':step,'source':str(seed),'initial_state':'293.15 K, zero copper vapor, zero fluid velocity; no solution inheritance','target_ns':end or 2120,'version':'6.4.0.429'})
(R/'results/prepared_cases64.json').write_text(json.dumps({'cases':cases,'prepared_epoch':time.time()},indent=2))
