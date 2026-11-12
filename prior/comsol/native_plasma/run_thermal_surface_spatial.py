"""From-initial spatial native Plasma test of documented thermal Cu feed."""
from pathlib import Path
import os,sys,subprocess,json,time,ctypes
R=Path(__file__).resolve().parent;case='P222'
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*map(str,args)],env=env,check=True)
# P216 was rebuilt from the cold state on the 40-cell radial comparison mesh.
# SurfaceStateFeed clears saved solutions and sets initialization to init.
py('run.py','java','SurfaceStateFeed',R/'models/P216_ready.mph',R/'models/P222_ready.mph','125[ns]')
(R/'results/P222_configuration.json').write_text(json.dumps({'source':'P216_ready','initial_state':'cold, recomputed','change':'documented Saha-Langmuir Cu ion emission and Boltzmann neutral-state partition','total_Cu_evaporation':'unchanged predictor history C201, supported to133ns','electron_injection':'native thermionic boundary and explicit sheath; no co-emitted artificial electron source','energy':'state enthalpy added to gas boundary; feedback to copper predictor pending','source_record':'data/surface_feed_sources.json','comparison':'physical boundary change; not a continuation of P218'},indent=2))
previous=json.loads((R/'logs/P221_P221_solve_process.json').read_text())
kernel=ctypes.WinDLL('kernel32',use_last_error=True)
kernel.OpenProcess.restype=ctypes.c_void_p
kernel.WaitForSingleObject.argtypes=[ctypes.c_void_p,ctypes.c_uint32]
kernel.CloseHandle.argtypes=[ctypes.c_void_p]
handle=kernel.OpenProcess(0x00100000,False,previous['pid'])
if handle:
 while kernel.WaitForSingleObject(handle,30000)==258:pass
 kernel.CloseHandle(handle)
py('run.py','solve',case)
py('run.py','java','MeshStateAudit',R/'models'/f'{case}.mph',R/'results'/f'{case}_mesh_audit.csv')
py('run.py','java','ExportMovingOptics',R/'models'/f'{case}.mph',R/'results'/f'{case}mapped','80')
py('run.py','java','ExportDrivenFeedback',R/'models'/f'{case}.mph',R/'results'/f'{case}_feedback.csv')
env.update(ROUTE1_CAMERA_ZERO_NS='90',ROUTE1_RADIATION_EXPOSURES_ONLY='1',ROUTE1_RADIATION_MIN_NS='80',ROUTE1_POST_THREADS='16')
py('plot_mapped_fields.py',case+'mapped');py('radiation_native.py',case+'mapped');py('preview_gate_sequence.py',case+'mapped')
