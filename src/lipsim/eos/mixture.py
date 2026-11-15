"""LTE equilibrium of the Cu–air mixture: atoms/ions (Saha–Boltzmann, Debye–Hückel lowering) plus
the diatomic molecules N2, O2, NO (rigid rotor – harmonic oscillator + listed low electronic states).

Model-form choices (grade F, listed in PREREG_v0): single temperature; ideal-gas mixture; molecular
ions (N2+, O2+, NO+) and other molecules (NO2, N2O, O3, CuO …) neglected; molecular partition
functions RRHO with constants from the registry; atomic partition functions are direct level sums
truncated at the lowered ionisation energy.

Energy reference: elements in their cold standard form (N2, O2 molecules in v=0,J=0; Cu, Ar ground-state
atoms) have zero internal energy, so cold ambient air has e ≈ 5/2 R T and all energies are positive.
"""
from __future__ import annotations

from dataclasses import dataclass
from functools import lru_cache
from pathlib import Path

import numpy as np
from scipy.optimize import brentq

from lipsim import params
from lipsim.eos import saha
from lipsim.eos.nist_io import read_ionization, read_levels
from lipsim.eos.saha import EV, H, K_B, M_E

C_LIGHT = params.get("c")
AMU = params.get("amu")
NIST = Path(__file__).resolve().parents[3] / "data" / "atomic" / "nist"
ROMAN = ["I", "II", "III", "IV", "V", "VI"]
MASS_AMU = {"Cu": params.get("A_Cu"), **params.get("atomic_weights_air")}
STAGES = {"Cu": 5, "N": 4, "O": 4, "Ar": 3}  # num: highest stage with NIST levels; the next one is a bare top stage
CM1_TO_EV = H * C_LIGHT * 100 / EV  # math (cm^-1 -> eV)


@lru_cache(maxsize=None)
def species(el: str, nist_dir: str = str(NIST)) -> saha.Species:
    d = Path(nist_dir)
    ie = read_ionization(d / f"{el}_ionization.csv")
    names = [f"{el} {ROMAN[k]}" for k in range(STAGES[el])]
    lv = [read_levels(d / f"{n.replace(' ', '_')}_levels.csv", n) for n in names]
    return saha.Species(el, lv, [ie[n] for n in names], g_top=1.0)


@dataclass(frozen=True)
class Diatomic:
    name: str
    a: str
    b: str
    D0_eV: float
    we_cm: float
    Be_cm: float
    sigma: int
    elec: tuple  # ((E_eV, g), ...)

    def q_int(self, T: float) -> float:
        """Internal partition function referenced to v=0, J=0 of the ground electronic state."""
        x = K_B * T / EV
        q_rot = x / (self.sigma * self.Be_cm * CM1_TO_EV)
        q_vib = 1.0 / (1.0 - np.exp(-self.we_cm * CM1_TO_EV / x))
        q_el = sum(g * np.exp(-E / x) for E, g in self.elec)
        return q_rot * q_vib * q_el

    def mean_energy_eV(self, T: float) -> float:
        """Mean rot+vib+elec energy above v=0,J=0 (eV); translation excluded."""
        x = K_B * T / EV
        hv = self.we_cm * CM1_TO_EV
        e_vib = hv / (np.exp(hv / x) - 1.0)
        w = [g * np.exp(-E / x) for E, g in self.elec]
        e_el = sum(wi * E for wi, (E, _) in zip(w, self.elec)) / sum(w)
        return x + e_vib + e_el  # rotational kT (linear molecule) + vib + elec


def _mol(name, a, b, key):
    v = params.get(key)
    return Diatomic(name, a, b, v["D0_eV"], v["we_cm"], v["Be_cm"], int(v["sigma"]),
                    tuple((float(E), int(g)) for E, g in v["elec"]))


@lru_cache(maxsize=1)
def molecules():
    return (_mol("N2", "N", "N", "mol_N2"), _mol("O2", "O", "O", "mol_O2"), _mol("NO", "N", "O", "mol_NO"))


