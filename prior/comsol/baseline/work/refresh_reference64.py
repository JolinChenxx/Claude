"""Re-export and refresh the preserved 6.2 solution in a distinct result namespace."""
from pathlib import Path
import json,shutil,subprocess,sys,time
import numpy as np
ROOT=Path(__file__).resolve().parents[1];W=ROOT/'work';R=ROOT/'results';CASE='H15R64'
def call(*a):subprocess.run([sys.executable,*map(str,a)],cwd=W,check=True)
def main():
    model=W/(CASE+'.mph')
    if not model.exists():shutil.copy2(W/'H15.mph',model)
    call(W/'run_standalone.py','ExportS4',model,R/CASE)
    call(W/'audit_s4.py',CASE)
    checks={}
    for suffix in ['gas','solid','wall','wallmax','photons','far','ale','alemax','max','fronts']:
        old=np.loadtxt(R/f'H15_{suffix}.csv',delimiter=',',skiprows=1,ndmin=2)
        new=np.loadtxt(R/f'{CASE}_{suffix}.csv',delimiter=',',skiprows=1,ndmin=2)
        if old.shape!=new.shape:raise RuntimeError('Migration export shape changed '+suffix)
        scale=np.maximum(np.max(np.abs(old),axis=0),1e-100)
        err=float(np.max(np.abs(old-new)/scale));checks[suffix]=err
        if not np.isfinite(err) or err>1e-8:raise RuntimeError('Migration export changed '+suffix+': '+str(err))
    (R/'migration_integral_check64.json').write_text(json.dumps({'passed':True,'normalized_errors':checks,'source':'H15 computed in 6.2; loaded/rebound and exported in 6.4','completed_epoch':time.time()},indent=2))
    for suffix in ['fields.json','fields.bin','visible_budget.csv','visible_summary.json']:
        dest=R/f'{CASE}_{suffix}';src=R/f'H15_{suffix}'
        if not dest.exists():
            if suffix=='fields.bin':dest.hardlink_to(src)
            else:shutil.copy2(src,dest)
    (R/f'{CASE}_provenance.json').write_text(json.dumps({'solution_origin':'COMSOL 6.2 H15','loaded_and_exported_with':'6.4.0.429','flow_recomputed':False,'field_export_reused_after_integral_check':True},indent=2))
    call(W/'postprocess_case.py',CASE)
    req=ROOT/'main_post_refresh_required.json'
    if req.exists():
        d=json.loads(req.read_text());d.update(reference_case=CASE,completed_epoch=time.time(),output_namespace=CASE,flow_origin='6.2')
        (ROOT/'main_post_refresh_completed.json').write_text(json.dumps(d,indent=2));req.unlink()
    print('REFERENCE_REFRESH_COMPLETE',CASE,flush=True)
if __name__=='__main__':main()
