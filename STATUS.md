# Codex pass log (latest first)

## 2026-11-25T14:03:23+08:00 (UTC+8)
- T030 high_a100n/it2 reached 800 ns at 14:00:28, rc 0. All 1590 budget rows were finite. Of 6.2942251010 ng launched, 1.2090838567 ng evaporated and 5.0851412443 ng remained airborne. Endpoint residual -2.50968954e-8 J (0.032439%); 2363943 ConsToPrim lines. Production evidence and original droplets.csv handed off at 628aee3; synced report verified.
- High RP imaging was running; low RP production advanced to 883.4920 ns at 14:01, and T023 high G1 it0 reached 631.9950 ns at 13:59. Started T030_high_a30n_post at 14:03:02, wrapper PID 44548, eight workers and NUMA node 0 preferred. Low/high 100 nm imaging remains pending. No incoming handoff or ready Codex card.

## 2026-11-25T13:36:09+08:00 (UTC+8)
- T030: replied to the RP progress request at 7247b77. High RP finished at 800 ns, solver elapsed 3:23:25; low RP was running at 806.9862 ns at 13:31:43 and advanced to 814.9990 ns at 13:35:47. No process stopped.
- At 8cd7fdc handed off three production members (high RP and high 30 nm at 800 ns; low 100 nm at 1300 ns) and high 10 nm imaging. All three original droplets files matched staged evidence; verified 30 imaging files, 29 band-b frames per mode, 18 metrics tables, diagnostics and peak lift. Band-b baseline P1 thin/SA 564.97/494.64 ns; all P2 unavailable. Report sync verified.
- Started T030_high_rp_post at 2026-11-25T13:36:09 with eight workers (wrapper PID 22208, commit 8cd7fdc); the two completed scan members await imaging. T030 high 100 nm at 580.4776 ns and T023 high G1 it0 at 628.9967 ns sampled at 13:33. No ready Codex card or outstanding incoming handoff.

## 2026-11-25T13:01:04+08:00 (UTC+8)
- T030 low_a30n imaging completed at 12:55:55. All five stages exited 0. Verified 30 archived files, both band-b stacks at 30 experimental times, 18 metric tables, peak lift and three diagnostics. Baseline band-b P1 thin/SA: 258.40/232.42 ns; P2: 637.88/556.35 ns. Imaging handoff pushed at 8f71c96; report sync verified.
- T023 and T030 production still running. Latest budget times at 12:59 (ns): T023 high G1 it0 625.9979; T030 high RP/30/100 nm 791.9936 / 733.4824 / 168.4873; low RP/100 nm 738.4854 / 681.9764. High 10 nm imaging still running. Inbox empty; no ready Codex card or new production completion. No job started.

## 2026-11-25T12:33:02+08:00 (UTC+8)
- T030 high_a10n it2 finished at 800 ns, rc 0. All 1590 budget rows finite. Droplets: 6.2942251010 ng launched, 3.5104148106 ng evaporated, 2.7838102904 ng airborne. Endpoint energy residual -1.09340603e-7 J (0.132226%); 3484894 ConsToPrim lines. Production handoff pushed at 9a982a2; full droplets.csv and synced report verified.
- Started T030_high_a10n_post at 2026-11-25T12:33:02, wrapper PID 39780, eight imaging workers, commit 9a982a2, NUMA node 1 preferred. T030_low_a30n_post still running image_v2. No ready Codex card or incoming handoff; neither RP member has completed production.
- Latest budgets sampled at 12:31 (ns): T023 high G1 it0 622.9937; T030 high RP/30/100 nm 755.4990 / 674.4718 / 70.99924; low RP/100 nm 697.9920 / 529.9809. Both production wrappers running.

## 2026-11-25T12:04:54+08:00 (UTC+8)
- T030 low 10 nm imaging completed (five zero exits); 30 archived products verified. Band-b baseline thin/self-absorbed P1 296.06/295.41 ns, P2 893.83/833.06 ns. Low 30 nm production completed at 1300 ns (rc 0), 1.4814777421 ng evaporated, 3.7115445729 ng airborne; 1425510 ConsToPrim lines. Separate handoffs pushed at e8e928f; synced report verified. All eight production inputs now verified.
- Started T030_low_a30n_post at 2026-11-25T12:04:54, wrapper PID 25692, eight workers, commit e8e928f. No incoming handoff or ready Codex card. Both RP members still in production.
- Budgets near 12:01 UTC+8 (ns): T023 high G1 it0 618.9955; T030 high RP/10/30/100 nm 694.4780 / 786.4738 / 606.4771 / 27.48391; low RP/100 nm 661.4853 / 478.9872. Both production wrappers running.

