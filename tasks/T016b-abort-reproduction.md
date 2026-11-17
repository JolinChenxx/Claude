---
id: T016b
title: Reproduce the T016 low-fluence aborts from the last output with a diagnostic build (rank logs + finite guards)
owner: codex
status: done
started: 2026-11-18T02:34:57.775986+08:00
depends: []
milestone: D2
est_time: 0.2h operations + under 1h machine time (8 cores per member)
---

## Background
T016 low_thin_G0 and low_trap_G0 (it0) aborted at about 320–355 ns on MPI ranks 1 and 4. The build wrote no logs for non-zero ranks, so the cause is unknown.

Diagnostic build v1c (this commit). The physics is unchanged; a cloud smoke run is bit-identical to v1b (energies and M_Cu equal to all digits). Changes:
- `MULTIPLE_LOG_FILES YES`, so every rank writes `pluto.N.log`.
- A finite-value guard in the interdiffusion flux. It prints all inputs before quitting.
- The table T-inversion now returns the table floor, flagged as a failure, for p or e ≤ 0 or NaN. Before, it silently returned the table maximum, 150 kK.

## Steps
1. `git pull --rebase origin ws`. Build into a new directory:
   `python scripts/build_pluto.py --problem pluto/lip --out D:\sim_tools\pluto_lip\build_v1c\mpi --platform windows --mpi --eos pvte --tc --extra-obj lip_tables.o lip_physics.o lip_glue.o --jobs 8`
   Acceptance: rc=0, 7 replaced files.
2. For each of `low_thin_G0` (restart file 141) and `low_trap_G0` (file 142):
   - **Copy** `D:\sim_runs\T016\<member>\it0` to `D:\sim_runs\T016b\<member>`. Keep `lip_tables`, `pluto.ini`, `grid.out`, `restart.out`, `dbl.out` and `data.<n>.dbl`. Do not modify the original directory.
   - In the copy, run in the background through job.ps1:
     `mpiexec -n 8 D:\sim_tools\pluto_lip\build_v1c\mpi\pluto.exe -restart <n> -dec 8 1`
   - Use a wall-clock limit of 60 minutes, or let it reach tstop. Both members can run alongside the 4 running T016 members (16 extra cores).
3. When each run ends (abort, limit or tstop), collect into `results/T016b/<member>/`:
   - rc and the last `budget.csv` rows;
   - for every `pluto.N.log`: the last 40 lines, plus all lines starting with "!" that do not contain "ConsToPrim: rhoe < 0", with the last 200 kept;
   - the step and t of the last output.
   Write `tasks/T016b.report.md` and `handoff/to_claude/T016b.md`, set the status to `review`, and push.

## Not allowed
- Changing parameters, the ini file or the tables. Only the restart and the diagnostic build are allowed.
- Restarting the 4 running T016 members.
