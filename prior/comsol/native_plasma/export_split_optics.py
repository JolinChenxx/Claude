"""Export actual native reaction populations and material volumes for ray transfer.

No fitted emission or prescribed plume shape. Export keeps intrinsic gas number
densities separate from gas volume fraction, avoiding an alpha_g^2 radiation bug.
"""
from pathlib import Path
import json,sys
import numpy as np
sys.path.insert(0,str(Path(__file__).resolve().parent))
from split_history import read_history
R=Path(__file__).resolve().parent;case=sys.argv[1];p=R/'results'/case;g=np.load(p/'grid.npz');r=g['r_m']*1e6;z=g['z_m']*1e6;keep=z>0
h=read_history(case);h=[s for s in h if s['saved_time_ns']>=80-1e-6]
if not h:raise ValueError('No saved state beyond80ns')
ex=['Tg','TeK','plas.ne','rhoCopper','rhoP','urP','uzP','pPl','plas.V','Qlaser','Qrad','Qff','nCuTotal','niZ2','jline3','jline4','jline5','jline6','tauLine3/w0','tauLine4/w0','tauLine5/w0','tauLine6/w0','nCu_1p','nCuH3','nCuH5','alphaGas','rhoCondensed']
pre=R/'results'/f'{case}native';masses=np.array([.0280134,.0319988,.0140067,.0159994,.0280134,.0319988,.0140067,.0159994,.0159994,.0319988]+[.063546]*8)/6.02214076e23
with Path(str(pre)+'_fields.bin').open('wb') as f:
 for s in h:
  d=np.load(s['frame']);U=d['U'];W=d['W'];S=d['S'];ag=np.maximum(W[:,:,6],1e-30);n=S[:,:,:18]/ag[:,:,None];ne=S[:,:,18]/ag;Te=2/3*S[:,:,19]/np.maximum(S[:,:,18],1e-100)*1.602176634e-19/1.380649e-23
  rx=np.zeros((*U.shape[:2],d['reaction_outputs'].shape[0]));ij=d['reaction_indices'];rx[ij[:,0],ij[:,1]]=d['reaction_outputs'].T
  ni=n[:,:,[4,5,6,7,8,9,17]].sum(2);qff=1.426e-40*np.sqrt(np.maximum(Te,0))*ne*ni
  extra=S[:,:,20:]/ag[:,:,None] if S.shape[2]>20 else np.zeros((*U.shape[:2],0))
  if extra.shape[2]:
   ni+=(extra*np.array([4,9,16])).sum(2);qff=1.426e-40*np.sqrt(np.maximum(Te,0))*ne*ni
  # Reconstruct from the saved populations after the energy/phase flash.
  # Native substep outputs precede that flash and may have different phase mass.
  linej=[];linea=[]
  for wavelength,A,up,lo,ratio,osc,photon in [(510.5548,2e6,14,11,1.5,.0052105220881473865,2.427744),(515.3238,6e7,15,13,.5,.4777486018933752,2.4052774),(521.8202,7.5e7,16,14,2/3,.4592530759477124,2.3753332),(522.007,1.5e7,15,14,1.,.06127759193714263,2.3744831)]:
   width=1/(wavelength*1e-9)*np.sqrt(2*1.380649e-23*W[:,:,0]/masses[10])
   cross=1.602176634e-19**2/(4*8.8541878128e-12*9.1093837139e-31*299792458)*osc/(np.sqrt(np.pi)*width)
   linej.append(A*n[:,:,up]*photon*1.602176634e-19/(4*np.pi));linea.append(cross*np.maximum(n[:,:,lo]-ratio*n[:,:,up],0))
  fields=[W[:,:,0],Te,ne,U[:,:,0]*W[:,:,5],(S[:,:,:18]*masses).sum(2),W[:,:,9],W[:,:,3],W[:,:,1],np.zeros_like(ag),np.zeros_like(ag),rx[:,:,6],qff,n[:,:,10:].sum(2),ni]
  fields[4]+=extra.sum(2)*masses[10]*ag;fields[13]=ni;fields[12]+=extra.sum(2)
  fields.extend(linej+linea);fields.extend([n[:,:,17],n[:,:,15],n[:,:,16],W[:,:,6],U[:,:,0]*(1-W[:,:,5])])
  fields[9][:]=np.nan # Absorption was not retained in the saved material frame.
  fields[8][:]=np.nan # Abstract reaction-cell potential is not a spatial potential.
  for field in fields:np.asarray(field[:,keep].T,dtype='>f4').tofile(f)
meta={'shape':[len(h),len(ex),int(keep.sum()),len(r)],'dtype':'>f4','times_s':[s['saved_time_ns']*1e-9 for s in h],'r_um':r.tolist(),'z_um':z[keep].tolist(),'expressions':ex,'gas_volume_fraction_radiation':True,'model_kind':'material FV with native Plasma local reactions; no sheath/electric drift/electron diffusion','Qlaser_export_available':False,'coordinates':'Fixed physical coordinates relative to initial copper surface; mixed-phase fraction retained.'}
Path(str(pre)+'_fields.json').write_text(json.dumps(meta,indent=2));print(pre)
