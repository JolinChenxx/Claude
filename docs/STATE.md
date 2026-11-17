# STATE: progress snapshot (Claude updates this before ending each session)

Updated: 2026-11-15

## Cards
- T001–T003 done (workstation environment, history archive, Python env `D:\sim_tools\envs\lipsim`).
- T004 done (conclusions below). T005 done: stock PLUTO 4.4-p4 at D:\sim_tools\pluto_ref, Sod/Sedov pass in serial and MPI8 (Sedov serial/MPI L1 difference 0.25%, use as the V14 tolerance reference).
- T007 (NIST download) ready. `handoff/to_codex/legacy_tasks.md` (disable the 3 legacy scheduled tasks) not yet done.
- The only process found by the earlier check, mpiexec 35756, is an unrelated FDTD job (7155a2b), not touched.
- T006 (user asks the authors): question list ready at `docs/QUESTIONS_FOR_AUTHORS.md`.
- The user agreed to stop the historical PLUTO 72 h controller (probably already exited); T004 step 0 checks.

## Claude work packages
- M0.2/M0.3 experimental dataset: **mostly done**. `data/exp/` (frames, metrics, trends.png), `docs/DATA_NOTES.md`, V11 closed-loop test passes.
  - Fig. 3d shock trajectory digitized (`data/exp/curves/fig3d_shock.csv`). To do (optional): cross-check against `prior/pluto/experiment/metrics.csv`.
- M0.4: **done**. `data/params/registry.yaml` (33 entries, 6 to be checked against NIST/datasheets in M1.1), `src/lipsim/params.py` (loader; '@name' references), `scripts/check_provenance.py` (registry schema / bare numbers in configs / untagged literals in physics source), `tests/test_provenance.py`, `docs/PREREG.md` skeleton.
- M1 started (not dependent on NIST): `src/lipsim/eos/nist_io.py` (NIST CSV parsing, excludes autoionising levels above the limit), `src/lipsim/eos/saha.py` (multi-element Saha–Boltzmann + Debye–Hückel IP lowering, partition-function cutoff tied to the lowering). `tests/test_saha.py`: analytic H limit + Cu checks (archived NIST tables for now; switches automatically once T007 data arrives).
  - To do: molecules (N2/O2/NO dissociation, needs molecular constants), thermodynamic tables (e, p, c_s), full-spectrum NEC, in-band emission, transport properties; M1.1 source list in `docs/MODEL.md`.

## Key numbers
- S4 10.72 µm/px, S3 9.24 µm/px. Experimental τ_L: high 182 ns / low 218 ns; Δt₁₀: 433 / 542 ns.

## T004 conclusions (2026-11-15, historical PLUTO best case, read-only)
- The emitting gas barely cools: emissivity-weighted temperature 10.7 kK (300 ns) → 9.4 kK (630 ns) → 8.7 kK (1 µs). Radiation + conduction cooling times are about 20–50 µs (the experimental brightness decays on ~0.18 µs).
- Brightness e-folding time at 300→630 ns: optically thick 1.6 µs; optically thin 1.1 µs; only changing the upper level to the correct 6.19 eV (Cu I 515.3/521.8 nm) gives 0.45 µs. Experiment: 0.18 µs.
- Conclusion: the historical model's "too bright late on" comes from both (a) missing cooling (only ~3 W of radiation, no full-spectrum NEC) and (b) a wrong in-band line upper level. Implication for M1: full-spectrum NEC and correct NIST line data are the highest priority.

## D0 (2026-11-15 evening, PLAN v3) Claude work-package progress
- W1 EOS: **done**. `src/lipsim/eos/mixture.py` (Cu–air: Cu I–V, N/O I–IV, Ar I–III atoms/ions + N2/O2/NO in RRHO; log-space solve, robust from 300 K to 150 kK); the screening length is bounded below by the ion-sphere radius (F-grade, to be recorded in PREREG_v0); energy reference = cold N2/O2/Cu/Ar. Tests `tests/test_mixture.py`: cold air e = 5/2 RT; 1 atm O2 half dissociation ≈ 3450 K, N2 ≈ 7000 K, NO peak ≈ 5%; agrees with the atomic solver for pure Cu.
  - Table builder `python -m lipsim.tables.build` (format LIPTAB01: e/p/ne/dchi_eV on (log10ρ, log10T, Y)); cloud low-res reference table `verify/ref/tables_v0_lowres/` (17×28×5, 5.5 s on 4 cores). Full resolution in `configs/tables/v0.yaml` (81×136×14), built by Codex in T010.
  - Molecular constants and N/O/Ar atomic weights are in the registry with verified=false (T014 checks them against the NIST WebBook).
