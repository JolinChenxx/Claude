"""Synthetic frames and metrics for one PLUTO run.

    python -m lipsim.image <run_dir> --series high --scenarios configs/imaging/v0.yaml --tables <dir> --out <dir>

For every experimental frame time t of the series: gate-average the band radiance over the PLUTO snapshots
inside [t, t+10 ns]; for every filter scenario and both transfer options (thin / self-absorbed) map onto the
experimental pixel grid. Writes frames_<filter>_<thin|sa>.npz (radiance, W/m²/sr per pixel) and
metrics_<filter>_<thin|sa>_<defocus>.csv computed with exactly the same function as the experiment
(lipsim.pipeline.metrics.frame_metrics). Gain: one scalar per series and scenario, so that the model's
total signal at t_ref (300 ns) equals the experiment's (PLAN §1.3) — it affects only secondary metrics.
"""
from __future__ import annotations

import argparse
import json
from dataclasses import asdict
from pathlib import Path

import numpy as np
import pandas as pd
import yaml

from lipsim import params as P
from lipsim.image import camera, forward
from lipsim.io import pluto
from lipsim.pipeline.metrics import decay_time, frame_metrics, time_to_fraction

REPO = Path(__file__).resolve().parents[3]


def rebin_r(r_edges, field, dr):
    """Volume-weighted average of field[z, r] onto uniform shells of width dr (m)."""
    new = np.arange(0.0, r_edges[-1] + 0.5 * dr, dr)
    new[-1] = min(new[-1], r_edges[-1])
    out = np.zeros((field.shape[0], len(new) - 1))
    w = np.zeros(len(new) - 1)
    for i in range(len(r_edges) - 1):
        a, b = r_edges[i], r_edges[i + 1]
        for k in range(len(new) - 1):
            lo, hi = max(a, new[k]), min(b, new[k + 1])
            if hi > lo:
                vol = hi ** 2 - lo ** 2
                out[:, k] += field[:, i] * vol
                w[k] += vol
    return new, out / np.where(w > 0, w, 1.0)


def snapshot_fields(run_dir, n, grid):
    s = pluto.read_snapshot(run_dir, n, grid)
    v = s.vars
    return s.t * 1e-9, v["rho"], v["T"], np.clip(v["tr1"], 0.0, 1.0)  # math (ns -> s; code density = kg/m^3)


def rebin_z(z_edges, field, row_edges):
    """Average field[z, :] (cell-length weighted) into pixel rows given by row_edges (ascending, m)."""
    out = np.zeros((len(row_edges) - 1, field.shape[1]))
    w = np.zeros(len(row_edges) - 1)
    for j in range(len(z_edges) - 1):
        a, b = z_edges[j], z_edges[j + 1]
        k0 = max(int(np.searchsorted(row_edges, a, "right")) - 1, 0)
        for k in range(k0, len(row_edges) - 1):
            lo, hi = max(a, row_edges[k]), min(b, row_edges[k + 1])
            if hi <= lo:
                if row_edges[k] >= b:
                    break
                continue
            out[k] += field[j] * (hi - lo)
            w[k] += hi - lo
    return out / np.where(w > 0, w, 1.0)[:, None], w > 0


_W = {}


def _init_worker(tables, run_dir):
    _W["tab"] = forward.Tables(tables)
    _W["run"] = Path(run_dir)
    _W["grid"] = pluto.read_grid(Path(run_dir) / "grid.out")


