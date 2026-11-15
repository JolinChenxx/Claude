"""Target heating and evaporation source (W4, PLAN v3 §3.2/§4.1) — v0 one-way coupling.

Per radial ring (no lateral conduction; 11 µm spot vs ~1 µm thermal length in 100 ns):
  ρ c_eff(T) ∂T/∂t = ∂/∂z (k(T) ∂T/∂z),   z ≥ 0 into the target, adiabatic at the bottom
  −k ∂T/∂z|_s = A(T_s) · τ_sh(r,t) · I(r,t) − ṁ(T_s) · L_v
Melting by apparent heat capacity over ΔT_m (N-grade); constant solid density for the Lagrangian grid
and no surface recession in v0 (ablated depth reported). T_s capped at f_Tcap·T_c; the energy that
would have raised T_s above the cap is booked as "over-cap" (U4 trigger, PLAN §3.6).
Evaporation: Clausius–Clapeyron p_sat through (T_boil, 1 atm) with L_v; Knight sonic Knudsen layer
(M = 1): T_v = 0.669 T_s, ρ_v = 0.308 ρ_sat, u_v = sqrt(5/3 k T_v / m), ṁ = ρ_v u_v (v0 uses the sonic
relation at all p_sat — an upper bound at low p_sat where ṁ is negligible; v1 adds back pressure).
Absorptance A(T): A_RT (optical constants) + Drude change driven by the measured resistivity,
A(T) = A_RT + A_D(ρ_e(T)) − A_D(ρ_e(293 K)), A_D from Fresnel with ε = 1 − ω_p²/(ω² + iων), ν = ε0 ω_p² ρ_e.
"""
from __future__ import annotations

from dataclasses import dataclass, field

import numpy as np
from scipy.linalg import solve_banded

from lipsim import params as P

K_B = P.get("k_B")
AMU = P.get("amu")
EPS0 = P.get("eps0")
E_CH = P.get("e")
M_E = P.get("m_e")
C = P.get("c")


def _tab(key, col):
    v = P.get(key)
    return np.asarray(v["T"], float), np.asarray(v[col], float)


