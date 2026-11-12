"""Check the actual rejected front state against the extended nuclear EOS."""
from pathlib import Path
import os,sys,json,numpy as np
sys.path.insert(0,str(Path(__file__).resolve().parent))
os.environ['ROUTE1_MULTICHARGE']='1'
os.environ['ROUTE1_PHASE_CHARGE_PARTNERS']='1'
from coupled_hem_plasma import ENERGY,CV
from hem_reactive_flash import reactive_flash
from numba import set_num_threads
set_num_threads(16)
R=Path(__file__).resolve().parent
d=np.load(R/'results/HC434/rejected_flash_state.npz')
e=np.load(R/'data/Cu_HEM_wide_20261110/eos.npz')
ts=e['temperatures_K'];rs=e['densities_kg_m3']
tab=np.ascontiguousarray(e['table'][:,2:].reshape(len(ts),len(rs),7))
w,s,err=reactive_flash(d['U'],d['S'],d['W'],np.log10(ts),np.log10(rs),tab,ENERGY,CV,True)
scale=np.maximum(abs(d['U'][:,:,4]),d['U'][:,:,:2].sum(2)*1e4)
report=dict(failed_cells=int(np.isnan(err).sum()),min_temperature_K=float(w[:,:,0].min()),max_scaled_energy_residual=float(np.nanmax(abs(err)/scale)),front_temperature_K=float(w[0,265,0]))
(R/'results/cold_eos_extension_audit.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report),flush=True)
if report['failed_cells'] or report['max_scaled_energy_residual']>1e-6:raise RuntimeError('Cold extension does not resolve rejected state')
