# Codex pass log (latest first)

## 2026-11-18 01:23 (UTC+8)
- card: T015, T016 | action: completed T015 collection, comparison, validation and review handoff (ws b949412); synced approved deliverables with hash verification.
- running jobs: T016 production; all T015 solver/imaging jobs finished rc=0.
- T016 progress: high_thin_G0/it0: t_ns=5.699978e+001; high_thin_G1/it0: t_ns=3.999874e+001; high_trap_G0/it0: t_ns=5.599832e+001; low_thin_G0/it0: t_ns=1.084996e+002; low_thin_G1/it0: t_ns=7.749895e+001; low_trap_G0/it0: t_ns=1.094967e+002
- skipped ready cards and why: none.
- notes: T015 14 solver + 14 image successes, 252 scenarios; 44,293,192 warnings, 40 negative P1, 222 no-crossing P2; max absolute residual 0.0344352%; package 15.03 MB. Scientific review pending; no tuning/reruns.

## 2026-11-18 01:05 (UTC+8)
- card: T015, T016 | action: verified all 14 T015 solver completions rc=0; published imaging4 results; launched T016 production and final T015 imaging.
- running jobs: T016 PID 1492, 64-core batch, started 01:05, initial status running/stderr empty; T015img5 PID 27116, 16 imaging workers, initial status running/stderr empty.
- progress: T015 solver finished (G0/G1 high 800 ns, low 1300 ns), 13/14 images published; high_trap_G0 it2 pending imaging. T016 just launched with verified v1b tables/build.
- skipped ready cards and why: none.
- notes: launch records pushed on ws (9044806). Next pass complete T015 collection/report/handoff after imaging5, and check T016.

## 2026-11-18 00:49 (UTC+8)
- card: T015, T016 | action: preserved two rc=0 solver completions (high thin G0 it2 and G1 it0); launched T015img4 with 16 imaging workers.
- running jobs: T015 solver batch; T015img4 PID 37776. T016 tables/build verified, production waiting.
- solver progress: high_thin_G0/it2: t_ns=8.000000e+002; high_thin_G1/it0: t_ns=8.000000e+002; high_trap_G0/it2: t_ns=7.474939e+002; low_thin_G0/it2: t_ns=1.300000e+003; low_thin_G1/it0: t_ns=1.300000e+003; low_trap_G0/it2: t_ns=1.300000e+003
- skipped ready cards and why: none; T016 production awaits T015 solver completion per task step 3.
- notes: launch record pushed on ws (2968636); no new handoff, no solver reruns or parameter changes.

## 2026-11-18 00:33 (UTC+8)
- card: T015, T016 | action: synced ws and checked T015 production; no new handoff or ready card.
- running jobs: T015 solver batch (running, stderr empty); imaging jobs finished. T016 tables/build verified, production waiting.
- solver progress: high_thin_G0/it2: t_ns=7.559992e+002; high_thin_G1/it0: t_ns=7.939970e+002; high_trap_G0/it2: t_ns=6.624961e+002; low_thin_G0/it2: t_ns=1.300000e+003; low_thin_G1/it0: t_ns=1.300000e+003; low_trap_G0/it2: t_ns=1.300000e+003
- skipped ready cards and why: none; T016 production awaits T015 solver completion per task step 3.
- notes: solver progress continues; no new completed solver registry rows, reruns or parameter changes.

## 2026-11-18 00:18 (UTC+8)
- card: T015, T016 | action: synced ws and checked T015 production; no new handoff or ready card.
- running jobs: T015 solver batch (running, stderr empty); imaging jobs finished. T016 tables/build verified, production waiting.
- solver progress: high_thin_G0/it2: t_ns=6.684997e+002; high_thin_G1/it0: t_ns=7.479800e+002; high_trap_G0/it2: t_ns=5.924977e+002; low_thin_G0/it2: t_ns=1.300000e+003; low_thin_G1/it0: t_ns=1.300000e+003; low_trap_G0/it2: t_ns=1.300000e+003
- skipped ready cards and why: none; T016 production awaits T015 solver completion per task step 3.
- notes: solver progress continues; no new completed solver registry rows, reruns or parameter changes.

## 2026-11-18 00:03 (UTC+8)
- card: T015, T016 | action: synced ws and checked T015 production; no new handoff or ready card.
- running jobs: T015 solver batch (running, stderr empty); imaging jobs finished. T016 tables/build verified, production waiting.
- solver progress: high_thin_G0/it2: t_ns=6.149980e+002; high_thin_G1/it0: t_ns=7.079951e+002; high_trap_G0/it2: t_ns=5.404991e+002; low_thin_G0/it2: t_ns=1.300000e+003; low_thin_G1/it0: t_ns=1.300000e+003; low_trap_G0/it2: t_ns=1.300000e+003
- skipped ready cards and why: none; T016 production awaits T015 solver completion per task step 3.
- notes: no new completed solver registry rows; no reruns or parameter changes.

## 2026-11-17 23:49 (UTC+8)
- card: T015, T016 | action: checked production; collected and handed off T015img3 completion (rc=0, 220.098942 seconds) and low G1 imaging.
- running jobs: T015 solver batch; imaging jobs finished. T016 tables/build verified; production waiting.
- solver progress: high_thin_G0/it2: t_ns=5.709978e+002; high_thin_G1/it0: t_ns=6.744922e+002; high_trap_G0/it2: t_ns=4.894955e+002; low_thin_G0/it2: t_ns=1.300000e+003; low_thin_G1/it0: t_ns=1.300000e+003; low_trap_G0/it2: t_ns=1.300000e+003
- skipped ready cards and why: none. T016 production waits for T015 solver completion as required by its card; solver progress continues.
- notes: eleven image summaries / 198 metrics CSV files published on ws (a4a7e7f); no new incoming instructions.

## 2026-11-17 23:36 (UTC+8)
- card: T015, T016 | action: validated T016 tables and isolated MPI build; started T015img3 (16 imaging workers); established status worktree.
- running jobs: T015 solver batch; T015img3 PID 32292. T016tab finished rc=0 (805.747013 s); T016 build rc=0, 7 replacements verified.
- solver progress: high_thin_G0/it2: t_ns=5.259963e+002; high_thin_G1/it0: t_ns=6.429953e+002; high_trap_G0/it2: t_ns=4.374937e+002; low_thin_G0/it2: t_ns=1.300000e+003; low_thin_G1/it0: t_ns=1.300000e+003; low_trap_G0/it2: t_ns=1.300000e+003
- skipped ready cards and why: none; T016 is running, but its production launch waits for T015 solver completion.
- notes: all v1b fields finite, shape [81,145,14], D_cuair/dh_dY present; old tables/build unchanged.

## 2026-11-17 23:15 (UTC+8) — branch created by Claude
- card: none | action: branch initialised; Codex writes the next entry.
