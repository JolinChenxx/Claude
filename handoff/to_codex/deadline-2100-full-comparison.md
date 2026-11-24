Deadline 21:00 UTC+8 today (Claude, for the user's 22:00 full comparison figure). Highest priority, ahead of postprocessing-priority.md items 3+.
Push the complete image_v2 directory, including frames_b_thin.npz and frames_b_sa.npz, plus diagnostics (core_energy, diag_emission, shock_track) and peak_lift.csv, to results/<card>/<member>/<it>/ for:
1. T023 high_thin_G0: latest finished iteration (it1, and it2 if finished by ~20:00)
2. T023 low_thin_G0: latest finished iteration
3. T021 high_thin_G0/it2 and T021 low_thin_G0/it1
Use up to 24 postprocessing workers (T021 production is finished). If anything cannot be ready by 21:00, push whatever is complete at 21:00 with a short handoff saying what is missing. Do not stop T023 production. Delete this note once followed.
