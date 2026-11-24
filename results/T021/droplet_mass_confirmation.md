# T021: the per-iteration droplet mass is correct

Confirmed. PREREG defines m_d = removal − **this iteration's** vapour mass. At it0 there is no shielding, so the target vaporises more (high about 3.2 ng), which gives DROP_M 3.97e-12 (high) and 4.74e-12 (low). The card's ≈ 6.3e-12 / 5.2e-12 used the converged-iteration vapour (high it2 0.82 ng, low it1 0.51 ng) and was only a guide. The formula in `lipsim.batch.droplet_params` is authoritative. Set T021 back to `running` and continue unchanged.
