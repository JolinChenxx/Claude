"""Frame metrics shared by experimental and synthetic images (PLAN v2 §8).

Primary (gain/blur/t0-insensitive) quantities are computed from the background-subtracted signal
inside an automatically found emission mask; secondary quantities reproduce the paper's SI S2
threshold pipeline (threshold 27 → radius/area/volume/mean gray).

Conventions: x = horizontal image axis (radial direction, µm), z = vertical, positive upwards (µm).
All choices below are fixed constants declared here, not tuned to any data.
"""
from __future__ import annotations

from dataclasses import asdict, dataclass

import numpy as np
from scipy import ndimage as ndi

CORNER = 6            # corner patch size (px) for background/noise estimation
MASK_K_SIGMA = 3.0    # emission mask: I - B > max(MASK_MIN, k*sigma_noise)
MASK_MIN = 5.0
MASK_DILATE = 2       # px
SAT_LEVEL = 250.0     # pixels at or above count as saturated (8-bit, JPEG tolerance)
PAPER_THRESHOLD = 27.0


@dataclass
class FrameMetrics:
    t_ns: float
    background: float
    noise: float
    n_sat: int
    peak: float
    L: float               # Σ(I-B) over mask, gray·px
    L_um2: float           # Σ(I-B)·pixel area, gray·µm²
    xc_um: float
    zc_um: float           # centroid height, measured upward from the frame bottom
    sigma_x2_um2: float
    sigma_z2_um2: float
    mask_px: int
    thr_radius_um: float   # paper: half of the maximum horizontal width above threshold 27
    thr_area_mm2: float
    thr_volume_mm3: float
    thr_mean_gray: float   # mean gray of pixels above threshold 27
    thr_top_um: float      # top / bottom edge of the threshold region (from frame bottom)
    thr_bottom_um: float


def corner_stats(img: np.ndarray, c: int = CORNER) -> tuple[float, float]:
    patches = np.concatenate([img[:c, :c].ravel(), img[:c, -c:].ravel(),
                              img[-c:, :c].ravel(), img[-c:, -c:].ravel()])
    med = float(np.median(patches))
    mad = float(np.median(np.abs(patches - med))) * 1.4826
    return med, max(mad, 0.5)


def _largest(mask: np.ndarray) -> np.ndarray:
    lab, n = ndi.label(mask)
    if n == 0:
        return mask & False
    sizes = ndi.sum(mask, lab, range(1, n + 1))
    return lab == (int(np.argmax(sizes)) + 1)


def threshold_region(img: np.ndarray, thr: float) -> np.ndarray:
    m = img > thr
    st = np.ones((3, 3), bool)
    m = ndi.binary_closing(ndi.binary_opening(m, st), st)
    return _largest(m)


def frame_metrics(img: np.ndarray, t_ns: float, um_per_px: float,
                  threshold: float = PAPER_THRESHOLD) -> FrameMetrics:
    img = np.asarray(img, dtype=np.float64)
    h, w = img.shape
    B, s = corner_stats(img)
    sig = img - B
    m = sig > max(MASK_MIN, MASK_K_SIGMA * s)
    m = _largest(ndi.binary_opening(m, np.ones((3, 3), bool)))
    m = ndi.binary_dilation(m, iterations=MASK_DILATE)
    wts = np.where(m, sig, 0.0)
    L = float(wts.sum())
    yy, xx = np.mgrid[0:h, 0:w]
    zz = (h - 1 - yy) * um_per_px
    xx = xx * um_per_px
    if L > 0:
        xc = float((wts * xx).sum() / L)
        zc = float((wts * zz).sum() / L)
        sx2 = float((wts * (xx - xc) ** 2).sum() / L)
        sz2 = float((wts * (zz - zc) ** 2).sum() / L)
    else:
        xc = zc = sx2 = sz2 = float("nan")

    reg = threshold_region(img, threshold)
    if reg.any():
        widths = reg.sum(1).astype(float)
        radius = widths.max() * um_per_px / 2
        area = reg.sum() * um_per_px ** 2 * 1e-6
        vol = float((np.pi * (widths * um_per_px / 2) ** 2 * um_per_px).sum()) * 1e-9
        mean_gray = float(img[reg].mean())
        rows = np.where(reg.any(1))[0]
        top = (h - 1 - rows.min()) * um_per_px
        bottom = (h - 1 - rows.max()) * um_per_px
    else:
        radius = area = vol = 0.0
        mean_gray = top = bottom = float("nan")

    return FrameMetrics(t_ns=float(t_ns), background=B, noise=s, n_sat=int((img >= SAT_LEVEL).sum()),
                        peak=float(img.max()), L=L, L_um2=L * um_per_px ** 2, xc_um=xc, zc_um=zc,
                        sigma_x2_um2=sx2, sigma_z2_um2=sz2, mask_px=int(m.sum()),
                        thr_radius_um=radius, thr_area_mm2=area, thr_volume_mm3=vol,
                        thr_mean_gray=mean_gray, thr_top_um=top, thr_bottom_um=bottom)


def metrics_table(frames, um_per_px: float) -> list[dict]:
    return [asdict(frame_metrics(f["gray"], f["t_ns"], um_per_px)) for f in frames]


def decay_time(t_ns, L, t0: float = 300.0, t1: float = 630.0) -> tuple[float, float]:
    """P1: τ_L = -1/slope of ln L on [t0,t1] (least squares). Returns (tau, standard error)."""
    t = np.asarray(t_ns, float)
    y = np.asarray(L, float)
    sel = (t >= t0) & (t <= t1) & (y > 0)
    if sel.sum() < 3:
        return float("nan"), float("nan")
    A = np.vstack([t[sel], np.ones(sel.sum())]).T
    coef, res, *_ = np.linalg.lstsq(A, np.log(y[sel]), rcond=None)
    k = coef[0]
    n = sel.sum()
    resid = np.log(y[sel]) - A @ coef
    se_k = np.sqrt((resid ** 2).sum() / max(n - 2, 1) / ((t[sel] - t[sel].mean()) ** 2).sum())
    tau = -1.0 / k
    return float(tau), float(se_k / k ** 2)


def time_to_fraction(t_ns, L, frac: float = 0.1, t_ref: float = 300.0) -> float:
    """P2: time after t_ref at which L first falls below frac·L(t_ref) (linear interpolation in ln L)."""
    t = np.asarray(t_ns, float)
    y = np.asarray(L, float)
    order = np.argsort(t)
    t, y = t[order], y[order]
    Lref = np.interp(t_ref, t, y)
    target = frac * Lref
    for i in range(1, len(t)):
        if t[i] > t_ref and y[i] < target <= y[i - 1]:
            a, b = np.log(max(y[i - 1], 1e-30)), np.log(max(y[i], 1e-30))
            return float(t[i - 1] + (np.log(target) - a) / (b - a) * (t[i] - t[i - 1]) - t_ref)
    return float("nan")
