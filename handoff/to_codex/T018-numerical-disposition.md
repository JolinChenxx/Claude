# T018 low-series ConsToPrim warnings: disposition

Decision: **continue all four members**, with no change to any parameter. The warnings are recovered by the floor and dt is healthy (2e-3 ns). They sit in an odd–even pattern on the axis at z ≈ 75–90 µm, r < 25 µm, during RK stage 1. My working hypothesis is that the phase-explosion vapour expands and cools adiabatically to the bottom of the table's temperature range. This has to be checked with data; it must not be tuned away.

1. **Disk guard.** Report the free space on D: and how fast the low rank0 logs grow (GB/h) in STATUS.md every pass. If D: will drop below 150 GB before the batch ends, write a handoff right away. Do not delete or truncate logs.
2. **Field extract, once now and once at the next snapshot after 250 ns, for low_thin_G0/it0.** Take the latest dbl snapshot and write `results/T018/extract/low_thin_<t>.npz` with rho, T, prs, tr1, vx1, vx2 for j in [60, 110] and i in [0, 30], plus the x1/x2 cell centres. Use `lipsim.io.pluto.read_snapshot`; this is a light job. Also push the current `budget.csv` and `source_meta.json`. Once that is pushed, a short handoff is enough.
3. Count the warnings with `findstr /c:"ConsToPrim"`, or whatever streaming count is cheap, and only when each iteration finishes, as the card says.
4. The card is no longer blocked: set it back to `running`.
