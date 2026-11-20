"""Read-only reconstruction of the current collision law and Drude diagnostics."""
from pathlib import Path
from functools import lru_cache
import json,numpy as np
R=Path(__file__).resolve().parent;EV=1.602176634e-19;ME=9.1093837139e-31;EPS=8.8541878128e-12;C=299792458.

@lru_cache(maxsize=1)
def data():
    return json.loads((R/'data/chemistry.json').read_text()),np.loadtxt(R/'data/log_rates.txt')

def diagnostic(S,W):
    ag=np.maximum(W[:,:,6],1e-30);n=S[:,:,:18]/ag[:,:,None];ne=S[:,:,18]/ag
    tev=np.maximum(2*S[:,:,19]/(3*np.maximum(S[:,:,18],1e-100)),.015)
    chemistry,table=data();x=np.log10(tev)
    kn=10**np.interp(x,table[:,0],table[:,chemistry['rate_columns'].index('N2_0')+1])
    ko=10**np.interp(x,table[:,0],table[:,chemistry['rate_columns'].index('O2_2')+1])
    heavy=n.sum(2);ni=n[:,:,[4,5,6,7,8,9,17]].sum(2)
    if S.shape[2]>20:
        high=S[:,:,20:]/ag[:,:,None];heavy+=high.sum(2);ni+=(high*np.array([4,9,16])).sum(2)
    # NativeReactionWorker inherits NeutralCollisionCount: charged heavy
    # particles contribute to nuEI only, not this neutral proxy.
    other_neutral=n[:,:,2]+n[:,:,3]+n[:,:,10:17].sum(2)
    nu_en=n[:,:,0]*kn+n[:,:,1]*ko+other_neutral*2e-19*np.sqrt(8*EV*tev/(np.pi*ME))
    logc=np.clip(23-np.log(np.sqrt(np.maximum(ne/1e6,1))*tev**(-1.5)),2,15)
    nu_ei=2.91e-12*ni*logc/tev**1.5;nu=np.maximum(nu_en+nu_ei,1e5)
    omega=2*np.pi*C/1064e-9;wp2=ne*EV**2/(EPS*ME)
    epsilon=1-wp2/(omega*(omega+1j*nu));index=np.sqrt(epsilon)
    weak=wp2/C*nu/(omega**2+nu**2);exact=2*omega/C*index.imag
    reflection=abs((1-index)/(1+index))**2
    return dict(nu=nu,weak_absorption=weak,drude_absorption=exact,
                susceptibility=abs(1-epsilon),collision_over_frequency=nu/omega,
                homogeneous_halfspace_reflection=reflection)
