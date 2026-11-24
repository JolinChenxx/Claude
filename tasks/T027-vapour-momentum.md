---
id: T027
title: v1p vapour exit momentum + v1n + v1k (v1bpdem cases), high thin G0 it0–it2, low thin G0 it0–it1; 16 cores when free
owner: codex
status: running
depends: []
milestone: D2
est_time: 0.5h operations + ~3–4h machine time
started: 2026-11-24T21:00:13+08:00
---

## Background
PREREG_v0, "v1p correction declared 2026-11-24 ~20:25". The user chose this ("先做第1个，蒸气向上动量"; physics only, no tuning). The vapour now carries its Knight exit momentum ṁu_v (+z) out of the wall cell. Otherwise identical to T026, so T026 is the reference.

Cloud smoke check (high, smoke grid, 0–160 ns, v1p against v1n):
- Momentum injected: 2.5e-9 kg m/s by 160 ns.
- Closure −0.63% at 80–120 ns, against −0.16%. After 140 ns both lose energy at the small-domain boundary.
- ConsToPrim: 10 278 lines against 283. Higher, but small. Report the count on G0 (PREREG (a)).
- Gas absorption at 40/60 ns: 15/129 µJ against 33/143 µJ. The momentum moves vapour off the face, and the onset is slightly later.

## Steps
1. `git pull --rebase origin ws`. Build build_v1p into `D:\sim_tools\pluto_lip\build_v1p\mpi` with the T026 build command (output dir changed). Expect rc 0. Run `python -m pytest -q tests/test_breakdown.py tests/test_knight_bp.py`.
2. Launch once ≥ 16 physical cores are free, before any new T023 trap postprocessing: `scripts\win\job.ps1 start -Id T027 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T027.yaml --max-cores 16"`. Check that pluto.ini has `VMOM 1`, `DROP_TB 2835`, `BACKP 1` and `BD_ON 0`.
3. Per finished iteration, as in T026: residuals at 200/250/300/400 ns, ConsToPrim, P_vap_mom, M_drop_condensed, image_v2 with frames, diagnostics and peak_lift. In the postprocessing queue this card ranks after T026.
4. Handoffs after high it0 and low it0 (production and imaging), and at the end. Time box as before. Report `tasks/T027.report.md`.

## Not allowed
- Physics, numerics or case changes. Stopping other cards. Deleting data.
