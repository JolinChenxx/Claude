---
id: T043
title: High-fluence emission-height decomposition (diagnostic) — scripts/emission_layers.py on three existing runs
owner: codex
status: running
started: 2026-11-26T21:30:52+08:00
depends: []
milestone: D2
est_time: ~5–15 min per run (single core each; run the three in parallel)
---

## Background
See PREREG_v0, "T043 emission-height decomposition". Diagnostic only. No solver runs and no changes to inputs.

## Steps
1. `git pull` on ws.
2. For each of D:/sim_runs/T031/high_v1rs/it2, D:/sim_runs/T039/high_wf/it2 and D:/sim_runs/T026/high_thin_G0/it2, run:
   `python scripts/emission_layers.py <run_dir> --tables D:/sim_runs/tables/v1c --out results/T043/<batch>_<member>_<it>.csv --tmax 200`
   - Smoke-tested in the cloud: about 1 s per snapshot on low-res runs.
3. Write tasks/T043.report.md with a table for each run at t ≈ 30, 50, 80, 110, 150, 200 ns, giving:
   - z50_um, z90_um, f_lt10, f_10_50, f_gt50, f_lines_lt10;
   - T_5, ne_5, rho_5;
   - T_100, Tmax_axis_K, zTmax_axis_um.
4. Write handoff/to_claude/T043.md and push.

## Acceptance
- Three CSVs, each covering every snapshot ≤ 200 ns.
- The report table is complete.
