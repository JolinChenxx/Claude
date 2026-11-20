"""Batch runner for PLUTO+lip cases (PLAN v3 §11), incl. the Picard chain of the one-way target coupling.

    python -m lipsim.batch configs/batches/<name>.yaml --max-cores 60 [--dry-run]

Batch YAML:
  name: T011
  pluto_exe: <path to pluto(.exe)>            # serial or MPI build
  mpiexec: mpiexec                              # omitted -> serial
  tables: <dir with eos.bin, opt.bin>
  root: <output root>                           # run dirs: <root>/<run id>/
  numerics:
    runs:
      - {id: high_thin_G0, case: configs/cases/v0_high.yaml, grid: G0, nec: thin, tstop_ns: 1000,
         procs: 8, picard: 2, src_on: 1}
Each run: it0 target source (unshielded) -> PLUTO -> merge surftrans -> target.build --shielding -> it1 ...
Every launched PLUTO run (complete or not) is appended to results/run_registry.csv (PLAN §1.5).
"""
from __future__ import annotations

import argparse
import csv
import datetime as dt
import hashlib
import json
import shutil
import subprocess
import sys
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

import numpy as np
import yaml

from lipsim import params as P

REPO = Path(__file__).resolve().parents[2]
NEC = {"thin": 0, "trap": 1}


def _sha(p: Path) -> str:
    return hashlib.sha256(Path(p).read_bytes()).hexdigest()


def _commit() -> str:
    try:
        return subprocess.run(["git", "-C", str(REPO), "rev-parse", "--short", "HEAD"], capture_output=True,
                              text=True).stdout.strip()
    except Exception:  # noqa: BLE001
        return "unknown"


def grid_lines(level: str) -> list[str]:
    g = yaml.safe_load((REPO / "configs/grids/levels.yaml").read_text())["numerics"][level]
    out = []
    for name, segs in (("X1-grid", g["x1"]), ("X2-grid", g["x2"])):
        parts = [str(len(segs))]
        for k, (a, n, typ, b) in enumerate(segs):
            parts += ([f"{a}"] if k == 0 else []) + [f"{n}", typ, f"{b}"]
        out.append(f"{name}    " + "  ".join(parts))
    out.append("X3-grid    1  0.0  1  u  1.0")
    return out


def make_ini(case_cfg: dict, level: str, nec: str, tstop_ns: float, src_on: int, rmax_par: float | None = None) -> str:
    """pluto.ini with every physical number taken from the registry via the resolved case config."""
    num = yaml.safe_load((REPO / "configs/grids/levels.yaml").read_text())["numerics"]
    from lipsim.target.model import Laser
    las = Laser(case_cfg["laser"]["energy"], case_cfg["laser"]["diameter_1e2"], case_cfg["laser"]["fwhm"])
    params = [("E_PULSE", las.E), ("D_BEAM", las.D), ("FWHM", las.fwhm), ("T_PEAK", las.t_peak),
              ("NEC_LIMIT", NEC[nec]), ("P_AMB", P.get("p_ambient")), ("T_AMB", P.get("T_ambient")),
              ("SRC_ON", src_on), ("T_FLOOR", P.get("T_ambient"))]
    L = ["[Grid]", "", *grid_lines(level), "", "[Chombo Refinement]", "", "Levels 4", "Ref_ratio 2 2 2 2 2",
         "Regrid_interval 2 2 2 2", "Refine_thresh 0.3", "Tag_buffer_size 3", "Block_factor 8", "Max_grid_size 64",
         "Fill_ratio 0.75", "", "[Time]", "", f"CFL {num['cfl']}", "CFL_max_var 1.1", f"tstop {tstop_ns}",
         *([f"rmax_par {rmax_par}"] if rmax_par else []),
         "first_dt 1.e-5", "", "[Solver]", "", "Solver hllc", "", "[Boundary]", "", "X1-beg axisymmetric",
         "X1-end outflow", "X2-beg reflective", "X2-end outflow", "X3-beg outflow", "X3-end outflow", "",
         "[Static Grid Output]", "", "uservar 3 T qlas qrad", f"dbl {num['output_dt_ns']} -1 single_file",
         "flt -1.0 -1 single_file", "vtk -1.0 -1 single_file", "tab -1.0 -1", "ppm -1.0 -1", "png -1.0 -1",
         "log 1000", f"analysis {num['analysis_dt_ns']} -1", "", "[Particles]", "", "Nparticles 0 -1",
         "particles_dbl 1.0 -1", "particles_flt -1.0 -1", "particles_vtk -1.0 -1", "particles_tab -1.0 -1", "",
         "[Parameters]", ""]
    L += [f"{k:<12s} {v:.10g}" for k, v in params]
    return "\n".join(L) + "\n"


