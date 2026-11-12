---
id: T004
title: Read-only forensics of the historical PLUTO best case
owner: codex
status: ready
depends: [T003]
milestone: M0
est_time: 3h
started:
---

Spec copied from docs/PLAN.md §12.3 (the PLAN is authoritative). Use the Python environment from T003: `D:\sim_tools\envs\lipsim\python.exe`. Background reading: `docs/PRIOR_DIAGNOSIS.md` §3.

**T004 历史 PLUTO 最佳算例只读取证**（owner: codex，depends: 无，est: 2–3 h）
- 目标：用已有原始场检验 `docs/PRIOR_DIAGNOSIS.md` §3 第 1–4 条判据，不重算。
- 输入：`D:\junlin\PlasmaPlumeHighInfluence\runs\case_00326_coarse\` 的场输出（路径见 `prior/pluto/raw_inventory.csv`）；读数与发射率公式照搬 `prior/pluto/scripts/postprocess.py`、`common.py`（复制到 `scripts/forensic/` 使用，不改 `prior/`）。
- 步骤：对每个已保存的输出时刻计算并写入 `results/T004/forensic_timeseries.csv`：
  1. 铜总质量；铜质量按温度分箱（< 3000、3000–6000、6000–8000、8000–10000、> 10000 K）。
  2. 铜质量加权平均温度；按归档发射率加权的平均温度 T_em。
  3. Y_Cu > 0.01 区域的体积、等效半径、平均压强。
  4. 该区域内的热能、电离能、动能。
  5. 该区域的辐射功率（归档公式）与穿过其边界的导热功率（归档导热系数公式）；各自的冷却时间 = 区域内能 ÷ 功率。
  6. 三种带内总信号（不加相机、不截幅）：归档的光学厚图像；同一场的光学薄极限；把上能级换成 6.19 eV 的光学薄极限。各自除以 300 ns 的值，并给出 300–630 ns 的 e 折时间。
  另出 4 张图（温度分箱、T_em、冷却时间、三种归一化信号）与 `forensic_summary.json`。
- 验收：5 个抽查时刻的铜总质量与 `budget.csv` 相差 < 1%；覆盖 0–1000 ns 不少于 50 个时刻；`D:\junlin\` 下无任何写入。
- 不允许：运行 PLUTO、修改任何历史参数、对结果做拟合。

## Finishing
Report `tasks/T004.report.md`, set to `review`, write `handoff/to_claude/T004.md`, push. The historical 72-hour PLUTO controller may still be running: **only read** `D:\junlin\`, do not stop or modify it.
