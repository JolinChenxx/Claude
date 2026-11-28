"""Serial equal-interval thread benchmark, with bounded official-save stops."""
from pathlib import Path
import json,subprocess,sys,os,time,shutil
R=Path(__file__).resolve().parents[1];W=R/'work';rows=[];start=time.time()
for threads in [8,16,32]:
    if time.time()-start>=900:break
    case='BENCH'+str(threads)
    if (W/(case+'.mph')).exists():raise FileExistsError(case)
    shutil.copy2(W/'BENCH64_ready.mph',W/(case+'_ready.mph'))
    env=os.environ.copy();env['S4_MAX_SEGMENT_SECONDS']=str(min(300,900-(time.time()-start)))
    p=subprocess.run([sys.executable,str(W/'campaign.py'),case,'--threads',str(threads)],cwd=W,env=env)
    b=json.loads((R/'solver_budget.json').read_text());run=next((x for x in reversed(b['runs']) if x['case']==case),None)
    auditpath=R/'results'/(case+'_audit.json')
    a=json.loads(auditpath.read_text()) if auditpath.exists() else {}
    end=a.get('metadata',{}).get('last_time_s',0)
    rows.append({'threads':threads,'case':case,'returncode':p.returncode,'last_time_ns':end*1e9,'solver_seconds':run['elapsed_seconds'] if run else None,'completed_interval':end>=180e-9-1e-16,'audit_checks':a.get('checks',{})})
    (R/'results/thread_benchmark64.json').write_text(json.dumps({'runs':rows,'status':'in_progress'},indent=2))
    if p.returncode:raise RuntimeError('Benchmark failed; inspect '+case)
eligible=[x for x in rows if x['completed_interval'] and x['returncode']==0]
if not eligible:raise RuntimeError('No complete equal-interval benchmark')
winner=min(eligible,key=lambda x:x['solver_seconds'])
c=json.loads((R/'session_64.json').read_text());c['solver_threads']=winner['threads'];(R/'session_64.json').write_text(json.dumps(c,indent=2))
report={'runs':rows,'selected_threads':winner['threads'],'elapsed_total_seconds':time.time()-start,'status':'complete','criterion':'Minimum solver wall time for the same 0-180 ns initial-value interval; includes batch startup and official saving.'}
(R/'results/thread_benchmark64.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
