---
id: T011
title: v0 快速基线生产批次（两通量 × NEC 两极限 × Picard × G0/G1 + 分支 B 保底）与报告包
owner: codex
status: draft
depends: [T010]
milestone: D0
est_time: 0.5h 操作 + 4-5h 机时
started:
---

> 草稿说明：依赖 Claude 的 W1–W6 与已提交的 `docs/PREREG_v0.md`。Claude 交付后核对批次清单与命令、填入 `<commit>` 再置 `ready`。快速通道见 T010 步骤 8。

## Goal
产出 PLAN v3 §10 定义的 24 小时交付物所需的全部算例与精简审查包。算例清单是**事先声明、全部报告**的：不增、不减、不挑。

## Inputs
- 代码提交 `<commit>`；`docs/PREREG_v0.md`（记录 SHA256）；`configs/batches/T011.yaml`；`configs/cases/v0_*.yaml`；`configs/imaging/v0.yaml`。
- 物性表 `D:\sim_runs\tables\v0`（T010）；`D:\sim_tools\pluto_lip\build\mpi\pluto.exe`（T010）。
- `results/T009/summary.json`（只用于分支 B 保底成员的能量区间）。

## Batch（`configs/batches/T011.yaml` 的内容；共 22 个流体算例）
| 组 | 算例 | 网格 | 进程 | 说明 |
|---|---|---|---|---|
| A | {high, low} × {NEC 光学薄, NEC 共振线俘获} × it0 | G0 | 8 | 靶面见未屏蔽光强（蒸气量上界） |
| B | 同上 4 例 × it1 | G0 | 8 | 用 A 组输出的透过率与壁面压强历史重算靶源（`lipsim.target.build --shielding`） |
| C | 同上 4 例 × it2 | G0 | 8 | 同理，用 B 组输出 |
| D | 同上 4 例，使用与 C 组相同的靶源（同一透过率历史，即 it2） | G1 | 16 | 只改网格；另输出 G1 自身的透过率，与 G0 比较 |
| E | 分支 B 保底：{high, low} × E_blast 区间的低/中/高，NEC 光学薄 | G0 | 8 | **保底预跑**；能量取自 T009，其余同 A 组。只有点燃关不过时才在报告正文呈现 |
- 模拟时间：high 0–1000 ns，low 0–1300 ns；每 2.5 ns 输出。
- 调度：A 与 E 同时启动（10 例 × 8 = 80 进程 > 64 核时由 `lipsim.batch` 自动排队）；B、C 依次接续；D 在 C 完成后以 4 × 16 = 64 进程运行。若 T009 的 `summary.json` 中某组实验的 E 区间为 null，E 组对应通量改用其中偏差最小的三个能量，并在报告中注明。

## Steps
1. `git pull`；确认 T010 的 `verify.json` 全过；记录机器负载（同 T009 步骤 0）。
2. 后台启动：`scripts\win\job.ps1 start -Id T011 -Cmd "D:\sim_tools\envs\lipsim\python.exe -m lipsim.batch configs/batches/T011.yaml --max-cores 60"`。本轮到此结束；`lipsim.batch` 会自动把每个算例（含失败的）追加到 `results/run_registry.csv`。
3. 下一轮：`job.ps1 status -Id T011`。全部结束后依次执行：
   - `python -m lipsim.image D:\sim_runs\T011 --scenarios configs/imaging/v0.yaml --all`（三个滤光片情景；合成帧时刻与 S4/S3 标注时刻一致，门 [t, t+10 ns]）
   - `python -m lipsim.report v0 --runs D:\sim_runs\T011 --blast results/T009/summary.json --out results/T011`
4. 检查 `results/T011/` 是否齐全（见 Outputs）；写 `tasks/T011.report.md`：每个算例一行（是否跑完、墙钟、收支残差、气相吸收能量、汽化质量、是否触发任何数值告警）。置 `review`，写 handoff，push。
5. 若已有 `ready` 的 T013，接着启动它。

## Outputs
- `results/T011/COMPARISON_v0.md`（PLAN §8.5 固定格式，由程序生成，不手改）
- `results/T011/`：`s4_side_by_side_high.png`、`s3_side_by_side_low.png`、`L_normalized.png`、`shock_vs_fig3d.png`、`metrics_table.csv`（每个成员一行：通量 × NEC 极限 × 迭代 × 网格 × 滤光片情景）、`budget_table.csv`、`ignition_gate.json`、`picard_convergence.csv`、`grid_G0_vs_G1.csv`、`diagnostics/`（最高温、最大 nₑ、越表占比随时间）、`run_registry_excerpt.csv`（≤ 20 MB）
- 原始场：`D:\sim_runs\T011\<case>\`（不入库）

## Acceptance criteria
- A–E 共 22 个算例全部有登记记录；跑完的每例收支残差 < 0.1%（it0 的靶–气不自洽量单列，不计入残差）。
- 未跑完或数值告警（负压、越表、NaN、时间步崩溃）的算例：原样记录日志末 50 行，**不重试、不改参数**；只要高通量 G0 的 A 组两例跑完，本卡即可置 `review`（其余标注"未完成"）。
- `metrics_table.csv` 覆盖全部已完成成员与三个滤光片情景；`COMPARISON_v0.md` 无缺节。
- 报告写明使用的代码提交号、`PREREG_v0.md` 与物性表的 SHA256。

## Not allowed
- 增删批次中的算例、修改任何物理或数值参数、调整增益/平移/时间零点使图像更像。
- 因为某个成员"更像实验"而在报告中突出它；删除失败或"不像"的成员。
- E 组（分支 B）的结果不得与 A–D 组混排，报告中单独成节并标注"半经验保底预跑"。
