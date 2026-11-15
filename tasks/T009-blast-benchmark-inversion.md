---
id: T009
title: 未改动 PLUTO 的 2D 轴对称点爆炸：性能/分区基准、网格收敛、对图 3d 的能量反推
owner: codex
status: running
depends: []
milestone: D0
est_time: 1h 操作 + 2-3h 机时
started: 2026-11-15T22:14:18+08:00
---

## Goal
不依赖任何新代码，立即把工作站算力用起来，为 PLAN v3 提供三样东西：(a) PLUTO 在本问题规模上的实测速度与 MPI 分区方式（决定 §6 的网格与并发）；(b) 纯流体的网格收敛信息（V2b）；(c) 理想气体点爆炸对图 3d 冲击波轨迹的能量区间 E_blast（PLAN §4.4 的独立检验；也是分支 B 的保底输入）。

**这是独立检验，不是标定。九个能量全部报告；本卡的任何结果都不得用来设定 v0 模型的参数。**

## Inputs
- 未改动的 PLUTO：`D:\sim_tools\pluto_ref\`（T005），MinGW GCC 5.1 + MS-MPI，构建方式同 `scripts/win/build_pluto_reference.py`。
- 实验冲击波轨迹：`data/exp/curves/fig3d_shock.csv`（列 fluence, t_ns, R_um）。

## Problem definition（照此写 `verify/pluto_blast/{definitions.h,pluto.ini,init.c}` 并入库）
- `definitions.h`：PHYSICS HD；DIMENSIONS 2；GEOMETRY CYLINDRICAL（x1 = r，x2 = z）；EOS IDEAL；RECONSTRUCTION LINEAR；TIME_STEPPING RK2；LIMITER VANLEER_LIM；SHOCK_FLATTENING MULTID；NTRACER 0；无冷却、无导热、无粘性；用户参数 2 个：`E_UJ`、`R0_UM`。单位：UNIT_DENSITY 1e-3、UNIT_LENGTH 1e-4、UNIT_VELOCITY 1e5（即 1 kg/m³、1 µm、1 km/s；时间 1 ns，压强 1 MPa，能量 1e-12 J）。
- `init.c`：`g_gamma = 1.4`。环境：ρ = 1.2041，p = 0.101325，速度 0（1 atm、293.15 K 空气）。能量沉积：球心在原点、半径 R0 = 20 µm 的半球内（√(r²+z²) < R0）加均匀超压 Δp，使域内超额内能 ΔE = Δp/(γ−1)·V 等于 E。V 必须用**离散单元体积之和**（2π r dr dz，对所有进程求和），不要用解析体积；用第 0 步输出核对 ΔE 与标称 E 相差 < 0.1%。1 µJ = 1e6 代码能量单位。
- `pluto.ini`：r、z ∈ [0, 1200]，均匀网格；`Solver hllc`；`CFL 0.4`；`first_dt 1e-6`；边界 X1-beg axisymmetric、X1-end outflow、X2-beg reflective、X2-end outflow；`flt` 每 5 ns 输出；`tstop 900`。
- 网格：基准 480×480（Δ = 2.5 µm）；收敛组 240×240（5 µm）与 960×960（1.25 µm）。

## Steps
0. 记录当前机器负载（`Get-Process` 中 CPU 占用最高的 10 个进程、空闲核数）。无关的 FDTD 作业不要动；若它占用 > 16 核，在报告中写明并相应减少并发。
1. 编译 MPI 版。把相对官方源码的全部改动列进报告（应与 T005 相同）。
2. **基准（单独运行，其间不跑别的作业）**：E = 90 µJ、480×480、`tstop 100`。进程数 1、8、16、32、64（默认分区）；再用 `-dec N 1`（只沿 r 分区）跑 N = 8、16、32。每次记录墙钟时间、步数、"单元·步/秒/核"。若 `-dec` 不被支持，记录帮助输出与报错，不要改源码。
3. **一致性**：`-dec 8 1` 与默认 8 进程在 t = 100 ns 的密度场相对 L1 差。
4. **能量扫描（后台，`job.ps1`）**：E ∈ {20, 30, 45, 65, 90, 130, 180, 250, 320} µJ，480×480，`tstop 900`，每例 7 进程、9 例并发（共 63 核；按步骤 0 的负载酌减并发）。
5. **网格收敛**：E = 90 µJ，Δ = 5、2.5、1.25 µm（1.25 µm 用 32 进程，排在步骤 4 之后）。
6. 后处理脚本 `scripts/verify/blast_post.py`（新写，入库）：
   - 激波位置：沿轴线（第一列，z 方向）、沿壁面（第一行，r 方向）、45° 方向，取压强自外向内首次超过 1.2 p_amb 的位置（相邻单元线性插值）。输出 `shock_E###.csv`（t_sim, R_axis, R_wall, R_45）。
   - 与实验比较：实验时间 t_exp = t_sim + t_c，t_c ∈ {25, 55, 85} ns（脉冲峰值在约定零点后 54.7 ns，±30 ns 为零点不确定度）。对高、低通量两组实验点分别计算 100–900 ns 内的均方根相对偏差（模型取 R_axis，线性插值到实验时刻），以及 300–900 ns 的线性拟合速度。输出 `deviation_table.csv`（9 个 E × 3 个 t_c × 2 组，全部列出）。
   - 图：`shock_vs_fig3d.png`（九条模型曲线 + 实验点）、`scaling.png`、`convergence.png`。
   - `summary.json`：每组实验下均方根偏差 ≤ 10% 的 E 区间（按相邻 E 的偏差线性插值求边界；若九个 E 无一满足，写 null 并给出偏差最小的三个）、早期幂指数、收敛组三套网格在 t = 100/300/600 ns 的 R_axis。
7. 报告 `tasks/T009.report.md`，置 `review`，写 handoff，push。

## Outputs
- `verify/pluto_blast/`（问题文件）、`scripts/verify/blast_post.py`
- `results/T009/`：`benchmark.csv`、`deviation_table.csv`、`summary.json`、`shock_E###.csv`、三张图、构建与运行日志（总计 ≤ 20 MB）
- 原始数据：`D:\sim_runs\T009\`（不入库）

## Acceptance criteria
- 9 个能量 + 3 套收敛网格全部跑完；每例的离散 ΔE 与标称 E 相差 < 0.1%。
- 激波到达外边界之前，域内总能量相对变化 < 0.1%。
- 激波半径在 R = 60–200 µm 区间（E = 90 µJ）的幂指数为 0.40 ± 0.03（强激波自相似段；背压使后期偏离是预期的，不算失败）。
- Δ = 2.5 与 1.25 µm 的 R_axis 在 t = 300、600 ns 相差 < 1%；轴向、壁面、45° 三个方向的 R 在 t = 300 ns 相差 < 2%（否则在报告中给出剖面图）。
- `benchmark.csv` 含全部进程数与两种分区方式；`deviation_table.csv` 含全部 54 行。

## Not allowed
- 改 γ、环境状态、求解器、限制器或 CFL 以改善与图 3d 的吻合；只报告"最好"的能量。
- 用本卡结果修改 `data/params/registry.yaml` 或任何 v0 配置。
- 加入任何项目物理补丁；停止或干扰无关的 FDTD 作业。
