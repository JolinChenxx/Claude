---
id: T012
title: 工作站常驻执行器（自动拉取并执行批次清单，免去每轮人工触发）
owner: codex
status: draft
depends: []
milestone: D0
est_time: 1h
started:
---

> **本卡需要用户明确批准后才能置 `ready`**（PLAN v3 §15-4）。未批准则保持 `draft`，改为由用户每天多次手动触发 Codex。历史上的 S4Plume_* 计划任务不是用户设置的，已按用户意见禁用；本卡不得重新启用它们。

## Goal
两周工期内 Codex 每一轮都要用户手动触发，是算力闲置的主要原因。建立一个**不含大模型、只执行白名单命令**的常驻执行器：定时 `git pull`，发现 `queue/` 下新的批次清单就执行，完成后把精简结果与 handoff 推回仓库，从而唤醒 Claude。Codex 仍负责需要判断的卡（构建、排错、写报告）。

## Inputs
- `scripts/win/runner.py`（由 Claude 提供；本卡不自己写执行逻辑，只部署与测试）。
- 仓库 `ws` 分支；`D:\sim_tools\envs\lipsim\python.exe`；`scripts/win/job.ps1`。

## Design constraints（Claude 写 `runner.py` 时遵守，Codex 部署时核对）
1. 只处理 `queue/*.yaml`，且清单必须由 `ws` 分支上的提交引入；每个清单只执行一次（完成后移到 `queue/done/`，失败移到 `queue/failed/`）。
2. 清单中每一步只能是白名单入口之一：`lipsim.batch`、`lipsim.image`、`lipsim.report`、`lipsim.verify`、`lipsim.tables.build`、`lipsim.target.build`、`scripts/win/build_pluto_lip.py`。不接受任意 shell 命令。
3. 只写 `D:\sim_runs`、`D:\sim_tools` 与本仓库；总核数上限 60；检测到无关作业（如 FDTD）占用时相应减少。
4. 每步的命令行、起止时间、退出码写入 `D:\sim_runs\runner\runner.log`；每个清单结束后提交 `results/<id>/` 的小文件与 `handoff/to_claude/<id>.md` 并 push（`git pull --rebase` 后再 push；冲突则停下并写日志，不强推）。
5. 仓库根目录存在 `queue/STOP` 文件时立即停止接单；正在运行的作业不杀。

## Steps
1. 确认用户批准记录已写入 `docs/DECISIONS.md`（没有则 `blocked`）。
2. 试运行：`python scripts/win/runner.py --once --dry-run`，确认能拉取、能解析 `queue/selftest.yaml`、拒绝含非白名单命令的 `queue/selftest_reject.yaml`。
3. 实跑自检清单（`lipsim.verify --suite tables` 的小算例），确认结果与 handoff 被推回、GitHub 上 `handoff` 工作流被触发。
4. 以**当前用户、无管理员权限**的方式常驻：用 `job.ps1 start -Id runner -Cmd "... runner.py --interval 300"` 启动。**不要**注册 Windows 计划任务或服务（需要时另行请示用户）；机器重启后由用户或下一轮 Codex 重新启动。
5. 报告 `tasks/T012.report.md`：部署方式、如何停止（`queue/STOP` 或 `job.ps1 stop -Id runner`）、自检结果。置 `review`，写 handoff，push。

## Outputs
- `results/T012/selftest.json`、`results/T012/runner_log_excerpt.txt`

## Acceptance criteria
- 白名单内清单被执行且结果回推；白名单外命令被拒绝并记录；`queue/STOP` 生效。
- 执行器进程以普通用户权限运行，未修改系统设置、执行策略或计划任务。

## Not allowed
- 让执行器运行任意命令、修改仓库中除 `results/`、`handoff/`、`queue/` 以外的文件、强制推送、推送到 `main`。
- 启用任何历史计划任务；安装需要管理员权限的软件。
