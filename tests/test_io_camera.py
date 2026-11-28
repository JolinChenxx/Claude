"""PLUTO reader round trip on synthetic files; camera gate/defocus/8-bit behaviour."""
import sys
from pathlib import Path

import numpy as np
import pytest

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
from lipsim.image import camera  # noqa: E402
from lipsim.io import pluto  # noqa: E402


def _write_run(d, n1=6, n2=4):
    e1 = np.linspace(0, 6, n1 + 1)
    e2 = np.linspace(0, 8, n2 + 1)
    with open(d / "grid.out", "w") as f:
        f.write("# GEOMETRY: CYLINDRICAL\n# DIMENSIONS: 2\n")
        for e in (e1, e2, np.array([0.0, 1.0])):
            f.write(f"{len(e)-1}\n")
            for i in range(len(e) - 1):
                f.write(f"{i+1}  {e[i]:.6e}  {e[i+1]:.6e}\n")
    rho = np.arange(n1 * n2, dtype="<f8").reshape(n2, n1)
    prs = rho * 2
    np.concatenate([rho.ravel(), prs.ravel()]).astype("<f8").tofile(d / "data.0003.dbl")
    (d / "dbl.out").write_text("3 1.500000e+01 1.0e-03 120 single_file little rho prs\n")
    return rho, prs


def test_pluto_reader_roundtrip(tmp_path):
    rho, prs = _write_run(tmp_path)
    s = pluto.read_snapshot(tmp_path, 3)
    assert s.t == 15.0
    assert np.array_equal(s.vars["rho"], rho) and np.array_equal(s.vars["prs"], prs)
    g = pluto.read_grid(tmp_path / "grid.out")
    assert len(g.x1) == 6 and len(g.x2) == 4


def test_gate_average_linear_signal():
    t = np.arange(0, 21) * 1e-9
    frames = np.array([np.full((2, 2), ti * 1e9) for ti in t])
    g = camera.gate_average(t, frames, 5e-9)
    assert np.allclose(g, 10.0)  # mean of a linear ramp over [5, 15] ns


def test_defocus_conserves_signal_and_8bit_clips():
    f = np.zeros((41, 41)); f[20, 20] = 100.0
    b = camera.blur(f, 10.72, defocus_um=250.0)
    assert b.sum() == pytest.approx(100.0, rel=1e-12)
    assert camera.to_8bit(np.array([1.0, 3.0]), 100.0).tolist() == [100.0, 255.0]
