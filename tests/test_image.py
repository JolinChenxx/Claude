"""V10 (minimal): imaging geometry/transfer against analytic results; thin vs self-absorbed consistency."""
import sys
from pathlib import Path

import numpy as np
import pytest

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
from lipsim.image import forward as fw  # noqa: E402


@pytest.mark.parametrize("kappa", [1e-2, 1e2, 1e5])
def test_uniform_sphere_formal_solution(kappa):
    R = 100e-6
    r_edges = np.linspace(0, 200e-6, 801)
    rc = 0.5 * (r_edges[1:] + r_edges[:-1])
    inside = rc < R
    kap = np.where(inside, kappa, 0.0)[:, None]
    S = np.where(inside, 1.0, 0.0)[:, None]
    x = np.linspace(-90e-6, 90e-6, 13)
    I = fw._formal(kap, S, fw.chord_matrix(r_edges, x))[:, 0]
    exact = 1 - np.exp(-2 * kappa * np.sqrt(R ** 2 - x ** 2))
    assert np.allclose(I, exact, rtol=0.02, atol=1e-6)


def test_thin_equals_selfabsorbed_for_dilute_plasma():
    tab = fw.Tables(ROOT / "verify" / "ref" / "tables_v0_lowres")
    r_edges = np.linspace(0, 300e-6, 61)
    rc = 0.5 * (r_edges[1:] + r_edges[:-1])
    zc = np.array([50e-6])
    T = (6000 + 6000 * np.exp(-(rc / 100e-6) ** 2))[None]
    rho = np.full_like(T, 1e-6)  # genuinely thin (at 1e-3 kg/m3 Cu I 515 nm already has tau0 ~ 2)
    Y = np.full_like(T, 0.5)
    x = np.linspace(-250e-6, 250e-6, 11)
    a = fw.band_image(r_edges, zc, rho, T, Y, tab, (515.0, 5.0), x, self_absorption=True)
    b = fw.band_image(r_edges, zc, rho, T, Y, tab, (515.0, 5.0), x, self_absorption=False)
    assert np.allclose(a, b, rtol=0.03)


def test_line_frequency_grid_converged_when_thick():
    tab = fw.Tables(ROOT / "verify" / "ref" / "tables_v0_lowres")
    r_edges = np.linspace(0, 300e-6, 61)
    rc = 0.5 * (r_edges[1:] + r_edges[:-1])
    T = (6000 + 6000 * np.exp(-(rc / 100e-6) ** 2))[None]
    rho = np.full_like(T, 1e-2)
    Y = np.full_like(T, 0.5)
    x = np.linspace(-250e-6, 250e-6, 11)
    a = fw.band_image(r_edges, [0], rho, T, Y, tab, (515.0, 5.0), x)
    old = fw.NF
    try:
        fw.NF = 61
        b = fw.band_image(r_edges, [0], rho, T, Y, tab, (515.0, 5.0), x)
    finally:
        fw.NF = old
    assert np.allclose(a, b, rtol=0.03)
