"""Near-wall fields, sonic Knight source (T018c, v1i/PE off, high it2) vs v1k back pressure (T023 extracts).

Rows: density, temperature, absorbed laser power density, all on the beam axis column (r = 1.25 um) and as the
cumulative fraction of the absorbed column power vs height. Extract format as results/T018c/extract (npz, [j, i],
x1/x2 in um, PLUTO code units: rho in kg/m^3 since UNIT_DENSITY = 1 kg/m^3).
    python scripts/plot_nearwall_ab.py [--new results/T023/extract] [--out results/figures/nearwall_ab.png]"""
import argparse
import glob
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
import numpy as np  # noqa: E402
from matplotlib import font_manager  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]
f = "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc"
try:
    font_manager.fontManager.addfont(f)
    plt.rcParams["font.family"] = font_manager.FontProperties(fname=f).get_name()
except Exception:
    pass
plt.rcParams["axes.unicode_minus"] = False
plt.rcParams["mathtext.fontset"] = "dejavusans"
BEAM_R_UM = 11.0  # num (1/e2 radius of the 22 um beam, registry D_beam_high / 2)


def load(p):
    d = np.load(p)
    r, z = d["x1"], d["x2"]
    dr, dz = np.gradient(r), np.gradient(z)
    dV = 2 * np.pi * r[None, :] * dr[None, :] * dz[:, None]
    P = (d["qlas"] * dV).sum(1)
    return dict(t=float(d["t_ns"]), z=z, r=r, rho=d["rho"], T=d["T"], q=d["qlas"], cum=np.cumsum(P) / max(P.sum(), 1e-300))


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("--new", type=Path, default=ROOT / "results/T023/extract")
    ap.add_argument("--out", type=Path, default=ROOT / "results/figures/nearwall_ab.png")
    a = ap.parse_args(argv)
    old = [load(ROOT / "results/T018c/extract/it2_high_thin_G0_62.498710ns.npz")]
    new = [load(p) for p in sorted(glob.glob(str(a.new / "*high_thin_G0*.npz")))]
    sets = [("旧：声速蒸发（T018c）", "k", old), ("新：带背压 v1k（T023）", None, new)]
    fig, ax = plt.subplots(1, 4, figsize=(15, 4.2))
    cols = plt.cm.viridis(np.linspace(0.1, 0.85, max(len(new), 1)))
    for lab, c0, S in sets:
        for k, s in enumerate(S):
            c = c0 or cols[k]
            ls = "--" if c0 else "-"
            L = f"{lab} {s['t']:.0f} ns"
            ax[0].semilogx(np.maximum(s["rho"][:, 0], 1e-3), s["z"], ls, color=c, label=L)
            ax[1].plot(s["T"][:, 0] / 1e3, s["z"], ls, color=c)
            ax[2].semilogx(np.maximum(s["q"][:, 0], 1e10), s["z"], ls, color=c)
            ax[3].plot(s["cum"], s["z"], ls, color=c)
            m = s["r"] < BEAM_R_UM
            print(f"{L}: wall-cell rho(beam) max {s['rho'][0, m].max():.3g} kg/m3; absorbed in z<5um {np.interp(5, s['z'], s['cum']):.2f}; Tmax {s['T'].max():.0f} K")
    for x, t in zip(ax, ["密度 ρ（轴线）[kg/m³]", "温度 T（轴线）[kK]", "激光吸收功率密度（轴线）[W/m³]", "离靶面高度以内的累计吸收份额"]):
        x.set_xlabel(t)
        x.set_ylim(0, 40)
        x.grid(alpha=0.3)
    from matplotlib.ticker import FuncFormatter, LogLocator
    for x in (ax[0], ax[2]):
        x.xaxis.set_major_locator(LogLocator(numticks=6))
        x.xaxis.set_major_formatter(FuncFormatter(lambda v, _: f"1e{int(round(np.log10(v)))}".replace("-", "-")))
        x.xaxis.set_minor_formatter(FuncFormatter(lambda v, _: ""))
    ax[0].set_ylabel("离靶面高度 z [µm]")
    ax[3].axhline(5, color="r", lw=0.8)
    ax[3].text(0.02, 6, "PREREG 判据：z<5 µm 内 < 50%", color="r", fontsize=8)
    ax[0].axvline(500, color="r", lw=0.8)
    ax[0].text(520, 36, "判据 500", color="r", fontsize=8)
    ax[0].legend(fontsize=7, loc="upper right")
    fig.suptitle("高通量 G0 靶面附近场：声速蒸发（虚线）vs 带背压 v1k（实线）")
    fig.tight_layout()
    a.out.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(a.out, dpi=150)
    print(a.out)


if __name__ == "__main__":
    main()
