"""Validate parallel disjoint native calls against the existing serial 126-128 ns segment, then continue."""
from pathlib import Path
import os,sys,time,json,subprocess,shutil
import numpy as np
R=Path(__file__).resolve().parent
source=R/'results/HC413'
while True:
    log=(R/'logs/HC413.log').read_text(encoding='utf8',errors='replace')
    if 'STOPPED_AT_SAVED_FRAME' in log:break
    if 'Traceback' in log:raise RuntimeError('Serial reference did not finish normally')
    time.sleep(30)
history=json.loads((source/'history.json').read_text())
reference=next(s for s in history if abs(s['saved_time_ns']-128)<1e-6)
base=next(s for s in history if abs(s['saved_time_ns']-126)<1e-6)
snapshot=R/'results/HC413_at126'
if not snapshot.exists():
    snapshot.mkdir()
    (snapshot/'status.json').write_text(json.dumps(base,indent=2))
    (snapshot/'history.json').write_text(json.dumps([s for s in history if s['saved_time_ns']<=126+1e-6],indent=2))
    for name in ['grid.npz','configuration.json']:shutil.copy2(source/name,snapshot/name)
env=os.environ.copy();env.update(ROUTE1_PHASE_FEED='ground_neutral',ROUTE1_EOS_INVERSION='warm',ROUTE1_MATERIAL_THREADS='12',ROUTE1_CU_REACTION_WORKER='R407_operator',ROUTE1_PARALLEL_REACTION_GROUPS='1')
def run(case,end,prior):
    with (R/'logs'/f'{case}.log').open('wb') as log:
        subprocess.run([sys.executable,'-X','utf8',str(R/'coupled_hem_plasma.py'),case,str(end),'R403_operator',prior],env=env,stdout=log,stderr=subprocess.STDOUT,check=True)
if not (R/'results/HC415/status.json').exists():run('HC415',128,'HC413_at126')
got=json.loads((R/'results/HC415/status.json').read_text());a=np.load(reference['frame']);b=np.load(got['frame']);differences={}
for key in ['U','W','S']:
    aa,bb=a[key],b[key];scale=np.maximum(np.max(abs(aa),axis=tuple(range(aa.ndim-1)),keepdims=True),1e-100) if key!='reaction_outputs' else np.maximum(np.max(abs(aa),axis=1,keepdims=True),1e-100)
    differences[key]=float(np.max(abs(aa-bb)/scale))
aa,bb=a['reaction_outputs'],b['reaction_outputs']
per_field=np.max(abs(aa-bb),axis=1)/np.maximum(np.max(abs(aa),axis=1),1e-100)
# Energy CHANGE and near-zero net charge are differences of large inventories;
# a relative tolerance normalized by their own zero is not an accuracy measure.
selected=[i for i in range(37) if i not in (5,18)]
differences['native_physical_fields']=float(per_field[selected].max())
g=np.load(source/'grid.npz');ij=a['reaction_indices'];v=g['cell_volume_m3'][ij[:,0],ij[:,1]];ag=a['W'][ij[:,0],ij[:,1],6]
energy_delta_difference=float(np.sum(abs(aa[5]-bb[5])*ag*v))
charge_delta_difference=float(np.sum(abs(aa[18]-bb[18])*ag*v))
same_cells=bool(np.array_equal(a['reaction_indices'],b['reaction_indices']))
audit=dict(serial_case='HC413',parallel_case='HC415',interval_ns=[126,128],max_component_scaled_difference=differences,
    reference_absorbed_J=reference['absorbed_J'],parallel_absorbed_J=got['absorbed_J'],
    native_output_relative_differences=per_field.tolist(),absolute_integrated_energy_delta_difference_J=energy_delta_difference,
    absolute_integrated_charge_delta_difference_C=charge_delta_difference,
    comparison_includes_restart=True,criterion='Component-scaled primary fields below native relative tolerance 1e-4; energy increment cancellation normalized to absorbed energy; net-charge difference recorded in C.',
    passed=same_cells and max(differences.values())<1e-4 and energy_delta_difference/reference['absorbed_J']<1e-6 and charge_delta_difference<1e-20)
(R/'results/parallel_native_validation.json').write_text(json.dumps(audit,indent=2));print(json.dumps(audit),flush=True)
if not audit['passed']:raise RuntimeError('Parallel sequence differs from reference; no automatic continuation')
cfgpath=R/'day_session_20261110.json';cfg=json.loads(cfgpath.read_text(encoding='utf8'));cfg['split_material_cases']=[x if x!='HC413' else 'HC416' for x in cfg['split_material_cases']];cfgpath.write_text(json.dumps(cfg,indent=2),encoding='utf8')
run('HC416',180,'HC415')
