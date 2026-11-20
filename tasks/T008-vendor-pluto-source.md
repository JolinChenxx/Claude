---
id: T008
title: PLUTO 4.4-patch4 官方压缩包与 Windows 构建适配文件入库（供云端 Linux 编译）
owner: codex
status: done
depends: []
milestone: D0
est_time: 20min
started: 2026-11-15T22:14:18+08:00
---

## Goal
云端沙箱访问不了 plutocode.ph.unito.it（代理 403），Claude 无法在 Linux 上编译测试 PLUTO 用户模块。把官方压缩包原样入库，并把 T005 实际用到的 Windows 构建适配文件整理到一处。这是 PLAN v3 §10 时间表上的第一步，**做完立即 handoff，不要等 T009**。

## Inputs
- 官方压缩包：`D:\junlin\PlasmaPlumeHighInfluence\vendor\pluto-4.4-patch4.tar.gz`（只读；T005 已用过）。期望 SHA256 `1ba5527b76d49fdd78ae24dbfbdad085ec83393748f1e618516a9d63bd945787`（`prior/pluto/provenance/upstream_sources.json`）。
- T005 的构建记录：`results/T005/build_provenance.json`、`scripts/win/build_pluto_reference.py`。

## Steps
1. 核对压缩包 SHA256。不一致则 `blocked`，不要换别的来源。
2. 复制（不是移动）到 `third_party/pluto/pluto-4.4-patch4.tar.gz`。若文件 > 45 MB：不要提交原包，改为重新打包只含 `PLUTO/Src`、`PLUTO/Config`、`PLUTO/Tools`、`PLUTO/Lib`、`PLUTO/Test_Problems/HD` 以及根目录的许可与说明文件，命名 `pluto-4.4-patch4-src-only.tar.gz`，并另存 `third_party/pluto/file_sha256.csv`（原包内每个文件的路径与 SHA256，用于证明重新打包的文件逐字节未改）。
3. 写 `third_party/pluto/README.md`：版本、下载 URL、SHA256、许可（照录压缩包内许可文件的名称与首段，不要凭记忆写）、"本目录文件不得修改"、是否重新打包。
4. 新建 `third_party/pluto/windows/`，放入 T005 构建实际使用的全部非官方文件的**副本**：改过分隔符的 `Src/HD/makefile`、`Config/Windows.serial.gcc.defs`、`Config/Windows.mpi8.gcc.defs`、`windows_compat.c`、`windows_compat.h`；并写 `windows/README.md` 说明每个文件的用途、编译器与 MS-MPI 路径、编译选项。不要复制 MS-MPI SDK 或任何 ANSYS 文件。
5. 记录 `D:\sim_tools\pluto_ref\build\Sedov_mpi8\pluto.exe -h`（或等效的帮助输出）到 `results/T008/pluto_help.txt`，用于确认命令行是否支持 `-dec`（自定义分区）。
6. 报告 `tasks/T008.report.md`，置 `review`，写 `handoff/to_claude/T008.md`，push。然后继续 T009。

## Outputs
- `third_party/pluto/…`（压缩包、README、windows/）
- `results/T008/pluto_help.txt`、`results/T008/sha256.txt`

## Acceptance criteria
- 入库压缩包的 SHA256 与期望值一致（或重新打包时 `file_sha256.csv` 覆盖全部文件，且抽查 20 个文件与原包逐字节一致）。
- `third_party/pluto/windows/` 中的文件与 `D:\sim_tools\pluto_ref` 实际使用的文件 SHA256 一致（列表写进报告）。
- 报告写明 `-dec` 选项是否出现在帮助输出中。

## Not allowed
- 修改压缩包内任何文件；加入 `prior/pluto/solver/` 或 `prior/pluto/patches/` 中的任何物理补丁。
- 写入 `D:\junlin\`。
