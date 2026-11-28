---
id: T036
title: Source check for the ignition inputs — paper timing (t = 0), the laser's temporal waveform, Cu absorptance at 1064 nm vs temperature; verbatim quotes only
owner: codex
status: done
started: 2026-11-25T23:31:16+08:00
depends: []
milestone: D2
est_time: 0.5–1 h (no computation)
---

## Background
docs/PLAN_overnight_1126.md: the model ignites the plasma 25–50 ns later than the experiment. The experimental shock is already at 39 µm at 10 ns (low fluence), and the model's gas energy is 1.7× (high) to 5× (low) short. Three input facts decide whether this is an input issue or missing physics. Quote sources verbatim, with page and line, in the style of data/exp/paper/droplets_notes.md, and add no interpretation.

## Steps
1. **Paper main text and SI (local PDFs):**
   - every sentence on how t = 0 of the plume, pump-probe and shock images is defined: trigger, delay generator, photodiode, synchronisation, jitter;
   - every sentence on the laser: model (IPG YLPN?), pulse-duration definition, waveform, repetition, beam diameter definition, M².
   - Write data/exp/paper/timing_laser_notes.md.
2. **Laser waveform:** search the IPG documentation and application notes, and published measurements of IPG YLPN or YLP pulse temporal profiles, preferably for 60 ns settings at 1064 nm. If a measured waveform plot exists, record the source (URL, figure) and digitise it to data/exp/laser/waveform_<source>.csv (t_ns, P_norm, with a digitisation uncertainty), plus an overlay check PNG. No smoothing, no fitting.
3. **Cu absorptance at 1064 nm against T:** Mattern et al. 2023, J. Mater. Eng. Perform., doi 10.1007/s11665-023-08961-8 (and the related paper doi 10.1007/s40194-023-01672-7). Record the measured reflectance or absorptance at room temperature for each surface condition, just below the melting point, and liquid at or above it, including the size of the jump at melting. Write data/exp/paper/cu_absorptance_notes.md.
4. Push, and write handoff/to_claude/T036.md listing what was found and what was not.

## Not allowed
- Computation that changes any run.
- Guessing values that are not in a source.
