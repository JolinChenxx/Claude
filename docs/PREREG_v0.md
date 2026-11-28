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

## v1n correction declared 2026-11-24 ~17:15 (UTC+8), before any v1n result: energy-limited droplet release
- **Why:** the v1j release is time-uniform, so it does not check whether the gas can pay L_v. At low fluence the core is too cold, and the floor clamp adds energy: T021 low it1 +1.74 µJ, T025 low it0 +1.14 µJ, both inside the window. The bound then holds more cooling than the energy allows, and it is not energy-consistent.
- **Model (DROP_TB > 0, cases v1bpde_*):** in each cell, the droplet mass released in a step evaporates only as far as the mixture stays at or above T_boil (registry cu_T_boil, 2835 K, grade C). It arrives at rest with e_vap(T_cap) − L_v, as in v1j. The rest stays condensed: it is removed from the bound and booked as M_drop_condensed. No new parameter. With DROP_TB = 0 the results are bit-identical to v1j/v1k.
- **Declared expectations, read whatever the outcome:**
  - (a) The droplet-window energy residual at low fluence is < 0.2 µJ, against 1.1–1.7 µJ.
  - (b) Low fluence: report L(1035)/L(300) and M_drop_condensed. The direction is not predicted. Less cooling could slow the decay, while the absence of the spurious floor energy could speed it up.
  - (c) High fluence: L(730)/L(300) within a factor 2 of the experiment is expected to survive, because the high core can pay.
  - (d) S1 and σ_z: report them.
- This is a consistency correction of the v1j bound. It is not chosen from the comparison: the trigger was the closure residual.

## v1p correction declared 2026-11-24 ~20:25 (UTC+8), before any v1p result: vapour exit momentum (user choice "先做第1个", with the instruction: no tuning, physics only)
- **Physics:** the vapour leaves the Knudsen layer with the Knight exit velocity u_v = M·c_v, normal to the face. M is set by the back pressure (v1k), and c_v = √(γkT_v/m) with γ = 5/3. Its momentum flux ṁu_v has been left out since the 11-17 engineering correction, which injected only mass and stagnation enthalpy. That correction was made under the sonic, uncapped source; the ConsToPrim repairs then arose with the PE source at 21× the choked flux. With back pressure the flux is choked or subsonic. v1p restores the dm·u_v momentum in the wall cell. The energy is unchanged, since h_v already contains u_v²/2. No new parameter. VMOM = 0 is bit-identical to v1n. Cases v1bpdem_* = v1bpde_* + vapour_momentum.
- **Motivation (descriptive, from the comparison):** the experimental emission aspect ratio σ_z/σ_x is ≈ 1.07, against ≈ 0.63 in the model. Blur cannot explain both widths. This motivated looking at the missing forward momentum, which is a known omission and not a fitted quantity.
- **Declared expectations, read whatever the outcome:**
  - (a) Momentum injected (P_vap_mom) is reported. ConsToPrim count and closure no worse than v1n (T026).
  - (b) σ_z/σ_x at 300 ns: report the change from T026. The direction is expected up, but the size is not predicted and may be negligible, because ṁu_v ~ p_v is small against the plume pressure.
  - (c) S1, L(730)/L(300) (high), L(715)/L(300) (low), shock at 500–700 ns: report them.
- **Reading:** if (b) changes by < 5%, vapour momentum is not the missing elongation mechanism. The next candidate is then droplet kinematics, with the measured 344 m/s as an independent input (declared separately), then radiative transfer.

### Reading recorded 2026-11-24 23:40 (after the results): T026 v1n high it1
- L(730)/L(300) (sa) is 0.146 against 0.107 in the experiment; T021 gave 0.155. The energy-consistent bound keeps the high-fluence decay within a factor 2. PREREG v1n (c) holds.
- S1 median 166 µm, 1/20 frames. σ_z/σ_x at 300 ns: 77/117 µm.

