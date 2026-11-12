"""Launch one accepted-state continuation and its exposure postprocessing."""
from pathlib import Path
import os,sys,json,subprocess
R=Path(__file__).resolve().parent
case,source,end=sys.argv[1:4];end=float(end)
cfg=json.loads((R/'results'/source/'configuration.json').read_text()) if source!='initial' else {'eos':'Cu_HEM_wide_20261110'}
eos=sys.argv[4] if len(sys.argv)>4 else cfg['eos']
state=json.loads((R/'results'/source/'status.json').read_text()) if source!='initial' else {'saved_time_ns':0.};start=state['saved_time_ns']
if end<=start:raise ValueError('Continuation end must exceed actual saved time')
if (R/'results'/case).exists() or (R/'logs'/f'{case}.log').exists():raise FileExistsError(case)
for name in ([] if '--actual-removal' in sys.argv else ['R414','R415','R416','R417']):
    root=R/'results'/f'{name}_operator'
    if not (root/'ready.txt').exists() or (root/'stop.txt').exists():raise RuntimeError(f'{name} is not ready')
audit=json.loads((R/'results/smaller_copper_worker_audit.json').read_text())
if max(audit['peak_scaled_field_differences'].values())>.001:raise RuntimeError('Native worker comparison needs review')
env=os.environ.copy();env.update(ROUTE1_MULTICHARGE='1',ROUTE1_PHASE_FEED='thermal_neutral',ROUTE1_PHASE_FLASH='coupled',ROUTE1_EOS_INVERSION='warm',ROUTE1_EOS_DIR=eos,ROUTE1_MATERIAL_THREADS='16',ROUTE1_CU_REACTION_WORKER='R417_operator',ROUTE1_HOT_CU_REACTION_WORKER='R414_operator',ROUTE1_AIR_REACTION_WORKER='R415_operator',ROUTE1_PARALLEL_REACTION_GROUPS='1',ROUTE1_REACTION_SCHEDULE=f'90:2,100:0.25,110:0.5,{end}:2')
if '--parallel-hot' in sys.argv:
    for worker in ['R418','R420']:
        comparison=json.loads((R/'results'/f'{worker}_operator/comparison.json').read_text())
        if max(comparison['peak_scaled_field_differences'].values())>.001:raise RuntimeError(f'{worker} comparison requires review')
    env.update(ROUTE1_HOT_AIR_REACTION_WORKER='R418_operator',ROUTE1_SECOND_HOT_CU_WORKER='R420_operator')
if '--four-hot' in sys.argv:
    if '--parallel-hot' not in sys.argv:raise ValueError('Four hot workers require the parallel-hot layout')
    for worker in ['R422','R423']:
        comparison=json.loads((R/'results'/f'{worker}_operator/comparison.json').read_text())
        if max(comparison['peak_scaled_field_differences'].values())>.001:raise RuntimeError(f'{worker} comparison requires review')
    env.update(ROUTE1_EXTRA_HOT_CU_WORKERS='R422_operator,R423_operator',ROUTE1_MATERIAL_THREADS='32')
main_worker='R416_operator'
if '--trace-air' in sys.argv:
    comparison=json.loads((R/'results/R434_operator/comparison.json').read_text())
    if max(comparison['peak_scaled_field_differences'].values())>.001:raise RuntimeError('Trace-air operator comparison needs review')
    env['ROUTE1_TRACE_AIR_WORKER']='R434_operator'
if '--actual-removal' in sys.argv:
    for worker in ['R426','R427','R428','R429','R430','R431','R432','R433']:
        folder=R/'results'/f'{worker}_operator'
        if not (folder/'ready.txt').exists() or (folder/'stop.txt').exists() or not json.loads((folder/'worker_config.json').read_text()).get('actual_electron_removal'):raise RuntimeError(f'{worker} corrected worker unavailable')
    main_worker='R427_operator'
    env.update(ROUTE1_CU_REACTION_WORKER='R426_operator',ROUTE1_AIR_REACTION_WORKER='R428_operator',ROUTE1_HOT_CU_REACTION_WORKER='R429_operator',ROUTE1_SECOND_HOT_CU_WORKER='R430_operator',ROUTE1_EXTRA_HOT_CU_WORKERS='R431_operator,R432_operator',ROUTE1_HOT_AIR_REACTION_WORKER='R433_operator',ROUTE1_MATERIAL_THREADS='32',ROUTE1_ACTUAL_ELECTRON_REMOVAL='1')
if '--direct-trace' in sys.argv:
    folder=R/'results/R436_operator'
    comparison=json.loads((folder/'comparison.json').read_text())
    if max(comparison['peak_scaled_field_differences'].values())>.001:raise RuntimeError('Direct trace startup comparison requires review')
    env['ROUTE1_TRACE_AIR_WORKER']='R436_operator'
if '--balanced-hot' in sys.argv:
    env['ROUTE1_BALANCED_HOT_BATCHES']='1'
with (R/'logs'/f'{case}.log').open('w') as out,(R/'logs'/f'{case}.err').open('w') as err:
    proc=subprocess.Popen([sys.executable,'-X','utf8',str(R/'coupled_hem_plasma.py'),case,str(end),main_worker]+([source] if source!='initial' else []),env=env,stdout=out,stderr=err)
targets=[t for t in [95,125,175,835,1000] if start<t<=end]
if 95<=start<125:targets.insert(0,start)
if not targets or targets[-1]!=end:targets.append(end)
with (R/'logs'/f'{case}_milestones.log').open('w') as out,(R/'logs'/f'{case}_milestones.err').open('w') as err:
    post=subprocess.Popen([sys.executable,'-X','utf8',str(R/'post_split_milestones.py'),case,*map(str,targets)],stdout=out,stderr=err)
session=json.loads((R/'day_session_20261110.json').read_text());session['split_material_cases']=[case if x==source else x for x in session['split_material_cases']]
(R/'day_session_20261110.json').write_text(json.dumps(session,indent=2))
report=dict(case=case,pid=proc.pid,post_pid=post.pid,source=source,start_ns=start,end_ns=end,eos=eos,workers=['R416','R415','R417','R414']+(['R418','R420'] if '--parallel-hot' in sys.argv else []))
if '--actual-removal' in sys.argv:report['workers']=['R426','R427','R428','R429','R430','R431','R432','R433']+(['R434'] if '--trace-air' in sys.argv else [])
if '--direct-trace' in sys.argv:report['workers'].append('R436')
report['balanced_hot_batches']='--balanced-hot' in sys.argv
(R/'results'/f'{case}_launch.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
