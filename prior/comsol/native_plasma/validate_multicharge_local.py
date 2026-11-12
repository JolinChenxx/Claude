from pathlib import Path
import json,time,sys,numpy as np
R=Path(__file__).resolve().parent;p=R/'results/R414_operator';key=sys.argv[1] if len(sys.argv)>1 else 'multicharge_hot_audit'
while not (p/f'complete_{key}.json').exists():
 if (p/f'error_{key}.txt').exists():raise RuntimeError((p/f'error_{key}.txt').read_text())
 time.sleep(3)
inp=np.loadtxt(p/'state.txt');meta=json.loads((p/f'complete_{key}.json').read_text());d=np.fromfile(p/f'output_{key}.bin',dtype='>f8').reshape(meta['shape']);n=d[19:].T;old=inp[:,7:]
charge=np.array([0,0,0,0,1,1,1,1,-1,-1,0,0,0,0,0,0,0,1,2,3,4]);mass=np.array([.0280134,.0319988,.0140067,.0159994,.0280134,.0319988,.0140067,.0159994,.0159994,.0319988]+[.063546]*11)/6.02214076e23
energy=np.array([0,0,4.88,2.58,15.58,12.06,19.414,16.198,1.119,-.45,0,1.388948,1.6422256,3.7858977,3.816692,6.1911751,6.1920252,7.72638,28.01877,64.85977,122.23977])*1.602176634e-19
cv=np.array([2.5,2.5,1.5,1.5,2.5,2.5,1.5,1.5,1.5,2.5]+[1.5]*11)*1.380649e-23
ei=inp[:,3]*inp[:,2]+old@energy+inp[:,5]*1.602176634e-19
ef=(inp[:,3]+(n-old)@cv)*d[0]+n@energy+d[2]*1.602176634e-19
charge_delta=(n@charge-d[1])-(old@charge-inp[:,4]);cu0=old[:,10:].sum(1);cu1=n[:,10:].sum(1)
report=dict(native_seconds=meta['seconds'],max_Cu_relative_change=float((abs(cu1-cu0)/np.maximum(cu0,1.)).max()),max_mass_relative_change=float((abs((n-old)@mass)/inp[:,1]).max()),max_charge_delta_over_initial_heavy=float((abs(charge_delta)/np.maximum(old.sum(1),1.)).max()),max_energy_relative_residual=float((abs(ef-ei-d[5])/np.maximum(abs(ei),1)).max()),max_Cu2_number_m3=float(n[:,18].max()),max_Cu3_number_m3=float(n[:,19].max()),max_Cu4_number_m3=float(n[:,20].max()),max_Te_K=float(d[17].max()),minimum_species_m3=float(n.min()),input='R414_operator/state.txt',saved_mph=f'R414_operator/native_operator_{key}.mph',scope='Actual hot local input; same published rate prescription, no spatial transport')
report['passed']=report['max_Cu_relative_change']<1e-5 and report['max_charge_delta_over_initial_heavy']<1e-5 and report['max_energy_relative_residual']<1e-3
import shutil
shutil.copy2(p/'state.txt',R/'results'/f'{key}_input.txt')
report['input']=f'{key}_input.txt';report['saved_mph']=f'R414_operator/native_operator_{key}.mph'
(R/'results/multicharge_local_validation.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
if not report['passed']:raise RuntimeError('Local multicharge balance audit failed')
