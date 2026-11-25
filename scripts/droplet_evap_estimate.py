"""A-priori estimate (no PLUTO): how fast can a Cu droplet of radius a evaporate in the plume core?

Heat flux to a droplet surface at T_boil from LTE gas (rho, T, Y):
  free-molecular  q_FM = (1/4) n_h v_h (eps(T) - eps(T_b)),  eps = e + p/(2 rho)  (effusive energy flux per mass,
                  2kT translational + ionisation/excitation; heavy-particle flux, electrons ambipolar), accommodation 1
  continuum       q_C  = (1/a) * integral_{T_b}^{T} kappa dT   (Nu = 2, Kirchhoff transform)
  combined        1/q  = 1/q_FM + 1/q_C   (resistances in series, the usual transition-regime interpolation)
Evaporation time at constant gas state (droplet held at T_boil): tau = rho_l a L_v / q in the free-molecular limit,
rho_l a^2 L_v / (3 a q_C a) ... integrated numerically below for the combined law.

Gas states: mass-weighted plume-core T and mean core density rho = M_Cu / V from the diagnostics of existing runs
(T023 = no droplets: the gas the droplets would meet; T026 = after v1n droplet loading). Y = 1 (Cu core).

    python scripts/droplet_evap_estimate.py
"""
from __future__ import annotations

import sys
from pathlib import Path

import numpy as np
import pandas as pd

REPO = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(REPO / "src"))
from lipsim import params as P  # noqa: E402
from lipsim.eos import mixture as mx  # noqa: E402
from lipsim.transport.kappa import kappa  # noqa: E402

K_B = 1.380649e-23
M_CU = P.get("A_Cu") * 1.66053906660e-27
RHO_L = P.get("cu_rho_liquid")
LV = P.get("cu_L_vap")
TB = P.get("cu_T_boil")
SIZES = [10e-9, 30e-9, 100e-9, 300e-9, 1e-6, 3e-6]  # m, scan only


def eps(rho, T):
    s = mx.equilibrium(rho, T, 1.0)
    return s.e + s.p / (2 * rho), s


def fluxes(rho, T, a):
    e_g, s = eps(rho, T)
    e_b, _ = eps(rho, TB)
    vh = np.sqrt(8 * K_B * T / (np.pi * M_CU))
    qfm = 0.25 * rho * vh * max(e_g - e_b, 0.0)
    Tg = np.linspace(TB, T, 12)
    kk = [kappa(mx.equilibrium(rho, t, 1.0))["total"] for t in Tg]
    Kint = np.trapezoid(kk, Tg)
    return qfm, Kint


def tau_evap(qfm, Kint, a0):
    """Time to evaporate a droplet of radius a0 at fixed gas state with 1/q = 1/q_FM + a/Kint."""
    # da/dt = -q(a)/(rho_l Lv); q(a) = 1/(1/qfm + a/Kint)  ->  dt = rho_l Lv (1/qfm + a/Kint) da
    return RHO_L * LV * (a0 / qfm + a0 ** 2 / (2 * Kint))


def main():
    rows = []
    for run in ["T023/high_thin_G0/it2", "T026/high_thin_G0/it2", "T023/low_thin_G0/it1", "T026/low_thin_G0/it1"]:
        d = pd.read_csv(REPO / f"results/{run}/diagnostics/core_energy.csv").dropna()
        for t in (100, 150, 200, 300, 500):
            r = d.iloc[(d.t_ns - t).abs().argmin()]
            rho, T = float(r.M_Cu / r.V), float(r.T_mw)
            if T <= TB + 50:
                continue
            qfm, Kint = fluxes(rho, T, 1e-6)
            row = {"run": run, "t_ns": t, "rho": rho, "T": T, "q_FM_W_m2": qfm, "Kint_W_m": Kint}
            for a in SIZES:
                row[f"tau_{a*1e9:.0f}nm_us"] = tau_evap(qfm, Kint, a) * 1e6
            rows.append(row)
    df = pd.DataFrame(rows)
    pd.set_option("display.width", 250)
    print(df.to_string(index=False, float_format=lambda x: f"{x:.3g}"))
    out = REPO / "results/droplets/evap_estimate.csv"
    out.parent.mkdir(parents=True, exist_ok=True)
    df.to_csv(out, index=False, float_format="%.4g")


if __name__ == "__main__":
    main()
