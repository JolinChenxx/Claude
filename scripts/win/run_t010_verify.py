"""T010 background table build and prescribed smoke checks; no retries or LLM calls.

Launch through job.ps1. Records partial results and exits nonzero on a failed
stage/criterion. Does not change task status, commit, push, or tune physics.
"""
from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import time
import traceback
import zipfile


def now():
    return dt.datetime.now(dt.timezone(dt.timedelta(hours=8))).isoformat()


def write_json(path, value):
    path.write_text(json.dumps(value, indent=2), encoding="utf-8")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--repo", type=Path, required=True)
    ap.add_argument("--raw", type=Path, default=Path("D:/sim_runs/T010"))
    a = ap.parse_args()
    repo, raw = a.repo.resolve(), a.raw.resolve()
    out = repo / "results/T010"
    raw.mkdir(parents=True, exist_ok=True)
    out.mkdir(parents=True, exist_ok=True)
    os.chdir(repo)
    os.environ.update(PYTHONPATH=str(repo / "src"), PYTHONUTF8="1",
                      PYTHONDONTWRITEBYTECODE="1", OMP_NUM_THREADS="1",
                      OPENBLAS_NUM_THREADS="1", MKL_NUM_THREADS="1")
    commit = subprocess.run(["git", "rev-parse", "HEAD"], capture_output=True, text=True, check=True).stdout.strip()
    state = {"started": now(), "commit": commit, "phase": "preparing", "stages": [],
             "failures": [], "note": "Smoke plumbing checks only; no experimental image validation."}
    t0 = time.monotonic()

    def save():
        state["updated"] = now()
        state["wall_seconds"] = time.monotonic() - t0
        write_json(raw / "batch_state.json", state)

    def stage(name, args):
        state["phase"] = name
        save()
        print(f"{now()} START {name}", flush=True)
        start = time.monotonic()
        command = [sys.executable, *map(str, args)]
        log = raw / f"{name}.log"
        with log.open("w", encoding="utf-8") as f:
            rc = subprocess.run(command, cwd=repo, stdout=f, stderr=subprocess.STDOUT).returncode
        entry = {"name": name, "command": command, "exitcode": rc,
                 "seconds": time.monotonic() - start, "log": str(log)}
        state["stages"].append(entry)
        if rc:
            state["failures"].append({"stage": name, "exitcode": rc})
        shutil.copy2(log, out / log.name)
        save()
        print(f"{now()} END {name} rc={rc} seconds={entry['seconds']:.1f}", flush=True)
        return rc

    try:
        # Retain exact inputs even if the repository advances on a later pass.
        source_files = set()
        for folder in ("src", "configs", "data", "pluto/lip", "verify/ref"):
            source_files.update(p for p in (repo / folder).rglob("*")
                                if p.is_file() and "__pycache__" not in p.parts)
        source_files.update((repo / "scripts/compare_budget.py", Path(__file__)))
        manifest = {"commit": commit, "files": {}}
        with zipfile.ZipFile(raw / "execution_inputs.zip", "x", zipfile.ZIP_DEFLATED) as z:
            for p in sorted(source_files):
                name = p.relative_to(repo).as_posix() if p.is_relative_to(repo) else "runner/" + p.name
                z.write(p, name)
                manifest["files"][name] = hashlib.sha256(p.read_bytes()).hexdigest()
        for kind in ("serial", "mpi"):
            exe = Path(f"D:/sim_tools/pluto_lip/build/{kind}/pluto.exe")
            dest = raw / "bin" / kind / "pluto.exe"
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(exe, dest)
            manifest[kind + "_exe_sha256"] = hashlib.sha256(exe.read_bytes()).hexdigest()
        write_json(raw / "execution_inputs.json", manifest)
        shutil.copy2(raw / "execution_inputs.json", out / "execution_inputs.json")

        tables = Path("D:/sim_runs/tables/v0")
        if (tables / "manifest.json").exists():
            raise RuntimeError("Existing full-table manifest: review it before overwriting; no automatic rerun")
        if stage("tables_full", ["-m", "lipsim.tables.build", "--config",
                                  "configs/tables/v0.yaml", "--out", tables, "--workers", "48"]):
            raise RuntimeError("Full-resolution table build failed")
        shutil.copy2(tables / "manifest.json", out / "tables_manifest.json")
        tabman = json.loads((tables / "manifest.json").read_text())
        assert set(tabman["files"]) == {"eos.bin", "opt.bin", "pop.bin"}
        assert all(v["shape"] == [81, 145, 14] for v in tabman["files"].values())
        sys.path.insert(0, str(repo / "src"))
        import numpy as np
        from lipsim.tables.build import read_bin
        finite = {}
        for fn in tabman["files"]:
            _, fields = read_bin(tables / fn)
            finite[fn] = sum(int((~np.isfinite(v)).sum()) for v in fields.values())
        state["table_nonfinite_counts"] = finite
        assert not any(finite.values()), "Nonfinite full-table values; do not tune or retry"

        runs = {}
        for name, config, cores, sub, run_id in (
            ("smoke_serial", "T010_smoke.yaml", 2, "smoke", "high_thin_smoke"),
            ("smoke_mpi", "T010_mpi.yaml", 4, "mpi", "high_thin_smoke_mpi4"),
        ):
            rc = stage(name, ["-m", "lipsim.batch", "configs/batches/" + config, "--max-cores", cores])
            rd = raw / sub / run_id / "it0"
            runs[sub] = rd
            if rc:
                raise RuntimeError(f"{name} batch command failed")
            summary = json.loads((raw / sub / "batch_summary.json").read_text())
            shutil.copy2(raw / sub / "batch_summary.json", out / f"{name}_summary.json")
            if any(i["returncode"] != 0 for run in summary for i in run["iterations"]):
                raise RuntimeError(f"{name} PLUTO returned nonzero (batch CLI itself may exit 0)")
            warnings = sum("ConsToPrim" in line for line in (rd / "pluto.log").read_text(errors="replace").splitlines())
            state[name + "_ConsToPrim_count"] = warnings
            if warnings:
                state["failures"].append({"stage": name, "ConsToPrim_count": warnings})
            shutil.copy2(rd / "budget.csv", out / f"budget_{sub}.csv")
            shutil.copy2(rd / "pluto.log", out / f"pluto_{sub}.log")
        stage("compare_cross_platform", ["scripts/compare_budget.py",
              "verify/ref/smoke_v0/budget_it0_linux.csv", runs["smoke"] / "budget.csv", "--tol", "0.005"])
        stage("compare_serial_mpi", ["scripts/compare_budget.py", runs["smoke"] / "budget.csv",
              runs["mpi"] / "budget.csv", "--tol", "0.005"])
        rc = stage("imaging", ["-m", "lipsim.image", runs["smoke"], "--series", "high",
                  "--tables", "verify/ref/tables_v0_lowres", "--out", raw / "smoke_img", "--workers", "8"])
        if rc == 0:
            shutil.copy2(raw / "smoke_img/image_summary.json", out / "image_summary.json")
        state["phase"] = "needs_review" if state["failures"] else "completed_pending_review"
    except Exception as exc:
        state["failures"].append({"phase": state["phase"], "error": str(exc)})
        state["phase"] = "failed_needs_review"
        (raw / "exception.txt").write_text(traceback.format_exc(), encoding="utf-8")
        shutil.copy2(raw / "exception.txt", out / "exception.txt")
        print(traceback.format_exc(), flush=True)
    state["finished"] = now()
    save()
    shutil.copy2(raw / "batch_state.json", out / "batch_state_final.json")
    print(f"{now()} FINISHED {state['phase']}", flush=True)
    return 0 if state["phase"] == "completed_pending_review" else 2


if __name__ == "__main__":
    raise SystemExit(main())
