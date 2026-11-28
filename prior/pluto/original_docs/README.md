# S4 羽流复现工作区

72 小时窗口：**2026-11-10 22:37:46 至 2026-11-13 22:37:46，北京时间**。

## 查看结果

- `reports/latest.txt`：每小时整点的简短文字汇报。
- `reports/latest.pdf`：最新 PDF；每天 07:00 自动更新。
- `figures/best_comparison.png`：全部 29 帧实验/计算对照。
- `figures/best_comparison.gif`：按实验延时顺序播放的对照动画。
- `figures/best_evolution.gif`：每 5 ns 一帧的计算演化动画，采用相同 10 ns 门控。
- `figures/best_metrics.png`、`best_budget.png`：形貌曲线与守恒预算。
- `state/status.json`：后台最新状态（每约 20-30 秒更新，纯本地程序，无模型调用）。

## 后台运行

Windows 任务 `S4Plume_Watchdog` 运行 Python 调度器。它保持计算期间系统唤醒，按阶段安排
MPI 算例、恢复检查点、处理图像、选优并保存状态。任务完成前的每 5 分钟触发仅用于进程异常退出后的
恢复；正常情况下已有实例继续运行，新触发被忽略。

`S4Plume_Hourly` 每小时整点生成文字；`S4Plume_Daily0700` 每天北京时间 07:00 生成 4 页 PDF。
这些任务不使用语言模型、API 密钥或网络服务，正常计算不会产生模型调用 token。
任务以当前 Windows 用户的交互登录身份运行；注销或关机会中断计算，重新登录后可从检查点恢复。
72 小时截止时间固定，不因重启延长。

## 复现命令（PowerShell，项目根目录）

```powershell
& .venv/Scripts/python.exe scripts/extract_experiment.py
& .venv/Scripts/python.exe scripts/build_solver.py
& .venv/Scripts/python.exe scripts/case.py --id example --ranks 8 --level coarse --tstop 1000
& .venv/Scripts/python.exe scripts/postprocess.py example
& .venv/Scripts/python.exe scripts/report.py daily
```

运行参数记录在每个 `runs/<case>/params.json` 和 `pluto.ini` 中。
调度资源、阶段时长和搜索范围由 `config/campaign.yaml` 读取；修改后在下一次控制器启动时生效。
源项与热力学代码在 `solver`，上游 PLUTO 版本/下载地址/哈希在 `vendor/sources.json`。
编译器和 MPI 路径记录在 `state/build.json`。独立 Python 虚拟环境复用本机 NumPy/SciPy，
额外依赖版本见 `config/python_environment.txt`。

## 控制

如需主动停止，在 `state` 下创建名为 `STOP` 的空文件；调度器会结束其子进程并生成停止时报告。
正常截止自动停止新增算例、结束仍在运行的计算并生成最终报告与结果索引。
后台最多安排 64 个 MPI 进程；最终精算切换到 2 个算例、每个 32 进程。

模型的物理范围、近似和参数定义见 `MODEL.md`。当前结果的“最佳”表示已计算候选中的最优，
不表示已经达到最终复现要求。