## 2026-11-25T11:30:32+08:00 (UTC+8)
- Work card: none. T023 high G1 it0 advanced to 615.9950 ns. T030 high RP/10/30 nm: 609.4813 / 732.4925 / 557.9885 ns; low RP/30/100 nm: 618.4809 / 873.4871 / 315.4333 ns. Both production wrappers are running; high 100 nm is queued.
- T030 low 10 nm production handoff received upstream. Its separate job T030_low_a10n_post is running image_v2. No new completion, incoming handoff or ready Codex card. No job started.

## 2026-11-25T11:03:55+08:00 (UTC+8)
- T030 low_a10n it1 production finished at 1300 ns (rc 0, 2576 finite budget rows). Launched/evaporated/airborne: 5.1930223150 / 2.1110136286 / 3.0820086864 ng. Endpoint residual -2.889637e-10 J; 698261 ConsToPrim lines. Full droplets.csv and production evidence handed off at be55f3f; report sync verified.
- Started T030_low_a10n_post at 2026-11-25T11:03:55 (wrapper PID 28116, eight image workers, commit be55f3f); imaging deliverables pending. Only this scan had completed production. The low 100 nm input passed all required parameter checks; high 100 nm still queued.
- Production still running. Latest budgets at 11:02 (ns): T023 high G1 it0 611.4934; T030 high RP/10/30 nm 375.4561 / 496.4146 / 500.9940; low RP/30/100 nm 570.4233 / 627.4762 / 65.99968. Inbox empty; no ready Codex cards.

## 2026-11-25T10:29:52+08:00 (UTC+8)
- Work card: none. T023 high G1 it0 reached 607.9993 ns. T030 high RP/10/30 nm reached 94.49997 / 97.49879 / 99.49571 ns; low RP/10/30 nm reached 341.4757 / 1056.986 / 542.4950 ns. Both wrappers running; the two 100 nm members are queued.
- No new production completion, incoming handoff or ready Codex card. No job started.

## 2026-11-25T10:01:59+08:00 (UTC+8)
- T030: verified the six generated production inputs, including both melt-depth radii and the 10/30 nm scans. Report pushed to ws at 728f7d6. The two 100 nm inputs have not been generated. No T030 completion row or new member handoff.
- T030 latest budgets at 10:00 UTC+8 (ns): high rp/10/30 nm 54.9974 each; low rp/10/30 nm 115.4461 / 118.4905 / 128.4762. T023 high G1 it0 604.9963 ns. Both job wrappers running. Inbox empty; no ready Codex card. No job launched.

## 2026-11-25T09:43:19+08:00 (UTC+8)
- T028 high G1 it0 imaging delivered with 34 verified files, completing both members. Grid comparison submitted at 5cef2af; T028 was subsequently marked done by Claude. T029 high G0 it2 reached 800 ns, then finished imaging at 09:40:25. All five T029 production and imaging deliveries verified; final member handed off at 84d4300; queue 10 exited 0.
- T030: v1r built, 15 tests passed, four T026 seed hashes verified, and paper size notes delivered. After release and claim, job.ps1 launched production at 09:41:59 with --max-cores 48 (wrapper PID 5272, commit 84d4300). Running at 09:42:16 with six member directories; generated ini checks pending. Launch handoff pushed at 3644335. T023 high G1 it0 budget: 602.9986 ns. No further ready Codex card identified; no foreground wait.

## 2026-11-25T08:59:50+08:00 (UTC+8)
- Work card: none. T023 high G1 it0 at 597.9961 ns and T029 high G0 it2 at 764.9889 ns. Both production wrappers are running; T028 production has finished.
- Queue 10 is still imaging T028 high G1 it0. Its T029 high G0 it1 milestone was already delivered. Inbox empty, no ready Codex card and no new job started.

