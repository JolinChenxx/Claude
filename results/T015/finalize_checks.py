"""Validate and summarize completed T015 outputs without changing scientific results."""
from pathlib import Path
import csv
import datetime
import hashlib
import json
import shutil
import numpy as np
import pandas as pd

root = Path('D:/sim_runs/T015')
out = Path('results/T015')
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
registry = [r for r in csv.DictReader(Path('results/run_registry.csv').open()) if r['batch'] == 'T015']
assert len(registry) == 14 and all(int(r['returncode']) == 0 for r in registry)
runs, copies, omitted, neg, missing = [], [], [], [], []
array_count = 0
for r in registry:
    rel = Path(r['run']) / ('it' + r['picard_it'])
    raw, dst = root / rel, out / rel
    b = pd.read_csv(dst / 'budget.csv')
    assert np.isfinite(b.to_numpy()).all()
    first, last = b.iloc[0], b.iloc[-1]
    assert last.t_ns == (800 if r['run'].startswith('high') else 1300)
    energy_in = last.E_gas_abs + last.E_src
    residual = last.E_internal + last.E_kinetic - first.E_internal - first.E_kinetic + last.E_rad + last.E_wall - energy_in
    count = int((dst / 'consToPrim_count.txt').read_text())
    runs.append(dict(member=r['run'], iteration=int(r['picard_it']), rc=int(r['returncode']), seconds=float(r['seconds']), ConsToPrim=count,
                     E_gas_abs_J=last.E_gas_abs, E_rad_J=last.E_rad, E_wall_J=last.E_wall, M_Cu_kg=last.M_Cu,
                     residual_J=residual, residual_percent=100*residual/energy_in))
    summary = json.loads((dst / 'image/image_summary.json').read_text())
    assert len(summary) == 18
    for key, values in summary.items():
        if values['P1_tau_ns'] < 0: neg.append(dict(run=str(rel), scenario=key, tau=values['P1_tau_ns']))
        m = pd.read_csv(dst / 'image' / ('metrics_' + key + '.csv'))
        assert np.isfinite(m[['t_ns', 'L_radiance']].to_numpy()).all()
        if not np.isfinite(values['P2_dt10_ns']):
            t, v = m.t_ns.to_numpy(), m.L_radiance.to_numpy()
            threshold = np.interp(300, t, v) * .1
            assert not np.any(v[t > 300] <= threshold)
            missing.append(dict(run=str(rel), scenario=key, lower_bound_ns=float(t.max()-300)))
    for archive in list((raw/'image').glob('frames_*.npz')) + [raw/'surface_transmission.npz']:
        if archive.exists():
            with np.load(archive) as data:
                for k in data.files:
                    a=data[k]
                    if np.issubdtype(a.dtype, np.number): assert np.isfinite(a).all(), str(archive)
            array_count += 1
    for f in dst.rglob('*'):
        if not f.is_file(): continue
        relative=f.relative_to(dst)
        source=raw/('source/source_meta.json' if str(relative)=='source_meta.json' else relative)
        if source.exists():
            assert sha(f)==sha(source),str(f)
            copies.append(dict(file=str(f),sha256=sha(f)))
    # Omit only verified duplicate arrays copied by the collector; originals remain in raw storage.
    duplicate=dst/'surface_transmission.npz'
    if duplicate.exists():
        assert duplicate.resolve().is_relative_to(out.resolve())
        assert sha(duplicate)==sha(raw/'surface_transmission.npz')
        omitted.append(dict(raw=str(raw/'surface_transmission.npz'),bytes=duplicate.stat().st_size,sha256=sha(duplicate)))
        duplicate.unlink()

job=Path('D:/sim_runs/T015img5')
meta=json.loads((job/'job.json').read_text(encoding='utf-8-sig'))
start=datetime.datetime.fromisoformat(meta['started']).astimezone()
end=datetime.datetime.fromtimestamp((job/'exitcode.txt').stat().st_mtime).astimezone()
assert int((job/'exitcode.txt').read_text())==0
completion=dict(returncode=0,started=start.isoformat(),finished=end.isoformat(),wall_seconds=(end-start).total_seconds())
for name in ('stdout.log','stderr.log'): shutil.copy2(job/name,out/('imaging5_'+name))
(out/'imaging5_completion.json').write_text(json.dumps(completion,indent=2)+'\n')
image_rc=[]
for jobid in ('T015img1','T015img2','T015img3','T015img4','T015img5'):
    j=Path('D:/sim_runs')/jobid
    assert int((j/'exitcode.txt').read_text())==0
    image_rc += [line for line in (j/'stdout.log').read_text().splitlines() if line.startswith('rc ')]
assert len(image_rc)==14 and all(line.startswith('rc 0 ') for line in image_rc)
assert len(set(image_rc))==14
pd.DataFrame(runs).to_csv(out/'run_summary.csv',index=False)
(out/'run_registry.csv').write_text(Path('results/run_registry.csv').read_text().splitlines()[0]+'\n'+'\n'.join(line for line in Path('results/run_registry.csv').read_text().splitlines()[1:] if ',T015,' in line)+'\n')
metrics=pd.read_csv(out/'metrics_table.csv');assert len(metrics)==252
grades={k:metrics[k].value_counts().to_dict() for k in ('P1','P2','P3')}
validation=dict(solver_runs=14,imaging_runs=14,all_rc_zero=True,verified_copies=len(copies),finite_array_archives=array_count,
                negative_P1=neg,P2_no_crossing=missing,grades=grades,total_ConsToPrim=sum(r['ConsToPrim'] for r in runs),
                max_abs_residual_percent=max(abs(r['residual_percent']) for r in runs),imaging_results=image_rc)
for name,value in [('output_validation.json',validation),('copy_verification.json',copies),('omitted_raw_arrays.json',omitted)]:
    (out/name).write_text(json.dumps(value,indent=2)+'\n')
size=sum(p.stat().st_size for p in out.rglob('*') if p.is_file());assert size<=20_000_000,size
(out/'package_verification.json').write_text(json.dumps(dict(bytes_before_this_record=size,limit_bytes=20_000_000,validator_sha256=sha(Path(__file__))),indent=2)+'\n')
print(json.dumps(dict(total_ConsToPrim=validation['total_ConsToPrim'],negative_P1=len(neg),P2_no_crossing=len(missing),grades=grades,max_abs_residual_percent=validation['max_abs_residual_percent'],package_bytes=size,omitted_bytes=sum(r['bytes'] for r in omitted)),indent=2))
print(pd.DataFrame(runs).to_markdown(index=False,floatfmt='.6g'))
