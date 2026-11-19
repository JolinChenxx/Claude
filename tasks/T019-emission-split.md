---
id: T019
title: Diagnostic split of the synthetic band signal into continuum and lines (reported members, image_v2)
owner: codex
status: ready
depends: []
milestone: D2
est_time: 0.1h operations + ~1–2h machine time (48 free cores)
---

## Background
The phase-explosion route is closed (STATE 2026-11-20 03:00). The open discrepancy is the late decay at high fluence. With PE off, high it2, image_v2 gives L(700)/L(300) ≈ 0.66 against 0.13 in the experiment.

This card asks which part of the synthetic band signal stays flat. A recombining plasma gives a falling free–bound/free–free continuum (∝ n_e n_i) and rising Cu I line emission, so the split tells us whether to look next at the line emissivity (LTE populations, IP lowering) or at the plume cooling.

The split is purely diagnostic. `band_image(components=...)` transfers each part on its own. No model is changed, and nothing from this card enters a reported metric. It is pre-declared here, before any result.

## Steps
1. `git pull --rebase origin ws`. Use tables v1c and the existing snapshots. Run 4 imaging jobs, 16 workers each, at most 3 in parallel:
   - `python -m lipsim.image D:\sim_runs\T016e\high_thin_G0\it2 --series high --scenarios configs/imaging/v2_continuum.yaml --tables D:\sim_runs\tables\v1c --out <run>\image_v2_continuum --workers 16`
   - the same with `v2_lines.yaml` → `image_v2_lines`
   - the same two for `D:\sim_runs\T016e\low_thin_G0\it1` with `--series low`.
   Use the actual run directories of these reported members. If they differ, take them from the registry.
2. Push to `results/T019/<member>/image_v2_{continuum,lines}/`: metrics CSVs, image_summary.json and validation. Frames stay local.
3. Also push `results/T019/split_table.csv`. For filter b, baseline defocus, thin and sa, give at every frame time: L_radiance for all (from the existing image_v2), continuum and lines.
4. Report `tasks/T019.report.md`, then one handoff when everything is done.
