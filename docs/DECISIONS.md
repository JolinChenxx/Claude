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
