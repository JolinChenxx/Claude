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
