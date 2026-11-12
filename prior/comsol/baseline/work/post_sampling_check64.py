"""Early active-gate sensitivity to FE export sampling and ray quadrature.

No flow solve, no camera fit, no claim about late inactive physical brightness.
"""
from pathlib import Path
import json,subprocess,sys,os
import numpy as np
from camera_s4 import gate,blur
R=Path(__file__).resolve().parents[1];W=R/'work';RES=R/'results'

def call(*args):subprocess.run([sys.executable,*map(str,args)],cwd=R,check=True)

def main():
    os.environ['S4_POST_THREADS']='8'
    calibration=json.loads((RES/'B64A_camera/calibration.json').read_text());experimental=np.array([0,20,30,50,60])
    nodes=experimental+calibration['time_shift_ns'];values={};report={}
    for case in ['B64A','G64A']:
        diagnostic=case+'_PP64'
        call(W/'run_standalone.py','ExportFields',W/f'{case}.mph',RES/diagnostic,'100000','.5','112','184')
        audit=json.loads((RES/f'{case}_audit.json').read_text())
        audit.update(case=diagnostic,source_flow_case=case,source_flow_audit_status=audit['status'],status='postprocessing_subset_only',postprocessing_only_subset=True)
        (RES/f'{diagnostic}_audit.json').write_text(json.dumps(audit,indent=2))
        call(W/'radiation_s4.py',diagnostic,'--ray-factor','.5')
        current=[]
        for identity in [case,diagnostic]:
            data=np.load(RES/f'{identity}_radiation/radiance.npz')
            current.append(blur(gate(data['times_s']*1e9,data['radiance'][:,0],nodes),1).sum(axis=(1,2)))
        error=abs(current[1]-current[0])/np.maximum(abs(current[0]),1e-100)
        values[case]=current
        report[case]={'standard':current[0].tolist(),'denser':current[1].tolist(),'relative_change':error.tolist(),'max_relative_change':float(error.max())}
        print('SAMPLING_CHECK',case,json.dumps(report[case]),flush=True)
    original=abs(values['G64A'][0]-values['B64A'][0])/values['B64A'][0]
    denser=abs(values['G64A'][1]-values['B64A'][1])/values['B64A'][1]
    output={'scope':'Only five active early physical-brightness S4 gates; identical saved flow solutions, 2x denser export grid per axis and 2x ray quadrature; no gain or saturation.',
            'experimental_times_ns':experimental.tolist(),'numerical_centres_ns':nodes.tolist(),'cases':report,
            'original_flow_grid_relative_difference':original.tolist(),'denser_post_flow_grid_relative_difference':denser.tolist(),
            'max_denser_post_flow_grid_relative_difference':float(denser.max()),
            'flow_mesh_difference_still_exceeds_10pct':bool(denser.max()>.1)}
    (RES/'post_sampling_check64.json').write_text(json.dumps(output,indent=2));print(json.dumps(output,indent=2))

if __name__=='__main__':main()
