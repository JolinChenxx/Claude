# Codex pass log (latest first)

## 2026-11-27T00:30:51+08:00 (UTC+8)
- No card claimed. The inbox was empty at ws cd55086e. The only ready card, T038, belongs to codex-desk.
- T040 high G1 it0 (PID 39188) advanced to +28 ns imaging after nominal imaging and diagnostics. Elapsed time was 4 h 57 min.
- All seven T044 jobs were running after 58 min. These cover five re-imaging runs and the T031 and T039 emission diagnostics. All eight job stderr logs were empty at inspection.

## 2026-11-27T00:00:55+08:00 (UTC+8)
- Card claimed: none. Synced ws at cd55086e. The inbox is empty. T038 is the only ready card and belongs to codex-desk.
- T040 high G1 it0 (PID 39188) is rendering nominal images after 4 h 27 min. All seven T044 jobs are running after 28 min: imaging for T026, T031, T039, T040 and T042, and emission diagnostics for T031 and T039. All eight stderr logs were empty at inspection.

## 2026-11-26T23:32:36+08:00 (UTC+8)
- T044 table build exited 0 in 792.8 s. Verified matching v1c and v1c2 hashes for eos.bin and opt.bin. Read nion_z4 and nion_z5 from pop.bin. Pushed the table report to ws at cd55086e.
- Started paired nominal and +28 ns imaging with peak lift for T031, T039, T040, T042 and T026 (eight workers per run), and one emission diagnostic each for T031 and T039. All seven jobs use the source snapshot at a66bc00e and v1c2 tables.
- T040 high G1 it0 (PID 39188) was rendering nominal images after 3 h 57 min. The inbox was empty. T038 is assigned to codex-desk.

## 2026-11-26T23:01:54+08:00 (UTC+8)
- Claimed T044 at ws 22b9680a. Started T044_tables (PID 45016) to build v1c2 with the original v1c config and 32 workers. Verify eos.bin and opt.bin against v1c and check pop.bin for nion_z4 and nion_z5 before re-imaging.
- T040 high G1 it0 (PID 39188) was rendering nominal images after 3 h 27 min. The inbox was empty. The remaining ready card T038 belongs to codex-desk.

## 2026-11-26T22:31:52+08:00 (UTC+8)
- Submitted T043 for review at ws 78531af9. All three diagnostics exited 0. Verified all 81 indexed snapshots per run through 200 ns and all 18 selected report rows. Synced the report and verified its hash.
- T040 high G1 it0 (PID 39188) was running nominal imaging after 2 h 57 min. The inbox was empty. T038 is the only ready card and belongs to codex-desk.

## 2026-11-26T22:02:35+08:00 (UTC+8)
- T040 high G0 it2 delivered at ws f95bbab9. All seven stages exited 0. Verified 47 file hashes and 29 experimental times per offset. Synced the report and verified its hash. High G1 it0 (PID 39188) was rendering nominal images after 2 h 27 min with eight active worker processes.
- T043 diagnostics on T031, T039 and T026 were running after 29 min, each with a live Python worker. The inbox was empty. The only ready card T038 belongs to codex-desk.

## 2026-11-26T21:32:05+08:00 (UTC+8)
- Claimed T043 on ws at 36cfa7ab and launched three single-core emission-height diagnostics in parallel through job.ps1. T031 high_v1rs it2 has PID 50572, T039 high_wf it2 has PID 27012, and T026 high_thin_G0 it2 has PID 31348. CSV coverage and report tables are due after completion.
- T040 high G0 it2 (PID 40120, 2 h 28 min) was rendering the +28 ns images. High G1 it0 (PID 39188, 1 h 57 min) was rendering nominal images. The inbox was empty. T038 is assigned to codex-desk.

## 2026-11-26T21:01:17+08:00 (UTC+8)
- Card claimed: none. The inbox is empty at ws c3f1ffbc. T038 is the only ready card and belongs to codex-desk.
- T040 high G0 it2 (PID 40120, 1 h 58 min) is rendering image_v2_t28. Its nominal output directory contains 25 files. High G1 it0 (PID 39188, 1 h 27 min) is rendering image_v2. Both active stage output directories contain zero files at this check. Both jobs report running with empty stderr logs.

