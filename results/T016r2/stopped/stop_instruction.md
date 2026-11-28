---
id: T016e
title: v1e production — tables v1c (Coulomb/polarisation Cu–air diffusion) with build v1d, all 6 members, 64 cores
owner: codex
status: running
started: 2026-11-18T16:50:33.297592+08:00
depends: []
milestone: D2
est_time: 0.3h operations + about 15 min tables + 12–24h machine time
---

## Background
The hard-sphere Cu–air D (tables v1b) treated ions as hard spheres. In the hot, ionised plasma (≥ 15–20 kK) this overestimates D by 50–250×, because ion–ion collisions are Coulomb collisions. That overestimate is what made the parabolic part stiff and the runs slow.

The model v2 (registry `diff_cuair_model_v2`, `polarizability`) has no free parameter:
- neutral–neutral pairs: hard spheres;
- ion–neutral pairs: max(hard sphere, Langevin polarisation);
- ion–ion pairs: first-order Chapman–Enskog Coulomb collisions;
- mixture rule: Blanc-type.

New/old D: 1.00 in neutral air; 0.97 in the late Cu core (9.4 kK); 0.76 at 10 kK / 0.01 kg/m³; 0.02 at 20 kK; 0.004 at 40 kK.

Only the table field `D_cuair` changes, so the v1d executable is reused. Cloud smoke test (low-res v1c, high fluence, 0–60 ns): 0 ConsToPrim, residual −0.22%, M_Cu = M_src, runtime −26% versus v1b tables.

## Steps
1. `git pull --rebase origin ws`. Build the tables now, in the background, into a **new** directory:
   `D:\sim_tools\envs\lipsim\python.exe -m lipsim.tables.build --config configs/tables/v0.yaml --out D:\sim_runs\tables\v1c --workers 32`
   Acceptance: rc=0, `manifest.json` has `"diff_model": "v2"`, opt.bin has `D_cuair` and `dh_dY`, shape [81,145,14], all values finite. Copy the manifest to results/T016e/.
2. Stop the current batches, under the standing authorization (docs/DECISIONS.md 2026-11-18 08:20). They are superseded by v1e; keep their data as the hard-sphere-D sensitivity.
   - Wait until **T016r2 low_trap_G0 it0** has finished, or until 18:30 UTC+8, whichever comes first.
   - Then stop the **T016h** and **T016r2** jobs and their mpiexec/pluto.exe children.
   - Do not delete anything. Record the last t_ns of each member under `results/T016h/stopped/` and `results/T016r2/stopped/`.
   - Set both cards to `done` with the note "superseded by T016e (v1e); finished iterations kept as hard-sphere-D sensitivity".
3. Once the tables are ready and both batches are stopped, start:
   `scripts\win\job.ps1 start -Id T016e -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T016e.yaml --max-cores 64"`
   Check that each pluto.ini has `rmax_par 1000` and that lip_tables is the v1c set (hash).
4. As iterations finish:
   - push budget.csv, the all-rank ConsToPrim count and source_meta.json;
   - image them (`--tables D:\sim_runs\tables\v1c`);
   - run `diag_emission.py` on the latest finished iteration of high_thin_G0 and low_thin_G0;
   - put dt and Nrkl in STATUS.md;
   - write a short interim handoff after each **first finished it0 per fluence**.
5. When everything is done:
   - run `collect_v0.py` and `report_v0.py` into results/T016e (`COMPARISON_v1e.md`);
   - write `tasks/T016e.report.md` with the T016 columns, M_Cu vs M_src, and the target-model vs PLUTO absorption per iteration;
   - copy everything to D:\Sync\T016e\;
   - set the status to `review` and write the handoff.

## Not allowed
- Parameter changes. No reruns of members that succeed.
- If a member aborts, record the evidence and continue with the others.
