"""Tabulate actual saved source-reservoir mass/species/enthalpy outflow."""
from pathlib import Path
import sys,json
import numpy as np
R=Path(__file__).resolve().parent
sys.path.insert(0,str(R));from plot_global_plume import load
cases=sys.argv[1:];d=load(cases[0])
for case in cases[1:]:
    q=load(case);keep=d['time_s']<q['time_s'][0]-1e-15;d={n:np.r_[v[keep],q[n]] for n,v in d.items()}
species=json.loads((R/'data/chemistry.json').read_text())['species']+[dict(name='CuT5',M=.063546)]
dens=np.array([d['c.nCuT5'] if s['name']=='CuT5' else d['c.plas.n_w'+s['name']] for s in species])
mass=dens*np.array([s['M'] for s in species])[:,None]/6.02214076e23
negative=float(np.minimum(mass,0).sum());mass=np.maximum(mass,0);fractions=mass/mass.sum(0)
arrays=[d['c.outMassRate'],d['c.outPower'],d['c.outElectronPower'],d['c.maxTarget(c.T)'],d['c.Plaser'],d['c.intSurf(c.Qsurface)'] if 'c.intSurf(c.Qsurface)' in d else d['c.intSurf(Qsurface)']]
names=['sourceMass','sourcePower','sourceElectron','sourceTargetK','sourceLaser','sourceTargetAbsorption']
for s,f in zip(species,fractions):names.append('sourceFlux'+s['name']);arrays.append(d['c.outMassRate']*f)
np.savetxt(R/'data/source_feed_table.txt',np.column_stack([d['time_s'],*arrays]),fmt='%.14e')
report=dict(cases=cases,columns=names,end_ns=float(d['time_s'][-1]*1e9),negative_trace_mass_density_sum_clipped=negative,correction='Negative roundoff species set0 and remaining mass fractions normalized to preserve total source mass exactly',coupling='One-way actual source outflow; no outer pressure/laser feedback; no extension outside saved interval')
(R/'data/source_feed_manifest.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
