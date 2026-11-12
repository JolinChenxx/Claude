---
id: T002
title: Collect the earlier simulation work (PLUTO+Python and the other method) into the repository
owner: codex
status: ready
depends: []
milestone: M0
est_time: 1h
started:
---

## Background
Before this repository existed, the user had Codex on this workstation simulate Fig. S4 with **two methods**, one of which was PLUTO + Python. The supervisor (an original author of the paper) commented that "the overall trend is good, but late on the experiment fades while the simulation stays relatively bright." Claude needs the complete model, the parameter sources and the results of these two pieces of work in order to re-plan. **This card only collects and documents. Do not change any model and do not rerun.**

## Steps
1. Locate the working directories of the two earlier methods. If this thread's history or memory has the paths, use them directly. Otherwise search `C:\Users\jun`, `D:\` and `C:\` (skipping system directories) for: `pluto.ini`, `definitions.h`, `init.c`, and `.py` files / notebooks related to plume/S4/plasma. Record the paths you find in the report. If you cannot find them, set `blocked` and ask the user to provide the paths.
2. For each method, create a directory `prior/<method-name>/` (e.g. `prior/pluto/`, `prior/<other>/`) and copy in:
   - **All hand-written or modified source/config**: PLUTO's `definitions.h`, `pluto.ini`, `init.c`, any modified `src/` files (only the modified ones; use `diff` against the stock PLUTO to judge), makefile/compile configuration; Python pre/post-processing scripts and notebooks (clear notebook outputs of large images).
   - **Results**: synthetic images and figures compared with S4, curves of time vs. radius/area/volume/grey level (csv), any summary figures you showed the user/supervisor, key logs. Total ≤ 50 MB per method; do not commit raw data such as dbl/vtk/h5 (only list their location and size in the README).
3. For each method, write `prior/<method-name>/README.md` (Chinese is fine). Be **truthful and specific**; for anything you cannot confirm, write "unknown":
   - Software and version, how it was compiled (which compiler/environment: MSYS2? WSL? conda?), run command, cores, wall-clock time
   - Physical model: governing equations, dimension/geometry, grid and computational domain, boundary conditions; how the laser energy deposition / initial conditions are given (initial blast? source term? plasma absorption?); equation of state (ideal gas? γ value? ionization?); whether there is radiative cooling, thermal conduction, viscosity, species mixing
   - How "brightness" is computed from the flow field (which quantity → which formula → line-of-sight integration? normalization? threshold?)
   - **Complete list of parameters**: value, units, source (literature / paper / guessed / **adjusted to fit the experimental curves**). Mark every parameter that was adjusted to fit the experiment, with how it was adjusted and which data it was matched to.
   - Comparison with experiment: which curves agree and which do not (especially the time range and size of the "late-time brightness too high" problem)
   - Known problems, numerical instabilities, anything you yourself think is unreasonable
4. Write the report `tasks/T002.report.md` (summary + list of paths), set the card to `review`, write `handoff/to_claude/T002.md`, push.

## Acceptance criteria
- Both methods have a README and the key source/config; Claude can reconstruct the full model from the README alone
- The parameter table marks the source of every parameter, with fitted parameters explicitly flagged

## Not allowed
- Do not change the earlier code, do not rerun simulations, do not "polish" earlier results.
