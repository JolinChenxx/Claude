"""v1k Knight Knudsen layer with back pressure: limits, registry consistency, monotonicity, C = Python."""
import subprocess
import sys
from pathlib import Path

import numpy as np

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
from lipsim import params as P  # noqa: E402
from lipsim.target import model as tm  # noqa: E402


def test_sonic_limit_matches_registry():
    tr, rr = tm.knight_M(1.0)
    kv = P.get("knight_M1")
    assert abs(tr - kv["Tv_over_Ts"]) < 5e-4 and abs(rr - kv["rhov_over_rhos"]) < 5e-4


def test_equilibrium_and_bounds():
    M, tr, rr = tm.knight_bp(np.array([0.0, 0.1, 0.2062, 1.0, 3.0]))
    assert np.allclose(M[:2], 1.0) and M[3] == 0.0 and M[4] == 0.0
    tr0, rr0 = tm.knight_M(0.0)
    assert abs(tr0 - 1.0) < 1e-12 and abs(rr0 - 1.0) < 1e-12      # M = 0: equilibrium with the saturated vapour


def test_pressure_ratio_is_reproduced_and_monotone():
    pr = np.linspace(0.21, 0.999, 200)
    M, tr, rr = tm.knight_bp(pr)
    assert np.max(np.abs(rr * tr - pr)) < 1e-10
    assert np.all(np.diff(M) < 0)


def test_sonic_state_unchanged_without_back_pressure():
    mat = tm.Material()
    Ts = np.array([3000.0, 5000.0, 7000.0])
    a = mat.knight(Ts)
    b = mat.knight(Ts, pw=np.zeros(3))
    for x, y in zip(a, b):
        assert np.allclose(x, y, rtol=1e-3)                          # registry values are rounded to 3 digits
    assert np.all(mat.knight(Ts, pw=2 * mat.psat(Ts))[0] == 0.0)


def test_c_matches_python(tmp_path):
    import shutil
    cc = shutil.which("gcc") or shutil.which("cc")
    if cc is None:
        import pytest
        pytest.skip("no C compiler")
    exe = tmp_path / "kn"
    subprocess.run([cc, "-O2", "-std=c99", "-Wall", "-Wextra", "-Werror", "-o", str(exe),
                    str(ROOT / "pluto/tests/test_lip_knight.c"), str(ROOT / "pluto/lip/lip_physics.c"),
                    str(ROOT / "pluto/lip/lip_tables.c"), "-lm"], check=True)
    pr = np.r_[0.0, np.linspace(0.15, 1.05, 91)]
    f = tmp_path / "pr.txt"
    f.write_text("\n".join(f"{x:.17g}" for x in pr))
    out = np.loadtxt(subprocess.run([str(exe), str(f)], capture_output=True, text=True, check=True).stdout.splitlines())
    M, tr, rr = tm.knight_bp(pr)
    tr1, rr1 = tm.knight_M(1.0)
    flux = rr * M * np.sqrt(tr) / (rr1 * np.sqrt(tr1))
    assert np.max(np.abs(out[:, 1] - flux)) < 1e-9
    assert np.max(np.abs(out[:, 2] - M)) < 1e-9
