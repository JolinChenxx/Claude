---
id: T013a
title: High-fluence G2 grid-convergence members (uses about 32 cores left idle by T011)
owner: codex
status: review
depends: [T010]
milestone: D0
est_time: 0.2h operations + an estimated 4–8h machine time (32 cores)
started: 2026-11-16T22:34:31+08:00
---

## Background
T011's runner starts at most 4 members at a time (a Claude code bug, fixed in this commit, takes effect from the next batch), so about 32 cores are idle. **Do not interrupt T011.** Use the idle cores for the G2 grid-convergence members already planned in PLAN v3 (numerical verification, grade N).

## Steps
1. `git pull --rebase origin ws`. Note the current CPU load. If T011 uses more than 40 cores (e.g. its G1 members have already started), wait until enough cores are free before starting (stay in the background, do not wait in the foreground; check again on the next pass).
2. Background: `scripts\win\job.ps1 start -Id T013a -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T013a_G2.yaml --max-cores 32"` (PYTHONPATH=src). End this pass.
3. Later passes: once `job.ps1 status -Id T013a` shows it finished, run imaging for both members (`python -m lipsim.image <member>\it0 --series high --tables D:\sim_runs\tables\v0 --out <member>\it0\image --workers 16`), then `python scripts/collect_v0.py D:\sim_runs\T013a --out results/T013a`.
4. Report `tasks/T013a.report.md` (wall-clock time, ConsToPrim count, final budget, image_summary per member), set to `review`, write the handoff, push.

## Not allowed
- Interrupting or affecting T011; changing any physical or numerical parameter.
