"""Continue an accepted trajectory with two disjoint native air reaction workers."""
from pathlib import Path
import os,sys,json,subprocess,time
R=Path(__file__).resolve().parent
source='HC423';case='HC424'
while 'STOPPED_AT_SAVED_FRAME' not in (R/'logs'/f'{source}.log').read_text(encoding='utf8',errors='replace'):
    if (R/'logs'/f'{source}.err').stat().st_size:raise RuntimeError('Source failed instead of saving normally')
    time.sleep(3)
for worker in ['R415','R416']:
    while not (R/'results'/f'{worker}_operator/ready.txt').exists():time.sleep(3)
(R/'results/R412_operator/stop.txt').write_text('Retire after accepted frame; parallel air workers R415/R416 replace this process.\n')
env=os.environ.copy();env.update(ROUTE1_MULTICHARGE='1',ROUTE1_PHASE_FEED='thermal_neutral',ROUTE1_PHASE_FLASH='coupled',ROUTE1_EOS_INVERSION='warm',ROUTE1_EOS_DIR='Cu_HEM_extended_20261110',ROUTE1_MATERIAL_THREADS='16',ROUTE1_CU_REACTION_WORKER='R413_operator',ROUTE1_HOT_CU_REACTION_WORKER='R414_operator',ROUTE1_AIR_REACTION_WORKER='R415_operator',ROUTE1_PARALLEL_REACTION_GROUPS='1',ROUTE1_REACTION_SCHEDULE='90:2,100:0.25,110:0.5,180:2')
with (R/'logs'/f'{case}.log').open('w') as out,(R/'logs'/f'{case}.err').open('w') as err:
    proc=subprocess.Popen([sys.executable,'-X','utf8',str(R/'coupled_hem_plasma.py'),case,'180','R416_operator',source],env=env,stdout=out,stderr=err)
with (R/'logs'/f'{case}_milestones.log').open('w') as out,(R/'logs'/f'{case}_milestones.err').open('w') as err:
    post=subprocess.Popen([sys.executable,'-X','utf8',str(R/'post_split_milestones.py'),case,'95','125','175'],stdout=out,stderr=err)
cfg=json.loads((R/'day_session_20261110.json').read_text());cfg['split_material_cases']=[case if x==source else x for x in cfg['split_material_cases']]
(R/'day_session_20261110.json').write_text(json.dumps(cfg,indent=2))
print(json.dumps(dict(case=case,pid=proc.pid,post_pid=post.pid,source=source,change='Same physical model; disjoint air batches in two16-thread native workers; accepted state continuation')),flush=True)
