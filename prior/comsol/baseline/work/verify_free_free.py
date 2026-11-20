"""Analytic thin and homogeneous thick checks for the no-floor continuum."""
import json
from pathlib import Path
import numpy as np
from scipy.integrate import quad
from numba import set_num_threads
from radiation_s4 import transfer_ff,planck,BANDS,H,K,C
ROOT=Path(__file__).resolve().parents[1]
set_num_threads(2)
rows=[]
for T in [293.15,1000.,7000.,30000.]:
 q=1e-50;length=1e-3
 out=transfer_ff(np.full((1,4),q),np.full((1,4),T),np.full(4,length/4))[:,0]
 expected=np.array([q*length*(np.exp(-H*C/(hi*1e-9*K*T))-np.exp(-H*C/(lo*1e-9*K*T))) for lo,hi in BANDS])
 relative=float(np.max(abs(out/expected-1)));assert relative<1e-12
 rows.append(dict(regime='thin',T_K=T,q=q,max_relative_error=relative))
T=7000.;q=1e14;length=1e-3
out=transfer_ff(np.full((1,40),q),np.full((1,40),T),np.full(40,length/40))[:,0]
def integrand(nu):
 b=planck(nu,T);alpha=q*C*C/(2*K*T*nu**3)*(-np.expm1(-H*nu/(K*T)))
 return b*(-np.expm1(-alpha*length))
expected=np.array([quad(integrand,C/(hi*1e-9),C/(lo*1e-9),epsabs=1e-8,epsrel=1e-10)[0] for lo,hi in BANDS])
relative=float(np.max(abs(out/expected-1)));assert relative<.002
rows.append(dict(regime='thick',T_K=T,q=q,max_relative_error=relative))
result={'passed':True,'absolute_emission_floor':0,'thin_attenuation_relative_error_bound':1e-4,'rows':rows}
(ROOT/'results/free_free_transport_audit.json').write_text(json.dumps(result,indent=2));print(result)
