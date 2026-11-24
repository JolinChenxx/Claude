"""Target-surface row in the Fig. S4 crops from the frames alone (no model, no fit).

For every frame: background-subtracted row sums p(row); peak row; on the lower side of the peak the rows where p
falls to 50% and 10% of the peak (linear interpolation). The emission cannot extend below the target face except
through blur, so a lower edge that stays fixed while the plume grows upward marks the occluding surface. For a blurred
step the edge lies at 50% of the intensity just above the face; that intensity is at most the peak, so the surface row
is bracketed by [lower50, lower10]. Lift of the emission peak above the surface: >= lower50 - peak.
Outputs results/surface/surface_rows.csv and summary.json."""
import json
from pathlib import Path

import numpy as np
import yaml
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "results/surface"


def cross(p, pk, lv):
    for i in range(pk, len(p)):
        if p[i] < lv * p.max():
            j = i - 1
            return i - (lv * p.max() - p[i]) / (p[j] - p[i] + 1e-12)  # num (division guard)
    return np.nan


def main():
    M = yaml.safe_load((ROOT / "data/exp/meta.yaml").read_text())
    OUT.mkdir(parents=True, exist_ok=True)
    rows, summ = [], {}
    for s in ("high", "low"):
        S = M["sets"][s]
        um = S["um_per_px"]
        R = []
        for f in S["frames"]:
            a = np.asarray(Image.open(ROOT / f"data/exp/frames/{s}/{f['file']}").convert("L"), float)
            bg = np.median(np.r_[a[:3].ravel(), a[:, :3].ravel(), a[:, -3:].ravel()])  # num (border rows/cols)
            p = np.clip(a - bg, 0, None).sum(1)
            pk = int(p.argmax())
            l50, l10 = cross(p, pk, 0.5), cross(p, pk, 0.1)  # num (edge levels)
            c = np.cumsum(p) / p.sum()
            top10 = float(np.searchsorted(c, 0.1))  # num
            R.append((f["t_ns"], pk, l50, l10, top10))
            rows.append(dict(series=s, t_ns=f["t_ns"], n_rows=len(p), peak_row=pk, lower50_row=round(l50, 2),
                             lower10_row=round(l10, 2), top10pct_mass_row=top10))
        R = np.array(R, float)
        late = R[:, 0] >= 150  # num (frames judged; early frames excluded by the supervisor's rule)
        nr = len(p)
        summ[s] = dict(
            frame_rows=nr, um_per_px=um,
            lower10_row_mean=float(np.nanmean(R[late, 3])), lower10_row_sd=float(np.nanstd(R[late, 3])),
            lower50_row_range=[float(np.nanmin(R[late, 2])), float(np.nanmax(R[late, 2]))],
            top10_row_range=[float(R[late, 4].min()), float(R[late, 4].max())],
            surface_row_bracket=[float(np.nanmedian(R[late, 2])), float(np.nanmedian(R[late, 3]))],
            surface_height_above_frame_bottom_um=[float((nr - 1 - np.nanmedian(R[late, 3])) * um),
                                                  float((nr - 1 - np.nanmedian(R[late, 2])) * um)],
            peak_lift_min_um_range=[float(np.nanmin(R[late, 2] - R[late, 1]) * um), float(np.nanmax(R[late, 2] - R[late, 1]) * um)],
            peak_lift_max_um_range=[float(np.nanmin(R[late, 3] - R[late, 1]) * um), float(np.nanmax(R[late, 3] - R[late, 1]) * um)],
        )
    import csv
    with open(OUT / "surface_rows.csv", "w", newline="") as fp:
        w = csv.DictWriter(fp, fieldnames=list(rows[0]))
        w.writeheader()
        w.writerows(rows)
    (OUT / "summary.json").write_text(json.dumps(summ, indent=1))
    print(json.dumps(summ, indent=1))


if __name__ == "__main__":
    main()
