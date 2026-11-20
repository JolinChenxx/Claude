---
id: T003
title: Install Miniforge (user-level) and create the lipsim environment
owner: codex
status: done
depends: []
milestone: M0
est_time: 30min
started: 2026-11-12T11:29:34+08:00
---

## Goal
Get a reproducible Python + Numba runtime environment on the workstation (PLAN §3.5). No administrator rights needed.

## Steps
1. Download the official installer through the proxy (`http://127.0.0.1:7897`):
   `https://github.com/conda-forge/miniforge/releases/latest/download/Miniforge3-Windows-x86_64.exe`
   Record its SHA256 and the actual version in the report.
2. Silent user-level install to `D:\sim_tools\miniforge3`, **without** modifying PATH or registering as the system Python:
   `Start-Process -Wait .\Miniforge3-Windows-x86_64.exe -ArgumentList '/InstallationType=JustMe','/RegisterPython=0','/AddToPath=0','/S','/D=D:\sim_tools\miniforge3'`
3. Configure the conda proxy (write only to `D:\sim_tools\miniforge3\.condarc`: `proxy_servers: {http: http://127.0.0.1:7897, https: http://127.0.0.1:7897}`), then:
   `D:\sim_tools\miniforge3\Scripts\conda.exe env create -f env\environment.yml -p D:\sim_tools\envs\lipsim`
   (If conda-forge is too slow or times out, switching to the Tsinghua mirror `https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud/conda-forge` is allowed. Note it in the report.)
4. Export the locked versions: `conda list -p D:\sim_tools\envs\lipsim --explicit > results\T003\conda-explicit-win.txt`
5. Self-check: `D:\sim_tools\envs\lipsim\python.exe scripts\check_env.py --threads 1 8 16 32 64 > results\T003\check_env.json` (foreground is fine, it finishes within a minute).
6. Add one line to the "Workstation" section of `AGENTS.md`: `Python: D:\sim_tools\envs\lipsim\python.exe` (this is the only allowed change to AGENTS.md).
7. Report, set the card to `review`, write the handoff, push.

## Outputs
- `results/T003/conda-explicit-win.txt`, `results/T003/check_env.json`
- `tasks/T003.report.md`

## Acceptance criteria
- Every library in check_env.json imports successfully (no `IMPORT FAILED`)
- `threading_layer_used` is `tbb` or `omp`; record the 1→64 thread stencil throughput (no hard requirement)

## Not allowed
- Do not change system/user environment variables, do not install to C:\Program Files, do not use pip to install anything outside environment.yml.