@dataclass
class Material:
    m_atom: float = field(default_factory=lambda: P.get("A_Cu") * AMU)
    Tm: float = field(default_factory=lambda: P.get("cu_T_melt"))
    Tb: float = field(default_factory=lambda: P.get("cu_T_boil"))
    Lm: float = field(default_factory=lambda: P.get("cu_L_melt"))
    Lv: float = field(default_factory=lambda: P.get("cu_L_vap"))
    rho: float = field(default_factory=lambda: P.get("cu_rho_solid"))
    cp_l: float = field(default_factory=lambda: P.get("cu_cp_liquid"))
    Tc: float = field(default_factory=lambda: P.get("cu_T_crit"))
    f_cap: float = field(default_factory=lambda: P.get("f_Tcap"))
    A_RT: float = field(default_factory=lambda: P.get("cu_A1064_RT"))
    p0: float = field(default_factory=lambda: P.get("p_ambient"))
    dTm: float = 2.0  # num: melting smear (apparent heat capacity), K

    def __post_init__(self):
        self.Tcp, self.cps = _tab("cu_cp_solid", "cp")
        self.Tks, self.ks = _tab("cu_k_solid", "k")
        self.Tkl, self.kl = _tab("cu_k_liquid", "k")
        self.Tr, self.rr = _tab("cu_resistivity", "rho_e")
        lam = P.get("wavelength_laser")
        self.omega = 2 * np.pi * C / lam  # math
        self.wp2 = P.get("cu_free_electron_density") * E_CH ** 2 / (EPS0 * M_E)
        self.A_D_RT = self._A_drude(np.interp(293.0, self.Tr, self.rr))  # num (reference temperature of A_RT)

    def cp(self, T):
        return np.where(T < self.Tm, np.interp(T, self.Tcp, self.cps), self.cp_l)

    def c_eff(self, T):
        """Apparent heat capacity incl. latent heat of fusion spread over [Tm - dTm/2, Tm + dTm/2]."""
        bump = np.where(np.abs(T - self.Tm) <= self.dTm / 2, self.Lm / self.dTm, 0.0)  # math
        return self.cp(T) + bump

    def k(self, T):
        # beyond the last table point the liquid conductivity is held constant (no extrapolation)
        return np.where(T < self.Tm, np.interp(T, self.Tks, self.ks), np.interp(T, self.Tkl, self.kl))

    def resistivity(self, T):
        # liquid: linear extrapolation of the last two table points beyond 2000 K
        r = np.interp(T, self.Tr, self.rr)
        slope = (self.rr[-1] - self.rr[-2]) / (self.Tr[-1] - self.Tr[-2])
        return np.where(T > self.Tr[-1], self.rr[-1] + slope * (T - self.Tr[-1]), r)

    def _A_drude(self, rho_e):
        nu = EPS0 * self.wp2 * rho_e
        eps = 1.0 - self.wp2 / (self.omega ** 2 + 1j * self.omega * nu)  # math (Drude)
        n = np.sqrt(eps)
        R = np.abs((n - 1.0) / (n + 1.0)) ** 2
        return 1.0 - R

    def absorptance(self, T):
        return self.A_RT + self._A_drude(self.resistivity(T)) - self.A_D_RT

    def psat(self, T):
        return self.p0 * np.exp(self.m_atom * self.Lv / K_B * (1.0 / self.Tb - 1.0 / T))

    def knight(self, Ts):
        kv = P.get("knight_M1")
        rho_sat = self.psat(Ts) * self.m_atom / (K_B * Ts)
        Tv = kv["Tv_over_Ts"] * Ts
        rhov = kv["rhov_over_rhos"] * rho_sat
        uv = np.sqrt(5.0 / 3.0 * K_B * Tv / self.m_atom)  # math (monatomic gamma)
        return rhov * uv, Tv, rhov, uv

    def h_table(self, T0, Tmax=12000.0, n=24000):  # num
        """Exact specific enthalpy h(T) − h(T0) [J/kg] with a sharp latent jump at Tm (for T(H) inversion)."""
        Tg = np.linspace(T0, Tmax, n)
        cp = self.cp(Tg)
        h = np.concatenate([[0.0], np.cumsum(0.5 * (cp[1:] + cp[:-1]) * np.diff(Tg))])  # math
        # sharp latent jump: duplicate the melting point with h and h+Lm
        i = np.searchsorted(Tg, self.Tm)
        Tg = np.concatenate([Tg[:i], [self.Tm, self.Tm], Tg[i:]])
        hm = np.interp(self.Tm, Tg[:i].tolist() + [self.Tm], h[:i].tolist() + [np.interp(self.Tm, np.linspace(T0, Tmax, n), h)])
        h = np.concatenate([h[:i], [hm, hm + self.Lm], h[i:] + self.Lm])
        return Tg, h

    @property
    def T_cap(self):
        return self.f_cap * self.Tc


@dataclass
class Laser:
    E: float
    D: float          # 1/e^2 diameter
    fwhm: float

    @property
    def w(self):
        return self.D / 2

    @property
    def t_peak(self):
        # t0 convention: P(0) = 10% of peak on the leading edge (registry t0_convention)
        return self.fwhm / 2 * np.sqrt(np.log(10.0) / np.log(2.0))  # math

    def power(self, t):
        s = self.fwhm / (2 * np.sqrt(2 * np.log(2.0)))  # math
        return self.E / (s * np.sqrt(2 * np.pi)) * np.exp(-(t - self.t_peak) ** 2 / (2 * s * s))  # math

    def intensity(self, r, t):
        return self.power(t) * 2 / (np.pi * self.w ** 2) * np.exp(-2 * r ** 2 / self.w ** 2)  # math


def depth_grid(dz0=10e-9, growth=1.03, depth=40e-6):  # num
    dz = [dz0]
    while sum(dz) < depth:
        dz.append(dz[-1] * growth)
    return np.asarray(dz)


