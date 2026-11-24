# PREREG_v0 — pre-registration of the v0 quick baseline (non-frozen, PLAN v3 §1.8)

Status: **committed before any model–experiment comparison of the v0 batch (T011).** Git timestamp is the evidence.
v0 is a *non-frozen quick baseline*; its results are all reported, and the v1 upgrade list below is fixed now.

## 1. Model (as implemented at the commit of this file)
- Gas: PLUTO 4.4-patch4 core unmodified (HLLC, linear reconstruction, van Leer limiter, RK2, CFL 0.3), 2D axisymmetric r–z,
  Euler + Cu mass-fraction tracer. Replaced upstream files: only those in `pluto/overrides/` (with diffs).
- EOS: LTE Cu–air tables (`src/lipsim/eos/mixture.py`): Cu I–V, N/O I–IV, Ar I–III levels from NIST (T007), N2/O2/NO RRHO,
  Debye–Hückel IP lowering with the screening length bounded below by the ion-sphere radius. Tables `configs/tables/v0.yaml`.
- Laser: Gaussian pulse (FWHM 60 ns, t0 at 10% of peak), Gaussian beam, 1/e² diameter 22 / 31 µm, energy 0.4 / 0.32 mJ,
  collimated column marching with inverse bremsstrahlung (e–i Kramers–Unsöld ff+bf, e–n Drude) + specular return of
  the part not absorbed by the target (1 − A(T_s)).
- Target: 1D conduction per ring with enthalpy correction, A(T) from RT optical constants + Drude/Matula resistivity,
  Clausius–Clapeyron + Knight sonic layer, T_s ≤ 0.9 T_c. One-way coupling with Picard iterations it0–it2 (relaxation 0.5).
- Radiation: Q = 4π NEC, two parameter-free limits run side by side: `thin` (all NIST lines with A + continuum) and `trap`
  (resonance lines removed). Neither is the baseline.
- Imaging: NIST in-band lines (Boltzmann from the tables) with Doppler self-absorption and Kramers–Unsöld continuum;
  rectangular filters a 515±5 / b 510±12 / c 500±20 nm; gate [t, t+10 ns]; defocus disk 0 / 250 / 500 µm; one gain scalar per
  series × scenario at t_ref = 300 ns (secondary metrics only).

## 2. Declared batch (T011, `configs/batches/T011.yaml`)
G0 Picard chains it0→it2: {high, low} × {thin, trap}; G1 it0: high × {thin, trap}. All 14 PLUTO runs are reported.
Branch B (semi-empirical fallback): **not run** — the cloud smoke run (G=smoke, high fluence) already ignites
self-consistently (gas absorption 135 µJ by 60 ns). If the T011 high-fluence G0 runs do *not* meet ignition gate G1
(§4.3: gas absorption ≥ 5% of the pulse by 150 ns and T_max ≥ 1.5×10⁴ K), branch B is added as a labelled variant.

## 3. Metrics and pass criteria
As PLAN v3 §8 with DECISIONS 2026-11-15 (trend first): P1 τ_L (300–630 ns), P2 Δt10, P3 growth→shrink turning time,
P4 shock trajectory, P5 centroid rise, P6 fluence ordering. Frames before 60 ns: reported, not judged.
Experimental values: `data/exp/meta.yaml` (P1: high 182 ns, low 218 ns; P2: 433 / 542 ns).

## 4. Expectations stated in advance
- v0 has no molecular transport and no wall heat loss → late emission may still decay **too slowly** (τ_L too long).
- `thin` NEC cools faster than `trap`; the experimental τ_L is expected between or near them if radiation dominates.
- P5 (centroid rise) likely fails (no spatter in the model).
- NIST Cu I has only 37 A-values → `thin` NEC is a lower bound on Cu line cooling.

## 5. v1 upgrade list (fixed now; added in D1–D4 regardless of v0 agreement)
Molecular transport (conduction incl. reactive, viscosity); wall heat exchange; two-way target coupling with back pressure
and recondensation; Kurucz line list for Cu I/II NEC; Voigt line shapes with Stark widths; V3/V5/V8b/V13 verification cases.

## v1 extension declared 2026-11-19 13:30 (UTC+8), before any hydro result: energy-limited phase explosion
- Motivation: (i) v0 discarded the over-cap enthalpy flux (23 µJ at high fluence, it0), so the target model did not conserve energy; (ii) the core-energy diagnostic (STATE 08:35) and Gojani 2008 crater depths point to an under-predicted vapour mass.
- Model (registry phase_explosion_model): m_pe = q_over/L_v at T_cap, injected with the Knight state at T_cap. This is an upper bound on the vapour, because droplets are not modelled. The v1e results (pe off) are the lower bound, and both bracket the vapour fraction.
- Target-model effect, unshielded: high fluence 3.2 → 8.1 ng, low fluence 1.0 → 1.6 ng; the energy budget closes exactly.
- Expectation, declared now: a lower specific energy of the emitting core at t_ref and a faster late decay at high fluence. This is reported whatever the outcome.

