"""Shock-front position vs time from PLUTO outputs (metric P4; independent of the emission images).

    python scripts/shock_track.py <run_dir> --out <csv> [--thr 1.2]

For every dbl snapshot, the shock front is taken as the outermost position where the pressure exceeds
thr × p_ambient. This is done along the axis (r = 0, measured from the target, z direction) and along the
target surface (z = first cell, r direction). The ambient pressure is the median of the outermost 5 cells,
assumed undisturbed. Output columns: t_ns, R_axis_um, R_surf_um. Compare R_axis with data/exp/curves/fig3d_shock.csv.
"""
from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
import pandas as pd

from lipsim.io import pluto


def front(x_c, p, p_amb, thr):
    idx = np.where(p > thr * p_amb)[0]
    return float(x_c[idx.max()]) if idx.size else np.nan


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("run_dir", type=Path)
    ap.add_argument("--out", type=Path, required=True)
    ap.add_argument("--thr", type=float, default=1.2)  # num (front criterion, relative to ambient)
    a = ap.parse_args(argv)
    g = pluto.read_grid(a.run_dir / "grid.out")
    r_c = 0.5 * (g.x1_edges[1:] + g.x1_edges[:-1])
    z_c = 0.5 * (g.x2_edges[1:] + g.x2_edges[:-1])
    rows = []
    for n, t, *_ in pluto.read_index(a.run_dir / "dbl.out"):
        s = pluto.read_snapshot(a.run_dir, n, g)
        p = s.vars["prs"]                                  # [z, r]
        p_amb = float(np.median(p[-5:, -5:]))
        rows.append({"t_ns": t, "R_axis_um": front(z_c, p[:, 0], p_amb, a.thr),
                     "R_surf_um": front(r_c, p[0, :], p_amb, a.thr)})
    a.out.parent.mkdir(parents=True, exist_ok=True)
    pd.DataFrame(rows).to_csv(a.out, index=False)
    print(pd.DataFrame(rows).iloc[::20].to_string(index=False))


if __name__ == "__main__":
    main()