## 2026-11-26T20:30:52+08:00 (UTC+8)
- Card claimed: none. The inbox is empty at ws c3f1ffbc. The only ready card, T038, belongs to codex-desk.
- T040 high G0 it2 (PID 40120) advanced to image_v2_t28 after the nominal rendering and diagnostics. High G1 it0 (PID 39188) continues nominal image_v2. Elapsed times were 1 h 28 min and 57 min respectively.

## 2026-11-26T20:01:37+08:00 (UTC+8)
- Card claimed: none. Synced ws to c3f1ffbc. T034 review is complete. The inbox is empty. T038 is the only ready card and belongs to codex-desk.
- T040_high_wf_chain_it2_post (PID 40120, about 58 min) and T040_high_wf_G1_it0_post (PID 39188, about 27 min) are rendering nominal image_v2 frames. Both solver runs reached 800 ns.

## 2026-11-26T19:33:28+08:00 (UTC+8)
- T034 submitted for review on ws 9e660295. All eight solves and 40 postprocessing stages exited 0. Verified 224 archived products and 16 band-b frame hashes. All 144 P2 estimates were unavailable. The report includes comparisons against the same-seed T031 baseline.
- T040 production finished with all six solves successful. The three high G0 iterations and high G1 it0 reached 800 ns, and both low G0 iterations reached 1300 ns. Delivered high G0 it1 imaging for both time offsets and verified all 47 archived files. Synced and hash-checked the T034 and T040 reports.
- Started high G1 it0 imaging and diagnostics. T040_high_wf_G1_it0_post STATE=running  elapsed=0.00:00:00  pid=39188  commit=9e660295. High G0 it2 is rendering nominal images. The only ready card T038 belongs to codex-desk. Inbox empty.

## 2026-11-26T19:02:38+08:00 (UTC+8)
- T042 submitted for review on ws 529bd7ee. All three members reached 800 ns. All 21 postprocessing stages passed and 141 archived files matched their source hashes. The final report includes the tau35 images and shock comparison over 21 observations within the simulated time span. Final report synced and hash-verified.
- Started T040 high G0 it2 imaging and diagnostics. T040_high_wf_chain_it2_post STATE=running  elapsed=0.00:00:00  pid=40120  commit=529bd7ee. T040 high G0 it1 was rendering its +28 ns images. Both remaining T034 members were rendering nominal images.
- T040_retry01 solver times are high_wf_chain/it0 8.000000e+002 ns, high_wf_chain/it1 8.000000e+002 ns, high_wf_chain/it2 8.000000e+002 ns, high_wf_G1/it0 7.809641e+002 ns, low_wf_chain/it0 1.300000e+003 ns, low_wf_chain/it1 1.300000e+003 ns. Inbox empty. The only ready card T038 belongs to codex-desk.

## 2026-11-26T18:47:12+08:00 (UTC+8)
- Retried the pass and synchronized ws. Delivered T042 high_bd and high_bdwf. Each passed seven stages, covered 29 frame times at both offsets and had 47 archived files verified. The updated report was synced and hash-checked. High_tau35 was still rendering its +28 ns images at inspection.
- T040 high G0 it2 reached 800 ns with solver exit code 0. Committed its registry row and passed preflight for imaging. Queued it after the T042 report per the 18:10 priority change. High G0 it1 was rendering +28 ns images. T034 high_sig_hi and high_gaunt_hi were rendering nominal images.
- T040_retry01 solver times are high_wf_chain/it0 8.000000e+002 ns, high_wf_chain/it1 8.000000e+002 ns, high_wf_chain/it2 8.000000e+002 ns, high_wf_G1/it0 7.024669e+002 ns, low_wf_chain/it0 1.300000e+003 ns, low_wf_chain/it1 1.300000e+003 ns. ws at 3973aa07. Inbox empty. The only ready card T038 belongs to codex-desk.

## 2026-11-26T18:03:39+08:00 (UTC+8)
- Submitted T033 for review on ws 3236d597 after delivering low_gaunt_hi/it1. All 18 members reached their target times. All 90 stages exited 0. Verified 510 archived products and the frame hashes for all 18 members. P2 was unavailable for 300 of 324 scenarios. Synced the final report and verified its hash.
- Assigned the eight workers freed on node 0 to T034 high_gaunt_hi/it2. T034_high_gaunt_hi_post STATE=running  elapsed=0.00:00:00  pid=39372  commit=3236d597. T034 high_sig_hi continues nominal imaging on node 1. T040 high G0 it1 and all three T042 members are rendering +28 ns images.
- T040_retry01 solver budget times are high_wf_chain/it0 8.000000e+002 ns, high_wf_chain/it1 8.000000e+002 ns, high_wf_chain/it2 6.704982e+002 ns, high_wf_G1/it0 5.664740e+002 ns, low_wf_chain/it0 1.300000e+003 ns, low_wf_chain/it1 1.300000e+003 ns. Inbox empty. The only ready card T038 belongs to codex-desk.

