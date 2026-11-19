---
id: T016d
title: Stop the stalled high_trap_G0 it3 and diagnose its time-step limiter with the instrumented build v1f
owner: codex
status: ready
depends: []
milestone: D2
est_time: 0.2h operations + 20 min machine time (8 cores)
---

## Background
T016e/high_trap_G0/it3 is stalled. dt has stayed at 3.7e-5 ns for more than 1 h (83 ns of 800), with Nrkl 2, while the thin member at a similar time runs at dt ≈ 3e-3 ns. A hydro CFL limit at this dt would imply a signal speed of about 8000 km/s, which is unphysical. The suspected cause is the LipSource limiter |Δ(ρe)| ≤ 0.2 ρe applied in a near-vacuum cell.

Build v1f (this commit) changes no physics. It only adds a log line every 1000 steps whenever a source limit is the binding constraint: "! lip dt-limit: step … kind … [i,j] rho T rhoe q dt_src".

## Steps (standing authorization, docs/DECISIONS.md 2026-11-18 08:20)
1. Stop **only** the high_trap_G0 member processes (its mpiexec/pluto.exe for it3). The T016e batch will record it as failed and continue the other members; that is expected. Do not stop the batch or any other member, and delete nothing. Record the last t_ns, the dbl index and the time under `results/T016e/high_trap_G0/stopped/`.
2. Build `python scripts/build_pluto.py --problem pluto/lip --out D:\sim_tools\pluto_lip\build_v1f\mpi --platform windows --mpi --eos pvte --tc --extra-obj lip_tables.o lip_physics.o lip_glue.o --jobs 8` (rc=0, 7 replaced files).
3. Copy it3 (lip_tables, pluto.ini, grid.out, restart.out, dbl.out and the last data.<n>.dbl) to `D:\sim_runs\T016d\high_trap_G0_it3`. Restart the copy with the v1f exe, 8 ranks, `-dec 8 1 -restart <n>`, with a **20-minute** wall limit.
4. Collect from every `pluto.N.log` all "! lip dt-limit" lines, the last 20 step/Nrkl lines and the "!" lines other than ConsToPrim, into `results/T016d/`. Report, set `review`, write a handoff and push.
