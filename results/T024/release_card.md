---
id: T024
title: v1m two-temperature avalanche breakdown (v1bd cases): high thin G0 it0–it2, low thin G0 it0–it1; 16 cores now, more as T023 frees them
owner: codex
status: running
depends: []
milestone: D2
est_time: 0.5h operations + several hours machine time
started: 2026-11-24T14:53:35+08:00
---

## Background
PREREG_v0, "v1m extension declared 2026-11-24". The user chose a two-temperature breakdown upgrade. In the pulse phase the model ignites late and at the face, while the experiment's shock is ~20 ns ahead and its emission core is elevated. v1m adds non-equilibrium Cu electrons (tracer 2) with a quasi-steady electron temperature, Lotz ionisation and Saha three-body recombination, applied only while the pulse is on. It builds on v1k (back pressure on). NTRACER is now 2, and budget.csv has two extra columns (E_bd_transfer, E_bd_store).

## Steps
1. `git pull --rebase origin ws`. Build into `D:\sim_tools\pluto_lip\build_v1m\mpi` with the T023 build command (output dir changed). Expect rc 0 and 7 replacements. Record the SHA256. `python -m pytest -q tests/test_breakdown.py tests/test_knight_bp.py` must pass.
2. Launch with `--max-cores 16`: `scripts\win\job.ps1 start -Id T024 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T024.yaml --max-cores 16"`. Check that pluto.ini has `BD_ON 1`, `BACKP 1`, `SIG_EN 1e-19` and `LOTZ_A 4.5e-18`.
3. Early check, as in T023 step 3: when high_thin_G0/it0 passes 80 ns, extract the snapshots nearest 30, 40, 60 and 80 ns (i = 0..30, j = 0..40, T018c format; tr2 included) and push them to results/T024/extract/. Also push the budget.csv rows up to 80 ns, then write a handoff.
4. Per finished iteration, as in T023 step 4 (budget, source_meta, ConsToPrim, image_v2 with frames, diagnostics, peak_lift). Report closure, including E_bd_store − E_bd_transfer.
5. When T023 members finish, do not start new T024 members; Claude adds the σ_en sensitivity members by a to_codex note.
Handoffs after step 3, after high it0, and at the end. Time box as in T023. Report `tasks/T024.report.md`.

## Not allowed
- Physics, numerics or case changes. Stopping T023. Deleting data.

## Cloud checks (fixed build, commit 6c41ee7; smoke grid, 5 µm wall cell, high, it0, 0–160 ns)
- The first v1m build double-counted the ionisation store (+69 µJ by 120 ns). Fixed: tracer 2 is now the advected excess (STATE 2026-11-24 ~16:05).
- Fixed build:
  - dE + store against the net sources: 205.38 / 205.71 µJ at 80 ns and 216.44 / 216.78 µJ at 120 ns, the same closure as v1k off (206.02 / 206.35 and 217.29 / 217.62).
  - At 160 ns both v1m and v1k lose about 26 µJ through the small smoke-domain boundary. This does not happen on G0.
  - E_bd_store = E_bd_transfer up to 120 ns (max 0.87 µJ). After the pulse the store returns to 0, with a residual transfer of 0.18 µJ.
  - 0 ConsToPrim lines.
- Onset (E_gas_abs = 1 µJ): about 28.5 ns with BD on, against 29.7 ns off. On the 5 µm smoke cell the effect is small. G0 (1 µm) is the test.
- BD_ON = 0 is bit-identical to v1k (81 rows to 50 ns). tests/test_breakdown.py and tests/test_knight_bp.py: 9 passed.
- Hold released (Claude). Build from the current ws.
