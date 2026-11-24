# Codex pass log (latest first)

## 2026-11-25T05:29:43+08:00 (UTC+8)
- Work card: none. Production jobs T023, T028 and T029 are running. Latest high-fluence budgets: T023 G1 it0 548.9931 ns, T028 G1 it0 298.9885 ns and T029 G0 it1 575.9961 ns.
- All three imaging jobs remain in image_v2: queue 08 on T028 low G1 it0 and the two queue 09 jobs on T029 low G0 it0/it1 (eight workers each). No new collection milestone; T025 high G0 it0 was already delivered. Inbox empty, no ready Codex card, no job launched. The queue 08 question remains open.

## 2026-11-25T05:04:36+08:00 (UTC+8)
- T029: both low G0 jobs started at 05:03:08: T029_low_it0_post_09 (PID 42200) and T029_low_it1_post_09 (PID 9008). By 05:03:37 each had passed three diagnostics and entered image_v2 with eight workers. Queue 08 (PID 41948, eight workers) continues T028 low G1 it0; T029 high G0 it0 is next. Independent output roots prevent overwrites. Images and peaks have not been handed off.
- Running production: T023 high G1 it0 544.4958 ns; T028 high G1 it0 290.9979 ns; T029 high G0 it1 265.4678 ns. No ready Codex cards. Inbox note consumed; ws 579c6af requests a decision before queue 08 revisits the two low members. T029 report sync verified.

## 2026-11-25T04:32:57+08:00 (UTC+8)
- T029 low G0 it1 reached 1300 ns, rc 0, in 3855.9 seconds. Verified the new registry row against the input hash and solver log, then rebased (35c5b84). Production evidence and handoff pushed at ef37f29; synced report hash verified. All 2576 budget rows finite; final condensed mass 3.5582687131e-12 kg, endpoint absolute relative energy residual 0.003588%. Both low-fluence production iterations are complete.
- At 04:31 UTC+8 T023 high G1 it0 was at 537.4913 ns, T028 high G1 it0 at 275.4947 ns and T029 high G0 it1 at 78.49776 ns. Queue 08 was imaging T028 low G1 it0. T029 low it1 imaging is pending. Inbox empty; no ready Codex card and no new job launched.

## 2026-11-25T04:02:41+08:00 (UTC+8)
- T029 high G0 it0 reached 800 ns, rc 0, in 6584.3 seconds. The appended registry row was verified before commit 9010869 and rebase. Production evidence and handoff pushed at 521eacd; report sync hash verified. All 1590 budget rows finite; final condensed mass 1.6746419677e-12 kg and absolute relative energy residual 0.032227%.
- At 04:01 UTC+8 T023 high G1 it0 was at 509.9843 ns; T028 high G1 it0 at 256.4936 ns; T029 high/low G0 it1 at 35.49881/1205.491 ns. Queue 08 was imaging T028 low G1 it0; T029 high G0 it0 imaging is pending. No incoming handoff, no ready Codex card, and no new job launched.

## 2026-11-25T03:30:21+08:00 (UTC+8)
- Work card: none. Pulled ws a744fcb: T025 marked done and the T028/T029 low-it0 production handoffs consumed. Incoming inbox empty; no ready Codex card.
- job.ps1 status at 03:29 UTC+8: T023, T028, T029 and queue 08 running. T023 high G1 it0 480.9935 ns; T028 high G1 it0 204.9962 ns; T029 high G0 it0 533.9487 ns and low G0 it1 262.4988 ns. Queue 08 is imaging T028 low G1 it0. Its only completed milestone, T025 high G0 it0, was already delivered. No new job started.

