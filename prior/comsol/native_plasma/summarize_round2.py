"""Saved-state balances for fixed-plane stages; never use geometric target mass."""
from pathlib import Path
import sys,json,csv
import numpy as np
R=Path(__file__).resolve().parent;case=sys.argv[1]
def read(suffix):
    with (R/'results'/f'{case}_{suffix}.csv').open() as f:
        rows=csv.reader(f);head=next(rows);a=np.array(list(rows),dtype=float)
    return head,np.atleast_2d(a)
_,h=read('history');_,b=read('balances');_,m=read('mesh_audit');mh,mech=read('mechanism')
t=h[:,0]
integral=lambda a:float(np.trapz(a,t))
massidx=mh.index('c.flatMassNg*1e-12[kg]')
feed=float(mech[-1,massidx]-mech[0,massidx]);gas=float(h[-1,8]-h[0,8]);cuout=integral(b[:,1])
q=h[:,18]+h[:,19];qout=integral(b[:,2]);qres=float(q[-1]-q[0]+qout)
absorbed=integral(h[:,13]+h[:,14]);stored=float(np.sum(h[-1,10:13]-h[0,10:13]))
rad=integral(h[:,15]);work=integral(h[:,17]);outer=integral(b[:,3]+b[:,4])
bad=(m[:,1]<=0)|(m[:,2]<=0)|(m[:,3]<=0)
def percent(x,den):return float(x/den*100) if abs(den)>1e-30 else None
out={'case':case,'stage':'fixed target plane','interval_ns':[float(t[0]*1e9),float(t[-1]*1e9)],
 'saved_mesh_times_checked':len(m),'saved_mesh_invalid_times_ns':(m[bad,0]*1e9).tolist(),
 'minimum_relative_volume':float(m[:,3].min()),'minimum_quality':float(m[:,2].min()),
 'Cu':{'integrated_feed_kg':feed,'gas_change_kg':gas,'outer_loss_kg':cuout,'gas_vs_feed_percent':percent(gas+cuout-feed,feed),
       'note':'Target loss is an integrated flux ledger; unchanged geometric target volume is not an independent mass test.'},
 'charge':{'initial_total_pC':float(q[0]*1e12),'final_total_pC':float(q[-1]*1e12),'outer_charge_pC':qout*1e12,
           'residual_percent_of_peak_metal_charge':percent(qres,float(np.max(np.abs(h[:,18]))))},
 'energy':{'absorbed_J':absorbed,'target_absorbed_J':integral(h[:,13]),'plume_absorbed_J':integral(h[:,14]),
           'stored_change_J':stored,'radiated_J':rad,'net_work_function_J':work,'outer_heat_and_energy_J':outer,
           'residual_percent':percent(stored+rad+work+outer-absorbed,absorbed),
           'note':'Fixed boundary has zero mechanical surface work. No extra subtraction of the condensed-enthalpy ledger.'},
 'peaks':{'surface_temperature_K':float(h[:,1].max()),'gas_temperature_K':float(h[:,4].max()),
          'electron_temperature_K':float(h[:,5].max()),'electron_density_m3':float(h[:,3].max())},
 'limitations':['Fixed target plane and disabled melt flow','Saved-node mesh checks do not replace convergence tests',
                'Material validity at the achieved temperature must be assessed separately','No independent electron density measurement']}
if 'c.mechMax(c.tailRelevantFraction)' in mh:
    out['tail_QSS']={'maximum_upper_over_parent_in_relevant_cells':float(mech[:,mh.index('c.mechMax(c.tailRelevantFraction)')].max()),
                    'maximum_relaxation_ns_in_relevant_cells':float(mech[:,mh.index('c.mechMax(c.tailRelevantLifetime)')].max()*1e9),
                    'relevant_cells':'parent population >1e15 m^-3 and gas T>1000 K; sampled diagnostic times'}
(R/'results'/f'{case}_verified_summary.json').write_text(json.dumps(out,indent=2),encoding='utf8')
print(json.dumps(out,indent=2))
