"""Collect the completed v1h check without changing numerical settings."""
from pathlib import Path
import datetime,hashlib,json,re,shutil
import psutil
repo=Path(__file__).resolve().parents[2];run=Path('D:/sim_runs/T016f/check')
out=repo/'results/T016f/check/final';out.mkdir(exist_ok=True)
archive=Path('D:/sim_runs/T016f/archive_check');archive.mkdir(exist_ok=True)
record=json.loads((run/'diagnostic_run.json').read_text());assert record['state'] in ('finished','time_limit','failed')
for p in psutil.process_iter(['pid','name']):
    if p.info['name'] in ('mpiexec.exe','pluto.exe'):assert Path(p.cwd())!=run
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
logs=sorted(run.glob('pluto.*.log'));assert len(logs)==8
items=[]
for p in logs:
    raw=p.read_bytes();target=archive/p.name
    if not target.exists():shutil.copy2(p,target)
    assert target.read_bytes()==raw
    lines=raw.decode(errors='replace').splitlines();inds=[i for i,s in enumerate(lines) if s.startswith('step:')];pairs=[]
    for i in inds[-20:]:pairs.extend([lines[i]]+[s for s in lines[i+1:i+6] if 'Nrkl' in s])
    stepdata=[dict(step=int(m[1]),t_ns=float(m[2]),dt_ns=float(m[3])) for s in lines if (m:=re.match(r'step:(\d+); t = ([^;]+); dt = ([^;]+);',s))]
    for label,ss in [('step_nrkl',pairs),('warnings',[s for s in lines if '!' in s and 'ConsToPrim' not in s])]:
        (out/(p.stem+'.'+label+'.log')).write_text('\n'.join(ss)+'\n')
    items.append(dict(rank=p.stem,bytes=len(raw),sha256=sha(p),archive=str(target),steps=stepdata,
      ConsToPrim=sum('ConsToPrim' in s for s in lines),dt_limit=sum('! lip dt-limit' in s for s in lines),vmax=sum('! lip vmax' in s for s in lines)))
for name in ('diagnostic_run.json','mpi_output.log','dbl.out','restart.out','grid.out','pluto.ini'):
    shutil.copy2(run/name,archive/name);shutil.copy2(run/name,out/name)
pre=json.loads((run/'restart_preflight.json').read_text());checks={n:sha(run/n)==h for n,h in pre['hashes'].items()}
if not checks['grid.out']:
    old=(Path(pre['source'])/'grid.out').read_bytes();new=(run/'grid.out').read_bytes()
    norm=lambda b:b'\n'.join(s for s in b.splitlines() if not s.startswith(b'# Generated on'))
    checks['grid_content_excluding_generation_comment']=norm(old)==norm(new)
summary=dict(collected=datetime.datetime.now().astimezone().isoformat(),run=record,ranks=items,input_checks=checks,budget_exists=(run/'budget.csv').exists(),last_dbl_line=(run/'dbl.out').read_text().splitlines()[-1])
steps=items[0]['steps'];summary['last_logged']=steps[-1];summary['max_logged_dt_ns']=max(s['dt_ns'] for s in steps)
summary['max_logged_t_ns']=max(s['t_ns'] for s in steps);summary['acceptance_pass']=summary['max_logged_dt_ns']>=1e-3 and summary['max_logged_t_ns']>85
if (run/'budget.csv').exists():
    shutil.copy2(run/'budget.csv',archive/'budget.csv');(out/'budget_tail.csv').write_text('\n'.join((run/'budget.csv').read_text().splitlines()[-20:])+'\n')
else:(out/'budget_tail.txt').write_text('No budget.csv was created during the bounded check.\n')
(out/'summary.json').write_text(json.dumps(summary,indent=2)+'\n')
print(json.dumps({k:v for k,v in summary.items() if k!='ranks'},indent=2));print('ConsToPrim',sum(x['ConsToPrim'] for x in items))
