"""Account for the LTE air lines in early renderer rows made before the fix."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from legacy_radiation import Atomic
from transport import ff_bands
from render_kinetics import fb_bands
import argparse,csv

def run(case,first_fixed_index):
    folder=RES/f'{case}_radiation';state=json.loads((folder/'progress.json').read_text());meta=json.loads((RES/f'{case}_fields.json').read_text())
    f=np.memmap(RES/f'{case}_fields.bin',dtype=meta['dtype'],shape=tuple(meta['shape']),mode='r');r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z']);atom=Atomic();changes=[]
    net=json.loads((ROOT/'data/network.json').read_text())
    for k in range(len(state['rows'])):
        row=state['rows'][k]
        native=np.array(f[state['ids'][k]],float);af=native[:9].copy();af[1]=native[34];jl,_=atom.emissivities(af);delta=np.zeros(3);phot=np.zeros(3)
        pop=np.maximum(native[11:19],0);Te=np.maximum(native[19],1);ne=native[1]
        en,pn=ff_bands(1.426e-40*np.sqrt(Te)*ne*native[8]/(4*np.pi),Te);fe,fp,_=fb_bands(pop,ne,Te)
        Le=4*np.pi*volume_integral(en+fe,r,z,native[10]);Lp=4*np.pi*volume_integral(pn+fp,r,z,native[10])
        for line in net['lines']:
            lum=float(volume_integral(H*line['nu']*line['A']*pop[line['up']],r,z,native[10]))
            for bi,(a,b) in enumerate(BANDS):
                if a<=line['wavelength_nm']<=b:Le[bi]+=lum;Lp[bi]+=lum/(H*line['nu'])
        for j,line in zip(jl,atom.lines):
            if line['element']==0:continue
            lum=4*np.pi*float(volume_integral(j,r,z,native[10]))
            for bi,(a,b) in enumerate(BANDS):
                if a<=line['wavelength_air_nm']<=b:delta[bi]+=lum;phot[bi]+=lum/(H*line['nu'])
        old=np.array(row['emitted_W']);row['emitted_W']=(Le+delta).tolist();row['emitted_photons_s']=(Lp+phot).tolist();row['air_luminosity_corrected']=True
        changes.append(float(np.max(abs(Le+delta-old)/np.maximum(old,1e-300))))
    # Run only after renderer finishes, so its progress cannot overwrite this.
    write_json(folder/'progress.json',state);summary=json.loads((folder/'summary.json').read_text());summary['rows']=state['rows'];write_json(folder/'summary.json',summary)
    with (folder/'luminosity.csv').open('w',newline='',encoding='utf-8') as file:
        w=csv.writer(file);w.writerow(['t_ns']+[f'energy_{int(lo)}_{int(hi)}_W' for lo,hi in BANDS]+[f'photons_{int(lo)}_{int(hi)}_s' for lo,hi in BANDS]);w.writerows([[a['t_ns'],*a['emitted_W'],*a['emitted_photons_s']] for a in state['rows']])
    write_json(folder/'air_luminosity_correction.json',dict(first_fixed_index=first_fixed_index,max_fraction_added=max(changes,default=0)))

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('case');p.add_argument('first_fixed_index',type=int);a=p.parse_args();run(a.case,a.first_fixed_index)