def _snapshot_images(args):
    """All scenario radiance images (pixel grid) for one PLUTO snapshot."""
    n, filters, sa_opts, num, um, hw_px = args
    grid = _W["grid"]
    h, w = hw_px
    _, rho, T, Y = snapshot_fields(_W["run"], n, grid)
    px = um * 1e-6  # math
    row_edges = np.arange(h + 1) * px                     # pixel rows above the target (bottom row = 0..px)
    z_edges = grid.x2_edges * 1e-6  # math
    if num.get("transfer_grid", "rebinned") == "native":
        return n, _native_images(grid, rho, T, Y, filters, sa_opts, num, px, row_edges, w)
    (rho, cov), (T, _), (Y, _) = (rebin_z(z_edges, f, row_edges) for f in (rho, T, Y))
    # pixel rows above the computational domain: cold ambient air (no emission), radiance zeroed below
    rho[~cov], T[~cov], Y[~cov] = 1.0, P.get("T_ambient"), 0.0
    rmax = num["r_max_um"] * 1e-6  # math
    r_edges = grid.x1_edges * 1e-6  # math
    rb, rho_b = rebin_r(r_edges, rho, num["r_bin_um"] * 1e-6)  # math
    _, T_b = rebin_r(r_edges, T, num["r_bin_um"] * 1e-6)  # math
    _, Y_b = rebin_r(r_edges, Y, num["r_bin_um"] * 1e-6)  # math
    keep = rb[1:] <= rmax + 1e-12  # num
    rb = rb[:int(keep.sum()) + 1]
    rho_b, T_b, Y_b = rho_b[:, keep], T_b[:, keep], Y_b[:, keep]
    T_b = np.maximum(T_b, 1.0)
    x = (np.arange(w) - (w - 1) / 2) * px
    zc = 0.5 * (row_edges[1:] + row_edges[:-1])
    out = {}
    for fname, (c, hwid) in filters.items():
        for sa in sa_opts:
            img = forward.band_image(rb, zc, rho_b, T_b, Y_b, _W["tab"], (c, hwid), x, self_absorption=sa)
            img[~cov] = 0.0
            out[(fname, sa)] = img[::-1]                  # image row 0 = top of frame
    return n, out


