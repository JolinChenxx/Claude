"""Export one saved case, with mechanism budgets and explicit fixed-plane metadata."""
from pathlib import Path
import subprocess,sys,os,json
R=Path(__file__).resolve().parent
case=sys.argv[1]
resume_at=sys.argv[2] if len(sys.argv)>2 else None
env=os.environ.copy();env['ROUTE1_THREADS']='16';env['ROUTE1_LOG_TAG']=case
for cls,suffix,extra in [
    ('MeshStateAudit','_mesh_audit.csv',[]),
    ('ExportNative','',[]),
    ('ExportMechanism','_mechanism.csv',['5']),
    ('PlasmaDiagnostic','_diagnostic.csv',[]),
    ('ExportBalances','_balances.csv',['trace']),
]:
    if resume_at is not None:
        if cls!=resume_at:continue
        resume_at=None
    subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java',cls,
                    str(R/'models'/f'{case}.mph'),str(R/'results'/(case+suffix)),*extra],env=env,check=True)
    if cls=='ExportNative':
        with (R/'logs'/f'{case}_optical_round2.log').open('wb') as f:
            p=subprocess.Popen([sys.executable,'-X','utf8',str(R/'optical_round2.py'),case],stdout=f,stderr=subprocess.STDOUT,env=env)
        print('OPTICAL_LAUNCHED',case,p.pid,flush=True)
(R/'results'/f'{case}_stage.json').write_text(json.dumps({
    'case':case,'stage':'fixed_target_plane','melt_motion':False,
    'target_mass':'initial target mass minus integrated net surface feed; fixed geometric volume is not remaining target mass',
    'target_energy':'fixed-domain sensible/latent energy with full gas-enthalpy removal at the boundary',
    'flatRemovedHuJ':'diagnostic condensed-enthalpy transport only; not an additional total-energy sink',
    'validation':'exports only; inspect balances and optical exposure coverage before accepting'
},indent=2),encoding='utf8')
print('ROUND2_EXPORTS_DONE',case,flush=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'summarize_round2.py'),case],env=env,check=True)
