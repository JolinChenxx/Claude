"""Run the two already prepared comparison cases sequentially, one solver."""
import sys,time
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from postsolve import call

ready=RES/'R2_COUPLED_state_validation.json'
while not ready.exists():time.sleep(5)
state=json.loads(ready.read_text())
if state['last_numerical_ns']<899.9:raise RuntimeError('q0 coupled history did not reach 900 ns')
call('comsol_driver.py','solve','R2_COUPLED_Q','--threads',8)
call('postsolve.py','R2_COUPLED_Q','--coupled','--quench',1,'--render')
call('comsol_driver.py','solve','R2_CR1T_R','--threads',8)
call('postsolve.py','R2_CR1T_R','--render')
print('PREPARED_COMPARISON_CASES_COMPLETE',flush=True)
