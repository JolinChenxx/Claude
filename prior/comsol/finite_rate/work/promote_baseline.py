"""Archive the transition-only linewidth baseline and promote corrected data."""
import sys,time,shutil
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from postsolve import call

new=RES/'R2_B64A_LTE_LW';old=RES/'R2_B64A_LTE';archive=RES/'R2_B64A_LTE_transition_width'
while not (new/'summary.json').exists():time.sleep(5)
summary=json.loads((new/'summary.json').read_text())
if summary.get('status')!='completed':raise RuntimeError('Corrected baseline incomplete')
if archive.exists():raise FileExistsError(archive)
old.rename(archive);new.rename(old)
call('plot_baseline.py');call('morphology.py')
call('plot_mechanisms.py','R2_CR1T')
call('compare_observables.py','R2_CR1T')
call('band_comparison.py','LTE','R2_CR1T')
print('CORRECTED_LTE_BASELINE_PROMOTED',flush=True)
