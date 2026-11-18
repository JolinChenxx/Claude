---
id: T016
title: v1 step 2 — Cu–air interdiffusion (on top of T015 conduction): tables v1b, build v1b, 6-member batch, imaging
owner: codex
status: done
started: 2026-11-17T23:18:42.6934325+08:00
depends: []   # steps 1-2 now; only step 3 (batch start) waits for the T015 solver runs
milestone: D2
est_time: 0.3h operations + about 0.3h tables + about 12h machine time (as T015)
---

## Background
The T015 emission diagnostic (STATE 2026-11-17 21:15) showed that the late in-band signal is 87–99% Cu I lines from nearly pure Cu vapour (Y ≈ 0.9) that stays at about 9–10 kK. The model has no species diffusion. The user approved adding Cu–air binary interdiffusion as v1 step 2. It has no free parameter: hard-sphere Chapman–Enskog D with the registry diameters, plus the interdiffusion enthalpy flux, in registry `diff_cuair_model`.

Implementation (commits 9f2b3dd and this one):
- Two new table fields in opt.bin: `D_cuair` and `dh_dY`.
- The flux is computed in tc_flux.c and applied to the tracer by tc_rhs.c. rkl.c adds TRC to the RKL variable list.
- Diffs and reasons are in `pluto/overrides/`. The build now replaces 7 upstream files.

Cloud smoke test (high fluence, smoke grid, 0–60 ns):
- 0 ConsToPrim warnings.
- Energy residual −0.17%.
- Cu mass in the domain equals the injected mass exactly.
- Mixed-cell Cu fraction 31% → 34%.

## Steps
1. `git pull --rebase origin ws`. The tables can be built now, alongside T015, in a **new** directory:
   `D:\sim_tools\envs\lipsim\python.exe -m lipsim.tables.build --config configs/tables/v0.yaml --out D:\sim_runs\tables\v1b --workers 32`
   Run it in the background through job.ps1. Expect several times the T015 table time, because ∂h/∂Y needs extra equilibrium solves.
   Acceptance: rc=0; opt.bin fields include `D_cuair` and `dh_dY`; shape [81,145,14]; all finite. Copy the manifest to results/T016/.
2. Build into a new directory:
   `python scripts/build_pluto.py --problem pluto/lip --out D:\sim_tools\pluto_lip\build_v1b\mpi --platform windows --mpi --eos pvte --tc --extra-obj lip_tables.o lip_physics.o lip_glue.o --jobs 8`
   Acceptance: rc=0, exactly 7 replaced files (the T015 five plus rkl.c and tc_rhs.c). Copy build_provenance.json to results/T016/.
3. Start the batch **after the T015 solver runs have finished**. T015 imaging may still be running.
   `scripts\win\job.ps1 start -Id T016 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T016.yaml --max-cores 64"`
4. As in T015, image members as they finish:
   `scripts/image_members.py D:\sim_runs\T016 --series low|high --tables D:\sim_runs\tables\v1b --workers 16`
   Push each finished member's budget.csv and ConsToPrim count right away; do not wait for the end. Also run
   `scripts/diag_emission.py <member>\it<n> --tables D:\sim_runs\tables\v1b --times 200 300 500 700 1000 1200 --filter 510 12 --out results/T016/diag/<member>_it<n>.csv`
   for low_thin_G0 and high_thin_G0 (latest finished iteration).
5. When everything is done:
   - Run `collect_v0.py` and `report_v0.py` into results/T016 (`COMPARISON_v1b.md`).
   - Write the report `tasks/T016.report.md` with the same per-run columns as T015, plus the M_Cu check against M_src.
   - Copy to D:\Sync\T016\.
   - Set the status to `review` and write the handoff.

## Not allowed
- Changing members, parameters, the D model or the table grid. No reruns and no image adjustments.
- If a run aborts, record the tail of its log and continue with the other members.
