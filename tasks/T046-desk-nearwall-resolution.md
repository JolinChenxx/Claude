---
id: T046
title: (desk node) near-wall resolution test of v1rs+v1u, high fluence — G0 vs G0w (0.25 µm in first 20 µm), Picard it0, to 320 ns; build tables v1d on the desk first
owner: codex-desk
status: ready
depends: []
milestone: D2
est_time: table build ~40 min (16 workers); G0 ~1.5 h on 6 ranks; G0w ~5–7 h on 12 ranks; imaging ~30 min
---

## Background
PREREG_v0, "Supervisor feedback 2026-11-27 and T046". The workstation is off from Friday night to Sunday night, so this runs on the desk node. Rules as in T032/T035/T038: at most 18 cores, ≥ 3 GB free RAM before launch, C: only. Full authority: no need to ask the user.

## Steps
1. `git pull` on ws.
2. Build the tables:
   `C:\sim_tools\envs\lipsim\python.exe -m lipsim.tables.build --config configs/tables/v1d.yaml --out C:/sim_work/tables/v1d --workers 16`
   The NIST Cu VI–IX levels are already in the repo. Check the manifest: eos_stages {Cu: 9}, and pop.bin has nion_z1..9.
3. In the background: `C:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T046.yaml --max-cores 18`.
4. Check the pluto.ini:
   - high_u_G0w: X2-grid `3 0.0 80 u 20.0 80 u 100.0 110 s 1500.0`.
   - Both runs: PULSE_TAB 0, BD_ON 0, COND_SIG 1.
5. For each run:
   - Imaging twice with `--tables C:/sim_work/tables/v1d`: `--out image_v2u` and `--t-offset-ns 28 --out image_v2u_t28`, both `--workers 4`.
   - peak_lift.
   - `scripts/laser_deposition.py <run> --out results/T046/<id>_deposition.csv --tmax 200`.
   - `scripts/emission_layers.py <run> --tables C:/sim_work/tables/v1d --out results/T046/<id>_emission.csv --tmax 200`.
   - `scripts/shock_track.py`.
6. Archive the small files to results/T046/<id>/: budget, pluto.ini, the metrics CSVs and the frames npz of both image folders, and diagnostics.
7. Write tasks/T046.report.md with a G0 vs G0w table:
   - at 30, 50, 80, 110 and 150 ns (model time): z50, z90, f_lt10, T_5, Tmax_axis, z90_abs;
   - E_gas_abs(150 ns);
   - for image_v2u_t28, defocus high, band b sa, at 0/20/30/50/80/150/300 ns: L(t)/L(300), σx, σz, zc.
8. Write handoff/to_claude/T046.md and push (commit the registry rows first).

## Acceptance
- Both runs reach 320 ns with rc 0.
- The pluto.ini checks pass.
- All CSVs and images are present.
- The report table is complete.
