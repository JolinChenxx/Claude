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

## 2026-11-17 23:55 (UTC+8) — T015 low_thin_G1 it0 (interim)
- L/L(300), filter b, sa, baseline at 400/500/600/700/1000/1200 ns: G1 0.70/0.46/0.39/0.26/0.09/0.06; G0 0.68/0.53/0.44/0.40/0.12/0.09; exp 0.70/0.45/0.29/0.17/0.04/0.016. Thin transfer: G1 0.92/0.80/0.72/0.64/0.37/0.28.
- G1 decays faster than G0 at late times (20–35% lower at ≥ 600 ns): low-fluence late-time grid sensitivity persists under v1 (as in T014); G1 (and a G2 check) is the reference for the low series.
- Codex heartbeat on branch codex-status works (entries 23:36, 23:49).

## 2026-11-18 01:40 (UTC+8) — T015 final review (v1 conduction; card done)
- All 14 solver + 14 imaging rc=0; |energy residual| ≤ 0.034%; E_wall 15–76 µJ.
- **Picard convergence (new finding, numerics):** the one-way target↔gas iteration (relax 0.5) converges from above. Target absorption assumed by the target model vs measured in PLUTO (A·I_s): high G0 62/8.8 → 32/10.9 → 22/17.5 µJ (it0→it2, still 27% apart); low G0 28/16 → 21.0/21.6 → 21.4/22.2 µJ (converged ≤ 4% at it1–it2). The monotone drop of high-fluence gas absorption (296→254→197 µJ) and M_Cu (3.3→1.6→1.0 ng) is this convergence, not a bug. **High-fluence results need it3–it4** until |model−PLUTO|/PLUTO < 10% (criterion declared here, before seeing further results). Batch runner now supports continuation (`start_it`, `prev_run`).
- High it2 (most self-consistent so far) shows a late re-brightening around 450–550 ns (L/L300 0.86→0.95 at 400→500 ns, thin 1.12) absent in it0 and in G1 it0 → to diagnose (diag_emission on it2) before interpreting.
- High G1 it0 vs G0 it0 (sa): 0.66/0.48/0.38/0.38 vs 0.81/0.46/0.36/0.30 at 400–700 ns; exp 0.54/0.37/0.21/0.13.
- ConsToPrim: 44.3 M lines in total, dominated by it0 G0 (11.9 M each high); falls with iteration (0.93 M at it2) and with grid (G1 0.46–0.59 M) → linked to the strong unshielded early vapour injection. Location/type extraction requested from Codex.
- Low series: converged Picard; G1 reference (see 23:55 entry).

