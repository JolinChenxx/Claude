"""Eight-state Cu kinetics: sourced data plus explicit, unfitted approximations.

Never extrapolate ADAS's >=1 eV effective rates into the sub-eV afterglow.
The resolved model uses Maxwell integrals, detailed balance, and Milne RR.
"""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
import csv

ENERGIES=np.array([0.,1.388948,1.6422256,3.7858977,3.8166920,6.1911751,6.1920252])
G=np.array([2.,6.,4.,2.,4.,4.,6.]);CHI=7.726380;GION=1.
A0=5.29177210903e-11;RY=13.605693122994

def transitions():
    lines=[]
    files=[ROOT/'data'/'Cu_I_visible_lines.tsv',SOURCE/'data'/'Cu_I_380_780.tsv']
    seen=set()
    for file in files:
        for row in csv.DictReader(file.open(encoding='utf-8'),delimiter='\t'):
            try:
                num=lambda name:float(row[name].strip('" []?='))
                lo,up,A=num('Ei(eV)'),num('Ek(eV)'),num('Aki(s^-1)')
                il,iu=int(np.argmin(abs(ENERGIES-lo))),int(np.argmin(abs(ENERGIES-up)))
                if abs(ENERGIES[il]-lo)>1e-4 or abs(ENERGIES[iu]-up)>1e-4:continue
                if (il,iu) in seen:continue
                wl=num('obs_wl_air(nm)') if row['obs_wl_air(nm)'].strip('" ') else num('ritz_wl_air(nm)')
                lines.append(dict(lo=il,up=iu,A=A,wavelength_nm=wl,source='local NIST ASD table'))
                seen.add((il,iu))
            except (ValueError,KeyError):continue
    # UV branching is essential: 510 nm is not the only 4p decay channel.
    # Winter & Hazi (1982), p.8: measured summed resonance oscillator f=0.65.
    # Split by statistical weights; inferred A values are NOT new NIST data.
    for up,f,wl in [(3,.65/3,327.396),(4,2*.65/3,324.754)]:
        A=6.670e15*G[0]/G[up]*f/(wl*10)**2
        lines.append(dict(lo=0,up=up,A=A,wavelength_nm=wl,
            source='Winter & Hazi 1982 measured summed f=0.65; statistical 1:2 split'))
    for line in lines:
        line['energy_eV']=float(ENERGIES[line['up']]-ENERGIES[line['lo']])
        # Vacuum frequency from the measured level separation.
        line['nu']=line['energy_eV']*EV/H
        line['f']=line['A']*(line['wavelength_nm']*10)**2/6.670e15*G[line['up']]/G[line['lo']]
    return sorted(lines,key=lambda a:(a['lo'],a['up']))

def excitation_pair(lo,up,f=None,threshold_power=1.):
    gap=ENERGIES[up]-ENERGIES[lo]
    def sigma(E):
        if lo==0 and up in (1,2,3,4):
            if up==1:ee=np.array([gap,6,10,20,60.]);ss=np.array([0,.35,.96,.30,.051])*1e-20
            elif up==2:ee=np.array([gap,6,10,20,60.]);ss=np.array([0,.25,.71,.25,.037])*1e-20
            else:
                ee=np.array([gap,6,10,20,60,100.]);ss=np.array([0,2.98,13.7,14.9,5.96,4.19])*1e-20*G[up]/6
            out=np.interp(E,ee,ss)
            near=E<6;out=np.where(near,ss[1]*np.maximum((E-gap)/(6-gap),0.)**threshold_power,out)
            out=np.where(E>ee[-1],ss[-1]*ee[-1]/E,out)
            return np.maximum(out,0.)
        # Allowed-line Bethe-type surrogate, not a measured Cu rate.
        return 4*np.pi*A0*A0*RY*RY/max(gap,1e-6)/E*float(f)*.2*np.log(np.maximum(E/gap,1.))
    return sigma

