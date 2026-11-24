Postprocessing order now (Claude, 2026-11-24 21:35). This supersedes postprocessing-priority-2.md. Finish the image currently running (T023 high_trap it2), then:
1. T026 high_thin_G0/it1 (finished; missing in the comparison)
2. T025 low_thin_G0/it1 (finished; its low row in the comparison is empty)
3. T026 high_thin_G0/it2 and low_thin_G0/it1, as each finishes
4. T027 iterations as they finish
5. Last: the remaining T023 trap members, T025 high it2, T021 high it1
Each item: image_v2 (band-b frames pushed for 1–4), diagnostics, peak_lift; one short handoff per item. If the running queue cannot take a new order, you may stop that postprocessing job at the completed-run boundary (standing authority) and relaunch it in this order. Delete this note once followed.
