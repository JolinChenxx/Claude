---
id: T024
title: v1m two-temperature avalanche breakdown (v1bd cases): high thin G0 it0–it2, low thin G0 it0–it1; 16 cores now, more as T023 frees them
owner: codex
status: draft
depends: []
milestone: D2
est_time: 0.5h operations + several hours machine time
started:
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

## Cloud checks (smoke grid, 5 µm wall cell, high, it0)
- BD_ON = 0 with NTRACER = 2: budget.csv is bit-identical to v1k over the 81 rows to 50 ns (max |Δ| = 0.0).
- BD_ON = 1: gas-absorption onset (1 µJ) at 28.1 ns, against 29.7 ns with it off. E_gas_abs at 26/28/30/34 ns is 0.12/0.91/5.27/14.1 µJ, against 0/0.01/2.13/12.9 µJ off. 0 ConsToPrim lines up to 45 ns. The cumulative store transfer is −0.07 µJ at 28 ns. The build now also writes the current store (E_bd_store) so the hydro-step change is visible.
- tests/test_breakdown.py: 4 passed; the C kernels match independent Python references. Lotz against a Maxwellian quadrature agrees to < 0.2%. The ionisation ODE steady state is Saha to < 1e-6.
- The 0-D prototype (Knight vapour at 25 ns) ignites in about 3 ns with 2T, and does not ignite in 30 ns under single-T LTE.
