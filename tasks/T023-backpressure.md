---
id: T023
title: v1k Knight evaporation with back pressure (v1bp cases): high thin/trap G0 it0–it2, low thin/trap G0 it0–it1, high thin G1 it0; 48 cores; plus Fig. 2 surface check
owner: codex
status: blocked
depends: []
milestone: D2
est_time: 0.5h operations + several hours machine time (measure it)
started: 2026-11-24T09:41:21+08:00
---

## Background
PREREG_v0, "v1k extension declared 2026-11-24". In the T018c 62 ns extract, the wall cell on the beam holds 6–7×10³ kg/m³ of Cu under ~14 kbar gas, and > 90% of the laser power is absorbed within 2 µm of the face. The cause is the sonic Knight source, which ignores the back pressure. v1k applies Knight's relations at the exit Mach number set by the wall-cell gas pressure (no new parameter; ṁ = 0 when p_w ≥ p_sat, with no recondensation). It also writes the wall pressure as a 4th column of surftrans_*.txt, which the target model uses at the next Picard iteration.

The user has asked for the whole machine (DECISIONS 2026-11-24). **T021 keeps running on its 16 cores**; this card takes 48 (5 members: 4×10 + 1×8).

Cloud checks (smoke grid, 5 µm wall cell, high, v1k build): see the "Cloud checks" section at the end.

## Steps
1. `git pull --rebase origin ws`. Build into `D:\sim_tools\pluto_lip\build_v1k\mpi`:
   `python scripts/build_pluto.py --problem pluto/lip --out D:\sim_tools\pluto_lip\build_v1k\mpi --platform windows --mpi --eos pvte --tc --extra-obj lip_tables.o lip_physics.o lip_glue.o --jobs 8`
   Expect rc 0 and 7 replaced files. Record the exe SHA256. `python -m pytest -q tests/test_knight_bp.py` must pass.
2. Launch: `scripts\win\job.ps1 start -Id T023 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T023.yaml --max-cores 48"`.
   Check that pluto.ini has `BACKP 1`, `DROP_M 0` and `rmax_par 1000`. At it1, check that source_meta.json has `"wall_pressure_from_previous_run": true`.
3. **Early check, at about 80 ns of high_thin_G0/it0 (do not wait for the end):** extract the dbl snapshots nearest 40, 60 and 80 ns for i = 0..30 and j = 0..40, with the same fields and format as `results/T018c/extract/` (script `results/T018c/extract_it2_fields.py`). Push them to `results/T023/extract/` and write a short handoff. Keep the runs going.
4. For every finished iteration, as in T021 step 3: budget.csv (now with a 15th column, M_bp_suppressed), source_meta.json, the all-rank ConsToPrim count, image_v2, diag_emission (times 100–1200), core_energy (thin/trap, tables v1c) and shock_track. Run postprocessing on the hyperthreads (≤ 16 workers) while the MPI runs continue.
   Then run `python scripts/peak_lift.py <image_v2 dir> --series high|low --out results/T023/<member>/<it>/peak_lift.csv` (the default b-filter frames; thin and sa are both read).
5. **Fig. 2 surface position (light, any time):** in the paper PDF on the workstation (main text, Fig. 2; SI p. 6 for S4), look for any marking of the target surface: a line, an arrow, a label, a scale origin, or text on how the crops were taken. Push a crop of Fig. 2 at the PDF's native resolution to `data/exp/paper/fig2.png`, together with the verbatim caption and any sentence on the imaging geometry (camera direction, target shape and size, whether the target is polished) in `data/exp/paper/fig2_notes.md`. Do not measure anything from it; Claude does that.
6. Handoffs: after step 3, after high it0, after low it1, and at the end, plus anything abnormal. Time box: if a member advances < 1 ns of t in 60 min, stop that member only. Delete nothing; push STOPPED.md and the budget.
Report `tasks/T023.report.md`.

## Acceptance criteria
- Build rc 0, the test passes, and the ini values are as in step 2.
- Extracts and per-iteration outputs as listed are pushed. Every number in the handoffs is copied from a file.

## Not allowed
- Any change to physics, numerics or the case files. Stopping T021. Deleting data.

## Cloud checks
- BACKP = 0 against v1j (droplets off): budget.csv is bit-identical over the 121 rows to 69.5 ns (max |Δ| = 0.0). M_bp_suppressed is 0.
- BACKP = 1, at 68.5 ns: 30% of the sonic vapour is suppressed (M_src 1.34 vs 1.88 pg). E_gas_abs is 192 vs 204 µJ, E_wall 23.8 vs 16.4 µJ. 0 ConsToPrim lines in both runs. On the smoke grid the wall cell is 5 µm, so the pile-up there is diluted. The decisive check is the 1 µm wall cell of G0 (step 3).
- Picard it1 path: merge_surftrans gives `pwall` (max 6.2 kbar). `lipsim.target.build --shielding` reads it (`wall_pressure_from_previous_run: true`), and the vapour falls from 3.20 to 2.27 ng (partial it0 input). tests/test_knight_bp.py: 5 passed. Full suite: 49 passed.
