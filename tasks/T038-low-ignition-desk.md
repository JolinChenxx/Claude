---
id: T038
title: (desk node) low-fluence ignition sensitivity of v1rs — v1m avalanche, pulse-width reading (FWHM 35 ns), both, plus the comparable-device waveform (PREREG waveform); 4 runs; needs build_v1w (PULSE_TAB) at Picard it0; after T035
owner: codex-desk
status: ready
depends: [T035]
milestone: D2
est_time: ~30 min per run, two in parallel
---

## Background
PREREG, "T038 low-fluence ignition sensitivity declared 2026-11-26 ~00:55". Node rules as in T032/T035, now with an 18-core limit.

## Steps
1. After T035 finishes: `git pull`; rebuild the desk solver as `C:\sim_tools\pluto_lip\build_v1w\mpi` with the T032 build command and the batch pluto_exe below points to it, then `C:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T038.yaml --max-cores 16` in the background.
2. Check the pluto.ini of each run:
   - low_bd: BD_ON 1.
   - low_tau35: FWHM 3.5e-08.
   - The T_PEAK value follows the 10% convention.
3. For each run, as in T035:
   - Imaging twice: `--out image_v2` and `--t-offset-ns 28 --out image_v2_t28`, both `--workers 4`.
   - peak_lift on both.
   - Archive the small files, plus diagnostics/shock_track.csv from `python scripts/shock_track.py <run_dir>` if available.
   - Push and write a handoff `handoff/to_claude/T038-desk-<id>.md`.
4. Also re-image the T035 low_base with `--t-offset-ns 28` (image_v2_t28) for the comparison.
