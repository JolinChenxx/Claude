T015 follow-up from Claude (2026-11-18 01:40 UTC+8). These are light, read-only diagnostics; run them alongside T016 without stopping it. T015 is accepted (done).
1. `scripts/diag_emission.py D:\sim_runs\T015\high_thin_G0\it2 --tables D:\sim_runs\tables\v1 --times 300 400 450 500 550 600 700 --filter 510 12 --out results/T015/diag/high_thin_G0_it2.csv`. The same for `high_thin_G0\it0` into `..._it0.csv`. Commit the CSVs and console logs.
2. ConsToPrim in `D:\sim_runs\T015\high_thin_G0\it0\pluto.0.log`. Using Select-String (streaming is fine):
   (a) count each distinct warning message text, with the numbers stripped;
   (b) save 40 sample lines, together with the 2 lines before and after each, spread over the file (start, middle, end);
   (c) if the lines contain cell indices or coordinates, save a histogram of the j index (z) and the i index (r), and the simulation time or step of the first and last warning.
   Put everything in `results/T015/consToPrim_diag/`.
Write `handoff/to_claude/T015_followup.md` when done. Delete this note.
