"""Collect the small outputs of a batch root into the repository (≤ 20 MB) for review.

    python scripts/collect_v0.py D:/sim_runs/T011 --out results/T011
Copies, per member/iteration: budget.csv, pluto.ini, source/source_meta.json, image/image_summary.json,
image/metrics_*.csv (frames_*.npz only for the last Picard iteration of each member), last 50 log lines.
"""
import argparse
import shutil
from pathlib import Path

ap = argparse.ArgumentParser()
ap.add_argument("root", type=Path)
ap.add_argument("--out", type=Path, required=True)
a = ap.parse_args()
for member in sorted(p for p in a.root.iterdir() if p.is_dir()):
    its = sorted(p for p in member.iterdir() if p.is_dir() and p.name.startswith("it"))
    for it in its:
        dst = a.out / member.name / it.name
        dst.mkdir(parents=True, exist_ok=True)
        for f in ("budget.csv", "pluto.ini", "surface_transmission.npz"):
            if (it / f).exists():
                shutil.copy2(it / f, dst / f)
        if (it / "source/source_meta.json").exists():
            shutil.copy2(it / "source/source_meta.json", dst / "source_meta.json")
        if (it / "pluto.log").exists():
            lines = (it / "pluto.log").read_text(errors="replace").splitlines()
            (dst / "pluto_tail.log").write_text("\n".join(lines[-50:]))
            (dst / "consToPrim_count.txt").write_text(str(sum("ConsToPrim" in ln for ln in lines)))
        img = it / "image"
        if img.exists():
            for f in list(img.glob("metrics_*.csv")) + [img / "image_summary.json"]:
                if f.exists():
                    (dst / "image").mkdir(exist_ok=True)
                    shutil.copy2(f, dst / "image" / f.name)
            if it == its[-1]:
                for f in img.glob("frames_b_*.npz"):  # display frames: filter b only (size); metrics cover all
                    shutil.copy2(f, dst / "image" / f.name)
print("collected into", a.out)
