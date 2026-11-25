---
id: T032
title: Second compute node (user's desk PC, DESKTOP-8MQBTK5) — toolchain, repo, tables, PLUTO build, smoke run; ≤ 12 cores
owner: codex-desk
status: ready
depends: []
milestone: D2
est_time: 1–2 h (mostly downloads and table build)
---

## Background
On 2026-11-25 the user approved installing a toolchain on the desk PC and using up to 12 of its 20 cores, so that it shares computation with the workstation. A read-only survey found:
- Core Ultra 7 265, 20 cores, 16 GB RAM (about 2 GB free at sampling).
- C: has 363 GB free; D: has only 3.5 GB.
- None of the following is installed: git, conda, gcc, MPI.

The workstation (owner `codex`) is unaffected. This node takes cards with `owner: codex-desk` only.

## Rules for this node
- **Locations:** everything goes under `C:\sim_tools` (tools) and `C:\sim_work` (repo, tables, runs). Never use D:.
- **Core limit:** at most 12 MPI ranks plus imaging workers in total. Start a job only if at least 3 GB RAM is free; otherwise wait.
- **Git:** branch `ws`, the same repository as the workstation.
  - Commit only `tasks/<card>.report.md`, `results/<card>/...` and `handoff/to_claude/<card>-desk-*.md`.
  - Never commit `results/run_registry.csv`. Restore it with `git checkout -- results/run_registry.csv` before each commit and list your runs in the report instead.
  - Push with `git pull --rebase origin ws` then `git push origin ws`.
- **Proxy:** if a download fails, try the HTTP proxy `127.0.0.1:7897`, the same one the workstation uses. If it is not available here, say so.

## Steps
1. **Install** (winget if present, otherwise the official installers, silent mode):
   - Git for Windows.
   - Miniforge3 into `C:\sim_tools\miniforge3`.
   - MSYS2 into `C:\sim_tools\msys64`, then `pacman -S --noconfirm mingw-w64-ucrt-x86_64-gcc make`.
   - Microsoft MPI runtime (msmpisetup.exe) and SDK (msmpisdk.msi).

   Record every version.
2. **Repository:** clone `https://github.com/JolinChenxx/Claude.git` into `C:\sim_work\Claude` on branch `ws`. Check that `git push --dry-run origin ws` works.
   - If authentication is needed, stop at this step.
   - Write `handoff/to_claude/T032-desk-auth.md` locally, saying exactly what login the user must do (for example `gh auth login` or Git Credential Manager), and report it in this thread.
3. **Python environment:** run `conda env create --file env\environment.yml --prefix C:\sim_tools\envs\lipsim`, then `python -m pytest -q tests`. Expect all tests to pass except `test_repository_is_clean`.
4. **Tables:** run `C:\sim_tools\envs\lipsim\python.exe -m lipsim.tables.build --config configs/tables/v0.yaml --out C:\sim_work\tables\v1c --workers 12`. Record the SHA-256 of eos.bin and opt.bin. The workstation hashes are in tasks/T016e.report.md; say whether they match.
5. **PLUTO build:** run `python scripts/build_pluto.py --problem pluto/lip --out C:\sim_tools\pluto_lip\build_v1s\mpi --platform windows --mpi --eos pvte --tc --extra-obj lip_tables.o lip_physics.o lip_glue.o --jobs 12`.
   - The compiler is the MSYS2 UCRT gcc. The workstation uses MinGW gcc 5.1, so bit-identity with the workstation is not expected.
   - If `build_pluto.py` needs a compiler path, pass or patch nothing in the repo. Set PATH for the child process only, and report what was needed.
6. **Smoke and cross-check:** run `configs/cases/v1bpde_low.yaml`, grid `smoke`, 4 ranks, tstop 110 ns, picard 0. Use a batch file at `C:\sim_work\smoke_T032.yaml` (root `C:\sim_work\runs\T032`). Report:
   - rc and wall time;
   - the last budget.csv row;
   - the relative difference of E_internal, M_Cu and E_rad at 110 ns from the cloud reference: v1n regression, 0–110 ns, smoke grid, 1 rank. The reference is in results/T032/cloud_ref_budget.csv, committed with this card.
7. **Report:** write `tasks/T032.report.md` with versions, hashes, timings, measured RAM per rank, and how many 8-rank G0 members this node can run at once. Push, then write `handoff/to_claude/T032-desk-ready.md`.

## Not allowed
- Touching the workstation.
- Changing physics, numerics, case files or `src/`.
- Deleting anything outside `C:\sim_work`.
- Using more than 12 cores.
