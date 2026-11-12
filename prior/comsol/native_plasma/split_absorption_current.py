"""Evaluate the inherited inverse-bremsstrahlung law on current populations.

The gas fraction and electron inventory change during material substeps. A
reaction-end opacity must not keep heating electrons after they condense.
"""
from pathlib import Path
import json,numpy as np
from numba import njit,prange
R=Path(__file__).resolve().parent
chemistry=json.loads((R/'data/chemistry.json').read_text());rates=np.loadtxt(R/'data/log_rates.txt')
LOGT=np.ascontiguousarray(rates[:,0]);KN=np.ascontiguousarray(rates[:,chemistry['rate_columns'].index('N2_0')+1]);KO=np.ascontiguousarray(rates[:,chemistry['rate_columns'].index('O2_2')+1])

@njit(parallel=True)
def current_absorption(S,W):
    out=np.zeros(S.shape[:2]);e=1.602176634e-19;me=9.1093837139e-31;eps=8.8541878128e-12;c=299792458.;omega=2*np.pi*c/1064e-9
    for i in prange(S.shape[0]):
        for j in range(S.shape[1]):
            ag=W[i,j,6]
            if ag<=1e-6 or S[i,j,18]<=0:continue
            ne=S[i,j,18]/ag;te=max(2*S[i,j,19]/(3*S[i,j,18]),.015);x=np.log10(te)
            kn=10**np.interp(x,LOGT,KN);ko=10**np.interp(x,LOGT,KO)
            n=S[i,j,:18]/ag;heavy=n.sum();ni=n[4:10].sum()+n[17]
            if S.shape[2]>20:
                for q in range(20,S.shape[2]):
                    heavy+=S[i,j,q]/ag;ni+=(q-18)**2*S[i,j,q]/ag
            other_neutral=n[2]+n[3]+n[10:17].sum()
            neutral=n[0]*kn+n[1]*ko+other_neutral*2e-19*np.sqrt(8*e*te/(np.pi*me))
            logc=max(2.,min(15.,23-np.log(np.sqrt(max(ne/1e6,1.))*te**(-1.5))))
            nu=max(neutral+2.91e-12*ni*logc/te**1.5,1e5)
            out[i,j]=ag*ne*e**2/(eps*me*c)*nu/(omega**2+nu**2)
    return out
