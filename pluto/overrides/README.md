# Replaced upstream PLUTO files (PLAN v3 §5 rule 2)

The physics enters through official extension points (init.c, pvte_law.c, userdef_output.c) plus five
replaced upstream files (the last two only in v1 builds with `--tc`). The full diffs against the official 4.4-patch4 sources are in this directory and
are also regenerated automatically in every build's `build_provenance.json` (`scripts/build_pluto.py`).

| Upstream file | Replacement | Reason |
|---|---|---|
| Src/split_source.c | pluto/lip/split_source.c | One added call `LipSource(d, dt, Dts, grid)` (laser, radiation, evaporation as operator-split sources). The function's own documentation lists "additional user-defined terms" as the intended use. |
| Src/EOS/PVTE/thermal_eos.c | pluto/lip/thermal_eos.c | The upstream LTE branch tabulates T(p/ρ, ρ) from GetMu(T, ρ), which cannot depend on the Cu tracer; p(ρ,T,Y) and its exact inverse come from the lipsim LTE table instead. |
| Src/EOS/PVTE/internal_energy.c | pluto/lip/internal_energy.c | Same reason for ρe(ρ,T,Y) and T(ρ,ρe,Y); the sound speed uses Gamma1() from the table's equilibrium c². |
| Src/parabolic_update.c | pluto/lip/parabolic_update.c | v1 heat conduction: upstream sets the conduction temperature to p/ρ (ideal gas). One line changed: T from the LTE table inverse `lip_T_from_prim` (K). |
| Src/Thermal_Conduction/tc_flux.c | pluto/lip/tc_flux.c | Same reason: the diffusion coefficient used for the RKL stage count is (γ−1)κ/ρ upstream; replaced by κ/(ρ c_v) with the table c_v. The flux κ∇T itself is unchanged. κ comes from user file tc_kappa.c (table field `kappa`, registry `kappa_e_model`/`kappa_h_model`). |

No file of the hydro core (Riemann solvers, reconstruction, time stepping, geometry, MPI) is modified.