## v1q extension declared 2026-11-24 ~23:45 (UTC+8), before any v1q result: droplets evaporate at the measured spatter top
- **Source (an independent measurement, not the S4 comparison data):** the paper's pump-probe shadowgraphy gives the rise speed of the molten-copper spatter top as 344 m/s (high) and ≈190 m/s (low). At high fluence the height is stable after 500 ns. The authors find the bright high-temperature plume centre coincides with the spatter top and interpret it as evaporation on entry. Verbatim quotes are in data/exp/paper/droplets_notes.md; registry cu_spatter_speed_high/low and cu_spatter_plateau_high, grade B.
- **Model (DROP_VD, DROP_TPL; cases v1bpdk_* = v1bpde_* + spatter kinematics):** the v1n droplet mass and window are unchanged: launch at t_peak + FWHM/2 from the face, release until t_ref, energy-limited. The release is restricted to the grid row that holds the spatter top z_d = v_s (t − t_launch), which stops at the plateau time where one is measured. Within that row the mass is spread over Cu-core cells (Y > 0.5) in proportion to ρY. No free parameter. The cell-row placement is the spatial discretisation. The window is kept so that only the location changes against v1n.
- **Caveat, declared:** the spatter-top speed is an edge speed, not a droplet speed distribution. Placing all droplets at the front is the extreme of "evaporation on entry to the hot core". The opposite extreme is v1n, release throughout the core. The two bracket the location.
- **Plan change, recorded:** the overnight rule ran v1q only after T027 (b) failed. It runs now in parallel because 16 cores are idle. It is an independent mechanism, and its reading does not depend on T027.
- **Declared expectations, read whatever the outcome:**
  - (a) Closure and the droplet-window residual as in v1n (< 0.2 µJ).
  - (b) σ_z/σ_x and S1 against T026: the direction is expected up, the size is not predicted.
  - (c) L ratios and shock: report them.
- **Reading:** if σ_z/σ_x moves by < 5%, the location of the droplet sink does not set the elongation either. The remaining candidate is then radiative transfer (absorption front up the beam), which is declared separately and decided by the user.

## v1r extension declared 2026-11-25 ~09:25 (UTC+8), before any v1r hydro result: droplet location from flight time and evaporation time (user decision 2026-11-25 "先做液滴空间分布的折中，按飞行时间和蒸发时间定")
- **Model (DROP_A0, DROP_RL; cases v1r_* and the scan v1r{10,30,100}n_*):** the v1n droplet mass, launch window and energetics are unchanged (launch from the face at a constant rate from t_peak + FWHM/2 to t_ref; vapour enters with e_vap(T_cap) − L_v; mixture energy-limited at T_boil). Each 1-ns launch bin is one parcel of equal droplets of radius a0. It flies drag-free at the measured spatter speed (Stokes time ≫ 1 µs). In the grid row it is in, the droplets are spread over the Cu-core cells (Y > 0.5) in proportion to ρY, as in v1n/v1q. Each droplet is held at T_boil and loses mass at Q/L_v. Q = 4πa²q, with 1/q = 1/q_FM + a/K. q_FM = ¼ ρ v̄_heavy [ε(T) − ε(T_b)], with ε = e + p/(2ρ) and accommodation 1 (an upper bound). K = ∫_{T_b}^{T} κ dT (continuum, Nu = 2, opt.bin κ). Mass the gas cannot pay for (the T_boil limit) stays on the droplets. Droplets leaving the domain or surviving to the end are booked airborne (budget M_drop_airborne; droplets.csv). Registry: droplet_heat_model, droplet_size_model, rp_drop_over_jet.
- **Droplet size, the one physical input that is not measured:** a0 = 0.945 × h_melt. This is Rayleigh–Plateau breakup of a crown rim whose diameter equals the area-mean maximum melt depth of the target model of the same Picard iteration. No size is taken from the plume images. Unshielded high-fluence target model: h_melt = 4.4 µm (axis 6.9 µm), so a0 ≈ 4 µm. The shielded value is computed per run.
- **Sensitivity scan, declared now and reported whatever the outcome:** fixed radii of 10, 30 and 100 nm. These are not fits. The reported v1r member is v1r_* (melt_rp). No scan member will be adopted because it matches the experiment better. A scan member that matches better would only show which droplet size the observed decay needs; that size is a prediction to test against the deposited-NP sizes (to be extracted from the paper by Codex).
- **A-priori estimate (scripts/droplet_evap_estimate.py, results/droplets/evap_estimate.csv; core states of T023/T026):**
  - Full evaporation takes 0.1–1 µs for a = 10 nm (high, 100–200 ns), about 10–500 µs for a = 1 µm, and longer for a = 4 µm.
  - The integral ∫q dt over 100–500 ns gives an evaporated fraction of ≈ 3×10⁻⁸ m / a.
