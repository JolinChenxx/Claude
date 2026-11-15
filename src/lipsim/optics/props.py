"""Optical properties of the LTE Cu–air mixture (W2, PLAN v3 §3.2).

alpha_1064(state)  : laser absorption coefficient [1/m] = e–ion free–free + photoionisation of excited
                     levels (Kramers–Unsöld) + e–neutral free–free (Drude-type with quantum factor),
                     all with stimulated-emission correction; no multiplicative tuning factors.
nec(state, limit)  : net emission coefficient [W/m^3/sr]; limit 'thin' (all NIST lines with A + continuum)
                     or 'trap' (resonance lines removed). Q_rad = 4π·NEC.
pop_table(state)   : n_z / Q_z per element and stage (m^-3), from which imaging computes any level
                     population n_u = (n_z/Q_z) g_u exp(−E_u/kT) exactly.
"""
from __future__ import annotations

from functools import lru_cache
from pathlib import Path

import numpy as np

from lipsim import params as P
from lipsim.eos import mixture as mx
from lipsim.eos.nist_io import read_lines

K_B = P.get("k_B")
H = P.get("h")
C = P.get("c")
E_CH = P.get("e")
M_E = P.get("m_e")
EPS0 = P.get("eps0")
EV = E_CH
NIST = Path(__file__).resolve().parents[3] / "data" / "atomic" / "nist"
LINE_SPECTRA = {"Cu": ["I", "II"], "N": ["I", "II"], "O": ["I", "II"], "Ar": ["I", "II"]}
POP_STAGES = {"Cu": [0, 1, 2], "N": [0, 1], "O": [0, 1], "Ar": [0, 1]}
ROMAN = ["I", "II", "III"]


def _K(nu, T, Z):
    """Kramers coefficient (m^5): κ'_ff = K n_e n_i (no stimulated emission), SI form of Zel'dovich–Raizer."""
    e2 = E_CH ** 2 / (4 * np.pi * EPS0)  # math
    return (4.0 / 3.0) * np.sqrt(2 * np.pi / (3 * M_E * K_B * T)) * Z ** 2 * e2 ** 3 / (H * C * M_E * nu ** 3) * P.get("gaunt_ff")  # math


def alpha_at(state: mx.State, wavelength: float) -> dict:
    T = state.T
    nu = C / wavelength
    x = H * nu / (K_B * T)
    ne = state.ne
    k_ei = 0.0
    for key, n in state.n.items():
        if "+" in key:
            z = int(key.split("+")[1])
            if z > 0:
                k_ei += _K(nu, T, z) * ne * n * np.expm1(x)  # ff+bf (Unsöld), incl. stimulated emission
    # e–neutral: Drude form with momentum-transfer collision frequency, quantum factor (1-e^-x)/x
    v_th = np.sqrt(8 * K_B * T / (np.pi * M_E))  # math
    n_cu0 = state.n.get("Cu+0", 0.0)
    n_air0 = sum(v for k, v in state.n.items() if k in ("N+0", "O+0", "Ar+0", "N2", "O2", "NO"))
    nu_en = v_th * (n_cu0 * P.get("sigma_en_Cu") + n_air0 * P.get("sigma_en_air"))
    omega = 2 * np.pi * nu  # math
    k_en = ne * E_CH ** 2 * nu_en / (EPS0 * M_E * C * omega ** 2) * (-np.expm1(-x)) / x
    return {"ei": k_ei, "en": k_en, "total": k_ei + k_en}


@lru_cache(maxsize=1)
def line_data():
    out = []
    for el, sps in LINE_SPECTRA.items():
        for z, sp in enumerate(sps):
            L = read_lines(NIST / f"{el}_{sp}_lines.tsv", f"{el} {sp}")
            out.append((el, z, L))
    return out


def nec(state: mx.State, limit: str = "thin") -> float:
    """Net emission coefficient W/m^3/sr (lines + continuum)."""
    T = state.T
    kT = K_B * T
    tot = 0.0
    for el, z, L in line_data():
        nz = state.n.get(f"{el}+{z}", 0.0)
        if nz <= 0 or state.Q is None or el not in state.Q:
            continue
        Qz = state.Q[el][z]
        sel = np.ones(len(L.A), bool)
        if limit == "trap":
            sel &= L.Ei > 0.0  # reg:nec_trap_rule (lower level = ground level -> resonance line removed)
        # only upper levels below the lowered ionisation limit exist
        lim = mx.species(el).chi_eV[z] - (z + 1) * state.dchi_eV
        sel &= L.Ek < lim
        nu = C / (L.wl_nm[sel] * 1e-9)  # math (nm -> m)
        tot += np.sum(H * nu * L.A[sel] * nz * L.gk[sel] * np.exp(-L.Ek[sel] * EV / kT) / Qz) / (4 * np.pi)  # math
    # continuum: free–free (Kramers, integrated over ν) + recombination (hydrogenic, P_fb = P_ff χ_z/kT)
    e2 = E_CH ** 2 / (4 * np.pi * EPS0)  # math
    pff_coef = 32 * np.pi / 3 * np.sqrt(2 * np.pi / (3 * M_E)) * e2 ** 3 / (M_E * C ** 3 * H) * np.sqrt(kT) * P.get("gaunt_ff")  # math
    for key, n in state.n.items():
        if "+" in key and n > 0:
            el, z = key.split("+")
            z = int(z)
            if z == 0:
                continue
            p_ff = pff_coef * z ** 2 * state.ne * n
            chi = mx.species(el).chi_eV[z - 1] * EV if z - 1 < len(mx.species(el).chi_eV) else 0.0
            tot += p_ff * (1.0 + chi / kT) / (4 * np.pi)  # reg:fb_over_ff
    return float(tot)


def pop_fields(state: mx.State) -> dict:
    out = {}
    for el, zs in POP_STAGES.items():
        for z in zs:
            nz = state.n.get(f"{el}+{z}", 0.0)
            q = state.Q[el][z] if (state.Q is not None and el in state.Q) else 1.0
            out[f"nQ_{el}{z}"] = nz / q
            out[f"n_{el}{z}"] = nz
    # all ions by charge (continuum emission/absorption needs Σ z² n_z, Σ n_z(z≥1) and the recombination energy)
    zsum = {1: 0.0, 2: 0.0, 3: 0.0}
    for key, n in state.n.items():
        if "+" in key:
            z = int(key.split("+")[1])
            if z in zsum:
                zsum[z] += n
    for z, v in zsum.items():
        out[f"nion_z{z}"] = v
    return out
