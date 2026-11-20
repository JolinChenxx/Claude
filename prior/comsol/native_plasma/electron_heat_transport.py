"""Conservative bulk electron heat operator; opt-in candidate, no particle flux.

The local-energy drift-diffusion closure at zero relative electron particle
flux gives q_e=-(5/2) n_e k_B D_e grad(T_e). Collision frequencies use the
existing native kinetic data. This is a classical local closure, not a sheath
or a nonlocal heat model. Face conductivities are lagged for one implicit step.
"""
import numpy as np
from scipy.sparse import coo_matrix,diags
from scipy.sparse.linalg import spsolve
from split_collision_optics import diagnostic,ME,EV
KB=1.380649e-23

def advance(U,S,W,grid,dt):
    nr,nz=S.shape[:2];n=nr*nz
    ne=np.maximum(S[:,:,18],0);ce=1.5*KB*ne
    old=np.divide(S[:,:,19]*EV,ce,out=np.zeros_like(ce),where=ce>0)
    nu=diagnostic(S,W)['nu']
    # Bulk ne already includes gas volume fraction: effective alpha_g*kappa_e.
    kappa=2.5*KB*ne*KB*old/(ME*nu)
    kappa=np.where(W[:,:,6]>1e-6,kappa,0)
    re=grid['radial_edges_m'];ze=grid['axial_edges_m']
    r=grid['r_m'];z=grid['z_m'];vol=grid['cell_volume_m3']
    cells=np.arange(n).reshape(nr,nz)
    def harmonic(a,b):return np.divide(2*a*b,a+b,out=np.zeros_like(a),where=(a+b)>0)
    gr=harmonic(kappa[:-1],kappa[1:])*2*np.pi*re[1:-1,None]*np.diff(ze)[None,:]/np.diff(r)[:,None]
    gz=harmonic(kappa[:,:-1],kappa[:,1:])*np.pi*np.diff(re**2)[:,None]/np.diff(z)[None,:]
    left=np.r_[cells[:-1].ravel(),cells[:,:-1].ravel()]
    right=np.r_[cells[1:].ravel(),cells[:,1:].ravel()]
    conductance=np.r_[gr.ravel(),gz.ravel()]*dt
    lap=coo_matrix((np.r_[conductance,conductance,-conductance,-conductance],
                   (np.r_[left,right,left,right],np.r_[left,right,right,left])),shape=(n,n)).tocsr()
    capacity=(ce*vol).ravel();diagonal=np.where(capacity>0,capacity,1.)
    matrix=(lap+diags(diagonal)).tocsr();pivot=matrix.diagonal()
    # Divide entries directly: reciprocal tiny capacities can overflow even
    # though every normalized matrix entry lies in [-1,1].
    matrix.data/=np.repeat(pivot,np.diff(matrix.indptr))
    new=spsolve(matrix,(capacity/pivot)*old.ravel()).reshape(nr,nz)
    if not np.isfinite(new).all() or np.min(new)<-1e-8:raise RuntimeError('Nonpositive electron conduction temperature')
    delta=ce*(new-old);outU=U.copy();outS=S.copy()
    outU[:,:,4]+=delta;outS[:,:,19]=np.where(ce>0,ce*new/EV,S[:,:,19])
    ledger=float((delta*vol).sum())
    return outU,outS,dict(electron_heat_boundary_J=0.,internal_heat_residual_J=ledger,
                        redistributed_J=float((abs(delta)*vol).sum()/2),
                        initial_electron_J=float((ce*old*vol).sum()),
                        final_electron_J=float((ce*new*vol).sum()),
                        min_Te_K=float(new[ce>0].min()) if np.any(ce>0) else 0.)