- Next: W4 (target/evaporation source model), W2 (α1064, NEC, in-band), W5 (imaging/batch), W3 (PLUTO C modules, waits for T008).
- W4 target/evaporation source: **core done** (2026-11-15 22:00). `src/lipsim/target/model.py` (1D implicit conduction per ring + Voller–Swaminathan enthalpy correction, exactly conservative; A(T) = A_RT + Drude change driven by Matula resistivity; Clausius–Clapeyron + Knight M=1; 0.9 T_c cap with over-cap energy booked), `python -m lipsim.target.build` (Picard interface: `--shielding <run_dir>` reads surface_transmission.npz, relaxation 0.5), `configs/cases/v0_{high,low}.yaml`. Tests: V6 constant-flux analytic solution < 1%, energy conservation < 1%.
  - Unshielded (it0) results: high fluence target absorption 62 µJ (15.7%), vaporised 3.3 ng, T_s hits the 0.9T_c = 7020 K cap at 24 ns, over-cap energy 37% of absorbed → **U4 triggered** (model-internal criterion); low fluence 29 µJ (9.1%), 1.0 ng.
  - Cu thermophysical properties registered (most verified=false, from memory of handbook/review values, to be checked).
- **Codex still has not picked up T008/T009 (issued 19:00, no push as of 21:50).**
- W2 optics: **done** (2026-11-15 22:20). `src/lipsim/optics/props.py`: α1064 = e–i ff+bf (Kramers–Unsöld, incl. stimulated emission) + e–n (Drude with momentum-transfer cross-section, quantum factor); NEC thin/trap (all NIST lines with A + Kramers ff + hydrogenic fb); n_z/Q_z population fields (imaging computes in-band levels exactly). Table builder outputs eos.bin/opt.bin/pop.bin. Checks: ff power coefficient matches the textbook 1.42e-40√T to 0.4%; air NEC(10 kK, 1 atm) = 8.5e8 W/m³/sr (literature order 1e8–1e9).
  - Note: NIST Cu I has only 37 A values, so the Cu NEC is a lower bound (v1 adds the Kurucz line list).
- W5 imaging core: `src/lipsim/image/forward.py` (in-band NIST lines, Boltzmann populations from the pop table, Doppler line shape with self-absorption via line-of-sight transfer; Kramers–Unsöld continuum; exact chords through concentric shells). V10 minimal tests: analytic uniform sphere (thin/intermediate/thick) < 2%, dilute thin = self-absorbed, frequency grid converged. Finding: Cu I 515 nm with Doppler-only width already has τ0 ~ 2 over 200 µm at ρ ≈ 1e-3 kg/m³, so self-absorption is not negligible.
  - To do: camera (gate / filter / blur / gain / 8 bit), PLUTO output reader (format to confirm after T008), lipsim.batch / report.
- W3 design notes (2026-11-15 21:55, from reading a PLUTO 4.4-patch1 third-party mirror, RU-TD/PLUTO4.4; API reference only, nothing executed, production still uses the T008 official 4.4-p4):
  - Source-term hook: `Src/split_source.c::SplitSource()` officially allows "additional user-defined terms". Local copy adds one line `LipSource(d, dt, Dts, grid)` (diff goes in pluto/overrides/). LipSource does: column marching of the laser along z (with `-dec N 1` each process holds full z columns) + 4π·NEC radiation + target-face evaporation source (first cell layer).
  - EOS: the stock PVTE equilibrium branch only supports e(T,ρ) and μ(T,ρ) (GetMu has no tracer), so it cannot carry Y_cu. Fix: replace `EOS/PVTE/thermal_eos.c` and `internal_energy.c` with lip_eos.c (same function names: InternalEnergy, GetEV_Temperature, Pressure, GetPV_Temperature, SoundSpeed2, plus InternalEnergyFunc/GetMu/Gamma1 from pvte_law), using our (log10ρ, log10T, Y) tables with Y from v[TRC]. List both replacement files and their diffs in pluto/overrides/.
  - Not done yet (waiting for T008 so the official source can be compiled in the cloud): lip_tables.c, lip_eos.c, lip_source.c, init.c, definitions.h, userdef_output.c (T, ne, alpha, I_laser), build_pluto_lip.py.
- W3 pure-C kernels (2026-11-15 22:15, PLUTO-independent, compiled and tested in the cloud): `pluto/lip/lip_tables.[ch]` (LIPTAB01 reading, trilinear interpolation, exact inversion e→T and p→T, equilibrium sound speed), `pluto/lip/lip_physics.[ch]` (laser column marching down + reflected return, vapour total enthalpy, (t,r) source-table lookup). Tests `tests/test_c_kernels.py`: C vs Python interpolation error 4e-14; Beer–Lambert / energy closure 1e-12. -Werror passes.
  - Still to write (after T008 arrives): PLUTO glue (init.c/definitions.h/pluto.ini, split_source override calling LipSource, PVTE replacements, userdef_output, Analysis budget) + build_pluto_lip.py.
