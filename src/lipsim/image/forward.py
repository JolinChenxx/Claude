"""Synthetic side-view emission images of an axisymmetric LTE plume (W5, PLAN v3 §3.2 / §8).

Per snapshot (fields on r–z cell centres):
  1. Interpolate tables (pop.bin, eos.bin) at (log10 ρ, log10 T, Y) per cell.
  2. In-band lines (NIST, all species/stages with A in [λ_min, λ_max]): n_u, n_l from n_z/Q_z, Boltzmann.
     Thin: j_l = hc/λ · A n_u / 4π. With self-absorption: Gaussian Doppler profile, κ_l(λ) from
     ∫κ dλ = λ⁴/(8πc) A (g_u/g_l) n_l (1 − e^{−hc/λkT}), source B_λ(T) (LTE), formal solution along each ray.
     Lines carrying 99% of the thin band emission (≤ MAX_RT_LINES) are transferred; the rest are added thin.
  3. Continuum: Kramers–Unsöld κ_c (same model as the 1064 nm absorption) with j_c = κ_c B_λ.
  4. Rays parallel to the image plane normal through concentric shells (exact chord lengths).
No line shift by flow and no Stark broadening in v0 (declared; v1 adds Voigt).
"""
from __future__ import annotations

from dataclasses import dataclass
from functools import lru_cache

import numpy as np
from scipy.interpolate import RegularGridInterpolator

from lipsim import params as P
from lipsim.eos.nist_io import read_lines
from lipsim.optics.props import LINE_SPECTRA, NIST, _K

K_B = P.get("k_B")
H = P.get("h")
C = P.get("c")
AMU = P.get("amu")
MASS_AMU = {"Cu": P.get("A_Cu"), **P.get("atomic_weights_air")}
MAX_RT_LINES = 24  # num
NF = 21            # num: frequency points per transferred line (±3.5 Doppler widths at T_REF_W)
T_REF_W = 3.0e4    # num: temperature defining the per-line frequency grid width


@dataclass
class BandLines:
    el: list
    z: np.ndarray
    wl: np.ndarray   # m
    A: np.ndarray
    El: np.ndarray   # eV
    Eu: np.ndarray
    gl: np.ndarray
    gu: np.ndarray
    mass: np.ndarray  # kg


@lru_cache(maxsize=4)
def band_lines(lmin_nm: float, lmax_nm: float) -> BandLines:
    rows = []
    for el, sps in LINE_SPECTRA.items():
        for z, sp in enumerate(sps):
            L = read_lines(NIST / f"{el}_{sp}_lines.tsv")
            sel = (L.wl_nm >= lmin_nm) & (L.wl_nm <= lmax_nm)
            for i in np.where(sel)[0]:
                rows.append((el, z, L.wl_nm[i] * 1e-9, L.A[i], L.Ei[i], L.Ek[i], L.gi[i], L.gk[i]))  # math (nm->m)
    el = [r[0] for r in rows]
    a = np.array([r[1:] for r in rows], float)
    return BandLines(el, a[:, 0].astype(int), a[:, 1], a[:, 2], a[:, 3], a[:, 4], a[:, 5], a[:, 6],  # math (column indices)
                     np.array([MASS_AMU[e] * AMU for e in el]))


def planck_lambda(lam, T):
    return 2 * H * C ** 2 / lam ** 5 / np.expm1(H * C / (lam * K_B * T))  # math


class Tables:
    """Trilinear interpolation of log10 fields from LIPTAB01 tables on (log10 ρ, log10 T, Y)."""

    def __init__(self, table_dir):
        from lipsim.tables.build import read_bin
        ax, pop = read_bin(f"{table_dir}/pop.bin")
        _, eos = read_bin(f"{table_dir}/eos.bin")
        self.axes = (ax["log10_rho"], ax["log10_T"], ax["Y_cu"])
        self.f = {}
        for k, v in {**pop, "ne": eos["ne"]}.items():
            self.f[k] = RegularGridInterpolator(self.axes, np.log10(np.maximum(v, 1e-300)),  # num (log floor)
                                                bounds_error=False, fill_value=None)

    def __call__(self, name, rho, T, Y):
        pts = np.stack([np.log10(rho), np.log10(T), Y], -1)
        lo = [a[0] for a in self.axes]
        hi = [a[-1] for a in self.axes]
        pts = np.clip(pts, lo, hi)
        return 10.0 ** self.f[name](pts)


