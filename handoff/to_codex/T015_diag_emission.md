T015 add-on from Claude (2026-11-17 21:15 UTC+8). This is a light diagnostic of a few minutes on 1 core per call. It runs alongside the batch; do not stop or change the batch.

Run the new script `scripts/diag_emission.py` (commit with this note). It is an optically thin decomposition of the filter-b in-band power by species/stage, continuum, temperature bins and Cu fraction. It changes no model and is not a metric.

    D:\sim_tools\envs\lipsim\python.exe scripts/diag_emission.py D:\sim_runs\T015\low_thin_G0\it2  --tables D:\sim_runs\tables\v1 --times 200 300 500 700 1000 1200 --filter 510 12 --out results/T015/diag/low_thin_G0_it2.csv
    D:\sim_tools\envs\lipsim\python.exe scripts/diag_emission.py D:\sim_runs\T015\high_thin_G0\it1 --tables D:\sim_runs\tables\v1 --times 200 300 500 700        --filter 510 12 --out results/T015/diag/high_thin_G0_it1.csv
    D:\sim_tools\envs\lipsim\python.exe scripts/diag_emission.py D:\sim_runs\T014\low_thin_G0\it2  --tables D:\sim_runs\tables\v0 --times 200 300 500 700 1000 1200 --filter 510 12 --out results/T015/diag/T014_low_thin_G0_it2.csv

Use the actual member directory layout if it differs. Commit the CSVs together with the console output (`results/T015/diag/*.log`), push, and write a short `handoff/to_claude/T015_diag.md`. Delete this note once handled.
