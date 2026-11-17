---
id: T016r
title: Rerun the three T016 low-fluence members with build v1d (kinetic flux limiter) on the free cores
owner: codex
status: running
started: 2026-11-18T04:03:57.258491+08:00
depends: []
milestone: D2
est_time: 0.1h operations + about 6–8h machine time (32 cores)
---

## Background
All three low-fluence members of T016 (built with v1b) aborted with energy NaN in rarefied near-target cells (T016b). Build v1d adds the kinetic limit on the Cu–air interdiffusion flux (registry `diff_cuair_limiter`). T016c restarted both low G0 cases from their abort points with v1d. They ran on to 497 and 598 ns with **zero** ConsToPrim, E<0 or other warnings on all 16 ranks, until the 40-minute limit stopped them.

v1d is now the T016 physics. This card reruns the three low-fluence members **from t = 0** with v1d, on the 32 cores that the three remaining v1b high-fluence members leave free. Do **not** stop those members; what happens to them is decided separately.

## Steps
1. `git pull --rebase origin ws`. Start in the background:
   `scripts\win\job.ps1 start -Id T016r -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T016r.yaml --max-cores 32"`
   It uses build_v1d, which T016c already verified, and tables v1b.
2. As members finish, push budget.csv and ConsToPrim counts (summed over **all** rank logs, since v1d writes one per rank). Image them as in T016 step 4 (`--tables D:\sim_runs\tables\v1b`). Run `diag_emission.py` on low_thin_G0 at its latest iteration.
3. When everything is done:
   - Run `collect_v0.py` and `report_v0.py` into results/T016r (`COMPARISON_v1d_low.md`).
   - Write the report `tasks/T016r.report.md` with the T016 columns, plus M_Cu vs M_src.
   - Copy to D:\Sync\T016r\.
   - Set the status to `review` and write the handoff.

## Not allowed
- Stopping the running T016 high-fluence members.
- Changing parameters. No reruns of members that succeed.