def air_element_fractions() -> dict:
    """Element number fractions of dry air (atoms), from the registered molecular mole fractions."""
    m = params.get("air_mole_fractions")
    tot = m["N2"] + m["O2"] + m["Ar"]
    nN, nO, nAr = 2 * m["N2"] / tot, 2 * m["O2"] / tot, m["Ar"] / tot  # math
    s = nN + nO + nAr
    return {"N": nN / s, "O": nO / s, "Ar": nAr / s}


def element_densities(rho: float, Y_cu: float) -> dict:
    """Element number densities (m^-3) for mass density rho (kg/m^3) and Cu mass fraction Y_cu."""
    fa = air_element_fractions()
    m_air_atom = sum(fa[e] * MASS_AMU[e] for e in fa) * AMU
    n_air_atoms = rho * (1.0 - Y_cu) / m_air_atom
    out = {e: fa[e] * n_air_atoms for e in fa}
    out["Cu"] = rho * Y_cu / (MASS_AMU["Cu"] * AMU)
    return out


def _stage_logratios(sp: saha.Species, T: float, ne: float, dchi: float):
    """log(n_z / n_0) for all stages, and the stage partition functions Q_z (Q_0 = atom)."""
    lpref = np.log(2 * saha.saha_prefactor(T) / ne)  # math
    Q = []
    for z in range(sp.stages - 1):
        Q.append(max(saha.partition_function(sp.levels[z], T, sp.chi_eV[z] - (z + 1) * dchi), 1e-300))  # num
    Q.append(sp.g_top)
    lr = np.zeros(sp.stages)
    for z in range(sp.stages - 1):
        chi = sp.chi_eV[z] - (z + 1) * dchi
        lr[z + 1] = lr[z] + lpref + np.log(Q[z + 1] / Q[z]) - chi * EV / (K_B * T)
    return lr, np.array(Q)


def _lse(v):
    m = np.max(v)
    return m + np.log(np.sum(np.exp(v - m)))


def _logK_diss(mol: Diatomic, Qa: float, Qb: float, T: float) -> float:
    """log of n_a n_b / n_mol (m^-3) with atomic Q's referenced to their ground levels."""
    mu = MASS_AMU[mol.a] * MASS_AMU[mol.b] / (MASS_AMU[mol.a] + MASS_AMU[mol.b]) * AMU
    ltrans = 1.5 * np.log(2 * np.pi * mu * K_B * T / H ** 2)  # math
    return ltrans + np.log(Qa * Qb / mol.q_int(T)) - mol.D0_eV * EV / (K_B * T)


@dataclass
class State:
    T: float
    rho: float
    Y_cu: float
    ne: float
    n: dict          # species number densities: 'Cu+0'.., 'N2','O2','NO'
    p: float
    u: float         # internal energy density J/m^3 (reference: cold N2, O2, Cu, Ar)
    e: float         # specific internal energy J/kg
    dchi_eV: float
    Q: dict = None   # element -> array of stage partition functions (truncated, ground-referenced)


