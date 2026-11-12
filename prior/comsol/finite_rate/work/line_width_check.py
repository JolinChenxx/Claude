"""Check the effect of total upper/lower radiative lifetime on green Cu lines."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from render_kinetics import physical_all
from transport import line_coefficients,line_transfer
from rate_tables import G
from numba import set_num_threads
import csv

def run(case):
    set_num_threads(8);meta=json.loads((RES/f'{case}_fields.json').read_text());net=json.loads((ROOT/'data/network.json').read_text())
    f=np.memmap(RES/f'{case}_fields.bin',dtype=meta['dtype'],shape=tuple(meta['shape']),mode='r')
    r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z']);ts=np.array(meta['times_s']);rays=Rays(r,z)
    widths=np.zeros(8)
    for line in net['lines']:widths[line['up']]+=line['A']
    rows=[]
    for ns in [160,240,830]:
        if ns>ts[-1]*1e9+.01:continue
        i=np.argmin(abs(ts*1e9-ns));a=physical_all(np.asarray(f[i],float),r,z);T=rays.sample(a[0]);pop=np.maximum(a[11:19],0)
        v=(rays.sample(a[5]).reshape(rays.shape)*rays.vproj[None,:,:]).reshape(T.shape)
        for line in net['lines']:
            if not 475<=line['wavelength_nm']<=525:continue
            lo,up=line['lo'],line['up'];j,alpha=line_coefficients(pop[lo],pop[up],G[lo],G[up],line['A'],line['nu']);jr,ar=rays.sample(j),rays.sample(alpha)
            vals=[]
            for gamma in (line['A'],widths[lo]+widths[up]):
                rad,thin,tau=line_transfer(jr,ar,T,v,rays.ds,line['nu'],gamma,MCU)
                vals.append(float(rays.image(rad).sum())*(PIXEL*1e-6)**2*OMEGA*1e-8/(H*line['nu']))
            row=dict(numerical_ns=float(ts[i]*1e9),wavelength_nm=line['wavelength_nm'],transition_A_s=line['A'],total_radiative_width_s=float(widths[lo]+widths[up]),old_photons=vals[0],lifetime_width_photons=vals[1],ratio=vals[1]/max(vals[0],1e-300))
            rows.append(row);print(row,flush=True)
    with (RES/f'{case}_line_width_check.csv').open('w',newline='',encoding='utf-8') as file:
        writer=csv.DictWriter(file,fieldnames=list(rows[0]));writer.writeheader();writer.writerows(rows)

if __name__=='__main__':run(sys.argv[1])
