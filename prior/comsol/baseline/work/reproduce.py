"""Build/solve/postprocess one explicitly named independent LTE/S4 case."""
from pathlib import Path
import argparse,json,subprocess,sys,re,math
ROOT=Path(__file__).resolve().parents[1];WORK=ROOT/'work'
def call(*args):subprocess.run([sys.executable,*map(str,args)],cwd=WORK,check=True)
def build(case,mesh,step):
    if (WORK/f'{case}_ready.mph').exists() or (WORK/f'{case}.mph').exists():raise RuntimeError('Do not overwrite an existing model: '+case)
    # 6.4 uses the audited final-physics initial seed; the 6.2 builder's
    # interpolation-import sequence is retained only as historical source.
    match=re.fullmatch(r'([0-9.eE+-]+)(?:\[ns\])?',str(step))
    if not match:raise ValueError('Specify a positive numeric step in ns, optionally with [ns]')
    dt=float(match.group(1));mf=float(mesh)
    if not all(math.isfinite(v) and v>0 for v in [dt,mf]):raise ValueError('Mesh and step must be positive finite values')
    call(WORK/'run_standalone.py','Prepare64',WORK/'VTE_ready.mph',WORK/f'{case}_ready.mph',str(mf),str(dt))

def resume(source,new,jacobian,damping,damp):
    output=WORK/f'{new}_ready.mph'
    if output.exists() or (WORK/f'{new}.mph').exists():raise RuntimeError('Do not overwrite an existing case: '+new)
    audit=json.loads((ROOT/f'results/{source}_audit.json').read_text())
    failed=[k for k,v in audit['checks'].items() if k!='complete' and not v]
    if failed:raise RuntimeError('Review failed physical/numerical checks before continuing: '+','.join(failed))
    command=[WORK/'run_standalone.py','ResumeS4',WORK/f'{source}.mph',output]
    if jacobian!='preserve' or damping!='preserve':
        if jacobian=='preserve' or damping=='preserve':raise ValueError('Specify both --jacobian and --damping, or preserve both.')
        command += [jacobian,damping]
        if damping=='const':command += ['preserve',str(damp)]
    call(*command)
    change=f'Newton strategy: jacobian={jacobian}, damping={damping}, constant factor={damp if damping=="const" else "unused"}. Equations, mesh, tolerance and time-step limit unchanged.'
    (WORK/f'{new}_resume.json').write_text(json.dumps({'history_source':source+'.mph','same_equations_and_mesh':True,'source_saved_end_s':audit['metadata']['last_time_s'],'continuation_change':change,'merge':'Official CombineSolution; export removes duplicate time labels only.'},indent=2))
if __name__=='__main__':
    p=argparse.ArgumentParser();sub=p.add_subparsers(dest='action',required=True)
    b=sub.add_parser('build');b.add_argument('case');b.add_argument('--mesh',default='1');b.add_argument('--step',default='0.5[ns]')
    s=sub.add_parser('solve');s.add_argument('case');s.add_argument('--threads',type=int,default=16)
    r=sub.add_parser('resume');r.add_argument('source');r.add_argument('new');r.add_argument('--jacobian',choices=['preserve','minimal','onevery'],default='preserve');r.add_argument('--damping',choices=['preserve','auto','const'],default='preserve');r.add_argument('--damp',type=float,default=1.)
    z=sub.add_parser('post');z.add_argument('case');z.add_argument('--reference');z.add_argument('--diagnostic',action='store_true')
    c=sub.add_parser('compare');c.add_argument('main');c.add_argument('variants',nargs='+')
    a=p.parse_args()
    if a.action=='build':build(a.case,a.mesh,a.step)
    elif a.action=='solve':call(WORK/'campaign.py',a.case,'--threads',a.threads)
    elif a.action=='resume':resume(a.source,a.new,a.jacobian,a.damping,a.damp)
    elif a.action=='post':
        args=[WORK/'postprocess_case.py',a.case]
        if a.reference:args+=['--reference',a.reference]
        if a.diagnostic:args+=['--diagnostic']
        call(*args)
    elif a.action=='compare':call(WORK/'compare_numerics.py',a.main,*a.variants)