def equilibrium(rho: float, T: float, Y_cu: float, max_outer: int = 30) -> State:  # num
    nel = element_densities(rho, Y_cu)
    els = [e for e in ("Cu", "N", "O", "Ar") if nel[e] > 0]
    sps = {e: species(e) for e in els}
    mols = [m for m in molecules() if nel.get(m.a, 0) > 0 and nel.get(m.b, 0) > 0]
    n_tot = sum(nel.values())
    dchi = 0.0
    for _ in range(max_outer):

        def solve_atoms(ne):
            """Returns charge density and, per element, A_e = density in all atomic/ionic forms,
            log(n_0/A_e), stage fractions f_z, Q_z, and log K of each molecule."""
            LR, Q, S, F = {}, {}, {}, {}
            for e in els:
                LR[e], Q[e] = _stage_logratios(sps[e], T, ne, dchi)
                S[e] = _lse(LR[e])
                F[e] = np.exp(LR[e] - S[e])
            LK = {m.name: _logK_diss(m, Q[m.a][0], Q[m.b][0], T) for m in mols}
            A = {e: float(nel[e]) for e in els}
            for _it in range(300):  # num: fixed point over the coupled quadratics
                old = dict(A)
                for e in ("N", "O"):
                    if e not in A:
                        continue
                    a, b = 0.0, 1.0
                    for m in mols:
                        if m.a == e and m.b == e:
                            a += 2.0 * np.exp(-(LK[m.name] + 2 * S[e]))  # math
                        elif e in (m.a, m.b):
                            o = m.b if m.a == e else m.a
                            b += old[o] * np.exp(-(LK[m.name] + S[e] + S[o]))
                    c = nel[e]
                    A[e] = 2 * c / (b + np.sqrt(b * b + 4 * a * c))  # math (stable root of a A^2 + b A - c = 0)
                if all(abs(A[e] - old[e]) <= 1e-13 * old[e] for e in A):  # num
                    break
            charge = sum(A[e] * np.dot(np.arange(sps[e].stages), F[e]) for e in els)
            return charge, A, S, F, Q, LK

        def resid(log_ne):
            ch = solve_atoms(np.exp(log_ne))[0]
            return np.log(ch + 1e-300) - log_ne  # num
        lo, hi = -600.0, np.log(n_tot * 10.0)  # num (ne from ~1e-261 m^-3 up to 10 n_tot)
        log_ne = brentq(resid, lo, hi, xtol=1e-12)  # num
        ne = np.exp(log_ne)
        charge, A, S, F, Q, LK = solve_atoms(ne)
        nz2 = sum(A[e] * np.dot(np.arange(sps[e].stages) ** 2, F[e]) for e in els)
        lam = saha.debye_length(T, ne, nz2)
        # F-grade choice (PREREG_v0): screening length not below the ion-sphere radius, which bounds
        # Debye–Hückel lowering in the strongly coupled corner of the table (dense vapour near the target)
        n_heavy = sum(A[e] for e in els)
        a_is = (3.0 / (4 * np.pi * max(n_heavy, 1.0))) ** (1.0 / 3.0)  # math
        new = saha.E_CH / (4 * np.pi * saha.EPS0 * max(lam, a_is))  # math (volts == eV per charge)
        if abs(new - dchi) <= 1e-8 * max(new, 1e-6):  # num
            dchi = new
            break
        dchi = new

    # assemble species and energy
    x = K_B * T / EV
    n = {}
    u_eV = 0.0  # J/m3 accumulated in eV units then converted
    n_heavy = 0.0
    ref_shift = {"N": params.get("mol_N2")["D0_eV"] / 2, "O": params.get("mol_O2")["D0_eV"] / 2, "Cu": 0.0, "Ar": 0.0}  # math
    for e in els:
        sp = sps[e]
        cum = 0.0
        for z in range(sp.stages):
            nz = A[e] * F[e][z]
            if z > 0:
                cum += sp.chi_eV[z - 1] - z * dchi
            exc = 0.0
            if z < sp.stages - 1:
                from lipsim.eos.thermo import mean_excitation_eV
                exc = mean_excitation_eV(sp.levels[z], T, sp.chi_eV[z] - (z + 1) * dchi)
            n[f"{e}+{z}"] = nz
            u_eV += nz * (1.5 * x + cum + exc + ref_shift[e])  # math
            n_heavy += nz
    for m in mols:
        nm = A[m.a] * A[m.b] * np.exp(-(LK[m.name] + S[m.a] + S[m.b]))
        n[m.name] = nm
        # energy relative to separated ground atoms is -D0; add the element reference shifts
        u_eV += nm * (1.5 * x + m.mean_energy_eV(T) - m.D0_eV + ref_shift[m.a] + ref_shift[m.b])  # math
        n_heavy += nm
    u_eV += ne * 1.5 * x  # math
    p = (n_heavy + ne) * K_B * T
    u = u_eV * EV
    return State(T, rho, Y_cu, ne, n, p, u, u / rho, dchi, Q)
