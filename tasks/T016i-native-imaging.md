---
id: T016i
title: Re-image all finished runs with native-grid radiative transfer (imaging v1)
owner: codex
status: ready
depends: []
milestone: D2
est_time: 0.1h operations + a few hours of background imaging (16 workers, sequential)
---

## Background
The v0 imaging volume-averages ρ, T and Y onto 5 µm shells and onto the 10 µm pixel rows **before** the strongly non-linear emission and absorption. In a cloud smoke snapshot (60 ns) this changes the thin band power by a factor of 1.9 and the sa/thin ratio from 0.15 to 0.11. It is a likely cause of the late sa "re-brightening" in T016e low G0 (STATE 2026-11-19 02:15).

Imaging v1 (`configs/imaging/v1.yaml`, `transfer_grid: native`) does the transfer on the native PLUTO cells and then averages the emergent **intensities** over each pixel row, which is what the detector integrates. Everything else is unchanged: filters, defocus, gate and gain.

## Steps
1. `git pull --rebase origin ws`. In the background, at low priority beside T016e, run one batch root at a time with 16 workers. Use the table set of each batch:
   - `scripts/image_members.py D:\sim_runs\T016e --series high|low --tables D:\sim_runs\tables\v1c --scenarios configs/imaging/v1.yaml --outname image_v1`. Repeat as more T016e iterations finish.
   - The same with `--outname image_v1` for `D:\sim_runs\T015` (tables v1), `D:\sim_runs\T016r2` and `D:\sim_runs\T016h` (tables v1b), and `D:\sim_runs\T014` (tables v0).
2. Commit `image_v1/metrics_*.csv` and `image_summary.json` per run, mirroring the existing `results/<batch>/<member>/<it>/image` layout under `image_v1`. Keep frame arrays local, as before. Write a short handoff after the T016e and T015 sets are done, and again at the end.

## Not allowed
- Changing filters, gains or any solver run. The v0 images stay where they are, for comparison.
