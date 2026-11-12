import json,sys
from pathlib import Path
import numpy as np
ROOT=Path(__file__).resolve().parents[1]
def audit(case):
 p=ROOT/'results'/case
 def read(s):return np.loadtxt(str(p)+'_'+s+'.csv',delimiter=',',skiprows=1,ndmin=2)
 m=read('max');g=read('gas');s=read('solid')[:,0];w=read('wall');wm=read('wallmax');ph=read('photons');far=read('far');t=m[:,0]
 def integ(v):return np.r_[0,np.cumsum((v[1:]+v[:-1])/2*np.diff(t))]
 meta=json.loads(Path(str(p)+'_metadata.json').read_text());incident=integ(ph[:,0]);reflected=integ(ph[:,1]);absorbed=integ(g[:,3]+w[:,2]);rad=integ(g[:,4]);storage=g[:,2]+s;out=integ(far[:,1]);cuout=integ(far[:,0]);massout=integ(far[:,2])
 energy=(storage[-1]+out[-1]+rad[-1]-absorbed[-1])/max(abs(absorbed[-1]),1e-30)
 mass=(g[-1,0]+cuout[-1]-w[-1,1])/max(abs(w[-1,1]),1e-30)
 photon=(absorbed[-1]+reflected[-1]-incident[-1])/max(abs(incident[-1]),1e-30)
 d={'case':case,'metadata':meta,'last_time_ns':t[-1]*1e9,'mass_relative_residual':mass,'energy_relative_residual':energy,'optical_relative_residual':photon,'incident_uJ':incident[-1]*1e6,'absorbed_uJ':absorbed[-1]*1e6,'reflected_uJ':reflected[-1]*1e6,'radiated_uJ':rad[-1]*1e6,'max_recession_um':wm[:,2].max()*1e6,'max_return_fraction_active_evaporation':wm[:,5].max(),'integrated_return_to_evap_ratio':integ(w[:,6])[-1]/max(integ(w[:,5])[-1],1e-30),'raw_Y_min':-m[:,5].max(),'raw_Y_max':m[:,4].max(),'min_density':-m[:,6].max(),'min_raw_thermal_specific_energy':-m[:,7].max(),'peak_T_K':m[:,1].max(),'peak_ne_m3':m[:,2].max(),'peak_speed_m_s':m[:,3].max(),'peak_Cu_ion_fraction':m[:,12].max(),'outer_pressure_disturbance':m[:,19].max(),'outer_Y_max':m[:,20].max()}
 plume_absorbed=integ(g[:,3]);target_absorbed=integ(w[:,2])
 d['plume_absorbed_uJ']=float(plume_absorbed[-1]*1e6);d['target_absorbed_uJ']=float(target_absorbed[-1]*1e6)
 if m.shape[1]>21:
  d['EOS_log_specific_thermal_upper_bound']=float(m[:,21].max())
 checks={'complete':t[-1]>=2.12e-6-1e-15,'mass':abs(mass)<.05,'energy':abs(energy)<.05,'optical':abs(photon)<.01,'positive_density':d['min_density']>0,'positive_raw_thermal':d['min_raw_thermal_specific_energy']>0,'bounded_Y':d['raw_Y_min']>=-1e-4 and d['raw_Y_max']<=1.0001,'EOS':d['min_raw_thermal_specific_energy']>=10**3.8 and m[:,8].max()<=8.5 and -m[:,11].max()>=-7 and m[:,10].max()<=3,'moving_surface_if_needed':bool(meta.get('ALE')) or d['max_recession_um']<=1.1,'outer':d['outer_pressure_disturbance']<.01 and d['outer_Y_max']<1e-5,'optical_bounds':-m[:,14].max()>-1e-4 and m[:,15].max()<1e-4 and -m[:,17].max()>-1e-4 and m[:,18].max()<1e-4,'net_evap_if_needed':meta['netEvap']>.5 or d['max_return_fraction_active_evaporation']<.1}
 activeE=absorbed>max(absorbed.max()*.01,1e-15);activeM=np.abs(w[:,1])>max(np.max(abs(w[:,1]))*.01,1e-25)
 if m.shape[1]>21:checks['EOS_energy_vertex_upper_bound']=m[:,21].max()<=8.5
 er=(storage-storage[0]+out+rad-absorbed)/np.maximum(abs(absorbed),1e-30);mr=(g[:,0]-g[0,0]+cuout-w[:,1])/np.maximum(abs(w[:,1]),1e-30)
 d['max_time_resolved_energy_relative_residual_after_1pct_absorption']=float(np.max(abs(er[activeE]))) if activeE.any() else None
 d['max_time_resolved_copper_relative_residual_after_1pct_ablation']=float(np.max(abs(mr[activeM]))) if activeM.any() else None
 checks['energy_time_resolved']=bool(activeE.any() and np.max(abs(er[activeE]))<.05);checks['mass_time_resolved']=bool(activeM.any() and np.max(abs(mr[activeM]))<.05)
 if meta.get('ALE'):
  al=read('ale');am=read('alemax');d['ALE_min_J']=-am[:,0].max();d['ALE_GCL_identity_max']=am[:,1].max()
  d['ALE_volume_relative_residual']=(al[-1,0]-al[0,0]-w[-1,1]/8940)/max(abs(w[-1,1]/8940),1e-30)
  d['total_mass_relative_residual']=(al[-1,3]+massout[-1]-w[-1,1])/max(abs(w[-1,1]),1e-30)
  checks['ALE_J_positive']=d['ALE_min_J']>0;checks['ALE_GCL_volume']=abs(d['ALE_volume_relative_residual'])<.05
  checks['total_mass']=abs(d['total_mass_relative_residual'])<.05
 d['checks']={k:bool(v) for k,v in checks.items()};d['status']='passed' if all(checks.values()) else 'diagnostic_needs_revision'
 Path(str(p)+'_audit.json').write_text(json.dumps(d,indent=2)+'\n')
 np.savetxt(str(p)+'_budget.csv',np.column_stack([t,incident,reflected,absorbed,storage,out,rad,g[:,0],w[:,1],cuout,plume_absorbed,target_absorbed]),delimiter=',',header='t_s,incident_J,reflected_J,absorbed_J,stored_J,outflow_J,radiated_J,copper_gas_kg,net_ablated_kg,copper_outflow_kg,plume_absorbed_J,target_absorbed_J',comments='')
 print(json.dumps(d,indent=2));return d
if __name__=='__main__':audit(sys.argv[1])
