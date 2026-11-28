"""Read-only high-temperature scope checks on accepted material/Plasma states."""
from pathlib import Path
import json,sys
import numpy as np
sys.path.insert(0,str(Path(__file__).resolve().parent))
from split_history import read_history
from split_collision_optics import diagnostic

R=Path(__file__).resolve().parent
EV=1.602176634e-19;KB=1.380649e-23;ME=9.1093837139e-31;EPS=8.8541878128e-12
omega=2*np.pi*299792458/1064e-9;ncrit=EPS*ME*omega**2/EV**2
screenT=np.geomspace(.015,100.,401);excess=np.geomspace(1e-6,4000.,6000);threshold=79.8
sigma=4.5e-18*7*np.log((excess+threshold)/threshold)/((excess+threshold)*threshold)
screenK=np.array([np.sqrt(8*EV/(np.pi*ME))*np.trapz(sigma*(excess+threshold)*np.exp(-excess/t),excess)/t**1.5*np.exp(-threshold/t) for t in screenT])

def audit(case):
    g=np.load(R/'results'/case/'grid.npz');vol=g['cell_volume_m3'];rows=[]
    for h in read_history(case):
        if h['saved_time_ns']<80:continue
        with np.load(h['frame']) as d:
            S,W,U=d['S'],d['W'],d['U'];ag=np.maximum(W[:,:,6],1e-30)
            ne=S[:,:,18]/ag;te=2*S[:,:,19]/(3*np.maximum(S[:,:,18],1e-100))
            copper=S[:,:,10:18].sum(2)
            if S.shape[2]>20:copper+=S[:,:,20:].sum(2)
            mass=copper*vol;active=(ag>1e-6)&(copper>1e10)
            def fraction(mask):return float(mass[mask].sum()/max(mass.sum(),1e-100))
            nion=S[:,:,17].copy()
            if S.shape[2]>20:nion+=S[:,:,20:].sum(2)
            spacing=(3/(4*np.pi*np.maximum(nion/ag,1.)))**(1/3)
            gamma=EV/(4*np.pi*EPS*spacing*np.maximum(te,1e-30))
            debye=np.sqrt(EPS*np.maximum(te,1e-30)/(EV*np.maximum(ne,1.)))
            nd=4*np.pi/3*ne*debye**3
            cu4=S[:,:,22] if S.shape[2]>22 else np.zeros_like(copper)
            k5=10**np.interp(np.log10(np.maximum(te,.015)),np.log10(screenT),np.log10(np.maximum(screenK,1e-300)))
            forward_step=ne*k5*.25e-9
            optics=diagnostic(S,W);weight=optics['weak_absorption']*W[:,:,6]*vol*np.exp(-2*(g['r_m'][:,None]/11e-6)**2)
            alpha_error=abs(optics['drude_absorption']/np.maximum(optics['weak_absorption'],1e-100)-1)
            rows.append(dict(time_ns=h['saved_time_ns'],critical_density_m3=ncrit,
                max_intrinsic_ne_over_critical=float(ne[active].max()/ncrit) if active.any() else 0.,
                gas_copper_fraction_ne_above_0p1_ncrit=fraction(active&(ne>.1*ncrit)),
                gas_copper_fraction_overcritical=fraction(active&(ne>ncrit)),
                Cu4_fraction_of_all_gas_copper=float((cu4*vol).sum()/max(mass.sum(),1e-100)),
                forward_only_Cu5_creation_per_0p25ns_over_total_Cu=float((cu4*vol*(-np.expm1(-forward_step))).sum()/max(mass.sum(),1e-100)),
                absorption_weighted_relative_Drude_correction=float((alpha_error*weight).sum()/max(weight.sum(),1e-100)),
                absorption_weight_fraction_susceptibility_above_0p1=float(weight[optics['susceptibility']>.1].sum()/max(weight.sum(),1e-100)),
                maximum_gas_dielectric_susceptibility=float(optics['susceptibility'][active].max()) if active.any() else 0.,
                gas_copper_fraction_weakly_coupled_invalid=fraction(active&(te>1)&((gamma>1)|(nd<1))),
                max_electron_temperature_eV=float(te[active].max()) if active.any() else 0.))
    out=dict(case=case,scope='Diagnostic only; no optical coefficients or reaction rates changed.',
             weak_coupling_note='Electron unit-charge Gamma and Debye-sphere electron count; not a replacement dense-plasma collision model.',
             Cu5_screen_note='NIST estimated Cu4+ ionization threshold79.8eV and same Lotz7-electron3d shell prescription; forward-only upper screen, excludes recombination; not an applied reaction.',
             optical_note='Read-only exact homogeneous Drude comparison with the existing effective collision law; log-linear rate interpolation. Weight is local weak absorption times gas volume and incident Gaussian, without upstream attenuation. ne/ncrit alone is not a reflection criterion in collisional plasma.',rows=rows)
    (R/'results'/case/'hot_regime_audit.json').write_text(json.dumps(out,indent=2))
    print(json.dumps(dict(case=case,last=rows[-1] if rows else None)))

if __name__=='__main__':
    for case in sys.argv[1:]:audit(case)
