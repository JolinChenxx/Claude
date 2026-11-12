"""Small isolated COMSOL launcher; records actual commands and return codes."""
from pathlib import Path
import os, sys, subprocess, json, datetime, zipfile, textwrap
ROOT=Path(__file__).resolve().parent
COMSOL=Path('C:/Program Files/COMSOL/COMSOL64/Multiphysics')
def env():
    e=os.environ.copy()
    e['PATH']=str(COMSOL/'lib/win64')+os.pathsep+e.get('PATH','')
    n=e.get('ROUTE1_THREADS','64')
    e.update(OMP_NUM_THREADS=n,MKL_NUM_THREADS=n,OPENBLAS_NUM_THREADS='1')
    return e
def main():
    action,name,*args=sys.argv[1:]
    if action=='compile':
        (ROOT/'src'/(name+'.class')).unlink(missing_ok=True)
        cmd=[str(COMSOL/'bin/win64/comsolcompile.exe'),str(ROOT/'src'/(name+'.java'))]
    elif action=='java':
        # Standalone API has no OSGi bundle context. Its SLF4J binding breaks
        # SQLite/ASHRAE initialization in Heat Transfer; retain the SLF4J API.
        cp='Class-Path: '+' '.join(p.as_uri() for p in sorted((COMSOL/'plugins').glob('*.jar')) if not p.name.startswith('org.osgi.slf4j.osgi'))
        manifest='Manifest-Version: 1.0\r\n'+ '\r\n '.join(cp[i:i+69] for i in range(0,len(cp),69))+'\r\n\r\n'
        tag=os.environ.get('ROUTE1_LOG_TAG','')
        jar=ROOT/'logs'/('standalone_classpath'+('_'+tag if tag else '')+'.jar')
        with zipfile.ZipFile(jar,'w') as z:z.writestr('META-INF/MANIFEST.MF',manifest)
        cmd=[str(COMSOL/'java/win64/jre/bin/java.exe'),'-Xmx24g','-XX:+UseAllWindowsProcessorGroups','-Djava.library.path='+str(COMSOL/'lib/win64'),'-cp',str(ROOT/'src')+os.pathsep+str(jar),name,*args]
    elif action=='solve':
        cmd=[str(COMSOL/'bin/win64/comsolbatch.exe'),'-np',os.environ.get('ROUTE1_THREADS','64'),'-inputfile',str(ROOT/'models'/(name+'_ready.mph')),'-outputfile',str(ROOT/'models'/(name+'.mph')),'-batchlog',str(ROOT/'logs'/(name+'_solver.log')),*args]
        if 'ROUTE1_PREFSDIR' in os.environ:
            cmd.extend(['-prefsdir',os.environ['ROUTE1_PREFSDIR']])
        if 'ROUTE1_BATCH_INI' in os.environ:
            cmd.extend(['-comsolinifile',os.environ['ROUTE1_BATCH_INI']])
        if 'ROUTE1_NUMA' in os.environ:
            cmd.extend(['-numasets','1','-numafirst',str(int(os.environ['ROUTE1_NUMA'])),'-mpmode','throughput'])
        if 'ROUTE1_JAVA_HEAP' in os.environ:
            cmd.extend(['--launcher.appendVmargs','-vmargs','-Xmx'+os.environ['ROUTE1_JAVA_HEAP'],'-XX:MaxMetaspaceSize=2g','-XX:+UseAllWindowsProcessorGroups'])
    else: raise ValueError(action)
    logname=name+('_'+os.environ['ROUTE1_LOG_TAG'] if os.environ.get('ROUTE1_LOG_TAG') else '')
    path=ROOT/'logs'/(logname+'_'+action+'.log')
    start=datetime.datetime.now(datetime.timezone.utc)
    print('START',start.isoformat(),name,action,flush=True)
    with path.open('wb') as f:
        p=subprocess.Popen(cmd,cwd=ROOT/'src',env=env(),stdout=f,stderr=subprocess.STDOUT)
        (ROOT/'logs'/(logname+'_'+action+'_process.json')).write_text(json.dumps({'pid':p.pid,'started_utc':start.isoformat(),'command':cmd},indent=2),encoding='utf8')
        code=p.wait()
    raw=path.read_bytes()
    try:message=raw.decode('utf8')
    except UnicodeDecodeError:message=raw.decode('gb18030',errors='replace')
    print(message[-7000:])
    if action=='compile' and not (ROOT/'src'/(name+'.class')).exists(): code=1
    print('EXIT',code,flush=True)
    sys.exit(code)
if __name__=='__main__':main()
