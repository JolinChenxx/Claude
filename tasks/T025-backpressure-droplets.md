---
id: T025
title: v1k back pressure + v1j droplet upper bound (v1bpdr cases), high thin G0 it0–it2, low thin G0 it0–it1, on T024's 16 cores
owner: codex
status: running
depends: []
milestone: D2
est_time: 0.2h operations + ~3–4h machine time
started: 2026-11-24T16:15:13+08:00
---

## Background
PREREG_v0, "Combination declared 2026-11-24 ~16:10". The droplet upper bound (T021, high it2) meets PREREG (a)–(c): L(730)/L(300) is 0.155 against 0.107 in the experiment, and the S1 lift median is 177 µm. Back pressure (v1k) is the pre-declared Knight item. T025 runs both together, as the current most complete model. It uses the build_v1m executable already on the workstation with BD_ON 0, which is bit-identical to v1k. Check that pluto.ini has `BD_ON 0`, `BACKP 1` and a non-zero `DROP_M`.

## Steps
1. `git pull --rebase origin ws`. After T024 is stopped (T024-stop.md), launch with `scripts\win\job.ps1 start -Id T025 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T025.yaml --max-cores 16"`.
2. Per finished iteration, as in T023 step 4 (budget, source_meta, ConsToPrim, image_v2 with frames, diagnostics, peak_lift). Report the residuals at 200/250/300/400 ns, as in T021.
3. Handoffs after high it0, low it1 and high it2. Time box as before.
Report `tasks/T025.report.md`.

## Not allowed
- Physics, numerics or case changes. Stopping T023. Deleting data.
