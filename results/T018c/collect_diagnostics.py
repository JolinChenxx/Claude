"""Archive completed diagnostics without rerunning computations or changing missing data."""
import argparse
import csv
import hashlib
import json
import math
import shutil
from datetime import datetime
from pathlib import Path

ap = argparse.ArgumentParser()
ap.add_argument('iteration', choices=['it0', 'it1', 'it2'])
a = ap.parse_args()
repo = Path(__file__).resolve().parents[2]
job = f'T018c_post_{a.iteration}_01'
jobdir = Path('D:/sim_runs') / job
summary = json.loads((jobdir / 'postprocess_summary.json').read_text(encoding='utf-8-sig'))
metadata = json.loads((jobdir / 'job.json').read_text(encoding='utf-8-sig'))
assert f'/results/T018c/postprocessing/run_{a.iteration}.py' in metadata['cmd'].replace('\\', '/')
now = datetime.now().astimezone().isoformat()
completed = []
def digest(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()
def copy(p, dst):
    before = digest(p)
    shutil.copy2(p, dst)
    assert before == digest(dst) == digest(p)
    return before

for member in ['high_thin_G0', 'high_trap_G0']:
    run = Path('D:/sim_runs/T018c') / member / a.iteration
    dst = repo / 'results/T018c' / member / a.iteration / 'diagnostics'
    dst.mkdir(parents=True, exist_ok=True)
    times = [float(line.split()[1]) for line in (run / 'dbl.out').read_text().splitlines() if line.strip() and not line.startswith('#')]
    assert len(times) == 321 and times[-1] == 800.0
    out = {'collected': now, 'member': member, 'iteration': a.iteration,
           'job': job, 'job_metadata': metadata, 'files': {}}
    for filename, name in [('diag_emission', 'emission'), ('core_energy', 'core_energy'), ('shock_track', 'shock_track')]:
        commands = [c for c in summary['commands'] if c['name'] == f'{member}_{name}']
        assert len(commands) == 1 and commands[0]['returncode'] == 0
        command = commands[0]
        args = command['command']
        assert Path(args[2]) == run
        assert Path(args[1]) == repo / 'scripts' / (filename + '.py')
        src = run / 'diagnostics' / (filename + '.csv')
        assert Path(args[args.index('--out')+1]) == src
        if name != 'shock_track':
            assert Path(args[args.index('--tables')+1]) == Path('D:/sim_runs/tables/v1c')
        if name == 'core_energy':
            assert args[args.index('--nec')+1] == member.split('_')[1]
        with src.open(encoding='utf-8-sig', newline='') as f:
            rows = list(csv.DictReader(f))
        blanks = {k: 0 for k in rows[0]}
        for row in rows:
            for k, v in row.items():
                if v.strip() == '' or v.lower() == 'nan':
                    blanks[k] += 1
                else:
                    assert math.isfinite(float(v)), (src, k, v)
        key = 't_snap_ns' if name == 'emission' else 't_ns'
        actual = [float(r[key]) for r in rows]
        if name == 'emission':
            req = [300., 500., 700., 1000.]
            assert [float(r['t_req_ns']) for r in rows] == req
            assert args[args.index('--times')+1:args.index('--out')] == ['300','500','700','1000']
            expected = [min(times, key=lambda t: abs(t - requested)) for requested in req]
            for row in rows:
                for prefix in ['frac ', 'T ', 'Y ']:
                    assert math.isclose(sum(float(v) for k,v in row.items() if k.startswith(prefix)), 1., abs_tol=1e-8)
            assert not any(blanks.values())
        else:
            expected = times[::4] if name == 'core_energy' else times
        assert len(actual) == len(expected) and all(math.isclose(x,y,abs_tol=1e-9) for x,y in zip(actual,expected))
        data = {'rows': len(rows), 'sha256': copy(src, dst / src.name),
                'all_present_values_finite': True, 'blank_counts': blanks,
                'snapshot_times_match': True, 'first_t_ns': actual[0], 'last_t_ns': actual[-1],
                'command': command, 'log_sha256': copy(Path(command['log']), dst / Path(command['log']).name)}
        if name == 'emission':
            data['selected_values'] = [{k:float(r[k]) for k in ['t_req_ns','t_snap_ns','P_total_W_sr','T_emw_K','frac Cu I']} for r in rows]
        elif name == 'core_energy':
            no_core = [float(r['t_ns']) for r in rows if float(r['M_Cu']) == 0.]
            assert all((r['E_int'] == '') == (float(r['M_Cu']) == 0.) for r in rows)
            data['no_core_t_ns'] = no_core
            data['missing_meaning'] = 'No Y>0.5 core at the listed times; adjacent derivatives consequently missing. Original values retained.'
        else:
            data['last_valid_by_axis'] = {k: next({'t_ns': float(r['t_ns']), 'R_um': float(r[k])} for r in reversed(rows) if r[k] and r[k].lower() != 'nan') for k in ['R_axis_um', 'R_surf_um']}
            data['missing_meaning'] = 'No pressure-threshold crossing at default threshold 1.2; missing values retained, not interpolated.'
        out['files'][filename] = data
        completed.append(command)
    p = dst / 'validation.json'
    p.write_text(json.dumps(out, indent=2, allow_nan=False) + '\n', encoding='utf-8')
    assert json.loads(p.read_text()) == out
    print(json.dumps({'member':member,'files':{k:{'rows':v['rows'],'missing':{x:y for x,y in v['blank_counts'].items() if y}} for k,v in out['files'].items()}}))
snap = repo / 'results/T018c/postprocessing' / f'diagnostics_{a.iteration}_completed'
snap.mkdir(exist_ok=True)
(snap / 'summary.json').write_text(json.dumps({'collected':now,'commands':completed}, indent=2) + '\n', encoding='utf-8')
