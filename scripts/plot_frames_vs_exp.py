"""Frame-by-frame comparison (Fig. S4 style): experiment vs reported model members (image_v2, filter b).
    python scripts/plot_frames_vs_exp.py  ->  results/figures/frames_{high,low}.png
The model frame is blurred with the baseline defocus and scaled to 8 bit with the single per-series gain fixed at
t_ref = 300 ns, exactly as in lipsim.image.run (camera.blur, gain_for_reference, to_8bit). Both rows share one gray scale."""
import sys
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import yaml
from matplotlib import font_manager
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))
from lipsim import params as P  # noqa: E402
from lipsim.image import camera  # noqa: E402

f = "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc"
try:
    font_manager.fontManager.addfont(f)
    plt.rcParams["font.family"] = font_manager.FontProperties(fname=f).get_name()
except Exception:
    pass
T_REF = 300  # num (ns, registry t_ref_norm)
MEMBERS = {"high": ("results/T016e/high_thin_G0/it2/image_v2", [80, 150, 200, 300, 400, 500, 580, 630, 730]),
           "low": ("results/T016e/low_thin_G0/it1/image_v2", [90, 140, 210, 280, 380, 500, 630, 750, 900, 1035, 1230])}
meta = yaml.safe_load(open("data/exp/meta.yaml"))["sets"]
dfoc = P.get("defocus_um_scenarios")["baseline"]

for series, (path, times) in MEMBERS.items():
    exp = pd.read_csv(f"data/exp/metrics_{series}.csv").groupby("t_ns", as_index=False).mean(numeric_only=True)
    L_ref = float(np.interp(T_REF, exp.t_ns, exp.L))
    files = {fr["t_ns"]: fr["file"] for fr in meta[series]["frames"]}
    fig, ax = plt.subplots(len(("thin", "sa")) + 1, len(times), figsize=(1.45 * len(times), 5.0))
    for row, sa in enumerate(("thin", "sa"), start=1):
        d = np.load(f"{path}/frames_b_{sa}.npz")
        um = float(d["um_per_px"])
        fr = {int(t): camera.blur(x, um, dfoc) for t, x in zip(d["t_ns"], d["radiance"]) if np.isfinite(x).all()}
        g = camera.gain_for_reference(fr[min(fr, key=lambda t: abs(t - T_REF))], L_ref)
        for c, t in enumerate(times):
            a = ax[row, c]
            if t in fr:
                a.imshow(camera.to_8bit(fr[t], g), cmap="inferno", vmin=0, vmax=255)
            a.set_xticks([]); a.set_yticks([])
            if c == 0:
                a.set_ylabel("模拟\n光学薄" if sa == "thin" else "模拟\n自吸收", fontsize=9)
    for c, t in enumerate(times):
        a = ax[0, c]
        a.imshow(np.array(Image.open(f"data/exp/frames/{series}/{files[t]}")), cmap="inferno", vmin=0, vmax=255)
        a.set_title(f"{t} ns", fontsize=9); a.set_xticks([]); a.set_yticks([])
        if c == 0:
            a.set_ylabel("实验", fontsize=9)
    name = {"high": "高通量（T016e 第 2 轮）", "low": "低通量（T016e 第 1 轮）"}[series]
    fig.suptitle(f"{name}：逐帧对照（波段 b，散焦 {dfoc} µm，同一 8 位灰度，增益在 300 ns 对齐）", fontsize=11)
    fig.tight_layout()
    fig.savefig(f"results/figures/frames_{series}.png", dpi=150)
    plt.close(fig)
