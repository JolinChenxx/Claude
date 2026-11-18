"""Cu–air interdiffusion model (lipsim.transport.diffusion): limits checked against closed forms."""
import numpy as np

from lipsim import params as P
from lipsim.eos import mixture as mx
from lipsim.transport.diffusion import D_binary, dh_dY, _mass

K_B = P.get("k_B")


def test_hard_sphere_formula_cold_air_limit():
    """Y→0 at 300 K: n is the air density, so D must equal the closed form with the air mean diameter."""
    s = mx.equilibrium(1.2, 300.0, 1e-6)
    d = P.get("hs_diameter")
    n = sum(v for k, v in s.n.items() if k.split("+")[0] in d)
    x = {k: v / n for k, v in s.n.items() if k.split("+")[0] in d and k.split("+")[0] != "Cu"}
    d_air = sum(x[k] * d[k.split("+")[0]] for k in x)
    m_air = sum(x[k] * _mass(k.split("+")[0]) for k in x)
    mu = _mass("Cu") * m_air / (_mass("Cu") + m_air)
    ref = 3 / (8 * n * (0.5 * (d["Cu"] + d_air)) ** 2) * np.sqrt(K_B * 300.0 / (2 * np.pi * mu))
    assert abs(D_binary(s) / ref - 1) < 0.01
    assert 1e-5 < D_binary(s) < 5e-5            # metal vapour in air at 1 bar, 300 K: order 1e-5 m^2/s


def test_D_scales_inverse_density():
    a = D_binary(mx.equilibrium(1.2, 300.0, 0.3))
    b = D_binary(mx.equilibrium(0.12, 300.0, 0.3))
    assert abs(b / a - 10.0) < 0.05


def test_dh_dY_cold_ideal_gas():
    """At 300 K both pseudo-species are ideal gases without excitation: h_Cu − h_air = (5/2 k/m_Cu − 7/2 k/m_air) T."""
    s = mx.equilibrium(1.2, 300.0, 0.5)
    m_air = 0.79 * _mass("N2") + 0.21 * _mass("O2")
    ref = (2.5 * K_B / _mass("Cu") - 3.5 * K_B / m_air) * 300.0
    assert abs(dh_dY(1.2, 300.0, 0.5) / ref - 1) < 0.03
    del s


def test_v2_limits():
    """v2 equals hard spheres for neutral gas and is far smaller in a fully ionised plasma (Coulomb collisions)."""
    from lipsim.transport.diffusion import D_binary_v2
    s = mx.equilibrium(1.2, 300.0, 0.5)
    assert abs(D_binary_v2(s) / D_binary(s) - 1) < 0.01
    s = mx.equilibrium(1e-3, 4.0e4, 0.5)
    assert D_binary_v2(s) < 0.05 * D_binary(s)
