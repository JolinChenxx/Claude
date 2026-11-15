"""LTE ionisation equilibrium of an atomic mixture (Saha–Boltzmann with Debye–Hückel IP lowering).

Model form (grade F, to be listed in PREREG): single temperature T; ideal gas of electrons, atoms and
ions; ionisation-potential lowering Δχ_z = (z+1) e² / (4π ε0 λ_D) with λ_D from electrons and ions
(Griem, Plasma Spectroscopy 1964); partition functions are direct sums over NIST bound levels with
E < χ_z − Δχ_z. Molecules (N2, O2, NO, …) are NOT included here; they are added in a separate
module for T < ~8 kK.
"""
from __future__ import annotations

from dataclasses import dataclass, field

import numpy as np
from scipy.optimize import brentq

from lipsim import params
from lipsim.eos.nist_io import Levels

K_B = params.get("k_B")
H = params.get("h")
M_E = params.get("m_e")
E_CH = params.get("e")
EPS0 = params.get("eps0")
EV = E_CH  # J per eV


@dataclass
class Species:
    """One element: levels[z] and ionisation energies chi[z] (eV) for charge states z = 0..Z-1.
    The highest charge state Z is treated as having partition function g_top (its ground level)."""
    name: str
    levels: list[Levels]
    chi_eV: list[float]
    g_top: float = 1.0
    stages: int = field(init=False)

    def __post_init__(self):
        if len(self.levels) != len(self.chi_eV):
            raise ValueError("need one ionisation energy per stage with levels")
        self.stages = len(self.levels) + 1  # includes the bare top stage


def partition_function(lv: Levels, T: float, cutoff_eV: float) -> float:
    sel = lv.energy_eV < cutoff_eV
    return float(np.sum(lv.g[sel] * np.exp(-lv.energy_eV[sel] * EV / (K_B * T))))


def saha_prefactor(T: float) -> float:
    return (2 * np.pi * M_E * K_B * T / H ** 2) ** 1.5  # math


def debye_length(T: float, ne: float, n_ion_z2: float) -> float:
    return np.sqrt(EPS0 * K_B * T / (E_CH ** 2 * max(ne + n_ion_z2, 1e-300)))  # num (guard)


def _fractions(sp: Species, T: float, ne: float, dchi_unit_eV: float):
    """Charge-state fractions of one element for given T, ne and unit IP lowering (eV)."""
    Z = sp.stages
    logs = np.zeros(Z)
    pref = saha_prefactor(T)
    Q = []
    for z in range(Z - 1):
        cut = sp.chi_eV[z] - (z + 1) * dchi_unit_eV
        Q.append(max(partition_function(sp.levels[z], T, cut), 1e-300))  # num (guard)
    Q.append(sp.g_top)
    for z in range(Z - 1):
        chi_eff = sp.chi_eV[z] - (z + 1) * dchi_unit_eV
        logs[z + 1] = logs[z] + np.log(2 * pref * Q[z + 1] / (Q[z] * ne)) - chi_eff * EV / (K_B * T)  # math
    logs -= logs.max()
    f = np.exp(logs)
    return f / f.sum(), np.array(Q)


@dataclass
class SahaResult:
    T: float
    ne: float
    fractions: dict
    partition: dict
    dchi_unit_eV: float
    lambda_D: float


def solve(species: list[Species], n_el: dict[str, float], T: float, ne_guess: float | None = None,
          tol: float = 1e-10, max_outer: int = 50) -> SahaResult:  # num (tol, iterations)
    """Solve charge neutrality for electron density at temperature T and element densities n_el (m^-3)."""
    n_tot = sum(n_el[s.name] for s in species)
    dchi = 0.0
    for _ in range(max_outer):
        def resid(log_ne):
            ne = np.exp(log_ne)
            charge = 0.0
            for s in species:
                f, _ = _fractions(s, T, ne, dchi)
                charge += n_el[s.name] * np.dot(np.arange(s.stages), f)
            return np.log(charge + 1e-300) - log_ne  # num (guard)
        lo, hi = np.log(n_tot * 1e-20 + 1.0), np.log(n_tot * 10.0)  # num (bracket)
        log_ne = brentq(resid, lo, hi, xtol=1e-12)  # num
        ne = np.exp(log_ne)
        fr, Q = {}, {}
        nz2 = 0.0
        for s in species:
            f, q = _fractions(s, T, ne, dchi)
            fr[s.name], Q[s.name] = f, q
            nz2 += n_el[s.name] * np.dot(np.arange(s.stages) ** 2, f)
        lam = debye_length(T, ne, nz2)
        # e/(4π ε0 λ_D) is a potential in volts, i.e. numerically the lowering in eV per unit charge
        new = E_CH / (4 * np.pi * EPS0 * lam) if lam > 0 else 0.0  # math
        if abs(new - dchi) <= tol * max(new, 1e-3):  # num
            dchi = new
            break
        dchi = new
    return SahaResult(T, ne, fr, Q, dchi, lam)
