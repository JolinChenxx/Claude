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
