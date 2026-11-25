"""Full-process simulation vs experiment comparison (user request 2026-11-24, 22:00 UTC+8 delivery).

Members are the pre-registered reported members of each model stage, shown side by side. None is picked by its
match to the data:
  baseline  : T016e (v1i, PE off, sonic Knight)           high it2 / low it1
  droplets  : T021  (v1j droplet upper bound)              high it2 / low it1
  backpress : T023  (v1k Knight with back pressure)        latest finished iteration
  combo     : T025  (v1k + v1j droplet upper bound)        latest finished iteration
Imaging: image_v2, filter b, self-absorption (sa) unless --mode thin; baseline defocus; one gain per model and series
set at t_ref = 300 ns (camera.gain_for_reference), common 0–255 scale.
Geometry: the model face is placed on the experimental surface row (results/surface/: low = Fig. 2a Air/Copper line,
high = transferred edge level, descriptive). Rows below the face are the target (dark) before the defocus blur.
Outputs results/figures/full_frames_{high,low}.png and results/figures/full_curves.png.
    python scripts/plot_full_comparison.py [--mode sa|thin]"""
import argparse
import json
import sys
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
import numpy as np  # noqa: E402
import pandas as pd  # noqa: E402
import yaml  # noqa: E402
from matplotlib import font_manager  # noqa: E402
from matplotlib.ticker import FuncFormatter  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
from lipsim import params as P  # noqa: E402
from lipsim.image import camera  # noqa: E402

try:
    f = "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc"
    font_manager.fontManager.addfont(f)
    plt.rcParams["font.family"] = font_manager.FontProperties(fname=f).get_name()
except Exception:
    pass
plt.rcParams.update({"font.size": 9, "axes.spines.top": False, "axes.spines.right": False, "axes.grid": True,
                     "grid.color": "#e6e6e6", "grid.linewidth": 0.6, "axes.unicode_minus": False,
                     "mathtext.fontset": "dejavusans"})
LOGFMT = FuncFormatter(lambda v, _: f"{v:g}")
T_REF = 300.0  # num (registry t_ref_norm, ns)
K = "#222222"
COL = {"baseline": "#2a78d6", "droplets": "#eb6834", "backpress": "#1baf7a", "combo": "#8e44ad", "elim": "#c0392b", "spatter": "#7f8c8d", "g1": "#000000", "flight": "#d4a017", "recond": "#16a085", "wave": "#e84393"}
LAB = {"baseline": "基线（声速蒸发）", "droplets": "液滴上界 v1j", "backpress": "带背压 v1k", "combo": "背压+液滴（T025）", "elim": "能量受限液滴 v1n（T026）", "spatter": "飞溅前沿液滴 v1q（T029）", "g1": "v1n 细网格 G1（T028）", "flight": "飞行+传热蒸发 v1r（T030，熔深液滴）", "recond": "v1r + 回凝 v1rs（T031）", "wave": "v1rs + 同类激光实测波形（T039，情景）"}


IMG = "image_v2"  # --image-dir image_v2_t28: t0-aligned imaging (PREREG t0 alignment)


def latest(base, its):
    for it in its:
        p = ROOT / base / it
        if (p / IMG / "frames_b_sa.npz").exists():
            return p
    return None


def members():
    if IMG != "image_v2":   # t0-aligned imaging (T037 holds the reported members; T039 its own)
        return {"high": {"elim": latest("results/T037/high_thin_G0", ["."]), "recond": latest("results/T037/high_v1rs", ["."]),
                         "wave": latest("results/T039/high_wf", ["it2"])},
                "low": {"elim": latest("results/T037/low_thin_G0", ["."]), "recond": latest("results/T037/low_v1rs", ["."]),
                        "wave": latest("results/T039/low_wf", ["it1"])}}
    return {
        "high": {"baseline": latest("results/T016e/high_thin_G0", ["it2"]),
                 "backpress": latest("results/T023/high_thin_G0", ["it2", "it1"]),
                 "combo": latest("results/T025/high_thin_G0", ["it2", "it1", "it0"]),
                 "elim": latest("results/T026/high_thin_G0", ["it2", "it1", "it0"]),
                 "spatter": latest("results/T029/high_thin_G0", ["it2", "it1", "it0"]),
                 "g1": latest("results/T028/high_thin_G1", ["it0"]),
                 "flight": latest("results/T030/high_rp", ["it2"]),
                 "recond": latest("results/T031/high_v1rs", ["it2"]),
                 "wave": latest("results/T039/high_wf", ["it2"])},
        "low": {"baseline": latest("results/T016e/low_thin_G0", ["it1"]),
                "backpress": latest("results/T023/low_thin_G0", ["it1", "it0"]),
                "combo": latest("results/T025/low_thin_G0", ["it1", "it0"]),
                "elim": latest("results/T026/low_thin_G0", ["it1", "it0"]),
                "spatter": latest("results/T029/low_thin_G0", ["it1", "it0"]),
                "g1": latest("results/T028/low_thin_G1", ["it0"]),
                "flight": latest("results/T030/low_rp", ["it1"]),
                "recond": latest("results/T031/low_v1rs", ["it1"]),
                "wave": latest("results/T039/low_wf", ["it1"])},
    }


