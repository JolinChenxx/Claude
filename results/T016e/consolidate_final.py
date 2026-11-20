"""Consolidate existing T016e results; never run or alter solvers/images."""
import csv
import hashlib
import json
import math
import os
import subprocess
import sys
from datetime import datetime
from pathlib import Path
import pandas as pd

repo = Path(__file__).resolve().parents[2]
out = repo / 'results/T016e'
raw = Path('D:/sim_runs/T016e')
now = datetime.now().astimezone().isoformat()
def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()
with (repo / 'results/run_registry.csv').open(encoding='utf-8-sig', newline='') as f:
    registry = sorted([r for r in csv.DictReader(f) if r['batch'] == 'T016e'], key=lambda r:(r['run'],int(r['picard_it'])))
assert len(registry) == 16
assert len({(r['run'],r['picard_it']) for r in registry}) == 16
rows, files = [], []
for r in registry:
    member, iteration = r['run'], 'it' + r['picard_it']
    dst, src = out / member / iteration, raw / member / iteration
    pairs = [(src/'budget.csv',dst/'budget.csv'),(src/'pluto.ini',dst/'pluto.ini'),(src/'source/source_meta.json',dst/'source_meta.json')]
    pairs += [(p, dst/'image'/p.name) for p in (src/'image').glob('metrics_*.csv')]
    if (src/'image/image_summary.json').exists():
        pairs.append((src/'image/image_summary.json',dst/'image/image_summary.json'))
    for original, copy in pairs:
        h=sha(original)
        assert sha(copy)==h,(original,copy)
        files.append({'path':copy.relative_to(repo).as_posix(),'source':str(original),'sha256':h})
    with (dst/'budget.csv').open(newline='') as f:
        budgets = [{k:float(v) for k,v in row.items()} for row in csv.DictReader(f)]
    assert all(math.isfinite(v) for b in budgets for v in b.values())
    assert all(b['t_ns']>a['t_ns'] for a,b in zip(budgets,budgets[1:]))
    first,last=budgets[0],budgets[-1]
    source=json.loads((dst/'source_meta.json').read_text())
    residual=last['E_internal']+last['E_kinetic']-first['E_internal']-first['E_kinetic']+last['E_rad']+last['E_wall']-last['E_gas_abs']-last['E_src']
    nimages=len(list((dst/'image').glob('metrics_*.csv')))
    done=int(r['returncode'])==0
    count=None
    if done:
        d=json.loads((dst/'completion.json').read_text())
        assert d['registry_record']['returncode']=='0'
        assert last==d['final_budget']
        count=d['total_ConsToPrim_lines']
        assert count==sum(x['ConsToPrim_lines'] for x in d['all_rank_logs'])
        assert len(d['all_rank_logs'])==int(r['procs'])
        assert nimages==18 and last['t_ns']==(800 if member.startswith('high') else 1300)
    else:
        assert nimages==0
    rows.append({'member':member,'iteration':iteration,'status':'completed; numerical review required' if done else 'authorized stop; partial',
                 'rc':int(r['returncode']),'seconds':float(r['seconds']),'ConsToPrim':count,
                 't_ns':last['t_ns'],'E_gas_abs_J':last['E_gas_abs'],'E_rad_J':last['E_rad'],'E_wall_J':last['E_wall'],
                 'M_Cu_kg':last['M_Cu'],'M_src_kg':last['M_src'],'M_Cu_minus_M_src_kg':last['M_Cu']-last['M_src'],
                 'residual_J':residual,'residual_percent':100*residual/(last['E_gas_abs']+last['E_src']),
                 'target_model_abs_J':source['budget_J']['abs'],'PLUTO_target_abs_J':last['E_targ_abs'],
                 'absorption_difference_percent':100*(source['budget_J']['abs']/last['E_targ_abs']-1),
                 'image_metric_sets':nimages})
