"""Where does the late in-band (filter) signal come from? Optically thin decomposition on the native PLUTO grid.

    python scripts/diag_emission.py <run_dir> --tables <table dir> --times 300 500 700 1000 --filter 510 12 --out <csv>

For each snapshot nearest to the requested times, the thin in-band power P = ∫ j dV (W/sr, full axisymmetric volume)
is split into:
  - line emission per species and ionisation stage (NIST lines in the band, LTE Boltzmann, same as image/forward.py)
  - Kramers–Unsöld continuum (same as image/forward.py)
  - temperature bins of the emitting cells (K) and Cu mass-fraction classes (Y<0.1, 0.1–0.5, >0.5)
Also gives the emission-weighted mean T, n_e, Y. Diagnostic only: it changes no model and is not a metric.
"""
from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
import pandas as pd

from lipsim import params as P
from lipsim.image import forward as F
from lipsim.image.run import snapshot_fields
from lipsim.io import pluto
from lipsim.optics.props import _K

T_BINS = [0, 3e3, 5e3, 7e3, 1e4, 1.5e4, 2e4, 3e4, 1e9]   # num (diagnostic bins, K)
Y_BINS = [0.0, 0.1, 0.5, 1.01]                            # num (diagnostic bins)


def decompose(rho, T, Y, r_edges, z_edges, tab, filt):
    lmin, lmax = filt[0] - filt[1], filt[0] + filt[1]
    bl = F.band_lines(float(lmin), float(lmax))
    dV = np.pi * np.diff(r_edges ** 2)[None, :] * np.diff(z_edges)[:, None]   # math (ring volumes)
    kT = F.K_B * T
    out = {}
    jtot = np.zeros_like(T)
    nQ = {}
    for i in range(len(bl.wl)):
        key = (bl.el[i], int(bl.z[i]))
        if key not in nQ:
            nQ[key] = tab(f"nQ_{key[0]}{key[1]}", rho, T, Y)
        j = F.H * F.C / bl.wl[i] * bl.A[i] * nQ[key] * bl.gu[i] * np.exp(-bl.Eu[i] * P.get("e") / kT) / (4 * np.pi)  # math
        out[f"{key[0]} {'I' * (key[1] + 1)}"] = out.get(f"{key[0]} {'I' * (key[1] + 1)}", 0.0) + float((j * dV).sum())
        jtot += j
    ne = tab("ne", rho, T, Y)
    lam_c = filt[0] * 1e-9  # math
    nu = F.C / lam_c
    kc = sum(_K(nu, T, k) * ne * tab(f"nion_z{k}", rho, T, Y) for k in (1, 2, 3)) * np.expm1(F.H * nu / kT)
    jc = kc * F.planck_lambda(lam_c, T) * 2 * filt[1] * 1e-9  # math (band integral)
    out["continuum"] = float((jc * dV).sum())
    jtot = jtot + jc
    w = jtot * dV
    W = w.sum()
    out["P_total_W_sr"] = float(W)
    for a, b in zip(T_BINS[:-1], T_BINS[1:]):
        out[f"T {a:.0f}-{b:.0f}"] = float(w[(T >= a) & (T < b)].sum() / W)
    for a, b in zip(Y_BINS[:-1], Y_BINS[1:]):
        out[f"Y {a:.1f}-{min(b, 1):.1f}"] = float(w[(Y >= a) & (Y < b)].sum() / W)
    out["T_emw_K"] = float((w * T).sum() / W)
    out["ne_emw_m3"] = float((w * ne).sum() / W)
    out["Y_emw"] = float((w * Y).sum() / W)
    return out


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("run_dir", type=Path)
    ap.add_argument("--tables", type=Path, required=True)
    ap.add_argument("--times", type=float, nargs="+", default=[300, 500, 700, 1000])
    ap.add_argument("--filter", type=float, nargs=2, default=[510, 12])
    ap.add_argument("--out", type=Path, required=True)
    a = ap.parse_args(argv)
    grid = pluto.read_grid(a.run_dir / "grid.out")
    idx = pluto.read_index(a.run_dir / "dbl.out")
    tab = F.Tables(a.tables)
    r_edges, z_edges = grid.x1_edges * 1e-6, grid.x2_edges * 1e-6  # math (um -> m)
    rows = []
    for t in a.times:
        n, tn = min(((e[0], e[1]) for e in idx), key=lambda e: abs(e[1] - t))
        _, rho, T, Y = snapshot_fields(a.run_dir, n, grid)
        d = decompose(rho, np.maximum(T, 1.0), Y, r_edges, z_edges, tab, tuple(a.filter))
        lines = {k: v for k, v in d.items() if k.split()[0] in ("Cu", "N", "O", "Ar") or k == "continuum"}
        tot = d["P_total_W_sr"]
        rows.append({"t_req_ns": t, "t_snap_ns": tn, **{f"frac {k}": v / tot for k, v in lines.items()},
                     **{k: v for k, v in d.items() if k not in lines}})
        print(f"t={tn:.0f} ns  P={tot:.3e} W/sr  T_emw={d['T_emw_K']:.0f} K  Y_emw={d['Y_emw']:.2f}  "
              + "  ".join(f"{k}:{v / tot:.2f}" for k, v in sorted(lines.items(), key=lambda kv: -kv[1])[:5]))
    a.out.parent.mkdir(parents=True, exist_ok=True)
    pd.DataFrame(rows).to_csv(a.out, index=False)


if __name__ == "__main__":
    main()
