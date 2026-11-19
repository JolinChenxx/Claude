---
id: T017a
title: Verify the Cu I Stark widths (Popov et al. 2019) and image with Voigt/Stark (imaging v2) as a sensitivity set
owner: codex
status: review
started: 2026-11-19T05:04:25+08:00
depends: []
milestone: D2
est_time: 0.5h (reading the paper) + background imaging
---

## Background
The imaging profile is Doppler only. At n_e ≈ 1e23 m⁻³ the Stark widths of Cu I 515.3 and 521.8 nm are about 100× the Doppler width at 10 kK, according to Popov et al. 2019 (MNRAS; arXiv:1907.02920, as reported by a web search: 17 / 152 / 180 pm). In a cloud smoke snapshot, adding Voigt/Stark profiles raises sa/thin from 0.15 to 0.87. This is a pre-declared v1 item ("Voigt"). The registry entry `stark_cu1_lines` is **unverified**: the normalisation density and the FWHM vs HWHM convention are not yet confirmed.

## Steps
1. Open arXiv:1907.02920 (or the MNRAS version) from the workstation. Record in `docs/DATA_NOTES.md`, under "Stark widths Cu I", with quotes and the table/page:
   - for each of the three lines (510.55, 515.32, 521.82 nm): the width value;
   - whether it is FWHM or HWHM;
   - the electron density and temperature it refers to (measured or normalised);
   - the stated uncertainty;
   - any temperature-dependence or ion-broadening parameter given.
   If the values or the convention differ from the registry entry, **do not edit the registry**. Report the differences in the handoff.
2. In parallel, as the reference set (widths verified 2026-11-19; the T dependence above 7.7 kK is an extrapolation and is treated as a sensitivity), run in the background, after T016i and with 16 workers:
   `scripts/image_members.py D:\sim_runs\T016e --series high|low --tables D:\sim_runs\tables\v1c --scenarios configs/imaging/v2.yaml --outname image_v2`
   Run it for the finished T016e iterations. Commit the metrics and summaries as in T016i.
3. Write a handoff once step 1 is done, and another once the T016e image_v2 set is done.

## Not allowed
- Changing the registry values yourself, or choosing among literature values. Claude decides from the quoted data.
