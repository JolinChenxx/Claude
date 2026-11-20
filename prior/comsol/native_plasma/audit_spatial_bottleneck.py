"""Read-only diagnostic of saved spatial fields near the copper surface."""
from pathlib import Path
import sys,json,numpy as np
R=Path(__file__).resolve().parent
case=sys.argv[1]
d=json.loads((R/'results'/f'{case}_fields.json').read_text())
a=np.memmap(R/'results'/f'{case}_fields.bin',dtype=d['dtype'],mode='r',shape=tuple(d['shape']))
r=np.array(d['r_um']);z=np.array(d['z_um']);rr,zz=np.meshgrid(r,z)
def field(frame,name):return np.asarray(a[frame,d['expressions'].index(name)],dtype=float)
out=[]
for frame in sorted(set([0,len(a)//2,len(a)-1])):
    ne=field(frame,'plas.ne');te=field(frame,'TeK')
    with np.errstate(invalid='ignore',divide='ignore'):
        debye=np.sqrt(8.8541878128e-12*1.380649e-23*te/(1.602176634e-19**2*ne))
    sample={'time_ns':d['times_s'][frame]*1e9,'regions':{}}
    for name,mask in [('near_wall',(zz<.1)&(rr<30)),('plume',(zz>=.1)&(zz<300)&(rr<300))]:
        item={}
        for n,v in [('ne_m3',ne),('Te_K',te),('Debye_nm',debye*1e9)]+[(n,field(frame,n)) for n in ['Tg','rhoCopper','pPl','plas.V','Qlaser','jline3']]:
            valid=mask&np.isfinite(v)
            if not valid.any():continue
            k=np.unravel_index(np.nanargmax(np.where(valid,v,np.nan)),v.shape)
            item[n]={'min':float(v[valid].min()),'max':float(v[valid].max()),'maximum_r_and_wall_distance_um':[float(rr[k]),float(zz[k])]}
        sample['regions'][name]=item
    out.append(sample)
report={'case':case,'note':'Sampled actual fields. Distance measured from computed copper surface. Debye length is diagnostic; mesh resolution requires normal element size, not isotropic h.','snapshots':out}
(R/'results'/f'{case}_bottleneck_audit.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report))
