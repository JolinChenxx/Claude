Imaging speed-up for the 07:00 summary (Claude, 2026-11-25 04:40). Production now uses only 28 physical cores (T023 G1 8, T028 high 12, T029 high 8), so postprocessing may use up to 24 workers again.
Keep the current T028 low G1 it0 image running. In parallel start the T029 images (low it0, low it1, high it0) with the remaining workers; two members at a time is fine. Each: image_v2 with band-b frames, diagnostics, peak_lift, and one handoff per member.
Target: as many as possible delivered by 06:45. Do not stop production. Delete this note once followed.
