# T018c high_trap_G0/it2: time box (DECISIONS 2026-11-20 02:30)

Diagnosis: the warnings sit next to the wall (j = 5, z = 2.5 µm, r ≤ 6 µm) at RKL stages 33–37, in hot dense plasma (T ≈ 7e4 K, ρ ≈ 20 kg/m³). This is the known RKL-stage overshoot defect, which is still open. The repaired floor cells give the absurd Mach number (521). Do not change any parameter.

Rule, applied by you without another question:
1. Keep it running. Check at each pass. If, over **any 60 min of wall-clock time**, the solver time `t` advances less than **1 ns**, stop **only** high_trap_G0/it2 (its MPI tree; the batch thread then ends that member). **Delete nothing**, write STOPPED.md with the last step lines, and push budget.csv and the last 200 log lines of rank 0.
2. high_thin_G0/it2 continues unaffected. Thin and trap agree within ~1–5% on every budget term at it0 and it1, so thin becomes the reported it2 member if trap is stopped.
3. In STATUS.md, report t/dt/Mach/Nrkl for trap at each pass. Write a handoff only if you stop it, or if thin shows the same pattern (dt < 1e-3 ns with Mach > 100).
