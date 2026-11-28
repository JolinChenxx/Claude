"""v1r droplet heat transfer: C parcel integration = analytic evaporation time; limits; melt depth; ini parameters."""
import shutil
import subprocess
import sys
from pathlib import Path

import numpy as np
import pytest
import yaml

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
sys.path.insert(0, str(ROOT / "scripts"))
from lipsim import params as P  # noqa: E402
from lipsim.target import model as tm  # noqa: E402


@pytest.fixture(scope="module")
def exe(tmp_path_factory):
    cc = shutil.which("gcc") or shutil.which("cc")
    if cc is None:
        pytest.skip("no C compiler")
    out = tmp_path_factory.mktemp("drop") / "drop"
    subprocess.run([cc, "-O2", "-std=c99", "-Wall", "-Wextra", "-Werror", "-o", str(out),
                    str(ROOT / "pluto/tests/test_lip_droplet.c"), str(ROOT / "pluto/lip/lip_physics.c"),
                    str(ROOT / "pluto/lip/lip_tables.c"), "-lm"], check=True)
    return out


def run(exe, *a):
    return [list(map(float, l.split())) for l in
            subprocess.run([str(exe), *map(str, a)], capture_output=True, text=True, check=True).stdout.splitlines()]


@pytest.mark.parametrize("a0,qfm,K", [(1e-8, 1e9, 1e4), (1e-6, 3e8, 8e3), (3e-6, 1e8, 1e3)])
def test_parcel_time_matches_analytic(exe, a0, qfm, K):
    from droplet_evap_estimate import tau_evap  # same formula as the a-priori estimate
    rl, Lv = P.get("cu_rho_liquid"), P.get("cu_L_vap")
    tau = tau_evap(qfm, K, a0)
    t, q = run(exe, a0, rl, qfm, K, Lv, tau / 20000)[0]
    assert abs(t / tau - 1) < 2e-3
    assert abs(q - 1 / (1 / qfm + a0 / K)) < 1e-9 * q


def test_flux_limits_and_qfm(exe):
    # free-molecular limit for a -> 0, continuum K/a for a -> inf; q_FM = rho vbar deps / 4
    rl, Lv = 8000.0, 4.73e6
    q_small = run(exe, 1e-12, rl, 1e8, 1e4, Lv, 1e-12)[0][1]
    q_big = run(exe, 1.0, rl, 1e8, 1e4, Lv, 1.0)[0][1]
    assert abs(q_small / 1e8 - 1) < 1e-3 and abs(q_big / 1e4 - 1) < 1e-3
    mCu = P.get("A_Cu") * 1.66053906660e-27
    rho, T, de = 0.1, 1e4, 2e7
    q = run(exe, 1e-6, rl, 1e8, 1e4, Lv, 1e-6, rho, T, mCu, de)[1][0]
    assert abs(q / (0.25 * rho * np.sqrt(8 * 1.380649e-23 * T / (np.pi * mCu)) * de) - 1) < 1e-12


def test_melt_depth_recorded():
    las = tm.Laser(P.get("E_pulse_low"), P.get("D_beam_low"), P.get("tau_fwhm"))
    r = np.linspace(0, 2.5 * las.w, 6)
    o = tm.run(las, tm.Material(), r, t_end=150e-9, dt=0.2e-9)
    hm = o["melt_depth_max"]
    assert hm[0] > 1e-7 and hm[0] < 40e-6 and np.all(np.diff(hm) <= 1e-12)   # molten at the centre, shallower outwards


def test_ini_params_v1r():
    from lipsim.batch import make_ini
    cfg = P.resolve(yaml.safe_load((ROOT / "configs/cases/v1r_high.yaml").read_text()))
    ini = make_ini(cfg, "G0", "thin", 800, 1, None, 8e-13, melt_depth_m=3e-6)
    par = dict(l.split()[:2] for l in ini.splitlines() if l and l.split()[0].startswith("DROP"))
    assert abs(float(par["DROP_A0"]) - P.get("rp_drop_over_jet") / 2 * 3e-6) < 1e-15
    assert float(par["DROP_VD"]) == P.get("cu_spatter_speed_high") and float(par["DROP_TPL"]) == 0.0
    assert float(par["DROP_TB"]) == P.get("cu_T_boil") and float(par["DROP_RL"]) == P.get("cu_rho_liquid")
    cfg["target"]["droplet_size"] = 3e-8
    par = dict(l.split()[:2] for l in make_ini(cfg, "G0", "thin", 800, 1, None, 8e-13, melt_depth_m=3e-6).splitlines()
               if l and l.split()[0].startswith("DROP"))
    assert float(par["DROP_A0"]) == 3e-8
