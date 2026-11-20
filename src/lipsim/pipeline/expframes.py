"""Experimental target dataset: extract S3/S4 plume frames from the SI PDF and invert the jet colormap.

Everything here is deterministic and parameter-free except the constants declared in FRAME_SETS,
which come directly from the SI (frame times, 400 µm scale bar) or are measured from the PDF image
itself (scale-bar pixel length, see measure_scale_bar).
"""
from __future__ import annotations

import io
import subprocess
import tempfile
from dataclasses import dataclass
from pathlib import Path

import numpy as np
from PIL import Image
from scipy import ndimage as ndi

# Frame times exactly as printed in the SI (Fig. S3 low fluence, Fig. S4 high fluence).
S3_TIMES_NS = [10, 15, 30, 35, 60, 90, 130, 140, 210, 220, 240, 280, 340, 380, 405, 480,
               500, 565, 630, 680, 715, 750, 825, 880, 900, 935, 960, 1035, 1120, 1230, 1230]
S4_TIMES_NS = [0, 20, 30, 50, 60, 70, 80, 150, 160, 180, 200, 250, 260, 300, 310,
               330, 350, 360, 400, 420, 450, 500, 510, 530, 580, 610, 630, 730, 740]


@dataclass(frozen=True)
class FrameSet:
    name: str          # 'high' (S4) or 'low' (S3)
    si_page: int       # 1-based page in the SI PDF
    times_ns: tuple
    bar_um: float = 400.0


FRAME_SETS = {
    "high": FrameSet("high", 6, tuple(S4_TIMES_NS)),
    "low": FrameSet("low", 5, tuple(S3_TIMES_NS)),
}


# ---------------------------------------------------------------- jet inversion
def jet_lut(n: int = 256) -> np.ndarray:
    """matplotlib 'jet' as an (n,3) float array in 0..255 (no matplotlib dependency at runtime)."""
    x = np.linspace(0.0, 1.0, n)
    # matplotlib _jet_data segment definitions
    def seg(pts):
        xs, ys = zip(*[(p[0], p[1]) for p in pts])
        return np.interp(x, xs, ys)
    r = seg([(0., 0), (0.35, 0), (0.66, 1), (0.89, 1), (1, 0.5)])
    g = seg([(0., 0), (0.125, 0), (0.375, 1), (0.64, 1), (0.91, 0), (1, 0)])
    b = seg([(0., 0.5), (0.11, 1), (0.34, 1), (0.65, 0), (1, 0)])
    return np.stack([r, g, b], 1) * 255.0


def invert_jet(rgb: np.ndarray, lut: np.ndarray | None = None) -> tuple[np.ndarray, np.ndarray]:
    """Nearest-neighbour inversion of jet. Returns (gray 0..255 float, rgb distance to the LUT)."""
    if lut is None:
        lut = jet_lut()
    # 8-bit rendering makes some neighbouring LUT entries identical (e.g. pure blue around
    # gray 28-31, right at the paper's threshold 27). Map each tie group to its mean index,
    # otherwise argmin always returns the first entry and biases those levels low.
    q = np.rint(lut)
    idx_val = np.arange(len(lut), dtype=np.float64)
    _, inv_u = np.unique(q, axis=0, return_inverse=True)
    inv_u = inv_u.ravel()
    group_mean = np.array([idx_val[inv_u == k].mean() for k in range(inv_u.max() + 1)])
    flat = rgb.reshape(-1, 3).astype(np.float64)
    d2 = ((flat[:, None, :] - q[None, :, :]) ** 2).sum(-1)
    idx = d2.argmin(1)
    gray = group_mean[inv_u[idx]] * (255.0 / (len(lut) - 1))
    dist = np.sqrt(d2[np.arange(len(idx)), idx])
    return gray.reshape(rgb.shape[:2]), dist.reshape(rgb.shape[:2])


def apply_jet(gray: np.ndarray, lut: np.ndarray | None = None) -> np.ndarray:
    if lut is None:
        lut = jet_lut()
    i = np.clip(np.rint(gray / 255.0 * (len(lut) - 1)), 0, len(lut) - 1).astype(int)
    return lut[i].astype(np.uint8)


# ---------------------------------------------------------------- PDF extraction
def extract_page_image(pdf: Path, page: int) -> Image.Image:
    """Embedded raster of one SI page via poppler `pdfimages -j` (original JPEG bytes, no resampling)."""
    with tempfile.TemporaryDirectory() as td:
        subprocess.run(["pdfimages", "-f", str(page), "-l", str(page), "-j", str(pdf), f"{td}/im"],
                       check=True)
        files = sorted(Path(td).iterdir())
        if len(files) != 1:
            raise RuntimeError(f"expected 1 image on page {page}, got {len(files)}")
        return Image.open(io.BytesIO(files[0].read_bytes())).convert("RGB")


