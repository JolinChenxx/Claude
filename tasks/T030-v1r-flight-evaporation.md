---
id: T030
title: v1r droplets located by flight time and gas-heat-transfer evaporation; 8 members (melt-depth size + 10/30/100 nm scan) × high/low, one Picard iteration seeded from T026; up to 48 cores
owner: codex
status: ready
depends: []
milestone: D2
est_time: 0.5h operations + ~3.5h (high) / ~1.2h (low) machine time per member
---

## Background
User decision 2026-11-25: "先做液滴空间分布的折中，按飞行时间和蒸发时间定". PREREG_v0, "v1r extension declared 2026-11-25 ~09:25".

v1n (T026) evaporates the droplets throughout the Cu core, and v1q (T029) evaporates them at the spatter top. In v1r, each 1-ns launch bin is a parcel of equal droplets of radius a0. The parcel flies at the measured spatter speed and loses mass by the gas heat flux in its grid row. The flux is the free-molecular and continuum series law, energy-limited at T_boil. What the gas cannot evaporate stays airborne.

The reported member uses a0 from the melt depth of the target model (Rayleigh–Plateau). The 10, 30 and 100 nm members are a declared sensitivity scan, not fits. Each member runs one Picard iteration with the T026 shielding of the same fluence as input, so v1r and T026 at that iteration differ only in the droplet rule.

Cloud checks:
- Build OK.
- Tests pass, including tests/test_droplet_v1r.py.
- Smoke runs (smoke grid, 1 core):
  - v1n regression with build_v1r vs build_v1q, v1bpde_low, 0–110 ns: all 175 budget rows are bit-identical. The new column M_drop_airborne is appended.
  - v1r 10 nm, low fluence, 0–180 ns: rc 0. 96 parcels launched 2.098 ng, evaporated 0.720 ng (34%), airborne 1.378 ng. Mass closure is 2e-11, and M_Cu in the gas equals M_src.

## Steps
1. Run `git pull --rebase origin ws`.
   - Build `build_v1r` into `D:\sim_tools\pluto_lip\build_v1r\mpi` with the T029 build command, changing only the output dir. Expect rc 0.
   - Run `python -m pytest -q tests/test_droplet_v1r.py tests/test_breakdown.py tests/test_knight_bp.py`.
2. Prerequisite: `D:/sim_runs/T026/high_thin_G0/it1` and `D:/sim_runs/T026/low_thin_G0/it0` must both contain `surface_transmission.npz` and `source/source_meta.json`. Record their hashes.
3. Launch: `scripts\win\job.ps1 start -Id T030 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T030.yaml --max-cores 48"`.
   - Members start as cores free up. T029 high it2 and T028 high G1 imaging keep their cores; do not stop them.
   - In every pluto.ini check:
     - DROP_A0: high_rp/low_rp ≈ 0.945 × melt_depth_mean_m from that run's source/source_meta.json (record both numbers); scan members exactly 1e-08, 3e-08 or 1e-07.
     - DROP_RL 8000, DROP_VD 344 (high) / 190 (low), DROP_TPL 0, DROP_TB 2835, BACKP 1, VMOM 0.
4. For every finished member, as in T026:
   - residual checkpoints and ConsToPrim counts;
   - the last row of `droplets.csv`, plus the whole file archived;
   - `M_drop_airborne` from budget.csv;
   - image_v2 with band-b frames, then diagnostics and peak_lift.
   - Postprocessing rank: high_rp, then low_rp, then the scan members, ahead of T028 and T023 G1.
5. Paper check (no image measurement): from the main text and SI PDFs, quote verbatim with page and line every statement about the **sizes of the deposited nanoparticles or droplets** (nm or µm values, SEM/TEM statistics), in the style of data/exp/paper/droplets_notes.md. Write it to `data/exp/paper/np_size_notes.md`. If there is no number, say so.
6. Write a handoff after each member's production and after its imaging. Report in `tasks/T030.report.md`.

## Not allowed
- Physics, numerics or case changes.
- Restarting a stopped v1r member from a dbl file. The parcel state is not saved, so rerun from t = 0 and record it.
- Stopping other cards. Deleting data.
