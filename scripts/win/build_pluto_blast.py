"""Build T009 against the T005 stock tree; no physics patches."""
from pathlib import Path
import hashlib, json, os, re, shutil, subprocess, tarfile, time

REPO = Path(__file__).resolve().parents[2]
ROOT = Path('D:/sim_tools/pluto_ref')
BUILD = ROOT / 'build/T009_blast'
OUT = REPO / 'results/T009'

def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()

def environment():
    p = json.loads((REPO/'results/T005/build_provenance.json').read_text())
    env = os.environ.copy()
    env['PATH'] = str(Path(p['compiler']).parent)+os.pathsep+str(Path(p['mpi_executable']).parent)+os.pathsep+env['PATH']
    for key in ('OMP_NUM_THREADS','OPENBLAS_NUM_THREADS','MKL_NUM_THREADS'): env[key]='1'
    return env,p

def main():
    OUT.mkdir(parents=True,exist_ok=True); BUILD.mkdir(parents=True,exist_ok=True)
    env,old=environment()
    archive=REPO/'third_party/pluto/pluto-4.4-patch4.tar.gz'
    assert sha(archive)==old['official_tar_sha256']
    differences=[]
    with tarfile.open(archive) as tf:
        for m in tf:
            if m.isfile() and m.name.startswith('PLUTO/'):
                dest=ROOT/m.name
                expected=hashlib.sha256(tf.extractfile(m).read()).hexdigest()
                if not dest.exists() or sha(dest)!=expected:
                    differences.append(dict(file=m.name,official_sha256=expected,actual_sha256=sha(dest) if dest.exists() else None))
    assert len(differences)==1 and differences[0]['file']=='PLUTO/Src/HD/makefile', differences
    for name in ('definitions.h','init.c'):
        shutil.copy2(REPO/'verify/pluto_blast'/name,BUILD/name)
    for name in ('windows_compat.c','windows_compat.h'):
        shutil.copy2(ROOT/'build/Sedov_mpi8'/name,BUILD/name)
    template=(ROOT/'PLUTO/Src/Templates/makefile').read_text()
    template=re.sub(r'(?m)^ARCH\s*=.*$', 'ARCH = Windows.mpi8.gcc.defs',template)
    template=re.sub(r'(?m)^PLUTO_DIR\s*=.*$',f'PLUTO_DIR = {(ROOT/"PLUTO").as_posix()}',template)
    template=re.sub(r'(?m)^VPATH\s*=.*$','VPATH = .;$(SRC)/New;$(SRC);$(SRC)/Time_Stepping;$(SRC)/States',template)
    extra='include $(SRC)/HD/makefile\ninclude $(SRC)/EOS/Ideal/makefile\nOBJ += plm_states.o rk_step.o update_stage.o windows_compat.o\n'
    template=template.replace('pluto: $(OBJ)',extra+'\npluto: $(OBJ)')
    (BUILD/'makefile').write_text(template)
    shutil.copy2(BUILD/'makefile',OUT/'makefile')
    cmd=[str(Path(old['compiler']).parent/'mingw32-make.exe'),'-j','8']
    start=time.perf_counter()
    with (OUT/'build.log').open('wb') as log:
        proc=subprocess.run(cmd,cwd=BUILD,env=env,stdout=log,stderr=subprocess.STDOUT,timeout=300)
    result=dict(exit_code=proc.returncode,wall_s=time.perf_counter()-start,command=cmd,
                compiler=old['compiler'],mpi_executable=old['mpi_executable'],official_tar_sha256=sha(archive),
                upstream_differences=differences,
                user_problem_files={n:sha(BUILD/n) for n in ('definitions.h','init.c')},
                adapters={n:sha(BUILD/n) for n in ('windows_compat.c','windows_compat.h')},
                notes=['Task-specified initial condition and budget diagnostics use official user hooks.',
                       'Only stock-tree difference is the T005 HD makefile separator adaptation.',
                       'No EOS, source, conduction, cooling, or solver patch.'])
    if proc.returncode==0: result['executable_sha256']=sha(BUILD/'pluto.exe')
    (OUT/'build_provenance.json').write_text(json.dumps(result,indent=2))
    print(json.dumps(result,indent=2),flush=True)
    raise SystemExit(proc.returncode)

if __name__=='__main__': main()
