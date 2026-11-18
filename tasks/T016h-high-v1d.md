---
id: T016h
title: High-fluence members with build v1d, rmax_par 1000, Picard it0–it4 (G0) — 32 free cores
owner: codex
status: running
started: 2026-11-18T09:19:20.372373+08:00
depends: []
milestone: D2
est_time: 0.1h operations + about 10–20h machine time
---

## Background
- v1d = conduction + Cu–air interdiffusion with the kinetic flux limit. The v1b high-fluence members aborted or stalled and were stopped (T016 done).
- T016s A/B from one checkpoint: rmax_par 1000 vs 100 gives budget increments within ≤ 2e-4 (E_wall 2e-3 in the first ns), mass-weighted Y within 2e-5, and ρ/p fields within 4e-5/3e-4 (L1). It is **2.15× faster**. Adopted as numerics (grade N).
- Picard: high fluence needs it3–it4 (STATE 01:40).

## Steps
1. `git pull --rebase origin ws`. Start in the background on the 32 cores T016 freed. T016r keeps its 32 cores; do not touch it:
   `scripts\win\job.ps1 start -Id T016h -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T016h.yaml --max-cores 32"`
   Check that the generated pluto.ini contains `rmax_par 1000`.
2. As iterations finish:
   - Push budget.csv, the ConsToPrim count summed over all ranks, and the source_meta.json of each iteration.
   - Image as in T016r (`--tables D:\sim_runs\tables\v1b`).
   - Run `diag_emission.py` on high_thin_G0 at its latest finished iteration.
   - Add dt and Nrkl to STATUS.md.
3. When everything is done:
   - Run `collect_v0.py` and `report_v0.py` into results/T016h (`COMPARISON_v1d_high.md`).
   - Write the report `tasks/T016h.report.md` with the T016 columns, plus the per-iteration target-model absorption vs PLUTO E_targ_abs.
   - Copy to D:\Sync\T016h\.
   - Set the status to `review` and write the handoff.

## Not allowed
- Parameter changes or reruns of members that succeed.
- If a member aborts, record the evidence (all rank logs are available in v1d) and continue with the others.
