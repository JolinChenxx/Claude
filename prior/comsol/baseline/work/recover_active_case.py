"""Adopt an existing solver after its Python worker failed; official save-stop.

Does not launch a solve, does not reset its start time, and appends its budget
entry once. Used for the VGE stop-request encoding failure.
"""
import ctypes,json,os,subprocess,sys,time
from pathlib import Path
from campaign import ROOT,WORK,BATCH,write,state

def main(case):
 status=json.loads((ROOT/'status.json').read_text(encoding='utf8'))
 if status['case']!=case:raise RuntimeError('Case identity mismatch')
 pid=int(sys.argv[2]);start=status['started_epoch'];out=Path(status['output'])
 kernel=ctypes.windll.kernel32
 kernel.OpenProcess.restype=ctypes.c_void_p
 handle=kernel.OpenProcess(0x00100000|0x1000,False,pid)
 if not handle:raise RuntimeError('Cannot attach actual solver process')
 lock=ROOT/'worker.lock';fd=os.open(str(lock),os.O_CREAT|os.O_EXCL|os.O_WRONLY);os.write(fd,str(os.getpid()).encode());os.close(fd)
 reason='early_Newton_checkpoint: refined mesh nonlinear stall near 258ns; official save-stop recovery after stop-request encoding caused the Python worker to exit. Actual solver elapsed time fully retained. Switch current Jacobian/automatic damping after audit.'
 try:
  request=ROOT/'STOP_REQUEST.json'
  if request.exists():request.replace(ROOT/'diagnostics/VGE_stop_request_wrong_encoding.json')
  state(state='official_save_stop',worker_pid=os.getpid(),solver_pid=pid,error=None,stop_reason=reason)
  subprocess.run([str(BATCH),'-stop','100','-inputfile',str(out)],cwd=WORK,check=False)
  while kernel.WaitForSingleObject(ctypes.c_void_p(handle),10000)==258:
   budget=json.loads((ROOT/'solver_budget.json').read_text());used=sum(r['elapsed_seconds'] for r in budget['runs'])
   write(ROOT/'solver_budget.json',dict(budget,active_case=case,active_elapsed_seconds=time.time()-start,total_seconds=used+time.time()-start))
  elapsed=time.time()-start;code=ctypes.c_ulong();kernel.GetExitCodeProcess(ctypes.c_void_p(handle),ctypes.byref(code));kernel.CloseHandle(ctypes.c_void_p(handle))
  budget=json.loads((ROOT/'solver_budget.json').read_text())
  if any(r['case']==case for r in budget['runs']):raise RuntimeError('Budget entry already exists')
  budget['runs'].append(dict(case=case,elapsed_seconds=elapsed,threads=8,started_epoch=start,finished_epoch=time.time(),exit_code=code.value,official_stop_requested=True,stop_reason=reason,recovered_worker_encoding_failure=True))
  budget.update(total_seconds=sum(r['elapsed_seconds'] for r in budget['runs']),active_case=None,active_elapsed_seconds=0);write(ROOT/'solver_budget.json',budget)
  state(state='exporting',solver_pid=None,elapsed_seconds=elapsed,solve_exit_code=code.value)
  for klass,args in [('MergeHistory',[str(out)]),('ExportS4',[str(out),str(ROOT/'results'/case)])]:
   subprocess.run([sys.executable,str(WORK/'run_standalone.py'),klass,*args],cwd=WORK,check=True)
  subprocess.run([sys.executable,str(WORK/'audit_s4.py'),case],cwd=WORK,check=True)
  audit=json.loads((ROOT/f'results/{case}_audit.json').read_text())
  state(state='audit_ready',worker_pid=None,solver_pid=None,error=None,audit=str(ROOT/f'results/{case}_audit.json'),last_accepted_time_s=audit['metadata']['last_time_s'],numerical_status=audit['status'])
 finally:lock.unlink(missing_ok=True)
if __name__=='__main__':main(sys.argv[1])
