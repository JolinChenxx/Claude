"""Extend the same nuclear material table; preserve every existing table entry."""
from pathlib import Path
import pickle,sys,json,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R/'vendor/pyaneos_reference'))
from pyaneos.phase import aneosv_point
from pyaneos import TCONV
old=R/'data/Cu_HEM_20261110';out=R/'data/Cu_HEM_extended_20261110';out.mkdir(exist_ok=True)
d=np.load(old/'eos.npz');a=json.loads((old/'audit.json').read_text());m=pickle.loads((R/'results/Cu_nuclear_ANEOS_audit/material.pkl').read_bytes());m.no_tension=True;m.invalidate_nt()
extraT=np.geomspace(d['temperatures_K'][-1]*1.03,1e6,80);rows=[]
for T in extraT:
 for rho in d['densities_kg_m3']:
  q,_,_=aneosv_point(float(T/TCONV),float(rho/1000),m)
  row=[np.log10(T),np.log10(rho),q.p*.1,q.e*1e-4-a['e_reference_J_kg'],q.cv*1e-4/TCONV,q.cs*.01,1.,1.,float(q.kpa)]
  if not np.isfinite(row).all() or row[4]<=0 or row[2]<0:raise ValueError(row)
  rows.append(row)
table=np.vstack([d['table'],rows]);np.savez_compressed(out/'eos.npz',table=table,temperatures_K=np.r_[d['temperatures_K'],extraT],densities_kg_m3=d['densities_kg_m3'])
a.update(extension='Same nuclear ANEOS evaluated up to1e6K; no energy extrapolation or temperature clamp; all old rows retained exactly',old_rows_identical=bool(np.array_equal(table[:len(d['table'])],d['table'])),high_temperature_ionization='Must be supplied by native kinetic species, not by this nuclear EOS',shape=[len(d['temperatures_K'])+len(extraT),len(d['densities_kg_m3'])]);(out/'audit.json').write_text(json.dumps(a,indent=2));print(json.dumps(a,indent=2))