### Outcome of the 2026-11-19 v1 extension (recorded 2026-11-20 03:00, after the results)
As specified, the extension injects up to 21× the choked Knight-layer flux ρ_v u_v(T_cap), which is inconsistent with its own sonic boundary condition (STATE 2026-11-20 03:00). Read consistently, with vapour capped at the choked flux and the excess as droplets, it reduces to PE off for the gas. Its declared expectations are therefore untestable, and its results are withdrawn. No replacement model is fitted to the data.

## Diagnostic declared 2026-11-20 06:50 (UTC+8), before the data: is LTE ionisation balance valid in the emitting core?
- Question: the model's Cu I line emission stays flat at high fluence because LTE Saha balance turns Cu II into Cu I as the core cools (STATE 2026-11-20 06:00). In an expanding plasma, recombination can lag (frozen ionisation), which would leave fewer Cu I atoms and a faster line decay.
- Test (T020): emission-weighted n_e and T of the band emission per frame (scripts/diag_emission.py, existing). Three-body recombination time τ_3br = 1/(α_3 n_e²), α_3 = 8.75e-39 T_e[eV]^-4.5 m⁶ s⁻¹ (NRL Plasma Formulary). Flow time t_fl = t (time since the pulse). McWhirter bound n_e ≥ 1.6e18 √T[K] ΔE[eV]³ m⁻³, with ΔE = 3.82 eV (Cu I 510.6 nm upper level).
- Declared reading: if τ_3br > 0.1 t_fl at the high-fluence frames ≥ 400 ns, LTE ionisation balance is not justified there, and the next step is a separately pre-registered time-dependent ionisation post-processing. If τ_3br ≪ 0.1 t_fl at all frames, LTE stands and the plume cooling and dilution in the hydro is the remaining suspect. No emissivity change and no parameter choice follow from this card.

## v1j extension declared 2026-11-24 08:20 (UTC+8), before any hydro result: droplet evaporation bounds (user choice "1", 2026-11-24)
- Motivation: the high-fluence emitting core stays 16–25 kK (T020), because its specific energy is high (≈ 44 MJ/kg, ≈ 1 ng of Cu). Explosive boiling ejects mostly condensed droplets (STATE 2026-11-20 03:00), which the model has none of. If the droplets evaporate in the plume, they take latent heat from the gas and add Cu atoms.
- Bracket, no free parameter fitted:
  - **Lower bound:** no droplets (the existing v1i/v1e results).
  - **Upper bound (v1j, case v1dr_*):** droplet mass m_d = removal − vapour. Removal = cu_removal_efficiency × E_pulse × ρ_solid, with 2000 µm³/mJ from Semerok 2001 (grade C, order of magnitude; other λ/τ/spot). Vapour = this iteration's target-model vaporised mass. This gives m_d ≈ 6.4 ng (high) and 5.2 ng (low). All of m_d evaporates instantly inside the Cu core (Y > 0.5), in proportion to ρY, uniformly in time from the end of the pulse (t_peak + FWHM/2 = 85 ns) to t_ref = 300 ns. **Window revised before any production run:** the first draft used the pulse FWHM (25–85 ns). A cloud smoke test (smoke grid, 0–300 ns, no comparison with experiment) showed that dumping ~6 ng into the ~0.05 ng early vapour during the pulse suppresses breakdown (gas absorption at 60 ns 0.07 µJ vs 156 µJ) and floor-clamps the cold cells. That confounds ignition with the late-cooling question this test asks. Explosive-boiling droplets are slow (10–100 m/s) and reach the plume after the pulse anyway. The droplets arrive at rest with e_vap(ρ, T_cap, Y=1) − L_v(T_b), so the gas pays L_v. L_v at T_b overstates the latent heat near T_cap, which keeps this an upper bound on the cooling.
- Runs: high G0 Picard it0–it2 (thin) and low G0 it0–it1 (thin), v1j build, tables v1c, image_v2, core_energy, shock_track, diag_emission. Picard as before.
- **Declared expectations, read whatever the outcome:**
  - (a) Lower specific energy and T_emw of the core at t_ref = 300 ns than the lower bound, at both fluences.
  - (b) A faster late decay: L(700)/L(300) at high fluence below the lower bound's 0.66 (experiment 0.13).
  - (c) Shock radius at 500–700 ns within 10% of the lower bound, because the energy sink is ≲ 30 µJ of ~260 µJ absorbed.
