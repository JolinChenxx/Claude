"""Target heating and evaporation source (W4, PLAN v3 §3.2/§4.1) — v0 one-way coupling.

Per radial ring (no lateral conduction; 11 µm spot vs ~1 µm thermal length in 100 ns):
  ρ c_eff(T) ∂T/∂t = ∂/∂z (k(T) ∂T/∂z),   z ≥ 0 into the target, adiabatic at the bottom
  −k ∂T/∂z|_s = A(T_s) · τ_sh(r,t) · I(r,t) − ṁ(T_s) · L_v
Melting by apparent heat capacity over ΔT_m (N-grade); constant solid density for the Lagrangian grid
and no surface recession in v0 (ablated depth reported). T_s capped at f_Tcap·T_c; the energy that
would have raised T_s above the cap is booked as "over-cap" (U4 trigger, PLAN §3.6).
Evaporation: Clausius–Clapeyron p_sat through (T_boil, 1 atm) with L_v; Knight sonic Knudsen layer
(M = 1): T_v = 0.669 T_s, ρ_v = 0.308 ρ_sat, u_v = sqrt(5/3 k T_v / m), ṁ = ρ_v u_v (v0 uses the sonic
relation at all p_sat — an upper bound at low p_sat where ṁ is negligible). v1k: with a wall-pressure history
p_w(t, r) from the previous Picard iteration, the Knight layer is subsonic when p_w > 0.206 p_sat and ṁ = 0 when
p_w ≥ p_sat (recondensation not modelled: declared bound); `knight_bp` below, same relations as the solver.
Absorptance A(T): A_RT (optical constants) + Drude change driven by the measured resistivity,
A(T) = A_RT + A_D(ρ_e(T)) − A_D(ρ_e(293 K)), A_D from Fresnel with ε = 1 − ω_p²/(ω² + iων), ν = ε0 ω_p² ρ_e.
"""
from __future__ import annotations

from pathlib import Path
from dataclasses import dataclass, field

import numpy as np
from scipy.linalg import solve_banded
from scipy.special import erfcx

from lipsim import params as P

K_B = P.get("k_B")
AMU = P.get("amu")
EPS0 = P.get("eps0")
E_CH = P.get("e")
M_E = P.get("m_e")
C = P.get("c")


GAMMA_V = 5.0 / 3.0  # math (monatomic vapour)


def knight_M(M):
    """Knight (AIAA J. 17, 519, 1979) jump at exit Mach M: (T_v/T_s, rho_v/rho_sat), gamma = 5/3."""
    m = np.asarray(M, float) * np.sqrt(GAMMA_V / 2)  # math
    b = (GAMMA_V - 1) / (GAMMA_V + 1) * m / 2  # math
    tr = (np.sqrt(1 + np.pi * b * b) - np.sqrt(np.pi) * b) ** 2  # math
    E = erfcx(m)
    rr = np.sqrt(1 / tr) * ((m * m + 0.5) * E - m / np.sqrt(np.pi)) + 0.5 / tr * (1 - np.sqrt(np.pi) * m * E)  # math
    return tr, rr


def knight_bp(pr):
    """Exit Mach M, T_v/T_s, rho_v/rho_sat for back-pressure ratio pr = p_w/p_sat (array). Sonic for
    pr <= p_v(1)/p_sat, M = 0 for pr >= 1, bisection in between (p_v/p_sat falls monotonically with M)."""
    pr = np.asarray(pr, float)
    lo, hi = np.zeros_like(pr), np.ones_like(pr)
    for _ in range(60):  # num (bisection to machine precision)
        M = 0.5 * (lo + hi)
        tr, rr = knight_M(M)
        up = rr * tr > pr
        lo, hi = np.where(up, M, lo), np.where(up, hi, M)
    M = 0.5 * (lo + hi)
    tr1, rr1 = knight_M(1.0)
    M = np.where(pr <= rr1 * tr1, 1.0, np.where(pr >= 1.0, 0.0, M))
    tr, rr = knight_M(M)
    return M, tr, rr


def _tab(key, col):
    v = P.get(key)
    return np.asarray(v["T"], float), np.asarray(v[col], float)


