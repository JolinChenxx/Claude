---
id: T026
title: v1n energy-limited droplet release + back pressure (v1bpde cases), high thin G0 it0–it2, low thin G0 it0–it1; 16 cores when free
owner: codex
status: ready
depends: []
milestone: D2
est_time: 0.5h operations + ~3–4h machine time
started:
---

## Background
PREREG_v0, "v1n correction declared 2026-11-24 ~17:15". The time-uniform droplet release adds floor energy at low fluence (+1.1–1.7 µJ in T021 low it1 and T025 low it0). In v1n, droplets evaporate only while the mixture stays ≥ T_boil, and the remainder stays condensed (budget column M_drop_condensed). It is otherwise identical to T025.

Cloud smoke check (low, 0–400 ns):
- Same budget as v1bpdr up to 200 ns.
- 1.0 of 5.6 ng stays condensed by 300 ns.
- The smoke domain loses energy at its boundary after 200 ns, so the residual test needs G0.

## Steps
1. `git pull --rebase origin ws`. Build build_v1n into `D:\sim_tools\pluto_lip\build_v1n\mpi` with the T024 build command (output dir changed). Expect rc 0. Run `python -m pytest -q tests/test_breakdown.py tests/test_knight_bp.py`.
2. Launch once ≥ 16 physical cores are free, for example after T023 high_trap it2 and low_thin it1 finish: `scripts\win\job.ps1 start -Id T026 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T026.yaml --max-cores 16"`. Check that pluto.ini has `DROP_TB 2835`, `BACKP 1` and `BD_ON 0`.
3. Per finished iteration, as in T025: residuals at 200/250/300/400 ns, M_drop_condensed, image_v2 with frames, diagnostics and peak_lift.
4. Handoffs after low it0, high it0 and at the end. Time box as before. Report `tasks/T026.report.md`.

## Not allowed
- Physics, numerics or case changes. Stopping T023/T025. Deleting data.