- Reading: if (b) brings high fluence within a factor 2 of the experiment, condensed-phase loading is a sufficient mechanism within the bracket. If not, even the upper bound cannot explain the slow decay, and the remaining explanation is outside this 2-D LTE model (mixing/turbulence). That would be reported as a model limitation. No intermediate droplet fraction is fitted.

## v1k extension declared 2026-11-24 (UTC+8), before any v1k hydro result: Knight evaporation with back pressure; plume lift metric
- **Model change (pre-declared v1 item, §5 above and PLAN v3 §3.2):** the Knudsen-layer jump uses Knight's relations at the exit Mach number M set by the gas pressure of the wall cell, p_w: sonic (M = 1) for p_w ≤ 0.206 p_sat(T_s), subsonic for 0.206 p_sat < p_w < p_sat, and ṁ = 0 for p_w ≥ p_sat. No new parameter (kinetic theory, γ = 5/3). The solver applies it with the current p_w each step; the target model uses p_w(t, r) of the previous Picard iteration for the latent cooling (it0: sonic, as before). Recondensation is not modelled: this keeps the vapour an upper bound. Build v1k, user parameter BACKP; BACKP = 0 must be bit-identical to v1j (droplets off). Cases v1bp_high / v1bp_low.
- **Why now:** a model-internal pathology, found in the T018c 62 ns field extract (high, it2; thin and trap): the wall cell on the beam (r < 10 µm) holds 6–7×10³ kg/m³ of Cu at 7–11 kK, under 72–89 kK gas at ~14 kbar, and > 90% of the absorbed laser power is deposited within 2 µm of the face. A sonic Knight source feeding a cell whose pressure is 10–100× p_sat is inconsistent with the Knight relation itself. The prompt for looking was the shape comparison with S4 (user, 2026-11-24); the fix is the pre-declared model item, not chosen to match the frames.
- **Surface row of the S4 crops, from the frames alone (scripts/exp_surface_rows.py, results/surface/):** the lower emission edge stays fixed while the plume grows upward (high: 10% edge at row 71.0 ± 1.2 over 150–740 ns; low: 50.8 ± 3.1), so it is the occluding face. Bracket for the face row: [lower50, lower10] (blurred step; intensity at the face ≤ peak). Paper Fig. 2 to be checked on the workstation (network policy blocks the publisher here); if it gives a surface position outside the bracket, that is reported and the metric below is recomputed with it.
- **New shape metric S1 (descriptive, gain-free), declared now:** emission-peak lift above the face. Experiment per frame: lift_min = (lower50 − peak)·px, lift_max = (lower10 − peak)·px. Model: band-b image, thin and sa, baseline defocus, face at the bottom edge (scripts/peak_lift.py). Pass if the model lift ≥ lift_min in ≥ 70% of the frames 150–630 ns. Current v1j-equivalent baseline (T016e): 0/20 (high, median 91 vs 178 µm), 0/11 (low, 60 vs 101 µm).
- **Declared expectations, read whatever the outcome:**
  - (a) Model-internal: the wall-cell density on the beam at 40–80 ns stays below 500 kg/m³, and the absorbed laser power in z < 5 µm at the pulse peak falls below 50% of the column total (T018c: > 90%).
  - (b) S1 lift rises above the baseline at both fluences (direction only; no size promised).
  - (c) Report as before: L(700)/L(300) (high), shock radius vs Fig. 3d, core specific energy and T_emw at t_ref, ConsToPrim count.
- **Reading:** if (a) holds but S1 stays < 50% of lift_min, the face-pinned emission is not caused by the source layer alone; the next pre-declared candidates are the converging beam ("会聚射线管", PLAN §3.2 sensitivity) and the 3-D end-face effect (PLAN §3.3), each declared separately before it is run. If (a) fails, the near-wall resolution is the suspect (G1 member in T023). Nothing is tuned to S1.

