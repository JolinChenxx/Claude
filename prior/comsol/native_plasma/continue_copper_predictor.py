from pathlib import Path
import os,sys,subprocess,json
R=Path(__file__).resolve().parent;source,case=sys.argv[1:3]
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case)
d=json.loads((R/'results'/f'{source}_copper_summary.json').read_text(encoding='utf-8'));start=d['saved_end_ns']
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
builder='ContinueCopperNewton' if len(sys.argv)>3 and sys.argv[3]=='newton' else 'ContinueSaved'
extra=['minimal'] if len(sys.argv)>3 and sys.argv[3]=='minimal' else []
py('run.py','java',builder,str(R/'models'/f'{source}.mph'),str(R/'models'/f'{case}_ready.mph'),f'{start}[ns]',f'range({start}[ns],1[ns],203[ns])',*extra)
py('run.py','solve',case)
py('run.py','java','ExportMelt',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_surface.csv'))
py('plot_partitioned_copper.py',case)
print('COPPER CONTINUATION SAVED',case,flush=True)
