"""W1 tests: Cu–air LTE mixture (molecules + atoms/ions). Known 1-atm air facts are used as checks only."""
import sys
from pathlib import Path

import numpy as np
import pytest

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
from lipsim.eos import mixture as mx  # noqa: E402

pytestmark = pytest.mark.skipif(not (ROOT / "data/atomic/nist/N_I_levels.csv").exists(), reason="NIST tables missing")


def at_p(T, p=101325.0, Y=0.0):
    rho = 1.2 * 293.0 / T
    for _ in range(40):
        s = mx.equilibrium(rho, T, Y)
        rho *= p / s.p
    return s


def frac_in(s, mol, el):
    n = s.n
    tot = sum(v for k, v in n.items() if k.startswith(el + "+")) + n.get("NO", 0) + 2 * n.get(el + "2", 0)
    return 2 * n[mol] / tot


def test_cold_air_is_ideal_diatomic():
    s = at_p(300.0)
    R_air = s.p / (s.rho * 300.0)
    assert R_air == pytest.approx(287.0, rel=3e-3)
    assert s.e == pytest.approx(2.5 * R_air * 300.0, rel=5e-3)   # 5/2 R T (rot + trans), vib frozen


def test_dissociation_temperatures_1atm():
    Ts = np.arange(3000.0, 4001.0, 100.0)
    fo = [frac_in(at_p(T), "O2", "O") for T in Ts]
    T_half_O2 = np.interp(0.5, fo[::-1], Ts[::-1])
    assert 3200 < T_half_O2 < 3800
    Ts = np.arange(6000.0, 8001.0, 200.0)
    fn = [frac_in(at_p(T), "N2", "N") for T in Ts]
    T_half_N2 = np.interp(0.5, fn[::-1], Ts[::-1])
    assert 6500 < T_half_N2 < 7500


def test_no_peak_fraction():
    x = [at_p(T).n["NO"] / sum(at_p(T).n.values()) for T in (3000.0, 3500.0, 4000.0)]
    assert 0.02 < max(x) < 0.08


def test_pure_cu_matches_atomic_solver():
    from lipsim.eos import saha, thermo
    sp = mx.species("Cu")
    rho = 1e24 * mx.MASS_AMU["Cu"] * mx.AMU
    for T in (5000.0, 15000.0):
        a = mx.equilibrium(rho, T, 1.0)
        b = thermo.state([sp], {"Cu": 1e24}, T)
        assert a.ne == pytest.approx(b["ne"], rel=1e-6)
        assert a.u == pytest.approx(b["u"], rel=1e-6)


def test_energy_monotonic_mixture():
    for Y in (0.0, 0.3, 1.0):
        es = [mx.equilibrium(0.5, T, Y).e for T in np.geomspace(300, 60000, 30)]
        assert np.all(np.diff(es) > 0)