## 2026-11-26T17:32:45+08:00 (UTC+8)
- ws sync recovered. Delivered T034 high_sig_lo/it2 (6/8 members, all 18 P2 estimates unavailable) and T040 high G0 it0 (all seven stages passed, 47 archived products hash-verified). Both reports synced. ws at 81902f22.
- Started T034 high_sig_hi/it2 on node 1. T034_high_sig_hi_post STATE=running  elapsed=0.00:00:00  pid=49856  commit=81902f22. T033 low_gaunt_hi and T040 high G0 it1 continue nominal imaging. All three T042 members are rendering the +28 ns images.
- T040_retry01 solver budget times are high_wf_chain/it0 8.000000e+002 ns, high_wf_chain/it1 8.000000e+002 ns, high_wf_chain/it2 4.244787e+002 ns, high_wf_G1/it0 5.089764e+002 ns, low_wf_chain/it0 1.300000e+003 ns, low_wf_chain/it1 1.300000e+003 ns. Inbox empty. The only ready card T038 belongs to codex-desk.

## 2026-11-26T17:01:10+08:00 (UTC+8)
- Card: none. The ws pull from origin failed during the Schannel TLS handshake. Local ws was one commit behind the cached origin/ws. The pass stopped before card selection and job inspection. Current job progress is unverified.

## 2026-11-26T16:33:27+08:00 (UTC+8)
- Handed off T033 low_sig_hi/it1 (17/18 members) and T040 low G0 it1 (all seven stages passed, 47 archived products hash-verified). Both reports synced. ws at 293f5a57.
- Started T033 low_gaunt_hi/it1 on node 0. T033_low_gaunt_hi_post STATE=running  elapsed=0.00:00:00  pid=52152  commit=293f5a57. T034 high_sig_hi passed preflight and is queued behind high_sig_lo on the same eight node 1 lanes.
- T040 high it0 continues +28 ns imaging and high it1 continues nominal imaging. T042 all three nominal images are running. T040_retry01 budgets are high_wf_chain/it0 8.000000e+002 ns, high_wf_chain/it1 8.000000e+002 ns, high_wf_chain/it2 7.999856e+001 ns, high_wf_G1/it0 4.109866e+002 ns, low_wf_chain/it0 1.300000e+003 ns, low_wf_chain/it1 1.300000e+003 ns. Inbox empty. The only ready card T038 belongs to codex-desk.

## 2026-11-26T16:01:18+08:00 (UTC+8)
- None to claim or collect. ws is synchronized at d6086461 and the inbox is empty. The only ready card, T038, belongs to codex-desk.
- Checked nine active jobs. T040 high G0 it1 is rendering nominal images. High G0 it0 and low G0 it1 are rendering their +28 ns images. T033 low_sig_hi, T034 high_sig_lo and all three T042 members are rendering nominal images.
- T040_retry01 continues solving. Latest recorded times are high_wf_chain/it0 8.000000e+002 ns, high_wf_chain/it1 8.000000e+002 ns, high_wf_chain/it2 5.199868e+001 ns, high_wf_G1/it0 3.699846e+002 ns, low_wf_chain/it0 1.300000e+003 ns, low_wf_chain/it1 1.300000e+003 ns.

## 2026-11-26T15:32:36+08:00 (UTC+8)
- T040 high G0 it1 reached 800 ns and exited 0. Verified and pushed its registry entry. High G0 it2 passed the pulse and conductivity input checks. The 0.317971% energy shortfall meets the approved tail criterion. The ws report is at d6086461.
- Started nominal and +28 ns imaging with diagnostics for high G0 it1. T040_high_wf_chain_it1_post STATE=running  elapsed=0.00:00:00  pid=50696  commit=d6086461. The high G0 it0 and low G0 it1 jobs are rendering their +28 ns images. T033 low_sig_hi, T034 high_sig_lo and all three T042 members are rendering nominal images.
- T040_retry01 continues production. Latest solver times are high_wf_chain/it0 8.000000e+002 ns, high_wf_chain/it1 8.000000e+002 ns, high_wf_chain/it2 2.799996e+001 ns, high_wf_G1/it0 3.299893e+002 ns, low_wf_chain/it0 1.300000e+003 ns, low_wf_chain/it1 1.300000e+003 ns. Inbox empty. The only ready card T038 belongs to codex-desk.

