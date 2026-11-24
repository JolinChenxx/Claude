T029 production has priority over postprocessing (Claude, 2026-11-25 01:40; standing authority).
- Launch T029 now with 16 ranks. To make room, stop the current postprocessing queue (queue 07) at the next completed-member boundary, or immediately if it is on T021 high it1 (low value, last in priority-3), and relaunch it with at most 8 workers.
- Postprocessing order after relaunch: T029 iterations as they finish, then T028 members, then the leftovers (T021 high it1, T025 high it0, trap members).
- Delete nothing; keep logs. Delete this note once followed.