REPO_ROOT = Path(__file__).resolve().parents[3]


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

    def knight(self, Ts, pw=None):
        """Sonic Knight state (pw None), or v1k with back pressure pw [Pa]: returns mdot, Tv, rhov, uv."""
        kv = P.get("knight_M1")
        rho_sat = self.psat(Ts) * self.m_atom / (K_B * Ts)
        if pw is None:
            Tv = kv["Tv_over_Ts"] * Ts
            rhov = kv["rhov_over_rhos"] * rho_sat
            uv = np.sqrt(5.0 / 3.0 * K_B * Tv / self.m_atom)  # math (monatomic gamma)
            return rhov * uv, Tv, rhov, uv
        M, tr, rr = knight_bp(np.asarray(pw, float) / self.psat(Ts))
        Tv, rhov = tr * Ts, rr * rho_sat
        uv = M * np.sqrt(GAMMA_V * K_B * Tv / self.m_atom)  # math
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
    waveform: str | None = None   # optional measured temporal shape (CSV t_ns,P_norm); scenario only (PREREG)

    def __post_init__(self):
        self._wf = None
        if self.waveform:
            import pandas as pd
            w = pd.read_csv(REPO_ROOT / self.waveform if not Path(self.waveform).is_absolute() else self.waveform)
            t, p = w.t_ns.to_numpy() * 1e-9, np.clip(w.P_norm.to_numpy(), 0.0, None)  # math (ns -> s)
            i10 = int(np.argmax(p >= 0.1 * p.max()))
            # t0 convention: shift so that the first 10%-of-peak crossing on the leading edge is t = 0
            t10 = t[i10 - 1] + (0.1 * p.max() - p[i10 - 1]) / (p[i10] - p[i10 - 1]) * (t[i10] - t[i10 - 1]) if i10 > 0 else t[0]
            t = t - t10
            self._wf = (t, p / np.trapezoid(p, t))   # normalised to unit time integral (1/s)

    @property
    def w(self):
        return self.D / 2

    @property
    def t_peak(self):
        if getattr(self, "_wf", None) is not None:
            t, p = self._wf
            return float(t[int(np.argmax(p))])
        # t0 convention: P(0) = 10% of peak on the leading edge (registry t0_convention)
        return self.fwhm / 2 * np.sqrt(np.log(10.0) / np.log(2.0))  # math

    def power(self, t):
        if getattr(self, "_wf", None) is not None:
            tw, pw = self._wf
            return self.E * np.interp(t, tw, pw, left=0.0, right=0.0)
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
        T0=None, n_inner=3, phase_explosion=False, wall_pressure=None):  # num
    """Integrate all rings. shielding: callable (r, t) -> transmission in [0,1] (Picard input) or None.
    wall_pressure: callable (r, t) -> gas pressure of the wall cell [Pa] (v1k Picard input) or None (sonic).
    The recorded vapour state (Tv, rhov, uv, mdot) stays the SONIC state at T_s: the solver applies the back
    pressure with its current wall-cell pressure. Latent cooling and the vaporised mass use the back-pressure ṁ."""
    T0 = P.get("T_ambient") if T0 is None else T0
    dz = depth_grid()
    nz, nr = len(dz), len(r)
    zc_dist = 0.5 * (dz[:-1] + dz[1:])  # centre-to-centre distances
    T = np.full((nr, nz), T0)
    Tg, hg = mat.h_table(T0)
    H = np.zeros((nr, nz))  # volumetric enthalpy rho*(h - h(T0)), J/m^3
    nt = int(round(t_end / dt))
    out = {k: np.zeros((nt + 1, nr)) for k in ("Ts", "mdot", "Tv", "rhov", "uv", "I_inc", "q_abs", "q_overcap", "A", "mdot_bp")}
    E = {k: np.zeros(nr) for k in ("inc", "abs", "vap_latent", "overcap", "mass", "pe_mass")}
    rho = mat.rho
    times = np.arange(nt + 1) * dt
    zface = np.concatenate([[0.0], np.cumsum(dz)])  # cell faces below the surface
    melt_max = np.zeros(nr)  # v1r: deepest liquid layer (contiguous T >= T_m from the surface) over the run, m

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
            mdot_bp = mdot if wall_pressure is None else mat.knight(Ts, np.maximum(wall_pressure(r, t), 0.0))[0]
            q_net = A * tau * I - mdot_bp * mat.Lv
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
        liq = T >= mat.Tm
        nliq = np.where(liq.all(axis=1), nz, np.argmin(liq, axis=1))  # contiguous liquid cells from the surface
        melt_max = np.maximum(melt_max, zface[nliq])
        mpe = np.zeros(nr)
        if phase_explosion and over.any():
            # registry phase_explosion_model: the over-cap enthalpy flux is not discarded. It drives explosive
            # boiling of the superheated surface layer and vaporises mass at T_cap, m_pe = q_over / L_v
            # (energy-limited; the liquid-droplet fraction is neglected, so this is the vapour upper bound).
            # The vapour leaves with the Knight state at T_cap.
            mpe = qo / mat.Lv
            mdot = mdot + mpe
            Tv_c, rv_c, uv_c = mat.knight(np.full(nr, mat.T_cap))[1:]
            sel = mpe > 0
            Tv, rv, uv = np.where(sel, Tv_c, Tv), np.where(sel, rv_c, rv), np.where(sel, uv_c, uv)
            E["pe_mass"] += mpe * dt
            E["vap_latent"] += qo * dt
            qo = np.zeros(nr)
        # book exactly the fluxes used in the last implicit solve (A, mdot from the last inner iterate);
        # the vapour state reported to the gas phase is the same one
        record(n, T[:, 0], I, tau, A, mdot, Tv, rv, uv, qo)
        E["inc"] += I * dt
        E["abs"] += A * tau * I * dt
        E["vap_latent"] += mdot_bp * mat.Lv * dt   # Knight evaporation (v1k: with back pressure); the phase-explosion part is booked above
        E["overcap"] += qo * dt
        E["mass"] += (mdot_bp + mpe) * dt
        out["mdot_bp"][n] = mdot_bp
    # stored energy per unit area (sensible + latent of fusion) for the conservation check
    TT = np.linspace(T0, 1.0, 2)  # placeholder to keep the namespace explicit
    del TT
    stored = (H * dz).sum(axis=1)
    out.update({"t": times, "r": r, "E_area": E, "stored_area": stored, "dz": dz, "T_final": T, "melt_depth_max": melt_max})
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
