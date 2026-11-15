---
id: T007
title: Download NIST ASD atomic data (levels, ionization energies, full-spectrum lines)
owner: codex
status: review
depends: []
milestone: M1
est_time: 30min
started: 2026-11-15T17:50:52+08:00
---

## Background
M1.1 needs NIST ASD data (B-grade sources) to build the partition functions, the full-spectrum net emission coefficient (NEC) and the in-band emission. The cloud sandbox cannot reach physics.nist.gov, but this workstation can (the historical COMSOL project downloaded from it before).

## Steps
1. Using `D:\sim_tools\envs\lipsim\python.exe` (via `scripts/win/run_lipsim.ps1` or by setting the proxy for this process only), run:
   `python scripts/fetch_nist.py --out data/atomic/nist`
   The script skips files already downloaded, so an interrupted run can simply be rerun.
2. Check `data/atomic/nist/manifest.json`: every file has `suspicious_html_or_error=false` and `data_rows > 0`. For any suspicious file, open it, look at the content and record it in the report (e.g. NIST returned an error page or "no lines"). **Do not edit the URLs or the data in the script**; if a query form is wrong, report it as blocked and give the content NIST returned.
3. Spot checks (record in the report): in `Cu_I_levels.csv`, does 3d9.4s2 2D5/2 appear at about 1.389 eV; in `Cu_I_lines.tsv`, can you find the lines around 510.55 / 515.32 / 521.82 nm (air wavelength) and their Aki.
4. Commit everything under `data/atomic/nist/` (expected total < 30 MB; if it is larger, report the size of each file and do not commit the files > 20 MB).
5. Report `tasks/T007.report.md`, set to `review`, write `handoff/to_claude/T007.md`, push.

## Acceptance criteria
- Ionization energies + levels + lines files exist and are non-empty: Cu I–V, N I–IV, O I–IV, Ar I–III (levels); Cu I–III, N I–II, O I–II, Ar I–II (lines)
- manifest records the URL, time and SHA256

## Not allowed
- Do not change the query parameters, and do not replace the data with tables from another source.
