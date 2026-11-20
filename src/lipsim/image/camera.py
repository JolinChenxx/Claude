"""Camera model (PLAN v3 §1.3/§3.2): 10 ns gate, rectangular filter scenario, blur, pixel grid,
single-scalar gain, 8-bit saturation. Produces frames on the experimental pixel grid.

Gate: the frame labelled t integrates radiance over [t, t+10 ns] (registry t0_convention), using the
trapezoid rule on the saved snapshots inside the gate (≥ 3 samples: output cadence ≤ 5 ns).
Blur: Gaussian with σ from the diffraction limit (Airy core σ ≈ 0.42 λ/(2NA)) combined with the pixel
footprint is negligible against the 10.7 µm pixel; the defocus scenario adds a uniform disk of radius
0.436·Δ (geometric, NA 0.4). Gain: frames = G·L with one scalar per series (§1.3), clipped to [0,255].
"""
from __future__ import annotations

import numpy as np
from scipy import ndimage as ndi

from lipsim import params as P


def gate_average(times, frames, t0, width=None):
    width = P.get("gate_width") if width is None else width
    t = np.asarray(times)
    sel = (t >= t0 - 1e-12) & (t <= t0 + width + 1e-12)  # num (float guard)
    if sel.sum() < 2:
        raise ValueError(f"gate at {t0*1e9:.1f} ns has {sel.sum()} samples; need output cadence <= 5 ns")  # math (s -> ns in message)
    ts = t[sel]
    fs = np.asarray(frames)[sel]
    return np.trapezoid(fs, ts, axis=0) / (ts[-1] - ts[0])


def to_pixels(img, z_rows_m, x_cols_m, um_per_px, frame_shape, surface_row=None):
    """Resample a radiance image (rows z ascending from the target, columns x) onto the experimental
    pixel grid; the target surface sits at the bottom edge of the frame unless surface_row is given."""
    h, w = frame_shape
    px = um_per_px * 1e-6  # math (µm -> m)
    surface_row = h - 1 if surface_row is None else surface_row
    zc = (surface_row - np.arange(h) + 0.5) * px          # pixel-centre heights above the target
    xc = (np.arange(w) - (w - 1) / 2) * px
    from scipy.interpolate import RegularGridInterpolator
    f = RegularGridInterpolator((z_rows_m, x_cols_m), img, bounds_error=False, fill_value=0.0)
    Z, X = np.meshgrid(zc, xc, indexing="ij")
    out = f(np.stack([Z, X], -1))
    out[zc < 0, :] = 0.0
    return out


def blur(frame, um_per_px, defocus_um=0.0):
    out = frame
    if defocus_um > 0:
        R = 0.436 * defocus_um / um_per_px  # math (tan(asin(0.4)) = 0.436, geometric defocus disk)
        n = int(np.ceil(R))
        yy, xx = np.mgrid[-n:n + 1, -n:n + 1]
        k = (xx ** 2 + yy ** 2 <= R ** 2).astype(float)
        out = ndi.convolve(out, k / k.sum(), mode="constant")
    return out


def to_8bit(frame, gain):
    return np.clip(np.rint(gain * frame), 0, 255)  # reg:threshold_paper (8-bit camera range 0..255, SI S2)


def gain_for_reference(model_frame_ref, exp_L_ref):
    """One scalar per series: model background-free total signal at t_ref equals the experiment's."""
    s = model_frame_ref.sum()
    return exp_L_ref / s if s > 0 else 0.0
