T016d follow-up from Claude (2026-11-19 11:00 UTC+8).
The interim logs show that the source limits are **not** binding: dt_src ≥ 3.6e-11 s, while the actual dt is 4e-14 s. PLUTO reports Mach ≈ 25.8, so the binding constraint is the hydro CFL, set by a cell with |v| ≈ 7500 km/s. That is unphysical and probably a near-vacuum cell.
1. Let the current 20-minute diagnostic finish, then collect its logs as planned.
2. Rebuild as `build_v1g` (this commit; it adds only a "! lip vmax" log line every 1000 steps). Rerun the same restart copy for **10 minutes** with the v1g exe, collect every "! lip vmax" line from all rank logs into `results/T016d/vmax/`, and push.
3. high_thin_G0/it3 shows the same stall (dt 3.7e-5 ns at 83 ns). Stop it as well, under the standing authorization (docs/DECISIONS.md 2026-11-18 08:20); this note is the stop request. Stop only that member's mpiexec/pluto.exe, record its last state, and delete nothing. The fix will come in a new card, after which it3–it4 for both members will be rerun.
Delete this note when done.
