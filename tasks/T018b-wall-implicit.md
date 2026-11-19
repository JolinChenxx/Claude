---
id: T018b
title: Build v1i (backward-Euler wall exchange); stop T018 low members; rerun low G0 it0–it1 with v1i (16 cores)
owner: codex
status: running
started: 2026-11-19T16:20:29+08:00
depends: []
milestone: D2
est_time: 0.3h operations + machine time (measure it; v1h ran at ~34 ns/h)
---

## Background
T018 low dt ≈ 8e-4 ns at 200 ns. All-rank diagnostics (results/T018/inflight_dt/) show that the limit is kind 1, the **explicit wall exchange** in the first gas cell. Once the plume has left, that cell is rarefied (ρ ≈ 2e-3 kg/m³, T ≈ 360 K, ρe ≈ 160 J/m³), so the relaxation time to T_s is ~1e-11 s.

Build v1i (this commit, pluto/lip/lip_glue.c) integrates the same exchange flux by **backward Euler in T** (κ frozen, bisection, T_n bounded between T and T_s). It has no stability limit and the physics is unchanged. This is a numerical fix, not tuning.

Cloud A/B (smoke grid, v1pe_low, 0–300 ns, v1h vs v1i):
- E_internal, E_kinetic, M_Cu and E_src agree within 0.1%. E_wall agrees within 2% at 50 ns and within 0.2% from 150 ns on; E_gas_abs within 0.6%.
- Runtime 4m53s → 1m24s. ConsToPrim lines 160k → 103k.

## Steps
1. `git pull --rebase origin ws`. Build: `python scripts/build_pluto.py --problem pluto/lip --out D:\sim_tools\pluto_lip\build_v1i\mpi --platform windows --mpi --eos pvte --tc --extra-obj lip_tables.o lip_physics.o lip_glue.o --jobs 8`. Expect rc 0 and 7 replaced files. Record the exe SHA256.
2. Stop **only the two T018 low members** (low_thin_G0/it0 and low_trap_G0/it0). Kill their mpiexec/pluto process trees, which you can identify by their working directory under `D:\sim_runs\T018\low_*`. The T018 batch thread for each of them then ends with rc ≠ 0 and does not start it1, while the two high members keep running. **Delete nothing.** Write `results/T018/low_*/it0/STOPPED.md` with the last step/t/dt line. Authority: DECISIONS 2026-11-19 16:40 (superseded by v1i).
3. Launch: `scripts\win\job.ps1 start -Id T018b -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T018b.yaml --max-cores 16"`. Check `phase_explosion: true` in source_meta and `rmax_par 1000` in pluto.ini.
4. When low_thin_G0/it0 passes 200 ns, write a short handoff with:
   - the dt/Nrkl lines at about 100, 150 and 200 ns;
   - the latest `lip dt-limit` line;
   - budget.csv;
   - the simulated ns per wall-clock hour.
   Then use `python scripts/compare_budget.py` (or a plain table) to compare the budget at 100/150/200 ns against T018 low_thin_G0/it0 (v1h). Report only, and keep the run going.
5. Then follow T018 step 2 for every finished iteration: budgets, source_meta, ConsToPrim count, image_v1 and image_v2, diag_emission, core_energy (thin/trap, tables v1c) and shock_track. Write a handoff after low it1.
6. If a T018 high member ever logs `lip dt-limit ... kind 1` with dt < 2e-3 ns, mention it in STATUS.md. Do not act on it.
Report `tasks/T018b.report.md`.
