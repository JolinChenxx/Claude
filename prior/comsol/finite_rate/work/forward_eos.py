"""Forward LTE reference for removing equilibrium Cu/e energy exactly once."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *

def run():
    a=np.load(SOURCE/'data/lte_eos.npz');T=a['Tgrid'];rho=np.unique(a['rhos']);Y=np.unique(a['Ys'])
    indices=np.array([[np.where((a['Ys']==yy)&(a['rhos']==rr))[0][0] for rr in rho] for yy in Y])
    vals=a['forward'][:,:,indices].transpose(1,2,3,0)
    levels=np.load(ROOT/'data/atomic_levels.npz')
    qs=[];es=[]
    for stage in range(3):
        w=levels[f'g0{stage}'][:,None]*np.exp(-levels[f'e0{stage}'][:,None]/(K*T))
        qs.append(w.sum(0));es.append((w*levels[f'e0{stage}'][:,None]).sum(0)/w.sum(0))
    ne=vals[2];rr=rho[None,:,None];yy=Y[:,None,None];tt=T[None,None,:]
    ratio=2*(2*np.pi*ME*K*tt/H**2)**1.5*(qs[2]/qs[1])*np.exp(-20.29239*EV/(K*tt))/np.maximum(ne,1e-250)
    f2=vals[4]*ratio/(1+ratio);f1=vals[4]/(1+ratio)
    cu=(1-vals[4])*es[0]+f1*(es[1]+7.72638*EV)+f2*(es[2]+28.01877*EV)
    eh=vals[0]-yy/MCU*cu-1.5*K*ne*tt/rr
    ph=vals[1]-K*ne*tt
    print('HEAVY minimum',eh.min(),ph.min(),'nonmonotone',int((np.diff(eh,axis=-1)<=0).sum()))
    if np.any(eh<=0) or np.any(ph<=0):raise ValueError('Invalid residual heavy thermodynamics')
    le=np.linspace(3,10,240);inverse=np.empty((len(Y),len(rho),len(le)))
    for iy in range(len(Y)):
        for ir in range(len(rho)):
            inverse[iy,ir]=np.interp(le,np.log10(eh[iy,ir]),np.log10(T))
    with (ROOT/'data/heavy_inverse.txt').open('w') as f:
        f.write('%Grid\n')
        for axis in [le,np.log10(rho),Y]:f.write(' '.join(f'{v:.13g}' for v in axis)+'\n')
        f.write('%Data logTHeavy\n');np.savetxt(f,inverse.reshape(-1,len(le)),fmt='%.13e')
    vals=np.concatenate([vals,eh[None],ph[None]],axis=0)
    names=['eRef','pRef','neRef','z2Ref','xRef','eHeavy','pHeavy'];out=ROOT/'data/forward_eos.txt'
    with out.open('w') as f:
        f.write('%Grid\n')
        for axis in [np.log10(T),np.log10(rho),Y]:f.write(' '.join(f'{v:.13g}' for v in axis)+'\n')
        for i,name in enumerate(names):
            f.write('%Data '+name+'\n');v=vals[i]
            if i!=4:v=np.log10(np.maximum(v,1e-250))
            np.savetxt(f,v.reshape(-1,len(T)),fmt='%.13e')
    levels=np.load(ROOT/'data/atomic_levels.npz');temp=np.geomspace(1,1e6,601);q=[];ee=[]
    for stage in range(3):
        weights=levels[f'g0{stage}'][:,None]*np.exp(-levels[f'e0{stage}'][:,None]/(K*temp))
        q.append(weights.sum(0));ee.append((weights*levels[f'e0{stage}'][:,None]).sum(0)/weights.sum(0))
    np.savetxt(ROOT/'data/Cu_equilibrium_internal.txt',np.column_stack([temp,*q,*ee]),fmt='%.13e')
    write_json(ROOT/'data/forward_eos.json',dict(source=str(SOURCE/'data/lte_eos.npz'),temperature_range_K=[float(T[0]),float(T[-1])],
        outputs=names,stored='log10 all outputs except linear Cu ion fraction xRef',shape=list(vals.shape),
        closure='LTE total thermal energy minus LTE Cu internal energy and all LTE electron thermal energy, plus resolved Cu internal and electron energies; formation energy remains in original flow equation.'))
    print('FORWARD EOS',vals.shape,flush=True)

if __name__=='__main__':run()
