import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
import subprocess,argparse,time
sys.path.insert(0,str(SOURCE.parent/'server_tools'))
import server_runtime as runtime
runtime.PROJECT=ROOT

def java(name,args,log):
    with Path(log).open('w',encoding='utf-8') as f:
        cmd=runtime.java_command(name,args);cmd.insert(1,'-XX:ActiveProcessorCount=8')
        result=subprocess.Popen(cmd,cwd=ROOT/'work',env=runtime.environment(),stdout=f,stderr=subprocess.STDOUT)
        from affinity import restrict
        restrict(result.pid);result.wait()
    print(Path(log).read_text(encoding='utf-8',errors='replace')[-3500:],flush=True)
    if result.returncode:raise RuntimeError(f'{name} failed: {result.returncode}')

def build(case,twoT,quench,end,coupled=False,conduction='1'):
    if coupled and str(twoT)!='1':raise ValueError('The coupled inverse-energy closure evolves electron energy and requires --twoT 1')
    java('R2BuildCoupled' if coupled else 'R2BuildKinetics',[SOURCE/'work/B64A.mph',ROOT/'models'/f'{case}_ready.mph',ROOT/'data/rates_K.txt',twoT,quench,
        f'range(120[ns],2[ns],{end}[ns])',conduction],ROOT/'logs'/f'{case}_build.log')

def solve(case,threads=8):
    command="Get-CimInstance Win32_Process -Filter \"Name='comsolbatch.exe'\" | Select-Object ProcessId,CommandLine | ConvertTo-Json -Compress"
    raw=subprocess.check_output(['powershell','-NoProfile','-Command',command],text=True).strip()
    processes=json.loads(raw) if raw else []
    if isinstance(processes,dict):processes=[processes]
    for process in processes:
        cmdline=process.get('CommandLine') or ''
        if str(ROOT/'models').lower() in cmdline.lower() and '-stop' not in cmdline:
            raise RuntimeError(f"Route 2 solver already active: PID {process['ProcessId']}; finish it before starting {case}")
    ready=ROOT/'models'/f'{case}_ready.mph';out=ROOT/'models'/f'{case}.mph';log=ROOT/'logs'/f'{case}_batch.log'
    if out.exists():raise FileExistsError(out)
    session=json.loads((ROOT/'session.json').read_text())
    from datetime import datetime,timezone
    deadline=datetime.fromisoformat(session['deadline_utc']).timestamp()
    cmd=runtime.batch_command()+['-inputfile',str(ready),'-outputfile',str(out),'-study','r2study','-np',str(threads),'-batchlog',str(log)]
    with (ROOT/'logs'/f'{case}_stdout.log').open('wb') as f:
        proc=subprocess.Popen(cmd,cwd=ROOT/'models',env=runtime.environment(),stdout=f,stderr=subprocess.STDOUT)
        from affinity import restrict
        restrict(proc.pid)
        write_json(ROOT/'active_solver.json',dict(case=case,pid=proc.pid,command=cmd,started_utc=datetime.now(timezone.utc).isoformat()))
        stopped=False
        while proc.poll() is None:
            if not stopped and (time.time()>deadline-180 or (ROOT/'STOP_REQUEST.json').exists()):
                subprocess.run(runtime.batch_command()+['-stop','100','-inputfile',str(out)],cwd=ROOT/'models',timeout=120)
                stopped=True
            time.sleep(5)
    log_text=log.read_text(encoding='utf-8',errors='replace') if log.exists() else ''
    failed=('以下特征遇到问题' in log_text or '/*****错误' in log_text or 'The following feature has encountered a problem' in log_text)
    write_json(ROOT/'active_solver.json',dict(case=case,pid=proc.pid,returncode=proc.returncode,status='failed' if failed else ('saved_stop' if stopped else 'finished')))
    print('SOLVER EXIT',proc.returncode,flush=True)
    if proc.returncode or failed:print(log_text[-4000:],flush=True);raise SystemExit(proc.returncode or 1)

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('action',choices=['build','solve','export']);p.add_argument('case');p.add_argument('--twoT',default='0');p.add_argument('--quench',default='0');p.add_argument('--end',default='900');p.add_argument('--threads',type=int,default=8);p.add_argument('--coupled',action='store_true');p.add_argument('--conduction',default='1');a=p.parse_args()
    if a.action=='build':build(a.case,a.twoT,a.quench,a.end,a.coupled,a.conduction)
    elif a.action=='solve':solve(a.case,a.threads)
    else:java('R2Export',[ROOT/'models'/f'{a.case}.mph',RES/a.case],ROOT/'logs'/f'{a.case}_export.log')