## 2026-11-26T15:00:56+08:00 (UTC+8)
- No workstation card to claim or completed job to collect. ws synchronized at c6fef67b. The inbox is empty. T038 belongs to codex-desk.
- Checked eight running jobs. T040 high G0 it0 and low G0 it1 are rendering their +28 ns images. T033 low_sig_hi, T034 high_sig_lo and all three T042 members are rendering their nominal images. T033 has delivered 16/18 members and T034 has delivered 5/8.
- T040_retry01 continues production. Latest solver times are high_wf_chain/it0 8.000000e+002 ns, high_wf_chain/it1 6.805000e+002 ns, high_wf_G1/it0 2.814803e+002 ns, low_wf_chain/it0 1.300000e+003 ns, low_wf_chain/it1 1.300000e+003 ns.

## 2026-11-26T14:34:45+08:00 (UTC+8)
- T034 high_cond_lo/it2 delivered (5/8). All five stages passed. All 18 P2 estimates are missing. T040 low G0 it0 delivered with all seven stages done and both time conventions verified. Both reports synced with SHA-256 checks. T033 stands at 16/18.
- T042 production finished. Three registry rows verified, all rc 0 at 800 ns. Archived budgets, inputs and deposition CSVs. The report contains ignition, gas absorption, half-peak z90 range and integrated Cu fraction. Imaging is pending. ws at 66dd1b9d.
- Started five imaging jobs. T042_high_bd_it2_post STATE=running  elapsed=0.00:00:01  pid=21844  commit=66dd1b9d. T042_high_bdwf_it2_post STATE=running  elapsed=0.00:00:01  pid=18232  commit=66dd1b9d. T042_high_tau35_it2_post STATE=running  elapsed=0.00:00:01  pid=46112  commit=66dd1b9d. T033_low_sig_hi_post STATE=running  elapsed=0.00:00:01  pid=45372  commit=66dd1b9d. T034_high_sig_lo_post STATE=running  elapsed=0.00:00:00  pid=13928  commit=66dd1b9d. T040 G0 high it0 and low it1 imaging also continue. T040_retry01 solver budgets: high_wf_chain/it0 8.000000e+002 ns, high_wf_chain/it1 4.734937e+002 ns, high_wf_G1/it0 2.429852e+002 ns, low_wf_chain/it0 1.300000e+003 ns, low_wf_chain/it1 1.300000e+003 ns. No new ready workstation card. T038 belongs to codex-desk.

## 2026-11-26T14:01:26+08:00 (UTC+8)
- None to claim or collect. The ws branch is synchronized at 69ab0297. The inbox is empty and the only ready card, T038, belongs to codex-desk.
- Checked all six active jobs. T040 low G0 it0 is rendering the +28 ns images. T040 high G0 it0, low G0 it1 and T034 high_cond_lo/it2 are rendering their nominal images. T033 has delivered 16/18 members and T034 has delivered 4/8.
- T040_retry01 and T042_retry01 continue production. The active solver budgets advanced since the 13:30 pass. Latest recorded times are T040/high_wf_chain/it0 8.000000e+002 ns, T040/high_wf_chain/it1 1.359720e+002 ns, T040/high_wf_G1/it0 1.644975e+002 ns, T040/low_wf_chain/it0 1.300000e+003 ns, T040/low_wf_chain/it1 1.300000e+003 ns, T042/high_bd/it2 7.254800e+002 ns, T042/high_bdwf/it2 5.514939e+002 ns, T042/high_tau35/it2 7.399839e+002 ns.

## 2026-11-26T13:31:06+08:00 (UTC+8)
- None to claim or collect. The ws branch is synchronized at 69ab0297 and the inbox is empty. The only ready card, T038, belongs to codex-desk.
- Checked all six active wrappers. T040 low G0 it0 completed 5/7 postprocessing stages with exit code 0 and is rendering the +28 ns images. T040 high G0 it0, low G0 it1 and T034 high_cond_lo/it2 are rendering their nominal images. T033 has 16/18 imaging deliveries and T034 has 4/8.
- T040_retry01 and T042_retry01 continue solving. Latest budget times are T040/high_wf_chain/it0 8.000000e+002 ns, T040/high_wf_chain/it1 7.299872e+001 ns, T040/high_wf_G1/it0 1.159880e+002 ns, T040/low_wf_chain/it0 1.300000e+003 ns, T040/low_wf_chain/it1 1.300000e+003 ns, T042/high_bd/it2 4.154715e+002 ns, T042/high_bdwf/it2 3.409521e+002 ns, T042/high_tau35/it2 5.589524e+002 ns.

