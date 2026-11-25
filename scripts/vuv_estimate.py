"""A-priori check for radiative transfer (PREREG v1t candidate): fraction of the plasma emission below 175 nm
(absorbed by cold O2 in the Schumann-Runge continuum within ~0.2 mm) at plume-core states, lines + continuum,
with the same NIST line data and continuum formulae as optics.props.nec (thin limit).

    python scripts/vuv_estimate.py
"""
import sys
from pathlib import Path

import numpy as np

REPO = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(REPO / "src"))
from lipsim.eos import mixture as mx  # noqa: E402
from lipsim.optics import props as op  # noqa: E402

LC = 175e-9   # m, red edge of the O2 Schumann-Runge continuum (absorption onset)


def split(state):
    T = state.T
    kT = op.K_B * T
    lines_tot = lines_uv = 0.0
    for el, z, L in op.line_data():
        nz = state.n.get(f"{el}+{z}", 0.0)
        if nz <= 0 or el not in state.Q:
            continue
        Qz = state.Q[el][z]
        lim = mx.species(el).chi_eV[z] - (z + 1) * state.dchi_eV
        sel = L.Ek < lim
        wl = L.wl_nm[sel] * 1e-9
        e = op.H * op.C / wl * L.A[sel] * nz * L.gk[sel] * np.exp(-L.Ek[sel] * op.EV / kT) / Qz / (4 * np.pi)
        lines_tot += e.sum(); lines_uv += e[wl < LC].sum()
    cont = op.nec(state, "thin") - lines_tot
    xc = op.H * op.C / LC / kT
    # ff: flat in nu x exp(-h nu/kT) -> fraction above nu_c = exp(-xc); fb (hydrogenic, edges at chi): same tail shape,
    # take the same fraction (lower bound for fb, whose spectrum starts at the edge)
    cont_uv = cont * np.exp(-xc)
    return lines_tot, lines_uv, cont, cont_uv


for rho, T, Y in [(0.1, 30000, 1.0), (0.1, 20000, 1.0), (0.15, 12000, 1.0), (0.18, 7000, 1.0), (0.1, 20000, 0.5)]:
    s = mx.equilibrium(rho, T, Y)
    lt, lu, ct, cu = split(s)
    print(f"rho {rho} T {T} Y {Y}: total {4*np.pi*(lt+ct):.3e} W/m3, VUV fraction {(lu+cu)/(lt+ct):.2f} (lines {lu/(lt+ct):.2f}, cont {cu/(lt+ct):.2f})")
