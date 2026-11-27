---
id: T045
title: v1u — fetch NIST Cu VI–IX levels, build tables v1d (Cu to IX, T to 3e5 K), run v1rs and waveform high (seeded) and image with v1d
owner: codex
status: done
started: 2026-11-27T02:30:47+08:00
depends: []
milestone: D2
est_time: NIST fetch ~5 min; table build ~15–20 min (32 workers); 2 runs × 8 procs ~2 h; imaging ~30 min
---

## Background
PREREG_v0, "Continuum charge states…" companion defect v1u, and its implementation note. In the hot, low-density plume the Cu charge ladder currently stops at a bare Cu⁵⁺. This is a completeness fix, not a fit. Start now, in parallel with the remaining T044 imaging (16 cores needed; Claude 2026-11-27 02:05).

## Steps
1. `git pull` on ws.
2. `python scripts/fetch_nist.py --out data/atomic/nist`. Existing files are skipped, and this adds Cu_VI..Cu_IX_levels.csv where NIST has them. Report which files arrived and their level counts. If a stage has no levels on NIST, the code falls back to the ground level; report which stages fell back. Commit the new files together with manifest.json.
3. `python -m lipsim.tables.build --config configs/tables/v1d.yaml --out D:/sim_runs/tables/v1d --workers 32`.
   - Check the manifest config: eos_stages {Cu: 9}.
   - Check that pop.bin has nion_z1..9 and the T axis ends at about 2.99e5 K.
4. Check, with a short run or only the pluto.ini, that the solver loads v1d. Then run `python -m lipsim.batch configs/batches/T045.yaml` in the background.
5. For each run, image as T044 but with `--tables D:/sim_runs/tables/v1d`, into image_v2u and image_v2u_t28. Then run peak_lift, shock_track, and:
   - `scripts/laser_deposition.py --tmax 200`;
   - `scripts/emission_layers.py --tables D:/sim_runs/tables/v1d --tmax 200`.
6. Write tasks/T045.report.md with, for each run, against T031 (high_v1rs_u) or T039 (high_wf_u) re-imaged with v1c2 (T044):
   - E_gas_abs at 150 ns;
   - Tmax on the axis at 50/80/110 ns;
   - σx / σz at 150/300/500 ns (band b, sa, defocus high);
   - zc;
   - L500/L300 and L730/L300;
   - the shock log-RMSE at Δ = 28 ns and at the best Δ.
7. Write handoff/to_claude/T045.md and push.

## Acceptance
- The NIST files are committed.
- The v1d table checks pass.
- Two runs reach 800 ns with rc 0.
- All images and diagnostics are present.
- The report table is complete.
