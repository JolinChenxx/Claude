Authority: handoff/to_codex/T018c-it2-both-alert.md, commit dea7dfe. Supersedes trap-only scope from 7278552.

# T018c it2: thin shows the same pattern. Extend the time box and extract the fields.

1. The time box in T018c-trap-it2-timebox.md now applies **to thin as well**: a member that advances < 1 ns of `t` in any 60 min of wall-clock time is stopped on its own. Nothing is deleted, and STOPPED.md and the budget get pushed. If both are stopped, the T018c batch ends; do not start it3.
2. Light extraction, once now, for **both** it2 members. Take the latest dbl snapshot and write `results/T018c/extract/it2_<member>_<t>.npz` with rho, T, prs, tr1, vx1, vx2, qlas, qrad for j in [0, 40] and i in [0, 30], plus the x1/x2 cell centres. Then, for every rank, give the last 3 `lip vmax` and the last 3 `lip dt-limit` lines. Also push `source/target_source.bin`'s companion `source_meta.json` and the source time profile, if it exists as csv/npz, for it1 and it2. Write a short handoff when this is pushed.
