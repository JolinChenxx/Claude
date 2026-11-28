"""Fixed geometry counterpart to the saved thermal-feed spatial test."""
from pathlib import Path
import os,sys,subprocess,json
R=Path(__file__).resolve().parent;case='P223'
env=os.environ.copy();env.update(ROUTE1_THREADS='32',ROUTE1_NUMA='1',ROUTE1_LOG_TAG=case)
def py(script,*args):subprocess.run([sys.executable,'-X','utf8',str(R/script),*map(str,args)],env=env,check=True)
py('run.py','java','PrepareFixedDriven',R/'models/P222_ready.mph',R/'models/P223_ready.mph','125[ns]')
(R/'results/P223_configuration.json').write_text(json.dumps({'source':'P222_ready','initial_state':'cold, recomputed','geometry':'fixed plane, zero copper material velocity','thermal_and_evaporation_history':'same computed C201, actual support to133ns','surface_states':'same Saha-Langmuir/Boltzmann source asP222','native_Plasma':'full spatial transport, potential and explicit sheath retained','purpose':'Separate prescribed surface-motion/ALE stiffness from thermal-state feed; not a real jet prediction','energy_feedback':'gas-to-copper iteration for thermal state energy pending','time_storage':'all accepted steps'},indent=2))
py('run.py','solve',case)
py('run.py','java','ExportMovingOptics',R/'models'/f'{case}.mph',R/'results'/f'{case}mapped','80')
py('run.py','java','ExportDrivenFeedback',R/'models'/f'{case}.mph',R/'results'/f'{case}_feedback.csv')
d=json.loads((R/'results'/f'{case}mapped_fields.json').read_text());(R/'results'/f'{case}_stage.json').write_text(json.dumps({'case':case,'saved_end_ns':d['times_s'][-1]*1e9,'fixed_plane':True}))
env.update(ROUTE1_CAMERA_ZERO_NS='90',ROUTE1_RADIATION_EXPOSURES_ONLY='1',ROUTE1_RADIATION_MIN_NS='80',ROUTE1_POST_THREADS='16')
py('plot_mapped_fields.py',case+'mapped');py('radiation_native.py',case+'mapped');py('preview_gate_sequence.py',case+'mapped')