- **Expectations, declared:**
  - (a) Mass closure: M_launched = M_evaporated + M_airborne to 1e-6. Energy closure as in v1n.
  - (b) For the melt_rp member: evaporated droplet mass ≲ 2% of the launched mass, so L(t)/L(300) close to T023 (v1k, no droplets), not to T026. If this holds, the v1n fast decay is physical only if the ejecta are ≲ 30 nm fragments. Report that plainly; do not adopt v1n as the physical model on the decay alone.
  - (c) Scan: the evaporated fraction falls with a. For 10 nm the decay is expected between T026 and T029. Report σz/σx, S1 and L ratios for every member.
  - (d) Independent check against the measured spatter top: the model's highest parcel that still holds more than half its mass, against the measured rise (344 m/s) and the high-fluence plateau after 500 ns. A plateau that the model does not produce is reported as a mismatch, not adjusted.
- **Runs:** one Picard iteration seeded with the T026 shielding of the same fluence (high: T026 it1 → v1r it2; low: T026 it0 → v1r it1), so that each v1r member and T026 at that iteration differ only in the droplet location rule.
- **Limitation, declared:** the parcel state is not in the restart files. A v1r run that stops is restarted from t = 0.

## v1s extension declared 2026-11-25 ~14:40 (UTC+8), before any v1s hydro result: recondensation of Cu vapour on the target
- **Why (diagnosis, not a fit):**
  - In every model the emitting core sits on the face as a half-dome.
  - A uniform emitting hemisphere on a wall projects to σz/σx = √(19/64) ≈ 0.55, about 0.6 after the 250 µm blur. That is the model value: 0.60–0.69 across all 18 imaging scenarios of T026 high it2, so the imaging choices are not the cause.
  - The experiment's core is detached from the face, with aspect 1.07.
  - The models have evaporation but no return flux. After the pulse the face cools to about T_m, p_sat(T_s) collapses, and kinetic theory then requires the dense near-wall Cu vapour to condense.
  - Estimate: at 5 kK the effusive speed is √(kT/2πm) ≈ 320 m/s, so a 100 µm layer empties in ~0.3 µs.
- **Model (COND_SIG, COND_TB; registry recondensation_model, cu_condensation_coeff = 1):**
  - Net Hertz–Knudsen Cu mass flux from the first gas cell into the face: j = σ[ρY √(kT/2πm_Cu) − p_sat(T_s) √(m_Cu/2πkT_s)], applied when j > 0.
  - p_sat uses the same Clausius–Clapeyron as the target model.
  - It is applied only in steps where the Knight source does not evaporate, so the two are not double-counted.
  - Each kg removed takes e_Cu + p_Cu/(2ρ_Cu) plus its kinetic energy out of the gas, booked as a negative source.
  - The latent heat released into the target is not fed back: the estimated surface rise is < 10 K for 300 ns at 1.5e8 W/m².
  - σ = 1 is the measured value for clean liquid metals and the kinetic upper bound. No value of σ will be chosen from the results.
