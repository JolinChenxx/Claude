"""Saved-state heat transport check; does not alter any production trajectory."""
from pathlib import Path
import sys,json,time,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from electron_heat_transport import advance,EV,KB
case=sys.argv[1];root=R/'results'/case
status=json.loads((root/'status.json').read_text());d=np.load(status['frame']);g=np.load(root/'grid.npz')
U,S,W=d['U'],d['S'],d['W'];rows=[]
for ns in [.25,.125]:
    u=U.copy();s=S.copy();start=time.perf_counter();reports=[]
    for _ in range(round(.25/ns)):
        u,s,q=advance(u,s,W,g,ns*1e-9);reports.append(q)
    rows.append(dict(step_ns=ns,seconds=time.perf_counter()-start,steps=reports,
                     heavy_species_max_change=float(np.max(abs(s[:,:,:18]-S[:,:,:18]))),
                     electron_number_max_change=float(np.max(abs(s[:,:,18]-S[:,:,18]))),
                     local_total_minus_electron_change_max_J_m3=float(np.max(abs((u[:,:,4]-U[:,:,4])-(s[:,:,19]-S[:,:,19])*EV)))))
    if ns==.25:first=s[:,:,19].copy()
    else:difference=float((abs(first-s[:,:,19])*g['cell_volume_m3']).sum()/max((s[:,:,19]*g['cell_volume_m3']).sum(),1e-100))
report=dict(case=case,saved_ns=status['saved_time_ns'],scope='Isolated electron heat step, frozen species; not a new coupled plume solution or S4 prediction.',rows=rows,whole_vs_two_half_energy_field_L1_relative=difference)
(root/'electron_heat_operator_audit.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
