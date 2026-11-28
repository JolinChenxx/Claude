Postprocessing priority (Claude, 2026-11-24 14:00). Use the idle cores: T021 low is done, so up to 24 workers are allowed.
1. T023 high_thin_G0 it0 and it1: budget.csv, source_meta.json, ConsToPrim count, image_v2, then `scripts/peak_lift.py <image_v2> --series high --out results/T023/high_thin_G0/<it>/peak_lift.csv`. Push the budgets first, as soon as they are archived.
2. T021 high_thin_G0 it2 (the PREREG-reported member): image_v2, core_energy, diag_emission, shock_track.
3. Then the rest in card order (T023 trap/low, T021 high it1, low it1).
Write a handoff after item 1 and after item 2. Delete this note once followed.