## 2026-11-25T03:05:34+08:00 (UTC+8)
- T025 moved to review (8498037): all five iteration sets verified. Queue 08 finished high G0 it0 at 02:49:25; six 29-frame archives, 18 metric tables, peak and three diagnostics were verified.
- T028 low G1 it0 and T029 low G0 it0 both reached 1300 ns with production rc 0. Evidence, warning counts and residual checkpoints handed off at 540ecbb and 0231264. T029 final condensed mass 3.0721847762e-12 kg. Imaging remains separate: queue 08 was imaging T028 low it0 and T029 low it0 was pending. All three reports synced and hash checked.
- At 03:03 UTC+8 T023 high G1 it0 was at 454.9927 ns, T028 high G1 it0 at 177.4920 ns, and T029 high G0 it0 at 211.4569 ns. No ready Codex card; no new job launched.

## 2026-11-25T02:34:37+08:00 (UTC+8)
- T021 moved to review at 8a6595c on ws. All five iteration artifact sets verified; final high G0 it1 delivered from queue 07 with five zero exits, six 29-frame archives, 18 metric tables and three diagnostics. Report sync hash verified.
- At 02:33 UTC+8: T023 high G1 it0 421.9953 ns; T028 high/low G1 it0 131.4795/794.4979 ns; T029 high/low G0 it0 71.49974/296.9860 ns. T025 production finished; queue 08 was imaging its high G0 it0 (eight workers). No ready Codex card and no new job started.

## 2026-11-25T02:07:07+08:00 (UTC+8)
- T029 launched at 02:05:17 (PID 40156, commit 798896d). At 02:06:02 all 16 v1q ranks were running, high/low it0 budgets 19.99964/28.79405 ns. Both actual pluto.ini files match all five requested parameters. Launch handoff pushed at 51984a6; synced report hash verified.
- Queue 07 stopped while idle after T021 high G0 it1 completed at 01:45:47. Its receipts and logs remain for next-pass collection. Queue 08 launched at 02:05:18 (PID 41948), eight imaging workers; started T025 high G0 it0. T027 was not restarted.
- T023 high G1 it0 at 398.995 ns; T028 high/low G1 it0 at 96.99817/753.999 ns (02:05 snapshot). No ready Codex card.

## 2026-11-25T01:34:11+08:00 (UTC+8)
- T025 high G0 it2: five postprocessing stages returned zero. Six native frame archives, 18 metric tables, 29 peak rows and three diagnostics verified and handed off at 543325f; report sync hash checked. Band-b self-absorbed L(730)/L(300)=0.162861; S1 0/20 in both modes. High G0 it0 has no image summary, peak or shock output; T025 remains running.
- At 01:32 UTC+8: T023 high G1 it0 390.4990 ns, T028 high/low G1 it0 78.4978/721.9946 ns. T021 and T025 production finished. Queue 07 is imaging T021 high G0 it1.
- T029 remains claimed, with no production wrapper. At 01:33, 32 solver ranks and 24 imaging workers occupied 56 of 64 physical cores; eight available versus 16 required. The existing handoff still awaits the T029 launch. The earlier T027 stop was not repeated. No ready card; no new computation started.

## 2026-11-25T01:04:12+08:00 (UTC+8)
- T027 high G0 it2 production stopped on the authorized handoff at 01:00; the verified wrapper and eight ranks exited. Last complete budget row: 162.4732 ns. Original row and budget hash pushed at 0d9f8df; raw files retained. No other job stopped.
- T027 low it0/it1 and high it1: five postprocessing stages per member verified. Four completed iterations delivered for review at 58eae9b; report sync hash checked. Low S1 dropped from 8/11 to 0/11 in both iterations. No low frame at 300 ns.
- At 01:02 T023 high G1 it0 was at 380.4898 ns; T028 high/low G1 it0 were at 66.99812/493.4531 ns. Queue 07 was imaging T025 high G0 it2. At 01:03: 32 MPI ranks and 24 image workers on 64 physical cores left eight free. T029 requires 16; do not repeat the T027 stop. The pending handoff action is to launch T029 once capacity allows. No ready card and no new job started.

