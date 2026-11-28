---
id: T021
title: v1j droplet upper bound (v1dr cases): high G0 thin it0–it2, low G0 thin it0–it1, 16 cores
owner: codex
status: done
superseded_by: T026
started: 2026-11-24T08:38:24+08:00
depends: []
milestone: D2
est_time: 0.3h operations + several hours machine time (measure it)
---

## Background
The user chose option 1 on 2026-11-24, the droplet evaporation bounds (PREREG_v0, "v1j extension declared 2026-11-24 08:20").

The upper bound puts the condensed ejecta, about 6.4 ng at high and 5.2 ng at low fluence, into the Cu core between the end of the pulse and t_ref. They arrive at rest and take the latent heat from the gas. The lower bound is the existing PE-off results.

Build v1j is pluto/lip/lip_glue.c plus definitions.h with 5 new user parameters. Cloud checks:
- With droplets off (DROP_M = 0) it is bit-identical to v1i.
- With droplets on (smoke grid, 0–300 ns), breakdown is unchanged. The gas loses about 8 µJ by 300 ns. M_Cu goes from 1.8 to 5.4 ng at 300 ns.

## Steps
1. `git pull --rebase origin ws`. Build into `D:\sim_tools\pluto_lip\build_v1j\mpi`:
   `python scripts/build_pluto.py --problem pluto/lip --out D:\sim_tools\pluto_lip\build_v1j\mpi --platform windows --mpi --eos pvte --tc --extra-obj lip_tables.o lip_physics.o lip_glue.o --jobs 8`
   Expect rc 0 and 7 replaced files. Record the exe SHA256.
2. Launch: `scripts\win\job.ps1 start -Id T021 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T021.yaml --max-cores 16"`.
   Check that the generated pluto.ini has `DROP_M` = removal − this iteration's vaporised mass (≈ 6.3e-12 high / 5.2e-12 low at the converged iteration; larger vapour, so smaller DROP_M, at it0), `DROP_T0` ≈ 8.47e-08, `DROP_T1` 3e-07 and `rmax_par 1000`.
3. For every finished iteration: budget.csv, source_meta.json, the all-rank ConsToPrim count, image_v2, diag_emission (times 100–1200 as in T020), core_energy (thin, tables v1c) and shock_track. Run postprocessing in parallel on the free cores.
4. Write a handoff after high it0, after low it1 and after high it2, plus anything abnormal. Time box: if any member advances < 1 ns of t in 60 min, stop that member only. Delete nothing; push STOPPED.md and the budget.
Report `tasks/T021.report.md`.
