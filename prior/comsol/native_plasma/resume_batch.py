"""Resume a stopped native COMSOL solver, preserving its continuation metadata.

Writes a new case; does not reset initial values to interpolated output fields.
"""
from pathlib import Path
import sys,os,subprocess,json,datetime
sys.path.insert(0,str(Path(__file__).resolve().parent))
from run import COMSOL,ROOT,env as comsol_env
prev,case,numa=sys.argv[1:4]
out=ROOT/'models'/f'{case}.mph'
if out.exists():raise FileExistsError(out)
os.environ['ROUTE1_THREADS']='32'
cmd=[str(COMSOL/'bin/win64/comsolbatch.exe'),'-np','32','-inputfile',str(ROOT/'models'/f'{prev}.mph'),'-outputfile',str(out),'-batchlog',str(ROOT/'logs'/f'{case}_solver.log'),'-continue','-numasets','1','-numafirst',numa,'-mpmode','throughput']
with (ROOT/'logs'/f'{case}_resume.log').open('wb') as f:
    p=subprocess.Popen(cmd,cwd=ROOT/'src',env=comsol_env(),stdout=f,stderr=subprocess.STDOUT)
    (ROOT/'logs'/f'{case}_resume_process.json').write_text(json.dumps({'pid':p.pid,'started_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'command':cmd},indent=2))
    code=p.wait()
print('NATIVE_BATCH_RESUME_EXIT',case,code,flush=True);sys.exit(code)
