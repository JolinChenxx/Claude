---
id: T044
title: Imaging fix v2.1 (continuum from all ion charges) — rebuild tables as v1c2, verify solver tables unchanged, re-image the high-fluence production runs, rerun T043 diagnostic
owner: codex
status: running
started: 2026-11-26T23:00:46+08:00
depends: []
milestone: D2
est_time: table build ~1 h (parallel), then imaging ~15 min per run
---

## Background
See PREREG_v0, "Continuum charge states in the imaging (v2.1 imaging fix)". This is a correctness fix, and no solver run is repeated. HIGH PRIORITY: it goes before any remaining T040 G1 / low-fluence work.

## Steps
1. `git pull` on ws, at the commit that contains this card.
2. Rebuild the property tables with the same config and options as D:/sim_runs/tables/v1c, at the current commit, into D:/sim_runs/tables/v1c2.
   - eos.bin and opt.bin must be byte-identical, or field-identical, to v1c. Report both sha256 values.
   - pop.bin must now contain nion_z4 and nion_z5.
   - If eos.bin or opt.bin differ, stop and write a block handoff.
3. Re-image with `--tables D:/sim_runs/tables/v1c2`, using the same scenarios as image_v2, into `image_v2c`, and with `--t-offset-ns 28` into `image_v2c_t28`. Run peak_lift on both. Runs:
   - T031/high_v1rs/it2
   - T039/high_wf/it2
   - T040/high_wf_chain/it2
   - T042/high_bd (it2)
   - T026/high_thin_G0/it2
4. Rerun `scripts/emission_layers.py` on T031/high_v1rs/it2 and T039/high_wf/it2 with `--tables D:/sim_runs/tables/v1c2`, into results/T044/<batch>_<member>_<it>_emission.csv.
5. Archive the image metrics and frames to results/<batch>/<member>/<it>/image_v2c{,_t28}/, as before.
6. Write tasks/T044.report.md with the following, side by side for image_v2 (old) and image_v2c (new), band b, sa, defocus `high`:
   - σx / σz at 150, 300 and 500 ns;
   - zc;
   - L500/L300 and L730/L300.
7. Write handoff/to_claude/T044.md and push.

## Acceptance
- The table hash checks pass.
- Five re-imaged runs, each with both time conventions.
- Two emission CSVs.
- The report table is complete.
