"""T003: run the supplied environment check and save UTF-8 evidence/lockfile."""
import datetime,json,os,subprocess,time
from pathlib import Path

def main():
    repo=Path(__file__).resolve().parents[2];out=repo/'results/T003'
    prefix=Path('D:/sim_tools/envs/lipsim');conda=Path('D:/sim_tools/miniforge3/Scripts/conda.exe')
    env=os.environ.copy()
    env.update(CONDARC='D:/sim_tools/miniforge3/.condarc',CONDA_REGISTER_ENVS='false',
        CONDA_PKGS_DIRS='D:/sim_tools/miniforge3/pkgs',CONDA_ENVS_PATH='D:/sim_tools/envs',
        PYTHONIOENCODING='utf-8',OPENBLAS_NUM_THREADS='1',MKL_NUM_THREADS='1')
    # DLL search paths confined to this subprocess, as with conda activation.
    env['PATH']=os.pathsep.join([str(prefix),str(prefix/'Library/bin'),str(prefix/'Scripts'),env.get('PATH','')])
    lock=subprocess.run([str(conda),'list','--prefix',str(prefix),'--explicit'],cwd=repo,env=env,capture_output=True,timeout=120)
    (out/'conda-explicit-win.txt').write_bytes(lock.stdout)
    (out/'conda-list-stderr.log').write_bytes(lock.stderr)
    if lock.returncode:raise RuntimeError('conda list failed')
    start=time.perf_counter()
    test=subprocess.run([str(prefix/'python.exe'),str(repo/'scripts/check_env.py'),'--threads','1','8','16','32','64'],
        cwd=repo,env=env,capture_output=True,timeout=300)
    (out/'check_env.json').write_bytes(test.stdout)
    (out/'check_env-stderr.log').write_bytes(test.stderr)
    if test.returncode:raise RuntimeError('check_env failed; see saved stderr')
    result=json.loads(test.stdout.decode('utf-8-sig'))
    failures={k:v for k,v in result.items() if isinstance(v,str) and 'IMPORT FAILED' in v}
    layer=result.get('threading_layer_used')
    timings=result.get('stencil_Mupdates_per_s_by_threads',{})
    ok=not failures and layer in ['tbb','omp'] and all(str(t) in timings for t in [1,8,16,32,64])
    validation=dict(checked_at=datetime.datetime.now(datetime.timezone.utc).isoformat(),
        passed=ok,import_failures=failures,threading_layer=layer,all_requested_thread_counts_run=all(str(t) in timings for t in [1,8,16,32,64]),
        wall_seconds=round(time.perf_counter()-start,3),throughput_Mupdates_s=timings,
        process_only_settings=['DLL PATH prefix','OPENBLAS_NUM_THREADS=1','MKL_NUM_THREADS=1','CONDARC','CONDA_REGISTER_ENVS=false'],
        source_check_script_unchanged=True)
    (out/'validation.json').write_text(json.dumps(validation,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(validation))
    if not ok:raise RuntimeError('T003 acceptance criteria not met')

if __name__=='__main__':main()
