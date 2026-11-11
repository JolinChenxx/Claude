# AGENTS.md: Instructions for Codex on the workstation

This repository is a two-agent project: **cloud-side Claude** writes the physics, the code and the task cards, and reviews results; **workstation Codex (you)** compiles, runs, monitors and reports back. All state lives in the Git repository. The two sides never talk directly.
The project goal and technical route are in `docs/PLAN.md`. Read it before you start; on every later pass, rereading only the parts your current task card cites is enough.

## Workstation
- Windows, 2× Xeon Platinum 8374C (76 cores), 256 GB RAM. Use PowerShell for commands. If the PLAN requires WSL2, follow the task card.
- Working branch: **`ws`** (do not push to `main`). Always `git pull --rebase origin ws` before `git push origin ws`.

## Execution protocol for each pass (manual trigger or automation)
1. `git pull --rebase origin ws`.
2. In `tasks/`, find the card with the lowest number where `owner: codex` and `status: ready` and whose `depends` are all `done`. If none, stop this pass and output "no executable task".
3. Set the card's `status` to `running`, fill in `started:`, then commit and push. This is your lock, so nothing runs twice.
4. Do the work as the card says. Follow the steps and acceptance criteria. Do not change the physical model or numerical parameters on your own.
   - **Long computations** (expected > 10 min): always start them in the background with `scripts/win/job.ps1 start`, then end this pass. On the next pass check them with `job.ps1 status`. Never block and wait in the foreground.
   - Compile errors, missing dependencies, path or encoding problems and other **engineering issues** may be fixed directly, with a note in the report.
   - **Physics or numerical issues** (non-convergence, NaN, results clearly unphysical, an acceptance criterion missed): do not tune parameters yourself. Write the details into the report, set `status: blocked`, and hand off (step 6).
5. Write the report to `tasks/<card-id>.report.md`, following the template in `tasks/TEMPLATE.report.md`. Put small output files (png/csv/json/logs, total ≤ 20 MB) in `results/<card-id>/`. Do not commit large raw data. It goes in `D:\sim_runs\` (or the path the card specifies).
6. Set the card's `status` to `review` (or `blocked`), create `handoff/to_claude/<card-id>.md` (one or two lines saying what needs review), then commit and push. That push automatically wakes Claude.
7. If there are more executable cards, continue with the next one.

## Hard rules
- Only change files inside this repository and the raw-data directory. Do not change system settings, do not install software that needs administrator rights (if it is required, write `blocked` and explain), and do not delete other people's data.
- Do not edit the physics in `docs/PLAN.md`, `src/` or `tasks/` cards. You may fix engineering details such as compilation or paths, and must list every change in the report.
- Do not fabricate or "polish" results. Record failures as they happened.
- Commit messages start with `[codex] <card-id>: `.
