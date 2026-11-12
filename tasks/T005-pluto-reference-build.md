---
id: T005
title: Unpatched PLUTO 4.4-patch4 reference build + Sod/Sedov
owner: codex
status: ready
depends: []
milestone: M0
est_time: 2h
started:
---

Spec copied from docs/PLAN.md §12.3 (the PLAN is authoritative).

**T005 未打补丁的 PLUTO 参考构建**（owner: codex，depends: 无，est: 1–2 h）
- 目标：为 V14 代码间对比准备一个不含任何项目补丁的 PLUTO 4.4-patch4。
- 输入：官方压缩包（URL 与 SHA256 见 `prior/pluto/provenance/upstream_sources.json`）；`prior/pluto/upstream_overrides/` 中仅与 Windows 编译有关的修复。
- 步骤：(1) 解压到 `D:\sim_tools\pluto_ref\` 并核对 SHA256；(2) 用现有 MinGW 与 MS-MPI 编译官方算例 `Test_Problems/HD/Sod` 与 `Test_Problems/HD/Sedov`（串行与 8 进程各一次），记录编译器路径与版本、所用的每一处 Windows 修复；(3) 可选：尝试用户级便携 GCC（解压到 `D:\sim_tools\`）重复编译，记录是否成功；(4) 输出两个算例的密度剖面 CSV 与耗时到 `results/T005/`。
- 验收：两个算例串行与 MPI 都跑完；Sod 激波位置与精确解相差不超过 1 格；Sedov 激波半径随时间的幂指数在 0.40 ± 0.02；报告列出相对官方源码的全部改动。
- 不允许：加入 `prior/pluto/solver/` 或 `patches/` 中的任何物理补丁；安装需要管理员权限的软件。

## Finishing
Report `tasks/T005.report.md`, set to `review`, write `handoff/to_claude/T005.md`, push. Compiler and build products stay under `D:\sim_tools\`; commit only CSVs, small figures and logs.
