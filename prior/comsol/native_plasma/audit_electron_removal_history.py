"""Bound the previous trajectory's floor-induced extra electron energy loss."""
from pathlib import Path
import sys,json,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from split_history import read_history
from scipy.interpolate import PchipInterpolator
EV=1.602176634e-19;KB=1.380649e-23
case=sys.argv[1];g=np.load(R/'results'/case/'grid.npz');vol=g['cell_volume_m3'];rows=[]
chem=json.loads((R/'data/chemistry.json').read_text());rates=np.loadtxt(R/'data/log_rates.txt')
f=[PchipInterpolator(rates[:,0],rates[:,chem['rate_columns'].index(name)+1]) for name in ['O2_0','O2_1']]
for h in read_history(case):
 d=np.load(h['frame']);S,W=d['S'],d['W'];ag=np.maximum(W[:,:,6],1e-30);ne=S[:,:,18]/ag;actual=2/3*S[:,:,19]/np.maximum(S[:,:,18],1e-100)
 active=(W[:,:,6]>1e-6)&(ne>0)&(actual<.015)&(actual>0)
 if not active.any():rows.append(dict(time_ns=h['saved_time_ns'],extra_removal_W=0.,low_Te_electron_number=0.));continue
 n=S[:,:,:18]/ag[:,:,None];T=np.maximum(actual*EV/KB,1e-100);Tg=W[:,:,0];tv=np.maximum(actual,.015)
 k=sum(10**q(np.clip(np.log10(tv),rates[0,0],rates[-1,0])) for q in f)
 events=ne*(n[:,:,1]*k+2.8e-13*np.sqrt(300/T)*n[:,:,4]+2e-13*(300/T)**.7*n[:,:,5]+1.4e-41*300/T*np.exp(np.clip(-600/Tg+700*(T-Tg)/(T*Tg),-745,700))*n[:,:,1]**2)
 power=1.5*EV*(tv-actual)*events*ag
 rows.append(dict(time_ns=h['saved_time_ns'],extra_removal_W=float((power[active]*vol[active]).sum()),low_Te_electron_number=float((S[:,:,18][active]*vol[active]).sum())))
energy=float(np.trapz([q['extra_removal_W'] for q in rows],np.array([q['time_ns'] for q in rows])*1e-9))
report=dict(case=case,estimated_previous_extra_removal_J=energy,fraction_of_absorbed_J=energy/max(h['absorbed_J'],1e-100),peak=max(rows,key=lambda q:q['extra_removal_W']),rows=rows,scope='Saved-time quadrature of the removed floor artifact; does not replace a time-resolution check. Same rate formulas; interpolation choice immaterial below the0.015eV threshold where the rate-temperature input is fixed.')
(R/'results'/case/'electron_removal_history_audit.json').write_text(json.dumps(report,indent=2));print(json.dumps({k:v for k,v in report.items() if k!='rows'}),flush=True)
