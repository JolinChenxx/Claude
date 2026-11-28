"""Evaluate the phase/energy projection on saved physical states, without a solve."""
from pathlib import Path
import sys,json,time,numpy as np
from numba import set_num_threads
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from coupled_hem_plasma import ENERGY,CV,MASS,EV,KB
from hem_reactive_flash import reactive_flash,energy_state
set_num_threads(12)
e=np.load(R/'data/Cu_HEM_20261110/eos.npz');ts=e['temperatures_K'];rs=e['densities_kg_m3'];tab=np.ascontiguousarray(e['table'][:,2:].reshape(len(ts),len(rs),7))
reports=[]
for case in sys.argv[1:]:
    status=json.loads((R/'results'/case/'status.json').read_text());d=np.load(status['frame']);U,W,S=d['U'],d['W'],d['S'];g=np.load(R/'results'/case/'grid.npz');v=g['cell_volume_m3']
    start=time.perf_counter();nw,ns,err=reactive_flash(U,S,W,np.log10(ts),np.log10(rs),tab,ENERGY,CV,status.get('phase_feed')=='thermal_neutral');secs=time.perf_counter()-start
    charge=lambda s:s[:,:,[4,5,6,7,17]].sum(2)-s[:,:,[8,9,18]].sum(2)
    cu=(ns[:,:,10:18]*MASS[10:18]).sum(2)
    report=dict(case=case,time_ns=status['saved_time_ns'],seconds=secs,failed_cells=int(np.isnan(err).sum()),energy_abs_J=float((abs(err)*v).sum()),Cu_mass_abs_kg=float((abs(cu-U[:,:,0]*nw[:,:,5])*v).sum()),charge_change_C=float(((charge(ns)-charge(S))*v).sum()*EV),Tmax_before=float(W[:,:,0].max()),Tmax_after=float(nw[:,:,0].max()),max_relative_T_change=float((abs(nw[:,:,0]-W[:,:,0])/W[:,:,0]).max()),Cu_gas_mass_before=float((U[:,:,0]*W[:,:,5]*v).sum()),Cu_gas_mass_after=float((U[:,:,0]*nw[:,:,5]*v).sum()))
    reports.append(report);print(json.dumps(report),flush=True)
(R/'results/reactive_flash_audit.json').write_text(json.dumps(reports,indent=2))
