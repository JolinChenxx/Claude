"""Estimate omitted conduction turnover on a solved two-temperature field."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from render_kinetics import physical_all

def run(case):
    meta=json.loads((RES/f'{case}_fields.json').read_text());f=np.memmap(RES/f'{case}_fields.bin',dtype=meta['dtype'],shape=tuple(meta['shape']),mode='r');r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z']);ts=np.array(meta['times_s']);rows=[]
    for ns in [120,140,160,240,350,500,900]:
        if ns>ts[-1]*1e9+.01:continue
        it=np.argmin(abs(ts*1e9-ns));a=physical_all(np.array(f[it],float),r,z);Tg=a[0];Te=np.maximum(a[19],1);ne=a[1];n=a[11:19];air=np.maximum(a[3]-a[33],0)/(28.97*1.66053906660e-27)
        ven=np.sqrt(8*K*Te/(np.pi*ME));nen=(air+n[:7].sum(0))*1e-19*ven;ln=np.maximum(2,23-np.log(np.sqrt(np.maximum(ne,1)*1e-6)/np.maximum(Te/11604.51812,1e-4)**1.5));nei=2.91e-12*ne*ln/np.maximum(Te/11604.51812,1e-4)**1.5
        ke=2.5*K*K*ne*Te/(ME*(nen+nei+1));dz,dr=np.gradient(Te,z*1e-6,r*1e-6);grad=np.sqrt(dz*dz+dr*dr);sat=.3*ne*K*Te*ven;ke/=1+ke*grad/np.maximum(sat,1e-20)
        qr=-ke*dr;qz=-ke*dz;divq=np.gradient(qr*r[None,:],r*1e-6,axis=1)/r[None,:]+np.gradient(qz,z*1e-6,axis=0)
        # Unresolved electron-vacuum temperature spikes are excluded explicitly.
        active=(ne>1e18)&(a[33]>a[33].max()*1e-4);U=1.5*K*ne*Te;power=float(volume_integral(abs(divq)*active,r,z));energy=float(volume_integral(U*active,r,z));stored=float(volume_integral((a[30]+a[32])*active,r,z))
        rows.append(dict(numerical_ns=float(ts[it]*1e9),absolute_conductive_turnover_W=power,active_electron_J=energy,electron_turnover_time_ns=energy/max(power,1e-300)*1e9,turnover_in_40ns_relative_to_active_bulk_energy=power*40e-9/max(stored,1e-300)))
    write_json(RES/f'{case}_conduction_estimate.json',dict(scope='Diagnostic divergence on interpolated saved fields; not a solved conduction correction. Active ne>1e18 m^-3 and Cu density>1e-4 peak excludes vacuum artifacts; boundary/interface accuracy is limited.',rows=rows));print(rows)

if __name__=='__main__':run(sys.argv[1])
