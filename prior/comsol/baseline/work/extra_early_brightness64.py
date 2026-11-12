"""Early physical-brightness diagnostic from the extra mesh checkpoint."""
from pathlib import Path
import json,os,subprocess,sys
import numpy as np
from camera_s4 import gate,blur
R=Path(__file__).resolve().parents[1];W=R/'work';RES=R/'results'
def call(*args):subprocess.run([sys.executable,*map(str,args)],cwd=R,check=True)
def main():
    os.environ['S4_POST_THREADS']='8';case='G264EARLY'
    call(W/'run_standalone.py','ExportFields',W/'G264.mph',RES/case,'100000','1','112','184')
    audit=json.loads((RES/'G264_audit.json').read_text(encoding='utf8'))
    audit.update(case=case,source_flow_case='G264',status='postprocessing_subset_only',postprocessing_only_subset=True)
    (RES/f'{case}_audit.json').write_text(json.dumps(audit,indent=2))
    call(W/'radiation_s4.py',case)
    experimental=np.array([0,20,30,50,60]);shift=json.loads((RES/'B64A_camera/calibration.json').read_text())['time_shift_ns'];values={}
    for identity in ['B64A','G64AC2',case]:
        data=np.load(RES/f'{identity}_radiation/radiance.npz');values[identity]=blur(gate(data['times_s']*1e9,data['radiance'][:,0],experimental+shift),1).sum(axis=(1,2))
    differences={}
    for base in ['B64A','G64AC2']:
        relative=abs(values[case]-values[base])/values[base]
        differences[base]={'relative_difference':relative.tolist(),'maximum':float(relative.max())}
    output={'scope':'Five early active physical-brightness gates only. Camera radius/area/grayscale and full S4 coverage are not assessed.',
            'extra_flow_checkpoint':'G264','experimental_times_ns':experimental.tolist(),
            'brightness':{k:v.tolist() for k,v in values.items()},'extra_vs_reference':differences}
    (RES/'extra_early_brightness64.json').write_text(json.dumps(output,indent=2));print(json.dumps(output,indent=2))
if __name__=='__main__':main()
