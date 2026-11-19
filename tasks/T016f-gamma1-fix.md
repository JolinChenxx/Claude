---
id: T016f
title: Verify the Gamma1 bound (build v1h) on the stalled it3 checkpoint, then continue high-fluence Picard it3–it4
owner: codex
status: ready
depends: []
milestone: D2
est_time: 0.3h + 20 min check + about 8–12h machine time (16 cores)
---

## Background
T016d established the cause of the stall. dt 4e-14 s, with |v| ≤ 23.5 km/s and PLUTO Mach ≈ 26, means a **diverging sound speed**, not a diverging velocity. In cells above the table T maximum, e(T) is flat, ∂e/∂T → 0, and the table c² blows up.

Build v1h (this commit) clips Γ1 = ρc²/p to [1, 5/3]. This is a thermodynamic bound for ideal-gas mixtures in equilibrium and has no free parameter. Cloud smoke test: **bit-identical** to v1d (the bound is never active in normal cells), 0 ConsToPrim.

## Steps
1. `git pull --rebase origin ws`. Build: `python scripts/build_pluto.py --problem pluto/lip --out D:\sim_tools\pluto_lip\build_v1h\mpi --platform windows --mpi --eos pvte --tc --extra-obj lip_tables.o lip_physics.o lip_glue.o --jobs 8` (rc=0, 7 replaced files).
2. Check: copy the T016d restart inputs (`D:\sim_runs\T016d\high_trap_G0_it3`) to `D:\sim_runs\T016f\check`. Restart with the v1h exe, 8 ranks, `-dec 8 1`, with a **20-minute** limit. Acceptance: within the 20 minutes dt recovers to ≥ 1e-3 ns, and t advances well past 85 ns. Record the step/Nrkl lines and the budget tail in results/T016f/check/.
3. If step 2 passes, start the continuation:
   `scripts\win\job.ps1 start -Id T016e_hc -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T016e_hc.yaml --max-cores 16"`
   It reruns it3 and it4 for both high G0 members, starting from the finished it2 (prev_run). Push budgets and source_meta as each iteration finishes. Image with image_v1 and image_v2, and run diag_emission and core_energy on the latest iteration.
4. If step 2 fails, hand off the evidence and do not start step 3.
Report `tasks/T016f.report.md`, set `review` when everything is done, and write handoffs at the step-2 result and after each finished iteration.
