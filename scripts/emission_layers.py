"""Where does the band-b emission come from, by height above the face? (diagnostic, high-fluence vertical extent; PREREG T043)

    python scripts/emission_layers.py <run_dir> --tables <dir> --out <csv> [--tmax 200] [--filter b] [--scenarios configs/imaging/v2.yaml]

Per snapshot up to tmax (ns), on the native PLUTO cells and with self-absorption (as the imaging):
  P_b                  band-b side-view power per unit height summed over rows [W/sr/m, relative]
  z10/z50/z90_um       emission-weighted height percentiles (unblurred)
  f_lt10, f_10_50, f_gt50   fraction of the band-b emission from z < 10 µm, 10–50 µm, > 50 µm
  f_lines_lt10         share of the lines (vs continuum) in the z < 10 µm layer (thin split, diagnostic)
  axis profiles at z = 2, 5, 10, 25, 50, 100, 150, 200 µm: T [K], rho [kg/m^3], Y_Cu, ne [m^-3]
  Tmax_axis_K, zTmax_axis_um, and T, ne, Y at the emission z50 on the axis
Diagnostic only; nothing here enters a run.
"""
from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
import pandas as pd
import yaml

from lipsim import params as P
from lipsim.image import forward
from lipsim.image.run import snapshot_fields
from lipsim.io import pluto

ZPROBE = (2, 5, 10, 25, 50, 100, 150, 200)  # um, diagnostic heights


def pct(z, w, q):
    c = np.cumsum(w)
    return float(np.interp(q * c[-1], c, z)) if c[-1] > 0 else np.nan


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("run_dir", type=Path)
    ap.add_argument("--tables", type=Path, required=True)
    ap.add_argument("--out", type=Path, required=True)
    ap.add_argument("--tmax", type=float, default=200.0)  # ns
    ap.add_argument("--filter", default="b")
    ap.add_argument("--scenarios", type=Path, default=Path("configs/imaging/v2.yaml"))
    ap.add_argument("--rmax-um", type=float, default=600.0)
    ap.add_argument("--nx", type=int, default=60)
    a = ap.parse_args(argv)
    sc = P.resolve(yaml.safe_load(a.scenarios.read_text()))
    c, hw = sc["filters"][a.filter]
    tab = forward.Tables(str(a.tables))
    g = pluto.read_grid(a.run_dir / "grid.out")
    re_all = g.x1_edges * 1e-6  # math (um -> m)
    nr = max(int(np.searchsorted(re_all, a.rmax_um * 1e-6, "right")) - 1, 1)  # math/num
    re = re_all[:nr + 1]
    ze = g.x2_edges  # um
    zc = 0.5 * (ze[1:] + ze[:-1])
    dz = np.diff(ze)
    x = np.linspace(-re[-1], re[-1], a.nx)  # impact parameters
    dx = x[1] - x[0]
    rows = []
    for n, t, _, _ in pluto.read_index(a.run_dir / "dbl.out"):
        if t > a.tmax:
            break
        _, rho, T, Y = snapshot_fields(a.run_dir, n, g)
        rho, T, Y = rho[:, :nr], np.maximum(T[:, :nr], 1.0), np.clip(Y[:, :nr], 0.0, 1.0)
        row = {"t_ns": t}
        img = forward.band_image(re, zc * 1e-6, rho, T, Y, tab, (c, hw), x, self_absorption=True)  # math
        w = img.sum(axis=1) * dx * dz  # power per row (relative), side view
        row["P_b"] = float(w.sum())
        if w.sum() > 0:
            for q, k in ((0.1, "z10_um"), (0.5, "z50_um"), (0.9, "z90_um")):
                row[k] = pct(zc, w, q)
            row["f_lt10"] = float(w[zc < 10].sum() / w.sum())
            row["f_10_50"] = float(w[(zc >= 10) & (zc < 50)].sum() / w.sum())
            row["f_gt50"] = float(w[zc >= 50].sum() / w.sum())
            lo = zc < 10
            li = forward.band_image(re, zc[lo] * 1e-6, rho[lo], T[lo], Y[lo], tab, (c, hw), x, self_absorption=False, components="lines")
            co = forward.band_image(re, zc[lo] * 1e-6, rho[lo], T[lo], Y[lo], tab, (c, hw), x, self_absorption=False, components="continuum")
            s = li.sum() + co.sum()
            row["f_lines_lt10"] = float(li.sum() / s) if s > 0 else np.nan
        ne = tab("ne", rho, T, Y)
        for zp in ZPROBE:
            j = int(np.clip(np.searchsorted(zc, zp), 0, len(zc) - 1))
            row[f"T_{zp}"], row[f"rho_{zp}"], row[f"Y_{zp}"], row[f"ne_{zp}"] = float(T[j, 0]), float(rho[j, 0]), float(Y[j, 0]), float(ne[j, 0])
        ja = int(np.argmax(T[:, 0]))
        row["Tmax_axis_K"], row["zTmax_axis_um"] = float(T[ja, 0]), float(zc[ja])
        if np.isfinite(row.get("z50_um", np.nan)):
            j = int(np.clip(np.searchsorted(zc, row["z50_um"]), 0, len(zc) - 1))
            row["T_z50"], row["ne_z50"], row["Y_z50"] = float(T[j, 0]), float(ne[j, 0]), float(Y[j, 0])
        rows.append(row)
    d = pd.DataFrame(rows)
    a.out.parent.mkdir(parents=True, exist_ok=True)
    d.to_csv(a.out, index=False, float_format="%.5g")
    cols = [k for k in ("t_ns", "z50_um", "z90_um", "f_lt10", "f_gt50", "f_lines_lt10", "T_5", "ne_5", "T_100", "Tmax_axis_K", "zTmax_axis_um") if k in d]
    with pd.option_context("display.width", 220):
        print(d[cols].iloc[::max(1, len(d) // 15)].to_string(index=False))


if __name__ == "__main__":
    main()
