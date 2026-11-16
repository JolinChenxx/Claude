---
id: T014
title: v0.1 — rerun the T011 members with the vapour-injection correction (+ low-fluence G1) and image them
owner: codex
status: review
depends: [T011]
milestone: D1
est_time: 0.3h operations + about 2.5h machine time
started: 2026-11-17T01:34:20+08:00
---

## Background
The 26M ConsToPrim warnings in T011 came from injecting ṁu_v momentum into the cells next to the target. Correction (commit ca85775): inject only mass and stagnation enthalpy. Cloud check (low fluence G0, 0–150 ns): warnings down by about 10x, energy budget residual 0.00%, **and low fluence now ignites** (gas absorption 89 µJ). This card reruns all T011 members with the new build.

## Steps
1. `git pull --rebase origin ws`. Rebuild (new directory, do not overwrite the T011 build):
   `python scripts/build_pluto.py --problem pluto/lip --out D:\sim_tools\pluto_lip\build_v01\mpi --platform windows --mpi --eos pvte --extra-obj lip_tables.o lip_physics.o lip_glue.o --jobs 8`
   Acceptance: rc=0, same 3 replaced files. Copy build_provenance.json to results/T014/.
2. Background: `scripts\win\job.ps1 start -Id T014 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T014.yaml --max-cores 64"` (the runner now packs by cores; 64 in total: 4×8 + 2×16). If T013a is still running, use `--max-cores 32` and record that. End the pass.
3. Later passes: as soon as some members finish, run imaging in the background with `scripts/image_members.py D:\sim_runs\T014 --series low|high --tables D:\sim_runs\tables\v0 --workers 16` (members that have finished first, as in T011).
4. When everything is done: `python scripts/collect_v0.py D:\sim_runs\T014 --out results/T014`, then `python scripts/report_v0.py results/T014 --out results/T014/COMPARISON_v01.md`. Report `tasks/T014.report.md` (per run: rc, wall-clock time, ConsToPrim lines, final E_gas_abs/E_rad/M_Cu). Copy the report and figures to D:\Sync\T014\. Set to `review`, write the handoff, push.

## Not allowed
- Changing members or parameters; reruns; image adjustments. Record ConsToPrim warnings as they are (report the count, do not stop).
