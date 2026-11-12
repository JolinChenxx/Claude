# AGENTS.md: Instructions for Codex on the workstation

This repository is a two-agent project: **cloud-side Claude** writes the physics, the code and the task cards, and reviews results; **workstation Codex (you)** compiles, runs, monitors and reports back. All state lives in the Git repository. The two sides never talk directly.
The project goal and technical route are in `docs/PLAN.md`. Read it before you start; on every later pass, rereading only the parts your current task card cites is enough.

## Workstation (measured by T001)
- Windows 11 (10.0.22631), PowerShell 5.1; 2× Xeon Platinum 8375C = **64 physical cores / 128 logical**, 2 NUMA nodes, 256 GB RAM. D: drive has 2.5 TB free.
- Raw-data root: `D:\sim_runs`. Tool directories: `D:\sim_tools\` (Miniforge etc., installed by the cards).
- Git: `%LOCALAPPDATA%\CodexTaskTools\MinGit-2.56.1\cmd` (prepend it to PATH each pass). Network goes through the local proxy `http://127.0.0.1:7897`.
- **You may set repository-local git config** (`git config --local`, written to `.git/config`): `http.proxy`, `user.name=Codex`, `user.email=codex@openai.com`, so you don't redo it every pass.
- PowerShell scripts in the repository: the user has allowed the current user to run local scripts (RemoteSigned). **Do not try to change the execution policy yourself or bypass it with -ExecutionPolicy**. If you still get an execution-policy error, report it as blocked.
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
