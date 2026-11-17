---
id: T015
title: v1 step 1 — heat conduction (PLUTO TC + wall exchange): full tables with kappa, v1 build, 6-member batch, imaging
owner: codex
status: ready
depends: []
milestone: D2
est_time: 0.3h operations + about 0.5h tables + about 3h machine time
---

## Background
v0/v0.1 has no heat conduction, and the late frames are too bright and last too long. PREREG_v0 lists conduction as the first pre-declared v1 upgrade. It adds physics that was missing; it is not a tuned parameter.

Implementation (this commit):
- PLUTO's own Thermal_Conduction module with RKL super-time-stepping.
- κ(ρ,T,Y) from a new table field `kappa`: an electron Spitzer-type term with e–i and e–n collisions, plus a heavy-particle hard-sphere term (registry `kappa_e_model`/`kappa_h_model`).
- Conductive exchange with the target surface at T_s(t,r), recorded in a new `E_wall` column in budget.csv.
- Two more upstream files are replaced: parabolic_update.c and tc_flux.c. The only changes are that T comes from the table instead of p/ρ, and the diffusion coefficient becomes κ/(ρc_v). Diffs are in `pluto/overrides/`.
- If the tables lack `kappa` or the source lacks `Ts`, a conduction build stops with an error.

Cloud smoke test (high fluence, smoke grid, 0–60 ns): 0 ConsToPrim warnings, budget residual −0.20%, E_wall 12.8 µJ.

T014 and T013a may still be running. Do **not** stop them; they keep their own build and tables. This card runs alongside them (see the core counts below).

## Steps
1. `git pull --rebase origin ws`. Build the full tables with kappa into a **new** directory; do not touch tables/v0:
   `D:\sim_tools\envs\lipsim\python.exe -m lipsim.tables.build --config configs/tables/v0.yaml --out D:\sim_runs\tables\v1 --workers 40`
   Run it in the background through `scripts\win\job.ps1 start -Id T015tab ...`.
   Acceptance: rc=0, opt.bin fields include `kappa`, shape [81,145,14], all values finite (same check as T010). Copy manifest.json to results/T015/tables_manifest.json.
2. Make a new build. Note the `--tc` flag, which the current definitions.h requires:
   `python scripts/build_pluto.py --problem pluto/lip --out D:\sim_tools\pluto_lip\build_v1\mpi --platform windows --mpi --eos pvte --tc --extra-obj lip_tables.o lip_physics.o lip_glue.o --jobs 8`
   Acceptance: rc=0, exactly 5 replaced files (internal_energy.c, parabolic_update.c, split_source.c, tc_flux.c, thermal_eos.c). Copy build_provenance.json to results/T015/.
3. When the tables are done, start the batch:
   `scripts\win\job.ps1 start -Id T015 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T015.yaml --max-cores N"`
   Set N to 64 minus the cores T014/T013a are still using, with a minimum of 32. Record N. End the pass.
4. In later passes, image the members that have finished, as in T014:
   `scripts/image_members.py D:\sim_runs\T015 --series low|high --tables D:\sim_runs\tables\v1 --workers 16`
5. When everything is done:
   - Run `python scripts/collect_v0.py D:\sim_runs\T015 --out results/T015`, then `python scripts/report_v0.py results/T015 --out results/T015/COMPARISON_v1.md`.
   - Write the report `tasks/T015.report.md`. For each run give: rc, wall-clock time, ConsToPrim count, final E_gas_abs, E_rad, E_wall, M_Cu, and the budget residual.
   - Copy the report and figures to D:\Sync\T015\.
   - Set the status to `review`, write the handoff, and push.

## Not allowed
- Do not change members, parameters, the κ model or the table grid. No reruns and no image adjustments.
- If a run aborts (for example with "! lip: conduction build needs ..." or an RKL failure), record the tail of its log and continue with the other members.
