from pathlib import Path
import subprocess,sys
r=Path(__file__).resolve().parent
sys.path.insert(0,str(r.parents[1]/'server_tools'))
from server_runtime import java_command,environment
if len(sys.argv)<2:raise SystemExit('Usage: run_standalone.py JavaClass [absolute file arguments ...]')
case_hint=Path(sys.argv[2]).stem if len(sys.argv)>2 else 'no_input'
out=r.parent/'logs'/(sys.argv[1]+'_'+case_hint+'_standalone.txt')
with out.open('wb') as f:
    result=subprocess.run(java_command(sys.argv[1],sys.argv[2:]),cwd=r,env=environment(),stdout=f,stderr=subprocess.STDOUT)
print('exit=',result.returncode,'log=',out)
print(out.read_text(encoding='utf8',errors='replace')[-1500:].encode('ascii','replace').decode())
sys.exit(result.returncode)
