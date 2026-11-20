"""EOS (Saha) unit tests: analytic limit + NIST-table sanity (archived COMSOL NIST tables until T007 lands)."""
import sys
from pathlib import Path

import numpy as np
import pytest

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
from lipsim.eos import saha  # noqa: E402
from lipsim.eos.nist_io import Levels, read_ionization, read_levels  # noqa: E402

PRIOR = ROOT / "prior" / "comsol" / "baseline" / "data"
NEW = ROOT / "data" / "atomic" / "nist"


def _hydrogen():
    lv = Levels("H I", np.array([0.0]), np.array([2.0]), 13.6, 1, 0)
    return saha.Species("H", [lv], [13.598434], g_top=1.0)


def test_saha_hydrogen_analytic_low_density():
    sp = _hydrogen()
    n, T = 1e16, 10000.0  # very dilute: IP lowering negligible
    r = saha.solve([sp], {"H": n}, T)
    # closed form: x^2/(1-x) = (1/n) * 2*(Q1/Q0)*pref*exp(-chi/kT) with Q1=1, Q0=2
    S = saha.saha_prefactor(T) * np.exp(-13.598434 * saha.EV / (saha.K_B * T)) / n
    x = (-S + np.sqrt(S * S + 4 * S)) / 2
    assert r.fractions["H"][1] == pytest.approx(x, rel=1e-6)
    assert r.ne == pytest.approx(x * n, rel=1e-6)
    assert r.dchi_unit_eV < 1e-3


def _levels_dir():
    if (NEW / "Cu_I_levels.csv").exists():
        return NEW
    if (PRIOR / "Cu_I_levels.csv").exists():
        return PRIOR
    pytest.skip("no NIST level tables available")


def test_cu_levels_and_partition():
    d = _levels_dir()
    lv = read_levels(d / "Cu_I_levels.csv", "Cu I")
    assert lv.first_limit_eV == pytest.approx(7.72638, abs=1e-4)
    assert lv.energy_eV.min() == 0.0 and lv.g[np.argmin(lv.energy_eV)] == 2
    # metastable 3d9 4s2 2D5/2 at 1.389 eV must be present with g=6
    i = np.argmin(abs(lv.energy_eV - 1.388948))
    assert abs(lv.energy_eV[i] - 1.388948) < 1e-4 and lv.g[i] == 6
    assert saha.partition_function(lv, 300.0, 7.7) == pytest.approx(2.0, rel=1e-6)
    q10k = saha.partition_function(lv, 10000.0, 7.7)
    assert 2.0 < q10k < 10.0


def test_cu_saha_charge_neutral_and_monotonic():
    d = _levels_dir()
    ie = read_ionization(d / "Cu_ionization.csv")
    stages = [s for s in ("Cu I", "Cu II", "Cu III") if (d / f"{s.replace(' ', '_')}_levels.csv").exists()]
    lvs = [read_levels(d / f"{s.replace(' ', '_')}_levels.csv", s) for s in stages]
    sp = saha.Species("Cu", lvs, [ie[s] for s in stages], g_top=1.0)
    n = 1e24
    prev = 0.0
    for T in (5000.0, 8000.0, 12000.0, 20000.0):
        r = saha.solve([sp], {"Cu": n}, T)
        zbar = np.dot(np.arange(sp.stages), r.fractions["Cu"])
        assert r.ne == pytest.approx(zbar * n, rel=1e-8)
        assert zbar > prev
        prev = zbar
    assert 0.0 < r.dchi_unit_eV < 1.0


def test_thermo_limits_and_energy_monotonic():
    from lipsim.eos import thermo
    d = _levels_dir()
    ie = read_ionization(d / "Cu_ionization.csv")
    stages = ["Cu I", "Cu II", "Cu III"]
    sp = saha.Species("Cu", [read_levels(d / f"{s.replace(' ', '_')}_levels.csv", s) for s in stages],
                      [ie[s] for s in stages])
    n = 1e24
    cold = thermo.state([sp], {"Cu": n}, 1000.0)
    # cold, neutral, ground state: ideal monatomic gas
    assert cold["zbar"] < 1e-10
    assert cold["u"] == pytest.approx(1.5 * n * saha.K_B * 1000.0, rel=1e-4)  # metastable 2D adds ~3e-6
    us = [thermo.state([sp], {"Cu": n}, T)["u"] for T in np.linspace(3000, 30000, 28)]
    assert np.all(np.diff(us) > 0)  # positive heat capacity
