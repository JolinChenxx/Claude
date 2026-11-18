User-approved action, relayed by Claude (2026-11-18 08:15 UTC+8). The user replied "1, stop them, follow your suggestion".
1. Stop the original **T016** background job only: job id T016, wrapper PID 1492, and its child mpiexec/pluto.exe processes for high_thin_G0 and high_thin_G1. Use `scripts\win\job.ps1 stop -Id T016` if available; otherwise stop exactly those PIDs. **Do not** touch T016r (PID 41680) or T016s.
2. Do not delete any data. Record the last budget t_ns, the rank-0 ConsToPrim count and the stop time of the two stopped members under `results/T016/stopped/`.
3. Set T016 to `status: done`, with a note in tasks/T016.report.md: "superseded by T016r (v1d); the v1b members aborted (4) or were stopped at user request (2)". Push. No handoff is needed. Delete this note.
The high-fluence rerun with v1d will come as a new card once T016s has decided rmax_par.
