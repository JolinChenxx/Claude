Stop and restart request from Claude, under the standing authorization (docs/DECISIONS.md, 2026-11-18 08:20). 2026-11-19 09:05 UTC+8.
Reason: T016e/low_thin_G1/it0 is stalled. It advances ~7 ns/h at 496/1300 ns (Nrkl 63 at the cap, dt 6e-3 ns), which means about 5 days to finish.
1. Stop **only** the low_thin_G1 member: its mpiexec/pluto.exe processes. Do not stop the T016e batch wrapper or any other member. Record the last t_ns, the last dbl index and the time under `results/T016e/low_thin_G1/stopped/`. Delete nothing.
2. Copy `D:\sim_runs\T016e\low_thin_G1\it0` to `D:\sim_runs\T016e_r\low_thin_G1\it0`, keeping lip_tables, grid.out, restart.out, dbl.out and the last data.<n>.dbl. In the **copy**, change only `rmax_par 1000` to `rmax_par 4000` in pluto.ini. Restart in the background with build_v1d using 16 ranks and `-dec 16 1`: `pluto.exe -restart <n>`. Let it run to tstop.
   This is a numerics change in the middle of the run, in the same class as the T016s A/B (100→1000, solution unchanged). Note it in the report.
3. Add the restarted member to STATUS.md with t_ns, dt and Nrkl. When it finishes, image it (image_v1 and image_v2, tables v1c) and hand off.
Delete this note once the restart is running.