- **Runs (T031):**
  - v1s = v1n + recondensation (cases v1s_*).
  - v1rs = v1r with melt-depth droplets + recondensation (cases v1rs_*); this one is physically consistent with T030.
  - Both are seeded with the T026 shielding as in T030: high it1 → it2, low it0 → it1.
- **Expectations, declared, read whatever the outcome:**
  - (a) Mass and energy closure as before. M_recondensed is booked in budget.csv.
  - (b) σz/σx rises against the same-iteration base (T026 for v1s, T030 rp for v1rs), and S1 lift rises. A rise < 5% means recondensation does not detach the core.
  - (c) L ratios: direction not predicted. Removing Cu lowers late emission, but the near-wall gas was dim anyway.
  - (d) Report the recondensed mass against the evaporated mass.

## T033 OAT declared 2026-11-25 ~18:30 (UTC+8), before any result: sensitivity of the reported model v1n over declared registry uncertainties
- **Purpose:** an uncertainty band for the reported model, and a test of whether any input within its declared range can change the morphology (σz/σx). This is not a fit. No value from these runs will be adopted or reported as "best".
- **Members, one input moved at a time.** Cases v1bpde_high and v1bpde_low, one Picard iteration seeded as in T030. Ranges are the registry's, except where stated:
  - cu_removal_efficiency (droplet mass): ×1/3 and ×3. The registry says "order of magnitude"; half an order is declared.
  - cu_A1064_RT: 0.015 and 0.05.
  - f_Tcap: 0.85 and 0.95 (registry: scenario).
  - sigma_en_Cu: 3e-20 and 3e-19 (variant tables).
  - gaunt_ff: 1.5 (variant tables).
- **Mechanism:** `overrides:` in the batch spec. The values are recorded in overrides.json next to pluto.ini, and the run's registry hash changes.
- **Reading, declared:**
  - For each member report the change against T026 at the same iteration in L500/L300, L730/L300 (high) or L715/L300, L1035/L300 (low), σz/σx at 300 ns, and S1.
  - If no member moves σz/σx by more than 10%, the half-dome morphology is insensitive to the model's uncertain inputs, which points to missing physics (radiative transfer is declared next).

## 2026-11-25 ~21:30 — Radiative-transfer candidate deprioritised (a-priori estimate); T034/T035 v1rs OAT declared before any result
- **Estimate (scripts/vuv_estimate.py).**
  - Below 175 nm the cold O2 absorbs (Schumann–Runge continuum), so emission there can preheat the ambient gas.
  - In the thin limit this VUV band carries 13–24% of the emission at 20–30 kK and 3% at 12 kK. It is mostly resonance lines, which the plume largely traps itself.
  - With the T026 core P_rad of 30–240 W, 0.2 × P_rad × 50 ns deposited in the air within 0.2 mm heats it by only tens of K.
  - Radiative preheating therefore cannot move the absorption front, and radiative transfer is not pursued as the morphology mechanism now.
- **Remaining candidates for size and morphology:** laser absorption in the vapour and the energy coupling into the gas. The model plume is about half the experimental size, and half of the incident energy escapes. T033 (v1n) and T034/T035 (v1rs) test whether the declared ranges of the absorption inputs move them: sigma_en_Cu, gaunt_ff, cu_A1064_RT, f_Tcap, and for v1rs also cu_condensation_coeff = 0.3.
- **T034:** workstation, high fluence, v1rs, seeded from T026 it1 → compare with T031 high_v1rs.
- **T035:** desk node, low fluence, v1rs, Picard it0 unseeded, with its own baseline on the same node, so compiler differences cancel.
- **Reading as T033:** sensitivity only, no value adopted. For each input report Δ(L ratios), Δ(σz/σx), Δ(S1), Δ(plume size σx). If no member changes σx by more than 20% or σz/σx by more than 10%, size and shape are structural, and the next step is to audit the energy coupling (laser absorption model and t0 alignment), not to add a new sink.

