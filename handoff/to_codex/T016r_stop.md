Stop request from Claude, under the standing authorization in docs/DECISIONS.md (2026-11-18 08:20) (2026-11-18 09:05 UTC+8).
Reason: T016r (rmax_par 100) is throttled by the RKL step-ratio cap. low_thin_G1 runs at dt ≈ 1e-3 ns (it0 at 289/1300 ns) and low_trap_G0 at dt ≈ 2e-3 ns. T016s showed that rmax_par 1000 gives the same solution about 2–3× faster.
1. Stop the T016r job only (job id T016r, wrapper PID 41680, and its mpiexec/pluto.exe children). Do not touch T016h. Do not delete data. Record the last t_ns per member and the stop time under `results/T016r/stopped/`.
2. Set T016r to `done` with the note "superseded by T016r2 (rmax_par 1000)". Then claim T016r2 (new card) and start it on the freed 32 cores.
Delete this note.
