---
id: T010
title: Windows build of pluto_lip, full-resolution property tables, smoke check (serial/MPI/cross-platform)
owner: codex
status: running
depends: [T008]
milestone: D0
est_time: 1.5h (about 1h of it machine time)
started: 2026-11-16T10:14:26.5211920+08:00
---

> Revised by Claude to match the code actually delivered (commit dd1a56c or later; use the latest `ws`). The PLAN v3 V3/V5/V8b/V13 cases move to D1–D2 (recorded in STATE); this card does only what the critical path needs. **If T009 is still running in the background, do not stop it**; this card's background jobs share the cores with it (60 workers total).

## Goal
Build the "unmodified PLUTO core + `pluto/lip` modules" on the workstation (serial and MPI), generate the full-resolution property tables, and confirm with a smoke case that Windows agrees with the cloud Linux build and serial agrees with MPI.

## Steps
1. `git pull --rebase origin ws`. Using `D:\sim_tools\envs\lipsim\python.exe` (via `scripts/win/run_lipsim.ps1`):
   - `python scripts/check_provenance.py` (must report 0 violations)
   - `python -m pytest -q` (all must pass. The C kernel tests need gcc; if `tests/test_c_kernels.py` is skipped because gcc is not on PATH, prepend the MinGW directory from T005 to PATH, rerun, and record that in the report)
2. Build (the official archive and Windows adapters come from `third_party/pluto/`):
   - Serial: `python scripts/build_pluto.py --problem pluto/lip --out D:\sim_tools\pluto_lip\build\serial --platform windows --eos pvte --extra-obj lip_tables.o lip_physics.o lip_glue.o --jobs 8`
   - MPI: same command with `--mpi --out D:\sim_tools\pluto_lip\build\mpi`
   - Acceptance: `make rc=0` for both; `build_provenance.json` must list exactly three replaced upstream files, matching the three `.diff` files in `pluto/overrides/`. Copy both `build_provenance.json` files to `results/T010/`.
   - Engineering fixes (make, paths, MinGW / MS-MPI flags, `windows_compat`) may be made **in scripts/build_pluto.py or the windows adapters**, each listed in the report. **Do not change the physics or numbers in `pluto/lip/*.c`/`*.h`**; if they fail to compile, report it as blocked and include the error.
3. Full-resolution tables (background via `job.ps1`, `--workers 48`; leave the rest for T009):
   `python -m lipsim.tables.build --config configs/tables/v0.yaml --out D:\sim_runs\tables\v0 --workers 48`
   Copy `manifest.json` to `results/T010/tables_manifest.json`. The tables (several hundred MB) are not committed.
4. Smoke check (low-resolution tables, same inputs as the cloud reference; run once each table step is done):
   - `python -m lipsim.batch configs/batches/T010_smoke.yaml --max-cores 2`
   - `python -m lipsim.batch configs/batches/T010_mpi.yaml --max-cores 4`
   - Cross-platform: `python scripts/compare_budget.py verify/ref/smoke_v0/budget_it0_linux.csv D:\sim_runs\T010\smoke\high_thin_smoke\it0\budget.csv --tol 0.005`
   - Serial vs MPI: `python scripts/compare_budget.py D:\sim_runs\T010\smoke\high_thin_smoke\it0\budget.csv D:\sim_runs\T010\mpi\high_thin_smoke_mpi4\it0\budget.csv --tol 0.005`
   - Imaging chain: `python -m lipsim.image D:\sim_runs\T010\smoke\high_thin_smoke\it0 --series high --tables verify/ref/tables_v0_lowres --out D:\sim_runs\T010\smoke_img --workers 8` (just needs to run through and produce `image_summary.json`)
   - Record the wall-clock time and `ConsToPrim` warning count of each run (`grep -c ConsToPrim pluto.log`).
   - Put the full output of both comparisons, the two `budget.csv` files and `image_summary.json` in `results/T010/`.
5. Report `tasks/T010.report.md`, set to `review`, write the handoff, push.

## Acceptance criteria
- check_provenance has 0 violations; pytest all pass (C tests not skipped)
- Serial and MPI builds both succeed; the list of replaced upstream files matches `pluto/overrides/`
- The tables build without errors, the manifest records 3 table files (eos/opt/pop), and the shape is [81, 145, 14]
- Both `compare_budget.py` comparisons exit 0 (worst relative difference ≤ 0.5%); 0 `ConsToPrim` warnings; imaging chain runs through

## Not allowed
- Changing `pluto/lip/` or the physics part of `src/lipsim/`, numbers in `configs/`, or the registry; relaxing tolerances.
- Comparing the smoke run's images with the experiment.
- Stopping or interfering with T009.