## t0 alignment declared 2026-11-26 ~00:15 (UTC+8), before any shifted image is made
- **Evidence that the experimental t = 0 is not the 10%-of-peak point:**
  - High fluence: the experimental t = 0 frame already emits, and at 20 ns the threshold radius is 236 µm (data/exp/metrics_high.csv). The model has no gas absorption before 25 ns.
  - Low fluence: the 10 ns frame is saturated, with a radius of 65 µm.
  - The paper gives no optical definition of t = 0; its delays are set by delay generators and checked on an oscilloscope (data/exp/paper/timing_laser_notes.md, T036).
  - The registry's t0_convention already declares ±30 ns.
- **Determination of the offset:**
  - It comes from an observable independent of the plume images, the high-fluence pump–probe shock radius (main-text Fig. 3d).
  - Log least squares over all 23 points against the model shock gives Δ = 28 ns, the same for v1n and v1rs (registry t0_offset_shock).
  - High fluence is used because its ignition is the most robust. Low fluence then tests the same Δ.
- **Use:** image_v2_t28 renders the experimental frame at t from the model over [t + 28, t + 38] ns (`lipsim.image --t-offset-ns 28`). Δ = 0 is kept as the bracket, and both are reported.
- **Declared readings:**
  - Early frames (≤ 150 ns) and the L(t)/L(150) trend against the experiment, for both Δ.
  - At low fluence the same Δ leaves the shock about 25% small (pre-computed), which is a real deficit in ignition and coupling.
  - No Δ other than 28 ns will be tried on the images.

## T038 low-fluence ignition sensitivity declared 2026-11-26 ~00:55 (UTC+8), before any result
- **Problem:** v1rs ignites the low-fluence plume at about 55 ns and couples about 3× too little energy into the gas (shock about 25% small at any t0 offset). The experiment emits by its 10 ns frame.
- **Members (desk node, Picard it0; baseline T035 low_base on the same node):**
  - (1) v1m two-temperature avalanche breakdown during the pulse, added to v1rs (case v1rsbd_low). It was declared on 2026-11-24 and judged ineffective on v1k; this is a retest on the reported model.
  - (2) Pulse-duration reading: "60 ns" in SI Table 1 is not defined as FWHM (T036 found no convention). The alternative reading, a 1/e² full width, gives FWHM = 60 × √(ln2/2) = 35 ns.
  - (3) Both together.
- **Readings, declared:**
  - ignition time, meaning the first time E_gas_abs exceeds 1 µJ;
  - E_gas_abs at 150 ns;
  - the shock radius against Fig. 3d with Δ = 28 ns;
  - σx and the L(t)/L(140) trend.
- **Use of the results:** no member is adopted for being closer. An interpretation of 60 ns would be adopted only on documentary evidence, such as the laser datasheet's definition of pulse width.

## Waveform scenario declared 2026-11-26 ~02:20 (UTC+8), before any waveform result
- **Source:** T036 (data/exp/laser/waveform_ipg_ylp_hp_T3_fig6.csv/.json) is a measured oscilloscope trace of an IPG YLP-HP fiber laser (61.7 ns scope width, 22.5 ns rise), digitised point by point without fitting. It is a comparable device of the same family, not the paper's YLPN-1-30×240-100-R; no trace of that laser was found.
- **Shape:** relative to a Gaussian of 60 ns FWHM (10%-point convention), 10% of the energy arrives at 13 ns instead of 22 ns, and 50% at 42 ns instead of 55 ns. The peak is at 40.5 ns instead of 54.7 ns.
- **Use:** a scenario under PLAN's pulse-shape row ("MOPA waveform"). Because it is a comparable device, it is reported as a bracket and does not replace the Gaussian baseline.
- **Implementation:** Laser(waveform=…) in the target model, plus PULSE_TAB with lip_tables/pulse.txt in the solver. The table is shifted so that t = 0 is the 10%-of-peak leading-edge crossing and is normalised to the pulse energy.
- **Runs (T039, workstation):** v1rswf_high/low, seeded as T031, against T031 v1rs.
- **Readings, declared:**
  - the ignition time, meaning the first time E_gas_abs exceeds 1 µJ;
  - E_gas_abs at 150 ns;
  - the shock against Fig. 3d (Δ = 28 ns, and Δ refitted for this scenario reported separately);
  - σx, σz/σx and the L(t)/L(150) trend.
