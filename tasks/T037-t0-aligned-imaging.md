---
id: T037
title: Re-image the reported members with the declared t0 alignment (Δ = 28 ns from the high-fluence shock); imaging only
owner: codex
status: running
started: 2026-11-26T00:00:44+08:00
depends: []
milestone: D2
est_time: ~0.5–1 h per member (imaging); 4 members
---

## Background
PREREG, "t0 alignment declared 2026-11-26 ~00:15". `lipsim.image` gains `--t-offset-ns`; image_summary.json records it.

## Steps
1. Pull. For each of these members, run image_v2 with `--t-offset-ns 28 --out <run_dir>/image_v2_t28`, using the same scenarios, tables and workers as in T031/T026 imaging:
   - T031 high_v1rs/it2
   - T031 low_v1rs/it1
   - T026 high_thin_G0/it2
   - T026 low_thin_G0/it1

   Then run peak_lift on each new folder.
2. Archive into results/<card>/<member>/image_v2_t28/: the metrics CSVs, image_summary.json and peak_lift, plus frames_b_sa.npz, because the comparison figures read it.
3. Priority: above T033/T034 postprocessing; this is imaging only. Do not stop production.
4. Write one handoff when all four are done.
