"""W4 target-model tests: V6 (constant-flux semi-infinite analytic solution) and energy conservation."""
import sys
from pathlib import Path

import numpy as np
import pytest

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
from lipsim.target import model as tm  # noqa: E402


class ConstMat(tm.Material):
    """Constant properties, no melting, no evaporation, constant absorptance."""
    def cp(self, T):
        return np.full_like(np.asarray(T, float), 400.0)

    def c_eff(self, T):
        return self.cp(T)

    def k(self, T):
        return np.full_like(np.asarray(T, float), 300.0)

    def absorptance(self, T):
        return np.full_like(np.asarray(T, float), 1.0)

    def knight(self, Ts):
        z = np.zeros_like(Ts)
        return z, z, z, z


class FlatLaser:
    def intensity(self, r, t):
        return np.full_like(np.asarray(r, float), 1e10)


def test_V6_constant_flux_semi_infinite():
    mat = ConstMat()
    o = tm.run(FlatLaser(), mat, np.array([0.0]), t_end=50e-9, dt=0.02e-9)
    t = o["t"][1:]
    exact = 2 * 1e10 * np.sqrt(t / (np.pi * 300.0 * mat.rho * 400.0))
    dT = o["Ts"][1:, 0] - mat.p0 * 0 - 293.15
    sel = t > 5e-9
    assert np.max(np.abs(dT[sel] / exact[sel] - 1)) < 0.01


def test_energy_conservation_high_fluence():
    from lipsim import params as P
    mat = tm.Material()
    las = tm.Laser(P.get("E_pulse_high"), P.get("D_beam_high"), P.get("tau_fwhm"))
    r = np.linspace(0, 25e-6, 6)
    o = tm.run(las, mat, r, t_end=150e-9, dt=0.05e-9)
    A = tm.ring_area(r)
    E = o["E_area"]
    absorbed = (E["abs"] * A).sum()
    out = ((o["stored_area"] + E["vap_latent"] + E["overcap"]) * A).sum()
    assert out == pytest.approx(absorbed, rel=0.01)