- **W3 PLUTO integration: first version compiles and runs on Linux** (2026-11-15 22:40). `pluto/lip/` (definitions.h, init.c, pvte_law.c, userdef_output.c, lip_glue.[ch], thermal_eos.c / internal_energy.c / split_source.c replacing upstream files with diffs in `pluto/overrides/`), `scripts/build_pluto.py` (unpacks the official archive with hash check, generates the makefile without setup.py, Linux/Windows, records replaced files and their diffs).
  - Source-term time-step limits (N-grade): |Δρe| ≤ 0.2ρe, injected mass ≤ 0.5ρ, vapour signal speed (u_v + c_v)/Δz.
  - Smoke case (high fluence, 60×80 coarse grid, low-res tables): **self-consistent ignition** — gas absorption 0.04 µJ at 36 ns → 25.7 µJ at 42 ns → 134.7 µJ at 60 ns (incident 234 µJ), target absorption stays at ≈ 8.3 µJ after ignition (shielding); Cu mass 1.36 ng; domain energy change vs. source sum 132.40/133.15 µJ (0.6%). 60 ns took 113 s on 1 core.
  - Table minimum temperature lowered to 200 K (ambient 293 K was below the old 300 K floor and caused ConsToPrim warnings; now 0).
  - Pending: pluto.ini generator + batch (lipsim.batch), post-processing chain (surftrans merge → Picard, snapshot → imaging → metrics), Windows build check (T010).
