---
id: T047
title: (desk node) Picard it1–it2 chains for the T046 pair (G0, G0w), v1rs+v1u high, to 320 ns; same imaging/diagnostics as T046
owner: codex-desk
status: ready
depends: [T046]
milestone: D2
est_time: G0 ~2 × 1.5 h; G0w ~2 × 6 h (more with more cores)
---

## Background
PREREG_v0, "T046". This repeats the near-wall resolution test at converged plasma shielding, as the independent review recommends. The user is not using the desk this weekend: use as many cores as the machine has, keeping 2 logical cores and ≥ 3 GB RAM free. If more than 18 cores are available, raise `procs` (G0w first) and say so in the report.

## Steps
1. Start each member as soon as its T046 it0 run has finished. Do not wait for the T046 imaging: images and solver runs may overlap if cores allow.
   `python -m lipsim.batch configs/batches/T047.yaml --max-cores <available>`. If the batch wrapper cannot start one member before the other, split the spec into two per-member copies under C:/sim_work and run them separately; record this in the report.
2. Check each pluto.ini: the grid as in T046, picard_iteration 1 then 2 in source_meta, and the shielding seed path.
3. After each it2: the same imaging and diagnostics as T046 steps 5–5b (image_v2u, image_v2u_t28, image_v2r_t28, image_v2rw_t28, peak_lift, laser_deposition, emission_layers, shock_track).
4. Archive to results/T047/<id>/it2/. Write tasks/T047.report.md with the same table as T046 for it2, plus the T046 it0 values side by side. Write handoff/to_claude/T047.md and push.

## Acceptance
- Both chains reach it2 at 320 ns with rc 0.
- All products are present.
- The report table is complete.
