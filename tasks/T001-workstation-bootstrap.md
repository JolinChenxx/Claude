---
id: T001
title: Workstation environment survey + collaboration-loop self-test
owner: codex
status: ready
depends: []
milestone: M0
est_time: 20min
started: 2026-11-12T08:38:42+08:00
---

## Resume notes (Claude, 2026-11-12)
- Step 1 (survey) is accepted, see `results/T001/env.txt`. Step 2 is done (`D:\sim_runs`). The MinGit deviation from the previous pass is accepted retroactively.
- **On resume, do only steps 3–4** (background self-test + report). Prerequisite: the user has run `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` once. Run `Get-ExecutionPolicy -List` first and record the output in the report.
- Rewrite the report: keep only a concise summary of this pass and the useful conclusions from before; delete the old pass-by-pass history.

## Goal
Find out what the workstation software environment can do (the basis for later choices of compiler/WSL route), and verify end to end that the "card → background job → report → handoff wake-up" loop works.

## Steps
1. Collect the following and write them to `results/T001/env.txt` (one item per line; write `not found` if absent):
   - `[System.Environment]::OSVersion`, `$PSVersionTable.PSVersion`, logical/physical core count (`Get-CimInstance Win32_Processor | Select Name,NumberOfCores,NumberOfLogicalProcessors`), total memory, NUMA node count
   - Free space on each drive (`Get-PSDrive -PSProvider FileSystem`)
   - `git --version`, `python --version`, `py -0p` (if present), `pip --version`
   - `where.exe cl g++ gcc clang cmake ninja mpiexec gfortran` (each)
   - Whether Visual Studio / Build Tools are installed: `& "${env:ProgramFiles(x86)}\Microsoft Visual Studio\Installer\vswhere.exe" -all -format json` (if present)
   - `wsl --status` and `wsl -l -v` (whether WSL2 and a distribution are already installed; **do not install**)
   - Whether you (Codex) can run commands without interactive confirmation, and whether processes started in the background survive after you exit (determined by step 3)
2. Create the raw-data root directory (prefer `D:\sim_runs`; if there is no D: drive, use the drive with the most free space and record the actual path in the report).
3. Loop self-test:
   - `.\scripts\win\job.ps1 start -Id T001 -Threads 4 -Cmd "ping -n 90 127.0.0.1 > nul && echo selftest-ok"`
   - Immediately run `status` once and confirm `STATE=running`.
   - **End this pass** (do not wait). On your next pass run `status` again and confirm `STATE=finished` and that stdout contains `selftest-ok`. If your working mode does not allow "the next pass", wait about 2 min in the same pass and then check, and say so in the report.
4. Write the report `tasks/T001.report.md`, set this card to `status: review`, create `handoff/to_claude/T001.md` with the content "T001 environment survey done, please review", then commit and push to `ws`.

## Outputs
- `results/T001/env.txt`
- `tasks/T001.report.md`

## Acceptance criteria
- env.txt covers every item in step 1
- The background job is reported as `finished` by `status` after the session ends, and exitcode = 0
- After the push, the GitHub Actions workflow `handoff` is triggered (Actions page of the repository)

## Not allowed
- Do not install any software (including WSL, compilers, Python packages). This card only surveys; Claude decides what to install.
