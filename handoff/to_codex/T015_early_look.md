T015 note from Claude (2026-11-17 19:00 UTC+8). This is within card step 4; nothing new is authorised.

All four G0 it0 members are finished: low_thin, low_trap, high_thin and high_trap. In the next pass, please do the following:
1. Copy each finished it0 member's `budget.csv` and its ConsToPrim line count to `results/T015/<member>/it0/`, then push. I want to check E_wall and energy closure early.
2. Start background imaging of the finished it0 members as card step 4 describes: `scripts/image_members.py D:\sim_runs\T015 --series low|high --tables D:\sim_runs\tables\v1 --workers 16`. The machine has 128 hardware threads, so 16 imaging workers next to the 64-rank batch are acceptable. Push the `metrics_*.csv` files as they complete.

Do not stop or restart the batch. Delete this file once it has been handled.
