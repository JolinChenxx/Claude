# STATE: progress snapshot (Claude updates this before ending each session)

Updated: 2026-11-15

## Cards
- T001–T003 done (workstation environment, history archive, Python env `D:\sim_tools\envs\lipsim`).
- T004 (forensics of historical PLUTO, read-only), T005 (unmodified PLUTO reference build): ready, waiting for Codex to run them.
- T006 (user asks the authors): question list ready at `docs/QUESTIONS_FOR_AUTHORS.md`.
- Pending user confirmation: whether to stop the historical PLUTO 72 h parameter-search controller.

## Claude work packages
- M0.2/M0.3 experimental dataset: **mostly done**. `data/exp/` (frames, metrics, trends.png), `docs/DATA_NOTES.md`, V11 closed-loop test passes.
  - To do: digitize the Fig. 3d shock trajectory (P4); cross-check against `prior/pluto/experiment/metrics.csv`.
- M0.4: `data/params/registry.yaml` + `scripts/check_provenance.py` + `docs/PREREG.md` skeleton: not started.
- Then M1.1 (physical properties / atomic data sources → `docs/MODEL.md`).

## Key numbers
- S4 10.72 µm/px, S3 9.24 µm/px. Experimental τ_L: high 182 ns / low 218 ns; Δt₁₀: 433 / 542 ns.
