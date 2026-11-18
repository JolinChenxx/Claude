---
id: T016s
title: A/B numerical check of the RKL step-ratio cap (rmax_par 100 vs 1000) from one T016r checkpoint
owner: codex
status: done
started: 2026-11-18T08:06:27.537367+08:00
depends: []
milestone: D2
est_time: 0.2h operations + 40 min machine time (2 × 8 cores)
---

## Background
T016r has slowed down after about 600 ns. dt falls from about 4e-2 to about 2e-3 ns while Nrkl stays at its cap of about 20. PLUTO limits the ratio dt_hyp/dt_par to `rmax_par`, which defaults to 100 because the ini does not set it. Once the parabolic part (conduction plus interdiffusion in rarefied cells) is stiffer than that, PLUTO shrinks the hydro step instead of adding RKL stages.

`rmax_par` is a numerical parameter (grade N), not physics. This card measures whether raising it to 1000 changes the solution. It has to be decided on solution differences and speed, never on agreement with the experiment. The batch runner now accepts `rmax_par` per run (default: unset = 100).

## Steps
1. `git pull --rebase origin ws`. Take the **latest** dbl output of `D:\sim_runs\T016r\low_trap_G0\it0` and copy the run inputs into **two** fresh directories. Copy `lip_tables`, `pluto.ini`, `grid.out`, `restart.out`, `dbl.out` and that `data.<n>.dbl`:
   - `D:\sim_runs\T016s\A`: unchanged.
   - `D:\sim_runs\T016s\B`: identical, except one line `rmax_par 1000` added to the `[Time]` block of pluto.ini.
   Do not touch the running T016r directory.
2. Restart both with `D:\sim_tools\pluto_lip\build_v1d\mpi\pluto.exe -restart <n> -dec 8 1`, using 8 ranks each, in the background. Give each a wall-clock limit of **40 min**. The machine is temporarily oversubscribed (64 + 16 ranks on 64 cores); that is accepted for this short test.
3. Collect into results/T016s/:
   - for A and B: the budget.csv rows after the restart time; t reached; the last 30 "step:" lines with their Nrkl lines; the ConsToPrim, E<0 and other "!" counts summed over all rank logs;
   - for the latest common output index of A and B, the data.<m>.dbl files. Copy them only if each is under 20 MB; otherwise give their path.
   Report `tasks/T016s.report.md`, set `review`, hand off and push.

## Not allowed
- Any other change to the ini, tables or exe.
- Touching T016 or T016r.
