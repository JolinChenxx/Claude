"""Current reported results vs experiment (image_v2, filter b, baseline defocus).
    python scripts/plot_current_vs_exp.py  ->  results/figures/current_vs_experiment.png
Panels: high/low L(t)/L(300 ns); high-fluence continuum/lines split (T019); low-fluence axial shock vs Fig. 3d."""
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from matplotlib import font_manager

for f in ("/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",):
    try:
        font_manager.fontManager.addfont(f)
        plt.rcParams["font.family"] = font_manager.FontProperties(fname=f).get_name()
    except Exception:
        pass
B, O, A, K = "#2a78d6", "#eb6834", "#1baf7a", "#222222"
plt.rcParams.update({"font.size": 10, "axes.spines.top": False, "axes.spines.right": False, "axes.grid": True,
                     "grid.color": "#e6e6e6", "grid.linewidth": 0.6, "axes.unicode_minus": False,
                     "axes.formatter.use_mathtext": True, "mathtext.fontset": "dejavusans"})
T_REF = 300.0  # num (normalisation time, ns; registry t_ref_norm)


def exp_L(series):
    e = pd.read_csv(f"data/exp/metrics_{series}.csv").groupby("t_ns", as_index=False).mean(numeric_only=True)
    return e.t_ns.values, e.L.values / np.interp(T_REF, e.t_ns, e.L)


def model_L(path, sa):
    m = pd.read_csv(f"{path}/metrics_b_{sa}_baseline.csv")
    return m.t_ns.values, m.L_radiance.values / np.interp(T_REF, m.t_ns, m.L_radiance)


def shade(a):
    a.axvspan(0, 60, color="#f0f0f0", zorder=0)
    a.text(32, 0.02, "0–60 ns\n不评判", ha="center", fontsize=8, color="#777")


from matplotlib.ticker import FuncFormatter
LOGFMT = FuncFormatter(lambda v, _: f"{v:g}")  # plain decimal tick labels (CJK font lacks the math minus)
fig, ax = plt.subplots(2, 2, figsize=(11, 8))
for a, (s, p, xmax, title) in zip(ax[0], [
        ("high", "results/T016e/high_thin_G0/it2/image_v2", 780, "高通量（第 2 轮，不加相爆炸）"),
        ("low", "results/T016e/low_thin_G0/it1/image_v2", 1280, "低通量（第 1 轮，已收敛）")]):
    te, le = exp_L(s)
    a.semilogy(te, le, "o", color=K, ms=5, label="实验")
    for sa, c, lab in (("thin", B, "模拟：光学薄"), ("sa", O, "模拟：自吸收")):
        t, l = model_L(p, sa)
        a.semilogy(t, l, "-", color=c, lw=2, label=lab)
    shade(a)
    a.set(xlim=(0, xmax), ylim=(0.005, 20), xlabel="t (ns)", ylabel="L(t) / L(300 ns)", title=title)
    a.legend(frameon=False, fontsize=9)

a = ax[1, 0]
s = pd.read_csv("results/T019/split_table.csv")
d = s[(s.series == "high") & (s["mode"] == "thin") & (s["filter"] == "b") & (s.defocus == "baseline")].sort_values("t_ns")
a300 = np.interp(T_REF, d.t_ns, d.L_radiance_all)
te, le = exp_L("high")
a.semilogy(te, le, "o", color=K, ms=5, label="实验")
for col, c, lab in (("all", B, "模拟：总亮度"), ("lines", O, "模拟：只算谱线"), ("continuum", A, "模拟：只算连续谱")):
    a.semilogy(d.t_ns, d["L_radiance_" + col] / a300, "-", color=c, lw=2, label=lab)
shade(a)
a.set(xlim=(0, 780), ylim=(0.005, 20), xlabel="t (ns)", ylabel="L / L_总(300 ns)", title="高通量亮度拆分（T019，光学薄）")
a.legend(frameon=False, fontsize=9)

a = ax[1, 1]
e = pd.read_csv("data/exp/curves/fig3d_shock.csv", comment="#")
ee = e[e.fluence == "low"]
a.plot(ee.t_ns, ee.R_um, "o", color=K, ms=5, label="实验（低通量）")
g0 = pd.read_csv("results/T016e/low_thin_G0/it2/shock_track.csv")
g1 = pd.read_csv("results/T016e_r/low_thin_G1/it0/diagnostics/shock_track.csv")
a.plot(g0.t_ns, g0.R_axis_um, "-", color=B, lw=2, label="模拟：G0 第 2 轮（算到 880 ns）")
a.plot(g1.t_ns, g1.R_axis_um, "-", color=O, lw=2, label="模拟：G1 细网格（算到 557 ns）")
a.set(xlim=(0, 900), xlabel="t (ns)", ylabel="轴向冲击波半径 (µm)", title="低通量冲击波（对比主文 Fig. 3d）")
a.legend(frameon=False, fontsize=9)
for a in (ax[0, 0], ax[0, 1], ax[1, 0]):
    a.yaxis.set_major_formatter(LOGFMT)
fig.suptitle("当前正式结果与实验对照（第二版成像 image_v2，波段 b，散焦基准）", fontsize=12)
fig.tight_layout()
fig.savefig("results/figures/current_vs_experiment.png", dpi=150)
