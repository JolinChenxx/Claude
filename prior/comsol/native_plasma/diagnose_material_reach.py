"""Separate material-front reach from optical reach in actual saved fields."""
from pathlib import Path
import sys,json,numpy as np
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from split_history import read_history
from split_saved_emission import visible_emissivity

def run(case):
    g=np.load(R/'results'/case/'grid.npz');r=g['r_m']*1e6;z=g['z_m']*1e6;v=g['cell_volume_m3'];rows=[]
    for h in read_history(case):
        if h['saved_time_ns']<80:continue
        with np.load(h['frame']) as d:
            U,W,S=d['U'],d['W'],d['S'];gas=U[:,:,0]*W[:,:,5];cond=U[:,:,0]-gas;j=visible_emissivity(S,W)
            record=dict(model_ns=h['saved_time_ns'],gas_mass_ng=float((gas*v).sum()*1e12),visible_power_W=float((j*v).sum()*4*np.pi),gas_fronts={})
            for cutoff in [1e-3,.1,10.]:
                mask=(gas>=cutoff)&(z[None,:]>0);ii,jj=np.where(mask)
                record['gas_fronts'][str(cutoff)]=dict(radial_um=float(r[ii].max()) if len(ii) else 0.,height_um=float(z[jj].max()) if len(ii) else 0.)
            luminous=j*v;total=luminous.sum()
            record['fraction_visible_above_50um']=float(luminous[:,z>50].sum()/max(total,1e-100))
            record['fraction_visible_radius_above_50um']=float(luminous[r>50,:].sum()/max(total,1e-100))
            record['gas_mass_above_50um_ng']=float((gas*v)[:,z>50].sum()*1e12)
            record['gas_mass_radius_above_50um_ng']=float((gas*v)[r>50,:].sum()*1e12)
            record['condensed_mass_above_initial_plane_ng']=float((cond*v)[:,z>0].sum()*1e12)
            rows.append(record)
    report=dict(case=case,units='Front thresholds in kg/m3, coordinates relative to initial target plane.',rows=rows)
    (R/'results'/case/'material_reach.json').write_text(json.dumps(report,indent=2));print(json.dumps(dict(case=case,last=rows[-1] if rows else None)))

if __name__=='__main__':
    for case in sys.argv[1:]:run(case)