def _native_images(grid, rho, T, Y, filters, sa_opts, num, px, row_edges, w):
    """Transfer on the native PLUTO cells (no averaging of ρ, T, Y before the non-linear emission/absorption).

    Rays are at the pixel-centre impact parameters x. Each native z row is transferred on its own, and the
    emergent intensities are averaged over the pixel rows with overlap weights, which is what a detector pixel
    integrates. Rows above the domain are dark (zero), as in the rebinned path."""
    z_edges = grid.x2_edges * 1e-6  # math (um -> m)
    r_edges = grid.x1_edges * 1e-6  # math
    rmax = num["r_max_um"] * 1e-6  # math
    nr = int(np.searchsorted(r_edges, rmax + 1e-12, "right")) - 1  # num
    nr = max(min(nr, len(r_edges) - 1), 1)
    re = r_edges[:nr + 1]
    zc = 0.5 * (z_edges[1:] + z_edges[:-1])
    x = (np.arange(w) - (w - 1) / 2) * px
    Tn = np.maximum(T[:, :nr], 1.0)
    out = {}
    for fname, (c, hwid) in filters.items():
        for sa in sa_opts:
            img_n = forward.band_image(re, zc, rho[:, :nr], Tn, np.clip(Y[:, :nr], 0.0, 1.0), _W["tab"], (c, hwid), x,
                                       self_absorption=sa, stark=bool(num.get("stark", False)),
                                       components=num.get("components", "all"))
            hide = num.get("rim_hide_m")  # v2r (PREREG 2026-11-28 'rim occlusion'): crater + melt-crown line-of-sight shield
            if hide is not None and hide[0] > 0:
                img_n[np.ix_(zc < hide[0], np.abs(x) < hide[1])] = 0.0
            img, cov = rebin_z(z_edges, img_n, row_edges)
            img[~cov] = 0.0
            out[(fname, sa)] = img[::-1]
    return out


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("run_dir", type=Path)
    ap.add_argument("--series", choices=["high", "low"], required=True)
    ap.add_argument("--scenarios", type=Path, default=REPO / "configs/imaging/v0.yaml")
    ap.add_argument("--tables", type=Path, required=True)
    ap.add_argument("--out", type=Path, required=True)
    ap.add_argument("--workers", type=int, default=0)
    ap.add_argument("--t-offset-ns", type=float, default=0.0,
                    help="model time = experimental frame label + offset (registry t0_offset_shock; PREREG t0 alignment)")
    a = ap.parse_args(argv)
    sc = P.resolve(yaml.safe_load(a.scenarios.read_text()))
    if a.workers:
        sc["numerics"]["workers"] = a.workers
    num = sc["numerics"]
    ser = sc["series"][a.series]
    um = ser["um_per_px"]
    meta = yaml.safe_load((REPO / ser["frames"]).read_text())["sets"][a.series]
    times = sorted({f["t_ns"] for f in meta["frames"]})
    h, w = meta["frame_px"]
    exp = pd.read_csv(REPO / f"data/exp/metrics_{a.series}.csv").groupby("t_ns", as_index=False).mean(numeric_only=True)
    L_ref_exp = float(np.interp(sc["t_ref"] * 1e9, exp.t_ns, exp.L))  # math (s -> ns)

    idx = pluto.read_index(a.run_dir / "dbl.out")
    snap_t = np.array([t for _, t, _, _ in idx]) * 1e-9  # math
    gate = P.get("gate_width")
    off = a.t_offset_ns * 1e-9  # math (ns -> s)
    need = sorted({k for t in times for k, ts in enumerate(snap_t) if t * 1e-9 + off - 1e-12 <= ts <= t * 1e-9 + off + gate + 1e-12})  # math/num
    a.out.mkdir(parents=True, exist_ok=True)
    rim = num.get("rim_occlusion")
    if rim:
        # side-view shield height = crater depth (model melt depth, source_meta) + measured spatter-top height
        # (paper Fig. 4e1, labels on the plume-image clock: model time - offset); shield half-width = melt radius
        meta = json.loads((a.run_dir / "source" / "source_meta.json").read_text())
        sp = pd.read_csv(REPO / rim["spatter_csv"], comment="#")
        d_m = float(meta[rim.get("crater_depth_key", "melt_depth_mean_m")])
        r_m = float(rim["half_width_um"]) * 1e-6 if rim.get("half_width_um") else float(meta["melt_radius_m"])  # math
        def _hide(ts):
            return (d_m + 1e-6 * float(np.interp(ts * 1e9 - a.t_offset_ns, sp.t_ns, sp.h_um, left=0.0)), r_m)  # math
    jobs = [(idx[k][0], sc["filters"], tuple(num["self_absorption"]), dict(num, rim_hide_m=_hide(snap_t[k])) if rim else num, um, (h, w))
            for k in need]
    workers = int(num.get("workers", 1))
    imgs = {}
    if workers > 1:
        import multiprocessing as mp
        from concurrent.futures import ProcessPoolExecutor
        # 'spawn' on every platform (Windows has no fork): workers import lipsim.image.run by name
        with ProcessPoolExecutor(workers, mp_context=mp.get_context("spawn"), initializer=_init_worker,
                                 initargs=(str(a.tables), str(a.run_dir))) as ex:
            for n, out in ex.map(_snapshot_images, jobs):
                imgs[n] = out
    else:
        _init_worker(str(a.tables), str(a.run_dir))
        for j in jobs:
            n, out = _snapshot_images(j)
            imgs[n] = out
    results = {}
    for fname in sc["filters"]:
        for sa in num["self_absorption"]:
            key = f"{fname}_{'sa' if sa else 'thin'}"
            frames = []
            for t in times:
                sel = [k for k in need if t * 1e-9 + off - 1e-12 <= snap_t[k] <= t * 1e-9 + off + gate + 1e-12]  # math/num
                if len(sel) >= 2:  # num
                    stack = np.array([imgs[idx[k][0]][(fname, sa)] for k in sel])
                    ts = snap_t[sel]
                    frames.append(np.trapezoid(stack, ts, axis=0) / (ts[-1] - ts[0]))
                else:
                    frames.append(np.full((h, w), np.nan))
            frames = np.array(frames)
            np.savez_compressed(a.out / f"frames_{key}.npz", t_ns=np.array(times), radiance=frames, um_per_px=um)
            for dname, dfoc in sc["defocus_um"].items():
                blurred = np.array([camera.blur(f, um, dfoc) if np.isfinite(f).all() else f for f in frames])
                tarr = np.array(times, float)
                ok = np.array([np.isfinite(f).all() for f in blurred])
                iref = int(np.argmin(np.where(ok, np.abs(tarr - sc["t_ref"] * 1e9), np.inf)))  # math
                G = camera.gain_for_reference(blurred[iref], L_ref_exp) if ok.any() else 0.0
                rows = []
                for t, f, good in zip(times, blurred, ok):
                    if not good:
                        continue
                    m = asdict(frame_metrics(camera.to_8bit(f, G), t, um))
                    m["L_radiance"] = float(f.sum())
                    rows.append(m)
                df = pd.DataFrame(rows)
                df.to_csv(a.out / f"metrics_{key}_{dname}.csv", index=False, float_format="%.6g")
                tau, _ = decay_time(df.t_ns, df.L_radiance) if len(df) else (np.nan, np.nan)
                d10 = time_to_fraction(df.t_ns, df.L_radiance) if len(df) else np.nan
                results[f"{key}_{dname}"] = {"gain": G, "t_ref_used_ns": float(tarr[iref]), "P1_tau_ns": tau, "P2_dt10_ns": d10}
    results["t_offset_ns"] = a.t_offset_ns
    (a.out / "image_summary.json").write_text(json.dumps(results, indent=1, default=float))
    print(json.dumps(results, indent=1, default=float))


