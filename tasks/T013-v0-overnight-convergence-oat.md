---
id: T013
title: v0 夜间批次：单因素敏感性系综、时间步收敛、G2 网格收敛
owner: codex
status: draft
depends: [T011]
milestone: D0-D1
est_time: 0.5h 操作 + 12-17h 机时
started:
---

> 草稿说明：成员清单取自 `docs/PREREG_v0.md` 的敏感性设计（Claude 在 H10 提交，早于任何 v0 结果）。Claude 核对 `configs/batches/T013.yaml` 后置 `ready`。本卡在 T011 收集完成的同一轮启动，让 64 核在 D0 夜间到 D1 午后保持满载。**快速通道**：若 `handoff/to_codex/` 中有 Claude 写明"T011 置 review 后可直接执行 T013"的说明，则不必等 T011 改为 `done`（本卡不依赖 T011 的比较结果，只依赖其代码、物性表与靶源文件）。

## Goal
在不看比较结果、不改任何基线值的前提下，给 v0 补上三类信息：(1) 各输入在其事先登记的不确定度范围两端对主指标与气相吸收能量的影响（PLAN §9，单因素，不是调参）；(2) 时间步收敛；(3) G2 网格收敛。

## Inputs
- 与 T011 相同的代码提交、物性表、可执行文件与 `PREREG_v0.md`（SHA256 必须与 T011 报告一致；不一致则 `blocked`）。
- `configs/batches/T013.yaml`。

## Batch（顺序即优先级；全部登记、全部报告）
| 阶段 | 算例 | 网格 | 进程 × 并发 | 机时（估计） |
|---|---|---|---|---|
| 1 | 单因素两端：`PREREG_v0.md` 列出的每个输入取其登记范围的下端与上端，其余取基线；{high, low}；NEC 光学薄；靶源按 it0→it1 两次迭代。预计约 12 个输入 × 2 端 × 2 通量 ≈ 48 例（以 PREREG 为准） | G0 | 8 × 7 | 3–4 h |
| 2 | 时间步收敛：T011 的 D 组中 {high, low} × NEC 光学薄，CFL 减半 | G1 | 16 × 2 | 3–5 h（与阶段 1 后半并行） |
| 3 | 网格收敛：{high, low} × NEC 光学薄，靶源同 T011 的 C/D 组 | G2 | 32 × 2 | 8–14 h |
- 需要重建物性表的输入（例如电子–中性截面的两端）：`lipsim.batch` 会先调用 `lipsim.tables.build` 生成带后缀的变体表（存 `D:\sim_runs\tables\v0_<名>`，哈希写入登记）；不得覆盖基线表。
- 只涉及后处理的情景（滤光片、时间零点、Stark 宽度）不在本卡重算流场，由 `lipsim.image` 在已有算例上完成。

## Steps
1. 记录机器负载；`scripts\win\job.ps1 start -Id T013 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T013.yaml --max-cores 60"`。本轮结束。
2. 后续每轮：`job.ps1 status -Id T013`。**阶段 1 与 2 一结束就先出一份中间包**（不必等阶段 3）：
   `python -m lipsim.image D:\sim_runs\T013 --scenarios configs/imaging/v0.yaml --all`
   `python -m lipsim.report v0-sens --runs D:\sim_runs\T013 --base D:\sim_runs\T011 --out results/T013`
   提交 `results/T013/` 并写 `handoff/to_claude/T013-partial.md`。
3. 阶段 3 结束后重跑上面两条命令（加 `--gci`），生成 `convergence_gci.csv`，写 `tasks/T013.report.md`，置 `review`，写 handoff，push。

## Outputs
- `results/T013/`：`oat_table.csv`（每个成员一行：输入名、端点值、通量、气相吸收能量、汽化质量、P1–P6）、`tornado_high.png`、`tornado_low.png`、`timestep_convergence.csv`、`convergence_gci.csv`（G0/G1/G2 的气相吸收能量与主指标、Richardson 外推、GCI）、`SENSITIVITY_v0.md`、`run_registry_excerpt.csv`（≤ 20 MB）
- 原始场：`D:\sim_runs\T013\`（不入库）

## Acceptance criteria
- 清单中每个算例都有登记记录；完成率 ≥ 95%，未完成的原样记录日志末 50 行，不重试、不改参数。
- 每个完成算例的收支残差 < 0.1%。
- `convergence_gci.csv` 含三套网格；CFL 减半与基线的主指标差写入 `timestep_convergence.csv`（目标 < 2%，超出则如实标注，不算本卡失败）。
- 龙卷风图按输入名排序画出全部成员，**不标注哪个成员更接近实验**。

## Not allowed
- 根据本卡结果修改任何基线值、登记表或批次清单；增加"范围之外"的取值。
- 只报告部分成员；在图表中把最接近实验的成员单独标出。
- G2 未收敛（G1→G2 变化超过 PLAN §6 的限度）时自行加密或改数值参数：如实报告，由 Claude 决定。
