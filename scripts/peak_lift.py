"""Emission-peak lift above the target face: experiment bracket vs model (PREREG 2026-11-24 v1k, metric S1).

Experiment (results/surface/surface_rows.csv, scripts/exp_surface_rows.py): lift_min = (lower50 - peak) px,
lift_max = (lower10 - peak) px, times um_per_px. Model: the band-b image (thin / sa) blurred with the baseline defocus
exactly as for the comparison figures; the target face is the bottom edge of the last row, lift = (n - 0.5 - peak) px.
    python scripts/peak_lift.py <image_dir> --series high|low [--out csv]"""
import argparse
import sys
from pathlib import Path

import numpy as np
import pandas as pd

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
from lipsim import params as P  # noqa: E402
from lipsim.image import camera  # noqa: E402


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("image_dir", type=Path)
    ap.add_argument("--series", required=True)
    ap.add_argument("--out", type=Path, default=None)
    a = ap.parse_args(argv)
    ex = pd.read_csv(ROOT / "results/surface/surface_rows.csv")
    ex = ex[ex.series == a.series].groupby("t_ns", as_index=False).mean(numeric_only=True)
    um_e = {"high": 10.724, "low": 9.244}[a.series]  # num (data/exp/meta.yaml um_per_px)
    ex["exp_lift_min_um"] = (ex.lower50_row - ex.peak_row) * um_e
    ex["exp_lift_max_um"] = (ex.lower10_row - ex.peak_row) * um_e
    dfoc = P.get("defocus_um_scenarios")["baseline"]
    out = ex[["t_ns", "exp_lift_min_um", "exp_lift_max_um"]].copy()
    for sa in ("thin", "sa"):
        d = np.load(a.image_dir / f"frames_b_{sa}.npz")
        um = float(d["um_per_px"])
        lift = {}
        for t, x in zip(d["t_ns"], d["radiance"]):
            if not np.isfinite(x).all() or x.max() <= 0:
                continue
            b = camera.blur(x, um, dfoc)
            p = b.sum(1)
            lift[int(t)] = (len(p) - 0.5 - int(p.argmax())) * um  # math (face = bottom edge of the last row)
        out[f"model_lift_{sa}_um"] = [lift.get(int(t), np.nan) for t in out.t_ns]
    sel = (out.t_ns >= 150) & (out.t_ns <= 630)  # num (PREREG window)
    for sa in ("thin", "sa"):
        ok = out.loc[sel, f"model_lift_{sa}_um"] >= out.loc[sel, "exp_lift_min_um"]
        print(f"{sa}: model lift 150-630 ns median {out.loc[sel, f'model_lift_{sa}_um'].median():.0f} um; "
              f"exp min-lift median {out.loc[sel, 'exp_lift_min_um'].median():.0f} um; frames >= exp min: {ok.sum()}/{sel.sum()}")
    if a.out:
        a.out.parent.mkdir(parents=True, exist_ok=True)
        out.to_csv(a.out, index=False, float_format="%.1f")
    return out


if __name__ == "__main__":
    main()
