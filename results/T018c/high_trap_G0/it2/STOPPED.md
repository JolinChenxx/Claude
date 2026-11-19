# high_trap_G0/it2 stopped

PE results withdrawn: STATE 2026-11-20 03:00

- Authority: handoff/to_codex/T018c-stop-it2.md, commit 23929ba; DECISIONS 2026-11-20 03:00.
- Reason supplied by Claude: the declared PE source exceeds the choked evaporation flux by 21 times; both it2 members are withdrawn. This explicit instruction supersedes the conditional time-box notes.
- Stop timestamp: 2026-11-20T03:08:27+08:00; verified 2026-11-20T03:10:57+08:00.
- Identity checked by creation time, working directory and ancestry: MPI root 50372, PLUTO ranks [12528, 43844, 26224, 16112, 52340, 39816, 19396, 12504]; descendants are in stop_state.json. taskkill /T /F returned 0. All original tree processes are absent.
- Last emitted timestep: `step:16000; t = 7.6311e+001; dt = 3.6459e-003; 9.5 % [Mach = 16.552672, Nrkl = 4]`
- Last budget t_ns: 76.99786; 151 finite budget records. The timestep line and budget are sampled independently, not an exact stop-time reconstruction.
- Both member return codes are 1 after the authorized termination. The batch wrapper exited 0 because its bookkeeping completed; this is not solver success. Generated registry rows are preserved unchanged.
- budget.csv, the exact last 200 rank0 lines (including the final partial line), last_step.log and stop_state.json are archived here. No complete-log warning recount was performed.
- All original fields/logs remain in `D:\sim_runs\T018c\high_trap_G0\it2`. Nothing was deleted or truncated. No restart, it3 or it2 postprocessing is authorized. Only the previously queued it1 postprocessing continues.
