"""T002: read-only snapshot of pre-repository work; never launches model code.

Only this archiver and its output are new. Source bytes are copied verbatim and
hashed, raw simulations stay at their original paths. Re-running updates files
without deleting anything. See prior/*/README.md for scientific interpretation.
"""
from pathlib import Path
import ast, csv, datetime, difflib, hashlib, io, json, re, shutil, tarfile

REPO=Path(__file__).resolve().parents[1]
PLUTO=Path('D:/junlin/PlasmaPlumeHighInfluence')
BASE=Path('D:/junlin/PlasmaPlume/COMSOL_CuPlasma_S4_server_handoff_20261105/project')
NATIVE=Path('D:/junlin/PlasmaPlume/route1')
NONLTE=Path('D:/junlin/PlasmaPlume/route2')
NOW=datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat()
RAW={'.flt','.dbl','.vtk','.vtu','.h5','.hdf5','.bin','.mph','.recovery','.npy','.npz'}
manifest=[]

def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()

def copy(source,target):
    if not source.is_file():return
    target=REPO/'prior'/target;target.parent.mkdir(parents=True,exist_ok=True)
    before=source.stat();shutil.copyfile(source,target);after=source.stat()
    manifest.append(dict(source=str(source),archive=str(target.relative_to(REPO)),bytes=target.stat().st_size,
        sha256=sha(target),source_mtime=datetime.datetime.fromtimestamp(after.st_mtime,datetime.timezone.utc).isoformat(),
        source_stable_while_copied=(before.st_size==after.st_size and before.st_mtime_ns==after.st_mtime_ns)))

def tree(source,target,extensions,max_size=2*1024**2):
    if not source.exists():return
    for p in sorted(source.rglob('*')):
        if p.is_file() and '__pycache__' not in p.parts and p.suffix in extensions and p.stat().st_size<=max_size:
            copy(p,Path(target)/p.relative_to(source))

def selected(source,target,patterns,max_size=2*1024**2):
    paths=set()
    for pat in patterns:paths.update(source.glob(pat))
    for p in sorted(paths):
        if p.is_file() and p.stat().st_size<=max_size:copy(p,Path(target)/p.name)

def csvwrite(path,rows,fields):
    path.parent.mkdir(parents=True,exist_ok=True)
    with path.open('w',encoding='utf-8',newline='') as f:
        w=csv.DictWriter(f,fieldnames=fields);w.writeheader();w.writerows(rows)

def raw_inventory(roots,target):
    rows=[];summary=[]
    for label,root in roots.items():
        count=total=0
        for p in root.rglob('*'):
            if p.is_file() and p.suffix.lower() in RAW:
                size=p.stat().st_size;count+=1;total+=size
                rows.append(dict(branch=label,path=str(p),bytes=size))
        summary.append(dict(branch=label,root=str(root),raw_files=count,raw_bytes=total))
    csvwrite(target/'raw_inventory.csv',rows,['branch','path','bytes'])
    (target/'raw_summary.json').write_text(json.dumps(summary,indent=2)+'\n',encoding='utf-8')
    return summary

def constants(root,target):
    rows=[]
    for p in sorted(root.rglob('*.py')):
        try:module=ast.parse(p.read_text(encoding='utf-8-sig'))
        except (SyntaxError,UnicodeError):continue
        for node in module.body:
            if isinstance(node,(ast.Assign,ast.AnnAssign)):
                value=node.value
                if value is None:continue
                try:text=ast.unparse(value)
                except Exception:continue
                targets=node.targets if isinstance(node,ast.Assign) else [node.target]
                for lhs in targets:
                    rows.append(dict(file=str(p.relative_to(root)),line=node.lineno,name=ast.unparse(lhs),expression=text,
                        units='See expression/code; otherwise unknown',source='Inherited code; unknown unless documented in README/source',
                        fitted='Unknown unless explicitly classified in README; do not infer not-fitted'))
    csvwrite(target,rows,['file','line','name','expression','units','source','fitted'])

