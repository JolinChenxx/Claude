"""Recover original LTE Cu emissivity with identical level populations."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from legacy_radiation import Atomic,planck
from transport import line_coefficients
from rate_tables import G,ENERGIES

def run():
    atom=Atomic();net=json.loads((ROOT/'data/network.json').read_text());f,r,z,t=fields();rows=[]
    for ns in [120,150,240]:
        i=np.argmin(abs(t*1e9-ns));a=np.asarray(f[i],float);T,ne,Y,rho,xc=a[:5]
        n0,n1,q=atom.populations(T,ne,rho,np.clip(Y,0,1),xc);old,_=atom.emissivities(a[:9]);q7=(G[:,None,None]*np.exp(-ENERGIES[:,None,None]*EV/(K*T))).sum(0)
        joldsum=0.;jmatchsum=0.;jtruncsum=0.;opacityerr=[]
        for line in net['lines']:
            if not 475<=line['wavelength_nm']<=525:continue
            li=min((k for k,x in enumerate(atom.lines) if x['element']==0 and x['stage']==0),key=lambda k:abs(atom.lines[k]['wavelength_air_nm']-line['wavelength_nm']))
            ref=atom.lines[li];lo,up=line['lo'],line['up'];nu=ref['nu']
            # Full original partition and original upper energy; lower energy is
            # inferred from the exact transition frequency for Kirchhoff recovery.
            nup=n0[0]*ref['gu']*np.exp(-ref['upper_eV']*EV/(K*T))/q[0,0]
            nlo=nup*G[lo]/G[up]*np.exp(np.minimum(H*nu/(K*T),700))
            j,alpha=line_coefficients(nlo,nup,G[lo],G[up],ref['A'],nu)
            jl=n0[0]*G[up]*np.exp(-ENERGIES[up]*EV/(K*T))/q7*H*line['nu']*line['A']/(4*np.pi)
            joldsum+=float(volume_integral(old[li],r,z,a[10]));jmatchsum+=float(volume_integral(j,r,z,a[10]));jtruncsum+=float(volume_integral(jl,r,z,a[10]))
            active=j>j.max()*1e-8;expected=j/np.maximum(planck(nu,T),1e-280)
            opacityerr.append(float(np.max(abs(alpha[active]/expected[active]-1))))
        rows.append(dict(numerical_ns=float(ns),matched_emissivity_relative_error=float(abs(jmatchsum/joldsum-1)),
            matched_opacity_relative_error=max(opacityerr),seven_state_LTE_to_original_emission=float(jtruncsum/joldsum)))
    assert max(x['matched_emissivity_relative_error'] for x in rows)<1e-12
    assert max(x['matched_opacity_relative_error'] for x in rows)<1e-10
    write_json(RES/'R2_LTE_adapter_check.json',dict(frames=rows,
        scope='The shared Einstein interface recovers the original four green Cu lines when given identical LTE populations. Seven-state truncation is separately quantified; it is not asserted to reproduce the full old EOS or all omitted lines.'))
    print(rows)

if __name__=='__main__':run()