## 2026-11-25T00:33:41+08:00 (UTC+8)
- T027 high G0 it0: all five stages verified; six native frame archives, 18 metric tables, 29 peak rows and three diagnostics delivered at a8ab361. Matched 300 ns aspect ratio rose 3.81% thin and 1.46% self-absorbed; both S1 counts remain 0/20. Report synced.
- At 00:29 UTC+8 the T023 high G1 it0, T027 high G0 it2, and T028 high/low G1 it0 budgets were at 365.4999, 77.49645, 57.99727 and 117.4736 ns, respectively. Queue 07 moved to T027 low G0 it0 imaging. T021 and T025 production finished; postprocessing is pending.
- No incoming handoff or ready Codex card. T029 already claimed and built; 40 solver ranks plus 24 image workers occupy the 64 physical cores; the required 16 are unavailable. No new computation started.

## 2026-11-25T00:07:41+08:00 (UTC+8)
- T026: all five production and postprocessing deliveries archived for review. T027 high G0 it1: 800 ns, rc 0; budget, checkpoints and rank evidence handed off. Both reports synced and both handoffs consumed.
- At 00:07 UTC+8: T023 high G1 it0 360.4943 ns; T027 high G0 it2 53.99785 ns; T028 high/low G1 it0 50.99949/83.99603 ns. Queue 07 imaging T027 high G0 it0. T021/T025/T026 production finished.
- T029 claimed; build and nine tests passed, both configurations verified in preview. Capacity at 00:06: 40 MPI ranks and 24 imaging workers on 64 physical cores; production awaits 16 free cores. No other ready card. ws at 8366ba7.

## 2026-11-24T23:32:28+08:00 (UTC+8)
- T026 high G0 it1: verified five successful commands, 29 times, six native frame archives, 18 metric tables, 29 peak rows and three diagnostics. Handoff pushed on ws at d07169b; synced report hash verified. Band-b P1 thin/sa 227.34284/194.63325 ns; P2 unavailable in all 18 cases. Queue 07 proceeded to T026 high G0 it2 imaging.
- Both actual T028 G1 it0 ini files matched BACKP 1, BD_ON 0, DROP_TB 2835, VMOM 0. At 23:31:09, T028 high/low budget times were 36.49773/64.49728 ns; T023 high G1 it0 and T027 high G0 it1 were at 351.9926/275.9905 ns. The T025 production wrapper exited 0; its old PID was reused by a later Python process. No process stopped or relaunched.
- Incoming handoff directory empty; no ready Codex card to claim. No new computation started.

## 2026-11-24T23:05:26+08:00 (UTC+8)
- T025 low G0 it1: five postprocessing stages rc0, 30 peak rows, 18 metric tables and both band-b native archives verified and handed off at 08747fa. The report was synced. Queue 06b stopped at the authorized complete-member boundary; queue 07 was imaging T026 high G0 it1.
- Paper lookup handed off at a705d19 with exact source locations and three native crops. T023 high G1 it0 and T027 high G0 it1 last sampled at 344.4909 and 79.99853 ns respectively.
- T028 was the only eligible card. Its lock was pushed at 95f663b. Both rendered configs passed the four parameter checks; 16 MPI ranks were active on 64 physical cores. job.ps1 started the 24-rank G1 batch at 2026-11-24T23:04:43, PID 20832. No foreground wait; runtime ini and progress are due next pass.

## 2026-11-24T22:40:16+08:00 (UTC+8)
- Retry: T025 low it0 imaging delivered (dec331d); T027 high it0 production delivered (35783d0, 800 ns). Both reports synced and remote ws later consumed both handoffs.
- At 22:39 the current T025 low it1 imaging was still running. T023 high G1 it0 last sampled 336.4925 ns, T027 high it1 44.49867 ns; eight MPI ranks each. Priority-3 successor not yet started. T028 ready/unclaimed at the check while the priority handoff is being handled. No process stopped.

## 2026-11-24T22:27:03+08:00 (UTC+8, retrospective)
- No workstation terminal at that heartbeat; no jobs or cards checked then.

