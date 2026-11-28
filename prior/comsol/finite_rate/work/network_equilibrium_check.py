"""Check conservation and thermal equilibrium with reactions, not just ratios."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from kinetic_screen import Rates
from rate_tables import ENERGIES,G,CHI,GION
from scipy.linalg import expm

def run():
    rates=Rates();raw=np.load(ROOT/'data/rates.npz');i=np.argmin(abs(raw['T']-6000));T=float(raw['T'][i]);v=rates.at(T);get=lambda n:float(v[rates.idx[n]])
    results=[]
    for ne in [1e20,1e23,1e26]:
        M=np.zeros((8,8));R=np.zeros((8,8))
        def transfer(A,lo,hi,rate):A[hi,lo]+=rate;A[lo,lo]-=rate
        for pair in rates.net['pairs']:
            transfer(M,pair['lo'],pair['up'],ne*get(pair['up_func']));transfer(M,pair['up'],pair['lo'],ne*get(pair['dn_func']))
        for j in range(7):
            transfer(M,j,7,ne*get(f'ion{j}'));transfer(M,7,j,ne*ne*get(f'tbr{j}'))
            transfer(R,7,j,ne*get(f'rr{j}'))
        for line in rates.net['lines']:transfer(R,line['up'],line['lo'],line['A'])
        weights=G*np.exp(-ENERGIES*EV/(K*T));saha=2*GION*(2*np.pi*ME*K*T/H**2)**1.5*np.exp(-CHI*EV/(K*T))/ne
        target=np.r_[weights,saha];target/=target.sum()
        scale=max(abs(M).max(),1);res=float(np.max(abs(M@target))/scale)
        full=M+R;A=full.copy()/scale;rhs=np.zeros(8);A[-1]=1;rhs[-1]=1;stationary=np.linalg.solve(A,rhs)
        results.append(dict(Te_K=T,ne_m3=ne,collision_equilibrium_residual=res,column_sum_relative=float(abs(full.sum(0)).max()/scale),radiating_stationary_L1=float(abs(stationary-target).sum()),stationary=stationary.tolist(),LTE=target.tolist()))
    assert max(r['collision_equilibrium_residual'] for r in results)<1e-12
    assert max(r['column_sum_relative'] for r in results)<1e-12
    assert results[-1]['radiating_stationary_L1']<results[0]['radiating_stationary_L1']
    write_json(RES/'R2_network_equilibrium_check.json',dict(results=results,scope='Fixed electron bath and atomic number; algebraic stationary reaction system. High-density radiating limit tends to the same truncated Saha-Boltzmann equilibrium.'))
    print(results)

if __name__=='__main__':run()
