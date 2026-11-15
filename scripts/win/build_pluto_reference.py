"""T005: pinned stock Sod01/Sedov03, with Windows build-only adaptation."""
from pathlib import Path
import concurrent.futures, hashlib, json, os, re, shutil, subprocess, tarfile, time
REPO=Path(__file__).resolve().parents[2]
ROOT=Path('D:/sim_tools/pluto_ref')
PLUTO=ROOT/'PLUTO'
OUT=REPO/'results/T005'
RAW=Path('D:/sim_runs/T005')
GCC=Path('C:/Program Files/ANSYS Inc/v261/SCADE Rapid Prototyper/contrib/Msys64/mingw64/bin')
MPI=Path('C:/Program Files/Microsoft MPI/Bin/mpiexec.exe')
TAR=Path('D:/junlin/PlasmaPlumeHighInfluence/vendor/pluto-4.4-patch4.tar.gz')
EXPECTED='1ba5527b76d49fdd78ae24dbfbdad085ec83393748f1e618516a9d63bd945787'

def digest(p): return hashlib.sha256(p.read_bytes()).hexdigest()

def prepare():
    OUT.mkdir(parents=True,exist_ok=True);RAW.mkdir(parents=True,exist_ok=True);ROOT.mkdir(exist_ok=True)
    assert digest(TAR)==EXPECTED
    if not PLUTO.exists():
        with tarfile.open(TAR) as t:t.extractall(ROOT,filter='data')
    # Native mingw32-make uses semicolon-separated VPATH. Only HD needs this.
    src=REPO/'prior/pluto/upstream_overrides/Src/HD/makefile'
    shutil.copy2(src,PLUTO/'Src/HD/makefile')
    sdk=ROOT/'msmpi'
    if not sdk.exists():shutil.copytree('D:/junlin/PlasmaPlumeHighInfluence/vendor/msmpi',sdk)
    for parallel in (False,True):
        mode='mpi8' if parallel else 'serial'
        flags='-c -O3 -std=c11 -Wno-implicit-function-declaration -DMSMPI_NO_SAL -include windows_compat.h'
        if parallel:flags+=f' -I{sdk.as_posix()}/include -I{sdk.as_posix()}/include/x64'
        (PLUTO/f'Config/Windows.{mode}.gcc.defs').write_text(
            f'CC = gcc\nCFLAGS = {flags}\nLDFLAGS = -lm '+(f'{sdk.as_posix()}/lib/msmpi.lib' if parallel else '')+
            '\nPARALLEL = '+('TRUE' if parallel else 'FALSE')+'\nUSE_HDF5 = FALSE\nUSE_PNG = FALSE\n')
    env=os.environ.copy();env['PATH']=str(GCC)+os.pathsep+str(MPI.parent)+os.pathsep+env['PATH']
    env['OMP_NUM_THREADS']='1';env['OPENBLAS_NUM_THREADS']='1'
    version=subprocess.run([str(GCC/'gcc.exe'),'--version'],env=env,capture_output=True,text=True,check=True).stdout
    (OUT/'compiler_version.txt').write_text(version,encoding='utf-8')
    changes=[]
    with tarfile.open(TAR) as tf:
        for member in tf:
            if member.isfile() and member.name.startswith('PLUTO/'):
                dest=ROOT/member.name
                if not dest.exists():changes.append({'file':member.name,'change':'missing'});continue
                original=hashlib.sha256(tf.extractfile(member).read()).hexdigest()
                actual=digest(dest)
                if original!=actual:changes.append({'file':member.name,'original_sha256':original,'actual_sha256':actual})
    manifest=dict(official_tar=str(TAR),official_tar_sha256=EXPECTED,compiler=str(GCC/'gcc.exe'),compiler_version=version,
      mpi_executable=str(MPI),modified_official_files=changes,
      added_config_files=['Config/Windows.serial.gcc.defs','Config/Windows.mpi8.gcc.defs'],
      compatibility_helpers={name:digest(REPO/'prior/pluto/solver'/name) for name in ['windows_compat.c','windows_compat.h']},
      notes=['No project EOS, conduction, cooling, source, main, or solver patches.',
       'Only upstream Src/HD/makefile VPATH separator adapted.',
       'Compatibility helpers supply POSIX drand48/srand48 and missing SAL annotation; no fluid physics.',
       'Sod official configuration01; Sedov official configuration03 (1D spherical, exponent target0.4).',
       'Sedov dbl output interval changes0.5 to0.025 for shock trajectory only; every numerical and physical setting unchanged.'])
    (OUT/'build_provenance.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
    assert len(changes)==1 and changes[0]['file']=='PLUTO/Src/HD/makefile',changes
    return env

def one(problem,parallel,env):
    mode='mpi8' if parallel else 'serial';name=problem+'_'+mode;cfg='01' if problem=='Sod' else '03'
    build=ROOT/'build'/name;run=RAW/name;build.mkdir(parents=True,exist_ok=True);run.mkdir(parents=True,exist_ok=True)
    source=PLUTO/'Test_Problems/HD'/problem
    shutil.copy2(source/f'definitions_{cfg}.h',build/'definitions.h')
    for filename in ('init.c','userdef_output.c'):
        if (source/filename).exists():shutil.copy2(source/filename,build/filename)
    for filename in ('windows_compat.c','windows_compat.h'):shutil.copy2(REPO/'prior/pluto/solver'/filename,build/filename)
    ini=(source/f'pluto_{cfg}.ini').read_text()
    if problem=='Sedov':ini=re.sub(r'(?m)^dbl\s+0\.5\s+-1\s+single_file', 'dbl        0.025  -1   single_file',ini)
    (run/'pluto.ini').write_text(ini)
    template=(PLUTO/'Src/Templates/makefile').read_text()
    template=re.sub(r'(?m)^ARCH\s*=.*$',f'ARCH = Windows.{mode}.gcc.defs',template)
    template=re.sub(r'(?m)^PLUTO_DIR\s*=.*$',f'PLUTO_DIR = {PLUTO.as_posix()}',template)
    template=re.sub(r'(?m)^VPATH\s*=.*$','VPATH = .;$(SRC)/New;$(SRC);$(SRC)/Time_Stepping;$(SRC)/States',template)
    interp='plm_states.o' if problem=='Sod' else 'weno3_states.o'
    extra=f'include $(SRC)/HD/makefile\ninclude $(SRC)/EOS/Ideal/makefile\nOBJ += {interp} rk_step.o update_stage.o windows_compat.o\n'
    template=template.replace('pluto: $(OBJ)',extra+'\npluto: $(OBJ)')
    (build/'makefile').write_text(template)
    for source_path,target_name in [(build/'definitions.h',f'{name}_definitions.h'),(run/'pluto.ini',f'{name}_pluto.ini'),(build/'makefile',f'{name}_makefile')]:
        shutil.copy2(source_path,OUT/target_name)
    start=time.perf_counter();cmd=[str(GCC/'mingw32-make.exe'),'-j','8']
    with (OUT/f'{name}_build.log').open('wb') as log:
        proc=subprocess.run(cmd,cwd=build,env=env,stdout=log,stderr=subprocess.STDOUT,timeout=300)
    build_s=time.perf_counter()-start
    result=dict(case=name,configuration=cfg,build_exit=proc.returncode,build_seconds=build_s,build_command=cmd,build_dir=str(build),run_dir=str(run))
    if proc.returncode:
        print(name+' BUILD FAILED',flush=True);return result
    executable=build/'pluto.exe';cmd=[str(executable)]
    if parallel:cmd=[str(MPI),'-n','8','-wdir',str(run),str(executable)]
    start=time.perf_counter()
    with (OUT/f'{name}_run.log').open('wb') as log:
        proc=subprocess.run(cmd,cwd=run,env=env,stdout=log,stderr=subprocess.STDOUT,timeout=300)
    result.update(run_exit=proc.returncode,run_seconds=time.perf_counter()-start,run_command=cmd,executable_sha256=digest(executable))
    print(json.dumps(result),flush=True);return result

def main():
    start=time.perf_counter();env=prepare(); results=[]
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        futures=[pool.submit(one,problem,parallel,env) for problem in ['Sod','Sedov'] for parallel in [False,True]]
        for future in concurrent.futures.as_completed(futures):
            try:results.append(future.result())
            except Exception as e:results.append({'exception':repr(e)})
            (OUT/'execution.json').write_text(json.dumps({'cases':results,'elapsed_s':time.perf_counter()-start},indent=2),encoding='utf-8')
    if not all(r.get('run_exit')==0 for r in results):raise SystemExit(1)

if __name__=='__main__':main()
