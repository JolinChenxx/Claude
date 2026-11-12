"""Reproducible export/diagnostics pipeline; optionally wait for current solve."""
import sys,time,subprocess,argparse
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *

def call(script,*args):
    print('RUN',script,*args,flush=True)
    subprocess.run([sys.executable,str(ROOT/'work'/script),*map(str,args)],check=True)

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('case');parser.add_argument('--wait',action='store_true')
    parser.add_argument('--merge-as');parser.add_argument('--segments',nargs='*',default=[]);parser.add_argument('--render',action='store_true')
    parser.add_argument('--coupled',action='store_true');parser.add_argument('--stride',type=int,default=2);parser.add_argument('--quench',type=float,default=0.);parser.add_argument('--no-trapping',action='store_true');a=parser.parse_args()
    if a.wait:
        while True:
            state=json.loads((ROOT/'active_solver.json').read_text())
            if state.get('case')!=a.case:raise RuntimeError('Active case changed before pipeline observed completion')
            if 'status' in state:
                if state['status']!='finished':raise RuntimeError(f'Solver is {state["status"]}; inspect before processing')
                break
            time.sleep(5)
    call('comsol_driver.py','export',a.case)
    if a.coupled:call('budget.py',a.case)
    case=a.case
    if a.merge_as:
        call('merge_exports.py',a.merge_as,*a.segments,a.case);case=a.merge_as
    if a.coupled:call('coupled_budget.py',case)
    call('kinetic_energy.py',case)
    call('state_validation.py',case)
    call('reaction_budget.py',case,a.quench,int(not a.no_trapping))
    call('reservoir.py',case)
    print('DIAGNOSTICS_READY',case,flush=True)
    if a.render:
        call('render_kinetics.py',case,'--stride',a.stride)
        call('gate_resolution.py',case+'_radiation')
        call('escape_consistency.py',case)
    print('POSTPROCESS_COMPLETE',case,flush=True)
