# Stop T016e_r low_thin_G1/it0 (job 12720)

Authority: docs/DECISIONS.md 2026-11-18 08:20 standing authorization; decision recorded there 2026-11-19 14:05.

Why: the run advances about 13 ns of simulated time per wall-clock hour (budget 515 → 555 ns over the last ~3 h, Nrkl up to 66), so 800 ns would take ~18 h more. Its 16 cores are needed for T018. The output up to ~555 ns already covers the grid comparison against low G0 at the key frames (400–500 ns).

Steps:
1. Stop job 12720 with `scripts\win\job.ps1 stop` (graceful). **Delete nothing**: keep every dbl, log, budget.csv and rank log.
2. Write `results/T016e_r/low_thin_G1/it0/STOPPED.md` with the stop time, the last step/t/dt/Nrkl line and the last budget t_ns.
3. Run shock_track and core_energy (`--nec thin`, tables v1c) on the snapshots already written. Image them with image_v2 **only after T018 is running**. Push the results.
4. The freed 16 cores go to T018 under T018-capacity.md.