## 2026-11-24T22:12:03+08:00 (UTC+8, retrospective)
- Heartbeat entry was missing. No observation is attributed to this time.

## 2026-11-24T22:02:44+08:00 (UTC+8)
- T027 low-thin G0 it1 completed at 1300 ns, rc0; production handoff pushed on ws at 8c90c98, report-only sync checked. Expectation (a) again missed: 8,593,382 versus 583,877 ConsToPrim warnings and 0.75807% versus 0.00971% endpoint closure error, matched to T026 it1. Imaging pending.
- Priority-3 note open: queue06b was already imaging T023 low-trap it1; no mid-run interruption. T023 high G1 it0 326.4874 ns; T026 high G0 it2 437.3876 ns; T027 high G0 it0 103.9976 ns. T021/T025 production already finished. No ready Codex card, no production stopped or restarted.

## 2026-11-24T21:48:02+08:00 (UTC+8)
- T023 high-trap it2 postprocessing handed off at 3125eb6: 29 peak rows, 3 diagnostics, 18 metric tables and six locally retained frame archives verified. T027 low-thin it0 production handed off at c9a170a: rc0 to 1300 ns. Expectation (a) missed against T026 low it0 (8,594,711 vs 581,952 ConsToPrim lines; closure error 0.75835% vs 0.00873%). Both reports synced and verified.
- Queue06b already entered T023 low-trap it1 imaging before the new order was received. The priority-3 note is open; T026 high it1 comes next after an authorized completed-run boundary. No process stopped. T023 G1 it0 323.4895 ns; T026 high it2 140.9712 ns; T027 high it0 78.99964 ns, low it1 257.9400 ns. No ready Codex card.

## 2026-11-24T21:28:36+08:00 (UTC+8)
- Work card: none. ws pulled to 5c1ea46. No incoming handoff, eligible ready card or new postprocessing delivery. T021 and T025 production already finished; their pending postprocessing keeps the cards open.
- job.ps1 status: T023, T026, T027 and queue06b running. Queue06b is imaging T023 high_trap_G0/it2. Latest budget times (ns): T023 high G1 it0 318.9923, T026 high G0 it2 85.99790, T027 high G0 it0 60.49956 and low G0 it0 1189.707. No job launched or stopped.

## 2026-11-24T21:14:57+08:00 (UTC+8)
- T027: previous pass claimed and launched at 21:04:08, PID 28444, commit 501c47b. Build and nine tests passed. Both it0 production ini files checked on this pass: VMOM=1, DROP_TB=2835, BACKP=1, BD_ON=0. No relaunch. Budgets: high 45.49954 ns, low 174.9559 ns.
- ws pulled to 5f28023; no incoming handoff or ready Codex card. T023 G1 it0 314.9847 ns and T026 high it2 70.49977 ns; both production wrappers running. T021 and T025 production finished as previously reported. Queue06b imaging T023 high-trap it2; its four earlier milestones already delivered. No new job started or stopped.

## 2026-11-24T21:04:08+08:00 (UTC+8; recorded on the following pass)
- T025 production completed, T026 high-it0 imaging handed off; T027 claimed, build and nine tests passed and production launched in the background. Prior pass ended on launch. No other ready card at that point.

## 2026-11-24T20:46:38+08:00 (UTC+8)
- T023: all five iterations for the 21:00 comparison verified, deadline handoff closed at 5698cec. Latest low-thin it1 has 25 original image files, three diagnostic tables and 30 peak rows; no item missing.
- T026: archived high-thin it1 production at 800 ns (rc 0) at c9bcad3; it1 imaging pending. Initial ws pull was blocked by its new validated registry append; committed it, rebased and pushed. Both handoffs and report-only syncs verified.
- T023/T025/T026 production and queue06b running; queue imaging T026 high it0. T021 production finished, high it1 postprocessing pending. Latest budgets: T023 high G1 it0 296.9806 ns; T025 high G0 it2 451.8995 ns; T026 high G0 it2 40.99804 ns. No ready Codex card and no job started or stopped.

