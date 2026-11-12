"""Continue the thermal chain after its accepted checkpoint with no abstract Poisson solve."""
from pathlib import Path
import json, os, subprocess, sys, time
R=Path(__file__).resolve().parent
old=R/'results/HC418'
(old/'stop_requested.txt').write_text('Checkpoint after the current accepted reaction step; use validated collision-only worker.\n')
while 'STOPPED_AT_SAVED_FRAME' not in (R/'logs/HC418.log').read_text():
    err=(R/'logs/HC418.err').read_text()
    if 'Traceback' in err:break
    time.sleep(10)
cfg=R/'results/R410_operator/worker_config.json'
c=json.loads(cfg.read_text());c.update(abstract_potential_solved=False,validation='results/no_potential_audit94.json');cfg.write_text(json.dumps(c,indent=2))
env=os.environ.copy();env.update(ROUTE1_PHASE_FEED='thermal_neutral',ROUTE1_EOS_INVERSION='warm',ROUTE1_MATERIAL_THREADS='12',ROUTE1_CU_REACTION_WORKER='R408_operator',ROUTE1_HOT_CU_REACTION_WORKER='R410_operator',ROUTE1_PARALLEL_REACTION_GROUPS='1',ROUTE1_REACTION_SCHEDULE='110:0.5,180:2')
p=R/'day_session_20261110.json';c=json.loads(p.read_text(encoding='utf8'));c['split_material_cases']=[x for x in c['split_material_cases'] if x!='HC418']+['HC419'];p.write_text(json.dumps(c,ensure_ascii=False,indent=2),encoding='utf8')
with (R/'logs/HC419_milestones.log').open('w') as out,(R/'logs/HC419_milestones.err').open('w') as err:
    subprocess.Popen([sys.executable,'-X','utf8',str(R/'post_split_milestones.py'),'HC419','95','125','175'],stdout=out,stderr=err,env=env)
with (R/'logs/HC419.log').open('w') as out,(R/'logs/HC419.err').open('w') as err:
    subprocess.run([sys.executable,'-X','utf8',str(R/'coupled_hem_plasma.py'),'HC419','180','R404_operator','HC418'],stdout=out,stderr=err,env=env,check=True)