def main():
    for name in ['pluto','comsol']:(REPO/'prior'/name).mkdir(parents=True,exist_ok=True)
    # PLUTO: all custom/overridden sources, driver scripts and compile definitions.
    tree(PLUTO/'solver','pluto/solver',{'.c','.h'})
    copy(PLUTO/'solver/makefile',Path('pluto/solver/makefile'))
    tree(PLUTO/'scripts','pluto/scripts',{'.py','.ps1'})
    tree(PLUTO/'config','pluto/config',{'.yaml','.txt'})
    copy(PLUTO/'vendor/PLUTO/Config/Windows.gcc.defs',Path('pluto/upstream_overrides/Config/Windows.gcc.defs'))
    for n in ['MODEL.md','README.md']:copy(PLUTO/n,Path('pluto/original_docs')/n)
    copy(PLUTO/'vendor/sources.json',Path('pluto/provenance/upstream_sources.json'))
    for p in (PLUTO/'figures').iterdir():
        if p.suffix in {'.png','.gif'}:copy(p,Path('pluto/figures')/p.name)
    for n in ['campaign.json','status.json','best.json','build.json','archive.json','controller.json','last_pdf.json']:
        copy(PLUTO/'state'/n,Path('pluto/state')/n)
    best=json.loads((REPO/'prior/pluto/state/best.json').read_text(encoding='utf-8'))
    cases={'baseline',best['run_id']}
    results=json.loads((REPO/'prior/pluto/state/archive.json').read_text(encoding='utf-8'))
    valid=[x for x in results if x.get('parameters',{}).get('level')=='medium' and abs(x.get('energy_residual',1))<.05]
    if valid:cases.add(min(valid,key=lambda x:x['score'])['run_id'])
    for case in sorted(cases):
        folder=PLUTO/'runs'/case
        selected(folder,Path('pluto/cases')/case,['*.json','*.csv','pluto.ini','grid.out','flt.out','dbl.out','*.log'],max_size=2*1024**2)
    tree(PLUTO/'data/experimental','pluto/experiment',{'.csv','.json','.jpeg','.png'})
    print('PLUTO source/config/results copied',flush=True)
    stock=[]
    with tarfile.open(fileobj=io.BytesIO((PLUTO/'vendor/pluto-4.4-patch4.tar.gz').read_bytes()),mode='r:gz') as tar:
        contents={}
        for m in tar:
            rel=m.name.partition('/')[2]
            if m.isfile() and rel.startswith(('Src/','Config/')):contents[rel]=tar.extractfile(m).read()
        print('Stock source read:',len(contents),'files',flush=True)
        for rel,old in contents.items():
            actual=PLUTO/'vendor/PLUTO'/rel
            if actual.is_file():
                new=actual.read_bytes()
                if old!=new:
                    copy(actual,Path('pluto/upstream_overrides')/rel)
                    stock.append(dict(file=rel,stock_sha256=hashlib.sha256(old).hexdigest(),actual_sha256=sha(actual)))
        for p in (PLUTO/'solver').glob('*.c'):
            matches=[(key,value) for key,value in contents.items() if Path(key).name==p.name]
            if len(matches)==1:
                key,value=matches[0];old=value.decode(errors='replace');new=p.read_text()
                diff=''.join(difflib.unified_diff(old.splitlines(True),new.splitlines(True),fromfile='stock/'+key,tofile='solver/'+p.name))
                if diff:
                    dest=REPO/'prior/pluto/patches'/(p.name+'.diff');dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(diff,encoding='utf-8')
    (REPO/'prior/pluto/provenance/modified_upstream.json').write_text(json.dumps(stock,indent=2),encoding='utf-8')
    print('Stock PLUTO comparison complete',flush=True)
    # COMSOL: full source branches, no compiled classes or solver data.
    for branch,root in [('baseline',BASE),('native_plasma',NATIVE),('finite_rate',NONLTE)]:
        dest=Path('comsol')/branch
        for sub in ['work','src']:
            tree(root/sub,dest/sub,{'.py','.java','.c','.h','.ps1','.yaml','.toml','.json'})
        if branch=='native_plasma':selected(root,dest,['*.py','*.json','*.md'])
        else:selected(root,dest,['*.json','*.md'])
        tree(root/'docs',dest/'docs',{'.md'})
        tree(root/'data',dest/'data',{'.csv','.tsv','.url','.json'},max_size=1024**2)
    # Baseline thermodynamics is a necessary source dependency, not a solved field.
    for p in (BASE/'data').glob('*.txt'):
        if p.stat().st_size<16*1024**2:copy(p,Path('comsol/baseline/data')/p.name)
    selected(BASE/'results','comsol/baseline/results',
        ['B64A*.json','B64A*.csv','G64AC2*.json','T64AC2*.json','G264R1C5*.json',
         '*comparison64.json','*comparison64.csv','*grid*.json','*grid*.csv','run_ledger.csv',
         'photon*.json','photon*.csv','*camera*.json','*summary*.json'],max_size=256*1024)
    for case in ['B64A','G64AC2','T64AC2','G264R1C5']:
        tree(BASE/'results'/(case+'_camera'),Path('comsol/baseline/results')/(case+'_camera'),{'.csv','.json'},max_size=256*1024)
    tree(BASE/'experiment','comsol/baseline/experiment',{'.json','.csv','.png','.jpeg'},max_size=1024**2)
    tree(BASE.parent/'server_tools','comsol/server_tools',{'.py','.java','.ps1','.txt'},max_size=256*1024)
    selected(BASE.parent,Path('comsol'),['run64.ps1'])
    selected(BASE.parent,Path('comsol'),['*CHECKS*.json'])
    selected(BASE/'docs',Path('comsol/baseline/docs'),['requirements*.txt'])
    selected(BASE/'figures','comsol/baseline/figures',
        ['B64A*.png','B64A_S4.gif','physical_numerical_comparison64.png','G264R1C5_S4_comparison.png'])
    selected(BASE/'logs','comsol/baseline/logs',['final_validation_tests64.txt','BENCH*.log'],max_size=128*1024)
    selected(NONLTE/'results','comsol/finite_rate/results',['*.csv','*.json'],max_size=256*1024)
    selected(NONLTE/'figures','comsol/finite_rate/figures',
        ['R2_mechanism*.png','R2_observable*.png','R2_late_spectral_fingerprint.png','R2_shape_mesh_comparison.png',
         'R2_COUPLED_budget.png','R2_COUPLED_Q_budget.png','R2_1T_numerical_comparison.png','R2_passband_comparison.png'])
    # Native-plasma branch: copy real partial output; never call it 29 completed gates.
    for case in ['P221chainmapped','HC454','P73']:
        tree(NATIVE/'results'/case,Path('comsol/native_plasma/results')/case,{'.json','.csv'},max_size=128*1024)
        selected(NATIVE/'figures'/case,Path('comsol/native_plasma/figures')/case,
            ['S4_complete_gates_physical.png','histories.png','S4_comparison.png'])
    for p in (NATIVE/'models').glob('*.java'):copy(p,Path('comsol/native_plasma/models')/p.name)
    # Inventory exported COMSOL parameter values (last assignment wins within a model).
    params=[]
    for p in (REPO/'prior/comsol').rglob('*.java'):
        final={}
        for match in re.finditer(r'\.param\(\)\.set\("([^"\n]+)",\s*"((?:\\.|[^"\\])*)"',p.read_text(encoding='utf-8-sig')):
            final[match[1]]=match[2]
        for name,value in final.items():params.append(dict(model=str(p.relative_to(REPO/'prior/comsol')),parameter=name,expression=value,
            units='In expression; otherwise unknown',source='Inherited model; see README, original docs and settings JSON; unknown if not established',
            fitted='Unknown except explicitly identified camera/numerical/physics inputs in README'))
    csvwrite(REPO/'prior/comsol/model_parameters.csv',params,['model','parameter','expression','units','source','fitted'])
    constants(REPO/'prior/pluto/scripts',REPO/'prior/pluto/code_constants.csv')
    constants(REPO/'prior/comsol/finite_rate/work',REPO/'prior/comsol/finite_rate/code_constants.csv')
    summary={
        'pluto':raw_inventory({'runs':PLUTO/'runs'},REPO/'prior/pluto'),
        'comsol':raw_inventory({'baseline':BASE,'native_plasma':NATIVE,'finite_rate':NONLTE},REPO/'prior/comsol')}
    for name in ['pluto','comsol']:
        files=[p for p in (REPO/'prior'/name).rglob('*') if p.is_file()]
        size=sum(p.stat().st_size for p in files)
        assert size<=50*1024**2,(name,size)
        print(name,len(files),round(size/1024**2,2),'MiB')
    out=REPO/'results/T002';out.mkdir(parents=True,exist_ok=True)
    (out/'copy_manifest.json').write_text(json.dumps(dict(snapshot_at=NOW,files=manifest,raw_summary=summary),ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('verbatim copies',len(manifest),'source changed during copy',sum(not x['source_stable_while_copied'] for x in manifest))

if __name__=='__main__':main()
