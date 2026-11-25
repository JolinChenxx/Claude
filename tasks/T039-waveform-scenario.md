---
id: T039
title: Measured comparable-device waveform scenario (IPG YLP-HP 61.7 ns trace from T036) on v1rs, high + low, seeded as T031; 16 cores; priority production
owner: codex
status: ready
depends: []
milestone: D2
est_time: ~3.5 h (high), ~2 h (low)
---

## Background
PREREG, "Waveform scenario declared 2026-11-26 ~02:20". The new build `build_v1w` adds PULSE_TAB. lipsim.batch writes lip_tables/pulse.txt from the case's `laser.waveform`, and the target model uses the same waveform. With PULSE_TAB 0 the build is identical to v1s.

Cloud smoke run (low, smoke grid, 0–85 ns, 1 core): rc 0 for both runs. The ignition time (E_gas_abs > 1 µJ) is 54.0 ns for the Gaussian and 39.5 ns with the waveform. E_gas_abs at 85 ns is 55.0 and 57.8 µJ. Energy closure is 1e-11 for both. pulse.txt has 4000 rows.

## Steps
1. Pull. Build `build_v1w` into `D:\sim_tools\pluto_lip\build_v1w\mpi` with the T031 build command, changing only the output dir. Run the T034 test set.
2. Launch now: `scripts\win\job.ps1 start -Id T039 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T039.yaml --max-cores 16"`. T034 production is finished, so cores are free. Do not stop running imaging.
3. Check every pluto.ini: PULSE_TAB 1. Check that lip_tables/pulse.txt exists, has 4000 rows, and integrates to E_pulse within 0.1%. Check COND_SIG 1 and DROP_A0 as in T031.
4. Image as usual (image_v2), and also with `--t-offset-ns 28 --out image_v2_t28`. Run peak_lift and diagnostics; shock_track is needed.
5. Handoffs as usual. This is priority postprocessing, ahead of T033/T034 imaging.
