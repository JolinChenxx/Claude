"""v1m two-temperature breakdown kernels (C) against independent Python references."""
import shutil
import subprocess
from pathlib import Path

import numpy as np
import pytest
from scipy.integrate import quad
from scipy.special import exp1

ROOT = Path(__file__).resolve().parents[1]
CC = shutil.which("gcc") or shutil.which("cc")
pytestmark = pytest.mark.skipif(CC is None, reason="no C compiler")
E, ME, KB, H = 1.602176634e-19, 9.1093837015e-31, 1.380649e-23, 6.62607015e-34
CHI = 7.72638


@pytest.fixture(scope="module")
def out(tmp_path_factory):
    d = tmp_path_factory.mktemp("bd")
    exe = d / "bd"
    subprocess.run([CC, "-O2", "-std=c99", "-Wall", "-Wextra", "-Werror", "-o", str(exe),
                    str(ROOT / "pluto/tests/test_lip_breakdown.c"), str(ROOT / "pluto/lip/lip_physics.c"),
                    str(ROOT / "pluto/lip/lip_tables.c"), "-lm"], check=True)
    lines = subprocess.run([str(exe)], capture_output=True, text=True, check=True).stdout.splitlines()
    rows = np.array([[float(x) for x in l.split()] for l in lines if not l.startswith("TE")])
    te = float([l for l in lines if l.startswith("TE")][0].split()[1])
    return rows, te


def test_e1(out):
    rows, _ = out
    assert np.allclose(rows[:, 4], exp1(rows[:, 0] / 1e4), rtol=1e-10)


def test_lotz_rate_matches_maxwellian_integral(out):
    rows, _ = out
    for Te, k in rows[::3, :2]:
        kT = KB * Te / E
        g = lambda En: 4.5e-18 * np.log(En / CHI) / (En * CHI) * np.sqrt(2 * En * E / ME) * 2 * np.sqrt(En / np.pi) * kT ** -1.5 * np.exp(-En / kT)  # noqa: E731
        ref = quad(g, CHI, CHI + 80 * kT, limit=400)[0]
        assert abs(k / ref - 1) < 2e-3


def test_saha_and_steady_state(out):
    rows, _ = out
    nCu = 6.4e26
    for Te, S, ne in rows[:, [0, 2, 5]]:
        kT = KB * Te / E
        QI = 2 + 6 * np.exp(-1.389 / kT) + 4 * np.exp(-1.642 / kT)
        Sref = 2 / QI * (2 * np.pi * ME * KB * Te / H ** 2) ** 1.5 * np.exp(-CHI / kT)
        assert abs(S / Sref - 1) < 1e-10
        # steady state of the ionisation ODE is Saha: ne^2 / (nCu - ne) = S
        if ne > 1e21:
            assert abs(ne ** 2 / (nCu - ne) / S - 1) < 1e-6


def test_absorption_positive_and_te_heated(out):
    rows, te = out
    assert np.all(rows[:, 3] > 0)
    assert 8000 < te < 30000      # 0-D prototype: Te ~ 1.0-1.3 eV in fresh Knight vapour at 1.7 GW/cm^2