def make_tables():
    lines=transitions()
    pairs=[dict(lo=0,up=1,method='Winter-Hazi table VI'),dict(lo=0,up=2,method='Winter-Hazi table VI')]
    for line in lines:
        pairs.append(dict(lo=line['lo'],up=line['up'],f=line['f'],
            method='Winter-Hazi table V' if line['lo']==0 else 'Bethe-type allowed-line approximation'))
    T=np.geomspace(1.,1e6,601);te=K*T/EV
    x,w=np.polynomial.laguerre.laggauss(64)
    table={};meta=[]
    pref=np.sqrt(8*EV/(np.pi*ME))
    for pair in pairs:
        lo,up=pair['lo'],pair['up'];gap=ENERGIES[up]-ENERGIES[lo]
        E=gap+te[:,None]*x
        base=pref/np.sqrt(te)*np.sum(w*E*excitation_pair(lo,up,pair.get('f'))(E),axis=1)
        tag=f'{lo}{up}'
        table['up'+tag]=base*np.exp(-gap/te);table['dn'+tag]=base*G[lo]/G[up]
        pair['up_func']='up'+tag;pair['dn_func']='dn'+tag
        # Near-threshold uncertainty is retained as a distinct bracket.
        base2=pref/np.sqrt(te)*np.sum(w*E*excitation_pair(lo,up,pair.get('f'),.5)(E),axis=1)
        pair['threshold_rate_ratio_at_5000K']=float(np.interp(5000,T,base2/np.maximum(base,1e-300)))
    thermal=2*(2*np.pi*ME*K*T/H**2)**1.5
    for i,level in enumerate(ENERGIES):
        bind=CHI-level;E=bind+te[:,None]*x
        # Lotz 1967, one active valence electron; 4.5e-14 cm2 eV2 -> m2 eV2.
        sigma=4.5e-18*np.log(E/bind)/(E*bind)
        base=pref/np.sqrt(te)*np.sum(w*E*sigma,axis=1)
        table[f'ion{i}']=base*np.exp(-bind/te)
        table[f'tbr{i}']=base*G[i]/GION/thermal
        # Effective hydrogenic Kramers photoionization -> spontaneous Milne RR.
        # A Cu-specific low-energy photoionization cross section is not available here.
        neff=np.sqrt(RY/bind);sig0=6.30e-22*neff
        eps=te[:,None]*x;nu=(bind+eps)*EV/H
        sigma_photo=sig0*(bind/(bind+eps))**3
        integrand=w*sigma_photo*nu**2
        factor=4*np.pi*G[i]/GION*(H*H/(2*np.pi*ME*K*T))**1.5/C**2*EV/H*te
        table[f'rr{i}']=factor*np.sum(integrand,axis=1)
        table[f'rrE{i}']=factor*np.sum(integrand*eps*EV,axis=1)
    names=list(table)
    np.savetxt(ROOT/'data'/'rates_K.txt',np.column_stack([T]+[table[k] for k in names]),fmt='%.12e')
    np.savez(ROOT/'data'/'rates.npz',T=T,**table)
    network=dict(energies_eV=ENERGIES.tolist(),degeneracies=G.tolist(),ionization_eV=CHI,ion_g=GION,
        lines=lines,pairs=pairs,functions=names,columns={n:i+1 for i,n in enumerate(names)},
        provenance={
          'levels_lines':'Existing NIST ASD exports; four Cu I lines in 475-525 nm retained.',
          'ground_excitation':'Winter & Hazi 1982, UCID-19314, DOI 10.2172/5526164, Tables V-VI. Visually checked.',
          'threshold_extension':'Linear from zero at threshold to measured 6 eV value. Square-root alternative tabulated as uncertainty; sub-10 eV data explicitly uncertain in source.',
          'allowed_other':'Bethe-type oscillator-strength surrogate, Gaunt factor 0.2, not a measured Cu rate.',
          'ionization':'Lotz 1967 empirical cross section, one active electron, no fitted multiplier.',
          'three_body':'Exact inverse of the same ionization coefficients by Saha detailed balance, per resolved state.',
          'radiative_recombination':'Effective hydrogenic Kramers photoionization and Milne detailed balance; low-charge Cu uncertainty, seven-state truncation, no dielectronic channel.',
          'air_quenching':'Collision limit pi*(3e-10 m)^2*v_rel; evaluate probability 0 and 1 bounds, not a fitted probability.',
          'ADAS':'Downloaded Cu effective rates retained for in-range comparison only (Te >=1 eV, ne 1e16-1e21 m^-3). Not added to resolved rates, which would double count recombination.'})
    write_json(ROOT/'data'/'network.json',network)
    print('NETWORK',len(ENERGIES)+1,'states',len(lines),'lines',len(names),'rate functions',flush=True)
    return network

if __name__=='__main__':make_tables()
