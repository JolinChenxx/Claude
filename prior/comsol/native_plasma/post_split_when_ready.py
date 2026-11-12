from pathlib import Path
import sys,time,json,os,subprocess
R=Path(__file__).resolve().parent;case=sys.argv[1];target=float(sys.argv[2])
while True:
 try:status=json.loads((R/'results'/case/'status.json').read_text())
 except (FileNotFoundError,json.JSONDecodeError):status={}
 if status.get('saved_time_ns',0)>=target:break
 time.sleep(30)
env=os.environ.copy();env.update(ROUTE1_CAMERA_ZERO_NS='90',ROUTE1_RADIATION_EXPOSURES_ONLY='1',ROUTE1_RADIATION_MIN_NS='80',ROUTE1_POST_THREADS='16')
for script,args in [('export_split_optics.py',[case]),('plot_split_fields.py',[case]),('radiation_native.py',[case+'native']),('preview_native.py',[case+'native']),('preview_gate_sequence.py',[case+'native']),('compare_s4_fixed_camera.py',[case+'native'])]:
 if script=='compare_s4_fixed_camera.py' and status.get('saved_time_ns',0)>=175 and status.get('plasma_wave_bound') and status.get('actual_electron_removal'):
  # The comparison script independently requires all seven complete gates
  # and creates the shared calibration exclusively, without overwriting it.
  args=args+['--freeze']
 subprocess.run([sys.executable,'-X','utf8',str(R/script),*args],env=env,check=True)
print('POST_COMPLETE',case,target,flush=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'audit_split_hot_regime.py'),case],check=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'diagnose_material_reach.py'),case],check=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'diagnose_electron_transport.py'),case],check=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'compare_first_gate_physical.py'),case],check=True)
