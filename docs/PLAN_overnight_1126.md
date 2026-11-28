# Overnight plan 2026-11-25 23:20 → 2026-11-26 07:00 (Beijing)

Goal (user, after the teacher's two remarks): the plume looks too small, and the brightness trend does not match. Physics only; no tuning to the images.

## Diagnosis done at 23:15 (independent of the images: shock data, main-text Fig. 3d)
- **Shock radius:** the model's blast is smaller than the experiment's at both fluences. Between 150 and 900 ns, R_exp/R_model = 1.12 (high) and 1.37 (low). If Sedov R ∝ E^(1/5) holds, the experiment's gas carries about 1.7× (high) and about 5× (low) the model's energy. The model absorbs 158 µJ (high) and 45 µJ (low) in the gas, while 205 µJ and 247 µJ escape after reflection.
- **Ignition is late:**
  - Low fluence, experiment: the shock is at 39 µm at 10 ns, 140 µm at 30 ns and 216 µm at 55 ns.
  - Low fluence, model: 0 µm at 30 ns and 20 µm at 55 ns.
  - High fluence, experiment: 138 µm at 51 ns; model: 94 µm.
- **Brightness trend:** normalised at about 150 ns, the experiment keeps brightening to 200–300 ns (low: 1.32 at 280 ns), while the model decays from 140 ns on (low: 0.50 at 280 ns). A weaker, late-igniting plume decays earlier.
- **Conclusion:** both of the teacher's points trace to energy coupling during the pulse, meaning ignition time and absorbed fraction, not to a missing late-time sink. The plume-size and brightness problems are one problem.
- **Laser inputs checked:** the fluences in the paper (105 and 42 J/cm²) equal E/(π w²) with w = D/2 = 11 and 15.5 µm, so the beam inputs are consistent.

## Stages
1. **Coupling audit (cloud, 23:30–01:30).** For each fluence:
   - time-resolved E_gas_abs(t), the surface transmission τ(t) and A(t);
   - the first time the gas absorbs more than 1 µJ;
   - an early Sedov energy E_exp(t) from the experimental shock (strong-shock regime).

   With a-priori magnitudes, test:
   - (i) target absorptance A(T) of hot and liquid Cu at 1064 nm against literature (the model uses Drude from resistivity; registry A_RT 0.021);
   - (ii) electron seeding of the vapour by thermionic emission from the hot surface;
   - (iii) pulse temporal shape (Gaussian assumed) and the t0 convention;
   - (iv) the v1m avalanche, revisited, which was judged ineffective.
2. **Read the sensitivities as they arrive.** T033/T034 (workstation) and T035 (desk) include the A and sigma_en members, which show how ignition time and size respond within the declared ranges.
3. **Pre-register and implement the best-supported candidate (01:30–04:00).** Smoke it in the cloud, then launch on both nodes with high and low members on v1rs. Reading criteria are declared before results: ignition time, early shock R(t) against Fig. 3d, size σx, and the brightness curve normalised at 150 ns.
4. **Read the results and write the 07:00 summary** (figures redrawn).

## Resources
- **Workstation (64 cores):** T033 then T034, at most 60 cores.
- **Desk:** the user has freed it, so the limit rises to 18 cores. T035 runs two members at once (`--max-cores 16`); table builds use 2 workers.
- **Hourly reports:** short.
