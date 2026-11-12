"""Physical axial profiles and column balances; not a plume-camera image."""
from pathlib import Path
import sys,json
import numpy as np
import matplotlib;matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;case=sys.argv[1];out=R/'figures'/case;out.mkdir(exist_ok=True)
d=json.loads((R/'results'/f'{case}_fields.json').read_text());a=np.memmap(R/'results'/f'{case}_fields.bin',mode='r',dtype='>f4',shape=tuple(d['shape']));x=np.array(d['x_m'])*1e6;t=np.array(d['times_s']);h=np.loadtxt(R/'results'/f'{case}_history.csv',delimiter=',',skiprows=1,ndmin=2)
def integ(y):return np.r_[0,np.cumsum((y[1:]+y[:-1])/2*np.diff(t))]
energy_flux=a[:,13,-1].astype(float)-a[:,13,0].astype(float);mass_flux=a[:,12,-1].astype(float)-a[:,12,0].astype(float)
absorbed=integ(h[:,4]);energy_res=h[:,3]-h[0,3]-absorbed+integ(energy_flux);mass_res=h[:,1]-h[0,1]+integ(mass_flux)
fig,axs=plt.subplots(2,3,figsize=(13,7),constrained_layout=True)
sel=np.unique(np.linspace(0,len(t)-1,5).astype(int))
for i in sel:
 label=f'{t[i]*1e9:.1f} ns'
 axs[0,0].plot(x,a[i,0],label=label);axs[0,1].semilogy(x,np.maximum(a[i,1],1e-10));axs[0,2].plot(x,a[i,8]);axs[1,0].plot(x,a[i,5])
for ax,title in zip(axs.flat[:4],['Temperature [K]','Total copper density [kg/m3]','Vapor mass fraction','Velocity [m/s]']):ax.set(xlim=(-5,70),xlabel='z [um]',title=title)
axs[0,0].legend(fontsize=8);axs[0,1].set_ylim(1e-8,2e4)
axs[1,1].plot(t*1e9,h[:,6],label='All vapor copper');axs[1,1].plot(t*1e9,h[:,7],label='All copper above z=0');axs[1,1].set(xlabel='Model time [ns]',ylabel='Column mass [kg/m2]');axs[1,1].legend(fontsize=8)
axs[1,2].plot(t*1e9,absorbed,label='Absorbed laser');axs[1,2].plot(t*1e9,h[:,3]-h[0,3],label='Stored energy change');axs[1,2].plot(t*1e9,energy_res,label='Balance residual');axs[1,2].set(xlabel='Model time [ns]',ylabel='Energy per area [J/m2]');axs[1,2].legend(fontsize=8)
fig.suptitle(f'{case}: axial equilibrium EOS/flux test; no Plasma or camera-image prediction\nLegacy Cu nuclear ANEOS, Tc=9030 K; inherited target absorption 0.1')
fig.savefig(out/'axial_eos_evolution.png',dpi=170);plt.close(fig)
report={'case':case,'saved_start_ns':float(t[0]*1e9),'saved_end_ns':float(t[-1]*1e9),'maximum_temperature_K':float(h[:,8].max()),'maximum_speed_m_s':float(h[:,9].max()),'minimum_temperature_K':float(h[:,10].min()),'absorbed_energy_J_m2':float(absorbed[-1]),'energy_residual_J_m2':float(energy_res[-1]),'energy_residual_fraction_of_absorbed':float(energy_res[-1]/absorbed[-1]) if absorbed[-1]>1e-12 else None,'copper_mass_residual_fraction_initial':float(mass_res[-1]/h[0,1]),'copper_above_z0_change_kg_m2':float(h[-1,7]-h[0,7]),'native_plasma_solved':False,'dimensionality':'1D axial material test','no_S4_fit':True}
(R/'results'/f'{case}_hem_summary.json').write_text(json.dumps(report,indent=2));print(json.dumps(report))