- **Expected:** ignition about 10 ns earlier. The low-fluence coupling deficit (about 3×) is not expected to close fully.

## S4 defocus calibration (high fluence), recorded 2026-11-26 ~10:30 (UTC+8)
- **What:** the imaging defocus D is an instrument property, not a model parameter. It is fixed from the earliest high-fluence frames (0 / 20 / 30 ns, σx = 108 / 113 / 117 µm), where the emitting region is far smaller than the blur disc and the frame is PSF-dominated.
- **Method:** re-blur the unblurred model frames for D = 250…600 µm and match σx of those three frames only. Result: D ≈ 475–500 µm for both T031 (v1rs, Δ = 28 ns) and T039 (waveform). That equals the declared `high` scenario (500 µm), so the declared value is used; no new value is introduced.
- **Low fluence:** its 10 ns frame (σ = 33 µm) is consistent with the declared `low` scenario (0 µm). The two series use different settings, consistent with the two series being recorded separately.
- **Transparency:** the d500 images and later-time metrics of T031/T039 had already been computed as part of the declared scenario bracket before this calibration. The calibration uses only the three earliest frames, and the later-time agreement is not used to choose D.
- **Experimental saturation:** high-fluence frames at 50–160 ns are saturated (n_sat 17–139, peak 255); 180 ns onward is unsaturated. The experimental L at 50–160 ns is therefore a lower bound, and brightness-trend comparisons are normalised at 300 ns (L(t)/L(300)), not at 150 ns.

## T042 high-fluence vertical extent declared 2026-11-26 ~11:40 (UTC+8), before any result
- **Observation that motivates it (experiment only):** at high fluence the experimental emission grows vertically while it is still PSF-limited sideways. At 0 / 20 / 30 ns, σz = 78 / 92 / 146 µm and σx = 108 / 113 / 117 µm, and the frames are unsaturated. A narrow, tall luminous column forms along the beam during the pulse.
- **Model fact (T041):** ≥ 99% of the laser energy is deposited within 3.5–6 µm of the face, and the gas above is transparent. With D = 500 µm, the model σz (100–111 µm) is close to the occluded PSF alone, so its intrinsic vertical extent is small.
- **Members (workstation, seeded and with Picard iteration as in T039):**
  - (1) v1m two-temperature avalanche on v1rs (v1rsbd_high). Non-equilibrium ionization is the declared candidate for absorption in the plume above the dense layer.
  - (2) The same on the waveform scenario (v1rsbdwf_high).
  - (3) The alternative pulse-width reading, FWHM 35 ns (as T038), at high fluence.
- **Readings, declared:**
  - the absorbed-power height z90_abs and the Cu fraction (scripts/laser_deposition.py);
  - σx and σz at D = 500 µm;
  - L(t)/L(300) from 300 to 730 ns;
  - the shock against Fig. 3d with Δ = 28 ns, plus a refitted Δ reported separately.
- **Use:** v1m is adopted into the reported model only if it is physically active (it raises z90_abs above 20 µm during the half-peak window), not because σz moves closer. Member (3) is adopted only on documentary evidence about the pulse-width convention. Otherwise all three are reported as sensitivity.
- **Ruled out before running (estimate, not implemented):**
  - Thermal emission from droplets: the v1r droplets stay below z ≈ 225 µm, which is below the emission centroid, so they cannot raise the plume.
  - A missing wall conduction sink: it is already in the model and removes about 35% of the absorbed energy.

