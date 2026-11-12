from pathlib import Path
import argparse,json,subprocess,os
from common import ROOT,runtime_env,atomic_json

DEFAULT=dict(mass_ng=5.0,energy_uj=40.,waist_um=22.,kinetic_frac=.02,
             tail_frac=.15,tail_ns=70.,line_width_nm=.2,cooling_scale=1.,source_height_um=6.)
LABELS=list(DEFAULT)

def write_case(folder,params=None,level='coarse',tstop=1000):
    p=DEFAULT.copy();p.update(params or {})
    if p['mass_ng']*6.+p['energy_uj']>400.:raise ValueError('Energy exceeds 0.4 mJ including vaporization')
    folder=Path(folder);folder.mkdir(exist_ok=True,parents=True)
    scale={'smoke':.5,'coarse':1,'medium':2,'fine':4}[level]
    # Source region 0..120 um, main plume region 120..720/900 um, outer ambient buffer.
    nr=[max(8,round(40*scale)),max(12,round(100*scale)),max(24,round(48*scale))]
    nz=[max(8,round(40*scale)),max(12,round(130*scale)),max(24,round(48*scale))]
    # Stretch only outside the observed plume. Axis and substrate boundaries are explicit.
    ini=f'''[Grid]
X1-grid 3 0.0 {nr[0]} u 120.0 {nr[1]} u 720.0 {nr[2]} s 1800.0
X2-grid 3 0.0 {nz[0]} u 120.0 {nz[1]} u 900.0 {nz[2]} s 2100.0
X3-grid 1 0.0 1 u 1.0
[Time]
CFL 0.25
CFL_par 0.25
CFL_max_var 1.05
tstop {tstop}
first_dt 0.0001
[Solver]
Solver hllc
[Boundary]
X1-beg axisymmetric
X1-end outflow
X2-beg reflective
X2-end outflow
X3-beg outflow
X3-end outflow
[Static Grid Output]
uservar 1 temperature
dbl 100.0 -1 single_file
flt 5.0 -1 single_file
vtk -1.0 -1 single_file
tab -1.0 -1
ppm -1.0 -1
png -1.0 -1
log 200
analysis 5.0 -1
[Parameters]

'''
    for key in LABELS:ini+=f'{key.upper()} {p[key]:.12g}\n'
    (folder/'pluto.ini').write_text(ini)
    atomic_json(folder/'params.json',dict(parameters=p,level=level,tstop_ns=tstop,
        total_energy_budget_uj=p['mass_ng']*6.+p['energy_uj'],model_version='cuair-frozen-species-v1'))
    return p

def launch(folder,ranks=8,restart=False):
    folder=Path(folder).resolve();log=(folder/'console.log').open('a')
    exe=ROOT/'solver/pluto.exe'
    cmd=['C:/Program Files/Microsoft MPI/Bin/mpiexec.exe','-n',str(ranks),'-wdir',str(folder),str(exe)]
    if restart:
        from budgets import prepare_restart
        cmd+=['-restart',str(prepare_restart(folder))]
    proc=subprocess.Popen(cmd,cwd=folder,env=runtime_env(),stdout=log,stderr=subprocess.STDOUT,
          creationflags=subprocess.CREATE_NO_WINDOW)
    log.close();return proc

if __name__=='__main__':
    ap=argparse.ArgumentParser();ap.add_argument('--id',default='smoke');ap.add_argument('--ranks',type=int,default=2)
    ap.add_argument('--level',default='smoke');ap.add_argument('--tstop',type=float,default=20);a=ap.parse_args()
    folder=ROOT/'runs'/a.id;write_case(folder,level=a.level,tstop=a.tstop)
    p=launch(folder,a.ranks);print('launched',p.pid,flush=True);rc=p.wait();print('exit',rc)
    if rc:print((folder/'console.log').read_text(errors='replace')[-5000:])
    raise SystemExit(rc)