def merge_surftrans(run_dir: Path):
    rows = []
    for f in sorted(run_dir.glob("surftrans_*.txt")):
        a = np.loadtxt(f, ndmin=2)
        if a.size:
            rows.append(a)
    if not rows:
        return None
    a = np.concatenate(rows)
    t = np.unique(a[:, 0])
    r = np.unique(a[:, 1])
    trans = np.ones((len(t), len(r)))
    it = np.searchsorted(t, a[:, 0])
    ir = np.searchsorted(r, a[:, 1])
    trans[it, ir] = a[:, 2]
    np.savez_compressed(run_dir / "surface_transmission.npz", t=t, r=r, trans=trans)
    return run_dir / "surface_transmission.npz"


def registry_append(row: dict):
    f = REPO / "results/run_registry.csv"
    new = not f.exists()
    with open(f, "a", newline="") as fp:
        w = csv.DictWriter(fp, fieldnames=list(row))
        if new:
            w.writeheader()
        w.writerow(row)


def run_one(spec: dict, batch: dict, dry: bool = False) -> dict:
    root = Path(batch["root"])
    case_cfg = P.resolve(yaml.safe_load((REPO / spec["case"]).read_text()))
    log = []
    # Picard continuation (STATE 2026-11-18): start at iteration `start_it` using the finished run `prev_run`
    # (directory of iteration start_it-1) as the shielding input; the iteration count itself is unchanged.
    prev = Path(spec["prev_run"]) if spec.get("prev_run") else None
    it_start = int(spec.get("start_it", 0))
    if (it_start > 0) != (prev is not None):
        raise ValueError(f"{spec['id']}: start_it > 0 requires prev_run and vice versa")
    for it in range(it_start, int(spec.get("picard", 0)) + 1):
        rd = root / spec["id"] / f"it{it}"
        (rd / "lip_tables").mkdir(parents=True, exist_ok=True)
        for f in ("eos.bin", "opt.bin"):
            shutil.copy2(Path(batch["tables"]) / f, rd / "lip_tables" / f)
        src_out = rd / "source"
        cmd = [sys.executable, "-m", "lipsim.target.build", "--case", str(REPO / spec["case"]), "--out", str(src_out)]
        if prev is not None:
            cmd += ["--shielding", str(prev)]
        if not dry:
            subprocess.run(cmd, check=True, cwd=REPO, env={**__import__("os").environ, "PYTHONPATH": str(REPO / "src")})
            shutil.copy2(src_out / "target_source.bin", rd / "lip_tables/target_source.bin")
        (rd / "pluto.ini").write_text(make_ini(case_cfg, spec["grid"], spec["nec"], spec["tstop_ns"], spec.get("src_on", 1),
                                                spec.get("rmax_par")))
        procs = int(spec.get("procs", 1))
        exe = str(Path(batch["pluto_exe"]).resolve())
        pcmd = ([batch["mpiexec"], "-n", str(procs), exe, "-dec", str(procs), "1"] if procs > 1 and batch.get("mpiexec")
                else [exe])
        t0 = dt.datetime.now()
        rc = None
        if not dry:
            with open(rd / "pluto.log", "w") as fo:
                rc = subprocess.run(pcmd, cwd=rd, stdout=fo, stderr=subprocess.STDOUT).returncode
            merge_surftrans(rd)
        row = {"time": t0.isoformat(timespec="seconds"), "batch": batch.get("name", ""), "run": spec["id"],
               "picard_it": it, "grid": spec["grid"], "nec": spec["nec"], "case": spec["case"], "procs": procs,
               "returncode": rc, "seconds": round((dt.datetime.now() - t0).total_seconds(), 1),
               "commit": _commit(), "registry_sha256": P.registry_hash()[:16],
               "ini_sha256": _sha(rd / "pluto.ini")[:16], "dir": str(rd)}
        if not dry:
            registry_append(row)
        log.append(row)
        if rc not in (0, None):
            break
        prev = rd
    return {"id": spec["id"], "iterations": log}


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("batch", type=Path)
    ap.add_argument("--max-cores", type=int, default=60)
    ap.add_argument("--dry-run", action="store_true")
    a = ap.parse_args(argv)
    batch = yaml.safe_load(a.batch.read_text())
    # Scheduling/run controls belong under numerics for provenance checking.
    # Retain compatibility with existing external batch files.
    runs = batch["numerics"]["runs"] if "numerics" in batch else batch["runs"]
    # pack by cores: a member starts as soon as its MPI ranks fit into the free cores (max_cores in total)
    import threading
    free = [a.max_cores]
    cv = threading.Condition()

    def gated(spec):
        need = min(int(spec.get("procs", 1)), a.max_cores)
        with cv:
            cv.wait_for(lambda: free[0] >= need)
            free[0] -= need
        try:
            return run_one(spec, batch, a.dry_run)
        finally:
            with cv:
                free[0] += need
                cv.notify_all()

    with ThreadPoolExecutor(max_workers=len(runs)) as ex:
        res = list(ex.map(gated, runs))
    out = Path(batch["root"]) / "batch_summary.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(res, indent=1, default=str))
    print(json.dumps([{"id": r["id"], "rc": [i["returncode"] for i in r["iterations"]]} for r in res]))


if __name__ == "__main__":
    main()
