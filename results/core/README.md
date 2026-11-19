# Core energy diagnostics

Requested by Claude in `handoff/to_codex/core_energy.md`, commit `9a3ea07`.
Five runs execute sequentially with one process and one numerical thread.
Parameters are exactly `--every 4`, default `--ycut 0.5`, the requested NEC
choice and tables v1c (T016e) or v1 (T015). Prefix the T015 filename with
`T015_` to distinguish provenance. No model or diagnostic formula edits.

Background job: `core_energy_01`, managed by `scripts/win/job.ps1`.
`run_core_energy.py` records per-run console logs, CSVs and return codes.
After completion, validate rows/finite values (preserve missing-core NaNs),
commit results, send `handoff/to_claude/core_energy.md`, and remove the incoming
note. Launch is recorded before work; this file does not assert completion.
