---
id: T029
title: v1q droplets evaporate at the measured spatter top (v1bpdk cases), high thin G0 it0–it2, low thin G0 it0–it1; 16 cores
owner: codex
status: running
depends: []
milestone: D2
est_time: 0.5h operations + ~4h machine time
started: 2026-11-25T00:05:35+08:00
---

## Background
PREREG_v0, "v1q extension declared 2026-11-24 ~23:45". The v1n droplets (T026) now evaporate only in the grid row holding the spatter top z_d = v_s (t − t_launch). The speeds are the paper's measured values (registry cu_spatter_speed_high 344 m/s with a 500 ns plateau, cu_spatter_speed_low 190 m/s; quotes in data/exp/paper/droplets_notes.md). There is no free parameter, and T026 is the reference.

Cloud smoke check (high, smoke grid, 0–140 ns): the ini values are DROP_VD 344, DROP_TPL 5e-07 and DROP_TB 2835. Closure −0.15% to −0.56%, the same as v1n. 197 ConsToPrim lines; droplet mass is released.

## Steps
1. `git pull --rebase origin ws`. Build build_v1q into `D:\sim_tools\pluto_lip\build_v1q\mpi` with the T026 build command (output dir changed). Expect rc 0. Run `python -m pytest -q tests/test_breakdown.py tests/test_knight_bp.py`.
2. Launch when ≥ 16 physical cores are free: `scripts\win\job.ps1 start -Id T029 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T029.yaml --max-cores 16"`. Check that pluto.ini has DROP_VD 344 (high) / 190 (low), DROP_TPL 5e-07 (high) / 0 (low), DROP_TB 2835, VMOM 0 and BACKP 1.
3. Per finished iteration, as in T026: residual checkpoints, ConsToPrim, M_drop_condensed, image_v2 with band-b frames, diagnostics and peak_lift. Postprocessing rank: after T027, before T028.
4. Handoffs after each iteration (production and imaging). Report `tasks/T029.report.md`.

## Not allowed
- Physics, numerics or case changes. Stopping other cards. Deleting data.
