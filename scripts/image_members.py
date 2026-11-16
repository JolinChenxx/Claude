"""Image every finished PLUTO run (it*/ with a completed dbl.out and no image/image_summary.json) under a batch root.
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
for it in sorted(a.root.glob(f"{a.series}_*/it*")):
    log = it / "pluto.log"
    done = log.exists() and "Done" in log.read_text(errors="replace")[-2000:]
    if not done or (it / "image" / "image_summary.json").exists():
        continue
    print("imaging", it, flush=True)
    r = subprocess.run([sys.executable, "-m", "lipsim.image", str(it), "--series", a.series, "--tables", a.tables,
                        "--out", str(it / "image"), "--workers", str(a.workers)], cwd=repo,
                       env={**__import__("os").environ, "PYTHONPATH": str(repo / "src")})
    print("rc", r.returncode, it, flush=True)