T=pd.DataFrame(rows)
assert (T.rc==0).sum()==11 and T.image_metric_sets.sum()==198
T.to_csv(out/'run_summary.csv',index=False)
notes = '''## Scope and limitations

This is the final execution inventory of **T016e v1e** (build v1d, diffusion tables v1c), using the originally requested v0 imaging. It is not a newly accepted physical baseline. The original batch launched at commit bc79e7d. Eleven iterations reached their configured endpoints with rc0; five later/other iterations were stopped by explicit Claude authority. Wrapper rc0 does not convert those member stops into successes. Full logs, fields and remaining frame archives stay under D:/sim_runs/T016e.

High G0 has completed it0/it1/it2 and partial stopped it3; low G0 has completed it0/it1 and partial stopped it2. High thin G1 has only it0, with 56,723,606 ConsToPrim lines and a large target-model/PLUTO absorption discrepancy. Original low thin G1 stopped at 498 ns; its separately stopped restart at 557.4858 ns is documented in results/T016e_r and is not spliced into the original budget. The incomplete members have no v0 image metrics and are explicitly listed in the inventory below.

The main metric table includes all 198 existing scenarios (11 iterations x 18); its grades retain the supplied report_v0.py definitions. The figure and P6 use the latest **available imaged** iteration: high G0 it2, low G0 it1, high G1 it0. They do not use the later partial budgets, establish Picard/grid convergence, or imply that stopped members were omitted from the inventory. Native/reference imaging is tracked separately by T016i/T017a and remains incomplete. No images or solvers were rerun for this consolidation.

All existing negative decay fits and missing threshold crossings are retained. Script-generated P1/P2/P3/P6 grades are not scientific acceptance. Completed-run all-rank warning counts below reuse previously hash-verified records; stopped-run counts are blank because a full stable all-rank count was not collected. This is not a zero-warning claim. Target-model absorption is the source-model integrated value; PLUTO absorption is through the stated budget time, so partial early runs need special care.

Residual_J = delta(E_internal + E_kinetic) + E_rad + E_wall - E_gas_abs - E_src; residual_percent divides it by E_gas_abs + E_src. This is the same inventory diagnostic as T015/T016, without a separate outgoing advective flux estimate. Small residuals do not resolve local ConsToPrim warnings. Absorption difference = 100*(target-model / PLUTO - 1).

## All production iterations, including authorized stops

'''
notes += T.to_markdown(index=False,floatfmt='.6g')+'\n'
failed=T[(T.rc==0)&(T.residual_percent.abs()>=.1)]
notes += '\n## Numerical review block\n\n'
notes += ('The inventory residual exceeds the PLAN section 4.2 requirement (<0.1%) for '+str(len(failed))+
          ' completed iterations: '+', '.join(f'{r.member}/{r.iteration} ({r.residual_percent:.7g}%)' for r in failed.itertuples())+
          '. This diagnostic omits explicit advective boundary flux, so interpretation requires Claude; no missing flux is invented. '
          'T016e is blocked for this numerical review, with execution/reporting complete and no further solver launch authorized.\n')
(out/'comparison_notes.md').write_text(notes,encoding='utf-8')
command=[sys.executable,str(repo/'scripts/report_v0.py'),str(out),'--out',str(out/'COMPARISON_v1e.md'),
         '--title','COMPARISON_v1e — T016e execution inventory and original v0 imaging','--notes',str(out/'comparison_notes.md')]
env=dict(os.environ)
prefix=Path(sys.executable).parent
env['PATH']=os.pathsep.join([str(prefix),str(prefix/'Library/bin'),str(prefix/'Scripts'),env.get('PATH','')])
env.update(PYTHONUTF8='1',OPENBLAS_NUM_THREADS='1',MKL_NUM_THREADS='1',OMP_NUM_THREADS='1')
with (out/'report_generation.log').open('w',encoding='utf-8') as log:
    subprocess.run(command,cwd=repo,env=env,stdout=log,stderr=subprocess.STDOUT,check=True)
metrics=pd.read_csv(out/'metrics_table.csv')
assert len(metrics)==198
assert metrics.groupby(['member','picard']).size().eq(18).all()
expected={(r['member'],r['iteration']) for r in rows if r['rc']==0}
assert set(zip(metrics.member,metrics.picard))==expected
validation={'collected':now,'solver_iterations':16,'completed_iterations':11,'authorized_stops':5,
            'image_metric_sets':198,'copy_verification':files,'generation_command':command,
            'grades':{k:metrics[k].value_counts().to_dict() for k in ['P1','P2','P3']},
            'completed_ConsToPrim_total':int(T.ConsToPrim.sum()),
            'max_abs_residual_percent':float(T.residual_percent.abs().max()),
            'completed_inventory_residual_above_0_1_percent':failed[['member','iteration','residual_percent']].to_dict('records'),
            'missing_warning_counts':'Five stopped iterations; not zero',
            'report_sha256':sha(out/'COMPARISON_v1e.md'),'metrics_sha256':sha(out/'metrics_table.csv')}
(out/'final_validation.json').write_text(json.dumps(validation,indent=2,allow_nan=False)+'\n',encoding='utf-8')
assert json.loads((out/'final_validation.json').read_text())==validation
print(json.dumps({k:v for k,v in validation.items() if k not in ('copy_verification','generation_command')}))
print('results_bytes',sum(p.stat().st_size for p in out.rglob('*') if p.is_file()))
