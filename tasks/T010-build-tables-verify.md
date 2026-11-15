---
id: T010
title: Windows 构建 pluto_lip、生成全分辨率物性表、跑 v0 验证小套件与双平台一致性
owner: codex
status: draft
depends: [T008]
milestone: D0
est_time: 1.5h（其中机时约 1h）
started:
---

> 草稿说明：本卡依赖 Claude 的工作包 W1–W6（PLAN v3 §12.2，预计 H10 提交）。下面的命令是 PLAN §11 的接口契约；Claude 交付代码后核对命令、填入 `<commit>` 再置 `ready`。

## Goal
把 Claude 在云端 Linux 上跑通的"未改动 PLUTO 内核 + `pluto/lip` 用户模块"在工作站上构建出来，生成全分辨率物性表，并用自动判定的验证小套件确认两个平台结果一致。全部通过后才允许跑 T011。

## Inputs
- 代码提交：`<commit>`（Claude 填写）。`pluto/lip/`、`pluto/overrides/`（应为空或只有列明的文件）、`src/lipsim/`、`configs/tables/v0.yaml`、`verify/ref/`（云端参考输出与 `ref_manifest.json`）。
- 官方压缩包：`third_party/pluto/`（T008）。构建适配：`third_party/pluto/windows/`。
- `docs/PREREG_v0.md`（只读；记录其 SHA256）。

## Steps
1. `git pull`；`python scripts/check_provenance.py`（须通过）；`pytest -q`（须全过；失败则 `blocked` 并贴出失败项）。
2. 构建：`powershell -NoProfile -File scripts/win/run_lipsim.ps1 scripts/win/build_pluto_lip.py --mode serial` 与 `--mode mpi`。产物在 `D:\sim_tools\pluto_lip\build\{serial,mpi}\pluto.exe`。`build_provenance.json` 必须列出：官方压缩包 SHA256、每个进入构建的非官方文件及其 SHA256、相对官方源码被替换的上游文件清单（与 `pluto/overrides/` 一一对应）。
3. 物性表（后台，`job.ps1`，60 个工作进程）：
   `python -m lipsim.tables.build --config configs/tables/v0.yaml --out D:\sim_runs\tables\v0 --workers 60`
   完成后把 `manifest.json`（各表 SHA256、网格范围、未收敛点数）复制到 `results/T010/tables_manifest.json`。表文件本身不入库。
4. 表的自检：`python -m lipsim.verify --suite tables --tables D:\sim_runs\tables\v0 --out results/T010/verify_tables`。
5. PLUTO 验证小套件（后台，约 30–40 核）：`python -m lipsim.verify --suite v0 --tables D:\sim_runs\tables\v0 --out results/T010/verify_v0`。包含（判据由程序内置并写入 `verify.json`，对应 PLAN §7）：
   | 项 | 内容 | 判据 |
   |---|---|---|
   | V4 | 表格往返 T(ρ, e(ρ,T,Y), Y)；热力学一致性；离格点对直接 Saha 解 | < 1e-3；< 1%；< 2% |
   | V3 | 平衡空气强激波，马赫 10/15/20 | 波后 T、ρ 与同表 Rankine–Hugoniot 解差 < 1% |
   | V5 | Cu/空气运动接触间断 | 压力振荡 < 1% |
   | V8 | 均匀吸收板的 Beer–Lambert 与往返反射 | 透过率与解析差 < 0.5% |
   | V8b | 均匀气体 NEC 冷却箱 | 与参考常微分方程解差 < 1% |
   | V13 | 封闭箱 + 蒸发源注入的质量、能量守恒 | 残差 < 0.1% |
   | V14′a | 串行与 MPI（`-dec 8 1`） | 密度场 L1 差 < 0.5% |
   | V14′b | 本机结果与 `verify/ref/`（云端 Linux） | 各算例 L1 差 < 0.5% |
6. 冒烟算例：`python -m lipsim.batch configs/batches/T010_smoke.yaml --max-cores 16`（高通量、G0、0–150 ns、it0）。只检查能跑完、`budget.csv` 残差 < 0.1%、输出可被 `lipsim.image` 读取；**不看图像与实验的比较**。记录墙钟时间与"单元·步/秒/核"。
7. 报告 `tasks/T010.report.md`（逐项列 `verify.json` 的结果），置 `review`，写 handoff，push。
8. **快速通道**：若步骤 1–6 的机检判据全部通过，且 `handoff/to_codex/` 中有 Claude 写明"T010 机检全过可直接执行 T011"的说明，则在同一轮继续执行 T011，不必等 Claude 把本卡改为 `done`。任何一项不过则停下。

## Outputs
- `results/T010/`：`build_provenance.json`、`tables_manifest.json`、`verify_tables/verify.json`、`verify_v0/verify.json`、各验证算例的小图与日志、`smoke_timing.json`（≤ 20 MB）
- `D:\sim_tools\pluto_lip\`、`D:\sim_runs\tables\v0\`、`D:\sim_runs\T010\`（不入库）

## Acceptance criteria
- 两种构建成功；被替换的上游文件清单与 `pluto/overrides/` 完全一致，其余官方文件 SHA256 未变。
- 物性表未收敛点数为 0（若不为 0：列出 (ρ, T, Y) 位置，置 `blocked`）。
- 步骤 5 表中各项全部通过；步骤 6 冒烟算例跑完且收支残差 < 0.1%。

## Not allowed
- 修改 `pluto/lip/*.c`、`src/lipsim/` 的物理部分、`configs/` 中的任何数值或登记表；验证不过时放宽判据或更换算例。
- 编译、路径、编码类工程问题可以直接修，但要在报告中逐条列出改了什么。
- 把冒烟算例的图像与实验帧比较，或据此提出参数建议。
