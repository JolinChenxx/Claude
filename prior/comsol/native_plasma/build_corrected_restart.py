"""Reproduce a physically corrected route-1 continuation from a saved case."""
from pathlib import Path
import subprocess,sys,os
r=Path(__file__).resolve().parent
source,case,start=sys.argv[1:]
env=os.environ.copy();env['ROUTE1_THREADS']='16'
steps=[('ConservativeHeavy',[start]),('MatchedALEBoundary',[]),('ElasticMeshCase',[]),('QuadraticSurface',[]),('FullNewtonCase',[])]
current=r/'models'/f'{source}.mph'
for i,(cls,args) in enumerate(steps):
    output=r/'models'/f'{case}_{i+1}.mph' if i<len(steps)-1 else r/'models'/f'{case}_ready.mph'
    subprocess.run([sys.executable,'-X','utf8',str(r/'run.py'),'java',cls,str(current),str(output),*args],env=env,check=True)
    current=output
print('CORRECTED CONTINUATION READY',current,flush=True)
