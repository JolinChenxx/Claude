"""Energy budget of the Cu-rich core (Y > Ycut) vs time: where does its energy go, or come from?

    python scripts/core_energy.py <run_dir> --tables <table dir> --nec thin|trap --out <csv> [--ycut 0.5]

Per snapshot, over the cells with Y > ycut:
  M_Cu      Cu mass [kg]
  E_int     internal energy Σ ρe dV [J]          (table e(ρ,T,Y))
  E_kin     kinetic energy [J]
  T_mw      Cu-mass-weighted temperature [K]
  P_rad     radiated power Σ 4π NEC dV [W]       (the same NEC table field the run used)
  V         volume [m^3]
Derived: dE_int/dt from finite differences, and "other" = dE_int/dt + P_rad. "other" is the net power into the
core from everything except radiation: conduction, interdiffusion, compression work, and mass exchange across the
Y = ycut surface. Diagnostic only.
"""
from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
import pandas as pd

from lipsim.image.run import snapshot_fields
from lipsim.io import pluto
from lipsim.tables.build import read_bin
from scipy.interpolate import RegularGridInterpolator


def interp_table(path, field):
    ax, f = read_bin(path)
    axes = (ax["log10_rho"], ax["log10_T"], ax["Y_cu"])
    v = f[field]
    lg = np.all(v > 0)
    it = RegularGridInterpolator(axes, np.log10(v) if lg else v, bounds_error=False, fill_value=None)
    lo, hi = [a[0] for a in axes], [a[-1] for a in axes]

    def ev(rho, T, Y):
        pts = np.clip(np.stack([np.log10(rho), np.log10(T), Y], -1), lo, hi)
        r = it(pts)
        return 10.0 ** r if lg else r
    return ev


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("run_dir", type=Path)
    ap.add_argument("--tables", type=Path, required=True)
    ap.add_argument("--nec", choices=["thin", "trap"], required=True)
    ap.add_argument("--ycut", type=float, default=0.5)  # num (core definition)
    ap.add_argument("--every", type=int, default=4)     # num (snapshot stride)
    ap.add_argument("--out", type=Path, required=True)
    a = ap.parse_args(argv)
    g = pluto.read_grid(a.run_dir / "grid.out")
    r, z = g.x1_edges * 1e-6, g.x2_edges * 1e-6  # math (um -> m)
    dV = np.pi * np.diff(r ** 2)[None, :] * np.diff(z)[:, None]  # math
    e_of = interp_table(a.tables / "eos.bin", "e")
    nec_of = interp_table(a.tables / "opt.bin", f"nec_{a.nec}")
    rows = []
    for k, (n, t, *_) in enumerate(pluto.read_index(a.run_dir / "dbl.out")):
        if k % a.every:
            continue
        s = pluto.read_snapshot(a.run_dir, n, g)
        _, rho, T, Y = snapshot_fields(a.run_dir, n, g)
        T = np.maximum(T, 1.0)
        c = Y > a.ycut
        if not c.any():
            rows.append({"t_ns": t, "M_Cu": 0.0})
            continue
        v2 = s.vars["vx1"] ** 2 + s.vars["vx2"] ** 2  # (km/s)^2 in code units
        e = e_of(rho[c], T[c], Y[c])
        mcu = (rho * Y * dV)[c]
        rows.append({"t_ns": t, "M_Cu": mcu.sum(), "E_int": (rho[c] * e * dV[c]).sum(),
                     "E_kin": (0.5 * rho[c] * v2[c] * 1e6 * dV[c]).sum(),  # math (km/s -> m/s)
                     "T_mw": (mcu * T[c]).sum() / mcu.sum(), "P_rad": (4 * np.pi * nec_of(rho[c], T[c], Y[c]) * dV[c]).sum(),  # math
                     "V": dV[c].sum()})
    d = pd.DataFrame(rows)
    if "E_int" in d:
        d["dEdt"] = np.gradient(d.E_int, d.t_ns * 1e-9)  # math (ns -> s)
        d["P_other"] = d.dEdt + d.P_rad
    a.out.parent.mkdir(parents=True, exist_ok=True)
    d.to_csv(a.out, index=False)
    with pd.option_context("display.width", 200):
        print(d.iloc[::max(1, len(d) // 12)].to_string(index=False, float_format=lambda x: f"{x:.3e}"))


if __name__ == "__main__":
    main()
