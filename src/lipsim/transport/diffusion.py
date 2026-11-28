"""Binary Cu–air interdiffusion of the LTE mixture (v1 step 2, STATE 2026-11-17 21:15).

Model (registry `diff_cuair_model`, grade F): Fick's law for the Cu mass fraction, J_Cu = −ρ D ∇Y, with the
first Chapman–Enskog hard-sphere binary coefficient between the Cu pseudo-species (Cu atoms and ions) and the
air pseudo-species (N2, O2, NO, N, O, Ar and their ions):

    D = 3 / (8 n d12²) · sqrt(k T / (2π μ)),   d12 = (d_Cu + d_air)/2,   μ = m_Cu m_air / (m_Cu + m_air)

where n is the total heavy-particle number density, d_air and m_air are mole-fraction means over the air species,
and the diameters are registry `hs_diameter` (the same as for kappa_h). Declared limitations: ions are treated
as hard spheres (no polarisation cross-section, no explicit ambipolar field), and there is no thermal diffusion.

The energy equation carries the interdiffusion enthalpy flux (h_Cu − h_air) J_Cu. For the binary LTE mixture
h_Cu − h_air = ∂h/∂Y at fixed p and T (h = e + p/ρ, energies with the mixture.py reference), tabulated as `dh_dY`.
"""
from __future__ import annotations

import numpy as np

from lipsim import params as P
from lipsim.eos import mixture as mx

K_B = P.get("k_B")
AMU = P.get("amu")
AIR = ("N2", "O2", "NO", "N", "O", "Ar")


def _mass(sp: str) -> float:
    w = P.get("atomic_weights_air")
    if sp == "Cu":
        return P.get("A_Cu") * AMU
    if sp in w:
        return w[sp] * AMU
    return sum(w[a] for a in sp if a.isalpha() and a.isupper() and a in w) * AMU * (2 if sp in ("N2", "O2") else 1)  # math (diatomic masses)


def D_binary(state: mx.State) -> float:
    """Cu–air binary diffusion coefficient [m^2/s] (hard-sphere Chapman–Enskog, first approximation)."""
    d = P.get("hs_diameter")
    n_cu, n_air, m_air, d_air = 0.0, 0.0, 0.0, 0.0
    for key, nk in state.n.items():
        sp = key.split("+")[0]
        if nk <= 0 or sp not in d:
            continue
        if sp == "Cu":
            n_cu += nk
        elif sp in AIR:
            n_air += nk
            m_air += nk * _mass(sp)
            d_air += nk * d[sp]
    n = n_cu + n_air
    if n <= 0:
        return 0.0
    if n_air > 0:
        m_air, d_air = m_air / n_air, d_air / n_air
    else:  # pure Cu: trace air behaves as N2 (limit Y -> 1)
        m_air, d_air = _mass("N2"), d["N2"]
    m_cu = _mass("Cu")
    mu = m_cu * m_air / (m_cu + m_air)
    d12 = 0.5 * (d["Cu"] + d_air)  # math
    return 3.0 / (8.0 * n * d12 ** 2) * np.sqrt(K_B * state.T / (2.0 * np.pi * mu))  # reg:diff_cuair_model


def _rho_at_p(p: float, T: float, Y: float, rho0: float) -> tuple[float, mx.State]:
    rho = rho0
    for _ in range(6):  # num (fixed-point iterations on rho at fixed p, T; p is close to linear in rho)
        s = mx.equilibrium(rho, T, Y)
        if abs(s.p / p - 1.0) < 1e-9:  # num
            break
        rho *= p / s.p
    return rho, s


def dh_dY(rho: float, T: float, Y: float, dY: float = 0.01) -> float:  # num (finite-difference step)
    """h_Cu − h_air = ∂h/∂Y |_{p,T} [J/kg] for the LTE mixture (central difference, one-sided at the ends)."""
    s0 = mx.equilibrium(rho, T, Y)
    p = s0.p
    lo, hi = max(Y - dY, 0.0), min(Y + dY, 1.0)
    out = []
    for y in (lo, hi):
        r, s = _rho_at_p(p, T, y, rho)
        out.append(s.e + p / r)
    return (out[1] - out[0]) / (hi - lo)


# ---------------------------------------------------------------- v2 (registry diff_cuair_model_v2)
EPS0 = P.get("eps0")
E_CH = P.get("e")


def _pair_D_v2(spi: str, zi: int, spj: str, zj: int, n: float, T: float, lnL: float) -> float:
    """Binary diffusion coefficient of one Cu species (stage zi) against one air species (stage zj) [m^2/s]."""
    d, alpha = P.get("hs_diameter"), P.get("polarizability")
    mi, mj = _mass(spi), _mass(spj)
    mu = mi * mj / (mi + mj)
    d12 = 0.5 * (d[spi] + d[spj])  # math
    D_hs = 3.0 / (8.0 * n * d12 ** 2) * np.sqrt(K_B * T / (2.0 * np.pi * mu))  # reg:diff_cuair_model
    if zi == 0 and zj == 0:
        return D_hs
    if zi > 0 and zj > 0:  # Coulomb
        return (3.0 * (4.0 * np.pi * EPS0) ** 2 * (K_B * T) ** 2.5  # reg:diff_cuair_model_v2
                / (4.0 * np.sqrt(2.0 * np.pi * mu) * n * zi ** 2 * zj ** 2 * E_CH ** 4 * lnL))  # reg:diff_cuair_model_v2
    # ion-neutral: polarisation of the neutral partner by the ion charge
    neutral, z = (spj, zi) if zj == 0 else (spi, zj)
    kL = 2.0 * np.pi * np.sqrt(alpha[neutral] * (z * E_CH) ** 2 / (4.0 * np.pi * EPS0 * mu))  # reg:diff_cuair_model_v2
    D_pol = K_B * T / (mu * n * 1.105 * kL)  # reg:diff_cuair_model_v2 (momentum-transfer factor)
    return min(D_hs, D_pol)


def D_binary_v2(state: mx.State) -> float:
    """Cu–air diffusion coefficient with Coulomb and polarisation collisions (Blanc-type mixture rule)."""
    from lipsim.transport.kappa import coulomb_log
    d = P.get("hs_diameter")
    cu, air = [], []
    for key, nk in state.n.items():
        sp = key.split("+")[0]
        if nk <= 0 or sp not in d:
            continue
        z = int(key.split("+")[1]) if "+" in key else 0
        (cu if sp == "Cu" else air if sp in AIR else []).append((sp, z, nk))
    n = sum(x[2] for x in cu) + sum(x[2] for x in air)
    if n <= 0 or not cu:
        return 0.0
    if not air:
        air = [("N2", 0, 1.0)]
    lnL = coulomb_log(state.ne, state.T)
    ncu, nair = sum(x[2] for x in cu), sum(x[2] for x in air)
    inv = 0.0
    for spi, zi, ni in cu:
        for spj, zj, nj in air:
            inv += (ni / ncu) * (nj / nair) / _pair_D_v2(spi, zi, spj, zj, n, state.T, lnL)
    return 1.0 / inv
