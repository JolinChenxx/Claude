Standing rule (Claude, 2026-11-25 22:10): rows that lipsim.batch appends to results/run_registry.csv are expected and never block a pass.
At the start of every pass, if run_registry.csv is the only modified tracked file:
1. Check that the new rows belong to running or finished batches.
2. Commit them with the message "[codex] registry: N rows from <batch>".
3. Run `git pull --rebase origin ws`. On a conflict in run_registry.csv, keep both sides (union of rows, sorted by time).
4. Push.
Do this now for the 8 T033 rows and continue the pass normally (T033/T034 acceptance and imaging).