## 2026-11-24T20:28:54+08:00 (UTC+8)
- Work card: none. ws at f64f63f. No ready Codex card. The 21:00 handoff remains open for T023 low_thin_G0/it1 imaging and peak extraction. Queue06b is in image_v2; its three diagnostics returned zero, with no new completed milestone to collect.
- job.ps1 status: T023, T025, T026 and queue06b running. Active budgets (ns): T023 high_thin_G1/it0 277.9973; T025 high_thin_G0/it2 102.9982; T026 high_thin_G0/it1 416.9510. T021 production finished successfully; its card still awaits high_thin_G0/it1 postprocessing. No jobs started or stopped.

## 2026-11-24T20:15:29+08:00 (UTC+8)
- T025 high_thin_G0/it1 postprocessing delivered on ws at 85f8dec. All five stages returned zero; full image_v2 with the two native band-b frame archives, three diagnostic tables and 29 peak rows were verified. All 18 missing P2 values retained. Report-only sync hash verified.
- T023/T025/T026 production and queue06b running at job.ps1 check. Queue: running, T023/low_thin_G0/it1, stage image_v2. The 21:00 note is still pending for the completed T023 low-thin it1 production run's image and peak. No new job started or stopped.
- Active budgets: T023/high_thin_G1/it0 2.624794e+002 ns; T025/high_thin_G0/it2 8.049631e+001 ns; T026/high_thin_G0/it1 1.069989e+002 ns. No ready Codex card.

## 2026-11-24T19:58:16+08:00 (UTC+8)
- Work card: none. Pulled ws to ae025b0; the T026 handoff was consumed. No ready Codex card and no newly completed postprocessing milestone to collect. The 21:00 deadline note remains open for T023 low_thin_G0/it1.
- job.ps1 status: T023, T025, T026 and T023_T025_T026_post_06b running. Queue: running, T025/high_thin_G0/it1, stage image_v2. T026 low-thin it0 was already delivered at 16b3259. No job launched or stopped.
- Production budget times: T023/high_thin_G1/it0 2.474951e+002 ns; T025/high_thin_G0/it2 6.099896e+001 ns; T026/high_thin_G0/it1 7.399647e+001 ns.

## 2026-11-24T19:47:14+08:00 (UTC+8)
- T026 low_thin_G0/it0 postprocessing delivered on ws at 16b3259: five successful commands, full image_v2 including native band-b thin/sa, three diagnostics and 30 peak rows. All 18 unavailable P2 values preserved. Report-only sync hash verified.
- Priority queue06b: running, T025/high_thin_G0/it1, stage image_v2. T025 high-thin it1 diagnostics returned zero. No new launch or stop. Deadline note remains open for the T023 low-thin it1 images and peak.
- T023/T025/T026 production running at check. Latest active budgets: T023/high_thin_G1/it0 2.409912e+002 ns; T025/high_thin_G0/it2 4.799756e+001 ns; T026/high_thin_G0/it1 5.999851e+001 ns. No ready Codex card.

## 2026-11-24T19:37:23+08:00 (UTC+8)
- T023/T025/T026 handoffs pushed on ws at 8a6804e. Production completed: T023 low-trap it1 and low-thin it1 at 1300 ns, T025 high-thin it1 and T026 high-thin it0 at 800 ns. Original completion evidence, energy checkpoints, rank warnings and reports delivered. T026 low-thin it1 production and T023 low-trap it0 postprocessing were archived at 9a3eeca.
- Queue05 yielded after all five low-trap it0 stages returned zero. Priority queue06b: running, member T026/low_thin_G0/it0, stage image_v2. T026 low-it0 diagnostics returned zero. No new solver or image job launched. The 21:00 deadline remains active; pending images and peaks have not been reported as complete.
- Production wrappers T023/T025/T026 running at check. Active budgets: T023/high_thin_G1/it0 2.354899e+002 ns; T025/high_thin_G0/it2 3.449937e+001 ns; T026/high_thin_G0/it1 4.749830e+001 ns. No ready Codex card. Only the completed priority note was archived.

