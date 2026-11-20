"""Finish the old render, archive it, then apply corrected Cu lifetimes."""
import sys,time
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from postsolve import call

case='R2_CR1T';folder=RES/f'{case}_radiation';archive=RES/f'{case}_radiation_transition_width'
while not (folder/'summary.json').exists():time.sleep(5)
state=json.loads((folder/'progress.json').read_text())
if state['finished']!=len(state['ids']):raise RuntimeError('Old renderer has not completed')
if archive.exists():raise FileExistsError(archive)
folder.rename(archive)
call('render_kinetics.py',case,'--stride',2)
call('gate_resolution.py',case+'_radiation')
call('escape_consistency.py',case)
call('plot_mechanisms.py',case)
call('compare_observables.py',case)
call('band_comparison.py','LTE',case)
print('CORRECTED_1T_RADIATION_READY',flush=True)
