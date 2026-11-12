"""One COMSOL solve at a time, bounded budget, official save-stop only."""
import argparse, datetime, json, os, re, subprocess, sys, time
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];WORK=ROOT/'work';LOG=ROOT/'logs'
sys.path.insert(0,str(ROOT.parent/'server_tools'))
from server_runtime import batch,batch_command,running_solvers
from session_runtime import config,solver_seconds_left,segment_seconds_left
from atomic_json64 import write_json
# Retain the historical Windows recovery helper's imported path name.
BATCH=batch()
def write(path,obj):
 write_json(path,obj)
def state(**kw):
 p=ROOT/'status.json';d=json.loads(p.read_text(encoding='utf8')) if p.exists() else {};d.update(kw,updated_beijing=datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat());write(p,d)
def main(case,threads=8):
 if (ROOT/'PAUSED_BY_USER.json').exists():raise RuntimeError('Project paused by user. Use server_tools/server_setup.py unpause explicitly before solving.')
 lock=ROOT/'worker.lock';fd=os.open(str(lock),os.O_CREAT|os.O_EXCL|os.O_WRONLY);os.write(fd,str(os.getpid()).encode());os.close(fd)
 p=None;recorded=False;stopped=False;stop_reason=None;start=None;b=None;used=0
 try:
  state(state='waiting_for_single_solver',worker_pid=os.getpid(),case=case,solver_pid=None,error=None,audit=None,elapsed_seconds=0,last_accepted_time_s=0,solve_exit_code=None,stop_reason=None,numerical_status=None,finished_epoch=None,phase='campaign64',output=None,log=None,paused_beijing=None,pause_reason=None)
  while running_solvers():
   if solver_seconds_left()<120:raise RuntimeError('Session solver deadline reached while waiting')
   time.sleep(10)
  budget=ROOT/'solver_budget.json';b=json.loads(budget.read_text(encoding='utf-8-sig'))
  used=sum(r['elapsed_seconds'] for r in b['runs'])
  if used>=b['budget_seconds'] or solver_seconds_left()<120:raise RuntimeError('Session solver budget/deadline exhausted')
  ready=WORK/(case+'_ready.mph');out=WORK/(case+'.mph');log=LOG/(case+'.log')
  if out.exists():raise RuntimeError('Output exists; do not overwrite '+out.name)
  start=time.time()
  with (LOG/(case+'_stdout.txt')).open('wb') as fo,(LOG/(case+'_stderr.txt')).open('wb') as fe:
   p=subprocess.Popen(batch_command()+['-inputfile',str(ready),'-outputfile',str(out),'-study','std','-np',str(threads),'-batchlog',str(log)],cwd=WORK,stdout=fo,stderr=fe)
   state(state='solving',solver_pid=p.pid,started_epoch=start,output=str(out),log=str(log))
   stopped=False;stop_reason=None
   while p.poll() is None:
    elapsed=time.time()-start
    content=log.read_text(encoding='utf8',errors='replace') if log.exists() else ''
    rows=re.findall(r'^\s*\d+\s+([\d.eE+-]+)\s+[\d.eE+-]+\s+\d+\s+\d+',content,re.M)
    state(state='official_save_stop' if stopped else 'solving',elapsed_seconds=elapsed,last_accepted_time_s=float(rows[-1]) if rows else 0)
    write(budget,dict(b,active_case=case,active_elapsed_seconds=elapsed,total_seconds=used+elapsed))
    request=ROOT/'STOP_REQUEST.json';manual={}
    if request.exists():
     try:request_text=request.read_text(encoding='utf-8-sig')
     except UnicodeDecodeError:
      # Windows editors / Python locale defaults may have written GBK.
      # Normalize the actual request instead of abandoning a running solver.
      request_text=request.read_text(encoding='gb18030')
      request.write_text(request_text,encoding='utf8')
     manual=json.loads(request_text)
    # Reserve time for the official save-stop and worker polling so neither
    # the per-case nor cumulative wall-time ledger overruns its hard limit.
    if (segment_seconds_left(elapsed,used,b)<=config().get('save_margin_seconds',90) or manual.get('case')==case) and not stopped:
     stop_reason=manual.get('reason') if manual.get('case')==case else 'solve_time_budget'
     state(state='official_save_stop',stop_reason=stop_reason);subprocess.run(batch_command()+['-stop','100','-inputfile',str(out)],cwd=WORK,timeout=120,check=False);stopped=True
     if manual.get('case')==case:request.unlink(missing_ok=True)
    time.sleep(2)
  elapsed=time.time()-start;b['runs'].append({'case':case,'elapsed_seconds':elapsed,'threads':threads,'started_epoch':start,'finished_epoch':time.time(),'exit_code':p.returncode,'official_stop_requested':stopped,'stop_reason':stop_reason,'comsol_version':'6.4.0.429'});b.update(total_seconds=used+elapsed,active_case=None,active_elapsed_seconds=0);write(budget,b);recorded=True
  if not out.exists():raise RuntimeError('No normally saved output; read solver logs')
  state(state='exporting',solver_pid=None,solve_exit_code=p.returncode)
  if (WORK/(case+'_resume.json')).exists():
   subprocess.run([sys.executable,str(WORK/'run_standalone.py'),'MergeHistory',str(out)],cwd=WORK,check=True)
  subprocess.run([sys.executable,str(WORK/'run_standalone.py'),'ExportS4',str(out),str(ROOT/'results'/case)],cwd=WORK,check=True)
  subprocess.run([sys.executable,str(WORK/'audit_s4.py'),case],cwd=WORK,check=True)
  finalaudit=json.loads((ROOT/'results'/(case+'_audit.json')).read_text())
  state(state='audit_ready',solver_pid=None,worker_pid=None,audit=str(ROOT/'results'/(case+'_audit.json')),elapsed_seconds=elapsed,last_accepted_time_s=finalaudit['metadata']['last_time_s'],numerical_status=finalaudit['status'],finished_epoch=time.time())
 except Exception as e:
  recovery_error=None
  if p is not None and p.poll() is None:
   stop_reason='worker_exception_save_stop';stopped=True
   try:
    subprocess.run(batch_command()+['-stop','100','-inputfile',str(out)],cwd=WORK,timeout=120,check=False)
    p.wait(timeout=180)
   except Exception as recovery:recovery_error=str(recovery)
  live=p is not None and p.poll() is None
  if p is not None and not live and not recorded and b is not None:
   elapsed=time.time()-start
   b['runs'].append({'case':case,'elapsed_seconds':elapsed,'threads':threads,'started_epoch':start,'finished_epoch':time.time(),'exit_code':p.returncode,'official_stop_requested':stopped,'stop_reason':stop_reason,'comsol_version':'6.4.0.429','worker_error':str(e)})
   b.update(total_seconds=used+elapsed,active_case=None,active_elapsed_seconds=0);write(budget,b);recorded=True
  state(state='needs_review_solver_alive' if live else 'needs_review',error=str(e),recovery_error=recovery_error,solver_pid=p.pid if live else None,worker_pid=None)
  raise
 finally:
  # A live native solver retains its lock and PID until explicitly recovered.
  if p is None or p.poll() is not None:lock.unlink(missing_ok=True)
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('case');p.add_argument('--threads',type=int,default=8);a=p.parse_args();main(a.case,a.threads)
