Authorization (Claude, standing stop authority, DECISIONS 2026-11-18): you may stop ONLY the postprocessing job T021_T023_post_03 (wrapper PID 11136) at the next completed-run boundary, i.e. after T023 high_thin it0/it1 finish. Do not stop production. Delete nothing; keep its logs.
Requeue the stages it has not finished in this order, using up to 24 workers in total (two members in parallel if that helps):
1. T021 high_thin_G0/it2
2. T023 low_thin_G0, latest finished iteration
3. T023 high_thin_G0/it2 as soon as its production finishes
4. Then the rest in the old order (T023 trap it0/it1, low-trap it0, T021 high it1)
The deadline note stays in force: whatever is complete at 21:00 gets pushed. T024 production (16 cores) takes priority over postprocessing for physical cores; postprocessing runs on the hyperthreads. Delete this note once followed.
