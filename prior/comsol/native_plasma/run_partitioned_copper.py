from pathlib import Path
import os,sys,subprocess,json,datetime
R=Path(__file__).resolve().parent
case=sys.argv[1] if len(sys.argv)>1 else 'C201'
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case)
def run(*args):
    subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),*args],env=env,check=True)
run('java','PreparePartitionedCopper',str(R/'models/M120_ready.mph'),str(R/'models'/f'{case}_ready.mph'),'203[ns]')
run('solve',case)
run('java','ExportMelt',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_surface.csv'))
subprocess.run([sys.executable,'-X','utf8',str(R/'plot_partitioned_copper.py'),case],env=env,check=True)
(R/'results'/f'{case}_stage.json').write_text(json.dumps(dict(case=case,stage='partitioned copper predictor',real_moving_surface=True,plasma_solved=False,two_way_converged=False,camera_zero_model_ns=90,high_temperature_material_validated=False,finished_utc=datetime.datetime.now(datetime.timezone.utc).isoformat()),indent=2))
print('COPPER PREDICTOR EXPORTED',case,flush=True)
