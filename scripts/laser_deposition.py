"""Where and in what does the laser deposit its energy, and how tall is the hot region? (diagnostic, high-fluence focus)

    python scripts/laser_deposition.py <run_dir> --out <csv> [--tmax 200]

Per snapshot up to tmax (ns), from the uservars written by the solver (qlas: absorbed laser power density, W/m^3)
and the primitive fields:
  P_las        total absorbed laser power in the gas [W]
  f_Cu         fraction of P_las absorbed where Y > 0.5 (Cu vapour), the rest is air or mixture
  z_abs_um     power-weighted mean height of absorption above the face
  z90_abs_um   height below which 90% of the absorbed power is deposited
  zT10_um      top of the region with T > 10 kK (highest cell on any column)
  zT10_axis_um the same on the axis column
  zCu_um       top of the Cu core (Y > 0.5)
  r_T10_um     radial extent of T > 10 kK
Diagnostic only; nothing here enters a run.
"""
from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
import pandas as pd

from lipsim.image.run import snapshot_fields
from lipsim.io import pluto


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("run_dir", type=Path)
    ap.add_argument("--out", type=Path, required=True)
    ap.add_argument("--tmax", type=float, default=200.0)  # ns
    a = ap.parse_args(argv)
    g = pluto.read_grid(a.run_dir / "grid.out")
    r, z = g.x1_edges, g.x2_edges                       # um
    rc, zc = 0.5 * (r[1:] + r[:-1]), 0.5 * (z[1:] + z[:-1])
    dV = np.pi * np.diff((r * 1e-6) ** 2)[None, :] * np.diff(z * 1e-6)[:, None]  # math (m^3)
    rows = []
    for n, t, _, names in pluto.read_index(a.run_dir / "dbl.out"):
        if t > a.tmax:
            break
        s = pluto.read_snapshot(a.run_dir, n, g)
        _, rho, T, Y = snapshot_fields(a.run_dir, n, g)
        q = s.vars.get("qlas")
        row = {"t_ns": t}
        if q is not None:
            P = q * dV
            Ptot = float(P.sum())
            row["P_las"] = Ptot
            if Ptot > 0:
                row["f_Cu"] = float(P[Y > 0.5].sum() / Ptot)
                pz = P.sum(axis=1)
                row["z_abs_um"] = float((pz * zc).sum() / Ptot)
                cz = np.cumsum(pz) / Ptot
                row["z90_abs_um"] = float(zc[np.searchsorted(cz, 0.9)])
        hot = T > 1.0e4
        row["zT10_um"] = float(zc[np.where(hot.any(axis=1))[0].max()]) if hot.any() else 0.0
        row["zT10_axis_um"] = float(zc[np.where(hot[:, 0])[0].max()]) if hot[:, 0].any() else 0.0
        row["r_T10_um"] = float(rc[np.where(hot.any(axis=0))[0].max()]) if hot.any() else 0.0
        cu = Y > 0.5
        row["zCu_um"] = float(zc[np.where(cu.any(axis=1))[0].max()]) if cu.any() else 0.0
        rows.append(row)
    d = pd.DataFrame(rows)
    a.out.parent.mkdir(parents=True, exist_ok=True)
    d.to_csv(a.out, index=False, float_format="%.5g")
    with pd.option_context("display.width", 200):
        print(d.iloc[::max(1, len(d) // 15)].to_string(index=False))


if __name__ == "__main__":
    main()
