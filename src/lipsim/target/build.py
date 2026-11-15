"""Target/evaporation source tables for PLUTO (W4).

    python -m lipsim.target.build --case configs/cases/<case>.yaml --out <dir> [--shielding <run_dir>]

Without --shielding: Picard iteration 0 (unshielded intensity; vapour-mass upper bound).
With --shielding <run_dir>: reads <run_dir>/surface_transmission.npz (r [m], t [s], trans[t, r]) produced by
the PLUTO post-processing, relaxes it with the transmission used by that run (<run_dir>/source/source_meta.json
→ previous target_source.npz), and recomputes. Outputs <out>/target_source.bin (LIPTAB01, axes t, r),
<out>/target_source.npz, <out>/source_meta.json (budget, iteration index, hashes).
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

import numpy as np
import yaml
from scipy.interpolate import RegularGridInterpolator

from lipsim import params as P
from lipsim.tables.build import write_bin
from lipsim.target import model as tm


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("--case", type=Path, required=True)
    ap.add_argument("--out", type=Path, required=True)
    ap.add_argument("--shielding", type=Path, default=None)
    a = ap.parse_args(argv)
    cfg = P.resolve(yaml.safe_load(a.case.read_text(encoding="utf-8")))
    num = cfg["numerics"]
    las = tm.Laser(cfg["laser"]["energy"], cfg["laser"]["diameter_1e2"], cfg["laser"]["fwhm"])
    r = np.linspace(0.0, num["r_max_over_w"] * las.w, num["rings"])
    it, shield, tau_grid = 0, None, None
    if a.shielding is not None:
        d = np.load(a.shielding / "surface_transmission.npz")
        prev_meta = json.loads((a.shielding / "source" / "source_meta.json").read_text())
        it = prev_meta["picard_iteration"] + 1
        new = RegularGridInterpolator((d["t"], d["r"]), d["trans"], bounds_error=False, fill_value=1.0)
        prev = np.load(a.shielding / "source" / "target_source.npz")
        tg, rg = prev["t"], prev["r"]
        T_, R_ = np.meshgrid(tg, rg, indexing="ij")
        tau_new = new(np.stack([T_, R_], -1))
        tau_grid = num["picard_relax"] * tau_new + (1 - num["picard_relax"]) * prev["trans_used"]
        interp = RegularGridInterpolator((tg, rg), tau_grid, bounds_error=False, fill_value=1.0)
        shield = lambda rr, tt: interp(np.stack([np.full_like(rr, tt), rr], -1))  # noqa: E731
    mat = tm.Material()
    o = tm.run(las, mat, r, t_end=num["t_end_ns"] * 1e-9, dt=num["dt_ns"] * 1e-9, shielding=shield)  # math (ns -> s)
    trans_used = np.ones_like(o["mdot"]) if shield is None else np.array([shield(r, t) for t in o["t"]])
    A = tm.ring_area(r)
    E = {k: float((v * A).sum()) for k, v in o["E_area"].items()}
    E["stored"] = float((o["stored_area"] * A).sum())
    a.out.mkdir(parents=True, exist_ok=True)
    fields = {k: o[k] for k in ("mdot", "Tv", "rhov", "uv", "Ts", "A", "I_inc", "q_abs", "q_overcap")}
    np.savez_compressed(a.out / "target_source.npz", t=o["t"], r=r, trans_used=trans_used, **fields)
    write_bin(a.out / "target_source.bin", {"t": o["t"], "r": r}, {k: fields[k] for k in ("mdot", "Tv", "rhov", "uv", "Ts")})
    meta = {"case": cfg["name"], "picard_iteration": it, "shielding_from": str(a.shielding) if a.shielding else None,
            "budget_J": E, "absorbed_fraction": E["abs"] / E["inc"], "overcap_fraction_of_absorbed": E["overcap"] / E["abs"],
            "U4_triggered": E["overcap"] / E["abs"] > 0.2,  # num: PLAN v3 §3.6 threshold
            "vaporised_mass_kg": E["mass"], "registry_sha256": P.registry_hash(),
            "bin_sha256": hashlib.sha256((a.out / "target_source.bin").read_bytes()).hexdigest()}
    (a.out / "source_meta.json").write_text(json.dumps(meta, indent=1))
    print(json.dumps({k: meta[k] for k in ("case", "picard_iteration", "absorbed_fraction", "vaporised_mass_kg", "U4_triggered")}))


if __name__ == "__main__":
    main()
