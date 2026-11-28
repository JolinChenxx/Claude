# User / supervisor decision record

## 2026-11-12
- **Supervisor (an original author of the paper):**
  - Do not dwell on the first few frames; focus on the overall trend afterwards. The first frames may be affected by noise and by errors in the experiment's time-zero setting.
  - Table 1 is wrong: "Beam waist radius" should be **diameter** (high fluence 22 µm, low fluence 31 µm).
  - Raw images, pixel size and other details will be requested later.
- **Supervisor:** the earlier PLUTO+Python result has a good overall trend, but late on the experiment fades while the simulation is still too bright.
- **Supervisor:** the model's physical credibility and interpretability must be guaranteed. **Adjusting parameters to fit the results is not allowed.** Anything resembling fabrication is to be avoided.
- **User:**
  - Agrees with all strategies in PLAN §11 (self-written open-source code, calibration/blind-prediction logic, leaving spatter/NPs/3D out of the model).
  - The workstation may be used without limit: install what is missing, and use disk and compute freely.
  - Re-plan using the earlier PLUTO results.

## 2026-11-15
- **User:** the simulation proceeds mainly along Claude's / PLAN v2 recommendations. Specifically:
  1. **Zero-fit logic accepted.** Agreement with the experiment should be **trend agreement first**: do not dwell on matching sizes. (Supervisor's view, reaffirmed.)
  2. Semi-empirical branch B (PLAN §4.5): following Claude's recommendation, the self-consistent model is the main line. Branch B is enabled only if the ignition gate fails, as a parallel variant clearly labelled "semi-empirical", and does not replace the self-consistent result.
  3. Spatter and nanoparticle dynamics are not modelled; they are listed as limitations.
  4. Historical PLUTO 72-hour parameter search controller: **user agrees to stop it** (2026-11-15). The user expects it has already exited automatically at the end of its 72-hour window. T004 step 0 checks; if it is still running, stop it. Do not delete or move data.
- Effect on PLAN: in §8, the trend metrics of brightness/evolution (P1, P2, P6, and the shape/direction of P3, P5) are the core of the judgement; absolute-size metrics are descriptive only.
- **User (2026-11-15):** the scheduled tasks S4Plume_Daily0700/Hourly/Watchdog were set up by an earlier Codex session, not by the user, and the user agrees to disable them as recommended (disable only, do not delete; reversible).

## 2026-11-15 (evening)
- **User:** the whole job must be **finished within two weeks**; deliver **a relatively optimised first result within 24 hours**; keep Codex fully busy. Fable re-planned to PLAN v3.
- **User:** "Once planning is done, have Codex start straight away and report the plan to me." → T008 and T009 (no new code needed) set to ready immediately.
- PLAN v3 route: the production hydro core is the unmodified PLUTO 4.4-patch4; all physics goes in as our own reviewable user modules (table EOS, 1064 nm absorption, NEC radiation, evaporation source); lipsim handles property tables / target model / imaging / metrics. Energy and mass are still not inputs; the historical fitted values are not used. This replaces the 11-12 decision that "self-written code is the only production code". (Implemented under the user's instruction above; the items in PLAN v3 §15 are reported to the user and can be overruled at any time.)

## 2026-11-18 08:15 (UTC+8) — stop the remaining T016 (v1b) members
User: "1，停掉吧 按你的建议来". The two remaining v1b high-fluence members (4 of 6 v1b members had aborted with the energy NaN fixed in v1d; the rest were nearly stalled by the RKL step-ratio cap) are stopped. High-fluence members will be rerun with v1d after the T016s rmax_par A/B check.

## 2026-11-18 08:20 (UTC+8) — standing authorization to stop failing or stalled computations
User: "对于类似这种某些计算跑不下去需要停掉的情况，我授予你完全权限，你可以自己确定他是否需要停止，我只需要你能给我好的计算结果就行".
Claude may decide on its own to stop workstation runs that are failing, stalled or superseded, and relays the stop to Codex in a handoff note that cites this entry. Conditions: data is never deleted; the reason, last state and stop time are recorded in results/ and STATE.md; the user is told at the next hourly report.

## 2026-11-19 14:05 (UTC+8) — stop T016e_r low_thin_G1/it0 (standing authorization)
Slow progress: ~13 ns of simulated time per hour at 515–555 ns, so ~18 h more to 800 ns. Its 16 cores are needed for T018 (phase explosion, the leading hypothesis for the high-fluence mismatch). The output up to ~555 ns is kept and used for the low-fluence grid comparison over 400–500 ns. Nothing is deleted.

## 2026-11-19 16:40 (UTC+8) — stop T018 low members, rerun with build v1i (standing authorization)
At 200 ns T018 low ran at dt ≈ 8e-4 ns (~34 ns/h; ~60 h for it0–it1). The limit was the explicit wall-exchange stability bound in the rarefied first gas cell. Build v1i integrates the same flux by backward Euler (no new parameter, same physics). The cloud A/B agrees within 0.6% on all budget terms and is 3.5× faster. The two low members are superseded and stopped. Their data (0–~210 ns) is kept as the v1h reference for the A/B at G0. High members are unaffected and continue on v1h.

## 2026-11-19 17:00 (UTC+8) — v1i accepted; T018 high members stopped and rerun as T018c (standing authorization)
T018b at G0 (low thin, 100/150/200 ns) agrees with v1h within 0.03% on E_internal, E_kinetic, M_Cu and E_src, within 1.4% on E_gas_abs and within 1.6% on E_wall. dt at 200 ns is 7.5e-2 ns against 8e-4 ns on v1h. The T018 high members (v1h, ~80 ns/h, trap wall-limited) are superseded: they are stopped and rerun on v1i (T018c), so that all phase-explosion results share one build. Data is kept.
Literature lookup (ablation mass): closed as a documented partial result (DATA_NOTES: Gojani 2008, Semerok); no further sources are needed to proceed.

## 2026-11-19 18:05 (UTC+8) — stop T016e low G0 it2 (standing authorization)
it2 was a convergence confirmation of it1 (target absorption 21.0/21.6 µJ). The target absorption is final after ~300 ns, and both members were at ~870 ns, running ~40 ns/h (~11 h left). They are stopped, the data is kept, and the partial budgets are used for the convergence check. The 16 cores go to imaging and diagnostics.

## 2026-11-20 02:30 (UTC+8) — time box on T018c high_trap_G0/it2 (standing authorization)
From 63.4 to 63.6 ns, dt fell from 4e-3 to 1.6e-4 ns, with Mach 521 and Nrkl 33. The ConsToPrim repairs sit at the wall, z = 2.5 µm, at RKL stages 33–37: the known RKL-stage overshoot defect. Codex stops trap it2 alone if t advances < 1 ns in 60 min, and thin it2 becomes the reported member (thin and trap agree within 1–5% at it0 and it1). Data is kept.

## 2026-11-20 03:00 (UTC+8) — stop T018c it2; withdraw the PE results (standing authorization)
Both it2 members stall at ~63 ns: mass piles up near the wall because the PE source exceeds the choked evaporation flux 21×. That is an inconsistency in the PE model specification, not a solver defect. The runs are stopped, the data kept, and the PE results withdrawn (STATE/PREREG). The reported high-fluence result stays T016e it2 (PE off).

## 2026-11-24 (UTC+8) — full machine for the project (user)
User: "目前只有我一个人在用工作站，直接尽可能的用满算力即可". Cards may fill all 64 physical cores (AGENTS.md; 128 logical) with MPI ranks, postprocessing using the hyperthreads, instead of the earlier ~60-core background ceiling. T021 keeps its 16 cores; T023 takes 48.

## 2026-11-24 (UTC+8) — v1k back pressure (user: "尽快能修复…热核留在壁面上的问题")
The face-pinned hot core traces to a sonic Knight source feeding a wall cell at 10–100× p_sat (T018c 62 ns extract). The pre-declared v1 item, Knight with back pressure, is implemented as v1k (PREREG v1k) and run as T023 alongside T021, which is not stopped: its droplet bound is still informative, and both runs answer separate pre-registered questions.

## 2026-11-24 15:45 (UTC+8) — requeue the T021/T023 postprocessing (standing authorization)
Codex asked for permission. T021_T023_post_03 has a fixed order that puts the trap members ahead of the 21:00 deliverables. It is stopped at a completed-run boundary and its unfinished stages are requeued: T021 high it2, T023 low-thin latest, T023 high-thin it2, then the rest. Postprocessing only; no production stopped, no data deleted.

## 2026-11-24 16:10 (UTC+8) — stop T024 (standing authorization); start T025
The v1m early fields answer its PREREG question. Onset is 1.5 ns earlier, and from 35 ns on everything is identical to v1k within 2%. The remaining iterations would only reproduce v1k, so T024 is stopped and its data kept. The 16 cores go to T025 (v1k + droplet upper bound), the combination of the two pre-declared components, after T021 high it2 met its PREREG (a)–(c).

## 2026-11-24 22:20 (UTC+8) — overnight plan (user: "持续工作，直到工作站的北京时间早上 7 点，给出规划")
Autonomous until 07:00. Hourly reports stay short. A summary with updated figures is due at 07:00.
1. Postprocessing first (priority-3 note): T026 high it1/it2, T025 low it1, then T027 iterations.
2. Readings as soon as data arrive: T026 high (does the energy-limited bound keep the high-fluence decay?), T027 PREREG v1p (b).
3. Decision rule fixed now. If T027 (b) changes σ_z/σ_x by < 5%, v1p is closed and the next pre-declared candidate, droplet kinematics (v1q: droplets launched at the measured 344 m/s, evaporating along their path), is declared, smoke-tested and run on the freed cores. If (b) changes ≥ 5%, the momentum source is first moved into a boundary-flux form (the ConsToPrim issue) before any further use.
4. Free cores also go to a G1 (fine-grid) run of the current best model, v1n (T026 cases), high it0, to show that the headline metrics (decay, S1, σ_z) are grid-converged.
5. No parameter is chosen from the comparison. Any new mechanism is pre-registered before its first result.

## 2026-11-24 23:45 (UTC+8) — v1q started in parallel with T027 (plan change)
16 cores are idle. v1q (droplets at the measured spatter top) is independent of the T027 reading, so it runs now instead of waiting for T027 (b). Pre-registered before any result.

## 2026-11-25 00:40 (UTC+8) — close v1p, stop T027 high it2 (standing authorization)
PREREG v1p (b), matched high it0 against T026 it0: σ_z/σ_x at 300 ns moves by +1.5% (sa) and +3.8% (thin), below the declared 5%. S1 is unchanged (0/20) and L730/L300 is 0.332 against 0.315. Vapour exit momentum is not the missing elongation mechanism, and it costs 1.5–15× more ConsToPrim. The remaining T027 production (high it2) is stopped and its data kept. Low it0 imaging stays as the confirmation. The freed cores go to T029 (v1q).

## 2026-11-25 01:40 (UTC+8) — T029 ahead of low-value imaging (standing authorization)
24 imaging workers held physical cores on T021 high it1, the lowest-priority item, and T029 could not start (8 cores free, 16 needed). Queue 07 is stopped at a member boundary and relaunched with ≤ 8 workers; T029 launches with 16 ranks. No production stopped, no data deleted.

## 2026-11-25 16:50 — Direct MCP channel to the workstation (user)
- The user connected two MCP servers. "WorkStaion_SuperCalculator" is the 76-core workstation: T3 project "Claude" at D:\Codex\2026-11-11\wo\outputs\Claude, Codex thread import:codex:01a1ccf8… "执行 AGENTS.md 的 T001", heartbeat automation "claude-codex". "WorkStation" is the user's desk PC, with only a scratch project.
- Use: task cards and reports stay in git as the record. The MCP thread is used for urgent instructions, status queries and stop orders (each also recorded in handoff/to_codex or DECISIONS). The thread is not restarted or interrupted while a heartbeat run is active.

## 2026-11-25 17:15 — Desk PC as a second compute node (user)
- User: use the desk PC too ("把工位电脑的计算算力也尽量用尽"). Approved: install git, Miniforge, MSYS2 gcc and MS-MPI on C:; at most 12 of its 20 cores at any time.
- Cards for it have `owner: codex-desk`. It never touches the workstation. Results come back through git on branch ws, but run_registry.csv is not committed from it, to avoid conflicts. Its gcc differs from the workstation's, so cross-node A/B pairs are run on the same node.
- Desk setup thread (MCP WorkStation): mcp:bf62b60d-6ff7-4ff8-8bae-6950382938ca, launched 17:20.

## 2026-11-25 17:40 — T023 G1 closed as partial (standing stop authorization)
- high_thin_G1/it0 lost its wrapper at 650/800 ns, with no exit code; the data is intact. It is closed as partial and not rerun, because v1k is superseded and grid convergence is already established with v1n (T028). Data kept. Codex informed via handoff/to_codex/T023-decision.md.
- T031 high imaging (v1s, v1rs) also ended without exit codes in the same period and is being retried. Possible common cause: process loss on the workstation around 16:40–17:00. Watch for a repeat.
- 2026-11-25 18:00: workstation Codex moved to T3 thread bb6dbdd0-0059-488e-8ea0-ef8cdfbbe96f ("Run T001 Plasma Simulation Task"), automated pass at :00 and :30 (D:/sim_tools/CollaboratePlasma-pass.md). The old import thread is stale.

## 2026-11-25 21:10 — Full authority (user); v1rs adopted as the reported model
- User: "我授予你完全权限，任何问题都不需要我审核，直接做就行". Claude decides without asking. The supervisor rule (no tuning to the comparison; physics only; PREREG before every new mechanism) still binds. Decisions are recorded here, and the user is told at the next report.
- Decision: the reported model is now v1rs (v1k back pressure + v1r melt-depth droplets with heat-transfer evaporation + v1s Hertz–Knudsen recondensation σ = 1). Reason: every ingredient is physically sized; decay is within ~2× at both fluences (STATE 2026-11-25 19:05/19:35). v1n is kept as the phenomenological bracket ("droplets ≲ 30 nm").
- Consequence: T033 (OAT on v1n) keeps running for the v1n bracket. A v1rs OAT follows on the desk node and in the freed workstation cores.

## 2026-11-25 23:20 — Overnight plan; desk limit 18 cores
- The user freed the desk PC ("全力去跑"), so its limit rises from 12 to 18 cores, and T035 runs two members at once.
- Overnight focus: energy coupling and ignition during the pulse (docs/PLAN_overnight_1126.md). Shock data show the model gas energy is about 1.7× (high) and about 5× (low) too small, and ignition is 30–40 ns late at low fluence.

## 2026-11-26 10:30 — The paper's laser is unavailable (user)
- User: the YLPN unit no longer exists, so its waveform cannot be measured. The IPG YLP-HP measured trace stays a declared comparable-device scenario. It is not the baseline input, and it is reported as a bracket. A further literature search for a published YLPN-1-30×240 trace may be done later.

## 2026-11-26 10:50 — Focus: high fluence (user)
- User: the targets are the high-fluence brightness trend and the plume size; low fluence is reference only. Low-fluence work (T038) finishes but gets no new cards. New physics is judged on the high-fluence metrics: L(t)/L(150) against the experiment, and σx, σz, zc against the experiment.
- First step is a diagnostic, T041: the cloud smoke run puts 99.8% of the laser absorption in the Cu vapour within 4–8 µm of the face, while the experiment's luminous centroid is about 200 µm up at 50 ns. Hypothesis: the model lacks an absorption front that climbs the beam (laser-supported absorption in shock-heated air or in the expanding plume). The production-grid deposition profile decides it.

- 2026-11-26 ~10:30 — High-fluence comparisons use the declared defocus scenario `high` (500 µm), selected by the instrument-only calibration on the 0–30 ns frames (PREREG_v0 "S4 defocus calibration"). Brightness trends at high fluence are normalised at 300 ns because the 50–160 ns experimental frames are saturated. (Claude, under full authority.)

- 2026-11-26 ~19:10 — T042 reviewed. The v1m avalanche is inactive at high fluence: z90_abs stays at 3.5–4.5 µm, below the pre-registered 20 µm, so it is not adopted. FWHM 35 ns couples 45% more gas energy but leaves σz at 97 µm (exp 151), and the independent Fig. 3d shock fit is worse (log-RMSE 0.149 at best Δ = 2 ns vs 0.085 for the 60 ns Gaussian), so it stays a sensitivity case. Conclusion: at high fluence σz is insensitive to coupled energy (+45%), pulse shape, avalanche and every registered parameter (T034). The vertical-extent gap is structural, not in the energy input. (Claude, under full authority.)

- 2026-11-26 ~19:55 — Occlusion calibration result (PREREG "Occlusion geometry"): in the experimental 0/20/30 ns frames 3–9% of the background-free signal lies below the face row, against 0% for O1 and 35–40% for O2. O1 (hard cut) is retained, and the "light below the face" seen in thr_bottom was a threshold artefact. In the same frames the experimental σz/σx rises 0.72 → 0.82 → 1.24 and the centroid rises 101 → 153 → 197 µm above the face. With O1 the model stays at 0.54–0.56 and 90–105 µm, because its unblurred band-b emission is concentrated in the first ~10 µm above the face until about 50 ns (z50 = 5 µm). The vertical gap is therefore a real early difference: the model's brightest emitter is the dense near-wall vapour layer, while the experimental emission is lifted by ~100 µm within the first 30 ns.

- 2026-11-26 ~22:05 — T040 high G0 waveform chain, self-consistent it2 (vs T039 seeded): σx 147 / σz 106 µm, aspect 0.72, L500/L300 0.41, L730/L300 0.215. The seeded T039 gave aspect 0.77 and 0.19, so the self-consistent chain moves the waveform scenario back towards v1rs (0.68, 0.22). The waveform's shape gain is mostly a seeding effect. The scenario stays a bracket. The G1 check is pending.

- 2026-11-27 ~03:35 — T044 reviewed: the v2.1 continuum fix changes the high-fluence metrics at ≥ 150 ns by < 0.3% in all five runs (σx, σz, zc, L500/L300, L730/L300). Its only effect is during the pulse (T039 emission z50 +5…13 µm, band power +20% at 30–60 ns). The pre-fix figures and the report stand for t ≥ 150 ns. The vertical-extent gap is not an imaging artefact.

- 2026-11-27 ~08:10 — T045 reviewed. v1u (Cu I–IX with NIST levels, tables v1d to 3×10⁵ K) is adopted into the reported model, as pre-registered (a completeness fix). High fluence, Δ = 28 ns, D = 500 µm, band b sa, compared with the v1c2-imaged baselines:
  - v1rs: σx/σz at 300 ns 148/102 → 140/104 µm, aspect 0.69 → 0.74; L500/L300 0.41 → 0.48; L730/L300 0.225 → 0.231; zc 162 → 171 µm; shock log-RMSE 0.086 → 0.087 (best Δ is still 28 ns).
  - Waveform: aspect 0.76 → 0.77; L730/L300 0.216 → 0.203.
  - The reported model is now v1rs + v1u (T045 high_v1rs_u). The vertical gap remains (σz 104–110 vs 151 µm).
