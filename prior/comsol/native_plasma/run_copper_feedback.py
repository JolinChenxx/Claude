from pathlib import Path
import json, os, subprocess, sys
import numpy as np
R=Path(__file__).resolve().parent
case,source=sys.argv[1:3]
iteration=int(sys.argv[3]) if len(sys.argv)>3 else 2
table=R/'data'/f'{source}_gas_feedback.txt'
d=np.loadtxt(table)
if not np.isfinite(d).all() or d[:,1].min()>1e-9:
    raise ValueError('Feedback must be finite and start at initial time')
end=float(d[:,1].max())
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
py('run.py','compile','ApplyCopperFeedback')
py('run.py','java','ApplyCopperFeedback',str(R/'models/C201_ready.mph'),str(R/'models'/f'{case}_ready.mph'),str(table),f'{end:g}[ns]','states' if d.shape[1]==11 else 'ground')
(R/'results'/f'{case}_coupling_iteration.json').write_text(json.dumps(dict(case=case,feedback_source=source,requested_end_ns=end,baseline='C201',iteration=iteration,two_way_converged=False),indent=2),encoding='utf-8')
py('solve_and_post_copper.py',case)