## 2026-11-25T08:29:55+08:00 (UTC+8)
- Work card: none. T023 high G1 it0 reached 595.4989 ns and T029 high G0 it2 reached 449.97 ns; both production jobs are running. T028 production finished previously.
- Queue 10 remains in image_v2 for T028 high G1 it0; its only completed milestone, T029 high G0 it1, was already delivered. Incoming handoff directory empty. No ready Codex card, new delivery or job launch.

## 2026-11-25T08:01:15+08:00 (UTC+8)
- T029 high G0 it1 finished imaging at 07:44:09 with five zero exits. Verified six 29-frame archives, 18 metric tables, 29 peak rows and three diagnostics. All 34 archived files matched their Git blob hashes. Result handoff pushed at 20fb4c4; synced report verified. Band-b baseline P1 thin / self-absorbed was 599.98 / 522.97 ns; P2 unavailable in all 18 scenarios.
- Queue 10 is imaging T028 high G1 it0 (its production wrapper finished). Running production budgets: T023 high G1 it0 591.9896 ns; T029 high G0 it2 129.4704 ns. Inbox empty and no ready Codex card. No new job started.

## 2026-11-25T07:31:08+08:00 (UTC+8)
- T029 high G0 it0 imaging finished at 07:14:18: five zero exits, six 29-frame archives, 18 metrics tables, 29 peak rows and three diagnostics. Verified 34 archived files against their SHA-256 receipts; result handoff pushed as d294fd6 and report sync verified. Band-b baseline P1 (thin / self-absorbed): 396.28 / 369.16 ns. P2 unavailable in all 18 scenarios.
- Queue 08 retired its idle coordinator PID 41384 at 07:14:18 after the high-it0 milestone. Wrapper exit 15 was the authorized retirement. Queue 10 is imaging T029 high G0 it1; no low members repeated. Inbox note consumed. Production: T023 high G1 it0 587.9947 ns and T029 high G0 it2 72.99895 ns. T028 production finished and its high imaging is pending. No ready card or new job.

## 2026-11-25T07:02:08+08:00 (UTC+8)
- T029 high G0 it1 and T028 high G1 it0 both completed at 800 ns, rc 0 (1,590 and 1,602 finite budget rows). The initial pull encountered the live registry append. Verified both new rows, input hashes and rank-0 Done markers, committed and rebased. Pushed separate production handoffs and reports at 75d03f7; 26 archived blobs and both synced reports were hash-verified. Absolute relative energy residuals: T029 0.269375%, T028 0.069599%.
- T028 production wrapper finished; high G1 imaging remains outstanding. At 07:01 queue 08 was imaging T029 high G0 it0. Queue 10 was still waiting for the authorized idle boundary; retain its inbox note until transition is confirmed. T023 high G1 it0 at 583.4959 ns, T029 high G0 it2 at 36.9998 ns. No ready Codex card; no new job launched.

## 2026-11-25T06:34:25+08:00 (UTC+8)
- T028 low G1 it0 (06:11:51) and T029 low G0 it0/it1 (06:27:04 / 06:28:21) completed imaging. All five stages per member returned zero. Three separate handoffs and 102 source/index hash-verified files pushed to ws at 7fd9fd8; T028/T029 report sync verified. Each member has six 30-frame archives, 18 metric tables, 30 peak rows, and core/shock/emission (131/521/12 rows).
- Queue 08 is now imaging T029 high G0 it0. Authorized successor T029_T028_post_10 (wrapper PID 32484, commit 4f89088) started at 06:33:56 and is waiting for its five-stage, child-free idle boundary. It excludes both delivered low members; check its transition before consuming T029-queue-answer.md. Nothing was stopped this pass.
- Production high G1 it0: T023 579.4863 ns, T028 601.4695 ns; T029 high G0 it1 759.4832 ns. No ready Codex card.

## 2026-11-25T05:59:53+08:00 (UTC+8)
- Work card: none. At 05:59, T023 high G1 it0 reached 569.4801 ns, T028 high G1 it0 reached 342.9556 ns and T029 high G0 it1 reached 666.4667 ns. All advanced since 05:29.
- job.ps1 reports all six jobs running. T028 low G1 it0 and T029 low G0 it0/it1 remain in image_v2, with eight live image workers each. No new collection milestone. Inbox empty; no ready Codex card. No job started or stopped. Previous queue 08 handoff is awaiting a reply.

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