MEMBERS = members()
SHOCK = {"high": {"baseline": "results/T016e/high_thin_G0/it2/shock_track.csv"},
         "low": {"baseline": "results/T016e/low_thin_G0/it2/shock_track.csv"}}
FRAME_T = {"high": [20, 60, 80, 150, 200, 250, 300, 350, 400, 450, 500, 580, 630, 740],
           "low": [15, 60, 90, 140, 210, 280, 340, 405, 500, 630, 750, 880, 1035, 1230]}


def surface_rows():
    reg = json.loads((ROOT / "results/surface/fig2_registration.json").read_text())
    return {"low": reg["low_surface_row"], "high": reg["high_transfer_auxiliary"]["high_surface_row"]}


def place(frame, srow):
    """Shift a model frame (face at the bottom edge) so the face sits on row srow; rows below are the target."""
    n = frame.shape[0]
    k = int(round(n - 0.5 - srow))  # math (rows to move up)
    out = np.zeros_like(frame)
    if k > 0:
        out[:n - k] = frame[k:]
    else:
        out = frame.copy()
    return out


def exp_frames(series):
    from PIL import Image
    M = yaml.safe_load((ROOT / "data/exp/meta.yaml").read_text())["sets"][series]
    out = {}
    for fr in M["frames"]:
        out.setdefault(int(fr["t_ns"]), np.asarray(Image.open(ROOT / f"data/exp/frames/{series}/{fr['file']}").convert("L"), float))
    return out, M["um_per_px"]


def occlude(img, srow):
    """Knife-edge occlusion by the target: after the defocus blur, image rows below the face receive no light
    (rays from the plume that head below the surface plane hit the copper in front of the source). Display option."""
    out = img.copy()
    out[int(np.floor(srow)) + 1:] = 0.0
    return out


OCCLUDE = False
GAIN_MODE = "tref"


def model_frames(path, mode, srow, L_ref):
    d = np.load(path / IMG / f"frames_b_{mode}.npz")
    um = float(d["um_per_px"])
    dfoc = P.get("defocus_um_scenarios")["baseline"]
    fr = {int(t): (occlude(camera.blur(place(x, srow), um, dfoc), srow) if OCCLUDE else camera.blur(place(x, srow), um, dfoc)) for t, x in zip(d["t_ns"], d["radiance"]) if np.isfinite(x).all()}
    if not fr:
        return {}, 0.0
    if GAIN_MODE == "peak150":
        # display option: one gain per model row, set so the 150 ns frame's brightest pixel is exactly 255
        k = min(fr, key=lambda t: abs(t - 150))
        g = 255.0 / float(fr[k].max())
    elif GAIN_MODE == "peak":
        # display option: one gain per model row, set so the brightest frame shown (t >= 60 ns) just reaches 255
        shown = [x for t, x in fr.items() if t >= 60]
        g = 255.0 / max(float(x.max()) for x in shown)
    else:
        g = camera.gain_for_reference(fr[min(fr, key=lambda t: abs(t - T_REF))], L_ref)
    return {t: camera.to_8bit(x, g) for t, x in fr.items()}, um


