# PREREG: pre-registration of the frozen baseline (skeleton, **not frozen**)

> Status: **DRAFT**. Freezing = this file is committed with "Status: FROZEN" + the commit hash, **before** any 2D case is compared with the comparison set (PLAN v2 §1.5). After freezing, the body may not be changed; the only permitted modification is a new entry appended to §9 "Deviation log".

## 0. Freeze information (filled in when frozen)
- Freeze commit: `TBD`
- `data/params/registry.yaml` SHA256: `TBD` (`python -c "from lipsim.params import registry_hash; print(registry_hash())"`)
- `data/atomic/*` table hashes: `TBD`
- Code version (`src/lipsim`) commit: `TBD`
- Statement: the planner and the implementer have both seen S3/S4/S7 and the historical fitted results; this is therefore not a strictly blind prediction. Credibility rests on the parameter-provenance rules, the freeze, and the complete run registry.

## 1. Model equations (copy the final form in after M1–M3; PLAN v2 §3.7)
- Gas phase: TBD
- Target and Knudsen layer: TBD
- Laser absorption (target + inverse bremsstrahlung in the gas phase): TBD
- Radiative cooling (full-spectrum NEC): TBD
- Equation of state and transport properties: TBD (table sources and hashes)

## 2. Parameters
- The registry is the single source. List of grade F choices: TBD (exported automatically from the registry).

## 3. Metric definitions (implemented by `src/lipsim/pipeline/metrics.py`; the code at freeze time is authoritative)
- P1 τ_L: e-folding time from a linear fit of ln L(t) over 300–630 ns. L = Σ(I−B) inside the automatic mask (corner background B; mask I−B > max(5, 3σ), opened and dilated by 2 px).
- P2 Δt₁₀: time after 300 ns at which L first drops below 10% of L(300 ns).
- P3: the growth→shrinkage turning time and sign of Δ(σx²+σz²) (relative to 300 ns). The magnitude is descriptive only (DECISIONS 2026-11-15).
- P4: shock trajectory R_s(t) at 100–900 ns, late-time speed (independent check set; may not be used to adjust parameters).
- P5: centroid rise Δz_c(t) (relative to 300 ns), 150–630 ns.
- P6: direction of change between the two fluences in τ_L, Δt₁₀ and the timing of the size peak.
- Experimental values: `data/exp/meta.yaml`, `metrics_*.csv`, `curves/fig3d_shock.csv` (commit `TBD`).

## 4. Pass criteria (copied from PLAN v2 §8.3, incl. the 2026-11-15 amendment)
| Metric | Pass | Partial pass | Fail |
|---|---|---|---|
| P1 τ_L | model/experiment ∈ [0.67, 1.5] | ∈ [0.4, 2.5] | otherwise |
| P2 Δt₁₀ | relative deviation ≤ 25% | ≤ 50% | otherwise |
| P3 | turning time deviation ≤ 30% and same sign | same sign | opposite sign |
| P4 R_s | RMS relative deviation ≤ 10%, late speed 550 ± 100 m/s | ≤ 20% | otherwise |
| P5 Δz_c | same direction and magnitude within a factor 2 | same direction | opposite direction |
| P6 | same direction of change | — | opposite direction |
- Expectation stated in advance: P5 will probably fail (the model has no spatter to lift the plume).
- Frames at 0–60 ns are reported, not judged.

## 5. Scenarios (all reported, none chosen as "the right one")
- Filter: a 515±5 / b 510±12 / c 500±20 nm. Defocus: 250 (baseline) / 0 / 500 µm. Pulse shape: Gaussian (baseline) / MOPA datasheet waveform.
- Gain: one normalising scalar for the secondary metrics only (t_ref = 300 ns), per series; also report the result with the low-fluence series using the high-fluence scalar.

## 6. Sensitivity design (forward propagation, no calibration; PLAN v2 §9)
- Inputs and ranges: TBD (from the registry `uncertainty` field). Method: Morris screening → forward ensemble of the important inputs.

## 7. Model-form escalation ladder (triggered only by the model's internal diagnostics; PLAN v2 §3.6)
- TBD: copy in the trigger thresholds for each level (LTE validity, optical thickness, two-temperature relaxation time, …).

## 8. Run registry
- Every 2D run is appended automatically to `results/run_registry.csv`; the run registry is attached in full to the paper's supplementary material.

## 9. Deviation log (append only after freezing)
| Date | Type (error correction / new E-grade information) | Change | Independent evidence | Before/after results |
|---|---|---|---|---|