## v1m extension declared 2026-11-24 ~15:40 (UTC+8), before any v1m hydro result: two-temperature avalanche breakdown (user choice)
- **Why:** STATE 2026-11-24 15:00. The model diverges during the pulse: it ignites late (~30 ns) and at the face, while the experiment shows an early, elevated emitting core and an early shock about 20 ns ahead. The 0-D prototype (fresh Knight vapour, 68 kg/m³, 4.7 kK, 1.7 GW/cm²) does not ignite in 30 ns under single-T LTE (α = 2.8 m⁻¹), but ignites in about 3 ns by Cu electron avalanche (T_e ≈ 1.2 eV). The user chose this upgrade (2026-11-24) over radiative transfer. It is outside PLAN v3 §3.4, which is recorded as a scope change by the user.
- **Model (build v1m, BD_ON, cases v1bd_*):** while the pulse is on (t < t_peak + 1.5 FWHM), the Cu cells (Y > 1e-3) carry non-equilibrium electrons n_e,neq ≥ n_e,LTE (tracer 2).
  - Quasi-steady T_e: inverse-bremsstrahlung power per electron = elastic loss 3(m_e/M)ν_m·(3/2)k(T_e − T_h) + ionisation power k_i n_0 χ.
  - Electron–neutral momentum-transfer cross-section σ_en_Cu = 1e-19 m², registry grade C.
  - Lotz electron-impact ionisation of Cu I: registry lotz_a_cu, grade B.
  - Three-body recombination by detailed balance with Saha(T_e). Cu I partition function from the 2S + 2D levels; Cu II ground state only.
  - The 1064 nm absorption is max(α_LTE, α(n_e,neq, T_e)).
  - Ionisation above LTE holds χ per electron outside the LTE EOS; the transfer is energy-conserving and booked as E_bd_store.
  - After the pulse the store relaxes to LTE.
  - Declared simplifications: only Cu avalanches (air ionisation at 12–15 eV is slower and ignored); electron thermal energy above T_h is neglected in the EOS; a quasi-steady T_e; the local intensity is lagged one step.
- **Sensitivity, not tuning:** σ_en_Cu at the registry bounds (3e-20, 3e-19) for high it0 only, reported whatever the outcome.
- **Declared expectations, read whatever the outcome:**
  - (a) Gas-absorption onset earlier than v1k. Onset = E_gas_abs reaching 1 µJ; prototype estimate 5–10 ns earlier, bounded by the vapour appearing at about 20 ns.
  - (b) Shock radius at 25–70 ns closer to Fig. 3d than v1k. Direction only.
  - (c) S1 lift and the second-moment height σ_z at 150–630 ns: report the change from v1k. No pass expected from (a) alone.
  - (d) Report L(700)/L(300) (high), T_emw and core specific energy at t_ref, and the late shock.
- **Reading:**
  - If (a) and (b) hold but S1 and σ_z do not move, ignition timing is not what sets the morphology, and radiative transfer (the user's alternative) is next.
  - If (a) fails (no earlier onset), the 2-D ignition is limited by vapour supply, not by electron kinetics.

### Outcome readings recorded 2026-11-24 ~16:10 (after the results)
- **v1j droplet upper bound (T021):**
  - High it2: T_emw at 300/500/700 ns is 8.7/7.6/7.1 kK against 25.3/19.9/16.9 kK, and the core specific energy is 4.5 against ≈ 44 MJ/kg. (a) holds.
  - L(730)/L(300) (sa) is 0.155 against 0.636; the experiment is 0.107. (b) holds: within a factor 2, a sufficient mechanism inside the bracket.
  - Axial shock at 500–700 ns: ±0.0% against the lower bound. (c) holds.
  - S1 (sa): median 177 µm. High: 7/20 frames (fails the 70% rule). Low it1: 8/11 frames (passes).
  - Low it1 L(1035)/L(300) is 0.193 against 0.113 for the lower bound (experiment 0.027). The late decay at low fluence gets slower; see the low-it1 energy-floor note.
  - σ_z at 300 ns: 79 µm (high), 65 µm (low), against 151 / 94 µm in the experiment. Still small.
- **v1m two-temperature breakdown (T024 high it0, early fields):**
  - Onset (1 µJ) at 27.5 ns against 29.0 ns: (a) holds, but small.
  - From 35 ns on, E_gas_abs, E_kin, E_wall and the near-wall fields match v1k within 2%. (b) is not met; the early shock is unchanged.
  - Reading: ignition timing does not set the morphology. T024 is stopped as uninformative; the data is kept.

## Combination declared 2026-11-24 ~16:10 (UTC+8), before any result: v1k back pressure + v1j droplet upper bound (cases v1bpdr_*)
- Both components were pre-declared separately. This is their joint run; no new parameter.
- Expectations, read whatever the outcome:
  - (a) High L(730)/L(300) within a factor 2 of the experiment, as T021.
  - (b) S1: report the frame fractions, high and low.
  - (c) σ_z: report it; no pass expected.
  - (d) Shock at 500–700 ns within 10% of T023 v1k.
- Low-fluence decay: report it against T021 low. If the low-it1 energy-floor clamp recurs, report it; no fix mid-test.
