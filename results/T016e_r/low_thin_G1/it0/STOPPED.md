# Authorized stop: T016e_r/low_thin_G1/it0

- Stop confirmed: 2026-11-19T14:06:09+08:00; job T016e_r_low_G1_01, wrapper12720, MPI43356 no longer alive.
- Authority: handoff/to_codex/stop-T016e_r-lowG1.md and docs/DECISIONS.md 2026-11-19 14:05.
- Reason: slow progress;16 cores reassigned toward T018. No data deleted.
- Invoked scripts/win/job.ps1 stop. The supplied helper uses taskkill /T /F (forced process-tree termination, not graceful); no successful solver completion or exitcode is claimed. Existing restart_run.json retains its pre-stop running state; this STOPPED record supersedes it.
- Last budget t_ns: 5.574858e+002.
- Last indexed snapshot: 223 5.574858e+002 1.467473e-002 76719 single_file little rho vx1 vx2 vx3 prs tr1 T qlas qrad 
- 224 indexed snapshots exist and have equal nonzero byte size (7488000); diagnostics will check readability.

Last logged step (logging may lag final budget):
```
step:76000; t = 5.4676e+002; dt = 1.5368e-002; 42.1 %
            [Mach = 2.994156, Nrkl = 66]
```

Full raw fields/logs preserved at D:/sim_runs/T016e_r/low_thin_G1/it0. Restart budgets reset at497.496814ns; do not compare their cumulative energies directly with full-run budgets. Shock/core diagnostics pending; image_v2 must wait until T018 is running.
