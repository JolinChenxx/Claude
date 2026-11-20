"""Audit the fixed native source reservoir without presenting it as an S4 image."""
from pathlib import Path
import sys,os,subprocess,time,json
import numpy as np
from scipy.integrate import cumulative_trapezoid
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent
sys.path.insert(0,str(R))
from plot_global_plume import load
case=sys.argv[1]
while True:
    p=R/'logs'/f'{case}_solver.log'
    s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    if 'Total time:' in s or '总时间:' in s:break
    time.sleep(30)
env=os.environ.copy();env.update(ROUTE1_THREADS='4',ROUTE1_LOG_TAG=case+'_sourcepost')
if not (R/'results'/f'{case}_global.csv').exists():
    subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java','ExportGlobalPlume',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_global.csv')],env=env,check=True)
segments=sys.argv[2:]+[case]
d=load(segments[0])
for segment in segments[1:]:
    more=load(segment);keep=d['time_s']<more['time_s'][0]-1e-15
    d={n:np.r_[values[keep],more[n]] for n,values in d.items()}
t=d['time_s'];ns=t*1e9
def c(n):return d['c.'+n]
energy=c('gasEnergy*energyScale')+c('intCu(rhoCu*c.hCond)')
cu=c('rhoCopper')*c('Vp')
base=cumulative_trapezoid(c('intBase(-c.kCu*(c.Tr*nr+c.Tz*nz))'),t,initial=0)
def delta(n):
    a=c(n);return a-a[0]
res=energy-energy[0]+delta('outEnergy*energyScale')+delta('radiatedEnergy*energyScale')+base-delta('absorbedEnergy*energyScale')
massres=cu-cu[0]+delta('outCuMass*mScale')-delta('removedMass*mScale')
report=dict(case=case,segments=segments,diagnostic_only=True,spatial_prediction=False,saved_start_ns=float(ns[0]),saved_end_ns=float(ns[-1]),radius_um=float(c('Rp')[0]*1e6),max_ne_m3=float(c('plas.ne').max()),max_target_K=float(c('maxTarget(c.T)').max()),max_gas_K=float(c('Tg').max()),max_electron_K=float(c('TeK').max()),plasma_absorbed_uJ=float(np.trapz(c('Plaser'),t)*1e6),plasma_absorption_peak_W=float(c('Plaser').max()),outflow_energy_uJ=float(delta('outEnergy*energyScale')[-1]*1e6),outflow_copper_ng=float(delta('outCuMass*mScale')[-1]*1e12),energy_error_percent=float(res[-1]/max(abs(delta('absorbedEnergy*energyScale')[-1]),1e-30)*100),copper_error_percent=float(massres[-1]/max(abs(delta('removedMass*mScale')[-1]),1e-30)*100))
out=R/'figures'/case;out.mkdir(exist_ok=True)
fig,ax=plt.subplots(2,3,figsize=(14,8),layout='constrained')
for n,label in [('Tg','Gas'),('TeK','Electron'),('maxTarget(c.T)','Target peak')]:ax[0,0].plot(ns,c(n),label=label)
ax[0,0].set_ylabel('K');ax[0,1].semilogy(ns,c('plas.ne'),label='ne');ax[0,1].set_ylabel('m^-3')
ax[0,2].plot(ns,c('Plaser'),label='Plasma absorption');ax[0,2].plot(ns,c('outPower'),label='Outflow enthalpy');ax[0,2].set_ylabel('W')
ax[1,0].plot(ns,cu*1e12,label='Stored Cu');ax[1,0].plot(ns,c('outCuMass*mScale')*1e12,label='Ejected Cu');ax[1,0].plot(ns,c('removedMass*mScale')*1e12,label='Target loss');ax[1,0].set_ylabel('ng')
for n,label in [('absorbedEnergy*energyScale','Absorbed'),('outEnergy*energyScale','Exported enthalpy'),('radiatedEnergy*energyScale','Radiated')]:ax[1,1].plot(ns,c(n)*1e6,label=label)
ax[1,1].set_ylabel('uJ');ax[1,2].plot(ns,res*1e6,label='Energy residual');ax[1,2].set_ylabel('uJ')
for a in ax.flat:a.set_xlabel('Model time [ns]');a.grid(alpha=.2);a.legend(fontsize=8)
fig.suptitle(f'{case}: native near-target source-layer diagnostic; fixed 11 um reservoir\nNot an S4 spatial prediction; no radiation assigned outside reservoir')
fig.savefig(out/'source_layer_history.png',dpi=160);plt.close(fig)
(R/'results'/f'{case}_source_summary.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2),flush=True)
