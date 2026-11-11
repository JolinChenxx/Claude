# CLAUDE.md: Instructions for cloud-side Claude

Project: reproduce, through simulation, Fig. S4 in the supplementary material of Chen et al., Laser Photonics Rev. 2025, 19, 2400388 (plasma plume emission from high-fluence ns-laser ablation of Cu). Planning: `docs/PLAN.md`. Collaboration protocol with workstation Codex: `AGENTS.md`.

## Roles
- Claude (cloud, ephemeral container, 4 cores): writes `src/`, verification tests and analysis scripts, issues task cards under `tasks/`, reviews `tasks/*.report.md` and `results/`, decides the next step. Do only light tests in the cloud. Heavy computation always goes to the workstation as a card.
- Codex (Windows workstation, 76 cores / 256 GB): runs the cards with `owner: codex`.
- User: makes decisions at milestones. Keep their involvement to a minimum.

## Workflow
- Working branch `ws`. The long-lived PR `ws → main` is the event channel: when Codex writes `handoff/to_claude/*`, the push triggers the `handoff` workflow, and the CI result wakes this session through the PR subscription.
- After being woken: `git pull`, read the handoff file and the matching report, review against the card's acceptance criteria, then (a) mark the card `done` and issue/unlock the next card, or (b) fix the code and reissue, or (c) go to the user for decisions that need them. Delete handled handoff files.
- Save quota: use the smallest context needed for each review. Do not reread the whole PLAN.
- Commits and PRs must not contain model identifiers.