## 2026-11-18 02:00 (UTC+8) — T015 follow-up diagnostics
- **ConsToPrim (high_thin_G0 it0, rank 0):** all 11.85 M lines are one message, "rhoe < 0 or T < T_CUT_RHOE". They occur only at steps 19037–31425 (starting at about 120 ns; dbl every 2.5 ns, file #48), in the dense near-target vapour (z 1.5–38 µm, r 6–45 µm, most at j = 4–8). Most are reported with **stage numbers up to 11, i.e. inside the RKL super-time-stepping stages** of the conduction update. The intermediate RKL stages overshoot below zero internal energy where κ/(ρc_v) varies steeply, and ConsToPrim repairs them. The energy effect is bounded by the global residual (≤ 0.034% of the sources), but this is a numerical defect of the v1 conduction coupling. Candidate remedies, to be decided before use and not tuned to the data: (a) the RKL stage count from the local maximum of κ/(ρc_v) including the c_v floor; (b) a positivity-preserving RKL2 variant, or operator-split implicit conduction in the dense near-wall layer. Keep counting in T016.
- **High it2 emission (thin):** less Cu (1.0 ng vs 3.3 ng in it0) at **T_emw 21–26 kK** (it0: 10–13 kK). The 510 nm band is dominated by **Cu II lines (≈ 70%)**, with Cu I 11–24% and continuum 7–13%. The thin power falls only 39% from 300 to 700 ns (it0: 56%). The late "re-brightening" in the sa images is the Cu II-dominated hot core becoming optically thinner, not a new energy input. As the high-fluence Picard iteration converges, less Cu carries the same gas energy, so it is hotter and decays more slowly. **The late-time excess at high fluence is therefore an energy-partition problem (≈ 200 µJ of absorbed laser energy kept in a small hot core), not only a cooling-rate problem.** Interdiffusion (T016) dilutes the core; whether that is enough will be seen in T016 it2 and later iterations.

## 2026-11-18 04:00 (UTC+8) — T016 aborts: cause and fix (v1d)
- T016b (diagnostic build with rank logs): first gas row next to the target (j=4, r ≈ 56–61 µm), ρ ≈ 7e-4 kg/m³ (mean free path ≈ 0.4 mm ≫ cells). There the continuum Fick flux exceeded the free-streaming bound by about 10³, its interdiffusion enthalpy flux drove E < 0 inside the RKL stages, and E became NaN. All three low-fluence v1b members aborted (G0 thin/trap at ≈ 355 ns, G1 at 185 ns).
- Fix v1d: J = J_F/(1+|J_F|/J_max), with J_max = ρY v_th/4 on the upwind side (registry diff_cuair_limiter; no free coefficient). Also: rank logs on; finite guard; table T-inversion returns the floor (flagged as failed) for p or e ≤ 0/NaN instead of silently returning T_max. Smoke test: limiter inactive in the dense phase (energies within 6e-4 of v1b).
- T016c: both low G0 cases restarted from the abort point ran to 497/598 ns with zero ConsToPrim, E<0 or other warnings on all ranks (40-min limit).
- T016r: low members rerun from t=0 with v1d on the free 32 cores. The three running v1b high members are left alone pending the user's decision (stop and rerun with v1d is recommended).

## 2026-11-18 08:34 (UTC+8) - Codex execution record: authorized T016 stop
- Executed the user-approved T016_stop_v1b instruction: wrapper PID 1492 and all 24 remaining v1b solver ranks stopped; verified at 08:34:28. Reason: v1b superseded after four member aborts.
- Final budgets: high_thin_G0 449.4998 ns (10,762,237 rank-zero ConsToPrim lines); high_thin_G1 203.9977 ns (359,341 lines). Evidence and exact stop timestamps: results/T016/stopped/. All raw data retained.
- T016 marked done as superseded, not as a successful production run. T016r and T016s process trees remained running; high-fluence v1d reruns await a new card after T016s.

## 2026-11-18 09:00 (UTC+8) — T016s: rmax_par 1000 adopted; T016h issued
- A/B from T016r low_trap_G0 it0 at 690 ns, 40 min each: A (100) reached 734 ns, B (1000) reached 784 ns, so B is **2.15× faster**. Zero ConsToPrim, E<0 or other warnings on all 16 ranks.
- Differences: budget increments ≤ 2e-4 relative (E_wall 2e-3 in the first ns); at data.0293, L1 relative ρ 4e-5, p 3e-4, T 3e-3, v ≈ 1e-2 (includes a 7 ps time offset); mass-weighted |ΔY| 2e-5.
- Open item: 35 rarefied near-wall cells (ρ ≈ 4e-3 kg/m³, z ≈ 1 µm, mass fraction 1e-7) differ strongly in T. One reaches the table maximum of 150 kK in B, against 35 kK in A. Check later whether such cells add to the in-band emission (diag_emission T > 30 kK bin).
- Decision (numerics, grade N): rmax_par = 1000 for new runs. T016r continues with 100; the difference is far below the physics uncertainties.
- T016h: high G0 thin/trap it0–it4 and high G1 thin it0, built with v1d, on the 32 freed cores.

## 2026-11-18 09:04 (UTC+8) - Codex execution record: authorized T016r stop
- Followed T016r_stop.md: stopped wrapper PID 41680 and its 32 solver ranks; verified 2026-11-18T09:04:33.5388017+08:00. Reason: superseded by T016r2 (rmax_par 1000). Raw data retained; T016h not started or touched.
- Last budget t_ns: low_thin_G0=7.649923e+002, low_trap_G0=7.504993e+002, low_thin_G1=2.944895e+002. Evidence: results/T016r/stopped/; report: tasks/T016r.report.md. T016r marked done as superseded.

## 2026-11-18 09:10 (UTC+8) — T016r stopped (rmax_par 100, throttled); T016r2 started
- All-rank ConsToPrim counts over the stopped t=0 runs (v1d): low thin G0 4.4 M, trap G0 4.3 M, G1 19.1 M. All are "rhoe < 0 or T < T_CUT_RHOE": the known RKL-stage overshoot in the dense near-target layer (STATE 02:00), now counted over all ranks rather than rank 0 only. No E<0 or NaN occurred, and the later restart intervals (T016c, T016s) are warning-free. The numerical-defect item remains open (positivity of RKL stages).

## 2026-11-18 14:40 (UTC+8) — T016h high_trap_G0 it0 (v1d, first finished iteration)
- rc=0 at 800 ns. Gas absorption 295.8 µJ (= T015), radiation 69.3 µJ (T015 64.8), E_wall 44.7 µJ (46.6), M_Cu = M_src = 3.292 ng (exact). The energy residual is −0.38% (T015 −0.05%): still < 1%, but larger. The likely origin is the floor repairs of the RKL-stage overshoots (23.4 M all-rank ConsToPrim lines, the known defect). Watch this over the iterations.
- Picard it0: target-model absorption 62.2 µJ vs PLUTO 8.8 µJ (as in T015 it0); it1–it4 continue.

## 2026-11-18 14:55 (UTC+8) — T016h high_trap_G0 it0 imaging: interdiffusion closes most of the late-time gap
- L/L(300), filter b, baseline, at 400/500/600/700 ns:
  - v1d (T016h): sa 0.60/0.39/0.30/0.23; thin 0.64/0.44/0.37/0.28
  - v1 (T015): sa 0.76/0.55/0.46/0.39; thin 0.78/0.58/0.50/0.43
  - v0.1 (T014): sa 0.67/0.55/0.46/0.34
  - experiment: 0.54/0.37/0.21/0.13
- P1 τ_L (300–630 ns e-fold): sa b 256 ns / c 225 ns; thin b 292 ns; experiment 182 ns (v1: 400–600 ns).
- Interpretation (it0 only, Picard not converged): the pre-declared, parameter-free Cu–air interdiffusion moves the late decay most of the way to the experiment. A residual excess of about ×1.4–1.8 at 600–700 ns remains. Wait for it1–it4 (convergence) and low fluence before drawing conclusions.

## 2026-11-18 16:40 (UTC+8) — Cu–air diffusion v2 (Coulomb/polarisation); T016e issued
- The stiffness in rarefied Cu-rich cells came from D (2–5× χ). The hard-sphere D treated ions as hard spheres, so it was overestimated by 50–250× in the ionised plasma above about 15 kK (the registry listed only a 2–3× uncertainty — an error of judgement, corrected here).
- v2 (registry diff_cuair_model_v2, polarizability; tests/test_diffusion.py): neutral–neutral hard spheres; ion–neutral max(hard sphere, Langevin); ion–ion Chapman–Enskog Coulomb; Blanc-type mixture rule. New/old D is 1.00 in neutral gas, 0.97 at 9.4 kK (the late Cu core), and 0.02–0.004 at 20–40 kK.
- Cloud smoke test (low-res v1c, v1d exe): 0 ConsToPrim, residual −0.22%, M_Cu = M_src, energies within 0.5% of hard-sphere D, runtime −26%.
- T016e: all 6 members (v1e = v1d + tables v1c), rmax_par 1000, 64 cores. T016h and T016r2 are stopped once low_trap_G0 it0 has finished. Their finished iterations are kept as the hard-sphere-D (upper bound) sensitivity: high trap G0 it0, low thin G0 it0, low trap G0 it0.
- T016r2 low_thin_G0 it0 (hard-sphere D): rc=0, M_Cu = M_src, target-model/PLUTO absorption 28.4/15.9 µJ (it0).

## 2026-11-18 16:55 (UTC+8) — T016r2 low_thin_G0 it0 (hard-sphere D) imaging + diagnostic
- L/L(300), filter b, baseline, at 400/500/600/700/800/1000/1200 ns:
  - sa: 0.61/0.46/0.42/0.42/0.43/0.47/0.22
  - thin: 0.76/0.63/0.53/0.46/0.42/0.38/0.17
  - T015 sa: 0.68/0.53/0.44/0.40/0.19/0.12/0.09
  - experiment: 0.70/0.45/0.29/0.17/0.13/0.04/0.016
- At low fluence, the hard-sphere D gives a **late plateau / re-brightening in sa (600–1000 ns), worse than T015**, even though the thin curve decays faster than T015.
- Diagnostic: Cu I 81–97%. T_emw stays at 10–11 kK from 500 to 1200 ns, while Y_emw falls from 0.78 to 0.43: Cu is mixed into air but the emitting gas does not cool. In sa the plateau is consistent with the mixed layer becoming optically thinner as it spreads.
- Interpretation pending v1e. The hard-sphere D is an upper bound, and the low-fluence plume (12–16 kK early) is exactly where v2 reduces D. Do not draw conclusions from the hard-sphere-D low run.

## Workstation stop record (2026-11-18T17:23:01.045561+08:00)
- T016e step 2 condition met: T016r2/low_trap_G0/it0 completed rc=0, 1300 ns. T016h (wrapper 39808) and T016r2 (wrapper 38008) stopped with their descendants; each 73-process tree verified absent. No data deleted.
- Reason: superseded by T016e (v1e); finished iterations kept as hard-sphere-D sensitivity. Exact stop times and final member budgets are in results/T016h/stopped/summary.json and results/T016r2/stopped/summary.json.
- T016e_tables finished rc=0; v1c diff_model v2, all three binaries [81,145,14], finite axes/fields and manifest hashes validated. Production launch pending bookkeeping.
- Transition completed: T016e production started 2026-11-18T17:23:37 UTC+8, PID 42332, commit bc79e7d, 64 configured ranks. All six actual initial inis and v1c table hashes verified; six initial budgets present.

## 2026-11-18 22:25 (UTC+8) — T016e high_trap_G0 it0 (v1e) budget
- rc=0. Gas absorption 295.8 µJ, radiation 64.7 µJ, E_wall 46.3 µJ, M_Cu = M_src = 3.292 ng, residual −0.10%. For comparison: hard-sphere-D v1d 69.3/44.7 µJ with residual −0.38%; T015 64.8/46.6 µJ with residual −0.05%. ConsToPrim (all ranks) 23.2 M, the known RKL-stage defect.
- With the v2 D the energy budget is almost identical to conduction-only. The larger residual seen with the hard-sphere D was therefore tied to the overestimated D. The imaging (pending) will show whether the late-decay improvement survives.

## 2026-11-18 22:40 (UTC+8) — T016e high_trap_G0 it0 imaging (v1e)
- L/L(300), filter b, baseline, at 400/500/600/700 ns:
  - v1e sa: 0.75/0.53/0.38/0.28; thin: 0.70/0.49/0.40/0.34
  - hard-sphere D: sa 0.60/0.39/0.30/0.23
  - T015 (no diffusion): sa 0.76/0.55/0.46/0.39
  - experiment: 0.54/0.37/0.21/0.13
- P1 τ_L (b): sa 304 ns, thin 316 ns (experiment 182; T015 400–600).
- **With the corrected (Coulomb/polarisation) D, about half of the earlier improvement remains.** 400–500 ns is unchanged from T015; 600–700 ns is 20–30% lower than T015. Part of the large gain seen with the hard-sphere D came from the overestimated D in the ionised core. This is reported as is.
- The late-time excess is now about ×1.8–2.2 at 600–700 ns (it0, Picard not converged). Next: low-fluence v1e it0, then the Picard iterations.
- T016e high_thin_G0 it0 (v1e), filter b, sa at 400/500/600/700 ns: 0.91/0.43/0.34/0.26 (T015: 0.81/0.46/0.36/0.30); thin: 0.79/0.46/0.37/0.30 (T015: 0.81/0.59/0.45/0.39). P1 sa 261 ns, thin 287 ns. Diagnostic: Y_emw 0.70 at 700 ns (T015 0.83), so mixing is real but moderate; T_emw 10.7 kK (T015 10.3 kK). The core does not cool faster, and the light curve gains come mainly in the thin transfer. The thin and trap members agree.

## 2026-11-19 01:05 (UTC+8) — Shock trajectory P4 (scripts/shock_track.py; front = outermost p > 1.2 p_amb on the axis)
- High fluence, experiment (Fig. 3d) R(t): 238/405/509/584/649/741 µm at 87/210/327/444/557/738 ns; late speed ≈ 542 m/s.
  - it0 (T015 or T016e, identical): 277/435/537/691/811 µm at 100/200/300/500/700 ns. That is about 12–15% too far at 500–700 ns; v_late 636 m/s.
  - **Picard it2 (T015): 231/375/461/590/713 µm; at 500/700 ns this is within 3–4% of experiment (≈ 614/720); v_late 583 m/s.**
  - The converged energy deposition is therefore consistent with an observable that does not depend on the emission. The late-time overbrightness at high fluence is **not** caused by excess absorbed energy; it points to cooling/emission (energy partition inside the plume, radiative losses).
- Low fluence (T016r2 it0, hard-sphere D): 172/317/398/521/628 µm at 100/200/300/500/700 ns; experiment ≈ 300/400/490/560/680. The model is **slower early** (−40% at 100 ns, −20% at 200–300 ns) and within about 8% late; v_late 470 m/s (experiment ≈ 521). The early energy deposition at low fluence is too low or comes too late (ignition timing). T015 Picard it1–it2 lowered the low-fluence gas absorption to ≈ 44 µJ, so the converged low-fluence shock would be slower still. This is the open item for the low series, independent of the late-time emission.

## 2026-11-19 01:55 (UTC+8) — T016e low G0 it0 and high_trap it1 budgets
- low thin/trap G0 it0 (v1e): gas absorption 87.5/87.7 µJ, radiation 22.7/21.1 µJ, wall 23.4/23.8 µJ, M_Cu = M_src = 1.016 ng, residual −0.64/−0.59% (still < 1%, larger than at high fluence). Target model vs PLUTO absorption 28.4/15.9 µJ (as in T015 it0). Shock track identical to the hard-sphere-D run (172/317/398/521/628 µm): diffusion does not affect P4.
- high_trap_G0 it1: gas absorption 254.3 µJ, radiation 52.3, wall 63.0, M_Cu = 1.613 ng, residual −0.11%; target model vs PLUTO 32.3/10.9 µJ (converging as in T015).
- Imaging of the low it0 runs is in progress.

## 2026-11-19 02:15 (UTC+8) — T016e low G0 it0 imaging (v1e)
- L/L(300), filter b, baseline, at 400/500/600/700/800/1000/1200 ns:
  - thin member: sa 0.63/0.44/0.32/0.24/0.32/0.28/0.20; thin 0.90/0.76/0.64/0.51/0.45/0.29/0.18
  - trap member: sa 0.65/0.46/0.35/0.27/0.32/0.40/0.25
  - experiment: 0.70/0.45/0.29/0.17/0.13/0.04/0.016
  - T015 sa (thin member): 0.68/0.53/0.44/0.40/0.19/0.12/0.09
- **400–700 ns sa is now close to the experiment (within 10–40%)**, better than T015 and the hard-sphere D. From 800 ns a **re-brightening appears in sa only**: the thin curve decays monotonically to 0.18 at 1200 ns, while sa rises from 0.24 to 0.32–0.40. Over the same interval the diagnostic shows Y_emw 0.85 → 0.69 and T_emw 10.0 → 9.2 kK. The Cu is diluted by interdiffusion, the self-absorption (trapping) drops faster than the emissivity, and the line emission "de-traps". This is physically possible in the model, but it is absent in the experiment and in T015. Possible causes:
  (a) The sa transfer uses the r-rebinned, z-rebinned fields; volume averaging of mixed Cu/air cells may under-estimate the line opacity of the residual cold Cu shell. Check with the native-grid transfer.
  (b) There is no Stark/van der Waals broadening: at late times, with ne ≈ 1e23 m⁻³ and n_air large, collisional broadening of Cu I 510–522 nm can exceed Doppler. That changes the optical depth of the de-trapping layer (Voigt is on the v1 list).
  (c) The cold outer Cu vapour (T < 5 kK) absorbs in the lower-level lines (Cu I 510.6 nm, lower level 3d⁹4s² ²D5/2 at 1.39 eV); its population depends on the mixing.
- Next: test (a) and (b) in the imaging code alone, without new hydro. This is diagnostic, and it is a declared v1 item (Voigt), so no tuning is involved.

## 2026-11-19 03:05 (UTC+8) — first native-grid image (T016e high_thin_G0 it0)
- L/L(300), filter b, baseline, at 100/200/400/500/600/700 ns:
  - native sa: 4.26/2.18/0.63/0.47/0.37/0.28; v0 sa: 5.01/2.10/0.91/0.43/0.34/0.26
  - native thin: 2.80/1.63/0.67/0.49/0.39/0.33; v0 thin: 4.64/1.55/0.79/0.46/0.37/0.30
  - experiment: 1.27/1.40/0.54/0.37/0.21/0.13
- P1 τ_L native: sa 319 ns, thin 334 ns (v0: 261/287 ns).
- The rebinning artefact mainly affected the early and 400 ns frames, where steep gradients sit (the spurious 0.91 bump at 400 ns disappears). Late frames are about 10% **brighter** with native transfer. The high-fluence late excess is therefore not an imaging artefact. All earlier light-curve comparisons are to be repeated with image_v1 (T016i running).

## 2026-11-19 04:05 (UTC+8) — native images, high fluence it0 → it1 (v1e)
- sa at 400/500/600/700 ns: thin member it0 0.63/0.47/0.37/0.28, it1 0.72/0.56/0.43/0.37; trap member it0 0.63/0.51/0.38/0.29, it1 0.72/0.57/0.45/0.39. Experiment 0.54/0.37/0.21/0.13.
- As expected from T015, **the decay becomes slower** as the Picard iteration converges (less Cu, same gas energy, hotter core). At it1 the late excess is ×2–3. it2–it4 are running; the converged answer will likely be at least as slow.
- Taken together with P4 (energy deposition consistent at convergence), the remaining high-fluence discrepancy sits in how the absorbed energy leaves the emitting Cu core: radiation, 3D/turbulent mixing, or the emission model at 10–25 kK (Cu II lines, Stark broadening). This is the main open physics question for the paper.

## 2026-11-19 04:40 (UTC+8) — low fluence native images (T016e it0, v1e): late re-brightening was an imaging artefact
- sa, filter b, baseline, at 400/500/600/700/800/1000/1200 ns:
  - thin member: native 0.62/0.42/0.30/0.20/0.16/0.12/0.10; v0 0.63/0.44/0.32/0.24/0.32/0.28/0.20
  - trap member: native 0.64/0.45/0.33/0.24/0.19/0.14/0.12
  - experiment: 0.70/0.45/0.29/0.17/0.13/0.04/0.016
- **With native transfer the 800–1000 ns re-brightening disappears, so it was an artefact of the rebinning.** For 400–800 ns, low fluence sa now agrees with experiment within ±10–25%. P1 τ_L: sa 260 ns (thin member) / 291 ns (trap member) vs 218 ns in experiment. At 1000–1200 ns the model is still too bright, by ×3–6.
- The thin transfer decays more slowly (P1 621–721 ns). The sa scenario is the physically consistent one; thin is an upper bound and is kept for reference only.
- Status: low fluence (it0, Picard not yet converged; T015 converged at it1) reproduces the trend to 800 ns. At high fluence (it1) the late excess is still ×2–3.

## 2026-11-19 07:55 (UTC+8) — image_v2 (Voigt/Stark, verified widths), high_thin_G0 it0/it1
- With Stark broadening the in-band Cu I lines become **almost optically thin**: sa ≈ thin within 1–6% after 200 ns.
- L/L(300) at 100/200/400/500/600/700 ns:
  - it0: v2 sa 2.64/1.57/0.66/0.48/0.39/0.32; v1 sa 4.26/2.18/0.63/0.47/0.37/0.28
  - it1: v2 sa 3.59/1.37/0.92/0.81/0.66/0.56; v1 sa 4.35/1.97/0.72/0.56/0.43/0.37
  - experiment: 1.27/1.40/0.54/0.37/0.21/0.13
- P1 (it1, b): sa 700 ns, thin 713 ns.
- Consequences:
  - The early overshoot is reduced, which is better at 100–200 ns.
  - The late decay now follows the thin emissivity and is **slower**: ×4 at 700 ns for it1. The "sa decays faster than thin" behaviour that helped in v1 came from neglecting the Stark width (Doppler-only cores saturate), so it was an artefact of the line-profile model.
  - With verified physics the conclusion is sharper. **The emitting Cu itself stays hot and dense too long.** The discrepancy sits in the plume energetics and evolution (energy loss, expansion, mixing), not in radiative transfer.
- Next physics candidates, to be evaluated quantitatively before any implementation:
  (1) radiative loss: thin NEC with complete Cu I/II line lists (Kurucz) plus continuum, against the present NIST-only NEC; is the core cooling rate low by ×2?
  (2) 2D-axisymmetric limitations: 3D instabilities and turbulent mixing at the contact surface. This cannot be added without free parameters, so it is a declared limitation and a sensitivity.
  (3) the target-gas coupling: late vapour supply, and the condensation/recondensation sink of Cu at the cold target (currently one-way, no back-flux to the target).

## 2026-11-19 08:35 (UTC+8) — Cu-core energy budget (scripts/core_energy.py, Y > 0.5)
- High fluence, T016e it1 (thin/trap members similar), at 300/500/700 ns:
  - M_Cu,core 1.01/0.83/0.66 ng; E_int 45/29/19 µJ; Cu-mass-weighted T 16.5/12.0/9.4 kK
  - P_rad 70/30/18 W; "other" (dE/dt + P_rad) −60/−15/−29 W
- Low fluence, it0, at 300/500/700/1100 ns: T 10.5/8.8/7.2/5.6 kK; P_rad 24/10/4/0.7 W; "other" −6/−5/0/−5 W.
- **The core is not heated by its surroundings.** "Other" is negative: expansion work, conduction and interdiffusion take energy out. Radiation is 35–60% of the losses.
- **The core does cool strongly: 16.5 → 9.4 kK between 300 and 700 ns at high fluence.** Yet the 510 nm emission falls slowly, because the Cu I fraction rises steeply as T falls (Cu II → Cu I recombination) and offsets the falling Boltzmann factor.
- The decisive difference is the **temperature at the normalisation time**. At 300 ns the high-fluence core is at about 16.5 kK and mostly Cu II, where cooling *raises* the Cu I in-band emissivity. The low-fluence core is at about 10.5 kK and mostly Cu I; there cooling lowers the emission, and the low series indeed agrees with experiment to 800 ns.
- The high-fluence core is too hot: its specific energy is E_int/M_Cu ≈ 44 MJ/kg at 300 ns, against ≈ 20 MJ/kg at low fluence. The converged high-fluence Picard state has little Cu (≈ 0.8 ng; plasma shielding cuts target absorption to about 20 µJ) and the same gas energy, so less ablated Cu carries the same energy. Hypotheses to test, each parameter-free:
  (a) **The ablated mass is too small.** The target model (surface evaporation with a 0.9 T_c cap, one-way coupled) has no phase explosion / volumetric boiling. The over-cap energy is only 2.8 µJ at it2, so the cap alone does not explain it. Check against literature crater/ablated-mass data at about 200 J/cm² (data, not fitting).
  (b) The emission vs T relation (Cu I/Cu II balance) depends on the Debye–Hückel IP lowering and on the partition-function truncation. These are model choices with known alternatives (Stewart–Pyatt); run a sensitivity check on the same fields with imaging only.

## 2026-11-19 09:05 (UTC+8) — stop/restart under standing authorization
- T016e/low_thin_G1/it0 is stalled at about 7 ns/h (496/1300 ns, Nrkl at the 63 cap). The member is stopped and restarted from its last output with rmax_par 4000, a numerics change of the same class as T016s, documented as a mid-run change. The data of the stopped run is kept.

## Workstation stop record ? 2026-11-19 10:36 UTC+8
- Under Claude T016d_stop.md/standing authorization, stopped only T016e/high_trap_G0/it3 (MPI14908+8ranks), reason prolonged dt about3.7e-5ns. Last budget83.49998ns,last checkpoint33 at82.499055505873ns.61 other solver/MPI processes and T016e wrapper preserved; all original data retained. Exact evidence: results/T016e/high_trap_G0/stopped/. Isolated v1f20-minute diagnostic follows under T016d.

## Workstation stop record ? 2026-11-19 11:06 UTC+8
- Claude T016d_vmax.md/standingauthorization: stopped only T016e/high_thin_G0/it3,MPI6744+8ranks,for dt stall near83ns. Lastbudget83.49996ns,lastcheckpoint33 at82.49686ns.52othersolver/MPI processes and batch preserved;data retained. v1f20minute diagnostic finished;v1g10minute follow-up prepared.

## 2026-11-19 11:40 (UTC+8) — T016d resolved; low fluence with image_v2
- Stall cause: diverging **table sound speed**, not velocity (|v|max 23.5 km/s; dt 4e-14 s; PLUTO Mach 26). Above the table T maximum, ∂e/∂T → 0. Fix v1h: Γ1 clipped to [1, 5/3] (thermodynamic bound). Smoke test bit-identical to v1d. T016f checks it on the stalled checkpoint and then continues high it3–it4.
- Low thin G0 it0 with image_v2 (Stark), sa at 100/200/400/600/800/1000/1200 ns: 3.06/1.13/0.94/0.62/0.31/0.19/0.13. image_v1: 6.23/1.94/0.62/0.30/0.16/0.12/0.10. Experiment: 0.76/0.93/0.70/0.29/0.13/0.04/0.016.
- **With the verified Stark widths the low-fluence late decay is slower too** (P1 sa 548 ns, against 260 ns with v1 and 218 ns in experiment). The good v1 agreement at 400–800 ns relied partly on Doppler-only line saturation. The early overshoot (100–200 ns) is much reduced (3.1/1.1 vs 6.2/1.9).
- Overall conclusion with the most complete physics so far (conduction + Coulomb/polarisation interdiffusion + native transfer + Voigt/Stark): both fluences reproduce the 200–400 ns normalised level better than before. Both decay too slowly after 400 ns (τ_L ≈ 550 ns low, > 1 µs high at Picard it2, against 182–218 ns in experiment). The common cause is that the emitting Cu stays hot and Cu I-rich for too long. This points to the plume energetics: too little ablated mass (high), missing 3D mixing/cooling, or the Cu I/II balance via IP lowering. Each needs an independent check, not tuning.

## 2026-11-19 12:10 (UTC+8) — high-fluence it3 stall: decision
- v1h (Γ1 bound) did **not** remove the stall: dt was still 3.9e-5 ns after 20 min at 82.96 ns. Neither the source limits (≈3e-3 ns) nor the cell-centred CFL (|v| ≤ 23.5 km/s) nor the parabolic step (Nrkl 2) explain this dt. The remaining candidate is a near-zero density in a reconstructed interface state, which inflates the Riemann-solver signal speed. Both members stall at the same 83 ns, so it is deterministic.
- **Decision (time-box):** high-fluence results are reported at **Picard it2**, with the convergence status stated: target-model/PLUTO absorption 22.3/17.5 µJ, 27% apart. The direction is clear from it0 → it2: less Cu, hotter core, slower late decay. The converged answer would be at least as slow, so the qualitative conclusion does not depend on it3–it4. The stall is logged as an open numerical defect (interface-state positivity). A fix would be a Riemann-solver diagnostic plus a positivity-preserving reconstruction or a documented density floor with A/B, after the physics questions are settled. The v1h Γ1 bound is kept: it is physically correct and inactive in normal cells.
- Low fluence is **converged at it1**: 21.0/21.6 µJ (3%), gas 44.0 µJ, M_Cu 0.52 ng. Low it1 is the reference low-fluence result.

## 2026-11-19 12:30 (UTC+8) — ablated-mass hypothesis: first literature evidence (qualitative)
- Gojani et al. 2008 (Appl. Surf. Sci. 255, 2777): Cu, **single pulse**, 7 ns, 532 nm, air, confocal crater depth ≈ 5 µm at ≈ 20 GW/cm² (Fig. 4b, c1). The spot size is still to be verified.
- Our case: peak intensity ≈ 210 J/cm² / 60 ns ≈ 3.5 GW/cm², 1064 nm, 60 ns. This is not a matched condition (pulse width, wavelength, intensity all differ), so no number is transferred.
- Order-of-magnitude check: the model's ablated vapour (0.8–1 ng at high fluence) corresponds to about 0.25 µm over the 1/e² spot. A crater of several µm (if confirmed at our conditions) implies removed mass of order 10 ng, but most of it may be melt displacement/ejection rather than vapour. This **supports, but does not prove**, that the surface-evaporation target model under-predicts the vapour mass (phase explosion / volumetric boiling missing).
- Status: candidate (a) remains the leading hypothesis for the high-fluence over-heated core. A parameter-free test is possible: a phase-explosion criterion (T_s → ~0.9 T_c with explosive boiling of the superheated layer, energy-limited). It would be pre-declared as a v1 model extension with its own uncertainty, and its effect checked first on the target model alone (mass and energy budget), before any hydro run.

## 2026-11-19 12:55 (UTC+8) — low fluence converged (it1), reference imaging v2
- low_trap_G0 it1 (converged Picard), image_v2 (native + Stark), filter b baseline, sa at 100/200/400/500/600/800/1000/1200 ns:
  - it1: 3.18/1.27/0.84/0.65/0.46/0.24/0.15/0.09
  - it0: 3.08/1.14/0.96/0.81/0.67/0.37/0.23/0.16
  - experiment: 0.76/0.93/0.70/0.45/0.29/0.13/0.04/0.016
- P1 sa 344 ns, against 218 ns in experiment (it0: 653 ns).
- **At low fluence the converged Picard state decays much faster than it0.** Gas absorption halves (88 → 44 µJ) while M_Cu halves (1.0 → 0.52 ng): the core is cooler and starts nearer the Cu I regime. From 400 to 800 ns the model is ×1.2–1.9 the experiment; the excess grows to ×4–6 at 1000–1200 ns. The early overshoot at 100 ns (×4) remains.
- At high fluence (it2, not converged) the trend is the opposite: slower with iteration. Both behaviours fit the core-energy picture: the specific energy of the emitting Cu at t_ref sets the decay.

## 2026-11-19 14:30 — low-fluence G1 (stopped at ~557 ns): shock and core
- Shock on the axis vs Fig. 3d, low fluence: −45% at 100 ns, −25% at 200 ns, −20% at 300 ns, −14% at 430–480 ns. The G0 result had the same pattern ("early shock 40% too slow"), so the deficit does not depend on the grid. It points to too little early energy or momentum delivered to the gas (vapour mass and launch), which T018 (phase explosion) tests.
- Cu core: M_Cu 7.4e-13 kg at 100 ns, falling to 4.9e-13 kg at 400 ns as Cu moves to Y < 0.5; T_mw 21.6 → 9.6 kK.

## 2026-11-19 15:45 — T018 low ConsToPrim warnings: diagnosis from the 190 ns field extract
- On the axis between the target and z ≈ 78 µm there is pure Cu vapour (Y = 1) at ρ ≈ 0.01 kg/m³, p ≈ 200 Pa (1 bar ≈ 0.1 code units, so this is ~500× below the plume pressure) and T held at the 200 K table floor, flowing up at 1.45 km/s. The source has stopped (M_src flat at 1.65e-12 kg since ~140 ns), so this is the over-expanded rarefaction cavity that the vapour leaves behind as the plume moves off. The plume above (5–6 kK, ~50–100 kPa) flows back into it (vz ≈ −0.65 km/s).
- The warnings sit on the steep interface between the cavity and the returning plume (z ≈ 75–90 µm). They are table-floor hits of an adiabatically cooled vapour. In reality that vapour would be supersaturated and would condense (nanoparticles), which the model does not include. The extra energy from the floor is ≲ 1e-12 J: the cavity mass is ~4e-16 kg and holds 200 K vapour. That is negligible against E_src = 3.6e-6 J. Neither the budget nor the emission (from regions > 4 kK) is affected.
- Disposition: physical rarefaction plus a model limit (no condensation). Not a solver defect; nothing is tuned. Check again with the extract after 250 ns. Phase explosion raises the low-fluence vaporised mass to 1.62e-12 kg (PE part 0.63e-12 kg); PLUTO M_Cu agrees (1.65e-12 kg).

## 2026-11-19 16:40 — T018 low dt limit = explicit wall exchange; build v1i
- All-rank `lip dt-limit` lines (results/T018/inflight_dt/): kind 1 (wall) in the first gas cell, ρ ≈ 2e-3 kg/m³, T ≈ 360 K, dt_src ≈ 8.4e-12 s. After the plume leaves, the cell next to the target is the rarefied cavity found at 190 ns, and its tiny heat capacity makes the explicit conductive exchange with T_s stiff.
- v1i: backward Euler in T for the same flux κ(T_s − T)/(dz/2), with κ frozen, solved by bisection, so T_n lies between T and T_s. The explicit limit LIP_LIM(1) is removed.
- Cloud A/B (smoke grid, v1pe_low, 300 ns): all budget terms within 0.6% (E_wall within 2% at 50 ns, then < 0.2%); 3.5× faster; ConsToPrim 160k → 103k. Card T018b.
- Open question: the high-fluence it3 stall (T016d/T016f, dt ~4e-14 s) could share this cause or not. Check after T018b (is `lip dt-limit kind 1` there?).

## 2026-11-19 17:30 — T018b low it0 (v1i, phase explosion on): no breakdown
- Budget at 1300 ns, compared with T016e low it0 (v1e, PE off, the same Picard iteration and tables):

  | term | PE on | PE off |
  |---|---|---|
  | gas absorption | 0.017 µJ | 87.5 µJ |
  | radiated | 0.019 µJ | 22.7 µJ |
  | escaped laser | 287 µJ | 212 µJ |
  | M_Cu | 1.65 ng | 1.02 ng |
  | E_src | 3.6 µJ | 2.3 µJ |

  The laser passes through, and the plume never becomes a plasma.
- Cloud check, same v1i build on the smoke grid: v0_low breaks down at 55–70 ns (gas absorption 48 µJ by 70 ns, axis T 40–65 kK), while v1pe_low does not (0.12 µJ). The cause is therefore the phase-explosion source, not the build or the grid. With PE on, the first gas cell holds dense vapour (ρ ≈ 130–165 kg/m³ against 20–28 kg/m³) at only 6.3–6.7 kK. The laser energy absorbed there is shared by ~6× more mass, so the inverse-bremsstrahlung runaway does not start.
- Reading against PREREG (v1 extension, 13:30): the declared expectation (lower core specific energy, faster late decay at high fluence) cannot be tested at low fluence, because there is no emitting core. The all-vapour upper bound is **inconsistent with the experiment at low fluence**: the measured plume is bright. Breakdown is therefore strongly non-linear in the vapour mass near the wall. Physically, much of the explosive-boiling ejecta are droplets, which neither absorb nor dilute like vapour, and that is exactly what the upper bound leaves out. The true vapour fraction lies between the two bounds. Nothing is tuned toward it. The high-fluence test (T018c) continues as declared.
- Shock (T018b low thin it0), axis, against Fig. 3d: 78 µm at 100 ns (exp 295), 232 at 273 ns (exp 456), 345 at 480 ns (exp 580); that is −74%, −49%, −41%. PE off (G1) was −45%, −20%, −15%. Without breakdown the gas gets no laser energy, so the blast is far weaker. This confirms, independently of any image, that the all-vapour PE bound is inconsistent with the low-fluence experiment.

## 2026-11-19 18:15 — low fluence Picard convergence confirmed (T016e it2, stopped at ~880 ns)
- Target absorption, model vs PLUTO: it1 21.0/21.6 µJ (2.6%); it2 21.45/22.15 µJ (3.2%), thin and trap alike. From it1 to it2 the PLUTO target absorption changes by +2.7% and the gas absorption by −4% (44.0 → 42.1 µJ). The low-fluence PE-off result at it1 is converged, and it1 stays the reported member.

## 2026-11-19 18:35 — T016e inventory residual review (PLAN §4.2 asks for < 0.1%)
- Low it1 thin, residual against time: 0.000% at 100 ns, 0.003% at 200 ns, −0.044% at 400 ns, −0.25% at 800 ns, −0.31% at 1300 ns. The other low members are similar; high thin it0 reaches −0.15%. The residual only builds up once the plume is cooling (> 400 ns). By then the laser absorption, the source and the shock energetics are fixed, so it does not touch the Picard criterion or the shock metric. It is negative (missing energy) and smooth.
- Likely origin, not yet proven: the budget measures internal energy through the table e(ρ,T,Y), while PLUTO carries ρe through its own PVTE inversion. In a slowly cooling plume, interpolation differences between the two accumulate. A boundary outflow is unlikely, because the shock (~800 µm at 1300 ns) is still inside the 1200 × 1500 µm domain.
- Disposition: this is a documented deviation from §4.2. It is accepted for v1e results because it is < 0.35% and develops late, and T016e is closed (done). Open check, low priority: recompute E_internal with PLUTO's own e for one late snapshot to confirm the origin.
- ConsToPrim: 123.1M lines over the completed T016e runs, the known RKL-stage overshoot defect (still open), already documented.
- T018b closed (done): 4 iterations, 20 postprocessing commands, 8 image sets. The P1 fits that are negative or missing, and the P2 values that are unavailable, follow from the absent emission (no breakdown) and are not imaging defects. 27.1M ConsToPrim lines, mostly in ranks 0–1 near the axis/wall: the rarefaction-cavity floor hits (STATE 15:45).

## 2026-11-19 21:00 — T018c high it0 (v1i, PE on): breakdown occurs, delayed
- Budget at 800 ns, PE on vs T016e high it0 (PE off; older build with the explicit wall), thin:

  | term | PE on | PE off |
  |---|---|---|
  | gas absorption | 276 µJ | 296 µJ (−7%) |
  | radiated | 64 µJ | 71 µJ |
  | escaped laser | 109 µJ | 92 µJ |
  | target absorption, PLUTO | 12.0 µJ | 8.8 µJ |
  | M_Cu | 8.30 ng | 3.29 ng (2.5×) |
  | E_src | 18.8 µJ | 10.3 µJ |

  Trap gives the same numbers.
- Breakdown is delayed: gas absorption at 30 ns is 0.004 µJ against 4.3 µJ, and at 40 ns 15 against 38 µJ. From 50 ns on it catches up. At high fluence the laser intensity overcomes the dilution that suppressed breakdown at low fluence.
- Rough plume specific energy, (E_gas_abs + E_src)/M_Cu (an upper bound, since air shares it): 35 MJ/kg with PE on, 93 MJ/kg with PE off. This points in the direction PREREG declared (lower core specific energy), but the declared test is the core_energy diagnostic at t_ref plus the late decay in image_v2. Those are pending (T018c_post_it0_01) and are judged when they arrive.
- E_wall 20 vs 50 µJ: partly a build difference (v1i implicit vs explicit wall), so it is not attributed to PE.
- ConsToPrim: 17.9M / 17.5M lines (thin/trap).
- Picard: target model 0.158 × 396.7 = 62.6 µJ against PLUTO 12.0 µJ, which is it0 with no shielding. it1 is running.
- T018c high it0 diagnostics (thin; trap similar):
  - Cu core (Y > 0.5) at t_ref = 300 ns: M_Cu 6.33 ng, specific internal energy 10.8 MJ/kg, T_mw 5.6 kK. PE off (T016e high it2): ≈ 44 MJ/kg, 16.5 kK. This is the **PREREG expectation "lower core specific energy at t_ref": met** (it0; to be confirmed at it2).
  - The core cools 5.6 → 4.2 kK and its radiated power falls 73 → 18.5 W from 300 to 800 ns, a factor 4. With PE off the 510 nm emission stayed flat (Cu II → Cu I recombination). This points to a faster late decay, the second expectation; the declared metric is image_v2 P1/L(t), pending.
  - Shock vs Fig. 3d: +5…+14% over 90–760 ns (too fast), 0% at 937 ns. it0 has no shielding (gas absorption 276 µJ). PE off at it2 was within 3–4% at 500–700 ns. Judge at it2.
- T018c high thin it0 image_v2, filter b, L normalised at 300 ns, at 400/500/600/700 ns:

  | case | 400 | 500 | 600 | 700 | P1 |
  |---|---|---|---|---|---|
  | PE on, it0 | 0.78 | 0.69 | 0.64 | 0.58 | 724 ns (sa 683 ns) |
  | PE off, T016e it2 | 0.84 | 0.81 | 0.72 | 0.66 | > 1 µs |
  | experiment | 0.54 | 0.37 | 0.21 | 0.13 | |

  PREREG "faster late decay" holds in direction but is small: the gap to the experiment closes by only ~10–15%. Phase explosion does not explain the slow high-fluence decay. The model still decays 3–4× too slowly at 600–700 ns. To be confirmed at it2. The leading remaining suspects are outside the vapour mass: emission physics (Cu I upper-level populations, Stark/IP lowering) or plume cooling (3-D mixing and radiation transport beyond the NEC approximation). Nothing is tuned.

## 2026-11-20 02:00 — T018c high it1 (PE on, shielding from it0)
- Target absorption, model vs PLUTO: 34.6 vs 14.1 µJ (thin; trap 14.3), so |Δ| = 145%. Not converged; it0 was 422%.
- With shielding the vaporised mass drops 8.08 → 3.40 ng (PE part 4.88 → 1.76 ng). Gas absorption 262 µJ (it0 276), radiation 53 µJ (64), PLUTO M_Cu 3.86 ng. At it1 the PE vapour mass is therefore close to the PE-off it0 value (3.29 ng). Whether the plume still differs from PE off depends on the converged iterate.
- it2 is running (started ~01:45). If it2 is still > 10%, extend to it3–it4 on v1i. The it3 stall of T016d was on the older build; check whether it recurs.

## 2026-11-20 03:00 — phase-explosion extension is internally inconsistent (choked-flux violation); PE results withdrawn
- Field extract at high trap it2, 65 ns, on the axis: the first gas cell holds ρ = 7.06e3 kg/m³ (close to solid Cu, 8.96e3) at 10.6 kK and p ≈ 1.5 GPa. The next cells are near vacuum (ρ ~ 1e-4 kg/m³ at the T floor) with |v| up to 3.7e6 m/s, which drives dt to 1e-4 ns. Both it2 members stall at ~63 ns this way.
- Cause, from the source profile on the axis (it1 and it2 identical; trans = 1): mdot = 1.47e6 kg m⁻² s⁻¹ at 50–60 ns, while the Knight sonic state at T_cap gives ρ_v u_v = 68 × 1010 = 6.9e4 kg m⁻² s⁻¹. **The PE term injects 21× the choked (M = 1) evaporation flux.** The Knight layer at M = 1 is the maximum vapour mass flux a surface at T_s can emit. The declared model (registry phase_explosion_model: "m_pe at T_cap with the Knight state") therefore violates its own boundary condition. The excess mass, with no momentum source next to a reflecting wall (v1 correction), piles up in the first cell until it reaches near-solid density.
- Consequence: the extra ejecta above the choked flux cannot be vapour in this framework. Physically they are condensed-phase droplets (Bulgakova & Bulgakov 2001; Miotello & Kelly 1999), the fraction the extension declared "not modelled". A consistent version caps the injected vapour at ρ_v u_v(T_cap) and books the rest as droplets that leave the gas. **At T_s = T_cap this is identical to PE off for the gas** (mdot = Knight flux at T_cap either way). Phase explosion therefore cannot raise the vapour mass in a sonic Knight-layer model.
- Withdrawn: the T018b low-fluence "no breakdown" result (the same pile-up: 130–165 kg/m³ in the first cell) and the T018c high it0/it1 differences (lower core specific energy, slightly faster decay). They are artefacts of the flux violation and are kept only as records. PREREG verdict: the declared extension is **invalid as specified**. Its premise (more vapour) is unreachable without violating choked flow. That answers the hypothesis "under-predicted vapour mass via phase explosion" in the negative.
- Status: T018c it2 is stopped (stalled, and superseded by this diagnosis). High fluence stays reported with PE off (T016e it2). The slow high-fluence decay remains open; the next suspects are emission physics and plume cooling, not vapour mass.

## 2026-11-20 06:00 — T019 split, high fluence (T016e high thin it2, image_v2, filter b, baseline)
- All parts normalised to the total at 300 ns, thin (sa within 0.01):

  | part | 200 | 300 | 400 | 500 | 600 | 700 ns |
  |---|---|---|---|---|---|---|
  | total | 1.43 | 1.00 | 0.84 | 0.81 | 0.72 | 0.66 |
  | continuum | 0.25 | 0.13 | 0.082 | 0.058 | 0.043 | 0.034 |
  | lines | 1.19 | 0.87 | 0.76 | 0.75 | 0.67 | 0.62 |
  | experiment | 1.40 | 1.00 | 0.54 | 0.37 | 0.21 | 0.13 |

- The model band is line-dominated (87% at 300 ns, 95% at 700 ns), and the **Cu I lines are what stays flat** (−29% from 300 to 700 ns). The continuum falls by 3.7× over the same interval, close to the experiment's 7.7×. The open discrepancy is therefore in the Cu I line emissivity of the cooling, recombining core: either the LTE line source (populations of the 3.8 eV upper levels while Cu II → Cu I recombination feeds the atoms) or a core that cools/dilutes too slowly. IP lowering is already Debye–Hückel in the tables.
- Next (to be pre-declared before running): (a) an LTE-validity diagnostic for the upper levels (McWhirter and recombination-time criteria on the emission-weighted n_e, T per frame); (b) the emission-weighted T, n_e, n_CuI per frame, to separate the "too hot" and "too many atoms" explanations. Diagnostic only; no emissivity change without a separate pre-registration.
- T019 low fluence (T016e low thin it1), normalised to the total at 300 ns. Lines are 97% of the band. Lines at 400/500/600/800/1000 ns: 0.83/0.64/0.45/0.23/0.13 (experiment total 0.70/0.45/0.29/0.13/0.04); continuum 0.017…0.001. At low fluence the Cu I lines do decay, about 1.5–3× slower than the experiment. At high fluence they stay flat. T019 closed (done).

## 2026-11-20 07:20 — T020 LTE-validity diagnostic (PREREG 2026-11-20 06:50), read as declared
- High fluence (T016e high thin it2), emission-weighted over the band. τ_3br/(0.1 t) is 1.46 at 300 ns, 1.34 at 400 ns, then 0.70 / 0.52 / 0.38 / 0.31 at 500 / 600 / 700 / 800 ns. n_e/n_McWhirter is 13–21. Low fluence (it1): τ_3br/(0.1 t) is 0.06–0.13 from 300 ns on, and n_e/n_McW is 7–25.
- **Declared reading: LTE stands.** The criterion τ_3br > 0.1 t fails only at 400 ns among the high frames ≥ 400 ns and holds from 500 ns on. At high fluence, 300–400 ns is marginal, within a factor 1.5 of the bound. McWhirter is satisfied everywhere. Per the declaration, a time-dependent ionisation post-processing is **not** triggered. The remaining suspect is the plume cooling and dilution in the hydro.
- What makes the high-fluence signal flat: the emission-weighted T is 25 kK at 300 ns and still 16–17 kK at 700–800 ns (low: 14 → 10 kK). The band power at 300–400 ns is 68% **Cu II lines**, then hands over to Cu I (Cu I 0.12 → 0.50, Cu II 0.68 → 0.41 from 300 to 800 ns). Cu II emission fading and Cu I recombination emission rising compensate each other, so the sum stays flat. At low fluence Cu I dominates from 200 ns on (74–99%), and the emitter is 10–14 kK.
- Consequence: at high fluence the emitting plasma stays too hot for too long. If the experiment's core cooled faster, the Cu II → Cu I handover would happen earlier and the band would decay. The question becomes: why does the high-fluence core stay at 16–25 kK for 300–800 ns in the model? Candidates are radiative cooling (NEC, thin vs trap barely differ), conduction and mixing at the plume edge, and the 2-D assumption. To be pre-registered before any run. T020 closed (done).

## 2026-11-24 08:30 — user chose option 1: droplet evaporation bounds (v1j, card T021)
- PREREG_v0 "v1j extension declared 2026-11-24 08:20". The upper bound evaporates removal − vapour (≈ 6.4 / 5.2 ng high/low, from Semerok's 2000 µm³/mJ) into the Cu core between the end of the pulse and t_ref. The droplets arrive at rest with e_vap(T_cap) − L_v. The lower bound is the existing PE-off runs.
- Cloud checks:
  - With DROP_M = 0, v1j is bit-identical to v1i.
  - With droplets on (smoke grid), breakdown and gas absorption are unchanged. The net heat sink is ≈ 8 µJ by 300 ns, about 1.3 MJ/kg: droplets at T_cap already carry e_vap(T_cap), and only L_v − e_vap(T_cap) is drawn. M_Cu rises from 1.8 to 5.4 ng.
- The release window was moved from the pulse FWHM to the end of the pulse–t_ref after the first smoke test. Pulse-time release suppressed breakdown, which confounds ignition with the late-cooling question. This is recorded in PREREG before any production run.

## 2026-11-24 09:10 — comparison figures (T022 data): results/figures/{current_vs_experiment,frames_high,frames_low}.png
- High-fluence shock (T016e high thin it2) vs Fig. 3d: −12% at 125 ns, −5…−7% at 200–480 ns, −2…−6% at 540–840 ns. T015 high trap it2, the source of the earlier "3–4%" statement, gives the same at these times; the front is grid-quantised.
- Frame geometry, an observation that is **not yet a metric**: the experimental band-b plume centroid sits zc ≈ 350–430 µm above the frame bottom (high fluence, 150–730 ns), with σ_z² 4–5× the model's. The model plume sits on the target, zc ≈ 100 µm, with its brightest pixels saturated at 8 bit. The low series looks the same. The target-surface row of the experimental crops is still unknown (DATA_NOTES "open questions"). Either the crop is not bottom-aligned with the surface, or the experimental emission is detached from the target. This matters for how the frames are compared, though not for the integrated L(t) metrics. Open item: establish the surface position in Fig. S4 before any shape metric is judged.

## 2026-11-24 — S4 surface row from the frames; face-pinned hot core traced to the sonic Knight source; v1k (T023)
- Earlier "round" Codex comparison (prior/pluto case_00326_coarse): the target row was a **fitted** camera parameter (surface_y_px = 61 of 81, bounds 45–100), and 180 µJ / kinetic fraction 0.12 sat at the search bounds. From 50 ns on, that model is also a flat-bottomed dome on the face. Not evidence of better physics.
- Surface row from the frames alone (scripts/exp_surface_rows.py → results/surface/): the lower 10% edge is fixed over 150–740 ns (high row 71.0 ± 1.2 of 81; low 50.8 ± 3.1 of 58) while the top edge rises. Face bracket [lower50, lower10]: the face sits 99–220 µm (high) and 67–195 µm (low) above the crop bottom. The experimental emission peak is ≥ 125–209 µm (high) and ≥ 90–185 µm (low) above the face. Current model (T016e): 0/20 and 0/11 frames reach that minimum lift (scripts/peak_lift.py → results/surface/lift_T016e_*.csv).
- T018c 62 ns extract: the wall cell on the beam has ρ = 6–7×10³ kg/m³ at 7–11 kK under 72–89 kK, ~14 kbar gas, and > 90% of the absorbed laser power is within 2 µm of the face. The sonic Knight source ignores the back pressure (model.py: "v1 adds back pressure", never done). v1k implements it (PREREG v1k, metric S1). Cloud: bit-identical when off; 30% of the vapour is suppressed by 68 ns on the smoke grid. Card T023 (48 cores; T021 continues on 16).

## 2026-11-24 — T021 low it1: energy residual inside the droplet window (accepted, corrected)
- Closure at 1300 ns: ΔE 21.86 µJ against net sources 20.11 µJ (+1.74 µJ). The residual is 0 up to 100 ns, 0.15 at 200, 0.64 at 250, 1.71 at 300 ns, then flat. M_Cu = M_src exactly. By comparison, it0 closes to 0.29 µJ and high it0 to 0.005%.
- Reading: in it1 the gas absorbs half of what it did in it0 (44.6 vs 87.8 µJ) for the same 5.2 ng of droplets. Late in the window the core cannot pay L_v, and the floor clamp adds the difference. The realised droplet cooling in low it1 is about 1.7 µJ less than booked. The run is kept as the upper bound with this correction. Making the droplet release energy-limited would be a model change and is not done mid-test.

## 2026-11-24 — T023 early check (high thin G0 it0, 40/60/80 ns) against PREREG v1k (a)
- The wall cell on the beam peaks at 114 / 642 / 466 kg/m³ (40/60/80 ns), against 6282 kg/m³ for the sonic source (T018c it2, 62 ns). That is 10× lower, but 60 ns is above the 500 kg/m³ threshold: (a)-1 fails marginally. The near-wall profile is now smooth: no 200 K floor cell and no 150 kK table cap (T_max 88/111/82 kK).
- 94 / 91 / 90% of the absorbed laser power is still within 5 µm of the face: **(a)-2 fails**. A dense absorbing vapour layer (10–600 kg/m³ over 0–3 µm) remains, and the hot 70–110 kK gas above it is too thin (0.1–10 kg/m³) to absorb first.
- Caveat: old = Picard it2, new = it0. The it0 source is unshielded and carries more vapour, so it1/it2 will be read the same way.
- PREREG reading for a failed (a): near-wall resolution is the suspect, and the G1 member (0.5 µm wall cell) is already running. Converging beam and the 3-D end face stay the next separately declared candidates. Nothing is tuned.

## 2026-11-24 14:00 — T023 resolution check (G1, 0.5 µm wall cell); T021 high it0 preliminary
- G1 against G0 (high thin it0, 40/60/80 ns): wall-cell ρ on the beam 157/680/530 against 114/642/466 kg/m³; absorbed fraction in z < 5 µm 0.93/0.88/0.89 against 0.94/0.91/0.90. **The near-wall absorbing layer is converged in resolution**, so PREREG v1k (a)-2 fails robustly. Grid is ruled out. Next are the separately declared candidates (converging beam, 3-D end face), after S1 from T023 it0/it1 imaging. Figure: results/figures/nearwall_G0_G1.png.
- T021 high it0 (droplet upper bound; preliminary, it0 against the baseline's it2):
  - Core specific energy at 300 ns: 9.7 MJ/kg against ≈ 44.
  - T_emw at 300/500/700 ns: 12.0/10.4/9.2 kK against 25.3/19.9/16.9 kK.
  - Direction (a) holds.
  - Axial shock radius at 500–700 ns is +14–17%. Because it0 is unshielded, this is not yet comparable. The reading of (a)–(c) waits for it2 imaging.

## 2026-11-24 15:00 — where the face-pinning starts: the pulse phase (diagnosis, no model change)
- Conduction is not the cause. On the axis at 60/80 ns (T023 high it0), the conductive flux through the near-wall layer is ≤ 0.5% of the free-streaming limit, and the wall sink (≈ 34 µJ by 80 ns, 53–77 µJ by 800 ns) is wide-area exchange with a resolved boundary layer.
- 1064 nm absorption (tables v1c):
  - Shocked air at 10× compression and 25 kK: L_abs = 6 µm. At 5× and 15 kK: 600 µm.
  - Hot thin Cu at 1 kg/m³ and 100 kK: 260 µm.
  - Wall layer at 600 kg/m³ and 10 kK: 0.1 µm.
  - In the model, the light passes the thin hot gas and dies in the wall layer.
- Experiment vs model during the pulse:
  - Fig. 3d shock (high): 86–114 µm at 25 ns, 138 µm at 51 ns, 224 µm at 69 ns. Model: ≈ 10 / 97 / 160 µm.
  - S3/S4 frames: the emission peak is already 80 µm (low, marked surface) and ~160 µm (high) above the face at 10–20 ns, and the emitting region reaches 190–360 µm.
  - In the experiment, energy is deposited earlier and higher up the beam than in the model, an absorption wave running toward the lens at several km/s. The model ignites late (gas absorption 21 µJ at 40 ns) and at the face.
- Candidate physics (all outside PLAN v3 §3.3/§3.4, so a scope decision for the user):
  1. Early breakdown out of LTE: laser-heated electrons (T_e ≫ T_h) avalanche-ionise the vapour and air within ns, which a single-T LTE model delays.
  2. Radiative precursor: VUV from the hot plasma photoionises and heats the air ahead.
  - A time-zero shift alone cannot fix it: the model lift stays ≈ 40–90 µm at all times.

## 2026-11-24 ~16:05 — v1m bookkeeping bug found by the cloud smoke test, fixed before any workstation run
- First v1m smoke (high, 0–120 ns): the cumulative store transfer reached −68.7 µJ, i.e. energy was created. Cause: the store was defined against the current LTE electron density. When the hydro moves a cell to a new (ρ, T), LTE recombination is already in the EOS energy, and the store released it a second time. Onset was 28.1 vs 29.7 ns, but that run is void.
- Fix: tracer 2 is now the excess electron number per Cu-atom mass. The store χ·x·ρ/m_Cu is advected and changes only in the source step, where each change is taken from or returned to ρe. Added mass carries no excess. T024 was put on hold before launch; the rerun is in progress.

## 2026-11-24 ~16:15 — T023 high thin it0 imaging (v1k), against the T016e baseline (it2), band b, sa
- L(730)/L(300): 0.376 against 0.636 (experiment 0.107). Back pressure speeds up the late decay, but it is still 3.5× slower than the experiment.
- σ_z / σ_x at 300 ns: 67 / 121 µm against 70 / 124 (experiment σ_z 151). Size unchanged.
- S1 lift at 150–630 ns: median 80 µm against 91 µm; the experimental minimum is 178 µm; 0/20 frames. PREREG v1k (b) fails: the lift does not rise.
- Caveat: v1k it0 against baseline it2. it1/it2 imaging is pending.

## 2026-11-24 17:05 — T025 low it0: droplet-window residual recurs (+1.14 µJ, 4.8% of net), accepted under the T021 rule
- The residual is 0.15/0.48/1.12/1.14 µJ at 200/250/300/400 ns, flat after the window. This is the same energy-floor clamp as T021 low it1: at low fluence the core cannot pay L_v for the time-uniform release. The realised droplet cooling at low fluence is about 1 µJ smaller than booked. It recurs, so an energy-limited release (droplets evaporate only as fast as the local gas can pay L_v) is a candidate model correction for the next declared version. It is not applied mid-test.
- T023 high thin it2: closure 0.04%, ConsToPrim 1.66M. Accepted; imaging in progress.

## 2026-11-24 18:50 — T026 low it0 (v1n energy-limited droplets): PREREG v1n (a) holds
- Endpoint residual 0.002 µJ, against +1.14 µJ for T025 low it0 (same case without the energy limit) and +1.74 µJ for T021 low it1. 0.99 ng of the 4.74 ng droplet mass stays condensed because the gas cannot pay L_v. The floor-clamp energy is gone, so the bound is now energy-consistent. Imaging is pending; (b)–(d) wait for it.

## 2026-11-24 20:15 — T025 high it1 (back pressure + droplet upper bound), PREREG "combination"
- (a) L(730)/L(300) (sa) is 0.157; the experiment is 0.107 and T021 0.155. Holds: within a factor 2.
- (b) S1 (sa): median 155 µm, 1/20 frames reach the experimental minimum (T021: 177 µm, 7/20). The lift is a little lower than with droplets alone.
- (c) σ_z at 300 ns: 76 µm against 151 µm in the experiment. Still small.
- (d) Axial shock at 600 ns: 628 µm against 609 µm for T023 (+3%). Holds.
- T026 low it0 (v1n): L(715)/L(300) 0.201 against 0.163 in the experiment, the closest of all members; S1 8/11 (passes); σ_z 68 against 94 µm; L(1035)/L(300) 0.136 against 0.027.

## 2026-11-24 20:55 — v1p smoke check (vapour exit momentum); T027 issued
- The first smoke run had VMOM 0: a string replace in the case generation slipped. The cases were fixed and rerun.
- v1p against v1n (high, smoke, 0–160 ns):
  - Momentum injected: 2.5e-9 kg m/s.
  - Closure −0.63% against −0.16%.
  - ConsToPrim: 10 278 lines against 283.
  - Gas absorption at 40/60 ns: 15/129 µJ against 33/143 µJ; vapour moves off the face and the onset is slightly later.
  - T027 (G0) gives the real test.
- T023 low it1 (v1k): S1 median 88 µm, 0/11 frames; back pressure alone does not lift the emission at low fluence either.

## 2026-11-24 21:50 — T027 low it0 (v1p vapour momentum): PREREG v1p (a) not met; run continues
- ConsToPrim: 8.59M against 0.58M for T026 low it0. Endpoint closure 0.76% against 0.009%.
- (a) as declared ("no worse than v1n") fails. Closure stays below the 2% limit, and the count is the same order as already accepted members (T021 high it0 4.4M, T025 low it1 3.2M). The run is kept, because the physics question (b), σ_z/σ_x, needs its imaging.
- The extra repairs sit where the vapour momentum enters next to the reflecting wall, the same location as the 11-17 problem. If (b) shows < 5% change, v1p is closed as both ineffective and numerically costlier. If (b) shows a real change, the momentum injection is moved into a boundary-flux form before it is reported as a result.

## 2026-11-24 23:10 — late-decay comparison checked on the same 8-bit pipeline as the experiment
- The experiment's L comes from 8-bit frames (saturation at 255, noise mask). The low frames are saturated at 130–340 ns (190 px at 300 ns), and the mask shrinks late (584 px at 1035 ns against ~3000 at 300 ns).
- The model's 8-bit L column, from the same pipeline and the same gain rule, gives ratios equal to or slightly slower than L_radiance:
  - Low v1n: L715/L300 = 0.217 and L1035/L300 = 0.146, against 0.163 / 0.027 in the experiment.
  - High droplets: 0.333 / 0.153, against 0.368 / 0.107.
- The slow late decay at low fluence is therefore not a metric artefact. It remains an open physics discrepancy.

## 2026-11-25 00:10 — T026 (v1n) final: the current best model, energy-consistent
- High it2:
  - L(500)/L(300) 0.296 and L(730)/L(300) 0.155, against 0.368 / 0.107 in the experiment.
  - S1 median 145 µm, 0/20 frames.
  - σ_z/σ_x at 300 ns: 75/116 µm.
- Low it1:
  - L(715)/L(300) 0.199 and L(1035)/L(300) 0.129, against 0.163 / 0.027.
  - S1 median 125 µm, 8/11 frames (passes).
  - σ_z/σ_x 67/94 µm.
- Droplet-window residual 0.002 µJ. This replaces T021 as the reported droplet member.
- T027 high it1: 2.82M ConsToPrim lines against 1.81M for T026; imaging pending. T029 claimed by Codex.

## 2026-11-25 00:20 — low-fluence late decay: diagnosis (no model change)
- T026 low it1 diag_emission: after 300 ns the emission-weighted T stays at 6.2 → 5.4 kK (300 → 1200 ns), with n_e,emw 3e22 → 7e21 m⁻³. Over 99.9% of the band signal is Cu I lines. The mass-weighted core temperature is only 2.5 kK, cooled by the droplets. The late signal therefore comes from a small pocket that stays near 5.5 kK and cools very slowly.
- The radiation limit is not the cause: T023 low trap against thin gives L1035/L300 = 0.111 against 0.098.
- Remaining candidates, both outside the current model:
  1. Mixing of the hot pocket with cold air at the decelerating Cu–air contact. That interface is Rayleigh–Taylor unstable; 2-D axisymmetric laminar flow with molecular diffusion under-represents it.
  2. Condensation, not modelled. The 2.5 kK core is below T_boil, which bears on the paper's nanoparticle subject. It would release heat and would not by itself speed the decay.
- Both would be model-form additions, so they are for the user to decide in the morning; nothing is tuned overnight.

## 2026-11-25 01:10 — T027 (v1p) final: closed; at low fluence it is clearly worse
- Low it1 against T026 low it1:
  - σ_z/σ_x at 300 ns: 0.699 against 0.711.
  - S1 median 42 µm, 0/11 frames, against 125 µm and 8/11.
  - L715/L300 0.723 and L1035/L300 0.455, against 0.199 / 0.129.
  - The vapour momentum keeps a hot emitting layer at the face and destroys the low-fluence decay and lift.
- High it2 was stopped at 162 ns as authorised. Together with the high it0 reading (aspect +1.5%), v1p is closed, and the reported model stays v1n (T026).

## 2026-11-25 06:40 — T029 low (v1q, droplets at the measured spatter top) and T028 low G1 (grid check of v1n)
- **T029 low it1 against T026 low it1, PREREG v1q:**
  - (b) σ_z/σ_x at 300 ns: 0.917 against 0.711, +29%; the experiment is 0.94. The plume shape becomes round, close to the experiment, but by narrowing (σ_x 94 → 79 µm against 99 in the experiment) more than by elongating (σ_z 67 → 73 µm against 94).
  - S1 median 134 µm, 8/11 frames, which passes.
  - (c) The decay gets worse: L715/L300 0.336 against 0.199 (experiment 0.163), and L1035/L300 0.159 against 0.129. At the low spatter speed, 190 m/s, the gas at the front can pay for only part of the droplets: 3.56 of 4.74 ng stay condensed, against 0.99 ng in T026. So the cooling is weaker.
  - Reading: the location of the droplet sink sets the plume aspect. v1n (whole core) and v1q (front) bracket the location, so each matches part of the data. Shape matches at the front; decay and width match in the core.
- **T028 low G1 it0 against T026 low G0 it0:**
  - L715/L300 0.228 against 0.201 (+13%); L1035/L300 0.150 against 0.136 (+10%).
  - S1 median 106 against 125 µm, 7/11 against 8/11 frames.
  - σ_z 69 against 68 µm; σ_z/σ_x 0.67 against 0.70.
  - The headline metrics agree to about 10–15% between grids; the decay and lift readings are unchanged. The high G1 member is at about 340 ns.

## 2026-11-25 07:35 — T029 high it0 (v1q) and T028 high G1 production
- T029 high it0 against T026 high it0 (matched iteration):
  - σ_z/σ_x at 300 ns: 0.632 against 0.551 (+15%, ≥ 5%).
  - S1 median 80 µm, 0/20 frames, against 123 µm.
  - L730/L300 0.356 against 0.315.
  - At high fluence the spatter-top sink raises the aspect ratio but lowers the lift and slows the decay. 1.7 ng stays condensed.
- T028 high G1 it0 production is complete (closure 0.07%); imaging is pending.
- Early over-brightness found while answering the user. With the gain fixed at 300 ns, the v1n low series has L(t)/L(300) = 20–58 at 60–140 ns, against 0.5–0.8 in the experiment (which is saturated there, so a lower bound). The droplet release window [85, 300] ns from v1j cools the 300 ns frame. A release window from the measured spatter duration (high: rising to ≈500 ns; low: scattering to ≈400 ns, ends ≈900 ns) is a candidate physical correction, left for the user to decide.

### 2026-11-25 08:00 — T029 (v1q) high it1 reading (band b, sa, 250 µm)
- T029 high it1: L500/L300 0.638, L730/L300 0.442; σz/σx at 300 ns 88/114 µm (0.769); S1 median 80 µm, 0/20.
- Same iteration T026 (v1n) it1: L730/L300 0.145; σz/σx 77/117 (0.653); S1 166 µm, 1/20.
- Experiment: L730/L300 0.107; σz/σx 1.07; min-lift median 178 µm.
- Reading: at high fluence v1q moves the aspect toward the experiment (+18% vs v1n), but the decay worsens (0.36 → 0.44 from it0 to it1) and the emission peak falls back toward the face. As at low fluence, v1q and v1n are the two ends of the droplet evaporation location. No change to the reported model (v1n).

### 2026-11-25 09:40 — T028 high G1 (v1n fine grid) reading; T028 closed
- High it0, G1 vs G0 (band b, sa, 250 µm): L500/L300 0.527 vs 0.568; L730/L300 0.280 vs 0.316; σz/σx at 300 ns 0.570 vs 0.551; S1 median 123 vs 123 µm (0/20 frames both).
- With the low-fluence result (10–15%), grid convergence of v1n is adequate at G0 for the readings made. T028 done.
- T029 high it2 production done (residual 0.027%); imaging is in queue 10.

### 2026-11-25 09:45 — T029 (v1q) high it2 reading; T029 closed
- T029 high it2 vs T026 it2 (band b, sa, 250 µm): L500/L300 0.570 vs 0.302; L730/L300 0.408 vs 0.156; σz/σx 88/110 (0.80) vs 75/116 (0.64); S1 median 80 vs 145 µm (0/20 both). Experiment: L730/L300 0.107, aspect 1.07.
- PREREG v1q readings final: (b) aspect +24% toward the experiment (met); (c) decay worse by 2.6×. Same pattern at both fluences. v1q remains the front-only bracket of the droplet location; v1r (T030) replaces the bracket with flight time and heat-transfer evaporation.

### 2026-11-25 09:50 — T030 (v1r) launched 09:41:59; paper has no particle-size number
- data/exp/paper/np_size_notes.md (Codex, verbatim): no numerical diameter or size distribution of deposited NPs or droplets in the main text or SI. Only qualitative: NP number and size grow with fluence near the trace; "nanometer-scale solid and liquid metal particles" form when the plume cools. The v1r droplet radius therefore has no independent measurement; the melt-depth rule stays the reported member and the scan stays sensitivity only.

### 2026-11-25 11:00 — T030 inputs verified (Codex 10:00)
- Melt depth (area mean, shielded target model of the seeded iteration): high 4.79 µm → DROP_A0 4.53 µm; low 3.62 µm → 3.42 µm. Scan inputs 1e-8 / 3e-8 verified; 100 nm members queued. No member finished yet.

### 2026-11-25 11:05 — T030 low_a10n (v1r, 10 nm scan member) production
- 1300 ns, rc 0. Launched 5.19 ng; evaporated 0.41 / 0.90 / 1.55 / 2.11 ng at 150 / 300 / 600 / 1300 ns (17% by t_ref, 41% at the end); airborne 3.08 ng. v1n (T026 low it1) evaporated 3.87 ng (M_Cu 4.45 vs 2.60 ng here).
- Even 10 nm droplets evaporate slowly: at 190 m/s they sit within 40 µm of the face up to 300 ns (lower plume edge), where the gas is coolest. The spatter-top check (d): the highest half-mass parcel keeps rising with the front (37 µm at 300 ns, 219 µm at 1300 ns); there is no evaporation-limited stop.
- Imaging pending. Melt-depth members (a0 3–5 µm) are expected to evaporate far less (PREREG (b)).

### 2026-11-25 12:10 — T030 low_a10n imaging; low_a30n production
- low_a10n it1 (scan member, band b, sa, 250 µm): L715/L300 0.250, L1035/L300 0.134; σz/σx at 300 ns 74/81 (0.91); S1 median 125 µm, 8/11.
- Same pipeline, same iteration: T026 v1n 0.219 / 0.141 / 0.72 / 8/11; T029 v1q 0.385 / 0.179 / 0.90; T023 v1k 0.303 / 0.121 / 0.69. Experiment: 0.163 / 0.027 / 0.94.
- Reading (scan, not adoptable): with 10 nm droplets the location from flight and heat transfer gives v1q's aspect and a decay close to v1n. Only 17% of the droplet mass has evaporated by t_ref. This shows that the decay is set by where and when the vapour cools the core, not only by how much evaporates.
- low_a30n production: evaporated 1.48 ng of 5.19 ng (29%) by 1300 ns; airborne 3.71 ng. Imaging pending.

### 2026-11-25 12:35 — T030 high_a10n production
- 800 ns rc 0. Launched 6.29 ng; evaporated 0.72 / 1.96 / 2.96 / 3.51 ng at 150 / 300 / 500 / 800 ns (56% at the end); airborne 2.78 ng. Energy residual 0.13% (v1n 0.03%); 3.5 M ConsToPrim lines. Half-mass spatter top 55 µm at 300 ns, 116 µm at 500 ns, 208 µm at 800 ns: it keeps rising, so no 500 ns plateau (PREREG (d) mismatch, as at low fluence). Imaging pending. high_rp/low_rp not finished yet.

### 2026-11-25 13:00 — T030 low_a30n imaging
- low_a30n it1 (scan): L715/L300 0.196, L1035/L300 0.052 (best late decay so far; exp 0.027, v1n 0.141); σz/σx 0.75; S1 116 µm, 8/11.
- The scan is not monotonic in a: 10 nm aspect 0.91 / late 0.134; 30 nm aspect 0.75 / late 0.052. Scan only, not adoptable. Asked Codex for high_rp/low_rp progress (no rp member delivered after 3 h 20 min).

### 2026-11-25 13:35 — T030 reported members (melt-depth droplets): PREREG (b) confirmed
- high_rp it2 (a0 4.53 µm), 800 ns rc 0: launched 6.29 ng, evaporated 0.023 ng (0.37%), airborne 6.27 ng.
- low_rp it1 (a0 3.42 µm), still running (807 ns of 1300 at 13:31, ~2 ns/min, progressing): evaporated 0.014 ng (0.28%).
- Reading: with droplets of the size the melt layer gives, gas heat transfer evaporates < 0.5% of them. The plume is then effectively the no-droplet case (v1k, T023: L730/L300 0.60 high). The v1n fast decay is physical only if the ejecta are ≲ 30 nm fragments at launch, which neither our model nor the paper's data establishes. v1n stays the best phenomenological model, but it is not a physically closed one. Recorded as the main open problem of the decay.
- Spatter top (d): the parcels keep their mass and keep rising (246 µm at 800 ns high), with no plateau. The measured stop after 500 ns is not reproduced: in the model the droplets neither evaporate nor stop.

### 2026-11-25 13:40 — T030 high_a10n imaging; high_a30n and low_a100n production
- high_a10n it2 (scan): L500/L300 0.674, L730/L300 0.437; σz/σx 0.76; S1 91 µm, 2/20. Against T026 v1n 0.302/0.156/0.64, T029 v1q 0.570/0.408/0.80, T023 v1k 0.746/0.667/0.58. Experiment L730/L300 0.107, aspect 1.07.
- At high fluence even 10 nm droplets (31% evaporated by t_ref, 56% at 800 ns) give a decay much slower than v1n. The low-fluence 10 nm result (decay near v1n) does not carry over.
- Production: high_a30n evaporated 2.26 of 6.29 ng; low_a100n 0.65 of 5.19 ng. Imaging pending. RP imaging first.
- 14:05 high_a100n production: evaporated 1.21 of 6.29 ng (19%) by 800 ns; residual 0.032%. Scan evaporated fraction at 800 ns (high): 10 nm 56%, 30 nm 36%, 100 nm 19%, melt-depth 4.5 µm 0.37%.
- 14:37 T030 low_rp production done (1300 ns, rc 0): evaporated 0.020 ng of 5.19 ng (0.4%), airborne 5.17 ng. Energy residual 0.57% — larger than v1n (0.004%) and the other T030 members (≤ 0.13%); within the 1% acceptance but recorded. All 8 T030 productions done. T031 claimed and built (15 tests pass).

### 2026-11-25 15:05 — T030 high_rp (v1r, reported member) imaging
- high_rp it2: L500/L300 0.747, L730/L300 0.709; σz/σx 0.57; S1 70 µm, 0/20. Practically identical to T023 v1k (no droplets: 0.746 / 0.667 / 0.58), as PREREG v1r (b) predicted. T026 v1n: 0.302 / 0.156 / 0.64.
- v1r summary (high): with physically sized droplets (melt depth) the droplet sink vanishes and the plume reverts to the no-droplet case. The v1n decay needs ≲ 30 nm ejecta, which neither the model nor the paper establishes. Figures redrawn with the v1r row.
- 15:30 T030 high_a30n imaging (scan, not adoptable): L500/L300 0.608, L730/L300 0.310, σz/σx 0.89. T031 low_v1s production (v1n + recondensation, 1300 ns rc 0): recondensed 2.67 ng of 4.56 ng gross Cu input (59%); net M_Cu 1.89 ng; residual 0.010%. Imaging pending.

### 2026-11-25 16:05 — T030 low_rp imaging; T031 high_v1s and low_v1rs production
- low_rp it1 (v1r, reported): L715/L300 0.335, L1035/L300 0.132; σz/σx 0.70; S1 88 µm, 0/11. Same as T023 v1k (0.303 / 0.121 / 0.69). At both fluences v1r with melt-depth droplets equals the no-droplet case. Low figures redrawn with the v1r row.
- T031 production: high_v1s recondensed 2.73 of 7.24 ng gross Cu (38%), residual 0.008%; low_v1rs recondensed 0.46 of 0.60 ng (77%), residual 0.008%. Imaging pending.

### 2026-11-25 16:40 — T031 low_v1s imaging (first v1s reading)
- low_v1s it1 vs T026 it1 (band b, sa, 250 µm): σz/σx at the t_ref frame (280 ns) 0.750 vs 0.721 (+4.0%); at 340 ns 0.747 vs 0.689 (+8%); at 500 ns 0.752 vs 0.690 (+9%). The core lifts: zc 150 vs 136 µm. S1 median 143 vs 125 µm, 10/11 vs 8/11 frames. L715/L300 0.204 vs 0.219; L1035/L300 0.156 vs 0.141.
- PREREG v1s (b) at the declared time: +4% < 5%, not met. Later frames +8–9%, and the lift improves. The aspect gain comes from a narrower σx (88 vs 94 µm), not a taller σz. Recondensation helps the lift but does not make the plume round: 0.75 against the experiment's 0.94. high_v1rs production done (68% recondensed); high imaging pending.

### 2026-11-25 17:50 — T031 high_v1s imaging: PREREG v1s (b) not met at either fluence
- high_v1s it2 vs T026 it2: σz/σx 0.632 vs 0.644 at 300 ns (−2%); 0.62 vs 0.63 at 400–630 ns. zc 158 vs 146 µm; S1 median 155 vs 145 µm (0/20 both). L500/L300 0.301 vs 0.302; L730/L300 0.160 vs 0.156.
- Reading: recondensation removes 38–59% of the gas-phase Cu and lifts the core by 10–15 µm, but it does not round the plume. At high fluence the aspect is unchanged. The half-dome shape is not set by the near-wall vapour. Next candidate, declared next: radiative transfer (absorption front moving up the beam). v1s is kept as physics (standard kinetic return flux), not as a morphology fix.

### 2026-11-25 19:05 — T031 high_v1rs imaging: recondensation matters in the physically sized model
- high_v1rs it2 (v1r melt-depth droplets + recondensation) vs T030 high_rp it2: σz/σx 0.667 vs 0.572 at 300 ns (+17%, PREREG v1s (b) met for v1rs); 0.73 vs 0.66 at 500 ns. zc 132 vs 94 µm; S1 median 113 vs 70 µm (0/20). L500/L300 0.435 vs 0.747; L730/L300 0.222 vs 0.709 (experiment 0.107).
- Reading: in the model without the ≲30 nm droplet assumption (droplets do not evaporate), recondensation is the dominant sink. It cuts the late emission threefold and lifts and rounds the core. v1rs is the first configuration where every ingredient is physically sized and nothing is phenomenological, and its decay is within ~2× of the experiment. In v1n the droplet cooling had already done this work, so recondensation added little there. low_v1rs imaging pending.

### 2026-11-25 19:35 — T031 low_v1rs imaging; T031 done; recommendation: v1rs as the reported model (user decision)
- low_v1rs it1 vs T030 low_rp it1: σz/σx at 300 ns 0.781 vs 0.695 (+12%); at 500 ns 0.913 vs 0.848 (experiment 0.94). zc 105 vs 94 µm; S1 median 106 vs 88 µm, 6/11 vs 0/11 (threshold 8/11). L715/L300 0.134 vs 0.335 (experiment 0.163); L1035/L300 0.044 vs 0.132 (experiment 0.027). That is the closest late decay of any model, and the first within 2× at 1 µs.
- v1rs at both fluences: decay within ~2× (high L730 0.22 vs 0.107; low L1035 0.044 vs 0.027), aspect +12–17% toward the experiment, lift improved. Every ingredient is physically sized: Knight evaporation with back pressure, melt-depth droplets with heat-transfer evaporation, Hertz–Knudsen recondensation with σ = 1. No size or rate assumption is unsupported.
- Recommendation to the user: v1rs replaces v1n as the reported model (v1n stays as the phenomenological bracket). Open: high aspect 0.67 vs 1.07; S1 at high 0/20. Next physics: radiative transfer. T033 OAT runs on v1n; repeating it on v1rs would be needed if v1rs is adopted.
- 21:05 T030 done (all 8 members produced and imaged). T033: first five productions complete.

### 2026-11-25 22:15 — T033 first readings (v1n OAT, high, vs T026 it2; sensitivity only)
- rem_hi (removal ×3): L500/L300 0.441 vs 0.302, L730/L300 0.165 vs 0.156; σz/σx 0.688 vs 0.644 (+7%); σx 111 vs 116 µm (−4%); zc 193 vs 146 µm; S1 188 µm, 12/20 frames (T026 0/20). This is the first high-fluence member above the experimental minimum lift in most frames. In v1n the droplet mass, which is uncertain by an order of magnitude, controls the lift.
- A_hi (absorptance 0.05): L500 0.394, L730 0.193; σz/σx 0.611 (−5%); σx 122 (+5%); S1 150 µm, 0/20.
- Neither moves σx by more than 20% or σz/σx by more than 10%. Await the remaining members; PREREG reading at the end.
- ws sync was blocked by uncommitted run_registry rows (21:30, 22:00). Fixed with a standing rule (handoff/to_codex/ws-sync-registry.md); Codex committed the rows and resumed.
- 22:35 T033 low_ftc_lo (f_Tcap 0.85): the plume does not ignite. Band radiance at 130 ns is 36 against 2e8 for T026; vaporised mass 0.36 vs 0.54 ng; M_Cu 0.88 ng; 5.3 ng of droplets stay condensed. The L ratios (1.4) are meaningless for a non-emitting plume. Finding: at low fluence the model sits near an ignition threshold in the surface-temperature cap, and the energy-limited droplet sink then quenches the vapour. Record this as a sensitivity and robustness limitation of v1n at low fluence; check the same in v1rs (T035 ftc_lo).
- Answered Codex: band-b NPZ for sensitivity cards stays on D: (manifest in git); low reference frame 280 ns as always.
- 22:45 T032 done: desk node ready. Smoke at 110 ns against the cloud reference: E_int +0.14%, M_Cu −0.04%, E_rad +3.6%. Different compiler and table hashes; the E_rad difference is acceptable for same-node comparisons only. T035 carries its own baseline, so cross-node A/B is not allowed. About 43 MiB per rank in the smoke run.

### 2026-11-26 00:45 — Overnight diagnosis: what sets the size and brightness-trend gaps
- **Shock (independent data).**
  - High fluence: with the declared t0 offset (28 ns, PREREG t0 alignment), the model shock matches Fig. 3d early (ratio 0.96) and late (1.07). High-fluence energy coupling is therefore adequate, and the earlier "late shock" was a t0 convention issue.
  - Low fluence: the shock is still about 25% small at any offset ≤ 60 ns, which is a real coupling deficit of roughly 3×. The model ignites the plume at about 55 ns. The experiment shows a saturated emitting spot at its 10 ns frame.
- **Hot-Cu absorptance:** T036 gives the measured Mattern 2023 values, R = 96.6–97.7% polished at room temperature (A = 0.023–0.034) and 93.4–93.9% liquid (A = 0.061–0.066). The model's A(T) is 0.044 near the melting point and 0.075 liquid, at or above the measurements. Absorptance does not explain the late ignition.
- **Laser waveform:** no measured trace of the YLPN-1-30×240-100-R at 60 ns exists in the sources found (T036). Only a YLP trace at 250 ns, from a different source, is available. Under PLAN the MOPA scenario runs only with a real waveform, so it is not run. Data request to the user: the laser's measured pulse shape, if the lab has it.
- **OAT, high fluence (T033, 7 of 9 inputs imaged):** σx 111–122 µm against 116 (≤ 5%); σz/σx 0.608–0.688 against 0.644 (≤ 7%). None reaches the PREREG thresholds (20% and 10%). Size and shape are structural, not input uncertainty.
- **Camera blur, constrained independently:** the experimental low-fluence 10 ns frame has σx = 33 µm and σz = 36 µm (source plus blur). The baseline 250 µm defocus disc alone has σ = 0.436·250/2 = 55 µm per axis, which is inconsistent. The declared 0 µm scenario is consistent. The defocus scenario is therefore chosen by the instrument constraint, not by the plume comparison. Low fluence with 0 µm blur: σx 63 µm against experiment 97 at 280 ns, so the low-fluence size deficit is about 35%. High-fluence early frames cannot bound the blur, because the plume is already 108 µm at t = 0.
- **Summary:** low fluence is limited by late ignition and about 3× too little coupled energy, which accounts for both the small size and the decaying trend. Its cause is not found among the declared inputs; candidates are the laser waveform (data needed) and the breakdown physics. High fluence is limited by a narrower luminous region (σx −20%) at the correct blast energy, which points to lateral transport or mixing that 2D laminar flow does not capture.
- 01:30 T035 low_ftc_lo (v1rs, f_Tcap 0.85, desk it0) DOES ignite (see numbers in report), unlike v1n T033 low_ftc_lo: v1rs is robust to the cap where v1n quenched (energy-limited droplet sink absent in v1rs).
- 02:40 Waveform smoke (low, smoke grid, to 85 ns): ignition at 39.5 ns with the comparable YLP-HP trace against 54.0 ns Gaussian (14.5 ns earlier); closure 1e-11. T039 released (workstation, seeded high + low).
- 03:00 T035 low_cond_lo (σ = 0.3): recondensed 0.484 ng against 0.472 ng at σ = 1. Recondensation is supply-limited, set by Cu arriving at the wall rather than by the sticking coefficient, so v1rs is robust to σ. T037: the first t0-shifted image is verified. T039 build and preflight are verified on the workstation.
