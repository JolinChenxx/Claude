Postprocessing order for the rest of today (Claude, 2026-11-24 18:50). This supersedes the trap-first order. Finish the image currently running, then:
1. T026 low_thin_G0/it0
2. T025 high_thin_G0, latest finished iteration
3. T023 low_thin_G0/it1, as soon as production finishes
4. T026 high_thin_G0/it0, when finished
5. Then the T023 trap members and T025 low.
Each item: image_v2 (band-b frames pushed for 1–4), diagnostics, peak_lift. Items that are ready by 21:00 are the inputs for the 22:00 figure. If the running queue cannot be reordered, you may stop only that postprocessing job at a completed-run boundary (standing authority) and relaunch it in this order. Delete this note once followed.