## Occlusion geometry of the synthetic image declared 2026-11-26 ~19:45 (UTC+8), before computing it
- **Motivation (experiment only):** in every high-fluence frame from 0 to 160 ns the experimental emission reaches 40–60 µm *below* the transferred face row (thr_bottom 97–107 µm above the crop bottom; face row 68, bracket 60.4–69.3). The imaging pipeline instead zero-pads below the face before blurring: no light at all can appear below the face (option O1, a hard cut). O1 is a convention and was never calibrated.
- **Options:**
  - O1, hard cut at the face (current).
  - O2, no cut: light blurred below the face reaches the sensor, as when the target's extent along the line of sight is too short to vignette the cone.
  - Partial vignetting lies between the two.
- **Calibration:** use only the earliest frames (0 / 20 / 30 ns, unsaturated, PSF-dominated) with D = 500 µm and Δ = 28 ns, for the T031 and T039 unblurred frames. Readings: σz/σx, and the fraction of light below the face row, compared with the experiment. The option (or the O1–O2 interval) consistent with those frames is used for all later frames.
- **Use:** later-time σz is reported under the selected convention and under both bracket ends. Later-time agreement is not used to choose.

## T043 emission-height decomposition declared 2026-11-26 ~20:20 (UTC+8), diagnostic only, before any result
- **Question:** why the model's band-b emission sits in the first ~10 µm above the face until about 50 ns, while the experiment's lifts about 100 µm within 30 ns. Is the near-wall dense layer too bright, or the lifted plume too dim?
- **Tool:** scripts/emission_layers.py, which uses the imaging's native-cell transfer with self-absorption. Outputs, per snapshot ≤ 200 ns:
  - emission height percentiles;
  - the fractions from z < 10, 10–50 and > 50 µm;
  - the line/continuum split in the near-wall layer;
  - axial T, ρ, Y, n_e profiles.
- **Runs:** T031 high_v1rs/it2, T039 high_wf/it2, T026 high_thin_G0/it2.
- **Use:** the result only identifies the layer and the emission channel. Any physics change it suggests is declared separately, before implementation.

## Continuum charge states in the imaging (v2.1 imaging fix) declared 2026-11-26 ~22:50 (UTC+8), before any re-imaged result
- **Defect:** forward.band_image builds the continuum (Kramers ff+bf, ∝ z² n_e n_z) from the ions with z = 1–3 only, through the pop-table fields nion_z1..3. The EOS carries Cu up to z = 5 and N/O up to z = 4. The laser absorption (props.alpha_at) and the radiative loss (NEC) already sum over every charge.
- **Size, from the EOS (Y = 1):** the continuum is understated by ×17 000 at (0.02 kg/m³, 140 kK), ×56 at (0.2, 100 kK) and ×4.9 at (1.5, 90 kK); the error is ≤ 1.1 below 60 kK. T043 shows the plume above the face at 50–135 kK during and just after the pulse, so the lifted hot plume has been rendered too dark.
- **Fix:** pop.bin carries nion_z1..5 (props.pop_fields), and band_image sums every nion_z it finds; old tables give the old result. The solver inputs (eos.bin, opt.bin) are unchanged, so no simulation is repeated, only re-imaged.
- **Use:** this is a correctness fix, adopted whatever the outcome. All high-fluence production runs are re-imaged (image_v2c) and reported against the old images.
- **Known companion defect, declared separately (v1u):** the Cu charge ladder stops at a bare Cu⁵⁺ (Q = 1), and the table ceiling is 150 kK. At 100–140 kK in the low-density plume, 15–69% of the Cu sits in that top stage, and the NIST IPs of Cu VI–IX (103–198 eV) are missing energy sinks. The plume temperature is therefore probably overestimated. v1u extends Cu to Cu IX (bare Cu X), with NIST levels where available and the ground level otherwise, and raises the table ceiling to 3×10⁵ K. It requires new tables and new simulations. It is declared now and run after the imaging fix.
- **v1u implementation (2026-11-26 ~01:50 UTC+8 on 11-27, before any v1u run):**
  - Table config configs/tables/v1d.yaml: numerics.eos_stages {Cu: 9}, with the log10 T spacing of v0 extended to 2.99×10⁵ K.
  - mixture.species uses the NIST levels file of a stage when it exists, otherwise the ground level only (nist_io.read_ground_level, g = 2J+1), which is a lower bound on Q.
  - scripts/fetch_nist.py now requests the Cu VI–IX levels.
  - EOS check, Y = 1: e(0.02 kg/m³, 140 kK) goes from 4.3 to 8.0×10⁸ J/kg (Z̄ 4.69 → 6.58); e(0.2, 100 kK) rises 7%; at 1.5 kg/m³, 90 kK, and everywhere below 60 kK, it is unchanged. The change is therefore confined to the hot low-density plume.
  - Runs (T045): v1rs high and the waveform scenario, seeded as T039, on tables v1d, imaged with v1d.
  - Use: a completeness fix, adopted whatever the outcome.

