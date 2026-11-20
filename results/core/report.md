# Core energy diagnostic report

- Executor: codex; completed 2026-11-19 08:05:30 UTC+8, validated 2026-11-19T08:20:23.568688+08:00.
- Execution commit9159021; job core_energy_01 PID44992; all five return codes0. Actual sequential elapsed time about24 seconds (the initial estimate was conservative).
- Requested five CSVs and console logs archived; paths, commands, timestamps and SHA256 in batch_summary.json and validation.json. T015 filename has a batch prefix to avoid ambiguity.

## Validation
- High cases:81 rows each,0-800ns; low cases:131 rows each,0-1300ns. Times exactly match every fourth source snapshot. All nonempty-core physical fields finite and nonnegative.
- High cases have3 empty-core rows and4 derivative-missing rows; low cases4 and5 respectively. Empty-core fields and the first derivative beside missing energy are NaN by the supplied script; preserved unchanged. dEdt and P_other checked against the prescribed gradient and sum.
- All logs lack tracebacks; job exit0. No diagnostic formula or physics edits. Engineering wrapper only: sequential commands and provenance/log capture,one process/thread.
- These are diagnostics of existing runs with documented solver warnings. P_other includes all nonradiative terms and changing-core-boundary transport, not an isolated conduction measurement.
