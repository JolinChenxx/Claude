"""Laser temporal waveform: Gaussian baseline (60 ns FWHM) vs the measured comparable-device trace (IPG YLP-HP,
T036) used in the waveform scenario. Both on the solver time base (t = 0 at the 10%-of-peak leading-edge crossing)
and at the same pulse energy (high fluence, 0.4 mJ). Two panels: power, and cumulative delivered energy.

    python scripts/plot_waveform_compare.py
"""
import sys
from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np
from matplotlib import font_manager

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
from lipsim import params as P  # noqa: E402
from lipsim.target.model import Laser  # noqa: E402

try:
    f = "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc"
    font_manager.fontManager.addfont(f)
    plt.rcParams["font.family"] = font_manager.FontProperties(fname=f).get_name()
except Exception:
    pass
plt.rcParams.update({"font.size": 10, "axes.spines.top": False, "axes.spines.right": False, "axes.grid": True,
                     "grid.color": "#e6e6e6", "grid.linewidth": 0.6, "axes.unicode_minus": False})
C1, C2, INK, INK2 = "#257de0", "#f47230", "#191919", "#666666"

E = P.get("E_pulse_high")
g = Laser(E, P.get("D_beam_high"), P.get("tau_fwhm"))
w = Laser(E, P.get("D_beam_high"), P.get("tau_fwhm"), "data/exp/laser/waveform_ipg_ylp_hp_T3_fig6.csv")
t = np.linspace(-10e-9, 160e-9, 3401)
pg, pw = g.power(t), w.power(t)
eg, ew = np.cumsum(pg) * (t[1] - t[0]) / E, np.cumsum(pw) * (t[1] - t[0]) / E
tn = t * 1e9

fig, (a, b) = plt.subplots(1, 2, figsize=(11, 4.2))
a.plot(tn, pg / 1e3, color=C1, lw=2, label="高斯基准（FWHM 60 ns）")
a.plot(tn, pw / 1e3, color=C2, lw=2, label="同类器件实测波形（IPG YLP-HP，61.7 ns）")
a.axvline(0, color=INK2, lw=0.8, ls=":")
a.text(1.5, a.get_ylim()[1] * 0.92 if a.get_ylim()[1] else 1, "t = 0：峰值 10% 前沿", color=INK2, fontsize=8)
for tp, c in ((g.t_peak, C1), (w.t_peak, C2)):
    a.axvline(tp * 1e9, color=c, lw=0.8, ls="--")
a.set(xlabel="t (ns)", ylabel="激光功率 (kW)", title="脉冲功率（同为 0.4 mJ）", xlim=(-10, 160))
a.legend(frameon=False, fontsize=8, loc="upper right")
b.plot(tn, eg * 100, color=C1, lw=2, label="高斯基准")
b.plot(tn, ew * 100, color=C2, lw=2, label="实测波形")
for frac in (10, 50):
    tg = tn[np.argmax(eg * 100 >= frac)]; tw = tn[np.argmax(ew * 100 >= frac)]
    b.annotate(f"{frac}%：{tw:.0f} ns vs {tg:.0f} ns", xy=(tw, frac), xytext=(tw + 25, frac - 8 if frac == 50 else frac + 6),
               fontsize=8, color=INK, arrowprops=dict(arrowstyle="-", color=INK2, lw=0.6))
b.set(xlabel="t (ns)", ylabel="已送达能量 (%)", title="累计能量：实测波形前段更集中", xlim=(-10, 160), ylim=(0, 102))
b.legend(frameon=False, fontsize=8, loc="lower right")
fig.suptitle("激光时间波形对照（注意：实测波形来自同系列器件，不是论文所用 YLPN 激光器）", fontsize=10)
fig.tight_layout(rect=(0, 0, 1, 0.94))
out = ROOT / "results/figures/laser_waveform_compare.png"
fig.savefig(out, dpi=200)
print(out)
