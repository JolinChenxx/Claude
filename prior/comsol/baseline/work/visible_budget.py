"""Full visible (380--780 nm) LTE emission upper bound, separate from camera bands.

NIST source queries are retained. Uses the unchanged EOS partition/population
closure. Tables exported for COMSOL integrate over the entire physical FE gas
domain; the small camera interpolation window is not the energy audit domain.
"""
from pathlib import Path
import argparse, concurrent.futures, csv, json, time
import numpy as np
import requests
from radiation_s4 import Atomic, EV, K, H, C

ROOT=Path(__file__).resolve().parents[1];DATA=ROOT/'data'
LABELS=['Cu_I','Cu_II','N_I','N_II','O_I','O_II']

def fetch(label):
    original=(DATA/f'{label}_visible_lines.url').read_text().strip()
    url=original.replace('low_w=450','low_w=380').replace('upp_w=550','upp_w=780')
    out=DATA/f'{label}_380_780.tsv'
    if not out.exists():
        response=requests.get(url,timeout=90);response.raise_for_status()
        if 'Aki(s^-1)' not in response.text:raise RuntimeError('No NIST transition table: '+label)
        out.write_text(response.text,encoding='utf8')
    (DATA/f'{label}_380_780.url').write_text(url+'\n')
    return label

def tables():
    with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:
        for label in pool.map(fetch,LABELS):print('SOURCE',label,flush=True)
    atom=Atomic();temps=atom.ts
    cols=[temps];accepted=[]
    for j,label in enumerate(LABELS):
        el=j//2;stage=j%2;power=np.zeros_like(temps);count=0
        for row in csv.DictReader((DATA/f'{label}_380_780.tsv').open(),delimiter='\t'):
            try:
                def val(key):return float(row[key].strip('" []?'))
                wl=val('obs_wl_air(nm)') if row['obs_wl_air(nm)'].strip('" ') else val('ritz_wl_air(nm)')
                A=val('Aki(s^-1)');up=val('Ek(eV)');gu=val('g_k')
                if not 380<=wl<=780 or up*EV>=atom.a['chi'][el,stage]:continue
                sigma=1000/wl;n=1+1e-8*(8060.51+2480990/(132.274-sigma*sigma)+17455.7/(39.32957-sigma*sigma))
                power+=H*C/(wl*1e-9*n)*A*gu*np.exp(-up*EV/(K*temps))/atom.q[el,stage]
                count+=1
            except (ValueError,KeyError):continue
        cols.append(power);accepted.append({'species':label,'transitions':count})
    for el in range(3):
        for stage in range(3):cols.append(atom.q[el,stage])
    # W per particle for six line-emission coefficients; dimensionless nine Q.
    np.savetxt(DATA/'visible_emission_tables.txt',np.column_stack(cols),fmt='%.16g')
    meta={'wavelength_nm':[380,780],'accepted':accepted,'retrieved_epoch':time.time(),
          'columns':['T_K',*['power_'+s for s in LABELS],*[f'Q_{el}_{s}' for el in ['Cu','N','O'] for s in range(3)]],
          'meaning':'Unattenuated LTE spontaneous line emission, W per atom/ion. Integral over full FE gas is an upper bound on escaping visible line energy.',
          'partition':'Identical finite original EOS partitions; no altered thermodynamic table.',
          'limitations':'Species with no available A are omitted; free-bound, molecular bands and higher ion stages are outside this specified radiation closure.'}
    (DATA/'visible_emission_tables.json').write_text(json.dumps(meta,indent=2));print(meta)

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--tables',action='store_true');a=p.parse_args()
    if a.tables:tables()
