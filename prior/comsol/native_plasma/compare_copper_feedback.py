"""Compare predictor and actual gas-feedback iterate over their common saved interval."""
from pathlib import Path
import json,sys
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;cases=sys.argv[1:]
data=[]
for case in cases:
 s=np.genfromtxt(R/'results'/f'{case}_surface.csv',delimiter=',',names=True)
 h=np.genfromtxt(R/'results'/f'{case}_melt_history.csv',delimiter=',',names=True)
 data.append((case,s,h))
end=min(x[2]['time_s'].max() for x in data);start=max(x[2]['time_s'].min() for x in data)
fig,ax=plt.subplots(2,2,figsize=(11,7),layout='constrained');rows=[]
for case,s,h in data:
 h=h[(h['time_s']>=start-1e-16)&(h['time_s']<=end+1e-16)]
 ts=np.unique(s['time_s']);ts=ts[(ts>=start-1e-16)&(ts<=end+1e-16)]
 temp=np.array([s['T_K'][s['time_s']==t].max() for t in ts]);top=np.array([s['z_m'][s['time_s']==t].max() for t in ts])*1e6;bottom=np.array([s['z_m'][s['time_s']==t].min() for t in ts])*1e6
 names=h.dtype.names;time=h['time_s'];mass=np.r_[0,np.cumsum(np.diff(time)*(h[names[3]][:-1]+h[names[3]][1:])/2)]*1e12
 ax[0,0].plot(ts*1e9,temp,label=case);ax[0,1].plot(time*1e9,mass,label=case)
 ax[1,0].plot(ts*1e9,top,label=case+' top');ax[1,0].plot(ts*1e9,bottom,ls='--',label=case+' bottom')
 ax[1,1].plot(time*1e9,h[names[5]],label=case)
 rows.append(dict(case=case,common_end_ns=float(end*1e9),last_peak_temperature_K=float(temp[-1]),net_copper_loss_ng=float(mass[-1]),last_top_um=float(top[-1]),last_bottom_um=float(bottom[-1]),net_emitted_enthalpy_uJ=float(np.trapz(h[names[5]],time)*1e6)))
for p,y,title in zip(ax.flat,['K','ng','um','W'],['Peak copper surface temperature','Integrated net copper loss','Actual surface motion','Net outgoing copper enthalpy power']):
 p.set(xlabel='Model time [ns]',ylabel=y,title=title);p.legend(fontsize=8);p.grid(alpha=.2)
fig.suptitle('Same copper mesh and flow discretization; actual saved gas feedback\nCommon saved interval only; partitioned feedback iterates, not converged two-way coupling')
out=R/'figures'/'copper_feedback';out.mkdir(exist_ok=True);fig.savefig(out/'feedback_comparison.png',dpi=170)
(R/'results'/'copper_feedback_comparison.json').write_text(json.dumps(dict(rows=rows,limitations=['High-temperature material closure remains unsupported','No surface or time convergence claimed','Quadrature at saved times, not an exact solver conservation ledger']),indent=2),encoding='utf-8')
print(json.dumps(rows,indent=2))
