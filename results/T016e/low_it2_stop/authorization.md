# Stop T016e low_thin_G0/it2 and low_trap_G0/it2 (standing authorization, DECISIONS 2026-11-19 18:05)

Why: it2 only confirms the convergence of it1. The Picard criterion is the target absorption, which is final once the pulse is over: E_targ_abs is constant from ~300 ns on in it1. The runs are at ~860–880 ns at ~40 ns/h (Nrkl ≈ 60, on the older build with the explicit wall limit), so they would need ~11 h more for data that adds nothing to the criterion.

1. Stop the T016e batch members low_thin_G0/it2 and low_trap_G0/it2 the usual way. **Delete nothing.** Write STOPPED.md with the last step line.
2. Push `results/T016e/low_{thin,trap}_G0/it2/budget.csv` and `source_meta.json`, plus `shock_track` on the snapshots already written.
3. Give the freed 16 cores to the queued T018b/T018c imaging and diagnostics first, then to T016i and T017a.
