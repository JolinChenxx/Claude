---
id: T034
title: OAT of the reported model v1rs at high fluence; 8 members seeded from T026 it1; start when T033 frees ≥ 16 cores
owner: codex
status: review
started: 2026-11-25T21:06:45+08:00
depends: []
milestone: D2
est_time: ~3.5 h per member
---

## Background
Decision of 2026-11-25 21:10 (DECISIONS): v1rs is the reported model. PREREG, "T034/T035 v1rs OAT". This is sensitivity only, with the same mechanism as T033 (`overrides:`). The variant tables already exist from T033 (v1c_sigen_lo, v1c_sigen_hi, v1c_gaunt15). The reference is T031 high_v1rs/it2, which has the same seed.

## Steps
1. Pull. Launch `scripts\win\job.ps1 start -Id T034 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T034.yaml --max-cores 16"` as soon as T033 leaves at least 16 cores idle. Raise --max-cores as T033 finishes; T033 + T034 together stay ≤ 60 cores.
2. Check every pluto.ini and overrides.json: COND_SIG 1 (0.3 for cond_lo), DROP_A0 ≈ 0.945 × melt depth, SIG_EN for the sig members.
3. Per member: production checks, image_v2 band b, peak_lift, diagnostics. Postprocessing rank: after T033 members of the same wave.
4. Handoffs as usual. Report in tasks/T034.report.md.

## Not allowed
Value changes outside the batch file. Stopping T033. Deleting data.