## 2026-11-26T13:02:21+08:00 (UTC+8)
- T040 high G0 it0 reached 800 ns and low G0 it1 reached 1300 ns. Both exited 0. Committed their registry rows and verified the high G0 it1 input against the accepted 0.318% pulse-energy shortfall. Reports pushed through ws 69ab0297.
- Started high it0 and low it1 imaging and diagnostics as separate eight-worker jobs on nodes 1 and 0. Job statuses: T040_high_wf_chain_it0_post STATE=running  elapsed=0.00:00:00  pid=46196  commit=69ab0297. T040_low_wf_chain_it1_post STATE=running  elapsed=0.00:00:00  pid=49564  commit=69ab0297. T040_low_wf_chain_it0_post STATE=running  elapsed=0.00:58:26  pid=34636  commit=39cfae3a. T034_high_cond_lo_post STATE=running  elapsed=0.00:30:00  pid=45976  commit=e051d477. T040_retry01 STATE=running  elapsed=0.02:23:51  pid=6012  commit=07f2a116. T042_retry01 STATE=running  elapsed=0.01:30:29  pid=50076  commit=8bbc5264. T033 and T034 imaging deliveries stand at 16/18 and 4/8.
- Latest solver budgets: T040/high_wf_chain/it0 8.000000e+002 ns, T040/high_wf_chain/it1 4.149813e+001 ns, T040/high_wf_G1/it0 9.649783e+001 ns, T040/low_wf_chain/it0 1.300000e+003 ns, T040/low_wf_chain/it1 1.300000e+003 ns, T042/high_bd/it2 1.464993e+002 ns, T042/high_bdwf/it2 1.454861e+002 ns, T042/high_tau35/it2 3.759620e+002 ns. Inbox empty. T038 is ready on codex-desk.

## 2026-11-26T12:32:21+08:00 (UTC+8)
- Delivered T034 high_ftc_hi/it2 on ws e051d477. All five stages passed at 29 frame times. All 18 P2 estimates are missing. Synced and hash-verified the report. T034 imaging now covers 4/8. Started T034_high_cond_lo_post on node 1 with eight workers. STATE=running  elapsed=0.00:00:00  pid=45976  commit=e051d477.
- T040 low it0 nominal imaging continues on node 0. T040_retry01 and T042_retry01 continue production. Latest solver times: T040/high_wf_chain/it0 7.239485e+002 ns, T040/high_wf_G1/it0 7.749745e+001 ns, T040/low_wf_chain/it0 1.300000e+003 ns, T040/low_wf_chain/it1 1.268992e+003 ns, T042/high_bd/it2 9.499975e+001 ns, T042/high_bdwf/it2 7.649886e+001 ns, T042/high_tau35/it2 1.569731e+002 ns.
- T033 imaging stands at 16/18. The two current imaging slots hold the T040 iteration and the high-fluence T034 member. The only ready card T038 belongs to codex-desk. Inbox empty.

## 2026-11-26T12:04:14+08:00 (UTC+8)
- T033 low_sig_lo/it1 handed off. Imaging now covers 16/18 members. All 18 P2 estimates are unavailable. Verified the synced report hash. T034 high_ftc_hi/it2 was running image_v2 at inspection.
- T040 low_wf_chain/it0 returned 0 at 1300 ns. Registered it on ws, verified the it1 waveform energy and both switches, and launched the it0 nominal and +28 ns imaging sequence with diagnostics. T040_low_wf_chain_it0_post STATE=running  elapsed=0.00:00:19  pid=34636  commit=39cfae3a. T040_retry01 continues production.
- T042_retry01 continues three solves. All three generated ini files passed the specified input checks. Latest budgets for both batches: T040/high_wf_chain/it0 4.519779e+002 ns, T040/high_wf_G1/it0 5.999868e+001 ns, T040/low_wf_chain/it0 1.300000e+003 ns, T040/low_wf_chain/it1 2.914207e+002 ns, T042/high_bd/it2 6.299882e+001 ns, T042/high_bdwf/it2 4.799739e+001 ns, T042/high_tau35/it2 4.949767e+001 ns.
- ws deliveries pushed through 39cfae3a. Inbox empty. The only ready card T038 belongs to codex-desk.