def run(laser: Laser, mat: Material, r: np.ndarray, t_end=400e-9, dt=0.05e-9, shielding=None,  # num
        T0=None, n_inner=3):  # num
    """Integrate all rings. shielding: callable (r, t) -> transmission in [0,1] (Picard input) or None."""
    T0 = P.get("T_ambient") if T0 is None else T0
    dz = depth_grid()
    nz, nr = len(dz), len(r)
    zc_dist = 0.5 * (dz[:-1] + dz[1:])  # centre-to-centre distances
    T = np.full((nr, nz), T0)
    Tg, hg = mat.h_table(T0)
    H = np.zeros((nr, nz))  # volumetric enthalpy rho*(h - h(T0)), J/m^3
    nt = int(round(t_end / dt))
    out = {k: np.zeros((nt + 1, nr)) for k in ("Ts", "mdot", "Tv", "rhov", "uv", "I_inc", "q_abs", "q_overcap", "A")}
    E = {k: np.zeros(nr) for k in ("inc", "abs", "vap_latent", "overcap", "mass")}
    rho = mat.rho
    times = np.arange(nt + 1) * dt

    def record(n, Ts, I, tau, A, mdot, Tv, rv, uv, qo):
        out["Ts"][n], out["mdot"][n], out["Tv"][n], out["rhov"][n], out["uv"][n] = Ts, mdot, Tv, rv, uv
        out["I_inc"][n], out["q_abs"][n], out["q_overcap"][n], out["A"][n] = I, A * tau * I, qo, A

    for n in range(1, nt + 1):
        t = times[n]
        I = laser.intensity(r, t)
        tau = np.ones(nr) if shielding is None else np.clip(shielding(r, t), 0.0, 1.0)
        Told = T.copy()
        Tn = T.copy()
        for _ in range(n_inner):
            Ts = Tn[:, 0]
            A = mat.absorptance(Ts)
            mdot, Tv, rv, uv = mat.knight(Ts)
            q_net = A * tau * I - mdot * mat.Lv
            kk = mat.k(Tn)
            kf = 2 * kk[:, :-1] * kk[:, 1:] / (kk[:, :-1] + kk[:, 1:])  # math (harmonic face conductivity)
            cap = rho * mat.c_eff(Tn) * dz / dt
            for i in range(nr):
                ab = np.zeros((3, nz))
                g = kf[i] / zc_dist
                ab[1] = cap[i]
                ab[1, :-1] += g
                ab[1, 1:] += g
                ab[0, 1:] = -g
                ab[2, :-1] = -g
                rhs = cap[i] * Told[i]
                rhs[0] += q_net[i]
                Tn[i] = solve_banded((1, 1), ab, rhs)
        # enthalpy correction (Voller & Swaminathan): the implicit solve conserves Σ cap·ΔT exactly;
        # add that energy to H and recover T from the exact h(T) (latent heat cannot be skipped)
        H = H + cap * dt / dz * (Tn - Told)  # exactly the heat capacity used in the last solve
        Tn = np.interp(H / rho, hg, Tg)
        # surface cap: clamp the first cell and book its excess enthalpy
        qo = np.zeros(nr)
        Hcap = rho * np.interp(mat.T_cap, Tg, hg)
        over = H[:, 0] > Hcap
        if over.any():
            qo[over] = (H[over, 0] - Hcap) * dz[0] / dt
            H[over, 0] = Hcap
            Tn[over, 0] = mat.T_cap
        T = Tn
        # book exactly the fluxes used in the last implicit solve (A, mdot from the last inner iterate);
        # the vapour state reported to the gas phase is the same one
        record(n, T[:, 0], I, tau, A, mdot, Tv, rv, uv, qo)
        E["inc"] += I * dt
        E["abs"] += A * tau * I * dt
        E["vap_latent"] += mdot * mat.Lv * dt
        E["overcap"] += qo * dt
        E["mass"] += mdot * dt
    # stored energy per unit area (sensible + latent of fusion) for the conservation check
    TT = np.linspace(T0, 1.0, 2)  # placeholder to keep the namespace explicit
    del TT
    stored = (H * dz).sum(axis=1)
    out.update({"t": times, "r": r, "E_area": E, "stored_area": stored, "dz": dz, "T_final": T})
    return out


def _stored(Tprof, T0, mat, dz):
    """∫ρ (h(T) − h(T0)) dz with h from cp and the latent heat of fusion (exact enthalpy, not smeared)."""
    Tg = np.linspace(T0, max(Tprof.max(), T0) + 1.0, 4000)  # num
    cp = mat.cp(Tg)
    h = np.concatenate([[0.0], np.cumsum(0.5 * (cp[1:] + cp[:-1]) * np.diff(Tg))])  # math
    h = h + np.where(Tg >= mat.Tm, mat.Lm, 0.0)
    return float(np.sum(mat.rho * np.interp(Tprof, Tg, h) * dz))


def ring_area(r):
    """Annulus areas for ring centres r (first ring is a disc)."""
    edges = np.concatenate([[0.0], 0.5 * (r[1:] + r[:-1]), [r[-1] + 0.5 * (r[-1] - r[-2])]])  # math
    return np.pi * (edges[1:] ** 2 - edges[:-1] ** 2)
