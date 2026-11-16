"""Thermal conductivity of the LTE Cu–air mixture (v1, PLAN v3 §3.2): electron + heavy-particle
(translational) parts. Reactive (dissociation/ionisation) conductivity is NOT included in v1 and is declared
as a limitation (it raises kappa near the N2/O2 dissociation and ionisation temperatures)."""
from __future__ import annotations

import numpy as np

from lipsim import params as P
from lipsim.eos import mixture as mx

K_B = P.get("k_B")
M_E = P.get("m_e")
AMU = P.get("amu")
E = P.get("e")


def coulomb_log(ne, T):
    """NRL formulary e–i Coulomb logarithm for T_e < 10 Z^2 eV, floored at 2."""
    Tev = K_B * T / E
    ne_cc = max(ne * 1e-6, 1e-30)  # math (m^-3 -> cm^-3)
    return max(23.0 - np.log(np.sqrt(ne_cc) * Tev ** -1.5), 2.0)  # reg:kappa_e_model


def nu_ei(ne, T, nz2):
    """Electron–ion collision frequency (NRL): 2.91e-6 n_e lnΛ T_eV^-1.5, scaled by Σz²n_z/n_e."""
    Tev = K_B * T / E
    if ne <= 0:
        return 0.0
    return 2.91e-6 * (nz2 * 1e-6) * coulomb_log(ne, T) * Tev ** -1.5  # reg:kappa_e_model (NRL, cgs density)


def kappa(state: mx.State) -> dict:
    T, ne = state.T, state.ne
    n = state.n
    nz2 = sum(int(k.split("+")[1]) ** 2 * v for k, v in n.items() if "+" in k and int(k.split("+")[1]) > 0)
    v_th = np.sqrt(8 * K_B * T / (np.pi * M_E))  # math
    n_cu0 = n.get("Cu+0", 0.0)
    n_air0 = sum(v for k, v in n.items() if k in ("N+0", "O+0", "Ar+0", "N2", "O2", "NO"))
    nu_en = v_th * (n_cu0 * P.get("sigma_en_Cu") + n_air0 * P.get("sigma_en_air"))
    nu = nu_ei(ne, T, nz2) + nu_en
    k_e = 3.16 * ne * K_B ** 2 * T / (M_E * nu) if (ne > 0 and nu > 0) else 0.0  # reg:kappa_e_model
    # heavy particles: hard-sphere Chapman–Enskog per species, mole-fraction weighted
    d = P.get("hs_diameter")
    mass = {"Cu": P.get("A_Cu") * AMU, **{k: v * AMU for k, v in P.get("atomic_weights_air").items()}}
    mass.update({"N2": 2 * mass["N"], "O2": 2 * mass["O"], "NO": mass["N"] + mass["O"]})
    tot, kh = 0.0, 0.0
    for key, nk in n.items():
        sp = key.split("+")[0]
        if sp not in d or nk <= 0:
            continue
        ks = 75.0 / 64.0 * K_B / d[sp] ** 2 * np.sqrt(K_B * T / (np.pi * mass[sp]))  # reg:kappa_h_model
        kh += nk * ks
        tot += nk
    k_h = kh / tot if tot > 0 else 0.0
    return {"e": k_e, "h": k_h, "total": k_e + k_h}