- **T009 accepted (2026-11-16 10:20).** Unmodified PLUTO ideal-gas point blast: energy conservation 1e-14, G480/G960 radius difference < 0.4%, radial `-dec 8 1` bit-identical to the default partition (so LipSource's full-column requirement does not change results). Early exponent 0.372 (R 60–200 µm): meets the card's ±0.03, misses the PLAN's ±0.02 — explained by the finite 20 µm source and the 1 atm back pressure (R/R0 ≈ 0.2, R0 = (E/p0)^{1/3} ≈ 1 mm); not a code issue, the V2b criterion should be evaluated at R ≪ R0 in the future.
  - Independent energy inversion (RMS ≤ 10%): high fluence 78–238 µJ (t0 offset 25 ns) / 106–296 µJ (55 ns) / 167–≥320 µJ (85 ns); low fluence 74–207 / 127–242 / none. **For reference only, not a calibration**: the cloud smoke case's self-consistent gas absorption at 60 ns is 135 µJ (radiation 6 µJ), inside the high-fluence interval.
  - Benchmark: ideal gas 480², radial 32 ranks is fastest; per-rank throughput about 2e6 cell-steps/s.
- 2026-11-16 13:10: `scripts/report_v0.py` (comparison report generator: P1/P2/P3/P6 judged automatically per PREREG_v0 §3, every member × iteration × filter × transfer × defocus listed, L(t) normalised plot) is ready; the report can be produced as soon as T011 results arrive. Waiting for Codex to close T010 and start T011 (no push since 10:30).
- **T011 v0 accepted (2026-11-17 00:10).** 14/14 solver runs rc=0, energy budget residual high ≤ 0.01%, low 0.7%.
  - ConsToPrim warnings (26M lines): localised to the vapour-injection layer (first cells, r ≈ 9 µm, z = 0.5 µm); energy budget unaffected (see residuals). Cause: ṁu_v momentum injected next to the reflecting wall. v1 fix (pre-declared engineering correction): inject mass + stagnation enthalpy and let the pressure drive it.
  - **High fluence**: self-consistent ignition, gas absorption 267 µJ (67%), radiation 79 µJ by 800 ns, Cu 3.3 ng; T009 independent window 106–296 µJ. L(t)/L(300) matches the experiment at 150–350 ns; after 400 ns the model flattens (0.4–0.7 at 740 ns vs experiment 0.1) → P1 τ 372–1350 ns vs 182 (partial/fail), P2 > 440 ns (n/a window), P3 turning time 530–630 vs 330 ns (partial, same sign). As expected in PREREG_v0: missing molecular transport / wall heat loss → late cooling too slow.
  - **Low fluence**: **no ignition** (gas absorption 0.0017 µJ); emission comes from hot vapour alone; τ 288–600 ns vs 218 (thin pass/partial, trap fail). Energy is ~2 orders of magnitude below the T009 shock-implied 70–240 µJ → ignition gate G1 fails at low fluence; per PREREG_v0, branch B (semi-empirical, energy from the shock inversion) must be added for low fluence.
  - Picard it0→it2: high-fluence results move by 20–35% (shielding feedback not converged; v1 two-way coupling); low fluence unchanged (no shielding).

## 2026-11-17 08:10 (UTC+8) — T013a review (v0 build, high fluence, it0, G0/G1/G2)
- Gas absorption 267 / 272 / 257 µJ, radiation 78.6 / 79.3 / 73.5 µJ (G0/G1/G2): spread ≤ 6%.
- L/L(300 ns), filter b, baseline defocus, at 500 ns: sa 0.68 / 0.60 / 0.64, thin 0.80 / 0.71 / 0.79 (trap NEC similar). Not monotonic, spread about ±10%.
- Experiment at about 500 ns: about 0.35. The late-time excess (about ×2) is far larger than the grid spread, so it is a missing-physics effect, not resolution. This supports the pre-declared v1 upgrade order (conduction first).
- ConsToPrim lines grow with resolution (G2: 2.3–2.6 M, first at step 1127, near wall). Open item for v1 (wall treatment), not resolved by T014 alone.

## 2026-11-17 19:50 (UTC+8) — T015 early look (v1 conduction, G0 it0, interim)
- Budgets: wall conduction removes 24/25 µJ (low thin/trap) and 44/47 µJ (high), i.e. 15–28% of gas absorption; radiated energy drops 20–28% vs T014; gas absorption unchanged (≤1.5%); residual ≤ 0.06%.
- L/L(300), filter b, baseline: late decay improves in the right direction but modestly. Examples: low sa 1000 ns 0.155→0.118 (exp 0.038); high thin 500 ns 0.633→0.587 (exp 0.37); low sa 400–600 ns 0.68/0.53/0.44 vs exp 0.70/0.45/0.29.
- Conclusion (interim): conduction is a real but not dominant missing sink; the late-time excess (×1.5–3) remains. Wait for it1/it2 + G1 before choosing the next pre-declared v1 item (candidates already in PREREG_v0: two-way coupling, Kurucz lines/Voigt; to be weighed: emission model at low T and ambient mixing).

## 2026-11-17 21:15 (UTC+8) — T015 emission-source diagnostic (scripts/diag_emission.py, thin, filter b)
- Late signal (≥500 ns) is 87–99% Cu I lines, from nearly pure Cu vapour (emission-weighted Y 0.87–0.94; air-rich cells < 5%), T_emw 9–11.5 kK, n_e ≈ 1.5–2.3e23 m^-3; continuum ≤ 3%, air lines ≈ 0. Same picture in T014 (v0.1) and T015 (v1).
- Conduction lowers absolute power about 2.5–3× but T_emw keeps a plateau near 9 kK → L/L(300) shape changes only modestly.
- Cloud estimate at core conditions (Y=0.92, 9.4 kK, ≈0.8 bar): radiative cooling time ρΔe/(4π NEC) ≈ 1.4 µs (thin) / 6 µs (trap); Cu I in-band e-fold needs ≈ 2000 K per ~200 ns (≈10 K/ns), radiation gives ≈ 5 K/ns. LTE holds (McWhirter ≈ 1e22 m^-3).
- Interpretation: the Cu vapour core neither mixes with air nor radiates fast enough. Pre-declarable physics without free parameters: (a) Cu–air molecular (Chapman–Enskog) interdiffusion; (b) more complete Cu I/II radiative cooling (Kurucz lines, escape-factor NEC instead of thin/trap limits). Turbulent mixing models are excluded (free constants).

## 2026-11-17 21:25 (UTC+8) — v1 step 2 implemented: Cu–air interdiffusion (user approved)
- Model: Fick J = −ρD∇Y, hard-sphere Chapman–Enskog binary D (registry diff_cuair_model, hs_diameter), with interdiffusion enthalpy flux (h_Cu − h_air)J, h_Cu − h_air = ∂h/∂Y|p,T from the LTE mixture. Tables: opt.bin D_cuair, dh_dY. PLUTO: carried by the RKL parabolic update (tc_flux.c flux, tc_rhs.c applies to TRC, rkl.c var list). Tests: tests/test_diffusion.py (cold-air closed form, 1/ρ scaling, ideal-gas ∂h/∂Y).
- Values: D ≈ 2e-5 m²/s (300 K, 1 bar); ≈ 1e-2 m²/s at core conditions (9.4 kK, 0.8 bar) → √(2Dt) ≈ 150 µm in 1 µs.
- Cloud smoke (high, smoke grid, 0–60 ns): 0 ConsToPrim, residual −0.17%, M_Cu = M_src, mixed-cell Cu 31% → 34%.
- Declared limitation: ions as hard spheres (no polarisation/ambipolar), no thermal diffusion; uncertainty factor ~2–3 on D.
- Card T016 (after T015 solver runs).
