"""Build the experimental target dataset from the SI PDF (PLAN v2 §10, M0.2/M0.3).

Usage: python scripts/build_expdata.py <SI.pdf> [--out data/exp]
Outputs: frames/{high,low}/t####ns.png (inverted 8-bit gray), meta.yaml, metrics_{high,low}.csv,
         check_{high,low}.png (re-coloured frames for visual comparison with the PDF).
"""
from __future__ import annotations

import argparse
import hashlib
import sys
from pathlib import Path

import numpy as np
import pandas as pd
import yaml
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))
from lipsim.pipeline.expframes import apply_jet, load_frameset  # noqa: E402
from lipsim.pipeline.metrics import decay_time, metrics_table, time_to_fraction  # noqa: E402


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("pdf", type=Path)
    ap.add_argument("--out", type=Path, default=Path("data/exp"))
    a = ap.parse_args()
    meta = {"source_pdf_sha256": hashlib.sha256(a.pdf.read_bytes()).hexdigest(),
            "source": "Chen et al., Laser Photonics Rev. 2025, 19, 2400388, Supporting Information",
            "note": "Gray levels are nearest-neighbour inversions of the matplotlib jet colormap applied to "
                    "the JPEG-compressed figure; not raw CCD counts. See docs/DATA_NOTES.md.",
            "sets": {}}
    for name in ("high", "low"):
        fs = load_frameset(a.pdf, name)
        d = a.out / "frames" / name
        d.mkdir(parents=True, exist_ok=True)
        seen = {}
        files = []
        for f in fs["frames"]:
            t = f["t_ns"]
            k = seen.get(t, 0)
            seen[t] = k + 1
            fn = f"t{t:04d}ns" + (f"_{k}" if k else "") + ".png"
            Image.fromarray(np.clip(np.rint(f["gray"]), 0, 255).astype(np.uint8)).save(d / fn)
            files.append({"t_ns": t, "file": fn, "box_rc": list(map(int, f["box"])),
                          "lut_dist_p95": round(float(np.percentile(f["lut_dist"], 95)), 1)})
        rows = metrics_table(fs["frames"], fs["um_per_px"])
        df = pd.DataFrame(rows)
        df.insert(1, "file", [x["file"] for x in files])
        df.to_csv(a.out / f"metrics_{name}.csv", index=False, float_format="%.6g")
        tau, tau_se = decay_time(df.t_ns, df.L)
        dt10 = time_to_fraction(df.t_ns, df.L, 0.1)
        h, w = fs["frames"][0]["gray"].shape
        meta["sets"][name] = {
            "figure": "S4" if name == "high" else "S3", "si_page": 6 if name == "high" else 5,
            "n_frames": len(files), "frame_px": [int(h), int(w)],
            "scale_bar_px": round(fs["bar_px"], 2), "scale_bar_px_err": round(fs["bar_px_err"], 2),
            "um_per_px": round(fs["um_per_px"], 3), "um_per_px_err": round(fs["um_per_px_err"], 3),
            "P1_tau_L_ns_300_630": round(tau, 1), "P1_tau_se_ns": round(tau_se, 1),
            "P2_dt10_ns_from_300": round(dt10, 1) if np.isfinite(dt10) else None,
            "frames": files}
        # visual check sheet: original RGB | re-coloured inversion
        tiles = [np.concatenate([f["rgb"], apply_jet(f["gray"])], 1) for f in fs["frames"]]
        cols = 5 if name == "high" else 4
        while len(tiles) % cols:
            tiles.append(np.full_like(tiles[0], 255))
        sheet = np.concatenate([np.concatenate(tiles[i:i + cols], 1) for i in range(0, len(tiles), cols)], 0)
        Image.fromarray(sheet).save(a.out / f"check_{name}.png")
        print(name, meta["sets"][name]["um_per_px"], "um/px; P1 tau =", round(tau, 1), "ns; P2 =", dt10)
    (a.out / "meta.yaml").write_text(yaml.safe_dump(meta, sort_keys=False, allow_unicode=True))


if __name__ == "__main__":
    main()
