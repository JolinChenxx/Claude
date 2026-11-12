"""Join separately exported pilot/resumed histories without pretending gaps exist."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
import argparse,csv

def run(out,cases):
    metas=[json.loads((RES/f'{c}_fields.json').read_text()) for c in cases];merged=[];last=-1.;chunks=[]
    for c,m in zip(cases,metas):
        if m['coordinates_um']!=metas[0]['coordinates_um'] or m['expressions']!=metas[0]['expressions']:raise ValueError('Export grids differ')
        ts=np.array(m['times_s']);idx=np.where(ts>last+1e-15)[0];chunks.append((c,m,idx));merged.extend(ts[idx].tolist());last=ts[-1]
    meta=metas[0].copy();meta['shape']=list(meta['shape']);meta['times_s']=merged;meta['shape'][0]=len(merged);meta['source_segments']=cases
    with (RES/f'{out}_fields.bin').open('wb') as f:
        for c,m,idx in chunks:
            a=np.memmap(RES/f'{c}_fields.bin',dtype=m['dtype'],shape=tuple(m['shape']),mode='r')
            for i in idx:f.write(a[i].tobytes())
    write_json(RES/f'{out}_fields.json',meta)
    rows=[];last=-1
    for c in cases:
        with (RES/f'{c}_integrals.csv').open() as f:
            for r in csv.DictReader(f):
                if float(r['t_s'])>last+1e-15:rows.append(r);last=float(r['t_s'])
    with (RES/f'{out}_integrals.csv').open('w',newline='') as f:
        w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
    for suffix in ['gas','solid','wall','far']:
        if not all((RES/f'{c}_{suffix}.csv').exists() for c in cases):continue
        lines=[];header=None
        for c,m,idx in chunks:
            source=(RES/f'{c}_{suffix}.csv').read_text().splitlines()
            if header is None:header=source[0]
            lines.extend(source[i+1] for i in idx)
        (RES/f'{out}_{suffix}.csv').write_text(header+'\n'+'\n'.join(lines)+'\n')
    print(out,len(merged),merged[-1])

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('out');p.add_argument('cases',nargs='+');a=p.parse_args();run(a.out,a.cases)
