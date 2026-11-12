"""Account saved split states by physical energy and material, without a camera fit."""
from pathlib import Path
import json,sys
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
sys.path.insert(0,str(Path(__file__).resolve().parent))
from split_history import read_history
from split_saved_emission import visible_emissivity

R=Path(__file__).resolve().parent
KB=1.380649e-23;EV=1.602176634e-19;MCU=.063546/6.02214076e23
EPS=np.array([0,0,4.88,2.58,15.58,12.06,19.414,16.198,1.119,-.45,0,1.388948,1.6422256,3.7858977,3.816692,6.1911751,6.1920252,7.72638])*EV
CHARGE=np.array([0,0,0,0,1,1,1,1,-1,-1,0,0,0,0,0,0,0,1])

def collect(case):
    g=np.load(R/'results'/case/'grid.npz');vol=g['cell_volume_m3'];r=g['r_m'];z=g['z_m'];rows=[]
    config=json.loads((R/'results'/case/'configuration.json').read_text())
    eos=json.loads((R/'data'/config.get('eos','Cu_HEM_20261110')/'audit.json').read_text());Tc=eos['critical_T_K']
    for state in read_history(case):
        d=np.load(state['frame']);U,W,S=d['U'],d['W'],d['S'];rho=U[:,:,0]+U[:,:,1]
        kinetic=(U[:,:,2]**2+U[:,:,3]**2)/(2*rho)
        electronic=S[:,:,19]*EV;chem=(S[:,:,:18]*EPS).sum(2)
        cu_exc=(S[:,:,11:17]*EPS[11:17]).sum(2);cu_ion=S[:,:,17]*EPS[17]
        mass_cu_species=S[:,:,10:18].sum(2)*MCU;mass_cu_eos=U[:,:,0]*W[:,:,5]
        ions=(S[:,:,:18]*CHARGE).sum(2);netcharge=EV*(ions-S[:,:,18])
        if S.shape[2]>20:
            high_ion=(S[:,:,20:]*np.array([28.01877,64.85977,122.23977])*EV).sum(2)
            chem+=high_ion;cu_ion+=high_ion;mass_cu_species+=S[:,:,20:].sum(2)*MCU
            ions+=(S[:,:,20:]*np.array([2,3,4])).sum(2);netcharge=EV*(ions-S[:,:,18])
        ag=W[:,:,6];te=2*S[:,:,19]/(3*np.maximum(S[:,:,18],1e-100))*EV/KB
        outputs=d['reaction_outputs'];idx=d['reaction_indices'];j=np.zeros_like(ag);alpha=np.zeros_like(ag)
        if len(idx):
            j[idx[:,0],idx[:,1]]=outputs[7]*ag[idx[:,0],idx[:,1]]
            alpha[idx[:,0],idx[:,1]]=outputs[16]*ag[idx[:,0],idx[:,1]]
        j=visible_emissivity(S,W)
        luminous=j*vol;power=4*np.pi*luminous.sum()
        peak=np.unravel_index(np.argmax(S[:,:,18]),ag.shape)
        condensed=U[:,:,0]*(1-W[:,:,5]);dense=(condensed>100)&(ag<.5)
        rows.append(dict(model_ns=state['saved_time_ns'],absorbed_J=state['absorbed_J'],radiated_J=state['radiated_J'],
            kinetic_J=float((kinetic*vol).sum()),electron_J=float((electronic*vol).sum()),chemical_J=float((chem*vol).sum()),
            Cu_excitation_J=float((cu_exc*vol).sum()),Cu_ionization_J=float((cu_ion*vol).sum()),
            remaining_material_internal_J=float(((U[:,:,4]-kinetic-electronic-chem)*vol).sum()),
            gas_Cu_kg=float((mass_cu_species*vol).sum()),EOS_gas_Cu_kg=float((mass_cu_eos*vol).sum()),
            EOS_critical_T_K=Tc,maximum_dense_condensed_T_K=float(W[:,:,0][dense].max()) if dense.any() else None,
            condensed_mass_above_0p8_Tc_kg=float((condensed*vol*(W[:,:,0]>.8*Tc)).sum()),
            condensed_mass_above_0p9_Tc_kg=float((condensed*vol*(W[:,:,0]>.9*Tc)).sum()),
            phase_species_mass_difference_kg=float(((mass_cu_species-mass_cu_eos)*vol).sum()),
            phase_species_absolute_mass_difference_kg=float((abs(mass_cu_species-mass_cu_eos)*vol).sum()),
            net_charge_C=float((netcharge*vol).sum()),absolute_local_charge_C=float((abs(netcharge)*vol).sum()),
            electron_charge_inventory_C=float((EV*S[:,:,18]*vol).sum()),visible_emission_W=float(power),
            light_centroid_z_um=float((luminous*z[None,:]).sum()/luminous.sum()*1e6) if power>0 else None,
            near_surface_light_fraction=float(luminous[:,z<1e-6].sum()/luminous.sum()) if power>0 else None,
            axial_laser_optical_depth=float((alpha[0]*np.diff(g['axial_edges_m'])).sum()),
            peak_ne_bulk_m3=float(S[peak][18]),peak_ne_r_um=float(r[peak[0]]*1e6),peak_ne_z_um=float(z[peak[1]]*1e6),
            peak_ne_Tg_K=float(W[peak][0]),peak_ne_Te_K=float(te[peak]),
            mass_ledger_residual_kg=state['mass_residual_kg'],energy_ledger_residual_J=state['energy_residual_J']))
    out=R/'results'/case/'energy_diagnosis.json'
    out.write_text(json.dumps(dict(case=case,phase_feed=json.loads((R/'results'/case/'configuration.json').read_text()).get('phase_feed'),
        scope='Accepted post-phase populations; emission reconstructed from neutral lines and visible continua, then volume-integrated, not line-of-sight camera intensity. Net charge is an inventory, not a complete boundary-current budget.',rows=rows),indent=2))
    return rows

def main(cases):
    fig,ax=plt.subplots(2,3,figsize=(13,7.5),layout='constrained')
    fields=[('gas_Cu_kg',1e12,'Gas Cu [ng]'),('peak_ne_bulk_m3',1,'Peak electron density [1/m3 bulk]'),
        ('visible_emission_W',1,'475-525 nm emission [W]'),('electron_J',1e6,'Electron energy [uJ]'),
        ('Cu_excitation_J',1e6,'Cu excitation energy [uJ]'),('light_centroid_z_um',1,'Emission centroid z [um]')]
    for case in cases:
        rows=collect(case);t=[q['model_ns'] for q in rows]
        for axis,(key,factor,label) in zip(ax.flat,fields):
            axis.plot(t,[q[key]*factor if q[key] is not None else np.nan for q in rows],label=case)
            axis.set(xlabel='Model time [ns]',ylabel=label);axis.grid(alpha=.2)
        print(json.dumps(dict(case=case,last=rows[-1])),flush=True)
    for axis in [ax[0,1],ax[0,2],ax[1,0],ax[1,1]]:axis.set_yscale('symlog',linthresh=1e-14)
    for axis in ax.flat:axis.legend(fontsize=8);axis.axvline(90,color='gray',ls=':',lw=1)
    fig.suptitle('Saved-state supply / ionization / energy comparison\nSame input laser; neutral phase feed and Cu charge-state closure are recorded per case. No fitted gain.')
    out=R/'figures/split_energy';out.mkdir(exist_ok=True)
    fig.savefig(out/'supply_energy_comparison.png',dpi=165);plt.close(fig)

if __name__=='__main__':main(sys.argv[1:])
