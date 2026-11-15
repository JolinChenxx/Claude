"""W2 optics tests: textbook limits and table round trip."""
import sys
from pathlib import Path

import numpy as np
import pytest

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
from lipsim import params as P  # noqa: E402
from lipsim.eos import mixture as mx  # noqa: E402
from lipsim.optics import props as op  # noqa: E402


def test_kramers_ff_power_textbook():
    # total free-free power coefficient: 1.42e-40 * sqrt(T) W m^3 (Z=1, g=1)
    kT = P.get("k_B") * 1e4
    e2 = P.get("e") ** 2 / (4 * np.pi * P.get("eps0"))
    coef = 32 * np.pi / 3 * np.sqrt(2 * np.pi / (3 * P.get("m_e"))) * e2 ** 3 / (P.get("m_e") * P.get("c") ** 3 * P.get("h")) * np.sqrt(kT)
    assert coef == pytest.approx(1.42e-38, rel=0.01)


def test_ib_absorption_order_of_magnitude():
    # 1 eV, ne = ni = 1e24 m^-3, Z = 1, 1064 nm: Kramers ff with stimulated emission ~10 m^-1
    T = P.get("e") / P.get("k_B")
    nu = P.get("c") / 1064e-9
    x = P.get("h") * nu / (P.get("k_B") * T)
    kff = op._K(nu, T, 1) * 1e48 * (-np.expm1(-x))
    assert 5 < kff < 20


def test_nec_trap_below_thin_and_positive():
    s = mx.equilibrium(0.05, 10000.0, 1.0)
    thin, trap = op.nec(s, "thin"), op.nec(s, "trap")
    assert 0 < trap < thin


def test_table_roundtrip():
    from lipsim.tables.build import read_bin
    d = ROOT / "verify" / "ref" / "tables_v0_lowres"
    ax, f = read_bin(d / "eos.bin")
    ax2, g = read_bin(d / "opt.bin")
    i, j, k = 5, 14, 2
    s = mx.equilibrium(10 ** ax["log10_rho"][i], 10 ** ax["log10_T"][j], ax["Y_cu"][k])
    assert f["e"][i, j, k] == pytest.approx(s.e, rel=1e-10)
    assert g["nec_thin"][i, j, k] == pytest.approx(op.nec(s, "thin"), rel=1e-10)
