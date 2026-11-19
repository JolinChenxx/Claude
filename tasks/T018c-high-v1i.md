---
id: T018c
title: Stop T018 high members; rerun high G0 it0–it2 with build v1i (16 cores)
owner: codex
status: ready
depends: []
milestone: D2
est_time: 0.1h operations + machine time
---

## Background
T018b accepted the v1i build:
- the low thin budgets at 100/150/200 ns agree with v1h within 0.03% (E_gas_abs within 1.4%, E_wall within 1.6%);
- dt at 200 ns rose from 8e-4 ns to 7.5e-2 ns.

The T018 high members on v1h run at ~80 ns/h, dt 4–6e-3 ns, with the trap member wall-limited (kind 1). So high it0–it2 would take more than 24 h. Rerun them on v1i, so that every phase-explosion result comes from the same build. Authority: DECISIONS 2026-11-19 17:00.

## Steps
1. Stop the T018 batch (PID 8480), which now runs only the two high members: stop the batch and its mpiexec/pluto trees. **Delete nothing.** Write `results/T018/high_*/it0/STOPPED.md` with the last step/t/dt line and the last budget t_ns. Set T018 to `superseded` in its card, citing T018b/T018c.
2. Launch: `scripts\win\job.ps1 start -Id T018c -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T018c.yaml --max-cores 16"`. Check `phase_explosion: true` in source_meta and `rmax_par 1000` in pluto.ini.
3. For every finished iteration, as in T018 step 2: budgets, source_meta, ConsToPrim count, image_v1 and image_v2, diag_emission, core_energy (thin/trap, tables v1c) and shock_track. Give T018b and T018c images/diagnostics priority over T016i and T017a. Write a handoff after high it0 and high it2.
4. Use the 16 cores freed by T018 for the T018b/T018c imaging and diagnostics first, then for T016i and T017a.
Report `tasks/T018c.report.md`.