def chord_matrix(r_edges: np.ndarray, x: np.ndarray) -> np.ndarray:
    """Half-chord length ds[x, i] of the ray at impact parameter |x| inside shell i (r_edges[i]..r_edges[i+1])."""
    xa = np.abs(x)[:, None]
    ro = r_edges[None, 1:]
    ri = r_edges[None, :-1]
    so = np.sqrt(np.clip(ro ** 2 - xa ** 2, 0, None))
    si = np.sqrt(np.clip(ri ** 2 - xa ** 2, 0, None))
    return so - si


def _formal(kap: np.ndarray, S: np.ndarray, ds: np.ndarray) -> np.ndarray:
    """Emergent intensity along rays crossing the shells twice (far half then near half).
    kap, S: [nr, nf]; ds: [nx, nr]. Returns [nx, nf]."""
    tau = ds[:, :, None] * kap[None]                     # [nx, nr, nf]
    tau_shell_total = 2 * tau                            # both halves
    # optical depth between a segment and the observer:
    # near-half segment i: sum_{j>i} tau_j (near half, outward)
    # far-half segment i : sum_all tau (near half) + sum_{j<i} tau_j (far half, inward)
    cum_out = np.cumsum(tau[:, ::-1], axis=1)[:, ::-1]   # sum_{j>=i}
    beyond_near = cum_out - tau                          # sum_{j>i}
    tot_half = cum_out[:, :1]                            # sum over all shells (one half)
    cum_in = np.cumsum(tau, axis=1) - tau                # sum_{j<i}
    contrib = S[None] * (-np.expm1(-tau))
    I_near = (contrib * np.exp(-beyond_near)).sum(1)
    I_far = (contrib * np.exp(-(tot_half + cum_in))).sum(1)
    del tau_shell_total
    return I_near + I_far


NF_V = 161         # num: frequency points for Stark-broadened (Voigt) lines
WING_HWHM = 25.0   # num: Voigt grid half-span in Lorentz HWHM (wings beyond hold < 3% of the area)


def stark_fwhm_m(lam0: float, ne: np.ndarray) -> np.ndarray:
    """Lorentzian Stark FWHM [m] for registry lines (linear in ne), zeros for other lines."""
    st = P.get("stark_cu1_lines")
    wl = np.asarray(st["wl_nm"]) * 1e-9  # math (nm -> m)
    k = int(np.argmin(np.abs(wl - lam0)))
    if abs(wl[k] - lam0) > 0.05e-9:  # num (line identification tolerance)
        return np.zeros_like(ne)
    return st["w_fwhm_pm"][k] * 1e-12 * ne / st["ne_ref_m3"]  # math (pm -> m)