def frames_figure(series, mode, srow):
    ex, um = exp_frames(series)
    e = pd.read_csv(ROOT / f"data/exp/metrics_{series}.csv").groupby("t_ns", as_index=False).mean(numeric_only=True)
    L_ref = float(np.interp(T_REF, e.t_ns, e.L))
    rows = [("实验", ex)]
    for key, p in MEMBERS[series].items():
        if p is not None:
            fr, _ = model_frames(p, mode, srow, L_ref)
            rows.append((f"{LAB[key]}\n{p.parent.parent.name} {p.name}", fr))
        else:
            rows.append((f"{LAB[key]}\n（待推送）", {}))
    times = FRAME_T[series]
    fig, ax = plt.subplots(len(rows), len(times), figsize=(1.25 * len(times), 1.3 * len(rows) + 0.5))
    for r, (lab, fr) in enumerate(rows):
        for c, t in enumerate(times):
            a = ax[r, c]
            a.set_xticks([]), a.set_yticks([])
            a.grid(False)
            for s in a.spines.values():
                s.set_visible(False)
            if t in fr:
                a.imshow(fr[t] if r else ex[t], cmap="inferno", vmin=0, vmax=255, interpolation="nearest")
            else:
                a.set_facecolor("#dddddd")
            a.axhline(srow, color="#7fd3ff", lw=0.7, ls="--")
            if r == 0:
                a.set_title(f"{t} ns" + ("*" if t < 60 else ""), fontsize=8)
            if c == 0:
                a.set_ylabel(lab, fontsize=7.5, rotation=0, ha="right", va="center")
    src = "Fig. 2a 作者标注的 Air/Copper 线" if series == "low" else "由低通量标注线换算（仅供参考）"
    fig.suptitle(f"{'高' if series == 'high' else '低'}通量逐帧对照（波段 b，{'自吸收' if mode == 'sa' else '光学薄'}，散焦 250 µm，"
                 f"{'模型各行按自身最亮帧定增益（不饱和），实验为原 8 位' if GAIN_MODE == 'peak' else ('模型各行按 150 ns 帧最亮像素 = 255 定增益，实验为原 8 位' if GAIN_MODE == 'peak150' else '各行在 300 ns 单独定增益')}，同一 0–255 色标{'；靶面遮挡（刀口）已加' if OCCLUDE else ''}{'；模型时间 = 实验标签 + 28 ns（t0 由激波确定）' if IMG != 'image_v2' else ''}）\n虚线为靶面（{src}）；* 0–60 ns 不评判；比例 {um:.2f} µm/px",
                 fontsize=10)
    fig.tight_layout(rect=(0.04, 0, 1, 0.92), h_pad=0.3, w_pad=0.2)
    out = ROOT / f"results/figures/full_frames_{series}{'_occluded' if OCCLUDE else ''}{'_unsat' if GAIN_MODE == 'peak' else ''}{'_g150' if GAIN_MODE == 'peak150' else ''}{'_' + IMG.split('_')[-1] if IMG != 'image_v2' else ''}.png"
    fig.savefig(out, dpi=170)
    plt.close(fig)
    return out


def geometry_figure(series, mode, srow):
    """Geometry check: model frames placed on the surface row WITHOUT defocus blur, each frame normalised to its own
    maximum and shown on a log scale (1e-3..1), so position and shape are visible without saturation."""
    from matplotlib.colors import LogNorm
    ex, um = exp_frames(series)
    rows = [("实验（8 位计数，各帧自身归一）", {t: np.clip(x - np.median(x[:3]), 1e-6, None) for t, x in ex.items()})]
    for key, p in MEMBERS[series].items():
        if p is None:
            continue
        d = np.load(p / IMG / f"frames_b_{mode}.npz")
        rows.append((f"{LAB[key]}\n{p.parent.parent.name} {p.name}",
                     {int(t): place(x, srow) for t, x in zip(d["t_ns"], d["radiance"]) if np.isfinite(x).all() and x.max() > 0}))
    times = FRAME_T[series]
    fig, ax = plt.subplots(len(rows), len(times), figsize=(1.25 * len(times), 1.3 * len(rows) + 0.5))
    for r, (lab, fr) in enumerate(rows):
        for c, t in enumerate(times):
            a = ax[r, c]
            a.set_xticks([]), a.set_yticks([])
            a.grid(False)
            for sp in a.spines.values():
                sp.set_visible(False)
            if t in fr and fr[t].max() > 0:
                cm = plt.get_cmap("inferno").copy()
                cm.set_bad("black")
                cm.set_under("black")
                a.imshow(np.clip(fr[t] / fr[t].max(), 1e-12, 1.0), cmap=cm, norm=LogNorm(1e-3, 1), interpolation="nearest")
            else:
                a.set_facecolor("#dddddd")
            a.axhline(srow, color="#7fd3ff", lw=0.7, ls="--")
            if r == 0:
                a.set_title(f"{t} ns" + ("*" if t < 60 else ""), fontsize=8)
            if c == 0:
                a.set_ylabel(lab, fontsize=7.5, rotation=0, ha="right", va="center")
    fig.suptitle(f"{'高' if series == 'high' else '低'}通量几何核对：模型不加散焦模糊，每帧按自身最大值归一，对数色标 1e-3 到 1\n"
                 f"虚线为靶面；用于看位置与形状（亮度比较见主图）", fontsize=10)
    fig.tight_layout(rect=(0.04, 0, 1, 0.92), h_pad=0.3, w_pad=0.2)
    out = ROOT / f"results/figures/full_frames_{series}_geometry.png"
    fig.savefig(out, dpi=170)
    plt.close(fig)
    return out


