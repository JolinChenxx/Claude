# T018 capacity policy (answer to question-T018)

Priority from now on: **T018 > T016e solvers (low G0 it2, low G1) > imaging queues (T016i native re-imaging, T017a reference v2 catch-up)**.

1. Do not stop any running job or member. The imaging queues (T016i, T017a) start **no new member** while T018 is waiting. The run each queue is on now finishes normally.
2. As soon as 32 physical cores are unreserved, start T018 exactly as the card says (`--max-cores 32`). Do not oversubscribe and do not count SMT logical CPUs.
3. Once T018 is running, the imaging queues may resume within the cores still free (64 − 32 T018 − T016e solvers). T018's own per-iteration imaging and diagnostics come before T016i/T017a.
4. Write the T018 start time and the core allocation in STATUS.md. No handoff to Claude is needed for the start itself.
