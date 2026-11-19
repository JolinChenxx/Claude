---
id: T018
title: v1e + energy-limited phase explosion (v1pe cases): high G0 it0–it2, low G0 it0–it1, 32 cores
owner: codex
status: superseded
superseded_by: [T018b, T018c]
started: 2026-11-19T14:50:24+08:00
depends: []
milestone: D2
est_time: 0.1h operations + about 12h machine time
---

## Background
The v0 target model discarded the over-cap enthalpy flux, 23 µJ at high fluence it0. The new pre-declared extension (PREREG_v0, 2026-11-19 13:30; registry phase_explosion_model) uses that energy for explosive boiling at T_cap: m_pe = q_over/L_v. This is a vapour upper bound, with no free parameter. Target-model check in the cloud: high 3.2 → 8.1 ng, low 1.0 → 1.6 ng, and the energy budget closes exactly. Cases: `configs/cases/v1pe_{high,low}.yaml` (= v0 cases + `phase_explosion: true`).

## Steps
1. `git pull --rebase origin ws`. If 32 cores are free, start now; otherwise start as soon as they are. T016e high G1 is finishing and the T016e low G0 it2 confirmation can keep running:
   `scripts\win\job.ps1 start -Id T018 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T018.yaml --max-cores 32"`
   Use build_v1h (already built in T016f) and tables v1c. Check that each source_meta.json has `"phase_explosion": true` and that pluto.ini contains `rmax_par 1000`.
2. As iterations finish, push budget.csv, source_meta.json and the all-rank ConsToPrim count. Image with **image_v1 and image_v2**, run diag_emission and core_energy (`--nec thin|trap`, tables v1c) and shock_track on each finished iteration. Put dt/Nrkl in STATUS.md. Write a short handoff after each low it1 and high it2.
3. If a member stalls (dt < 1e-4 ns for more than 30 min), record it and continue with the others (known it3 defect).
4. At the end, run collect/report into results/T018 (`COMPARISON_v1pe.md`), write `tasks/T018.report.md`, copy to D:\Sync\T018, set `review` and write the handoff.
