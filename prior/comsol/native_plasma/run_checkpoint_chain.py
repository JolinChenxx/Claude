"""Run a fresh named model, save first exposure, and continue its unchanged physics."""
from pathlib import Path
import os,sys,subprocess
R=Path(__file__).resolve().parent
case,numa=sys.argv[1:3]
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA=numa,ROUTE1_LOG_TAG=case)
with (R/'logs'/f'{case}_checkpoint.log').open('wb') as f:
    subprocess.Popen([sys.executable,'-X','utf8',str(R/'checkpoint_run.py'),case,'123'],stdout=f,stderr=subprocess.STDOUT,env=env)
code=subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'solve',case],env=env).returncode
print('INITIAL_SOLVE_EXIT',case,code,flush=True)
env.pop('ROUTE1_LOG_TAG',None)
if (R/'models'/f'{case}.mph').exists():
    with (R/'logs'/f'{case}_chain.log').open('wb') as f:
        subprocess.Popen([sys.executable,'-X','utf8',str(R/'flat_chain.py'),case,numa],stdout=f,stderr=subprocess.STDOUT,env=env)
    print('UNCHANGED_PHYSICS_CHAIN_STARTED',case,flush=True)
