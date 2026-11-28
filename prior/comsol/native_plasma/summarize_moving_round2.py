"""Moving-domain inventories and the implemented mechanical energy terms."""
from pathlib import Path
import sys,json,numpy as np
R=Path(__file__).resolve().parent;case=sys.argv[1]
read=lambda suffix:np.loadtxt(R/'results'/f'{case}_{suffix}.csv',delimiter=',',skiprows=1,ndmin=2)
h=read('history');b=read('balances');w=read('moving_work');mesh=read('mesh_audit');t=h[:,0]
integral=lambda v:float(np.trapz(v,t))
target_loss=float(h[0,9]-h[-1,9]);gas_change=float(h[-1,8]-h[0,8]);cuout=integral(b[:,1])
qt=h[:,18]+h[:,19];charge=float(qt[-1]-qt[0])+integral(b[:,2]);absorbed=integral(h[:,13]+h[:,14]);stored=float(np.sum(h[-1,10:13]-h[0,10:13]))
work_terms=np.trapz(w[:,1:7],w[:,0],axis=0);mechanical=float(work_terms.sum());outer=integral(b[:,3]+b[:,4]);rad=integral(h[:,15]);wf=integral(h[:,17]);res=stored+rad+wf+outer-absorbed-mechanical
ratio=lambda a,b:float(a/b*100) if abs(b)>1e-30 else None
out={'case':case,'stage':'real moving surface','saved_interval_ns':(t[[0,-1]]*1e9).tolist(),'Cu':{'geometric_target_loss_kg':target_loss,'gas_Cu_change_kg':gas_change,'outer_loss_kg':cuout,'total_inventory_residual_percent_of_target_loss':ratio(gas_change+cuout-target_loss,target_loss),'surface_flux_trapezoid_kg':integral(h[:,16]),'note':'Early evaporated mass is tiny compared with target inventory; coarse saved-time flux quadrature is a separate diagnostic'},'charge':{'residual_percent_of_peak_metal_charge':ratio(charge,np.max(abs(h[:,18])))},'energy':{'absorbed_J':absorbed,'stored_change_J':stored,'radiated_J':rad,'work_function_J':wf,'outer_flux_J':outer,'mechanical_contribution_J':mechanical,'mechanical_terms_J':dict(zip(['gas_pressure_work','liquid_traction_work','capillary_work','removed_liquid_kinetic','Darcy_loss','viscous_loss'],work_terms.tolist())),'residual_percent_including_mechanical_terms':ratio(res,absorbed)},'mesh':{'minimum_relative_volume':float(mesh[:,3].min()),'invalid_saved_times_ns':(mesh[(mesh[:,1]<=0)|(mesh[:,2]<=0)|(mesh[:,3]<=0),0]*1e9).tolist()},'limits':['Mechanical terms audit the implemented equations; dissipative losses are not automatically assumed returned to target heat','Output-time quadrature remains to be refined','High-temperature target closure and spatial convergence are not validated','No independent electron density measurement']}
(R/'results'/f'{case}_moving_summary.json').write_text(json.dumps(out,indent=2));print(json.dumps(out,indent=2))
