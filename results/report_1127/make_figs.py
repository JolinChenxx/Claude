"""报告 1127 的新图：v1rs 与波形情景对实验的聚焦对比 + 各工况 σz / L730/L300 柱状图。不改动 results/figures。"""
import sys
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts"))
import plot_full_comparison as M
import matplotlib.pyplot as plt, numpy as np, pandas as pd
OUT = Path(__file__).parent / "figs"
M.IMG = "image_v2_t28"; M.MEMBERS = M.members(); M.OCCLUDE = True; M.GAIN_MODE = "peak150"; M.DEFOCUS["high"] = "high"
srow = M.surface_rows()["high"]
ex, um = M.exp_frames("high")
e = pd.read_csv(ROOT / "data/exp/metrics_high.csv").groupby("t_ns", as_index=False).mean(numeric_only=True)
L_ref = float(np.interp(M.T_REF, e.t_ns, e.L))
rows = [("实验", ex)]
for k, name in (("recond", "报告模型 v1rs\n(T031)"), ("wave", "波形情景\n(T039)")):
    fr, _ = M.model_frames(M.MEMBERS["high"][k], "sa", srow, L_ref); rows.append((name, fr))
times = M.FRAME_T["high"]
fig, ax = plt.subplots(3, len(times), figsize=(1.3 * len(times), 4.6))
for r, (lab, fr) in enumerate(rows):
    for c, t in enumerate(times):
        a = ax[r, c]; a.set_xticks([]); a.set_yticks([]); a.grid(False)
        for s in a.spines.values(): s.set_visible(False)
        if t in fr: a.imshow(fr[t], cmap="inferno", vmin=0, vmax=255, interpolation="nearest")
        a.axhline(srow, color="#7fd3ff", lw=0.7, ls="--")
        if r == 0: a.set_title(f"{t} ns", fontsize=8)
        if c == 0: a.set_ylabel(lab, fontsize=9, rotation=0, ha="right", va="center")
fig.suptitle("高通量：实验 vs 报告模型 v1rs vs 波形情景（Δ=28 ns，散焦 500 µm，靶面遮挡，模型每行按其 150 ns 帧定增益）", fontsize=10)
fig.tight_layout(rect=(0.04, 0, 1, 0.93), h_pad=0.3, w_pad=0.2)
fig.savefig(OUT / "focus_exp_v1rs_wave.png", dpi=170); plt.close(fig)

# 柱状图：各工况 300 ns 的 σz 与 L730/L300（散焦 500 µm，未平移；T040 为数值引自任务报告）
data = [("基线\nT016e", 91, .627), ("背压\nv1k", 87, .597), ("背压+液滴\nT025", 102, .163), ("能量受限\nv1n", 103, .156), ("飞溅前沿\nv1q", 105, .416),
        ("v1n\n细网格", 98, .286), ("飞行蒸发\nv1r", 87, .639), ("v1rs\n(报告)", 100, .219), ("波形\nT039", 111, .194), ("波形\nT040", 106, .215)]
fig, ax = plt.subplots(1, 2, figsize=(12, 4))
n = [d[0] for d in data]; x = np.arange(len(n))
cols = ["#999"] * len(n); cols[7] = "#16a085"; cols[8] = "#e84393"; cols[9] = "#e84393"
ax[0].bar(x, [d[1] for d in data], color=cols); ax[0].axhline(151, color="k", ls="--"); ax[0].text(0, 154, "实验 σz = 151 µm", fontsize=9)
ax[0].set(ylim=(0, 175), ylabel="σz (µm)，300 ns", title="纵向尺寸：所有工况都只有 87–111 µm"); ax[0].set_xticks(x, n, fontsize=7)
ax[1].bar(x, [d[2] for d in data], color=cols); ax[1].axhline(0.107, color="k", ls="--"); ax[1].text(0, 0.12, "实验 0.107", fontsize=9)
ax[1].set(ylabel="L730/L300", title="后期衰减：有液滴/回凝的工况落在 0.15–0.22"); ax[1].set_xticks(x, n, fontsize=7)
fig.suptitle("各工况高通量指标对比（散焦 500 µm，自吸收，未平移时间；T040 取自任务报告）", fontsize=10)
fig.tight_layout(rect=(0, 0, 1, 0.94)); fig.savefig(OUT / "bars_sigmaz_decay.png", dpi=160)
