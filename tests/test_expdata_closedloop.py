"""V11 closed-loop test of the experimental-frame pipeline (PLAN v2 §7/§10).

Synthetic frames with known metrics -> 8-bit gray -> jet -> JPEG (quality 75, 4:2:0, the settings
measured from the SI page image) -> jet inversion -> metrics. The errors quantify what the PDF
extraction alone can contribute to experiment-vs-model differences.
"""
import io
import sys
from pathlib import Path

import numpy as np
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))
from lipsim.pipeline.expframes import apply_jet, invert_jet  # noqa: E402
from lipsim.pipeline.metrics import decay_time, frame_metrics, time_to_fraction  # noqa: E402

UM = 10.724
H, W = 82, 85


def synth(t):
    """Smooth plume-like blob: amplitude decays with tau=150 ns after 300 ns, size grows then shrinks."""
    amp = 230.0 if t <= 300 else 230.0 * np.exp(-(t - 300) / 150.0)
    sx = 9.0 + 3.0 * np.tanh(t / 200.0)
    sz = 8.0 + 3.0 * np.tanh(t / 200.0)
    zc = 30.0 + 0.01 * t
    yy, xx = np.mgrid[0:H, 0:W]
    z = H - 1 - yy
    g = amp * np.exp(-((xx - 42) ** 2 / (2 * sx ** 2) + (z - zc) ** 2 / (2 * sz ** 2)))
    rng = np.random.default_rng(int(t))
    return np.clip(g + 3.0 + rng.normal(0, 1.5, g.shape), 0, 255)


def through_pdf(gray):
    rgb = apply_jet(np.rint(gray))
    buf = io.BytesIO()
    Image.fromarray(rgb).save(buf, "JPEG", quality=75, subsampling=2)
    back = np.asarray(Image.open(io.BytesIO(buf.getvalue())).convert("RGB"))
    inv, _ = invert_jet(back)
    return inv


TIMES = [150, 200, 250, 300, 330, 360, 400, 450, 500, 530, 580, 630, 730]


def test_closed_loop_errors():
    truth, ext, gray_err = [], [], []
    for t in TIMES:
        g = np.rint(synth(t))
        e = through_pdf(g)
        gray_err.append(np.sqrt(np.mean((e - g) ** 2)))
        truth.append(frame_metrics(g, t, UM))
        ext.append(frame_metrics(e, t, UM))
    # gray-level round trip
    assert max(gray_err) < 8.0, gray_err
    # P1 decay time and P2 extinction time
    tau_t, _ = decay_time(TIMES, [m.L for m in truth])
    tau_e, _ = decay_time(TIMES, [m.L for m in ext])
    assert abs(tau_e / tau_t - 1) < 0.05, (tau_t, tau_e)
    d_t = time_to_fraction(TIMES, [m.L for m in truth])
    d_e = time_to_fraction(TIMES, [m.L for m in ext])
    assert abs(d_e - d_t) < 20.0, (d_t, d_e)
    # centroid within 1 px (primary metric)
    for a, b in zip(truth, ext):
        if a.peak >= 60:
            assert abs(a.zc_um - b.zc_um) < UM, (a.t_ns, a.zc_um, b.zc_um)
    # secondary threshold-27 radius: jet is nearly flat (pure blue) around gray 22-32, so the
    # paper's threshold is poorly resolved after JPEG. Documented systematic: <= 1.5 px for frames
    # whose peak is >= 60; near-threshold frames are flagged in DATA_NOTES, not asserted.
    for a, b in zip(truth, ext):
        if a.peak >= 60:
            assert abs(a.thr_radius_um - b.thr_radius_um) <= 1.5 * UM, (a.t_ns, a.thr_radius_um, b.thr_radius_um)


if __name__ == "__main__":
    for t in TIMES:
        g = np.rint(synth(t)); e = through_pdf(g)
        a, b = frame_metrics(g, t, UM), frame_metrics(e, t, UM)
        print(t, round(float(np.sqrt(np.mean((e - g) ** 2))), 2), round(b.L / a.L, 3),
              round(a.thr_radius_um, 1), round(b.thr_radius_um, 1), round(b.zc_um - a.zc_um, 1),
              round(b.sigma_x2_um2 / a.sigma_x2_um2, 3))
    test_closed_loop_errors()
    print("ok")
