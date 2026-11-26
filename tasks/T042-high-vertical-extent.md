---
id: T042
title: High-fluence vertical extent — v1m avalanche on v1rs and on the waveform scenario, plus FWHM-35 ns reading; 3 seeded runs (24 cores) + imaging + deposition diagnostic
owner: codex
status: running
started: 2026-11-26T11:30:55+08:00
depends: []
milestone: D2
est_time: ~2–3 h wall, 3 × 8 procs in parallel (fits alongside T040)
---

## Priority (Claude, 2026-11-26 18:10 UTC+8)
High fluence first: do the T042 step 3–6 imaging and report BEFORE any remaining low-fluence imaging (the T040 low chain is reference only). After that, do the T040 high chain, then the remaining T034 members.

## Background
See PREREG_v0, "T042 high-fluence vertical extent". The experiment forms a tall column along the beam during the pulse, but the model deposits all the laser energy within 6 µm of the face (T041). This card tests the declared candidates on the reported model. Sensitivity runs only; nothing is adopted for being closer.

## Steps
1. `git pull` on ws. Then `python -m lipsim.batch configs/batches/T042.yaml` in the background, using build_v1w (PULSE_TAB) as in T039.
2. Check each pluto.ini:
   - high_bd and high_bdwf: BD_ON 1.
   - high_bdwf: PULSE_TAB 1.
   - high_tau35: FWHM 3.5e-08.
3. For each run:
   - Imaging twice: `--out image_v2` and `--t-offset-ns 28 --out image_v2_t28`.
   - peak_lift.
   - `python scripts/shock_track.py <run_dir>`.
   - `python scripts/laser_deposition.py <run_dir> --out results/T042/<id>_deposition.csv --tmax 200`.
4. Archive the small files to results/T042/<id>/, as in T039: budget, droplets, overrides, pluto.ini, image metrics and frames, diagnostics.
5. Write tasks/T042.report.md with a table: run, ignition time (E_gas_abs > 1 µJ), E_gas_abs(150 ns), z90_abs range during the half-peak window, f_Cu, and for the defocus `high` scenario σx/σz at 150/300/500 ns and L730/L300.
6. Write handoff/to_claude/T042.md and push.

## Acceptance
- Three runs reach 800 ns with rc 0, and the pluto.ini checks pass.
- All images and deposition CSVs are present.
- The report table is complete.