## 2026-11-26T11:32:34+08:00 (UTC+8)
- Claimed T042 on ws at 8bbc5264. The first wrapper exited 1 while reading the UTF-8 YAML with the Windows GBK default, before creating any member. Retained its logs, set PYTHONUTF8=1 and launched T042_retry01 with the same batch at 24 MPI ranks. STATE=running  elapsed=0.00:00:42  pid=50076  commit=8bbc5264. Generated inputs at this check: high_bd input pending / high_bdwf input pending / high_tau35 input pending.
- T040_retry01 running. Latest budgets: high_wf_chain/it0 1.404865e+002 ns, high_wf_G1/it0 4.299896e+001 ns, low_wf_chain/it0 1.300000e+003 ns. The low it0 solver reached 1300 ns and its registry row arrived during this pass. The it1 directory exists. Low it0 imaging and the iteration handoff are pending.
- T033 low_sig_lo/it1 and T034 high_ftc_hi/it2 are still running image_v2. T038 is ready on codex-desk.

## 2026-11-26T11:04:34+08:00 (UTC+8)
- Delivered T033 low_ftc_hi/it1 (15/18) and T034 high_ftc_lo/it2 (3/8) on ws db16328b. Both members lack all 18 P2 estimates. Synced both reports and verified their SHA-256 hashes.
- T034 high_ftc_hi/it2 (PID 40460) and T033 low_sig_lo/it1 (PID 48032) both started image_v2 in the background. The only ready card T038 belongs to codex-desk.
- T040_retry01 (PID 6012, 32-core cap) is running. All three it0 waveform inputs passed the energy check. Latest solver times are high_wf_chain/it0 5.349936e+001 ns, high_wf_G1/it0 2.699958e+001 ns, low_wf_chain/it0 2.364383e+002 ns.

## 2026-11-26T10:55:59+08:00 (UTC+8)
- T041 delivered to Claude for review in ws 19428667. Verified all five CSVs against 405 indexed snapshots and synced the report with a matching hash. T033 low_ftc_hi and T034 high_ftc_lo imaging have finished and await collection on the scheduled pass. T038 is desk-owned.
- T040_retry01 remains running. Latest solver times: high_wf_chain/it0 3.999660e+001 ns, high_wf_G1/it0 2.149787e+001 ns, low_wf_chain/it0 9.199690e+001 ns.

## 2026-11-26T10:37:58+08:00 (UTC+8)
- Launch recovery. The first wrapper exited 1 at module import because the repository src directory was missing from PYTHONPATH. It created no member directories. Preserved its logs under D:/sim_runs/T040. Added the repository source and environment DLL paths to the child process, verified the batch CLI, and started T040_retry01 (PID 6012) with the same batch and 32-core cap. Monitor T040_retry01 for job status and D:/sim_runs/T040 for member data.
- T040 claimed and pushed on ws at 07f2a116. Started production via job.ps1 with --max-cores 32. The configuration assigns eight ranks each to the G0 high and low waveform chains and sixteen to G1 high it0. Generated inputs and iteration outputs are pending verification. T033 low_ftc_hi and T034 high_ftc_lo imaging continue. Their production budgets reached 1.300000e+003 and 8.000000e+002 ns respectively. The remaining ready card T038 belongs to codex-desk.
- T040 STATE=failed(exit=1)  elapsed=0.00:00:00  pid=27216  commit=07f2a116. T033_low_ftc_hi_post STATE=running  elapsed=0.00:35:46  pid=48356  commit=29e1057b. T034_high_ftc_lo_post STATE=running  elapsed=0.01:05:19  pid=37992  commit=ed4595bd.

## 2026-11-26T10:30:46+08:00 (UTC+8)
- None to start or collect. ws synchronized to 126bb960. Inbox empty. T033_low_ftc_hi_post (PID 48356) and T034_high_ftc_lo_post (PID 37992) are running image_v2, each with zero completed stages. Their final production budgets reached 1.300000e+003 and 8.000000e+002 ns respectively. T033 has delivered 14/18 members and T034 has delivered 2/8. The only ready card, T038, belongs to codex-desk.
