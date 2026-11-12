"""Estimate omitted bulk electron heat transport from accepted fields only."""
from pathlib import Path
import sys,json,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from split_collision_optics import diagnostic,EV,ME
from split_saved_emission import visible_emissivity
KB=1.380649e-23
case=sys.argv[1];status=json.loads((R/'results'/case/'status.json').read_text())
d=np.load(status['frame']);S,W,U=d['S'],d['W'],d['U'];g=np.load(R/'results'/case/'grid.npz')
rc,zc,vol=g['r_m'],g['z_m'],g['cell_volume_m3']
te=2/3*S[:,:,19]/np.maximum(S[:,:,18],1e-100)*EV/KB
nu=diagnostic(S,W)['nu'];D=KB*te/(ME*nu)
grad=np.hypot(np.gradient(te,rc,axis=0),np.gradient(te,zc,axis=1))
length=te/np.maximum(grad,1e-100)
thermal_speed=np.sqrt(8*KB*np.maximum(te,0)/(np.pi*ME));mean_free_path=thermal_speed/nu
tau=length**2/np.maximum(5/3*D,1e-100)
kn=mean_free_path/np.maximum(length,1e-100)
div=np.gradient(W[:,:,9],rc,axis=0)+W[:,:,9]/rc[:,None]+np.gradient(W[:,:,3],zc,axis=1)
gas=(W[:,:,6]>.99)&(S[:,:,18]>1e15)&(te>1000)
weight=visible_emissivity(S,W)*vol;den=max(float(weight[gas].sum()),1e-100)
def fraction(mask):return float(weight[gas&mask].sum()/den)
def quantile(v):
 a=v[gas];w=weight[gas]
 if not len(a) or not w.sum():return None
 order=np.argsort(a);cdf=np.cumsum(w[order])/w.sum()
 return np.interp([.1,.5,.9],cdf,a[order]).tolist()
report=dict(case=case,saved_ns=status['saved_time_ns'],scope='Read-only bulk screening, not an applied heat flux. Same inherited momentum collision law. Te gradient scale uses resolved saved fields.',closure='At zero electron particle flux, drift-diffusion energy flux reduces to q_e=-(5/2)*n_e*kB*D_e*grad(Te); D_e=kB*Te/(m_e*nu). Local classical closure requires a small mean-free-path/gradient-length ratio.',visible_weight_fraction_in_screened_cells=float(weight[gas].sum()/max(weight.sum(),1e-100)),visible_weighted_10_50_90_heat_time_ns=quantile(tau*1e9),visible_weighted_10_50_90_Knudsen=quantile(kn),visible_fraction_heat_time_below1ns=fraction(tau<1e-9),visible_fraction_heat_faster_than_expansion=fraction(tau*np.abs(div)<1),visible_fraction_Knudsen_above0p1=fraction(kn>.1))
(R/'results'/case/'electron_transport_screen.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
