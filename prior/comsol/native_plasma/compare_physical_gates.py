"""Compare actual complete exposures with one physical radiance color scale."""
from pathlib import Path
import json, sys
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm

R = Path(__file__).resolve().parent
sys.path.insert(0, str(R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project/work'))
from camera_s4 import gate, blur

cases = sys.argv[1:]
zero = 90.0
rows, frames = [], []
for case in cases:
    a = np.load(R/'results'/f'{case}_radiation/radiance.npz')
    t = a['times_s']*1e9
    if t[0] > zero-5+1e-6 or t[-1] < zero+5-1e-6:
        raise ValueError(f'{case}: incomplete gate')
    q = blur(gate(t, a['radiance'], [zero]), 1)[0]
    x, z = a['x_um'], a['z_um']
    if frames and (not np.array_equal(x, frames[0][1]) or not np.array_equal(z, frames[0][2])):
        raise ValueError('Different image coordinates')
    px = float(np.abs(np.diff(x)).mean())
    photons = q*(2*np.pi*(1-np.sqrt(1-.4**2)))*(px*1e-6)**2*10e-9*525e-9/(6.62607015e-34*299792458.)
    total = q.sum()
    radial_order = np.argsort(np.abs(x))
    radial_cumulative = np.cumsum(q.sum(axis=0)[radial_order])
    r90 = abs(x[radial_order[np.searchsorted(radial_cumulative, .9*total)]]) if total else None
    rows.append(dict(case=case, peak_radiance_W_m2_sr=float(q.max()),
                     ideal_photons=float(photons.sum()), pixels_ge_one_photon=int((photons>=1).sum()),
                     photon_radius90_um=float(r90) if r90 is not None else None,
                     photon_centroid_z_um=float((q.sum(axis=1)*z).sum()/total) if total else None))
    frames.append((q, x, z))

fig, ax = plt.subplots(1, len(cases), figsize=(6*len(cases), 6), squeeze=False, layout='constrained')
peak = max(q.max() for q, _, _ in frames)
for p, (q, x, z), row in zip(ax[0], frames, rows):
    image = p.imshow(np.maximum(q, peak*1e-8), origin='upper',
                     extent=[x[0]-px/2, x[-1]+px/2, z[-1]-px/2, z[0]+px/2],
                     norm=LogNorm(max(peak*1e-6, 1e-30), peak), cmap='inferno')
    p.axhline(0, color='cyan', lw=.7, label='Initial copper plane')
    p.set(xlabel='x [um]', ylabel='z [um]', title=f"{row['case']}\nPeak {row['peak_radiance_W_m2_sr']:.3g} W/m2/sr; ideal photons {row['ideal_photons']:.3g}\n90% photon radius {row['photon_radius90_um']:.2f} um")
fig.colorbar(image, ax=list(ax[0]), label='W / m2 / sr; shared physical scale')
fig.suptitle('Complete 85-95 ns exposure; assumed camera zero = model 90 ns\n475-525 nm; same PSF; no fitted gain; bulk emission >=0.1 um above moving copper')
out=R/'figures'/'physical_comparison'; out.mkdir(exist_ok=True)
fig.savefig(out/'first_gate_comparison.png', dpi=170)
report=dict(model_gate_ns=[85,95],camera_zero_assumed_ns=90,rows=rows,
            limitations=['Ideal collection NA=0.4, throughput=QE=1, not measured counts',
                         'Photon radius is an integrated diagnostic, not the paper gray-level radius',
                         'Near-surface excluded layer remains incompletely audited',
                         'Gas source supplied by independent copper prediction; not converged two-way coupling'])
(R/'results'/'first_gate_comparison.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