## Supervisor feedback 2026-11-27 and T046 near-wall resolution test, declared 2026-11-28 (UTC+8), before any result
- **Feedback:** the morphology after 150 ns is good. The remaining problem is the early (0–150 ns) plume, which hugs the wall and is too low. The supervisor's hypothesis is that the splashed molten Cu is not coupled to the plume (paper Fig. 4: the spatter top coincides with the plume bottom).
- **Pre-computed checks (no run):**
  - Fig. 4e1, high fluence: the spatter top is ≤ 5 µm until 100 ns and 20 µm at 150 ns. The plume bottom is already 25–35 µm at 50–80 ns. The coincidence holds from about 150 ns to 450 ns.
  - With the model melt mass (≈ 12 ng; ≈ 100 ng if the crown is wide), the kinetic energy at 344 m/s is 0.7–6 µJ, against ≈ 190 µJ absorbed in the gas, and the melt moves at 344 m/s while the plume moves at km/s. The melt cannot lift the plume.
  - Treating everything below the measured spatter top as opaque, in object space before the blur, on T045 v1rs+v1u: no change at 0–80 ns (the spatter top is < 1 px). At 150–500 ns the centroid rises by 5–42 µm and σz by 2–4 µm, and L falls by 11–49%.
  - So the spatter can shape the plume bottom after 150 ns (a candidate improvement there), but it cannot produce the early lifted emission. This conclusion is shared by an independent analysis (desk assistant).
- **T046 (desk; the workstation is off over the weekend):**
  - The model's brightest early emitter is a 60–100 kK dense layer within about 10 µm of the face, which receives ≥ 99% of the laser energy within 3.5–6 µm, i.e. 3–6 cells of G0 (1 µm).
  - Test: a paired run of v1rs+v1u at Picard it0, G0 against G0w (0.25 µm cells in the first 20 µm, otherwise G0), to 320 ns.
  - Readings:
    - z50, f_lt10 and the near-wall T_5 and Tmax (emission_layers);
    - z90_abs (laser_deposition);
    - band-b L(t)/L(300) at 0–80 ns, σz and zc at 0–150 ns (D = 500 µm, Δ = 28 ns);
    - E_gas_abs(150 ns).
  - Decision rule:
    - If G0w moves the 0–80 ns readings by more than 20% (z50, f_lt10, L(t)/L(300)), the early discrepancy is at least partly numerical. Production then moves to near-wall-refined grids.
    - Otherwise the near-wall layer is resolved and physics must be added. The first candidate is the melt-surface-coupled vapour source (the vapour source rides on the deforming/rising melt, using the measured spatter-height curve as the only input), declared separately.
