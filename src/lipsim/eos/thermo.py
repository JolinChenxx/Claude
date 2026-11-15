"""Thermodynamic state of an LTE atomic plasma mixture from the Saha solution.

Per unit volume (heavy particles + electrons, ideal gas, Debye–Hückel pressure/energy corrections
omitted at this stage and listed as a model-form choice in PREREG):
  p = (Σ n_heavy + n_e) k T
  u = 3/2 p + Σ_species Σ_z n_z [ E_ion,cum(z) + <E_exc>_z ]
where E_ion,cum(z) = Σ_{j<z} (χ_j − (j+1)Δχ) and <E_exc>_z = k T² d ln Q_z / dT (Boltzmann mean
excitation energy, computed from the same truncated level sums as Q).
"""
from __future__ import annotations

import numpy as np

from lipsim.eos import saha
from lipsim.eos.saha import EV, K_B


def mean_excitation_eV(lv, T: float, cutoff_eV: float) -> float:
    sel = lv.energy_eV < cutoff_eV
    w = lv.g[sel] * np.exp(-lv.energy_eV[sel] * EV / (K_B * T))
    s = w.sum()
    return float((w * lv.energy_eV[sel]).sum() / s) if s > 0 else 0.0


def state(species, n_el: dict, T: float):
    """Return dict with p (Pa), u (J/m3, internal energy incl. ionisation+excitation), ne, zbar, result."""
    r = saha.solve(species, n_el, T)
    n_heavy = sum(n_el.values())
    p = (n_heavy + r.ne) * K_B * T
    u_chem = 0.0
    for s in species:
        f = r.fractions[s.name]
        cum = 0.0
        for z in range(s.stages):
            if z > 0:
                cum += s.chi_eV[z - 1] - z * r.dchi_unit_eV
            exc = 0.0
            if z < s.stages - 1:
                exc = mean_excitation_eV(s.levels[z], T, s.chi_eV[z] - (z + 1) * r.dchi_unit_eV)
            u_chem += n_el[s.name] * f[z] * (cum + exc) * EV
    u = 1.5 * p + u_chem  # math
    return {"p": p, "u": u, "ne": r.ne, "zbar": r.ne / n_heavy, "saha": r}
