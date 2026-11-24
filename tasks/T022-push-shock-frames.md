---
id: T022
title: Push high-fluence shock track and image_v2 frames of the reported members (light, for the comparison figure)
owner: codex
status: done
started: 2026-11-24T08:54:15+08:00
depends: []
milestone: D2
est_time: 0.3h
---

## Background
The user asked for the high-fluence shock trajectory and frame-by-frame images, for the comparison against experiment (Fig. S4, main-text Fig. 3d). The cloud figure is `scripts/plot_current_vs_exp.py`. Reported members: **T016e high_thin_G0/it2** (high) and **T016e low_thin_G0/it1** (low). This is a light job: run it next to T021 and do not delay T021.

## Steps
1. Shock: if `D:\sim_runs\T016e\high_thin_G0\it2` has no shock CSV, run
   `python scripts/shock_track.py D:\sim_runs\T016e\high_thin_G0\it2 --out results/T016e/high_thin_G0/it2/shock_track.csv`
   Do the same for `high_trap_G0/it2`. If the earlier high shock comparison (the "3–4% at 500–700 ns" figure) came from another file, push that file too and name its source in the report.
2. Frames: copy the local image_v2 frame archives of both reported members into the repo:
   `results/T016e/high_thin_G0/it2/image_v2/frames_b_thin.npz`, `frames_b_sa.npz`
   `results/T016e/low_thin_G0/it1/image_v2/frames_b_thin.npz`, `frames_b_sa.npz`
   These are filter b, the experiment's band. Use the files exactly as image_v2 wrote them. If the four together exceed 20 MB, push the `thin` pair first, report the sizes, and ask before pushing `sa`.
3. Report SHA256 and size of each file in `tasks/T022.report.md`, then write one handoff.
