"""Image every finished PLUTO run (it*/ with a completed solver log and no image/image_summary.json) under a batch root.
usage: python scripts/image_members.py <root> --series high|low --tables <dir> [--workers N]"""
import argparse
import subprocess
import sys
from pathlib import Path

ap = argparse.ArgumentParser()
ap.add_argument("root", type=Path)
ap.add_argument("--series", required=True, choices=["high", "low"])
ap.add_argument("--tables", required=True)
ap.add_argument("--workers", type=int, default=16)
a = ap.parse_args()
repo = Path(__file__).resolve().parents[1]


def solver_finished(run_dir):
    # Windows MPI writes PLUTO's rank-zero log separately from captured stdout.
    for name in ("pluto.log", "pluto.0.log"):
        log = run_dir / name
        if not log.is_file():
            continue
        with log.open("rb") as stream:
            stream.seek(max(0, log.stat().st_size - 2000))
            if b"Done" in stream.read():
                return True
    return False


failed = []
for it in sorted(a.root.glob(f"{a.series}_*/it*")):
    if not solver_finished(it) or (it / "image" / "image_summary.json").exists():
        continue
    print("imaging", it, flush=True)
    r = subprocess.run([sys.executable, "-m", "lipsim.image", str(it), "--series", a.series, "--tables", a.tables,
                        "--out", str(it / "image"), "--workers", str(a.workers)], cwd=repo,
                       env={**__import__("os").environ, "PYTHONPATH": str(repo / "src")})
    print("rc", r.returncode, it, flush=True)

    if r.returncode != 0:
        failed.append(str(it))
if failed:
    print("failed imaging members:", failed, flush=True)
raise SystemExit(1 if failed else 0)