def lift_curve(path, mode):
    d = np.load(path / IMG / f"frames_b_{mode}.npz")
    um = float(d["um_per_px"])
    dfoc = P.get("defocus_um_scenarios")["baseline"]
    t_, l_ = [], []
    for t, x in zip(d["t_ns"], d["radiance"]):
        if np.isfinite(x).all() and x.max() > 0:
            p = camera.blur(x, um, dfoc).sum(1)
            t_.append(t)
            l_.append((len(p) - 0.5 - int(p.argmax())) * um)  # math
    return np.array(t_), np.array(l_)


def curves_figure(mode, srows):
    fig, ax = plt.subplots(2, 3, figsize=(15, 8.6))
    for r, series in enumerate(("high", "low")):
        e = pd.read_csv(ROOT / f"data/exp/metrics_{series}.csv").groupby("t_ns", as_index=False).mean(numeric_only=True)
        xmax = 780 if series == "high" else 1280
        name = "高通量" if series == "high" else "低通量"
        # (1) normalised total signal
        a = ax[r, 0]
        a.semilogy(e.t_ns, e.L / np.interp(T_REF, e.t_ns, e.L), "o", color=K, ms=4, label="实验")
        for key, p in MEMBERS[series].items():
            if p is None:
                continue
            for md, ls in ((mode, "-"), ("thin" if mode == "sa" else "sa", ":")):
                f = p / IMG / f"metrics_b_{md}_baseline.csv"
                if f.exists():
                    m = pd.read_csv(f)
                    a.semilogy(m.t_ns, m.L_radiance / np.interp(T_REF, m.t_ns, m.L_radiance), ls, color=COL[key], lw=1.8 if ls == "-" else 1.1,
                               label=f"{LAB[key]}（{'自吸收' if md == 'sa' else '光学薄'}）")
        a.axvspan(0, 60, color="#f0f0f0", zorder=0)
        a.set(xlim=(0, xmax), ylim=(0.005, 20), xlabel="t (ns)", ylabel="L(t)/L(300 ns)", title=f"{name}：总亮度衰减（与增益无关）")
        a.yaxis.set_major_formatter(LOGFMT)
        a.legend(frameon=False, fontsize=7)
        # (2) peak lift above the face
        a = ax[r, 1]
        sr = pd.read_csv(ROOT / "results/surface/surface_rows.csv")
        sr = sr[sr.series == series].groupby("t_ns", as_index=False).mean(numeric_only=True)
        um_e = 10.724 if series == "high" else 9.244  # num (meta.yaml um_per_px)
        a.fill_between(sr.t_ns, (sr.lower50_row - sr.peak_row) * um_e, (sr.lower10_row - sr.peak_row) * um_e, color="#bbbbbb", alpha=0.6,
                       label="实验（靶面区间）")
        a.plot(sr.t_ns, (srows[series] - sr.peak_row) * um_e, "o", color=K, ms=3.5,
               label="实验（Fig. 2a 标注靶面）" if series == "low" else "实验（换算靶面，参考）")
        for key, p in MEMBERS[series].items():
            if p is None:
                continue
            t_, l_ = lift_curve(p, mode)
            a.plot(t_, l_, "-", color=COL[key], lw=1.8, label=LAB[key])
        a.axvspan(0, 60, color="#f0f0f0", zorder=0)
        a.set(xlim=(0, xmax), ylim=(0, 400), xlabel="t (ns)", ylabel="发光峰离靶面高度 (µm)", title=f"{name}：发光峰高度（指标 S1）")
        a.legend(frameon=False, fontsize=7)
        # (3) intensity-weighted width/height (gain-free second moments)
        a = ax[r, 2]
        a.plot(e.t_ns, np.sqrt(e.sigma_x2_um2), "o", color=K, ms=3.5, label="实验 σx（横向）")
        a.plot(e.t_ns, np.sqrt(e.sigma_z2_um2), "s", color=K, ms=3.5, mfc="none", label="实验 σz（纵向）")
        for key, p in MEMBERS[series].items():
            if p is None:
                continue
            f = p / IMG / f"metrics_b_{mode}_baseline.csv"
            if f.exists():
                m = pd.read_csv(f)
                a.plot(m.t_ns, np.sqrt(m.sigma_x2_um2), "-", color=COL[key], lw=1.8, label=f"{LAB[key]} σx")
                a.plot(m.t_ns, np.sqrt(m.sigma_z2_um2), "--", color=COL[key], lw=1.2, label=f"{LAB[key]} σz")
        a.axvspan(0, 60, color="#f0f0f0", zorder=0)
        a.set(xlim=(0, xmax), xlabel="t (ns)", ylabel="亮度加权半宽 (µm)", title=f"{name}：发光区尺寸（二阶矩，与增益无关）")
        a.legend(frameon=False, fontsize=6.5, ncol=2)
    fig.suptitle(f"全过程仿真与实验对照：亮度衰减、发光峰高度、发光区尺寸（波段 b，{'自吸收' if mode == 'sa' else '光学薄'}；实线为主模式）", fontsize=11)
    fig.tight_layout(rect=(0, 0, 1, 0.96))
    out = ROOT / f"results/figures/full_curves{'_' + IMG.split('_')[-1] if IMG != 'image_v2' else ''}.png"
    fig.savefig(out, dpi=160)
    plt.close(fig)
    return out


