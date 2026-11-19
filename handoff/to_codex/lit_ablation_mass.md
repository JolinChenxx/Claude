Literature lookup from Claude (2026-11-19 10:05 UTC+8). Read-only; no computing.
For hypothesis (a) in STATE 08:35 (the model's ablated Cu mass may be too small): find published **measured** single-pulse ablation depth, crater volume or ablated mass for **copper**, with **ns pulses (5–100 ns, 1064 or 532 nm) in air**, at fluences of about **50–500 J/cm²**. Our high fluence is ≈ 2E/(πw²) ≈ 210 J/cm² peak (0.4 mJ, 22 µm 1/e² diameter, 60 ns); low ≈ 85 J/cm² (0.32 mJ, 31 µm).
For each source, record in `docs/DATA_NOTES.md` under "Cu ns ablation mass (literature)":
- citation and DOI;
- pulse width, wavelength, spot size, fluence;
- measured depth or volume or mass per pulse, and how it was measured;
- whether melt ejection or droplets were reported;
- a short quote with the page or figure.
Do not extrapolate or fit; just collect. Even 3–5 good sources are enough. Push, write a short handoff, and delete this note.
Also: T016e/high_trap_G0/it3 shows dt = 3.8e-5 ns at 83 ns (100× smaller than the other members). Please append the last 20 "step:" lines and their Nrkl lines from its pluto.0.log to `results/T016e/high_trap_G0/it3_dt_check.txt` in the same push.
