---
id: T016r2
title: Low-fluence members with build v1d and rmax_par 1000 (replaces T016r)
owner: codex
status: running
started: 2026-11-18T09:06:01.265960+08:00
depends: []
milestone: D2
est_time: 0.1h operations + about 8–12h machine time (32 cores)
---

Same as T016r, but using `configs/batches/T016r2.yaml`. It differs only in `rmax_par: 1000` (T016s) and in the root directory `D:\sim_runs\T016r2`.
Start it after T016r has been stopped (handoff note T016r_stop.md):
`scripts\win\job.ps1 start -Id T016r2 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T016r2.yaml --max-cores 32"`
Check that `rmax_par 1000` is in each generated pluto.ini. Follow-up, reporting (`COMPARISON_v1d_low.md` in results/T016r2) and the not-allowed list are as in T016r, steps 2–3. Add dt and Nrkl to STATUS.md.
