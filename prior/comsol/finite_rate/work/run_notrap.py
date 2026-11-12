"""One mechanistic ablation, after the existing comparison queue finishes."""
import sys,time,json
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from postsolve import call
# Start after the 1T solver exits; its independent rendering can overlap.
while True:
    state=json.loads((ROOT/'active_solver.json').read_text())
    if state.get('case')=='R2_CR1T_R' and state.get('status')=='finished':break
    time.sleep(5)
call('comsol_driver.py','solve','R2_COUPLED_NT','--threads',8)
call('postsolve.py','R2_COUPLED_NT','--coupled','--no-trapping','--render')
while not (RES/'R2_CR1T_R_radiation/summary.json').exists():time.sleep(5)
call('numerical_comparison.py')
print('TRAPPING_ABLATION_COMPLETE',flush=True)
