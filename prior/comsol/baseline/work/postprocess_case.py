"""Reproducible saved-solution export and camera comparison entry point."""
from pathlib import Path
import argparse,json,subprocess,sys,time
ROOT=Path(__file__).resolve().parents[1];WORK=ROOT/'work';RES=ROOT/'results'
def call(*args):subprocess.run([sys.executable,*map(str,args)],cwd=ROOT,check=True)
def process(case,reference=None,diagnostic=False,stride=1):
 audit=json.loads((RES/f'{case}_audit.json').read_text())
 if audit['status']!='passed' and not diagnostic:raise RuntimeError('Case has not passed numerical audit; use --diagnostic to preserve honest labeling.')
 p=RES/f'{case}_fields';meta=p.with_name(p.name+'.json');binary=p.with_name(p.name+'.bin')
 complete=False
 if meta.exists() and binary.exists():
  m=json.loads(meta.read_text());nbytes=4
  for n in m['shape']:nbytes*=n
  complete=binary.stat().st_size==nbytes and len(m['times_s'])==audit['metadata']['saved_times']
 if not complete:call(WORK/'run_standalone.py','ExportFields',WORK/f'{case}.mph',RES/case)
 call(WORK/'radiation_s4.py',case,'--stride',stride)
 camera=[WORK/'camera_s4.py',case]
 if reference:camera+=['--reference',RES/f'{reference}_camera/calibration.json']
 call(*camera);call(WORK/'plot_s4.py',case);call(WORK/'plot_metrics.py',case)
 if (RES/f'{case}_visible_budget.csv').exists():call(WORK/'photon_upper_bound.py',case)
 call(WORK/'plot_photon_scale.py',case)
 call(WORK/'plot_conservation.py',case)
 call(WORK/'run_standalone.py','CreateViews',WORK/f'{case}.mph',WORK/f'{case}_view.mph',f'{case} | LTE S4 | '+('DIAGNOSTIC - numerical audit not passed' if diagnostic else 'numerical audit passed; S4 comparison assessed separately'))
 call(WORK/'run_standalone.py','ExportSettings',WORK/f'{case}.mph',RES/f'{case}_settings.json')
 call(WORK/'run_figure_qa.py',case)
 report={'case':case,'numerical_status':audit['status'],'diagnostic':diagnostic,'reference_camera':reference,'temporal_export_stride':stride,'completed_epoch':time.time()}
 (RES/f'{case}_postprocess.json').write_text(json.dumps(report,indent=2))
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('case');p.add_argument('--reference');p.add_argument('--diagnostic',action='store_true');p.add_argument('--stride',type=int,default=1);a=p.parse_args();process(a.case,a.reference,a.diagnostic,a.stride)