def shock_figure():
    fig, a = plt.subplots(figsize=(6.5, 4.8))
    e = pd.read_csv(ROOT / "data/exp/curves/fig3d_shock.csv", comment="#")
    for fl, mk, lab in (("high", "s", "实验 高通量"), ("low", "o", "实验 低通量")):
        ee = e[e.fluence == fl]
        a.plot(ee.t_ns, ee.R_um, mk, color=K, ms=5, mfc="none" if fl == "high" else K, label=lab)
    for series, ls in (("high", "-"), ("low", "--")):
        cand = dict(SHOCK[series])
        for key, p in MEMBERS[series].items():
            if p is None or key == "baseline":
                continue
            for f in (p / "diagnostics" / "shock_track.csv", p / "shock_track.csv"):
                if f.exists():
                    cand[key] = str(f.relative_to(ROOT))
                    break
        for key, f in cand.items():
            g = pd.read_csv(ROOT / f)
            off = P.get("t0_offset_shock") if IMG != "image_v2" else 0.0  # experimental label = model time - offset
            a.plot(g.t_ns - off, g.R_axis_um, ls, color=COL[key], lw=1.8, label=f"{LAB[key]}（{'高' if series == 'high' else '低'}通量）")
    a.set(xlim=(0, 900), ylim=(0, 1000), xlabel="t (ns)", ylabel="轴向冲击波半径 (µm)", title="冲击波（对比主文 Fig. 3d）" + (f"，模型时间平移 {P.get('t0_offset_shock'):g} ns" if IMG != "image_v2" else ""))
    a.legend(frameon=False, fontsize=7, loc="lower right")
    fig.tight_layout()
    out = ROOT / f"results/figures/full_shock{'_' + IMG.split('_')[-1] if IMG != 'image_v2' else ''}.png"
    fig.savefig(out, dpi=160)
    plt.close(fig)
    return out


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("--mode", default="sa", choices=["sa", "thin"])
    ap.add_argument("--gain", default="tref", choices=["tref", "peak", "peak150"], help="display option: gain at t_ref (default) or no-saturation gain per row")
    ap.add_argument("--occlude", action="store_true", help="display option: knife-edge occlusion of the image below the face")
    ap.add_argument("--image-dir", default="image_v2", help="imaging subfolder, e.g. image_v2_t28 (t0-aligned)")
    a = ap.parse_args(argv)
    global OCCLUDE, GAIN_MODE, IMG, MEMBERS
    IMG = a.image_dir
    if IMG != "image_v2":
        MEMBERS = members()
    OCCLUDE = a.occlude
    GAIN_MODE = a.gain
    srows = surface_rows()
    if OCCLUDE:
        for s_ in ("high", "low"):
            print(frames_figure(s_, a.mode, srows[s_]))
        return
    for s in ("high", "low"):
        print(s, {k: (str(v.relative_to(ROOT)) if v else None) for k, v in MEMBERS[s].items()})
        print(frames_figure(s, a.mode, srows[s]))
        if IMG == "image_v2":
            print(geometry_figure(s, a.mode, srows[s]))
    print(curves_figure(a.mode, srows))
    print(shock_figure())


if __name__ == "__main__":
    main()