def find_frames(page_rgb: np.ndarray, min_size: int = 30) -> list[tuple[int, int, int, int]]:
    """Bounding boxes (r0,r1,c0,c1) of the jet-coloured sub-frames, in reading order."""
    a = page_rgb.astype(int)
    sat = a.max(2) - a.min(2)
    m = ndi.binary_fill_holes(ndi.binary_closing(sat > 60, iterations=2))
    lab, _ = ndi.label(m)
    boxes = []
    for s in ndi.find_objects(lab):
        h, w = s[0].stop - s[0].start, s[1].stop - s[1].start
        if h > min_size and w > min_size:
            boxes.append((s[0].start, s[0].stop, s[1].start, s[1].stop))
    # reading order: cluster rows by top edge, then left to right
    boxes.sort(key=lambda b: (b[0], b[2]))
    rows: list[list] = []
    for b in boxes:
        if rows and abs(b[0] - rows[-1][0][0]) < 10:
            rows[-1].append(b)
        else:
            rows.append([b])
    return [b for r in rows for b in sorted(r, key=lambda b: b[2])]


def measure_scale_bar(page_gray: np.ndarray, region: tuple[int, int, int, int]) -> tuple[float, float]:
    """Scale-bar length in px (sub-pixel half-max crossings). Returns (mean, spread) over bar rows.

    The bar is the thick dark line inside the '400 µm' box; box outlines (longer runs) are excluded
    by taking the rows whose longest dark run is shorter than 90% of the box width.
    """
    r0, r1, c0, c1 = region
    sub = page_gray[r0:r1, c0:c1].astype(float)
    lengths = []
    longest_box = 0
    runs_by_row = []
    for i, p in enumerate(sub):
        idx = np.where(p < 128)[0]
        if len(idx) == 0:
            continue
        runs = np.split(idx, np.where(np.diff(idx) > 1)[0] + 1)
        L = max(runs, key=len)
        longest_box = max(longest_box, len(L))
        runs_by_row.append((i, L))
    for i, L in runs_by_row:
        if 20 < len(L) < 0.9 * longest_box:
            p = sub[i]
            a, b = L[0], L[-1]
            if a == 0 or b + 1 >= len(p):
                continue
            left = a - 1 + (p[a - 1] - 128) / (p[a - 1] - p[a])
            right = b + (p[b] - 128) / (p[b] - p[b + 1])
            lengths.append(right - left)
    lengths = np.array(lengths)
    # keep the core rows of the bar (reject anti-aliased top/bottom rows)
    core = lengths[lengths > np.median(lengths) - 1.0]
    return float(core.mean()), float(max(core.std(), 0.5))


SCALE_BAR_REGIONS = {"high": (640, 733, 400, 501), "low": (640, 718, 400, 529)}


def load_frameset(pdf: Path, name: str) -> dict:
    fs = FRAME_SETS[name]
    page = np.asarray(extract_page_image(pdf, fs.si_page))
    boxes = find_frames(page)
    if len(boxes) != len(fs.times_ns):
        raise RuntimeError(f"{name}: found {len(boxes)} frames, expected {len(fs.times_ns)}")
    gray_page = np.asarray(Image.fromarray(page).convert("L"))
    bar_px, bar_err = measure_scale_bar(gray_page, SCALE_BAR_REGIONS[name])
    # common crop size so that all frames share one pixel grid (trim 1 px border of JPEG ringing)
    h = min(b[1] - b[0] for b in boxes) - 2
    w = min(b[3] - b[2] for b in boxes) - 2
    frames = []
    for (r0, r1, c0, c1), t in zip(boxes, fs.times_ns):
        rgb = page[r0 + 1:r0 + 1 + h, c0 + 1:c0 + 1 + w]
        gray, dist = invert_jet(rgb)
        frames.append({"t_ns": t, "box": (r0, r1, c0, c1), "rgb": rgb, "gray": gray, "lut_dist": dist})
    return {"name": name, "frames": frames, "bar_px": bar_px, "bar_px_err": bar_err,
            "um_per_px": fs.bar_um / bar_px, "um_per_px_err": fs.bar_um / bar_px * bar_err / bar_px,
            "page_shape": page.shape}
