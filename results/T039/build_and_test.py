"""Build the released v1w source and run the card's three test modules."""
from datetime import datetime
from pathlib import Path
import hashlib
import json
import os
import shutil
import subprocess
import time

REPO = Path(__file__).resolve().parents[2]
OUT = REPO / 'results/T039'
BUILD = Path('D:/sim_tools/pluto_lip/build_v1w/mpi')
PYTHON = Path('D:/sim_tools/envs/lipsim/python.exe')
GCC = Path('C:/Program Files/ANSYS Inc/v261/SCADE Rapid Prototyper/contrib/Msys64/mingw64/bin')
MPI = Path('C:/Program Files/Microsoft MPI/Bin')
TEMP = Path('D:/sim_tools/T039_build_tmp')


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    assert not (BUILD / 'pluto.exe').exists(), 'Do not silently rebuild an existing production executable'
    assert not (OUT / 'build_test_execution.json').exists()
    TEMP.mkdir(parents=True, exist_ok=True)
    env = dict(os.environ)
    env.update(PATH=os.pathsep.join([str(GCC),str(MPI),str(PYTHON.parent),str(PYTHON.parent/'Library/bin'),env['PATH']]),
               PYTHONPATH=str(REPO/'src'), PYTHONUTF8='1', PYTHONDONTWRITEBYTECODE='1',
               OMP_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', MKL_NUM_THREADS='1', TEMP=str(TEMP), TMP=str(TEMP))
    evidence = {'source_commit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=REPO,text=True).strip(), 'commands':[]}
    commands = [
        ('build', [str(PYTHON),'scripts/build_pluto.py','--problem','pluto/lip','--out',str(BUILD),'--platform','windows','--mpi','--eos','pvte','--tc','--extra-obj','lip_tables.o','lip_physics.o','lip_glue.o','--jobs','8']),
        ('tests', [str(PYTHON),'-m','pytest','-q','tests/test_overrides.py','tests/test_target.py','tests/test_droplet_v1r.py','tests/test_breakdown.py','tests/test_knight_bp.py','--basetemp',str(TEMP/'pytest')])]
    try:
        for name, command in commands:
            row={'name':name,'command':command,'started':datetime.now().astimezone().isoformat()}
            start=time.perf_counter()
            log=OUT/('build_driver.log' if name=='build' else 'tests.log')
            with log.open('wb') as stream:
                row['returncode']=subprocess.run(command,cwd=REPO,env=env,stdout=stream,stderr=subprocess.STDOUT).returncode
            row.update(finished=datetime.now().astimezone().isoformat(),seconds=time.perf_counter()-start,log=log.relative_to(REPO).as_posix(),log_sha256=sha(log))
            evidence['commands'].append(row)
            print(json.dumps(row),flush=True)
            assert row['returncode']==0, name+' failed; see '+str(log)
        prov=json.loads((BUILD/'build_provenance.json').read_text())
        assert prov['make_returncode']==0 and len(prov['replaced_upstream_files'])==7
        assert sha(BUILD/'pluto.exe')==prov['executable_sha256']
        for name,digest in prov['problem_files'].items():
            assert sha(REPO/'pluto/lip'/name)==digest
            if name.endswith(('.c','.h')):assert sha(BUILD/name)==digest
        tests=(OUT/'tests.log').read_text(encoding='utf-8')
        assert ' passed' in tests and 'failed' not in tests and 'skipped' not in tests
        for name in ['build.log','build_provenance.json']:
            shutil.copyfile(BUILD/name,OUT/name)
            assert sha(BUILD/name)==sha(OUT/name)
        evidence.update(executable=str(BUILD/'pluto.exe'),executable_sha256=prov['executable_sha256'],
                        replaced_count=7,verified_problem_files=len(prov['problem_files']),test_output=tests,
                        compiler_log='results/T039/build.log',compiler_warning_count=(BUILD/'build.log').read_text().count('warning:'),
                        environment_policy='Process-only PATH/PYTHONPATH and single-thread BLAS/OMP. TEMP/TMP and pytest scratch under D:/sim_tools. No execution policy/system settings/physics changes.')
        print(json.dumps({k:v for k,v in evidence.items() if k!='commands'}),flush=True)
    finally:
        (OUT/'build_test_execution.json').write_text(json.dumps(evidence,indent=2)+'\n',encoding='utf-8')


if __name__=='__main__':
    main()
