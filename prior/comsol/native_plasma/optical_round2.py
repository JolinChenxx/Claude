"""Optical postprocessing launched only after the native field export completes."""
from pathlib import Path
import os,sys,subprocess,json
R=Path(__file__).resolve().parent;case=sys.argv[1]
env=os.environ.copy();env.update(ROUTE1_POST_THREADS='16',ROUTE1_RADIATION_MIN_NS='113',ROUTE1_RADIATION_EXPOSURES_ONLY='1',ROUTE1_RADIATION_MIN_SPACING_NS='0.5')
def run(name,*args):subprocess.run([sys.executable,'-X','utf8',str(R/name),*args],env=env,check=True)
run('plot_native.py',case,'250')
d=json.loads((R/'results'/f'{case}_fields.json').read_text());t=d['times_s']
if t[-1]<113e-9-1e-16:
    print('FIELDS_ONLY_BEFORE_FIRST_EXPOSURE',case,flush=True);sys.exit(0)
run('radiation_native.py',case)
if t[0]<=113e-9+1e-16 and t[-1]>=123e-9-1e-16:run('preview_native.py',case)
print('OPTICAL_STAGE_READY',case,flush=True)