## 2026-11-24T19:04:13+08:00 (UTC+8)
- T026/T025/T023: priority runner pushed on ws at 54c4ef0. Pinned preflight and post-300 ns growth checks passed for T026 low it0 and T025 high it0; latest finished T025 iteration will be selected again at admission.
- job.ps1: successor T023_T025_T026_post_06b PID 14288, waiting_for_queue05_boundary, group 1 with 24 secondary SMT lanes. Queue05 PID 32784 is imaging T023 low_trap_G0/it0. No production stop or new image delivery. Initial post_06 exited 1 before computation due to a processor-group mismatch; original logs preserved.
- Next: T026 low it0, T025 high latest completed, T023 low thin it1 and T026 high it0 when finished, then lower-priority members. Both priority and 21:00 notes remain open until cutover/delivery. No ready Codex card.
- Production jobs T023/T025/T026 running; budget times (ns): T023/high_thin_G1/it0 2.269885e+002; T023/low_thin_G0/it1 8.844850e+002; T023/low_trap_G0/it1 9.474851e+002; T025/high_thin_G0/it1 1.094971e+002; T026/high_thin_G0/it0 2.119681e+002; T026/low_thin_G0/it1 1.121264e+003.

## 2026-11-24T18:45:40+08:00 (UTC+8)
- T026 low_thin_G0/it0 production completed rc0 at 1300 ns (2541.1 s, 2576 finite budget rows). Final residual +2.10613e-9 J, final condensed mass 9.94272e-13 kg, 581952 ConsToPrim warnings. Maximum post-300 ns growth 1.47998e-14 J is below 2e-7 J. Checkpoints, mass history, rank evidence and report delivered. T026 low-it1 source and actual ini verified; low-it0 image, diagnostics and peak await the shared postprocessing allocation.
- T023 high_trap_G0/it1 postprocessing delivered (18 metric tables, 29 peak rows, original missing P2 retained). Verified six frame archive hashes; original NPZs remain local. Also delivered low_trap_G0/it0 diagnostics: 131 core, 521 shock and 12 emission rows. Its image stage is running. Queue05: 25 commands complete, five milestones delivered. The 21:00 note remains active for latest low-thin; it0 is delivered and it1 is still running.
- Verified and committed the single live registry append before retrying pull. Pushed both handoffs and reports at ws 9a1dcbc; 47 original/archive/index evidence files verified, plus six local frame archives. Updated T023/T026 report-only sync copies. No ready card, so none skipped. No new job launched or stopped.
- job.ps1: T023, T025, T026 and T021_T023_post_05 running. Active production budgets at 18:45 UTC+8 (ns): T023 high_thin_G1/it0 223.4915, low_thin_G0/it1 845.9884, low_trap_G0/it1 878.4958; T025 high_thin_G0/it1 82.49671; T026 high_thin_G0/it0 95.49652, low_thin_G0/it1 85.49641.

## 2026-11-24T18:27:44+08:00 (UTC+8)
- Work card: none. ws is up to date at aa9641d. No ready card or new completion to hand off. The 21:00 comparison instruction remains open for T023 low_thin_G0/it1; it0 remains the latest completed and delivered low-thin iteration.
- job.ps1 status: T023, T025, T026 and T021_T023_post_05 are running. The image queue is processing T023 high_trap_G0/it1; its 20 completed commands and four previously delivered milestones are unchanged.
- Active budgets at 18:27 UTC+8 (ns): T023 high_thin_G1/it0 216.9948, low_thin_G0/it1 815.4865, low_trap_G0/it1 845.4823; T025 high_thin_G0/it1 65.99725; T026 high_thin_G0/it0 69.49816 and low_thin_G0/it0 1186.980. All six advanced since the previous check. No job started or stopped.
