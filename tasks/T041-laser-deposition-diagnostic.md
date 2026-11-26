---
id: T041
title: Diagnostic (no new runs) — laser deposition height/medium and hot-region height vs time for high-fluence reported runs; minutes per run
owner: codex
status: review
started: 2026-11-26T10:53:10+08:00
depends: []
milestone: D2
est_time: < 30 min total
---

## Background
The user's focus is now the high-fluence brightness trend and plume size. After t0 alignment the model's blast energy matches the experiment. The luminous core is still lower and smaller: at 50 ns the experiment's emission centroid is about 200 µm above the face.

A cloud smoke run (5 µm cells) puts 99.8% of the laser absorption in the Cu vapour within about 4–8 µm of the face. The question is where the production runs deposit the laser energy, and whether an absorption front climbs the beam.

## Steps
1. Pull. For each of the following, run `D:\sim_tools\envs\lipsim\python.exe scripts/laser_deposition.py <run_dir> --out results/T041/<name>.csv --tmax 200`:
   - T026 high_thin_G0/it2 (v1n)
   - T031 high_v1rs/it2 (v1rs)
   - T039 high_wf/it2 (waveform)
   - T028 high_thin_G1/it0 (fine grid)
   - T026 high_thin_G0/it0 (first iteration)
2. Commit the five CSVs and write a handoff. This takes priority over the queued imaging because it is minutes of CPU.
