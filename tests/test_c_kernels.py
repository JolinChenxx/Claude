"""Compile and run the pure-C PLUTO-module kernels (pluto/lip) against the Python reference."""
import shutil
import subprocess
import sys
from pathlib import Path

import numpy as np
import pytest

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
CC = shutil.which("gcc") or shutil.which("cc")
pytestmark = pytest.mark.skipif(CC is None, reason="no C compiler")


def _build(tmp_path, test, *srcs):
    exe = tmp_path / test
    cmd = [CC, "-O2", "-std=c99", "-Wall", "-Wextra", "-Werror", "-o", str(exe),
           str(ROOT / "pluto/tests" / f"{test}.c"), *[str(ROOT / "pluto/lip" / s) for s in srcs], "-lm"]
    subprocess.run(cmd, check=True)
    return exe


def test_c_tables_match_python(tmp_path):
    from scipy.interpolate import RegularGridInterpolator as RGI
    from lipsim.tables.build import read_bin
    tab = ROOT / "verify/ref/tables_v0_lowres/eos.bin"
    ax, f = read_bin(tab)
    A = (ax["log10_rho"], ax["log10_T"], ax["Y_cu"])
    ie, ip = RGI(A, np.log10(f["e"])), RGI(A, np.log10(f["p"]))
    rng = np.random.default_rng(1)
    lines = []
    for _ in range(300):
        pt = [rng.uniform(A[0][0], A[0][-1]), rng.uniform(A[1][0], A[1][-1]), rng.uniform(0, 1)]
        lines.append("%.15g %.15g %.15g %.17g %.17g" % (*pt, 10 ** ie(pt)[0], 10 ** ip(pt)[0]))
    cases = tmp_path / "cases.txt"
    cases.write_text("\n".join(lines))
    exe = _build(tmp_path, "test_lip_tables", "lip_tables.c")
    r = subprocess.run([str(exe), str(tab), str(cases)], capture_output=True, text=True)
    assert r.returncode == 0, r.stdout + r.stderr


def test_c_laser_column(tmp_path):
    exe = _build(tmp_path, "test_lip_physics", "lip_physics.c", "lip_tables.c")
    r = subprocess.run([str(exe)], capture_output=True, text=True)
    assert r.returncode == 0, r.stdout + r.stderr
