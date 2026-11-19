---
id: T020
title: Emission-weighted n_e, T per frame for the reported members (LTE-validity diagnostic, pre-registered)
owner: codex
status: done
started: 2026-11-20T06:51:24+08:00
depends: []
milestone: D2
est_time: 0.1h operations + < 1h machine time
---

## Background
PREREG_v0, "Diagnostic declared 2026-11-20 06:50": is LTE ionisation balance valid in the emitting core? The criteria are evaluated in the cloud from your output. This is a diagnostic only.

## Steps
1. `git pull --rebase origin ws`. For **T016e high_thin_G0/it2** and **T016e low_thin_G0/it1** (the reported members), run:
   `python scripts/diag_emission.py <run_dir> --tables D:\sim_runs\tables\v1c --times 100 150 200 250 300 400 500 600 700 800 1000 1200 --filter 510 12 --out results/T020/<member>_diag.csv`
   (high: up to 800 ns only). These are light, serial runs, and both may run in parallel.
2. Push the two CSVs and the logs. Report `tasks/T020.report.md`, then one handoff.
