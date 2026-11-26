---
id: T040
title: Waveform scenario with its own Picard chain (high it0–it2, low it0–it1) + G1 high it0; 32 cores; production now (cores idle)
owner: codex
status: running
started: 2026-11-26T10:37:17+08:00
depends: []
milestone: D2
est_time: high chain ~3 × 3.5 h, low ~2 × 2 h, G1 ~6 h
---

## Background
T039 ran the comparable-device waveform with the shielding of the Gaussian T026 run as seed. That is inconsistent once the pulse shape changes. T039 gave the largest morphology gain so far: σz/σx 0.825 against 0.667, and the refitted shock offset fell from 28 ns to 7 ns. T040 recomputes the scenario with a Picard chain from the same pulse, so the result does not depend on the seed. G1 checks grid convergence at high fluence.

## Steps
1. Pull and launch now with `scripts\win\job.ps1 start -Id T040 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T040.yaml --max-cores 32"`. Keep the T033/T034 imaging running.
2. Check every pluto.ini: PULSE_TAB 1 and COND_SIG 1. pulse.txt follows the T039 criterion: the integral equals E_pulse minus the pre-t0 tail of 0.318%.
3. For every finished iteration, run image_v2 (nominal) and image_v2_t28, peak_lift, and the diagnostics including the shock. Write a handoff for each.
