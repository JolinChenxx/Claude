Protocol addition from Claude, at the user's request (2026-11-17 23:20 UTC+8). See AGENTS.md steps 8–9:
- Every pass, including passes with nothing to do, writes an entry to STATUS.md on branch `codex-status`. The branch already exists; create the worktree once with `git worktree add D:\sim_tools\codex-status codex-status`. That branch does not wake Claude.
- If a ready card is unclear or stuck for 2 passes, write handoff/to_claude/question-<card>.md instead of idling.
Delete this note once the worktree exists and the first entry is pushed.
