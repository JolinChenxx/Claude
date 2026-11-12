"""Resume the checked hot-cell solver from before the 94-96 ns rapid transition."""
from pathlib import Path
import os,sys,time,json,subprocess,shutil
R=Path(__file__).resolve().parent
audit=json.loads((R/'results/hot_group_audit/comparison.json').read_text())
if not audit['passed']:raise RuntimeError('Hot-cell comparison has not passed')
while True:
    log=(R/'logs/HC414.log').read_text(encoding='utf8',errors='replace')
    if 'STOPPED_AT_SAVED_FRAME' in log:break
    errors=(R/'logs/HC414.err').read_text(encoding='utf8',errors='replace')
    if 'Native trace ion negativity exceeds conservative rounding repair' in errors:
        # The 98 ns trial was rejected; use the verified 92 ns save, not its fields.
        for tag in ['R404_operator','R408_operator']:
            worker=R/'results'/tag;props=dict(q.split('=',1) for q in (worker/'request.properties').read_text().splitlines() if '=' in q)
            if not (worker/f'complete_{props["id"]}.json').exists():raise RuntimeError('Old native request is still running')
        print('REJECTED_COARSE_TRIAL_AFTER_96NS; RESUME_VERIFIED_92NS',flush=True);break
    if 'Traceback' in log:raise RuntimeError('Source stopped with an error')
    time.sleep(20)
history=json.loads((R/'results/HC414/history.json').read_text());snapshot=R/'results/HC414_at92';snapshot.mkdir(exist_ok=True)
state=next(q for q in history if abs(q['saved_time_ns']-92)<1e-6)
(snapshot/'status.json').write_text(json.dumps(state,indent=2));(snapshot/'history.json').write_text(json.dumps([q for q in history if q['saved_time_ns']<=92+1e-6],indent=2))
for filename in ['grid.npz','configuration.json']:shutil.copy2(R/'results/HC414'/filename,snapshot/filename)
if (R/'results/HC417/status.json').exists():raise RuntimeError('Refuse to overwrite HC417')
env=os.environ.copy();env.update(ROUTE1_PHASE_FEED='thermal_neutral',ROUTE1_EOS_INVERSION='warm',ROUTE1_MATERIAL_THREADS='12',ROUTE1_CU_REACTION_WORKER='R408_operator',ROUTE1_HOT_CU_REACTION_WORKER='R409_operator',ROUTE1_PARALLEL_REACTION_GROUPS='1',ROUTE1_REACTION_SCHEDULE='110:0.5,180:2')
cfgpath=R/'day_session_20261110.json';cfg=json.loads(cfgpath.read_text(encoding='utf8'));cfg['split_material_cases']=[x if x!='HC414' else 'HC417' for x in cfg['split_material_cases']];cfgpath.write_text(json.dumps(cfg,indent=2),encoding='utf8')
with (R/'logs/HC417.log').open('wb') as log:subprocess.run([sys.executable,'-X','utf8',str(R/'coupled_hem_plasma.py'),'HC417','180','R404_operator','HC414_at92'],env=env,stdout=log,stderr=subprocess.STDOUT,check=True)
