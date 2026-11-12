"""Conditional spatial light bound: explicit Cu split and phase-energy ledger.

Particles follow the exported Cu distribution as a deliberately favorable
entrainment envelope. This is not a nucleation/particle-trajectory prediction.
The atomic component is NOT added undiminished; 10% of Cu is assigned to particles.
"""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from legacy_radiation import physical_grid
from particles import enthalpy,RHO,LV,cross_section
import csv

def run():
    cooling=list(csv.DictReader((RES/'R2_particle_cooling.csv').open()));tab=np.load(RES/'R2_particle_optical_tables.npz')
    photons=RegularGridInterpolator((tab['radius_m'],tab['T_K']),tab['band_photons_s'],bounds_error=False,fill_value=None)
    f,r,z,t=fields();rays=Rays(r,z);fraction=.1;cases=['a10_T2800_ambient','a30_T2800_ambient','a100_T2800_ambient'];panels=np.array([150.,260.,400.,580.,740.])
    allmaps=[];rows=[]
    for case in cases:
        cc=[x for x in cooling if x['case']==case];tc=np.array([float(x['experiment_ns']) for x in cc]);temp=np.array([float(x['T_particle_K']) for x in cc]);remain=np.array([float(x['remaining_mass_fraction']) for x in cc]);radius0=float(cc[0]['radius_initial_nm'])*1e-9
        maps=[]
        for ns in panels:
            it=int(np.argmin(abs(t*1e9-(ns+90))));a=physical_grid(np.array(f[it],float),r,z);cu=a[3]*np.clip(a[2],0,1)
            T=float(np.interp(ns,tc,temp));mf=float(np.interp(ns,tc,remain));radius=radius0*mf**(1/3);mp=4*np.pi/3*radius**3*RHO
            rho_particle=fraction*mf*cu;rho_gas=a[3]-rho_particle;cu_gas=cu-rho_particle
            # Available vapor reference energy (latent + Cu sensible) is reduced
            # explicitly. Remainder is a budget for phase-change heat, not added light.
            hparticle=float(enthalpy(T));available=LV+1.5*K*a[0]/MCU
            energy_removed=rho_particle*available;energy_particle=rho_particle*hparticle
            remainder=energy_removed-energy_particle
            if remainder.min()<-1e-8:raise ValueError('Particle energy exceeds local removed Cu budget')
            # Frequency-resolved particle self-absorption and an actual 10 ns gate,
            # including the half gate preceding the assumed formation at 150 ns.
            columnCu=rays.project(cu);nodes,weights=np.polynomial.legendre.leggauss(12)
            lam=500e-9+25e-9*nodes;dw=25e-9*weights;image=np.zeros_like(columnCu);max_tau=0.
            tn,tw=np.polynomial.legendre.leggauss(6);left=max(150.,ns-5);right=ns+5
            for xx,ww in zip(tn,tw):
                time=(left+right)/2+xx*(right-left)/2;temp_now=float(np.interp(time,tc,temp));mf_now=float(np.interp(time,tc,remain))
                aa=radius0*mf_now**(1/3);mass_now=4*np.pi/3*aa**3*RHO
                col=columnCu*fraction*mf_now/mass_now;absorption=cross_section(aa,lam)
                B=2*H*C*C/lam**5/np.expm1(H*C/(lam*K*temp_now))
                for wavelength,wlweight,cabs,bb in zip(lam,dw,absorption,B):
                    tau=col*cabs;max_tau=max(max_tau,float(tau.max()))
                    image+=bb*(-np.expm1(-tau))*wavelength/(H*C)*wlweight*ww*(right-left)*.5e-9
            image=blur(image)*OMEGA*(PIXEL*1e-6)**2;maps.append(image)
            mass=float(volume_integral(cu,r,z));pmass=float(volume_integral(rho_particle,r,z))
            rows.append(dict(case=case,experiment_ns=ns,T_particle_K=T,remaining_fraction=mf,copper_before_kg=mass,particle_kg=pmass,copper_gas_kg=mass-pmass,
                particle_internal_J=float(volume_integral(energy_particle,r,z)),removed_vapor_energy_J=float(volume_integral(energy_removed,r,z)),unassigned_phase_change_heat_J=float(volume_integral(remainder,r,z)),
                max_particle_optical_depth=max_tau,ideal_photons_10ns=float(image.sum()),peak_photons=float(image.max()),**{k:v for k,v in metrics(image).items() if k!='total'}))
            np.savez_compressed(RES/f'R2_particle_split_{case}_{int(ns)}ns.npz',r_um=r,z_um=z,particle_density_kgm3=rho_particle,remaining_gas_density_kgm3=rho_gas,remaining_gas_copper_density_kgm3=cu_gas,
                removed_vapor_energy_Jm3=energy_removed,particle_internal_Jm3=energy_particle,unassigned_phase_change_heat_Jm3=remainder,photons=image)
        allmaps.append(maps)
    maps=np.array(allmaps);np.savez_compressed(RES/'R2_particle_spatial.npz',cases=cases,experiment_ns=panels,photons=maps)
    with (RES/'R2_particle_spatial.csv').open('w',newline='',encoding='utf-8') as file:
        w=csv.DictWriter(file,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
    plt=setup_plot();from matplotlib.colors import LogNorm
    fig,axs=plt.subplots(4,len(panels),figsize=(17,11),layout='constrained');exp=np.load(SOURCE/'experiment/S4_reconstructed.npz')
    h,w=exp['gray'].shape[-2:];mid=maps.shape[-1]//2;vmax=max(10,float(maps.max()))
    for j,ns in enumerate(panels):
        axs[0,j].imshow(exp['gray'][np.argmin(abs(exp['times_ns']-ns))],cmap='turbo',vmin=0,vmax=255);axs[0,j].set_title(f'{ns:g} ns')
        for i,case in enumerate(cases):
            im=maps[i,j,-h:,mid-w//2:mid+(w-w//2)]
            artist=axs[i+1,j].imshow(np.ma.masked_less_equal(im,0),cmap='inferno',norm=LogNorm(1e-6,vmax))
            axs[i+1,j].text(.03,.96,f'{im.sum():.2e} photons',transform=axs[i+1,j].transAxes,va='top',color='white',fontsize=8)
    for a in axs.ravel():a.set_xticks([]);a.set_yticks([])
    for a,name in zip(axs[:,0],['Experiment']+cases):a.set_ylabel(name)
    fig.colorbar(artist,ax=axs[1:].ravel().tolist(),fraction=.015,pad=.01,label='Expected photons / pixel / 10 ns')
    fig.suptitle('Conditional particles: 10% Cu split, 2800 K at 150 ns | shared ideal optics | Cu-distribution entrainment envelope')
    fig.savefig(FIG/'R2_particle_spatial.png');plt.close(fig)
    write_json(RES/'R2_particle_spatial_notes.json',dict(status='conditional_spatial_envelope',Cu_condensed_fraction=fraction,
        mass_bookkeeping='Gas Cu is reduced by current particle mass in every saved split. The atomic emission must be rerendered from these depleted fields before a combined prediction.',
        energy_bookkeeping='Particle internal energy is debited from removed Cu vapor latent+sensible energy; unused phase-change heat is saved explicitly and is not assigned to visible emission.',
        limitations=['Entrainment envelope follows gas Cu distribution; no predicted nucleation or finite-drag particle transport.','The budget residual is not a solved condensation heat-release process.','Images show particles alone, so no undiminished atomic light is double counted.','Gas Cu column is held at gate center; particle cooling and formation boundary are integrated over the gate.','Room-temperature optical constants and formation timing remain approximate.']))

if __name__=='__main__':run()
