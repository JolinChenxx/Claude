"""Quantify partition-cutoff impact without changing the validated EOS."""
from pathlib import Path
import csv,json
from fractions import Fraction
import numpy as np
ROOT=Path(__file__).resolve().parents[1];D=ROOT/'data';a=np.load(D/'atomic_levels.npz');EV=1.602176634e-19;K=1.380649e-23
def clean(v):return v.strip().removeprefix('="').removesuffix('"').strip('[]()? ')
out={}
for j,el in enumerate(['Cu','N','O']):
 for s,stage in enumerate(['I','II']):
  E=[];g=[]
  for row in csv.DictReader((D/f'{el}_{stage}_levels.csv').open()):
   try:
    energy=float(clean(row['Level (eV)']))*EV;gg=2*float(Fraction(clean(row['J'])))+1
    if energy<a['chi'][j,s]:E.append(energy);g.append(gg)
   except (ValueError,KeyError,ZeroDivisionError):pass
  E=np.array(E);g=np.array(g);ts=np.array([293.15,2000,4000,6000,7000,10000,20000])
  q=(g[:,None]*np.exp(-E[:,None]/(K*ts))).sum(0);qold=(a[f'g{j}{s}'][:,None]*np.exp(-a[f'e{j}{s}'][:,None]/(K*ts))).sum(0)
  out[f'{el}_{stage}']={'temperatures_K':ts.tolist(),'full_bound_to_original_partition':(q/qold).tolist(),'original_levels':len(a[f'e{j}{s}']),'all_bound_levels':len(E)}
(D/'partition_cutoff_audit.json').write_text(json.dumps(out,indent=2));print(json.dumps(out['Cu_I'],indent=2))
