"""Record the thermal surface closure and its deterministic NIST partition.

This does not change total evaporation or gas reaction coefficients. The surface
equilibrium assumption is a candidate, not an experimental validation at 8500 K.
"""
from pathlib import Path
import json, numpy as np
R=Path(__file__).resolve().parent
states=[s for s in json.loads((R/'data/chemistry.json').read_text())['species'] if s['name'].startswith('Cu')]
energy=np.array([s['energy_eV'] for s in states]);g=np.array([s['g'] for s in states]);charge=np.array([s['charge'] for s in states])
kB=8.617333262145e-5;work=4.65
rows=[]
for T in [300,1000,2000,3000,4000,5000,6000,7000,8000,8500,9000]:
    w=g*np.exp((charge*work-energy)/(kB*T));f=w/w.sum()
    rows.append([T,*f,float(f@energy)])
np.savetxt(R/'data/surface_feed_partition.csv',rows,delimiter=',',header=','.join(['surface_T_K']+[s['name'] for s in states]+['mean_internal_eV']),comments='')
data={
 'closure':'Saha-Langmuir thermal ion emission plus Boltzmann neutral internal-state partition',
 'weights':'neutral g_j exp(-E_j/kBTs); Cu+ g+ exp((W-I)/kBTs); normalize over all eight states',
 'scope':'Only the outgoing Cu boundary flux; gas remains native time-dependent non-LTE Plasma',
 'total_Cu_flux':'Existing mdotEvap divided among states, unchanged in total before thermal feedback',
 'energy':'Add sum_j Gamma_j E_j to Qmass; full I for Cu+; QworkWall=W*Iwall includes all returning/emitted charges',
 'charge':'Iwall receives -e*Gamma_Cu+; no extra emitted-electron source; native thermionic wall and explicit sheath retained',
 'sources':[
  {'source':'NIST Atomic Spectra Database; existing chemistry.json','use':'Eight-state energies and statistical weights; Cu I threshold 7.72638 eV'},
  {'source':'Saha-Langmuir detailed-balance relation','use':'Ion/neutral emission partition from surface electron chemical potential -W, no adjusted rate'},
  {'source':'Kools et al., Appl. Phys. B 53, 125-130 (1991)','doi':'10.1007/BF00330226','local':'round2_references/Cu_surface_ionization_1988.pdf','use':'Context for thermally ionized Cu laser ablation; abstract calls Saha-Langmuir, body discusses gas Saha. It is not direct validation of this boundary closure.'},
  {'source':'Inherited copper thermionic boundary','use':'W=4.65 eV shared with electron emission; no new work-function fit'}
 ],
 'limitations':[
  'Assumes electronic state equilibration at the emitting surface; actual rapidly heated surface may deviate.',
  'Available reference experiment used UV nanosecond pulses in vacuum at lower fluence; it does not validate atmospheric 1064 nm behavior.',
  'Eight-state partition truncates higher Cu levels; ion excited levels are omitted.',
  'Inherited high-temperature condensed Cu and vapor-pressure extrapolations remain unresolved.',
  'Surface charge/field effects are resolved by the inherited sheath; no added Schottky correction.'
 ],
 'at_8500_K':dict(zip([s['name'] for s in states]+['mean_internal_eV'],rows[-2][1:]))
}
(R/'data/surface_feed_sources.json').write_text(json.dumps(data,indent=2),encoding='utf8')
print(json.dumps(data['at_8500_K'],indent=2))
