"""Assemble final comparisons only after the last mechanism run completes."""
import sys,time
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from postsolve import call
while not (RES/'R2_COUPLED_NT_radiation/summary.json').exists():time.sleep(5)
cases=['R2_CR1T_R','R2_COUPLED','R2_COUPLED_Q','R2_COUPLED_NT']
call('plot_mechanisms.py',*cases)
call('compare_observables.py',*cases)
call('band_comparison.py','LTE','R2_COUPLED','R2_COUPLED_Q','R2_COUPLED_NT')
call('kinetic_layers.py','R2_COUPLED_NT')
call('final_report.py')
print('FINAL_OUTPUTS_READY_FOR_REVIEW',flush=True)
