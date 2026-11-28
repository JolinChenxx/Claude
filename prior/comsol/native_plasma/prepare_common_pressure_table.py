"""Tabulate the same additive-volume equilibrium closure used by the FV audit.

This removes local pressure/density algebraic Newton solves from COMSOL, while
retaining the conservative masses, momentum and total energy as primary fields.
Coordinates: log10(T), log10(total density), log10(copper/air mass ratio).
"""
from pathlib import Path
import sys,json,time,os
import numpy as np
from numba import njit,prange,set_num_threads
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from hem_common_thermo import mixture

@njit(parallel=True)
def generate(ts,rs,ratios,lt,lr,tab):
    out=np.zeros((len(ratios),len(rs),len(ts),6))
    for k in prange(len(ratios)):
        y=1/(1+10**(-ratios[k]))
        for j in range(len(rs)):
            rc=rs[j]*y;ra=rs[j]/(1+10**ratios[k])
            for i in range(len(ts)):
                q=mixture(ts[i],rc,ra,lt,lr,tab)
                out[k,j,i]=np.array([q[0]/rs[j],q[1],q[2],q[3],q[4],q[5]])
    return out

def main():
    set_num_threads(8)
    d=np.load(R/'data/Cu_HEM_20261110/eos.npz')
    ts=d['temperatures_K'];rs0=d['densities_kg_m3']
    tab=np.ascontiguousarray(d['table'][:,2:].reshape(len(ts),len(rs0),7))
    rs=np.unique(np.r_[np.geomspace(1e-6,10000,120),np.linspace(500,5000,46),np.linspace(6500,10000,71),8942.230330721954,101325/(287*293.15)])
    ratios=np.unique(np.r_[np.arange(-12,18.01,.5),np.log10(1e-10/(101325/(287*293.15))),np.log10(8942.230330721954/1e-12)])
    start=time.time();out=generate(ts,rs,ratios,np.log10(ts),np.log10(rs0),tab)
    dest=R/'data/Cu_air_HEM_20261110';dest.mkdir(exist_ok=True)
    names=['mixE','mixP','mixXv','mixAg','mixCs','mixAc']
    with (dest/'mixture_grid.txt').open('w') as f:
        f.write('%Grid\n')
        for arr in [np.log10(ts),np.log10(rs),ratios]:f.write(' '.join(f'{x:.13e}' for x in arr)+'\n')
        for n,name in enumerate(names):
            f.write('%Data ('+name+')\n')
            np.savetxt(f,out[:,:,:,n].reshape(-1,len(ts)),fmt='%.12e')
    np.savez_compressed(dest/'mixture.npz',T=ts,rho=rs,log_mass_ratio=ratios,values=out)
    de=np.diff(out[:,:,:,0],axis=2)/np.diff(ts)[None,None,:]
    report={'shape':list(out.shape),'coordinates':['log10(T/K)','log10(rho_total/(kg/m3))','log10(rhoCu/rhoAir)'],
            'functions':names,'finite':bool(np.isfinite(out).all()),'minimum_cv_along_constant_mass_densities':float(de.min()),
            'nonpositive_cv_count':int((de<=0).sum()),'pressure_min_Pa':float(out[:,:,:,1].min()),
            'seconds':time.time()-start,'closure':'common pressure, additive occupied volumes; nuclear copper EOS plus ideal air',
            'native_plasma_included':False,'purpose':'material closure precomputation, no experiment fitting'}
    (dest/'audit.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
if __name__=='__main__':main()
