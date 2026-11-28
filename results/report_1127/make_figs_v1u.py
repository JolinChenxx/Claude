"""报告 1127 补充图：最新报告模型 v1rs+v1u（T045）与实验的逐帧对比；附原 v1rs（T031，修正后成像）作参照。不改动 results/figures。"""
import sys
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts"))
import plot_full_comparison as M
import matplotlib.pyplot as plt, numpy as np, pandas as pd
OUT = Path(__file__).parent / "figs"
M.OCCLUDE = True; M.GAIN_MODE = "peak150"; M.DEFOCUS["high"] = "high"
srow = M.surface_rows()["high"]
ex, um = M.exp_frames("high")
e = pd.read_csv(ROOT / "data/exp/metrics_high.csv").groupby("t_ns", as_index=False).mean(numeric_only=True)
L_ref = float(np.interp(M.T_REF, e.t_ns, e.L))
rows = [("实验", ex)]
for img, path, name in (("image_v2u_t28", ROOT / "results/T045/high_v1rs_u/it2", "最新报告模型\nv1rs+v1u (T045)"),
                        ("image_v2c_t28", ROOT / "results/T031/high_v1rs/it2", "原报告模型\nv1rs (T031)")):
    M.IMG = img
    fr, _ = M.model_frames(path, "sa", srow, L_ref); rows.append((name, fr))
times = M.FRAME_T["high"]
fig, ax = plt.subplots(len(rows), len(times), figsize=(1.3 * len(times), 1.55 * len(rows)))
for r, (lab, fr) in enumerate(rows):
    for c, t in enumerate(times):
        a = ax[r, c]; a.set_xticks([]); a.set_yticks([]); a.grid(False)
        for s in a.spines.values(): s.set_visible(False)
        if t in fr: a.imshow(fr[t], cmap="inferno", vmin=0, vmax=255, interpolation="nearest")
        a.axhline(srow, color="#7fd3ff", lw=0.7, ls="--")
        if r == 0: a.set_title(f"{t} ns", fontsize=8)
        if c == 0: a.set_ylabel(lab, fontsize=9, rotation=0, ha="right", va="center")
fig.suptitle("高通量：实验 vs 最新报告模型 v1rs+v1u（Δ=28 ns，散焦 500 µm，靶面遮挡，模型每行按其 150 ns 帧定增益；蓝虚线为靶面）", fontsize=10)
fig.tight_layout(rect=(0.04, 0, 1, 0.93), h_pad=0.3, w_pad=0.2)
fig.savefig(OUT / "focus_exp_v1rsu.png", dpi=170); plt.close(fig)
print("ok", [len(f) for _, f in rows])
