"""All-domain/all-direction visible photon bound independent of camera gain/FOV."""
import argparse,csv,json
from pathlib import Path
import numpy as np
from camera_s4 import gate
ROOT=Path(__file__).resolve().parents[1]

def run(case):
 x=np.loadtxt(ROOT/f'results/{case}_visible_budget.csv',delimiter=',',skiprows=1)
 times=x[:,0]*1e9;power=x[:,1:3].sum(1)[:,None,None]
 e=np.load(ROOT/'experiment/S4_reconstructed.npz');rows=[]
 # 781 nm is conservatively beyond 780 nm in air after vacuum conversion.
 shifts=np.arange(60,122,2);factor=10e-9*781e-9/(6.62607015e-34*299792458.)
 counts=np.array([gate(times,power,e['times_ns']+shift)[:,0,0]*factor for shift in shifts])
 for i,ns in enumerate(e['times_ns']):
  upper=float(counts[:,i].max());rows.append(dict(experimental_time_ns=int(ns),max_emitted_visible_photons_over_allowed_shifts=upper,shift_at_max_ns=int(shifts[counts[:,i].argmax()]),bound_on_probability_of_any_collected_photon=min(1.,upper)))
 result={'case':case,'scope':'380–780 nm NIST atomic lines plus free-free closure; full finite-element gas domain, all emitted directions, no absorption, perfect collection/QE. Does not bound omitted radiation mechanisms.','gate_ns':10,'shift_scan_ns':shifts.tolist(),'photon_energy_lower_bound_wavelength_nm':781,'derivation':'Integrate total unattenuated visible emission over each gate and divide by hc/(781 nm), conservatively beyond the air-to-vacuum converted 780 nm endpoint. Maximize across every allowed time shift. Camera collection and absorption can only reduce this closure-specific emitted-photon bound. P(at least one collected photon) <= min(1, expected emitted photons), without a Poisson assumption.','frames':rows}
 (ROOT/f'results/{case}_photon_upper_bound.json').write_text(json.dumps(result,indent=2))
 with (ROOT/f'results/{case}_photon_upper_bound.csv').open('w',newline='') as f:
  w=csv.DictWriter(f,fieldnames=rows[0]);w.writeheader();w.writerows(rows)
 print(json.dumps([r for r in rows if r['experimental_time_ns'] in [80,150,260,400,740]],indent=2))
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('case');run(p.parse_args().case)
