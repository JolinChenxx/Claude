# User / supervisor decision record

## 2026-11-12
- **Supervisor (an original author of the paper):**
  - Do not dwell on the first few frames; focus on the overall trend afterwards. The first frames may be affected by noise and by errors in the experiment's time-zero setting.
  - Table 1 is wrong: "Beam waist radius" should be **diameter** (high fluence 22 µm, low fluence 31 µm).
  - Raw images, pixel size and other details will be requested later.
- **Supervisor:** the earlier PLUTO+Python result has a good overall trend, but late on the experiment fades while the simulation is still too bright.
- **Supervisor:** the model's physical credibility and interpretability must be guaranteed. **Adjusting parameters to fit the results is not allowed.** Anything resembling fabrication is to be avoided.
- **User:**
  - Agrees with all strategies in PLAN §11 (self-written open-source code, calibration/blind-prediction logic, leaving spatter/NPs/3D out of the model).
  - The workstation may be used without limit: install what is missing, and use disk and compute freely.
  - Re-plan using the earlier PLUTO results.

## 2026-11-15
- **User:** the simulation proceeds mainly along Claude's / PLAN v2 recommendations. Specifically:
  1. **Zero-fit logic accepted.** Agreement with the experiment should be **trend agreement first**: do not dwell on matching sizes. (Supervisor's view, reaffirmed.)
  2. Semi-empirical branch B (PLAN §4.5): following Claude's recommendation, the self-consistent model is the main line. Branch B is enabled only if the ignition gate fails, as a parallel variant clearly labelled "semi-empirical", and does not replace the self-consistent result.
  3. Spatter and nanoparticle dynamics are not modelled; they are listed as limitations.
  4. Historical PLUTO 72-hour parameter search controller: **user agrees to stop it** (2026-11-15). The user expects it has already exited automatically at the end of its 72-hour window. T004 step 0 checks; if it is still running, stop it. Do not delete or move data.
- Effect on PLAN: in §8, the trend metrics of brightness/evolution (P1, P2, P6, and the shape/direction of P3, P5) are the core of the judgement; absolute-size metrics are descriptive only.
- **User (2026-11-15):** the scheduled tasks S4Plume_Daily0700/Hourly/Watchdog were set up by an earlier Codex session, not by the user, and the user agrees to disable them as recommended (disable only, do not delete; reversible).

## 2026-11-15 (evening)
- **User:** the whole job must be **finished within two weeks**; deliver **a relatively optimised first result within 24 hours**; keep Codex fully busy. Fable re-planned to PLAN v3.
- **User:** "Once planning is done, have Codex start straight away and report the plan to me." → T008 and T009 (no new code needed) set to ready immediately.
- PLAN v3 route: the production hydro core is the unmodified PLUTO 4.4-patch4; all physics goes in as our own reviewable user modules (table EOS, 1064 nm absorption, NEC radiation, evaporation source); lipsim handles property tables / target model / imaging / metrics. Energy and mass are still not inputs; the historical fitted values are not used. This replaces the 11-12 decision that "self-written code is the only production code". (Implemented under the user's instruction above; the items in PLAN v3 §15 are reported to the user and can be overruled at any time.)

## 2026-11-18 08:15 (UTC+8) — stop the remaining T016 (v1b) members
User: "1，停掉吧 按你的建议来". The two remaining v1b high-fluence members (4 of 6 v1b members had aborted with the energy NaN fixed in v1d; the rest were nearly stalled by the RKL step-ratio cap) are stopped. High-fluence members will be rerun with v1d after the T016s rmax_par A/B check.

## 2026-11-18 08:20 (UTC+8) — standing authorization to stop failing or stalled computations
User: "对于类似这种某些计算跑不下去需要停掉的情况，我授予你完全权限，你可以自己确定他是否需要停止，我只需要你能给我好的计算结果就行".
Claude may decide on its own to stop workstation runs that are failing, stalled or superseded, and relays the stop to Codex in a handoff note that cites this entry. Conditions: data is never deleted; the reason, last state and stop time are recorded in results/ and STATE.md; the user is told at the next hourly report.
