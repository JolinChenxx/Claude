"""早期 0–80 ns 显示对比（仅显示方式不同，不改动模型或实验数据）。
行 1 实验原图（8 位，50–80 ns 本身已饱和）；行 2 模型，0–80 ns 共用一个增益，使该窗口内最亮像素 = 255（消除饱和、保留窗口内亮度变化）；
行 3/4 实验与模型各帧按自身最亮像素归一（只比形状，不比亮度）。"""
import sys
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts"))
import plot_full_comparison as M
from lipsim.image import camera
import matplotlib.pyplot as plt, numpy as np
OUT = Path(__file__).parent / "figs"
M.DEFOCUS["high"] = "high"; M.IMG = "image_v2_t28"
srow = M.surface_rows()["high"]
ex, _ = M.exp_frames("high")
T = [0, 20, 30, 50, 60, 70, 80]
p = ROOT / "results/T045/high_v1rs_u/it2"
d = np.load(p / "image_v2u_t28/frames_b_sa.npz"); um = float(d["um_per_px"]); D = M.dfoc_of(p)
mod = {int(t): M.occlude(camera.blur(M.place(x, srow), um, D), srow) for t, x in zip(d["t_ns"], d["radiance"]) if int(t) in T}
g = 255.0 / max(mod[t].max() for t in T)
rows = [("实验（原图）", {t: ex[t] for t in T}),
        ("模型 v1rs+v1u\n0–80 ns 共用增益", {t: np.clip(g * mod[t], 0, 255) for t in T}),
        ("实验\n逐帧归一（只看形状）", {t: 255 * ex[t] / ex[t].max() for t in T}),
        ("模型 v1rs+v1u\n逐帧归一（只看形状）", {t: 255 * mod[t] / mod[t].max() for t in T})]
fig, ax = plt.subplots(len(rows), len(T), figsize=(1.75 * len(T) + 1.6, 1.75 * len(rows) + 0.6))
for r, (lab, fr) in enumerate(rows):
    for c, t in enumerate(T):
        a = ax[r, c]; a.set_xticks([]); a.set_yticks([])
        for s in a.spines.values(): s.set_visible(False)
        a.imshow(fr[t], cmap="inferno", vmin=0, vmax=255, interpolation="nearest")
        a.axhline(srow, color="#7fd3ff", lw=0.7, ls="--")
        if r == 0: a.set_title(f"{t} ns", fontsize=9)
        if c == 0: a.set_ylabel(lab, fontsize=8.5, rotation=0, ha="right", va="center")
fig.suptitle("高通量早期 0–80 ns：显示方式对比（Δ=28 ns，散焦 500 µm，靶面遮挡；蓝虚线为靶面）\n只改显示增益，不改数据；实验 50–80 ns 帧本身已饱和", fontsize=10)
fig.tight_layout(rect=(0.06, 0, 1, 0.9), h_pad=0.4, w_pad=0.3)
fig.savefig(OUT / "early_0_80ns_display.png", dpi=170); plt.close(fig)
print("gain", g)
