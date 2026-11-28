---
id: T016c
title: Check the kinetic flux limiter (build v1d) past the T016 abort point from the same restarts
owner: codex
status: done
started: 2026-11-18T03:03:56.934543+08:00
depends: []
milestone: D2
est_time: 0.2h operations + about 1h machine time (16 cores)
---

## Background
T016b located the abort. In the first gas row next to the target (j=4, r ≈ 56–61 µm), the density had dropped to about 7e-4 kg/m³ (mean free path about 0.4 mm, far larger than the cells). There the continuum Fick flux exceeded the free-streaming limit by about 10³. The interdiffusion enthalpy flux that goes with it drove E < 0 inside the RKL stages, and the energy then became NaN.

Fix in build v1d (this commit, registry `diff_cuair_limiter`):
- The Cu flux is limited by the one-way Maxwellian flux on the upwind side, ρY·v_th/4, using J = J_F/(1+|J_F|/J_max). There is no free coefficient.
- Cloud smoke test (dense early phase): 0 ConsToPrim, residual −0.18%, M_Cu = M_src. Energies are within 6e-4 of v1b, so the limiter barely acts while the gas is dense.

## Steps
1. `git pull --rebase origin ws`. Build:
   `python scripts/build_pluto.py --problem pluto/lip --out D:\sim_tools\pluto_lip\build_v1d\mpi --platform windows --mpi --eos pvte --tc --extra-obj lip_tables.o lip_physics.o lip_glue.o --jobs 8`
   Acceptance: rc=0, 7 replaced files.
2. Make fresh copies of the T016 restart inputs, as in T016b, into `D:\sim_runs\T016c\<member>` for low_thin_G0 (file 141) and low_trap_G0 (file 142). Restart each with the v1d exe, using 8 ranks and `-dec 8 1`, in the background. Use a wall-clock limit of **40 minutes** each.
3. Collect the same items as T016b into results/T016c/:
   - rc and the last t reached;
   - the budget tail;
   - per rank: the "!" lines other than "ConsToPrim: rhoe < 0", the count of "E < 0" lines, and the ConsToPrim count.
   Report `tasks/T016c.report.md`, set `review`, write the handoff and push.

## Not allowed
- Stopping or restarting the 4 running T016 members.
- Changing parameters.
