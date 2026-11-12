"""Cu+ through Cu4+ extension; independent atomic data, no S4 fitting."""
from pathlib import Path
import json
import numpy as np
from scipy.constants import e,k,m_e,h,c,pi
from numpy.polynomial.laguerre import laggauss
R=Path(__file__).resolve().parent;D=R/'data'
T=np.geomspace(.015,100,501);z=np.geomspace(1e-6,4000,6000);x,w=laggauss(64)
data=[];names=[];checks=[]
for charge,I,gl,gu,q in [(1,20.29239,1,6,10),(2,36.841,6,9,9),(3,57.38,9,10,8)]:
    sig=4.5e-18*q*np.log((z+I)/I)/((z+I)*I)
    scaled=np.array([np.sqrt(8*e/(pi*m_e))*np.trapz(sig*(z+I)*np.exp(-z/t),z)/t**1.5 for t in T])
    ion=scaled*np.exp(-I/T);rec=scaled/((2*gu/gl)*(2*pi*m_e*e*T/h**2)**1.5)
    sigma0=6.3e-22*np.sqrt(13.605693/I);nu0=I*e/h;rr=[];mean=[]
    for t in T:
        nu=nu0+t*e/h*x;v=w*nu**2*sigma0*(nu0/nu)**3
        rr.append(gl/(2*gu)*(h*h/(2*pi*m_e*e*t))**1.5*8*pi/c**2*(t*e/h)*v.sum());mean.append(np.sum(v*h*nu/e)/v.sum())
    for name,values in [('ion',ion),('rec',rec),('rr',np.array(rr)),('mean',np.array(mean))]:
        names.append(f'cu{charge}_{name}');data.append(np.log10(np.maximum(values,1e-300)))
    active=ion>1e-280
    err=np.max(abs(ion[active]/rec[active]/((2*gu/gl)*(2*pi*m_e*e*T[active]/h**2)**1.5*np.exp(-I/T[active]))-1))
    assert err<1e-12
    checks.append(dict(parent_charge=charge,threshold_eV=I,g_parent=gl,g_daughter=gu,occupied_3d=q,detailed_balance_max_error=float(err)))
np.savetxt(D/'copper_multicharge_rates.txt',np.column_stack([np.log10(T),*data]),fmt='%.14e')
report=dict(columns=names,checks=checks,energy_levels_source='NIST ASD CuII,III,IV,V ionization tables in round2_references; bracketed CuIV/V values are estimates',rate_model='Lotz valence3d ionization, inverse Maxwellian detailed balance; Kramers-Milne ground-state radiative recombination same approximation as inherited model',not_fitted_to_S4=True,limitations=['Ground ionic states only; excited ionic lines and dielectronic recombination omitted','Lotz and hydrogenic recombination are semiempirical, not measured charge-resolved cross sections','Cu4+ upper truncation must be checked; Cu4+ ->Cu5+ threshold79.8eV'])
(D/'copper_multicharge_sources.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
