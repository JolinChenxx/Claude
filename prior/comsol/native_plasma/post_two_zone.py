"""Conservation and actual-state diagnostics for the native two-zone model."""
from pathlib import Path
import os,sys,subprocess,time,csv,json
import numpy as np
from scipy.integrate import cumulative_trapezoid
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent
case=sys.argv[1];segments=sys.argv[2:]+[case]
while True:
    p=R/'logs'/f'{case}_solver.log';s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    if 'Total time:' in s or '\u603b\u65f6\u95f4:' in s:break
    time.sleep(30)
env=os.environ.copy();env.update(ROUTE1_THREADS='4',ROUTE1_LOG_TAG=case+'_twopost')
dest=R/'results'/f'{case}_two_zone.csv'
lock=dest.with_suffix('.export.lock')
while True:
    try:
        lockfd=os.open(lock,os.O_CREAT|os.O_EXCL|os.O_WRONLY);os.close(lockfd);break
    except FileExistsError:time.sleep(5)
try:
    if not dest.exists():
        pending=dest.with_suffix('.pending.csv')
        subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java','ExportTwoZone',str(R/'models'/f'{case}.mph'),str(pending)],env=env,check=True)
        pending.replace(dest)
finally:lock.unlink(missing_ok=True)
def load(name):
    with (R/'results'/f'{name}_two_zone.csv').open() as f:
        rd=csv.DictReader(f);names=rd.fieldnames;rows=list(rd)
    return {n:np.array([float(row[n]) for row in rows]) for n in names}
d=load(segments[0])
for seg in segments[1:]:
    q=load(seg);keep=d['time_s']<q['time_s'][0]-1e-15
    d={n:np.r_[v[keep],q[n]] for n,v in d.items()}
t=d['time_s'];ns=t*1e9
def c(n):return d['c.'+n]
def o(n):return d['outer.'+n]
def delta(a):return a-a[0]
target=c('intCu(rhoCu*c.hCond)')
energy=target+c('gasEnergy*energyScale')+o('totalGasShellEnergy')
base=cumulative_trapezoid(c('intBase(-c.kCu*(c.Tr*nr+c.Tz*nz))'),t,initial=0)
eres=delta(energy)+delta(c('radiatedEnergy*energyScale'))+delta(c('ambientWork*energyScale'))-delta(c('entrainedEnergy*energyScale'))+base-delta(c('absorbedEnergy*energyScale'))
cu0=c('rhoCopper')*c('Vp');cu1=o('rhoCopper')*o('Vp')
mres=delta(cu0+cu1)-delta(c('removedMass*mScale'))
denom=max(abs(delta(c('absorbedEnergy*energyScale'))[-1]),1e-30)
negative={n:float(v.min()) for n,v in d.items() if (n.startswith('c.plas.w') or n.startswith('outer.plas2.w')) and v.min()<0}
report=dict(case=case,segments=segments,native_two_reservoir_model=True,spatial_PDE_prediction=False,saved_start_ns=float(ns[0]),saved_end_ns=float(ns[-1]),energy_error_percent=float(eres[-1]/denom*100),copper_error_percent=float(mres[-1]/max(abs(delta(c('removedMass*mScale'))[-1]),1e-30)*100),negative_mass_fractions=negative,max_outer_radius_um=float(o('Rp').max()*1e6),max_target_K=float(c('maxTarget(c.T)').max()),inner_peak_ne_m3=float(c('plas.ne').max()),outer_peak_ne_m3=float(o('plas2.ne').max()),inner_laser_absorption_uJ=float(np.trapz(c('Plaser'),t)*1e6),outer_laser_absorption_uJ=float(np.trapz(o('Plaser'),t)*1e6),limitations=['Fixed11um inner control volume and uniform outer hemisphere','No resolved target separation or sheath','Inherited high-temperature copper closure','Line escape approximated independently in each reservoir; radiation exchange between reservoirs not included in kinetic closure'])
report['higher_copper_charge_states']='c.nCu2' in d
report['prescribed_thermal_source']='c.thermalSourceMode' in d
if report['prescribed_thermal_source']:
    report['energy_denominator']='Injected thermal-source enthalpy plus both gas laser absorptions; target internal energy excluded'
    report['limitations']+=['Prescribed actual thermal evaporation history; no plasma shielding, gas heat or return flux feedback to copper','Target reserves10percent of laser energy; gas receives remaining share, a modular predictor rather than converged two-way thermal coupling']
if report['higher_copper_charge_states']:
    report['max_ionic_fractions']={p+str(q):float(np.max(d[p+f'nCu{q}']/np.maximum(d[p+'nCuTotal'],1))) for p in ['c.','outer.'] for q in [2,3,4]}
    report['limitations'].append('Ground ionic states through Cu4+ only; ionic excited lines and dielectronic recombination omitted')
else:report['limitations'].append('Singly ionized copper truncation is inadequate in the5-9eV hot core; see G32_second_ionization_audit.json')
out=R/'figures'/case;out.mkdir(exist_ok=True)
fig,ax=plt.subplots(2,3,figsize=(14,8),layout='constrained')
ax[0,0].plot(ns,o('Rp')*1e6,label='Outer material radius');ax[0,0].plot(ns,c('Rp')*1e6,label='Fixed source radius');ax[0,0].set_ylabel('um')
for zone,prefix in [('Source','c.'),('Outer','outer.')]:
    for var,ls in [('Tg','-'),('TeK','--')]:ax[0,1].plot(ns,d[prefix+var],ls,label=zone+' '+var)
ax[0,1].plot(ns,c('maxTarget(c.T)'),':',label='Target');ax[0,1].set_ylabel('K')
ax[0,2].semilogy(ns,c('plas.ne'),label='Source ne');ax[0,2].semilogy(ns,o('plas2.ne'),label='Outer ne');ax[0,2].set_ylabel('m^-3')
ax[1,0].plot(ns,c('Plaser'),label='Source absorption');ax[1,0].plot(ns,o('Plaser'),label='Outer absorption');ax[1,0].plot(ns,c('outPower'),label='Source-to-outer enthalpy');ax[1,0].set_ylabel('W')
ax[1,1].plot(ns,cu0*1e12,label='Source Cu');ax[1,1].plot(ns,cu1*1e12,label='Outer Cu');ax[1,1].plot(ns,c('removedMass*mScale')*1e12,'--',label='Target loss');ax[1,1].set_ylabel('ng')
ax[1,2].plot(ns,eres/denom*100,label='Total energy');ax[1,2].set_ylabel('% of absorbed energy')
for a in ax.flat:a.set_xlabel('Model time [ns]');a.grid(alpha=.2);a.legend(fontsize=7)
fig.suptitle(f'{case}: two native Plasma reservoirs with conservative mass/enthalpy exchange\nReduced geometry; no claim of resolved luminous-core position')
fig.savefig(out/'two_zone_history.png',dpi=160);plt.close(fig)
(R/'results'/f'{case}_two_zone_summary.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2),flush=True)
