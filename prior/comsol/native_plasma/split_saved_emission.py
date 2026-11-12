"""Visible volume emissivity of accepted, post-phase-update populations."""
from pathlib import Path
from functools import lru_cache
import json,numpy as np
R=Path(__file__).resolve().parent
EV=1.602176634e-19;KB=1.380649e-23

@lru_cache(maxsize=1)
def rates():
    chemistry=json.loads((R/'data/chemistry.json').read_text())
    return chemistry,np.loadtxt(R/'data/log_rates.txt'),np.loadtxt(R/'data/rr_mean_energy.txt')

def visible_emissivity(S,W):
    ag=np.maximum(W[:,:,6],1e-30);n=S[:,:,:18]/ag[:,:,None];ne=S[:,:,18]/ag
    actual_tev=np.maximum(2*S[:,:,19]/(3*np.maximum(S[:,:,18],1e-100)),0.)
    tev=np.maximum(actual_tev,.015)
    te=actual_tev*EV/KB;ni=n[:,:,[4,5,6,7,8,9,17]].sum(2)
    if S.shape[2]>20:ni+=(S[:,:,20:]/ag[:,:,None]*np.array([4,9,16])).sum(2)
    elo=6.62607015e-34*299792458/(525e-9*EV);ehi=6.62607015e-34*299792458/(475e-9*EV)
    j=1.426e-40*np.sqrt(te)*ne*ni/(4*np.pi)*(np.exp(-elo/tev)-np.exp(-ehi/tev))
    for A,up,photon in [(2e6,14,2.427744),(6e7,15,2.4052774),(7.5e7,16,2.3753332),(1.5e7,15,2.3744831)]:
        j+=A*n[:,:,up]*photon*EV/(4*np.pi)
    chemistry,rate,mean=rates();rrs=[q['name'] for q in chemistry['reactions'] if q['name'].endswith('rr')]
    for state,threshold in [('CuH3',1.5352049),('CuH5',1.5343548)]:
        col=chemistry['rate_columns'].index(state+'_rr')+1;mi=rrs.index(state+'rr')+1
        alpha=10**np.interp(np.log10(tev),rate[:,0],rate[:,col]);energy=10**np.interp(np.log10(tev),mean[:,0],mean[:,mi])
        j+=ne*n[:,:,17]*alpha*energy*EV/(4*np.pi)*(np.exp(-(elo-threshold)/tev)-np.exp(-(ehi-threshold)/tev))
    return j*W[:,:,6]
