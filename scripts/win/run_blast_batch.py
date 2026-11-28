"""T009 durable batch: serial benchmarks, bounded scan, fine grid, postprocess.

Run via scripts/win/job.ps1. No LLM calls, Git writes, or unrelated process control.
Frozen copies of this script/postprocessor/sampler and binary live in the raw run.
"""
from pathlib import Path
import argparse, concurrent.futures, csv, hashlib, json, math, os, re
import shutil, subprocess, sys, threading, time, traceback
from datetime import datetime, timezone, timedelta

ENERGIES=[20,30,45,65,90,130,180,250,320]
LOCK=threading.Lock()

def now():
    return datetime.now(timezone(timedelta(hours=8))).isoformat()

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def save(path,value):
    path=Path(path); temp=path.with_suffix(path.suffix+'.tmp')
    temp.write_text(json.dumps(value,indent=2,allow_nan=False),encoding='utf-8');temp.replace(path)

class Batch:
    def __init__(self,repo,raw):
        self.repo=repo;self.raw=raw;self.out=repo/'results/T009'
        self.out.mkdir(parents=True,exist_ok=True);raw.mkdir(parents=True,exist_ok=True)
        self.env=os.environ.copy()
        prov=json.loads((repo/'results/T009/build_provenance.json').read_text())
        self.mpi=Path(prov['mpi_executable'])
        self.env['PATH']=str(Path(prov['compiler']).parent)+os.pathsep+str(self.mpi.parent)+os.pathsep+self.env['PATH']
        for key in ['OMP_NUM_THREADS','OPENBLAS_NUM_THREADS','MKL_NUM_THREADS']: self.env[key]='1'
        self.exe=raw/'bin/pluto.exe'; self.base=(raw/'code/pluto.ini').read_text()
        assert sha(self.exe)==prov['executable_sha256'],'Executable changed'
        self.state=dict(started=now(),phase='starting',running={},completed=[],failures=[],
                        executable_sha256=sha(self.exe),raw_dir=str(raw))
        self.state_path=raw/'batch_state.json'
        self.update()

    def update(self,phase=None):
        with LOCK:
            if phase:self.state['phase']=phase
            self.state['updated']=now();save(self.state_path,self.state)

    def sample(self,name):
        file=self.raw/(name+'.json')
        p=subprocess.run(['powershell.exe','-NoProfile','-File',str(self.raw/'code/sample_load.ps1'),'-OutFile',str(file)],
              cwd=self.repo,env=self.env,stdout=subprocess.PIPE,stderr=subprocess.PIPE,timeout=30)
        if p.returncode:raise RuntimeError(p.stderr.decode(errors='replace'))
        return json.loads(file.read_text(encoding='utf-8-sig'))

    def run(self,name,n,energy,tstop,ranks,radial=False,maxsteps=None):
        run=self.raw/name;run.mkdir(parents=True,exist_ok=True)
        recipe=dict(name=name,n=n,energy_uJ=energy,tstop=tstop,ranks=ranks,
                    partition='radial' if radial else 'default',maxsteps=maxsteps,executable_sha256=sha(self.exe))
        result_file=run/'result.json'
        if result_file.exists():
            previous=json.loads(result_file.read_text())
            if previous.get('recipe')!=recipe:raise RuntimeError(f'Recipe differs; preserve {run}')
            if not previous.get('successful'):raise RuntimeError(f'Previous failed case preserved: {run}')
            return previous
        if (run/'flt.out').exists():raise RuntimeError(f'Interrupted data preserved: {run}; needs explicit recovery')
        ini=self.base
        for key,value in [('X1-grid',f'1 0.0 {n} u 1200.0'),('X2-grid',f'1 0.0 {n} u 1200.0'),('E_UJ',str(energy)),('tstop',str(tstop))]:
            ini,count=re.subn(r'(?m)^'+re.escape(key)+r'\s+[^\n]*',key+' '+value,ini)
            assert count==1,key
        (run/'pluto.ini').write_text(ini);save(run/'recipe.json',recipe)
        cmd=[str(self.mpi),'-n',str(ranks),'-wdir',str(run),str(self.exe)]
        if radial:cmd+=['-dec',str(ranks),'1']
        if maxsteps is not None:cmd+=['-maxsteps',str(maxsteps)]
        started=now();clock=time.perf_counter()
        with (run/'run.log').open('wb') as log:
            proc=subprocess.Popen(cmd,cwd=run,env=self.env,stdout=log,stderr=subprocess.STDOUT)
            with LOCK:self.state['running'][name]=dict(pid=proc.pid,started=started,recipe=recipe)
            self.update();print(f'{started} START {name} pid={proc.pid} ranks={ranks}',flush=True)
            code=proc.wait()
        elapsed=time.perf_counter()-clock
        result=dict(recipe=recipe,command=cmd,started=started,finished=now(),wall_s=elapsed,exit_code=code,successful=False)
        try:
            with (run/'budget.csv').open() as f:bud=list(csv.DictReader(f))
            result['steps']=int(bud[-1]['step']);result['final_time_ns']=float(bud[-1]['t_ns'])
            result['initial_relative_error']=abs(float(bud[0]['excess_uJ'])/energy-1)
            result['cell_steps_per_second_per_rank']=n*n*result['steps']/elapsed/ranks
            result['successful']=(code==0 and result['initial_relative_error']<.001 and
                                  (maxsteps is not None or result['final_time_ns']>=tstop-.01))
        except Exception as exc:result['error']=repr(exc)
        save(result_file,result)
        with LOCK:
            self.state['running'].pop(name,None)
            self.state['completed'].append(name)
            if not result['successful']:self.state['failures'].append(dict(name=name,result=str(result_file)))
        self.update();print(f'{now()} END {name} exit={code} seconds={elapsed:.1f} ok={result["successful"]}',flush=True)
        return result

    def execute(self,smoke=False):
        if smoke:
            self.update('smoke')
            sys.path.insert(0,str(self.raw/'code'))
            from blast_post import analyze_case
            checks={}
            for name,ranks,radial in [('smoke',8,True),('smoke_default7',7,False)]:
                r=self.run(name,480,90,100,ranks,radial,maxsteps=1)
                if not r['successful']:return 1
                frames,checks[name]=analyze_case(self.raw/name)
                assert checks[name]['energy_checks_passed'],checks[name]
            save(self.out/'smoke_checks.json',checks)
            self.update('smoke_finished')
            return 0
        load=self.sample('load_before_benchmarks');save(self.out/'load_initial.json',load)
        self.update('benchmarks_serial')
        rows=[]
        cases=[(n,False) for n in [1,8,16,32,64]]+[(n,True) for n in [8,16,32]]
        for ranks,radial in cases:
            name=f'benchmark/{"radial" if radial else "default"}_n{ranks:02d}'
            result=self.run(name,480,90,100,ranks,radial)
            rows.append(dict(ranks=ranks,partition='radial' if radial else 'default',grid_n=480,
                energy_uJ=90,tstop_ns=100,wall_s=result['wall_s'],steps=result.get('steps'),
                cell_steps_per_second_per_rank=result.get('cell_steps_per_second_per_rank'),
                exit_code=result['exit_code'],successful=result['successful'],
                unrelated_load_initial_cores=load['sampled_cpu_core_equivalents'],raw_dir=str(self.raw/name)))
            with (self.out/'benchmark.csv').open('w',newline='') as f:
                w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
            if not result['successful']:raise RuntimeError(f'Benchmark failed: {name}; physics/parameters unchanged')
        sys.path.insert(0,str(self.raw/'code'))
        from blast_post import density_consistency
        consistency=density_consistency(self.raw/'benchmark/default_n08',self.raw/'benchmark/radial_n08')
        save(self.out/'partition_consistency.json',consistency)
        load=self.sample('load_before_scan')
        # Reserve one physical core plus measured unrelated CPU activity.
        slots=max(1,min(9,(64-1-math.ceil(load['sampled_cpu_core_equivalents']))//7))
        self.state['scan_concurrency']=slots;self.state['scan_ranks_per_case']=7
        self.state['scan_load_snapshot']=load;self.update('energy_scan')
        print(f'{now()} SCAN concurrency={slots}; unrelated measured cores={load["sampled_cpu_core_equivalents"]:.2f}',flush=True)
        failures=[]
        with concurrent.futures.ThreadPoolExecutor(max_workers=slots) as pool:
            futures={pool.submit(self.run,f'scan/E{e:03d}',480,e,900,7):e for e in ENERGIES}
            for future in concurrent.futures.as_completed(futures):
                r=future.result()
                if not r['successful']:failures.append(futures[future])
        if failures:raise RuntimeError(f'Failed scan energies: {failures}; no tuning or automatic retries')
        self.update('grid_convergence')
        for n,ranks in [(240,7),(960,32)]:
            r=self.run(f'convergence/G{n}',n,90,900,ranks)
            if not r['successful']:raise RuntimeError(f'Convergence run failed: G{n}')
        save(self.out/'convergence_reuse.json',dict(G480='scan/E090',reason='Identical E90, 480x480, 900 ns run is shared, not rerun.'))
        self.update('postprocessing')
        command=[sys.executable,str(self.raw/'code/blast_post.py'),'--raw',str(self.raw),'--out',str(self.out),
                 '--experiment',str(self.raw/'code/fig3d_shock.csv')]
        with (self.out/'postprocess.log').open('wb') as log:
            p=subprocess.run(command,cwd=self.repo,env=self.env,stdout=log,stderr=subprocess.STDOUT)
        self.state['postprocess_exit']=p.returncode
        self.update('completed_pending_review' if p.returncode==0 else 'needs_review')
        save(self.out/'batch_state_final.json',self.state)
        # Gather small diagnostics; raw field data stay under D:/sim_runs.
        for path in self.raw.rglob('result.json'):
            dest=self.out/'runs'/path.parent.relative_to(self.raw);dest.mkdir(parents=True,exist_ok=True)
            for name in ['result.json','recipe.json','initialization.csv','budget.csv','run.log','pluto.0.log']:
                source=path.parent/name
                if source.exists() and source.stat().st_size<1_000_000:shutil.copy2(source,dest/name)
        print(f'{now()} FINISHED phase={self.state["phase"]}; review {self.out}',flush=True)
        return p.returncode

def main():
    p=argparse.ArgumentParser();p.add_argument('--repo',type=Path,required=True);p.add_argument('--raw',type=Path,default=Path('D:/sim_runs/T009'));p.add_argument('--smoke',action='store_true')
    args=p.parse_args();batch=Batch(args.repo,args.raw)
    try:code=batch.execute(args.smoke)
    except Exception as exc:
        batch.state['exception']=repr(exc);batch.state['traceback']=traceback.format_exc();batch.update('failed_needs_review')
        save(batch.out/'batch_failure.json',batch.state);traceback.print_exc();code=1
    raise SystemExit(code)

if __name__=='__main__':main()
