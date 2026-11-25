---
id: T033
title: One-at-a-time sensitivity of v1n over declared registry ranges; 9 inputs × high/low = 18 members, 3 variant tables; up to 48 cores
owner: codex
status: ready
depends: []
milestone: D2
est_time: 0.5 h ops + table builds (~3 × table time) + ~3 waves of 3.5 h (high) / 2 h (low)
---

## Background
PREREG_v0, "T033 OAT declared 2026-11-25 ~18:30". This is sensitivity of the reported model v1n, not a fit, run so that the workstation has useful work while radiative transfer is developed.

The batch configs/batches/T033.yaml sets `overrides:` per member. lipsim.batch applies them in-process and passes them to the target-model subprocess through the env variable LIPSIM_OVERRIDES. Each run directory gets an overrides.json. Tests: tests/test_overrides.py.

## Steps
1. Run `git pull --rebase origin ws`, then `python -m pytest -q tests/test_overrides.py tests/test_droplet_v1r.py`. Use the existing build_v1s; no rebuild is needed.
2. Build the variant tables. Each sets the override for the table build only. In PowerShell, set the variable in the same command:
   - `$env:LIPSIM_OVERRIDES='{"sigma_en_Cu": 3.0e-20}'; D:\sim_tools\envs\lipsim\python.exe -m lipsim.tables.build --config configs/tables/v0.yaml --out D:\sim_runs\tables\v1c_sigen_lo --workers 32`
   - the same with `3.0e-19` → `v1c_sigen_hi`;
   - `{"gaunt_ff": 1.5}` → `v1c_gaunt15`.
   - Clear the variable afterwards with `Remove-Item Env:LIPSIM_OVERRIDES`.
   - Record the hashes. Check that eos.bin is byte-identical to v1c, because these inputs affect optics only; report it if it is not.
3. Launch: `scripts\win\job.ps1 start -Id T033 -Threads 1 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T033.yaml --max-cores 48"`.
   - The members that do not need variant tables can start before the table builds finish only if you split the batch. Otherwise build the tables first.
   - Raise --max-cores to 56 once T031 imaging is finished.
   - Check every pluto.ini and overrides.json:
     - rem: DROP_M scales ×1/3 and ×3 against T026.
     - sig: SIG_EN 3e-20 and 3e-19.
     - A and ftc: the target source_meta budget changes.
4. For each member: production checks as in T030, then image_v2 band b, peak_lift and diagnostics. Postprocessing goes after the T031 imaging.
5. Write a handoff after each member is imaged (batching two or three is fine). Report in tasks/T033.report.md.

## Not allowed
- Changing any value outside the batch file.
- Using overrides on any other card.
- Deleting data.
