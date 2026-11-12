"""Portable COMSOL 6.4 paths; no solver is started by importing this module."""
from pathlib import Path
import os, re, shutil, subprocess,json

BUNDLE=Path(__file__).resolve().parents[1]
PROJECT=BUNDLE/'project'

def root():
    default='C:/Program Files/COMSOL/COMSOL64/Multiphysics' if os.name=='nt' else '/opt/comsol64/multiphysics'
    return Path(os.environ.get('COMSOL_ROOT',default)).expanduser().resolve()

def native():
    return Path(os.environ.get('COMSOL_NATIVE',str(root()/'lib'/('win64' if os.name=='nt' else 'glnxa64'))))

def batch():
    override=os.environ.get('COMSOL_BATCH')
    if override:return Path(override)
    candidates=[root()/'bin/win64/comsolbatch.exe'] if os.name=='nt' else [root()/'bin/comsol',root()/'bin/glnxa64/comsol']
    return next((p for p in candidates if p.is_file()),candidates[0])

def batch_command():
    p=batch()
    if not p.is_file():raise FileNotFoundError('Set COMSOL_ROOT or COMSOL_BATCH to the installed COMSOL 6.4 executable.')
    # Linux uses the documented `comsol batch` launcher.
    return [str(p)] if p.name.lower().startswith('comsolbatch') else [str(p),'batch']

def java():
    override=os.environ.get('COMSOL_JAVA')
    if override:return Path(override)
    platform='win64' if os.name=='nt' else 'glnxa64'
    candidates=[root()/f'java/{platform}/jre/bin'/('java.exe' if os.name=='nt' else 'java'),root()/f'java/{platform}/bin'/('java.exe' if os.name=='nt' else 'java')]
    found=next((p for p in candidates if p.is_file()),None)
    return found or Path(shutil.which('java') or candidates[0])

def environment():
    env=os.environ.copy();key='PATH' if os.name=='nt' else 'LD_LIBRARY_PATH'
    env[key]=str(native())+os.pathsep+env.get(key,'')
    return env

def java_command(class_name,args):
    if not java().is_file():raise FileNotFoundError('Set COMSOL_JAVA to the Java bundled with COMSOL.')
    heap=os.environ.get('S4_JAVA_HEAP','16g')
    return [str(java()),'-Xmx'+heap,'-Djava.library.path='+str(native()),'-cp',str(PROJECT/'work')+os.pathsep+str(root()/'plugins/*'),class_name,*map(str,args)]

def compile_command(source):
    override=os.environ.get('COMSOL_COMPILE')
    if override:return [override,str(source)]
    direct=root()/('bin/win64/comsolcompile.exe' if os.name=='nt' else 'bin/comsolcompile')
    if direct.is_file():return [str(direct),str(source)]
    launcher=root()/('bin/win64/comsol.exe' if os.name=='nt' else 'bin/comsol')
    if not launcher.is_file():raise FileNotFoundError('Set COMSOL_ROOT or COMSOL_COMPILE to the installed compiler.')
    return [str(launcher),'compile',str(source)]

def running_solvers():
    if os.name=='nt':
        command="Get-CimInstance Win32_Process -Filter \"Name='comsolbatch.exe'\" | Select-Object ProcessId,CommandLine | ConvertTo-Json -Compress"
        r=subprocess.run(['powershell','-NoProfile','-Command',command],capture_output=True,text=True,check=True)
        rows=json.loads(r.stdout) if r.stdout.strip() else []
        if isinstance(rows,dict):rows=[rows]
        own=str(PROJECT).replace('\\','/').lower()
        return '\n'.join(str(x['ProcessId']) for x in rows if own in (x.get('CommandLine') or '').replace('\\','/').lower())
    r=subprocess.run(['ps','-eo','pid=,comm=,args='],capture_output=True,text=True,check=True)
    return '\n'.join(s for s in r.stdout.splitlines() if re.search(r'\bcomsolbatch\b|\bcomsol\s+batch\b',s,re.I))