def band_image(r_edges, z_c, rho, T, Y, tables: Tables, filt, x, self_absorption=True, stark=False):
    """Band-integrated radiance [W/m^2/sr] on (z_c rows) × (x columns) for one snapshot.
    rho, T, Y: [nz, nr] on shells r_edges; filt = (centre_nm, halfwidth_nm), rectangular transmission."""
    lmin, lmax = (filt[0] - filt[1]), (filt[0] + filt[1])
    bl = band_lines(float(lmin), float(lmax))
    ds = chord_matrix(r_edges, x)                        # [nx, nr]
    lam_c = filt[0] * 1e-9                               # math (nm -> m)
    width = 2 * filt[1] * 1e-9                           # math
    nz, nr = T.shape
    img = np.zeros((nz, len(x)))
    nQ = {}
    for el in sorted(set(bl.el)):
        for z in (0, 1):
            nQ[(el, z)] = tables(f"nQ_{el}{z}", rho, T, Y)
    ne = tables("ne", rho, T, Y)
    nion = {k: tables(f"nion_z{k}", rho, T, Y) for k in (1, 2, 3)}
    kT = K_B * T
    # continuum at band centre (slowly varying across ≤ 40 nm)
    nu_c = C / lam_c
    kc = sum(_K(nu_c, T, k) * ne * nion[k] for k in (1, 2, 3)) * np.expm1(H * nu_c / kT)   # [nz, nr]
    Bc = planck_lambda(lam_c, T)
    # thin line emissivities
    jl = np.zeros((len(bl.wl),) + T.shape)
    for i in range(len(bl.wl)):
        nq = nQ[(bl.el[i], bl.z[i])]
        jl[i] = H * C / bl.wl[i] * bl.A[i] * nq * bl.gu[i] * np.exp(-bl.Eu[i] * P.get("e") / kT) / (4 * np.pi)  # math
    for iz in range(nz):
        # continuum (absorbing), integrated over the band width
        I_c = _formal(kc[iz][:, None], Bc[iz][:, None], ds)[:, 0] * width
        if not self_absorption:
            I_c = 2 * (ds * (kc[iz] * Bc[iz])[None]).sum(1) * width
        # lines
        thin_cols = 2 * ds @ jl[:, iz, :].T              # [nx, nlines]
        tot = thin_cols.sum(0)
        if self_absorption and tot.sum() > 0:
            order = np.argsort(tot)[::-1]
            cum = np.cumsum(tot[order]) / tot.sum()
            k = min(int(np.searchsorted(cum, 0.99)) + 1, MAX_RT_LINES)  # num (fraction of thin band emission transferred exactly)
            rt = order[:k]
            I_l = thin_cols.sum(1) - thin_cols[:, rt].sum(1)
            for i in rt:
                lam0 = bl.wl[i]
                dl_ref = lam0 * np.sqrt(8 * K_B * T_REF_W * np.log(2) / (bl.mass[i] * C ** 2))  # math
                Ti = T[iz]
                sig = lam0 * np.sqrt(K_B * Ti / (bl.mass[i] * C ** 2))                    # Gaussian σ_λ
                gam = 0.5 * stark_fwhm_m(lam0, ne[iz]) if stark else np.zeros_like(Ti)  # math (Lorentz HWHM)
                if gam.max() > 0.1 * sig.min():  # num (Voigt needed)
                    from scipy.special import voigt_profile
                    half = 3.5 * dl_ref / (2 * np.sqrt(2 * np.log(2))) + WING_HWHM * gam.max()  # math
                    lg = lam0 + np.linspace(-1.0, 1.0, NF_V) * half  # math
                    phi = voigt_profile(lg[None] - lam0, sig[:, None], gam[:, None])
                else:
                    lg = lam0 + np.linspace(-3.5, 3.5, NF) * dl_ref / (2 * np.sqrt(2 * np.log(2)))  # math
                    phi = np.exp(-0.5 * ((lg[None] - lam0) / sig[:, None]) ** 2) / (np.sqrt(2 * np.pi) * sig[:, None])  # math
                nq = nQ[(bl.el[i], bl.z[i])][iz]
                n_l = nq * bl.gl[i] * np.exp(-bl.El[i] * P.get("e") / kT[iz])
                kint = lam0 ** 4 / (8 * np.pi * C) * bl.A[i] * bl.gu[i] / bl.gl[i] * n_l * (-np.expm1(-H * C / (lam0 * kT[iz])))  # math
                kap = kint[:, None] * phi
                S = planck_lambda(lam0, Ti)[:, None] * np.ones_like(phi)
                Iv = _formal(kap, S, ds)                  # [nx, nf] per unit wavelength
                I_l += np.trapezoid(Iv, lg, axis=1)
        else:
            I_l = thin_cols.sum(1)
        img[iz] = I_c + I_l
    return img
