"""Explicit server setup; check/compile/relocate never run a flow solve."""
from pathlib import Path
import argparse, datetime, hashlib, importlib.metadata, json, os, shutil, subprocess, sys
from server_runtime import BUNDLE, PROJECT, root, native, batch, java, compile_command, environment

WORK=PROJECT/'work';LOG=PROJECT/'logs'
SOURCES=['AuditBounds','BoundaryMap','BuildALE','BuildS4','CreateViews','ExportFields','ExportS4','ExportSettings','ExportVisible','MergeHistory','QuickWall','ResumeS4','SetSolverStrategy','RelocateS4','Prepare64','Inspect64']
DEFAULT_MODELS=['work/H15.mph','work/H15_view.mph','work/VGEA.mph','work/VTE_ready.mph','work/VG_ready.mph','work/VT_ready.mph','sources/LTE_baseline_ready.mph']

def sha(p):
    h=hashlib.sha256()
    with p.open('rb') as f:
        for part in iter(lambda:f.read(8*1024*1024),b''):h.update(part)
    return h.hexdigest()

def check():
    b=json.loads((PROJECT/'solver_budget.json').read_text(encoding='utf8'));used=sum(x['elapsed_seconds'] for x in b['runs'])
    deps={}
    for name in ['numpy','scipy','matplotlib','numba','requests','Pillow','PyMuPDF']:
        try:deps[name]=importlib.metadata.version(name)
        except importlib.metadata.PackageNotFoundError:deps[name]='MISSING'
    table_ok=sha(PROJECT/'data/lte_grid.txt')=='59d4eeb04edd2ac3b1b86199fca80228058741b393164ae4f213da7b00f23dca'
    result={'bundle':str(BUNDLE),'project':str(PROJECT),'python':sys.version,'COMSOL_ROOT':str(root()),'batch_exists':batch().is_file(),'java_exists':java().is_file(),'native_exists':native().is_dir(),'dependencies':deps,'LTE_table_sha256_ok':table_ok,'paused_by_user':(PROJECT/'PAUSED_BY_USER.json').exists(),'solver_budget_used_seconds':used,'solver_budget_remaining_seconds':b['budget_seconds']-used,'server_runtime_not_executed':True}
    print(json.dumps(result,indent=2,ensure_ascii=False))
    return all([table_ok,batch().is_file(),java().is_file(),native().is_dir(),all(v!='MISSING' for v in deps.values())])

def compile_sources():
    shutil.copy2(BUNDLE/'server_tools/RelocateS4.java',WORK/'RelocateS4.java')
    for name in SOURCES:
        cmd=compile_command(WORK/(name+'.java'));print('COMPILE',name,flush=True)
        with (LOG/('server_compile_'+name+'.txt')).open('wb') as out:
            p=subprocess.run(cmd,cwd=WORK,env=environment(),stdout=out,stderr=subprocess.STDOUT)
        if p.returncode or not (WORK/(name+'.class')).exists():raise RuntimeError('Compiler failed; see server_compile_'+name+'.txt')
    (PROJECT/'migration_compiled.json').write_text(json.dumps({'compiled_classes':SOURCES,'COMSOL_ROOT':str(root()),'completed_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'no_flow_solve':True},indent=2),encoding='utf8')

def relocate(models):
    if not (PROJECT/'migration_compiled.json').exists():raise RuntimeError('Run compile first.')
    if sha(PROJECT/'data/lte_grid.txt')!='59d4eeb04edd2ac3b1b86199fca80228058741b393164ae4f213da7b00f23dca':raise RuntimeError('LTE table differs from the audited original.')
    backup=PROJECT/'migration_originals/models';backup.mkdir(parents=True,exist_ok=True)
    report=[]
    for rel in models:
        src=(PROJECT/rel).resolve()
        if PROJECT.resolve() not in src.parents or src.suffix!='.mph':raise ValueError('Model must be an MPH inside project: '+str(src))
        if not src.is_file():raise FileNotFoundError(src)
        old=backup/rel;old.parent.mkdir(parents=True,exist_ok=True)
        if not old.exists():shutil.copy2(src,old)
        dest=src.with_name('_server_relocation_'+src.name)
        if dest.exists():raise FileExistsError('Inspect previous relocation output: '+str(dest))
        cmd=[sys.executable,str(WORK/'run_standalone.py'),'RelocateS4',str(src),str(dest),str(PROJECT/'data/lte_grid.txt'),str(WORK)]
        subprocess.run(cmd,cwd=WORK,check=True)
        if not dest.is_file():raise RuntimeError('Missing relocated model: '+str(dest))
        before=sha(src);os.replace(dest,src)
        report.append({'model':rel,'original_sha256':before,'relocated_sha256':sha(src),'original_backup':str(old.relative_to(PROJECT)),'solution_time_arrays_unchanged':True})
    (PROJECT/'migration_relocation.json').write_text(json.dumps({'models':report,'no_flow_solve':True,'completed_utc':datetime.datetime.now(datetime.timezone.utc).isoformat()},indent=2),encoding='utf8')

def unpause():
    if not (PROJECT/'migration_compiled.json').exists() or not (PROJECT/'migration_relocation.json').exists():raise RuntimeError('Compile and relocate the active models first.')
    flag=PROJECT/'PAUSED_BY_USER.json'
    if not flag.exists():raise RuntimeError('Project is already unpaused; inspect running workers before starting.')
    data=json.loads(flag.read_text(encoding='utf8'));data['server_unpaused_utc']=datetime.datetime.now(datetime.timezone.utc).isoformat()
    (PROJECT/'SERVER_RESUME_ACKNOWLEDGED.json').write_text(json.dumps(data,indent=2,ensure_ascii=False),encoding='utf8');flag.unlink()
    print('Server pause flag removed. No calculation or automation was started. Existing solver budget is unchanged.')

if __name__=='__main__':
    p=argparse.ArgumentParser();sub=p.add_subparsers(dest='action',required=True)
    sub.add_parser('check');sub.add_parser('compile');r=sub.add_parser('relocate');r.add_argument('models',nargs='*',default=DEFAULT_MODELS)
    u=sub.add_parser('unpause');u.add_argument('--acknowledge-existing-budget',action='store_true',required=True)
    a=p.parse_args()
    if a.action=='check':sys.exit(0 if check() else 1)
    elif a.action=='compile':compile_sources()
    elif a.action=='relocate':relocate(a.models or DEFAULT_MODELS)
    elif a.action=='unpause':unpause()
