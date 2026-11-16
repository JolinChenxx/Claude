---
id: T011
title: v0 quick-baseline production batch (2 fluences × 2 NEC limits × Picard it0–2 on G0, plus high fluence on G1) + imaging
owner: codex
status: done
depends: [T010]
milestone: D0
est_time: 0.5h operations + about 3h machine time (64 cores)
started: 2026-11-16T20:06:14+08:00
---

> Revised by Claude on 2026-11-16 to match the code actually delivered. The batch list was declared in advance in `docs/PREREG_v0.md` and `configs/batches/T011.yaml`. Every member is run and reported: none added, removed or selected. The branch B fallback group is not run, because the self-consistent model already ignites in the cloud smoke run; this is recorded in the PREREG.
> **Fast lane:** this card can only start after T010 is `done`, or once every automatic check of T010 passes (both compare_budget runs exit 0, build OK, tables built). In the latter case Codex may go straight on to this card in the same pass and note that in the report.

## Steps
1. `git pull --rebase origin ws`. Confirm that `D:\sim_runs\tables\v0\` has eos.bin/opt.bin/pop.bin, and that `D:\sim_tools\pluto_lip\build\mpi\pluto.exe` exists and is the T010 build (record its SHA256).
2. Start in the background (all 6 members run concurrently, 64 processes in total; Picard chains run sequentially inside each member):
   `scripts\win\job.ps1 start -Id T011 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T011.yaml --max-cores 64"`
   (set PYTHONPATH to `src`; run_lipsim.ps1 already does this). **End this pass after starting it.** Expected wall-clock time 2–3 h.
3. Next pass: `job.ps1 status -Id T011`. **Do not wait in the foreground** if it is still running. Once everything is finished (or after 4 h, process whatever has finished):
   - Imaging (high fluence; `<m>` ranges over all finished member directories, i.e. every `it*` under `D:\sim_runs\T011\high_*\`):
     `python -m lipsim.image <m> --series high --tables D:\sim_runs\tables\v0 --out <m>\image --workers 16`
   - Low fluence likewise with `--series low`.
   - Then: `python scripts/collect_v0.py D:\sim_runs\T011 --out results/T011` (gathers image_summary.json, budget.csv, metrics_*.csv and run_registry for every member).
4. Write `tasks/T011.report.md`: one row per member/iteration (finished or not, wall-clock time, ConsToPrim count, final budget columns E_gas_abs / E_rad / M_Cu, the P1/P2 values from image_summary.json). Set to `review`, write the handoff, push.

## Acceptance criteria
- All 6 members (14 PLUTO runs including the Picard iterations) are recorded in `results/run_registry.csv`.
- For runs that did not finish or produced numerical warnings: copy the last 50 lines of the log as-is. **Do not retry and do not change parameters.** As long as both high-fluence G0 it0 runs finished, this card may be set to `review`.
- Every finished run has image outputs (frames_*.npz, metrics_*.csv, image_summary.json).

## Not allowed
- Adding or removing batch members, or changing any physical or numerical parameter or the gain/shift/time zero.
- Comparing images with the experiment and drawing conclusions (Claude writes the comparison report).
