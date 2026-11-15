(User approval 2026-11-15: these were set up by an earlier Codex session, not by the user; the user agrees to disable them.)
At the start of the next pass, before continuing any card:
1. Run `Get-ScheduledTask -TaskName S4Plume_* | Select TaskName,State,Actions,Triggers` and record the output (including what each task runs) in results/T004/legacy_tasks_disable.txt.
2. Run `Disable-ScheduledTask -TaskName <name>` on S4Plume_Daily0700, S4Plume_Hourly and S4Plume_Watchdog. **Only disable them. Do not delete or unregister them, and do not touch any data.** This is reversible (Enable-ScheduledTask).
3. Run Get-ScheduledTask again to confirm State=Disabled and append that to the same file. If the current user lacks permission to disable them, record the error and report back; do not escalate privileges.
4. Also note whether mpiexec.exe PID 35756 (parent 31644) in the T004 check output is still there, and what its parent process is (record only, do not stop it).
5. Commit, delete this file, push.
