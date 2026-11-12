"""Minimal eliminated Cu I 5d 2D5/2 channel; no image data or adjustable rates.

Rates use the same van Regemorter, Lotz, and Kramers-Milne approximations as the
existing chemistry. This adds a physically accessible 1064 nm photoionization
path absent from a level set whose highest binding threshold is 1.53 eV.
"""
from pathlib import Path
import json
import numpy as np
from scipy.constants import e,k,m_e,h,c,pi
from numpy.polynomial.laguerre import laggauss
R=Path(__file__).resolve().parent;D=R/'data'
T=np.geomspace(.015,100,501)
lower,upper,gi,gl,gu,A,lam=3.816692,6.8676456,1,4,6,2.1e7,406.2638
I=7.72638-upper;delta=upper-lower;Iparent=7.72638-lower
f=1.4991938e-16*(lam*10)**2*A*gu/gl
ups=14.5*gl*f*(13.605693/delta)**2*.2
up=8.629e-12*ups/(gl*np.sqrt(T*e/k))*np.exp(-delta/T)
down=8.629e-12*ups/(gu*np.sqrt(T*e/k))
z=np.geomspace(1e-6,4000,6000)
sig=4.5e-18*np.log((z+I)/I)/((z+I)*I)
scaled=np.array([np.sqrt(8*e/(pi*m_e))*np.trapz(sig*(z+I)*np.exp(-z/t),z)/t**1.5 for t in T])
ion=scaled*np.exp(-I/T)
rec=scaled/((2*gi/gu)*(2*pi*m_e*e*T/h**2)**1.5)
x,w=laggauss(64);sigma0=6.3e-22*np.sqrt(13.605693/I);nu0=I*e/h
rr=[];rrmean=[]
for t in T:
    nu=nu0+t*e/h*x;v=w*nu**2*sigma0*(nu0/nu)**3
    rr.append(gu/(2*gi)*(h*h/(2*pi*m_e*e*t))**1.5*8*pi/c**2*(t*e/h)*v.sum())
    rrmean.append(np.sum(v*h*nu/e)/v.sum())
vals=[up,down,ion,rec,np.array(rr),np.array(rrmean)]
assert all(np.all(np.isfinite(v)) and np.all(v>0) for v in vals)
assert np.allclose(up/down,(gu/gl)*np.exp(-delta/T),rtol=1e-12)
assert np.allclose(ion/rec,(2*gi/gu)*(2*pi*m_e*e*T/h**2)**1.5*np.exp(-I/T),rtol=1e-12)
photon=h*c/(1064e-9)/e;sigma1064=sigma0*(I/photon)**3
np.savetxt(D/'tail5d_log_rates.txt',np.column_stack([np.log10(T),*[np.log10(v) for v in vals]]),fmt='%.14e')
report={'level':'Cu I 3d10.5d 2D5/2','upper_energy_eV':upper,'parent':'CuP3',
 'parent_energy_eV':lower,'g_upper':gu,'g_parent':gl,'ion_threshold_eV':I,
 'parent_ion_threshold_eV':Iparent,'photon_1064_eV':photon,
 'dominant_decay_nm':lam,'dominant_A_s':A,'oscillator_strength':f,
 'photo_sigma_1064_m2':sigma1064,'rate_columns':['up','down','ion','3rec','rr','rr_mean_eV'],
 'level_data':'NIST ASD Cu I 200-20000 nm; transition accuracy C',
 'excitation':'van Regemorter 1962, fixed gbar=0.2, same prescription as existing visible transitions',
 'ionization':'Lotz valence formula; inverse from detailed balance',
 'photoionization':'Hydrogenic Kramers approximation, identical threshold scaling to existing recombination model; not measured Cu cross section',
 'recombination':'Milne inverse of the same photoionization cross section',
 'reduction':'Quasisteady eliminated upper state; source and loss channels retained, dominant decay to CuP3',
 'limits':['Only one high level; not a complete Rydberg manifold','Omitted weaker decay branches','Check upper-state population and relaxation time before accepting elimination'],
 'checks':{'excitation_detailed_balance':'passed','ionization_detailed_balance':'passed'}}
# Independently sum elementary energy transfers and compare with the reduced
# source. Normalize by gross channel powers: near detailed balance the net
# power can vanish and is an ill-conditioned denominator for roundoff checks.
rng=np.random.default_rng(7);errors=[]
for _ in range(10000):
    U,C3,Crr=10**rng.uniform(-3,8,3)
    decay,deexc,coll,photo=10**rng.uniform(-2,12,4)
    mean=I+10**rng.uniform(-2,1);loss=decay+deexc+coll+photo
    nr=(U+C3+Crr)/loss
    net=U*(coll+photo)/loss-(C3+Crr)*(decay+deexc)/loss
    channels=np.array([-delta*U,delta*nr*deexc,-I*nr*coll,I*C3,-(mean-I)*Crr,(photon-I)*nr*photo])
    compact=photon*nr*photo-delta*nr*decay-mean*Crr-Iparent*net
    gross=max(np.abs(channels).sum(),Iparent*(U+C3+Crr),photon*nr*photo,delta*nr*decay+mean*Crr,1e-100)
    errors.append(abs(channels.sum()-compact)/gross)
assert max(errors)<2e-14,max(errors)
report['checks'].update(energy_random_states=10000,energy_identity_gross_normalized_max_error=max(errors))
(D/'tail5d_sources.json').write_text(json.dumps(report,indent=2),encoding='utf8')
print(json.dumps(report,indent=2))
