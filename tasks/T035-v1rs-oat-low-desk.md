---
id: T035
title: (desk node) OAT of v1rs at low fluence, Picard it0, own baseline; 9 runs sequential at 8 ranks; after T032
owner: codex-desk
status: done
depends: [T032]
milestone: D2
est_time: ~1.5–2.5 h per run on this node
---

## Background
PREREG, "T034/T035 v1rs OAT". This runs on the desk node, under the T032 node rules: at most 12 cores and C:\ only.

The node cannot reach the workstation's seeds, so every run is Picard it0. The batch includes its own baseline, low_base, so that compiler and node differences cancel in the comparison.

## Steps
1. After T032 is complete and its smoke cross-check has passed:
   - `git pull`.
   - Build three variant tables, one at a time with `--workers 12`, setting `LIPSIM_OVERRIDES` for that command only, exactly as T033 step 2 but with outputs `C:\sim_work\tables\v1c_sigen_lo`, `C:\sim_work\tables\v1c_sigen_hi` and `C:\sim_work\tables\v1c_gaunt15`.
   - Record the hashes and compare them with the workstation's in tasks/T033.report.md.
2. Run `C:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T035.yaml --max-cores 8` in the background, so that runs go one at a time.
   - Before each run, check that at least 3 GB RAM is free; the batch waits for cores only, so if needed pause between runs and report it.
3. Per run:
   - production checks: rc, the last budget row, M_recondensed, droplets.csv;
   - then `python -m lipsim.image <run_dir> --series low --tables C:\sim_work\tables\v1c --out <run_dir>\image_v2 --workers 4`, then `scripts/peak_lift.py`.
   - Copy into results/T035/<id>/: budget.csv, droplets.csv, image_v2/*.csv, image_summary.json, peak_lift.csv, pluto.ini, overrides.json, source/source_meta.json. Do not copy the dbl files.
4. Push after each run with a handoff `handoff/to_claude/T035-desk-<id>.md`. Write the report in tasks/T035.report.md.

## Not allowed
- More than 12 cores.
- Writing to D:.
- Committing run_registry.csv.
- Value changes outside the batch file.
