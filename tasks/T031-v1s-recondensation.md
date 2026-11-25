---
id: T031
title: v1s recondensation of Cu vapour on the target; v1s (on v1n) and v1rs (on v1r melt-depth) × high/low, one Picard iteration seeded from T026; 32 cores
owner: codex
status: ready
depends: []
milestone: D2
est_time: 0.3h operations + ~3.5h (high) / ~2–4h (low) machine time per member
---

## Background
PREREG_v0, "v1s extension declared 2026-11-25 ~14:40".

In every model the emitting core sits on the face as a half-dome, with σz/σx ≈ 0.6 in all 18 imaging scenarios. The experiment's core is detached, with aspect 1.07.

The models evaporate Cu but have no return flux. v1s adds the net Hertz–Knudsen recondensation of Cu vapour onto the cooling target, with σ = 1 from the measured liquid-metal value, and no free parameter. The budget gets a new column, M_recondensed.

Cloud smoke runs (smoke grid 300×400 µm, 1 core):
- v1n regression with build_v1s (COND_SIG 0), v1bpde_low, 0–110 ns: all 175 budget rows are bit-identical to build_v1q.
- v1s_low, 0–320 ns: rc 0. Recondensed Cu: 0.25 / 0.60 / 1.00 / 1.60 ng at 100 / 150 / 200 / 300 ns. M_Cu = M_src (net) to all digits.
- Energy closure is within 0.001% up to 230 ns. After 245 ns the shock leaves the small smoke domain, M_total falls, and the residual grows. That is outflow, not a v1s term; the production grid is larger.

## Steps
1. Run `git pull --rebase origin ws`.
   - Build `build_v1s` into `D:\sim_tools\pluto_lip\build_v1s\mpi` with the T030 build command, changing only the output dir.
   - Run the T030 test set. Expect rc 0.
2. Launch when 32 physical cores are free; do not stop T030:
   `scripts\win\job.ps1 start -Id T031 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T031.yaml --max-cores 32"`.
   - Check every pluto.ini: COND_SIG 1, COND_TB 2835, BACKP 1, VMOM 0.
   - v1s members: DROP_A0 0.
   - v1rs members: DROP_A0 ≈ 0.945 × melt_depth_mean_m, as in T030.
3. For every member, as in T030:
   - residual checkpoints, ConsToPrim counts;
   - the last budget row, including M_recondensed;
   - droplets.csv for v1rs;
   - image_v2 with band-b frames, then diagnostics and peak_lift.
   - Postprocessing rank: high_v1s, high_v1rs, low_v1s, low_v1rs. They come after T030 high_rp imaging and before the T030 scan members.
4. Write a handoff after each production and after each imaging. Report in `tasks/T031.report.md`.

## Not allowed
- Physics, numerics or case changes.
- Stopping T030.
- Restarting v1rs members from dbl files. Rerun them from t = 0.
- Deleting data.
